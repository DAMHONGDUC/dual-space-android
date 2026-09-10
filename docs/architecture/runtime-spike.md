# Runtime feasibility spike

## Goal

Run multiple independent instances of an arbitrary third-party Android game and render each instance inside a Parallel_app tab without root access or game modifications.

## Result

For the personal, sideload-only build, a virtual-app runtime can provide independent game data without a server. It still cannot use only public Android APIs and should not be treated as a Google Play release candidate.

On 2026-09-10, The Universe 4.7.1 was built from source and tested on a Pixel Tablet emulator running Android 15/ARM64 with `vn.tinhlinh.game2dhay`:

- The installed game was imported into virtual users 0 and 1.
- Both copies launched through separate proxy activities and separate guest processes.
- The libGDX game rendered its login screen inside the virtual runtime.
- Both proxy tasks and both guest processes remained present after switching between copies.

This proves the basic Parallel Space flow on the Android 15 emulator. It does not yet prove Android 16 compatibility, long-session stability, notifications, Google/Facebook sign-in, purchases, or anti-cheat compatibility.

The profile-launcher variant was validated on the Pixel 9 emulator: Parallel_app requested Android's managed-profile consent flow, became profile owner after consent, and Android created user 11 named Parallel_app. This proves one additional isolated installation can be created without a server; it does not enable in-tab embedding.

The no-server companion path was validated on a Pixel 9 emulator. Parallel_app used `LauncherApps` to open the same test package in user 0 and managed profile user 10; Android reported the second activity as running under user 10, proving independent profile execution. The game still opens as a separate Android task rather than inside the Parallel_app surface.

- Android isolates every application by UID and sandbox.
- Cross-application activity embedding requires the target application to opt in and trust the host certificate. Arbitrary games do not provide that opt-in.
- A work profile provides one additional isolated installation, but it is an enterprise-managed profile and does not allow the host to embed or control the game's activity.
- Private Space also creates an isolated installation, but its lifecycle belongs to the user and locked apps cannot continue foreground or background activity.
- Loading or modifying executable code to bypass these limits creates Google Play policy and game-integrity risk.

## Runtime candidates

### The Universe — selected for the personal prototype

- Apache-2.0 source is available and the project has recent Android 16/ARM64 work.
- The tested game can be installed and launched under two virtual users on Android 15.
- Upstream explicitly warns that game support is incomplete, so each target game must pass a compatibility test.

The upstream source and license attribution must remain recorded in the repository's third-party notice when this runtime is distributed.

### SpaceCore — not selected

- Its public repository exposes a suitable demo API, but the runtime itself is a closed AAR.
- The repository is older and its current Android 16 behavior could not be verified from source.

Source: https://github.com/FSpaceCore/SpaceCore

### VirtualApp — not selected

- It is the historical base for this category, but its public source has not been maintained for modern Android versions.

Source: https://github.com/asLody/VirtualApp

## Viable product paths

### Cloud runtime

Each session runs on a remote Android worker. The mobile app displays the video stream and sends touch/controller input. This supports true tabs and multiple simultaneous sessions, but requires GPU infrastructure, regional latency control, licensing, and materially higher operating cost.

### Companion workspace

The app becomes a compliant launcher and account/session dashboard. It can open games installed in personal or user-managed profiles but cannot keep arbitrary game UIs live inside its tabs. This is inexpensive and Play-friendly, but it does not satisfy simultaneous in-app farming.

## Rejected for the Play build

Do not implement hidden-API task embedding, APK rewriting, runtime code injection, memory hooks, anti-cheat bypass, or silent installation. These approaches are unreliable across Android releases and conflict with the agreed product safety boundary.

## Current decision

Continue with The Universe only in a clearly labeled personal/sideload flavor. Keep the Play-compatible companion build independent so the virtual runtime, broad package visibility, hidden-API hooks, and inherited permissions cannot leak into a Play artifact.

The runtime is now exposed behind Parallel_app's `VirtualGameRuntime` boundary; Compose does not call The Universe APIs directly. The integrated APK was smoke-tested on Android 15: the existing Open game action imported `vn.tinhlinh.game2dhay` into virtual user 0 and launched it through Parallel_app's own proxy task.

The remaining gate is the physical Android 16 tablet test, followed by migrating the temporary Original/Managed labels to explicit copy indexes and supporting more than two virtual users.

## Primary references

- https://developer.android.com/guide/components/fundamentals
- https://developer.android.com/develop/ui/views/layout/activity-embedding
- https://developer.android.com/work/guide
- https://developer.android.com/about/versions/15/features
- https://support.google.com/googleplay/android-developer/answer/16559646
