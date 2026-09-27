import unittest

from play_test_companion.device_checks import crash_detected, is_foreground, package_installed, parse_pid

PACKAGE = "com.dd.dual.space"


class DeviceChecksTest(unittest.TestCase):
    def test_installed_requires_exact_package_line(self) -> None:
        self.assertTrue(package_installed("package:com.dd.dual.space\npackage:com.dd.dual.space.dev\n", PACKAGE))
        self.assertFalse(package_installed("package:com.dd.dual.space.dev\n", PACKAGE))

    def test_foreground_requires_focus_on_exact_package(self) -> None:
        focused = "  mCurrentFocus=Window{1a2b u0 com.dd.dual.space/com.dd.dual.space.MainActivity}"
        prefix = "  mCurrentFocus=Window{1a2b u0 com.dd.dual.space.dev/com.dd.dual.space.MainActivity}"
        background = "  Window #3 Window{9f u0 com.dd.dual.space/com.dd.dual.space.MainActivity}\n  mCurrentFocus=Window{2 u0 com.android.launcher3/.Launcher}"
        self.assertTrue(is_foreground(focused, PACKAGE))
        self.assertFalse(is_foreground(prefix, PACKAGE))
        self.assertFalse(is_foreground(background, PACKAGE))

    def test_crash_detection_covers_java_native_and_anr(self) -> None:
        self.assertTrue(crash_detected("E AndroidRuntime: Process: com.dd.dual.space, PID: 42", PACKAGE))
        self.assertTrue(crash_detected("F DEBUG : pid: 42, tid: 42, name: main  >>> com.dd.dual.space <<<", PACKAGE))
        self.assertTrue(crash_detected("I am_anr  : [0,42,com.dd.dual.space,0,ANR in com.dd.dual.space]", PACKAGE))
        self.assertFalse(crash_detected("E AndroidRuntime: Process: com.dd.dual.space.dev, PID: 7", PACKAGE))

    def test_pid_parsing_handles_missing_process(self) -> None:
        self.assertEqual(42, parse_pid("42\n"))
        self.assertIsNone(parse_pid(""))


if __name__ == "__main__":
    unittest.main()
