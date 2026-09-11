# Pending setup

The following publisher-owned configuration is required before authentication, advertising, and Premium can be enabled in a release build. Do not commit credential files or secret values.

## Firebase

- Create the Android app `com.duplicateapp.gamespace` in the publisher's Firebase project.
- Add the upload and Play App Signing SHA-1 and SHA-256 certificate fingerprints.
- Enable Google as a Firebase Authentication provider.
- Download `google-services.json` to `app/google-services.json`; this path is gitignored.
- Configure a backend endpoint that accepts a Firebase ID token and Google Play purchase token, verifies both, and returns the current entitlement.

## Google Play Billing

- Create the Premium subscription and its base plans in Play Console.
- Choose and record the final subscription product ID in local release configuration.
- Link the Google Cloud project and grant the backend access to the Google Play Developer API.
- Configure Real-time Developer Notifications and handle purchase, renewal, grace-period, hold, cancellation, expiry, refund, and revocation states.

## AdMob

- Create the Android app and a rewarded ad unit in the publisher's AdMob account.
- Choose the exact reward duration displayed to users before they opt in.
- Record the production AdMob app ID and rewarded ad unit ID in local release configuration.
- Use Google's demo rewarded unit or registered test devices during development; never click production ads while testing.

## Store and release

- Configure the upload signing key and enroll in Play App Signing.
- Publish the privacy policy at a public HTTPS URL and update it for Firebase Authentication, purchase verification, and advertising data practices.
- Complete Data safety and the Ads declaration from the final SDK inventory and release AAB.
