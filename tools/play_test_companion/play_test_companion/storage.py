from __future__ import annotations

import json
import logging
import sqlite3
from datetime import date, datetime, timedelta
from pathlib import Path
from typing import Any

from .domain import Campaign

LOGGER = logging.getLogger(__name__)
SCHEMA_VERSION = 1


class CompanionStorage:
    def __init__(self, path: Path) -> None:
        self._path = path
        self._path.parent.mkdir(parents=True, exist_ok=True)
        self._initialize()

    def _connect(self) -> sqlite3.Connection:
        connection = sqlite3.connect(self._path)
        connection.row_factory = sqlite3.Row
        return connection

    def _initialize(self) -> None:
        LOGGER.info("storage_initialize path=%s", self._path)
        with self._connect() as connection:
            connection.executescript(
                """
                CREATE TABLE IF NOT EXISTS metadata (
                    key TEXT PRIMARY KEY,
                    value TEXT NOT NULL
                );
                CREATE TABLE IF NOT EXISTS campaign (
                    id INTEGER PRIMARY KEY CHECK (id = 1),
                    started_at TEXT NOT NULL,
                    ends_at TEXT NOT NULL,
                    running INTEGER NOT NULL
                );
                CREATE TABLE IF NOT EXISTS test_runs (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    started_at TEXT NOT NULL,
                    finished_at TEXT NOT NULL,
                    device_serial TEXT NOT NULL,
                    device_model TEXT NOT NULL,
                    status TEXT NOT NULL,
                    checks_json TEXT NOT NULL,
                    artifact_directory TEXT,
                    error TEXT
                );
                CREATE TABLE IF NOT EXISTS testers (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    label TEXT NOT NULL,
                    opted_in_at TEXT,
                    notes TEXT NOT NULL DEFAULT ''
                );
                """
            )
            connection.execute(
                "INSERT OR REPLACE INTO metadata(key, value) VALUES('schema_version', ?)",
                (str(SCHEMA_VERSION),),
            )
        LOGGER.info("storage_initialized schema_version=%d", SCHEMA_VERSION)

    def save_campaign(self, campaign: Campaign) -> None:
        LOGGER.info("campaign_save running=%s ends_at=%s", campaign.running, campaign.ends_at.isoformat())
        with self._connect() as connection:
            connection.execute(
                """
                INSERT OR REPLACE INTO campaign(id, started_at, ends_at, running)
                VALUES(1, ?, ?, ?)
                """,
                (campaign.started_at.isoformat(), campaign.ends_at.isoformat(), int(campaign.running)),
            )

    def load_campaign(self) -> Campaign | None:
        with self._connect() as connection:
            row = connection.execute("SELECT * FROM campaign WHERE id = 1").fetchone()
        if row is None:
            return None
        return Campaign(
            started_at=datetime.fromisoformat(row["started_at"]),
            ends_at=datetime.fromisoformat(row["ends_at"]),
            running=bool(row["running"]),
        )

    def record_run(self, result: dict[str, Any]) -> int:
        LOGGER.info(
            "test_run_record device=%s status=%s",
            result["device_serial"],
            result["status"],
        )
        with self._connect() as connection:
            cursor = connection.execute(
                """
                INSERT INTO test_runs(
                    started_at, finished_at, device_serial, device_model, status,
                    checks_json, artifact_directory, error
                ) VALUES(?, ?, ?, ?, ?, ?, ?, ?)
                """,
                (
                    result["started_at"], result["finished_at"], result["device_serial"],
                    result["device_model"], result["status"], json.dumps(result["checks"]),
                    result.get("artifact_directory"), result.get("error"),
                ),
            )
            return int(cursor.lastrowid)

    def list_runs(self, limit: int = 100) -> list[dict[str, Any]]:
        with self._connect() as connection:
            rows = connection.execute(
                "SELECT * FROM test_runs ORDER BY id DESC LIMIT ?", (limit,)
            ).fetchall()
        return [self._run_from_row(row) for row in rows]

    def run_summary(self) -> dict[str, int]:
        with self._connect() as connection:
            rows = connection.execute(
                "SELECT status, COUNT(*) AS count FROM test_runs GROUP BY status"
            ).fetchall()
        counts = {str(row["status"]): int(row["count"]) for row in rows}
        return {"total": sum(counts.values()), "passed": counts.get("passed", 0), "failed": counts.get("failed", 0)}

    def add_tester(self, label: str, opted_in_at: str | None, notes: str) -> int:
        LOGGER.info("tester_add label=%s opted_in_at=%s", label, opted_in_at)
        with self._connect() as connection:
            cursor = connection.execute(
                "INSERT INTO testers(label, opted_in_at, notes) VALUES(?, ?, ?)",
                (label, opted_in_at, notes),
            )
            return int(cursor.lastrowid)

    def remove_tester(self, tester_id: int) -> None:
        LOGGER.info("tester_remove id=%d", tester_id)
        with self._connect() as connection:
            connection.execute("DELETE FROM testers WHERE id = ?", (tester_id,))

    def list_testers(self) -> list[dict[str, Any]]:
        with self._connect() as connection:
            rows = connection.execute("SELECT * FROM testers ORDER BY id").fetchall()
        return [dict(row) for row in rows]

    def tester_summary(self, required_count: int = 12, required_days: int = 14) -> dict[str, Any]:
        testers = self.list_testers()
        cutoff = date.today() - timedelta(days=required_days)
        eligible_count = 0
        for tester in testers:
            opted_in_at = tester.get("opted_in_at")
            try:
                is_eligible = bool(opted_in_at) and date.fromisoformat(str(opted_in_at)) <= cutoff
            except ValueError:
                LOGGER.exception("tester_opt_in_date_invalid tester_id=%s value=%s", tester["id"], opted_in_at)
                is_eligible = False
            tester["declared_continuous_days"] = (
                max(0, (date.today() - date.fromisoformat(str(opted_in_at))).days)
                if opted_in_at and self._is_iso_date(str(opted_in_at))
                else 0
            )
            tester["eligible"] = is_eligible
            eligible_count += int(is_eligible)
        return {
            "required_count": required_count,
            "required_days": required_days,
            "registered_count": len(testers),
            "eligible_count": eligible_count,
            "declared_ready": eligible_count >= required_count,
            "testers": testers,
        }

    @staticmethod
    def _run_from_row(row: sqlite3.Row) -> dict[str, Any]:
        result = dict(row)
        result["checks"] = json.loads(result.pop("checks_json"))
        return result

    @staticmethod
    def _is_iso_date(value: str) -> bool:
        try:
            date.fromisoformat(value)
            return True
        except ValueError:
            return False
