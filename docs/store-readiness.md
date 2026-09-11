# Store readiness review

Reviewed on 2026-09-11. The current repository builds, but the full runtime artifact is not ready for Google Play submission.

## Release blockers

1. The merged The Universe manifest contributes hundreds of permission declarations, including `QUERY_ALL_PACKAGES`, `MANAGE_EXTERNAL_STORAGE`, SMS, call-log, background-location, microphone, camera, and package-install permissions. Google Play restricts several of these to narrow core use cases and declaration processes.
2. The native runtime includes hooking, hidden-API, spoofing, and anti-detection code. Even if the product does not expose cheat features, shipping those capabilities creates a material Device and Network Abuse review risk. Google Play explicitly prohibits bypassing Android sandbox protections and affecting other apps without authorization.
3. The Play artifact needs a separate flavor that excludes `packages/the_universe`. The Play version should use Android-managed-profile capabilities only; retain the full runtime for a separately distributed build after legal and security review.
4. Privacy and Data safety declarations cannot be finalized until the Play flavor and its final merged manifest are fixed.
5. Release signing, Play Console products, support contact, public privacy-policy URL, device testing, and native-language review remain incomplete.

## Current strengths

- Targets API 37, above the Google Play API 36 requirement effective 2026-08-31.
- Uses a feature-first clean architecture for application code and keeps low-level runtime modules under `packages/`.
- Has adaptive Compose grids, 48dp control targets, local-only workspace persistence, system/light/dark appearance, and 11 configured locales.
- Does not currently include ads, analytics, remote accounts, or backend collection in application code.

## Market position

Parallel Space advertises 100M+ installs, 24 languages, private/hidden apps, secure lock, quick switching, concurrent accounts, and a Pro tier. 2Accounts advertises 50M+ installs and monetizes unlimited clones plus Secret Zone and Security Lock. Both products reveal a mature market, but reviews repeatedly surface reliability, notification, login, pricing, and account-identification friction.

The best position is not “another unlimited cloner.” Position Parallel_app as the trustworthy game-account switcher: clear account identity, compatibility status per game, honest device limitations, no ads in the game-launch path, and privacy controls users can understand.

## Recommended paid roadmap

### Ship first

- Free: two accounts per game, limited monthly usage, system/light/dark theme, local-only data, and a compatibility report.
- Pro subscription: more profiles where the device/runtime supports them, unlimited usage, per-account custom icon/color/name, launch shortcuts, app lock, notification routing, and priority compatibility updates.
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
- Start pricing experiments only after retention and successful-launch metrics are trustworthy; a quota based only on local elapsed time is easy to reset and should not control paid entitlement.

## Sources

- Google Play Device and Network Abuse: https://support.google.com/googleplay/android-developer/answer/16559646
- Google Play target API requirements: https://support.google.com/googleplay/android-developer/answer/11926878
- Google Play SMS and Call Log permissions: https://support.google.com/googleplay/android-developer/answer/10208820
- Google Play All files access policy: https://support.google.com/googleplay/android-developer/answer/10467955
- Google Play subscriptions policy: https://support.google.com/googleplay/android-developer/answer/9900533
- Parallel Space listing: https://play.google.com/store/apps/details?id=com.lbe.parallel.intl
- 2Accounts listing: https://play.google.com/store/apps/details?id=com.excelliance.multiaccount
