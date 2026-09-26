import tempfile
import unittest
from pathlib import Path

from play_test_companion.domain import new_campaign
from play_test_companion.storage import CompanionStorage


class StorageTest(unittest.TestCase):
    def setUp(self) -> None:
        self._temporary_directory = tempfile.TemporaryDirectory()
        self._storage = CompanionStorage(Path(self._temporary_directory.name) / "test.sqlite3")

    def tearDown(self) -> None:
        self._temporary_directory.cleanup()

    def test_campaign_survives_storage_restart(self) -> None:
        campaign = new_campaign(14)
        self._storage.save_campaign(campaign)

        restored = CompanionStorage(Path(self._temporary_directory.name) / "test.sqlite3").load_campaign()

        self.assertEqual(campaign, restored)

    def test_records_run_and_summary(self) -> None:
        self._storage.record_run({
            "started_at": "2026-09-13T00:00:00+00:00",
            "finished_at": "2026-09-13T00:00:10+00:00",
            "device_serial": "device-1",
            "device_model": "Pixel",
            "status": "passed",
            "checks": {"installed": True},
            "artifact_directory": "/tmp/artifacts",
            "error": None,
        })

        self.assertEqual({"total": 1, "passed": 1, "failed": 0}, self._storage.run_summary())
        self.assertEqual({"installed": True}, self._storage.list_runs()[0]["checks"])

    def test_adds_and_removes_tester(self) -> None:
        tester_id = self._storage.add_tester("Tester 01", "2026-09-13", "Pixel 9")
        self.assertEqual("Tester 01", self._storage.list_testers()[0]["label"])

        self._storage.remove_tester(tester_id)

        self.assertEqual([], self._storage.list_testers())

    def test_tester_summary_marks_old_opt_in_as_eligible(self) -> None:
        self._storage.add_tester("Tester 01", "2020-01-01", "Pixel 9")

        summary = self._storage.tester_summary(required_count=1, required_days=14)

        self.assertEqual(1, summary["eligible_count"])
        self.assertTrue(summary["declared_ready"])
        self.assertTrue(summary["testers"][0]["eligible"])


if __name__ == "__main__":
    unittest.main()
