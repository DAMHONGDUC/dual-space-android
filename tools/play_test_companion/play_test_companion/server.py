from __future__ import annotations

import csv
import io
import json
import logging
import mimetypes
import webbrowser
from http import HTTPStatus
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path
from urllib.parse import urlparse

from .request_guard import reject_reason
from .service import CompanionService

LOGGER = logging.getLogger(__name__)
HOST = "127.0.0.1"
PORT = 8765


class CompanionHandler(BaseHTTPRequestHandler):
    service: CompanionService
    static_root: Path

    def do_GET(self) -> None:
        path = urlparse(self.path).path
        LOGGER.info("http_get path=%s", path)
        if self._rejected("GET"):
            return
        if path == "/api/status":
            self._json(self.service.status())
            return
        if path == "/api/report":
            self._json(self.service.report(), download_name="play-test-report.json")
            return
        if path == "/api/report.csv":
            self._csv_report()
            return
        self._static(path)

    def do_POST(self) -> None:
        path = urlparse(self.path).path
        LOGGER.info("http_post path=%s", path)
        if self._rejected("POST"):
            return
        try:
            if path == "/api/campaign/start":
                self.service.start_campaign()
            elif path == "/api/campaign/stop":
                self.service.stop_campaign()
            elif path == "/api/run":
                self.service.run_now()
            elif path == "/api/testers":
                self.service.add_tester(self._read_json())
            elif path.startswith("/api/testers/"):
                self.service.remove_tester(int(path.rsplit("/", 1)[1]))
            else:
                self.send_error(HTTPStatus.NOT_FOUND)
                return
            self._json({"ok": True})
        except Exception as exception:
            LOGGER.exception("http_post_failed path=%s", path)
            self._json({"ok": False, "error": str(exception)}, HTTPStatus.BAD_REQUEST)

    def log_message(self, format: str, *args: object) -> None:
        LOGGER.info("http_access message=%s", format % args)

    def _rejected(self, method: str) -> bool:
        reason = reject_reason(method, self.headers, PORT)
        if reason is None:
            return False
        LOGGER.warning("http_rejected method=%s reason=%s", method, reason)
        status = HTTPStatus.REQUEST_ENTITY_TOO_LARGE if reason == "body_too_large" else HTTPStatus.FORBIDDEN
        self.send_error(status)
        return True

    def _read_json(self) -> dict[str, object]:
        length = int(self.headers.get("Content-Length", "0"))
        return json.loads(self.rfile.read(length) or b"{}")

    def _json(
        self,
        payload: object,
        status: HTTPStatus = HTTPStatus.OK,
        download_name: str | None = None,
    ) -> None:
        body = json.dumps(payload, ensure_ascii=False, indent=2).encode("utf-8")
        self.send_response(status)
        self.send_header("Content-Type", "application/json; charset=utf-8")
        self.send_header("Content-Length", str(len(body)))
        if download_name:
            self.send_header("Content-Disposition", f'attachment; filename="{download_name}"')
        self.end_headers()
        self.wfile.write(body)

    def _static(self, path: str) -> None:
        relative_path = "index.html" if path == "/" else path.lstrip("/")
        candidate = (self.static_root / relative_path).resolve()
        if self.static_root.resolve() not in candidate.parents and candidate != self.static_root.resolve():
            self.send_error(HTTPStatus.FORBIDDEN)
            return
        if not candidate.is_file():
            self.send_error(HTTPStatus.NOT_FOUND)
            return
        body = candidate.read_bytes()
        self.send_response(HTTPStatus.OK)
        self.send_header("Content-Type", mimetypes.guess_type(candidate.name)[0] or "application/octet-stream")
        self.send_header("Content-Length", str(len(body)))
        self.end_headers()
        self.wfile.write(body)

    def _csv_report(self) -> None:
        output = io.StringIO()
        fields = ["started_at", "finished_at", "device_serial", "device_model", "status", "checks", "artifact_directory", "error"]
        writer = csv.DictWriter(output, fieldnames=fields)
        writer.writeheader()
        for run in self.service.runs():
            row = {key: run.get(key) for key in fields}
            row["checks"] = json.dumps(row["checks"], ensure_ascii=False)
            writer.writerow(row)
        body = output.getvalue().encode("utf-8")
        self.send_response(HTTPStatus.OK)
        self.send_header("Content-Type", "text/csv; charset=utf-8")
        self.send_header("Content-Disposition", 'attachment; filename="play-test-runs.csv"')
        self.send_header("Content-Length", str(len(body)))
        self.end_headers()
        self.wfile.write(body)


def main() -> None:
    root = Path(__file__).resolve().parents[1]
    log_directory = root / "data"
    log_directory.mkdir(parents=True, exist_ok=True)
    logging.basicConfig(
        level=logging.INFO,
        format="%(asctime)s %(levelname)s %(name)s %(message)s",
        handlers=[logging.FileHandler(log_directory / "companion.log"), logging.StreamHandler()],
    )
    CompanionHandler.service = CompanionService(root)
    CompanionHandler.static_root = root / "static"
    server = ThreadingHTTPServer((HOST, PORT), CompanionHandler)
    url = f"http://{HOST}:{PORT}"
    LOGGER.info("server_started url=%s", url)
    webbrowser.open(url)
    try:
        server.serve_forever()
    except KeyboardInterrupt:
        LOGGER.info("server_stopped reason=keyboard_interrupt")
    finally:
        server.server_close()


if __name__ == "__main__":
    main()
