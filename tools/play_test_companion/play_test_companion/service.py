from __future__ import annotations

import json
import logging
import threading
from dataclasses import replace
from datetime import datetime, timezone
from pathlib import Path
from typing import Any

from .adb_runner import AdbRunner
from .domain import AppProfile, new_campaign
from .storage import CompanionStorage

LOGGER = logging.getLogger(__name__)


class CompanionService:
    def __init__(self, root: Path) -> None:
        self._root = root
        self._profile = self._load_profile(root / "config.json")
        self._storage = CompanionStorage(root / "data" / "companion.sqlite3")
        self._runner = AdbRunner(self._profile, root / "data" / "artifacts")
        self._lock = threading.Lock()
        self._timer: threading.Timer | None = None
        self._run_in_progress = False
        self._restore_schedule()

    def status(self) -> dict[str, Any]:
        campaign = self._storage.load_campaign()
        tester_summary = self._storage.tester_summary()
        return {
            "profile": self._profile.__dict__,
            "campaign": None if campaign is None else {
                "started_at": campaign.started_at.isoformat(),
                "ends_at": campaign.ends_at.isoformat(),
                "running": campaign.running,
                "remaining_seconds": campaign.remaining_seconds,
            },
            "devices": self._safe_devices(),
            "summary": self._storage.run_summary(),
            "runs": self._storage.list_runs(50),
            "tester_summary": {key: value for key, value in tester_summary.items() if key != "testers"},
            "testers": tester_summary["testers"],
            "run_in_progress": self._run_in_progress,
        }

    def start_campaign(self) -> None:
        campaign = new_campaign(self._profile.campaign_days)
        self._storage.save_campaign(campaign)
        LOGGER.info("campaign_started started_at=%s ends_at=%s", campaign.started_at, campaign.ends_at)
        self._schedule(0)

    def stop_campaign(self) -> None:
        campaign = self._storage.load_campaign()
        if campaign is not None:
            self._storage.save_campaign(replace(campaign, running=False))
        self._cancel_timer()
        LOGGER.info("campaign_stopped")

    def run_now(self) -> None:
        threading.Thread(target=self._run_cycle, name="test-cycle", daemon=True).start()

    def add_tester(self, payload: dict[str, Any]) -> int:
        label = str(payload.get("label", "")).strip()
        if not label:
            raise ValueError("Tester label is required")
        opted_in_at = str(payload.get("opted_in_at", "")).strip() or None
        notes = str(payload.get("notes", "")).strip()
        return self._storage.add_tester(label, opted_in_at, notes)

    def remove_tester(self, tester_id: int) -> None:
        self._storage.remove_tester(tester_id)

    def report(self) -> dict[str, Any]:
        return {
            "generated_at": datetime.now(timezone.utc).isoformat(),
            "notice": "Automated QA evidence does not replace Google Play's real opted-in tester requirement.",
            **self.status(),
        }

    def runs(self) -> list[dict[str, Any]]:
        return self._storage.list_runs(10000)

    def _restore_schedule(self) -> None:
        campaign = self._storage.load_campaign()
        if campaign is not None and campaign.running and campaign.remaining_seconds > 0:
            LOGGER.info("campaign_restored ends_at=%s", campaign.ends_at.isoformat())
            self._schedule(5)

    def _schedule(self, delay_seconds: int) -> None:
        self._cancel_timer()
        self._timer = threading.Timer(delay_seconds, self._run_cycle)
        self._timer.daemon = True
        self._timer.start()
        LOGGER.info("test_cycle_scheduled delay_seconds=%d", delay_seconds)

    def _cancel_timer(self) -> None:
        if self._timer is not None:
            self._timer.cancel()
            self._timer = None

    def _run_cycle(self) -> None:
        if not self._lock.acquire(blocking=False):
            LOGGER.info("test_cycle_skipped reason=already_running")
            return
        self._run_in_progress = True
        try:
            devices = self._safe_devices()
            LOGGER.info("test_cycle_start device_count=%d", len(devices))
            for device in devices:
                self._storage.record_run(self._runner.run_device(device))
            LOGGER.info("test_cycle_finish device_count=%d", len(devices))
        except Exception:
            LOGGER.exception("test_cycle_failed")
        finally:
            self._run_in_progress = False
            self._lock.release()
            campaign = self._storage.load_campaign()
            if campaign is not None and campaign.running and campaign.remaining_seconds > 0:
                self._schedule(self._profile.run_interval_minutes * 60)

    def _safe_devices(self) -> list[dict[str, str]]:
        try:
            return self._runner.devices()
        except Exception:
            LOGGER.exception("device_discovery_failed")
            return []

    @staticmethod
    def _load_profile(path: Path) -> AppProfile:
        LOGGER.info("profile_load path=%s", path)
        values = json.loads(path.read_text(encoding="utf-8"))
        profile = AppProfile(**values)
        LOGGER.info("profile_loaded app=%s package=%s", profile.app_name, profile.package_name)
        return profile
