import unittest

from play_test_companion.request_guard import MAX_BODY_BYTES, REQUEST_HEADER, reject_reason

PORT = 8765


def post_headers(**overrides: str) -> dict[str, str]:
    headers = {"Host": f"127.0.0.1:{PORT}", REQUEST_HEADER: "1", "Content-Length": "10"}
    headers.update(overrides)
    return headers


class RequestGuardTest(unittest.TestCase):
    def test_page_request_from_the_companion_ui_is_allowed(self) -> None:
        self.assertIsNone(reject_reason("POST", post_headers(Origin=f"http://127.0.0.1:{PORT}"), PORT))
        self.assertIsNone(reject_reason("GET", {"Host": f"localhost:{PORT}"}, PORT))

    def test_rebound_host_is_rejected(self) -> None:
        self.assertEqual("host_not_allowed", reject_reason("GET", {"Host": f"attacker.example:{PORT}"}, PORT))
        self.assertEqual("host_not_allowed", reject_reason("POST", post_headers(Host="attacker.example"), PORT))

    def test_cross_site_post_is_rejected(self) -> None:
        self.assertEqual("origin_not_allowed", reject_reason("POST", post_headers(Origin="https://attacker.example"), PORT))

    def test_post_without_custom_header_is_rejected(self) -> None:
        headers = post_headers()
        del headers[REQUEST_HEADER]
        self.assertEqual("missing_request_header", reject_reason("POST", headers, PORT))

    def test_oversized_or_malformed_body_is_rejected(self) -> None:
        self.assertEqual("body_too_large", reject_reason("POST", post_headers(**{"Content-Length": str(MAX_BODY_BYTES + 1)}), PORT))
        self.assertEqual("invalid_content_length", reject_reason("POST", post_headers(**{"Content-Length": "-1"}), PORT))


if __name__ == "__main__":
    unittest.main()
