import unittest
from datetime import datetime, timedelta, timezone

from play_test_companion.domain import Campaign, new_campaign


class CampaignTest(unittest.TestCase):
    def test_new_campaign_ends_after_requested_days(self) -> None:
        campaign = new_campaign(14)

        self.assertEqual(timedelta(days=14), campaign.ends_at - campaign.started_at)
        self.assertTrue(campaign.running)

    def test_completed_campaign_has_no_negative_remaining_time(self) -> None:
        campaign = Campaign(
            started_at=datetime.now(timezone.utc) - timedelta(days=15),
            ends_at=datetime.now(timezone.utc) - timedelta(days=1),
            running=True,
        )

        self.assertEqual(0, campaign.remaining_seconds)


if __name__ == "__main__":
    unittest.main()
