from __future__ import annotations

from dataclasses import dataclass
from datetime import datetime, timedelta, timezone


@dataclass(frozen=True)
class AppProfile:
    app_name: str
    package_name: str
    campaign_days: int
    run_interval_minutes: int
    launch_wait_seconds: int
    monkey_events: int


@dataclass(frozen=True)
class Campaign:
    started_at: datetime
    ends_at: datetime
    running: bool

    @property
    def remaining_seconds(self) -> int:
        remaining = self.ends_at - datetime.now(timezone.utc)
        return max(0, int(remaining.total_seconds()))


def new_campaign(days: int) -> Campaign:
    started_at = datetime.now(timezone.utc)
    return Campaign(
        started_at=started_at,
        ends_at=started_at + timedelta(days=days),
        running=True,
    )
