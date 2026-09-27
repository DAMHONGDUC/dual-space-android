# Pending setup

The following publisher-owned configuration is required before authentication, advertising, and Premium can be enabled in a release build. Do not commit credential files or secret values.

## Firebase

- Create the Android app `com.dd.dual.space` in the publisher's Firebase project.
- Add the upload and Play App Signing SHA-1 and SHA-256 certificate fingerprints.
- Enable Google as a Firebase Authentication provider.
- Download `google-services.json` to `app/google-services.json`; this path is gitignored.
- Use the Firebase UID as the non-guessable RevenueCat App User ID after sign-in.

## RevenueCat and Google Play Billing

- Create a RevenueCat project and Android app, then store its public SDK key in local release configuration.
- Create the Premium subscription and base plans in Play Console, then import the products into RevenueCat.
- Create the `premium` entitlement, attach the products, and configure a current Offering.
- Link Google Play service credentials to RevenueCat and configure Google Real-time Developer Notifications.
- Decide RevenueCat restore behavior before testing account changes; verify anonymous-to-Firebase-UID merge and transfer scenarios.
- Configure authenticated RevenueCat webhooks only if server-side entitlement sync, support automation, or audit history is required.

## AdMob

- Create the Android app and a banner ad unit in the publisher's AdMob account.
- Record the production AdMob app ID and banner ad unit ID in local release configuration.
- Use Google's demo banner unit or registered test devices during development; never click production ads while testing.

## Store and release

- Configure the upload signing key and enroll in Play App Signing.
- Publish the privacy policy at a public HTTPS URL and update it for Firebase Authentication, purchase verification, and advertising data practices.
- Complete Data safety and the Ads declaration from the final SDK inventory and release AAB.
