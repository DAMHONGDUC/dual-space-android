# Play Test Companion

Local Android quality-test orchestrator for a 14-day Google Play closed-test campaign.
It does not create testers or interact with Google accounts. The Play requirement still
needs at least 12 real people who remain opted in continuously.

## Start

1. Install Android platform tools so `adb` is available.
2. Enable USB debugging and connect one or more Android devices.
3. Install the closed-test build of Dual Space on each device.
4. Double-click `run.command`, then select **Bắt đầu / làm lại**.

The dashboard is available at <http://127.0.0.1:8765>. Test state, logs, screenshots,
and crash extracts are stored in `data/`, which is intentionally ignored by Git.

## Add another app

Copy this directory and change `app_name` and `package_name` in `config.json`.
The remaining settings control campaign length, test interval, launch wait, and the
number of safe Android Monkey interaction events.
