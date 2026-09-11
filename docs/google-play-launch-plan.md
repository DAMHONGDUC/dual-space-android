# Google Play launch plan

Reviewed on 2026-09-11. Only the `prod` flavor is eligible for Google Play.

## Product position

Parallel Game Space is a privacy-first game account switcher that uses Android's personal and managed profiles. It does not embed, modify, automate, or bypass protections in third-party games.

Do not describe the Play build as an unlimited app cloner. The Play build opens apps that are already installed in an Android profile, and device support varies by manufacturer and Android version.

## Launch goals

### Gate 1: internal testing

- Upload a signed `prodRelease` Android App Bundle.
- Complete the main store listing, privacy-policy URL, Data safety form, content rating, target audience, app access, and ads declaration.
- Add review instructions and a short video showing profile consent, profile creation, game selection, launch, and profile removal.
- Test the full personal/managed-profile flow on certified physical devices running Android 13 through Android 16.

### Gate 2: closed testing

- Verify profile creation, installation discovery, launch, process recreation, upgrade, and profile removal.
- Track successful launch rate by a privacy-preserving manual test sheet until consented diagnostics exist.
- Resolve every reproducible crash, ANR, and setup dead end before expanding the tester group.
- If the developer account is a personal account created after 2023-11-13, keep at least 12 testers opted in continuously for 14 days before applying for production access.

### Gate 3: production

- Release gradually after the closed-test requirements and policy review are complete.
- Start with device exclusions for models where managed-profile creation or cross-profile launching fails.
- Use `dev` for local testing only; it mirrors `prod` behavior but has a separate application ID.

## Release blockers

1. The current release bundle is not configured with the publisher's upload signing key.
2. The privacy policy is still a draft and has no publisher identity, support contact, retention details, or public HTTPS URL.
3. Store icon, feature graphic, screenshots, short description, full description, and support page are not complete.
4. The complete managed-profile journey has not been proven on certified physical devices across supported Android versions.
5. The review team needs an accurate explanation of why the app becomes a profile owner and how users remove the profile and its data.

## Market findings

- Parallel Space reports 100M+ installs, 24 languages, two free accounts, app hiding, security lock, and a Pro tier. Its listing and reviews expose demand for clearer account identity, fewer launch failures, reliable notifications, and less intrusive monetization.
- Multiple Accounts reports 50M+ installs and competes on broad app compatibility, simultaneous accounts, Secret Zone, and Security Lock.
- Both major competitors now state that apps declaring `REQUIRE_SECURE_ENV` are unsupported. Parallel Game Space must preserve this boundary in every distribution.

The defensible wedge is trust and game-specific reliability rather than clone count: clear profile identity, honest compatibility status, local-only state, no launch-path ads, and actionable setup diagnostics.

## Feature priorities

## Monetization and identity

- Free users can launch games without a time limit and see a small banner outside the launch path.
- Premium removes every advertising surface while RevenueCat reports the `premium` entitlement as active.
- Google sign-in identifies the user across reinstalls and devices; it does not prove a purchase by itself.
- After Firebase sign-in, the app calls RevenueCat `logIn` with the Firebase UID and derives access only from the active `premium` entitlement in `CustomerInfo`.
- RevenueCat owns store receipt validation and entitlement state. The client must not grant Premium from Firestore writes, SharedPreferences, an email address, or a raw Billing callback.
- RevenueCat configuration must handle offerings, pending purchases, renewals, grace periods, account hold, cancellation, expiry, refunds, revocations, restore behavior, and anonymous-account merging.
- RevenueCat webhooks are optional for the client experience but recommended when server-side sync, customer support automation, or independent audit history is needed.
- Banner ads stay outside the launch action. Never insert an interstitial between tapping a game and opening it.
- Ad requests start only after the UMP consent state permits them; expose privacy options whenever UMP requires an entry point.
- Releases with Play update priority 4 or 5 trigger the blocking Immediate Update flow. Lower-priority releases never block startup.

### Before production

1. Compatibility check: the catalog shows personal-profile games, marks whether each game is already available in the managed profile, and blocks dead sessions. Neither flavor claims an unavailable game is ready or imports it automatically.
2. Setup guide: explain how to install the game in the second Android profile and how removal deletes that profile's apps and data.
3. Diagnostic export: the user-reviewed report contains Android version, device model, profile status, and categorized failures without account names or game data.
4. Recovery UX: detect interrupted provisioning, stale sessions, missing games, and process recreation, then present a specific recovery action.
5. Account identity: each account has a local label, stable color, last-opened timestamp, and confirmation before launch.

## Release acceptance criteria

- Setup completion: a new tester can create the second space, install a game there, rescan, add it, and launch it without developer help.
- Reliability: at least 95% successful launches in the closed-test device matrix, with every failure mapped to a recovery action.
- Quality: no reproducible crash or ANR in the core setup/add/launch/remove journey; lint and scoped tests pass for `prodRelease`.
- Trust: every visible capability is real, the app never displays fabricated performance values, and profile removal consequences are explained before consent.
- Policy: neither app flavor contains The Universe runtime or restricted permissions; Data safety matches Firebase, RevenueCat, and AdMob behavior.

### After stable launch

1. Extend the current four per-account launcher shortcuts with pinning and user-selected ordering.
2. Extend the current biometric/device-credential Privacy Lock with optional hiding of account labels in recents.
3. Compatibility Center with tested device/game/Android combinations and known limitations.
4. Local backup/export that never includes game credentials or game data.
5. Paid convenience features only after successful-launch and retention metrics are trustworthy.

Do not add gameplay macros, auto-clicking, fake GPS, device-identity spoofing, anti-cheat bypass, root hiding, ad interference, or executable-code downloads.

## Console checklist

- App details: production name without an underscore, default language, app/game category, free/paid choice, and verified support contact.
- Store listing: localized text, 512 px icon, 1024 x 500 feature graphic, phone screenshots, and tablet screenshots.
- App content: privacy policy, Data safety, ads, app access, target audience, content rating, and device-policy review instructions.
- Release: signed AAB, Play App Signing enrollment, release notes, pre-launch report, device catalog review, and staged rollout settings.
- Quality: Android vitals monitoring, crash/ANR alerts, feedback channel, and documented rollback criteria.

## Primary sources

- Google Play Device and Network Abuse: https://support.google.com/googleplay/android-developer/answer/16559646
- Google Play Data safety: https://support.google.com/googleplay/android-developer/answer/10787469
- Google Play testing requirements: https://support.google.com/googleplay/android-developer/answer/14151465
- Google Play app setup: https://support.google.com/googleplay/android-developer/answer/9859152
- Google Play review preparation: https://support.google.com/googleplay/android-developer/answer/9859455
- Android Enterprise DPC guide: https://developer.android.com/work/dpc/build-dpc
- Parallel Space listing: https://play.google.com/store/apps/details?id=com.lbe.parallel.intl
- Multiple Accounts listing: https://play.google.com/store/apps/details?id=com.excelliance.multiaccounts
