from __future__ import annotations

import re

# Pure parsers for adb output, kept separate so each verdict rule is unit-testable.


def package_installed(pm_list_output: str, package_name: str) -> bool:
    """`pm list packages <filter>` matches substrings; only an exact line proves installation."""
    return any(line.strip() == f"package:{package_name}" for line in pm_list_output.splitlines())


def is_foreground(window_dump: str, package_name: str) -> bool:
    """True only when the focused window belongs to exactly this package, not a prefix of it."""
    focus_pattern = re.compile(r"mCurrentFocus=Window\{[^}]*\s" + re.escape(package_name) + r"/")
    return any(focus_pattern.search(line) for line in window_dump.splitlines() if "mCurrentFocus" in line)


def parse_pid(pidof_output: str) -> int | None:
    parts = pidof_output.split()
    return int(parts[0]) if parts and parts[0].isdigit() else None


def crash_detected(crash_buffer: str, package_name: str) -> bool:
    """Java crashes name the process; native crashes carry `>>> package <<<` in the tombstone."""
    for line in crash_buffer.splitlines():
        if f"Process: {package_name}," in line or f">>> {package_name} <<<" in line:
            return True
        if f"ANR in {package_name}" in line:
            return True
    return False
