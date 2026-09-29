# Pending setup

The app is free and funded by a banner ad. There is no sign-in, account, or purchase. The following publisher-owned setup is required before a Play release. Do not commit credential files or secret values.

## AdMob

- Create the Android app and one banner ad unit in the publisher's AdMob account.
- Put the production IDs in `env/env.prod.properties` or protected Gradle properties: `ADMOB_APP_ID` and `ADMOB_BANNER_AD_UNIT_ID`. `prodRelease` fails while either is missing or still a Google test ID.
- Publish the GDPR/UMP consent message in AdMob Privacy & messaging; the app shows it before requesting ads.
- Use Google's test IDs (the fallback when `env/env.dev.properties` is empty) during development; never click production ads while testing.

## Store and release

- Configure the upload signing key and enroll in Play App Signing.
- Fill the owner fields at the top of `docs/privacy-policy.md` and publish it at a public HTTPS URL.
- Complete Data safety and the Ads declaration from the final release AAB.
- Declare the `specialUse` foreground service and review the sensitive permissions the engine declares for guest games.
