from __future__ import annotations

from collections.abc import Mapping

# Browsers cannot attach a custom header to a cross-site request without a CORS preflight, which
# this server never approves, so requiring it blocks drive-by POSTs from any open web page.
REQUEST_HEADER = "X-Companion-Request"
MAX_BODY_BYTES = 16 * 1024


def allowed_hosts(port: int) -> set[str]:
    return {f"127.0.0.1:{port}", f"localhost:{port}"}


def reject_reason(method: str, headers: Mapping[str, str], port: int) -> str | None:
    """Returns why a request must be refused, or None when it may proceed."""
    host = headers.get("Host", "")
    # A mismatched Host means DNS rebinding: a remote name resolving to this loopback server.
    if host not in allowed_hosts(port):
        return "host_not_allowed"
    if method != "POST":
        return None
    origin = headers.get("Origin")
    if origin is not None and origin not in {f"http://{allowed}" for allowed in allowed_hosts(port)}:
        return "origin_not_allowed"
    if headers.get(REQUEST_HEADER) != "1":
        return "missing_request_header"
    length = headers.get("Content-Length", "0")
    if not length.isdigit():
        return "invalid_content_length"
    if int(length) > MAX_BODY_BYTES:
        return "body_too_large"
    return None
