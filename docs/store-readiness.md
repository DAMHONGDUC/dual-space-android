# Store readiness review

Reviewed on 2026-09-11. Only the `prod` flavor is intended for Google Play; `dev` mirrors its managed-profile behavior with a separate application ID.

## Release blockers

1. Both flavors link `packages/the_universe`, and guest games can only use permissions the host declares. The engine manifest keeps a minimal set (network, Wi-Fi state, wake lock, vibrate, notifications, foreground service with `specialUse`); check the merged `prodRelease` manifest before every release so restricted permissions, including `com.android.vending.BILLING`, do not return.
2. Managed-profile provisioning makes the app a device policy controller. Store metadata, onboarding, and review notes must explain this core behavior accurately.
3. Privacy and Data safety declarations must be finalized from the release AAB and actual runtime behavior.
4. Release signing, support contact, public privacy-policy URL, device testing, and native-language review remain incomplete.

The `dev` flavor is not a Google Play artifact. Neither app flavor includes the experimental native container runtime.

## Current strengths

- Targets API 37, above the Google Play API 36 requirement effective 2026-08-31.
- Uses a feature-first clean architecture for application code and keeps low-level runtime modules under `packages/`.
- Has adaptive Compose grids, 48dp control targets, local-only workspace persistence, system/light/dark appearance, and 11 configured locales.
- Uses consent-gated AdMob banners, optional Firebase authentication, RevenueCat purchase validation, and Play immediate updates.
- Checks profile/game readiness before launch, confirms account identity, publishes account shortcuts, exports redacted diagnostics, and offers an opt-in device-authentication lock.

## Market position

Parallel Space advertises 100M+ installs, 24 languages, private/hidden apps, secure lock, quick switching, concurrent accounts, and a Pro tier. 2Accounts advertises 50M+ installs and monetizes unlimited clones plus Secret Zone and Security Lock. Both products reveal a mature market, but reviews repeatedly surface reliability, notification, login, pricing, and account-identification friction.

The best position is not “another unlimited cloner.” Position Dual Space as the trustworthy game-account switcher: clear account identity, compatibility status per game, honest device limitations, no ads in the game-launch path, and privacy controls users can understand.

## Recommended paid roadmap

### Ship first

- Free: two accounts per game, unrestricted launching, a small banner ad, system/light/dark theme, local-only data, and a compatibility report.
- Pro subscription: no ads, plus future convenience features such as custom account identity, shortcuts, app lock, notification routing, and priority compatibility updates.
- Lifetime purchase: offer this only for durable local features; recurring compatibility maintenance is better matched to a subscription.

### Highest-value differentiators

1. Compatibility Center: tested game/device/Android-version matrix, visible known issues, and one-tap diagnostic export with sensitive values removed.
2. Account identity: custom labels, colors, icons, last-used state, and confirmation before launching the wrong account.
3. Reliability: health check for Play Services, WebView, notifications, battery optimization, storage, and profile state before launch.
4. Privacy lock: biometric/PIN lock, hide sensitive account names in recents, and privacy-first local storage.
5. Smart shortcuts: direct launcher shortcuts for each game account and optional schedules/reminders that never automate gameplay.

Avoid selling fake GPS, device-identity spoofing, root hiding, anti-cheat bypass, macros, ad interference, or downloadable executable modules. These conflict with the product boundary and materially increase policy and account-enforcement risk.

## Monetization proposal

- Keep onboarding usable without a trial or payment wall.
- Test a monthly and annual Pro plan plus a clearly labeled lifetime purchase for local features.
- Gate convenience and scale, not privacy or basic stability.
- Show full billing amount, renewal cadence, trial conversion, and cancellation access in every supported locale.
- Start pricing experiments only after retention and successful-launch metrics are trustworthy; Premium sells an ad-free experience rather than access to basic launching.

## Sources

- Google Play Device and Network Abuse: https://support.google.com/googleplay/android-developer/answer/16559646
- Google Play target API requirements: https://support.google.com/googleplay/android-developer/answer/11926878
- Google Play SMS and Call Log permissions: https://support.google.com/googleplay/android-developer/answer/10208820
- Google Play All files access policy: https://support.google.com/googleplay/android-developer/answer/10467955
- Google Play subscriptions policy: https://support.google.com/googleplay/android-developer/answer/9900533
- Parallel Space listing: https://play.google.com/store/apps/details?id=com.lbe.parallel.intl
- 2Accounts listing: https://play.google.com/store/apps/details?id=com.excelliance.multiaccount
