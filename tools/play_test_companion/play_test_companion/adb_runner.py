from __future__ import annotations

import logging
import subprocess
import time
from datetime import datetime, timezone
from pathlib import Path
from typing import Any

from .domain import AppProfile

LOGGER = logging.getLogger(__name__)
COMMAND_TIMEOUT_SECONDS = 45


class AdbRunner:
    def __init__(self, profile: AppProfile, artifact_root: Path) -> None:
        self._profile = profile
        self._artifact_root = artifact_root

    def devices(self) -> list[dict[str, str]]:
        output = self._adb([], "devices", "-l").stdout
        devices: list[dict[str, str]] = []
        for line in output.splitlines()[1:]:
            if "\tdevice" not in line:
                continue
            serial = line.split()[0]
            model = next((part.split(":", 1)[1] for part in line.split() if part.startswith("model:")), "unknown")
            devices.append({"serial": serial, "model": model})
        LOGGER.info("devices_discovered count=%d serials=%s", len(devices), [item["serial"] for item in devices])
        return devices

    def run_device(self, device: dict[str, str]) -> dict[str, Any]:
        started_at = datetime.now(timezone.utc)
        serial = device["serial"]
        artifact_directory = self._artifact_root / started_at.strftime("%Y%m%dT%H%M%SZ") / serial.replace(":", "_")
        artifact_directory.mkdir(parents=True, exist_ok=True)
        checks: dict[str, bool] = {}
        error: str | None = None
        LOGGER.info("device_test_start serial=%s package=%s", serial, self._profile.package_name)
        try:
            packages = self._adb([serial], "shell", "pm", "list", "packages", self._profile.package_name).stdout
            checks["installed"] = f"package:{self._profile.package_name}" in packages
            if not checks["installed"]:
                raise RuntimeError("Package is not installed on the device")

            launch = self._adb(
                [serial], "shell", "monkey", "-p", self._profile.package_name,
                "-c", "android.intent.category.LAUNCHER", "1",
            )
            checks["launch_command"] = launch.returncode == 0
            time.sleep(self._profile.launch_wait_seconds)

            foreground = self._adb([serial], "shell", "dumpsys", "window", "windows").stdout
            checks["foreground"] = self._profile.package_name in foreground

            monkey = self._adb(
                [serial], "shell", "monkey", "-p", self._profile.package_name,
                "--throttle", "150", "--pct-syskeys", "0", str(self._profile.monkey_events),
            )
            checks["interaction"] = monkey.returncode == 0 and "Events injected" in monkey.stdout

            screenshot = self._adb([serial], "exec-out", "screencap", "-p", binary=True)
            screenshot_path = artifact_directory / "screenshot.png"
            screenshot_path.write_bytes(screenshot.stdout)
            checks["screenshot"] = screenshot_path.stat().st_size > 0

            crash_log = self._adb(
                [serial], "logcat", "-d", "-t", "500", "AndroidRuntime:E", "*:S",
                allow_failure=True,
            ).stdout
            (artifact_directory / "crash.log").write_text(crash_log, encoding="utf-8")
            checks["no_fatal_exception"] = "FATAL EXCEPTION" not in crash_log
        except Exception as exception:
            LOGGER.exception("device_test_failed serial=%s", serial)
            error = str(exception)

        status = "passed" if checks and all(checks.values()) and error is None else "failed"
        finished_at = datetime.now(timezone.utc)
        LOGGER.info("device_test_finish serial=%s status=%s checks=%s", serial, status, checks)
        return {
            "started_at": started_at.isoformat(),
            "finished_at": finished_at.isoformat(),
            "device_serial": serial,
            "device_model": device["model"],
            "status": status,
            "checks": checks,
            "artifact_directory": str(artifact_directory),
            "error": error,
        }

    def _adb(
        self,
        device: list[str],
        *arguments: str,
        binary: bool = False,
        allow_failure: bool = False,
    ) -> subprocess.CompletedProcess[Any]:
        command = ["adb"]
        if device:
            command.extend(["-s", device[0]])
        command.extend(arguments)
        LOGGER.info("adb_execute command=%s", command)
        result = subprocess.run(
            command,
            capture_output=True,
            text=not binary,
            timeout=COMMAND_TIMEOUT_SECONDS,
            check=False,
        )
        if result.returncode != 0 and not allow_failure:
            stderr = result.stderr.decode(errors="replace") if binary else result.stderr
            raise RuntimeError(f"ADB failed ({result.returncode}): {stderr.strip()}")
        return result
