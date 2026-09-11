# Pending setup

- Reconnect the Lenovo TB710FU and run the The Universe ARM64 spike on Android 16.
- Validate two-account login, 30-minute simultaneous survival, audio focus, background resume, network reconnect, and storage on the target game.
- Keep the The Universe runtime in a personal/sideload build flavor; do not include it in a Google Play artifact.
- Create a Play-safe product flavor that excludes native hooks, hidden APIs, permission mirroring, and the The Universe dependency.
- Reduce the release manifest to permissions demonstrably required by the Play build and verify it from the signed AAB.
- Have native speakers review ES, PT-BR, FR, DE, ID, HI, JA, KO, and zh-CN store copy and in-app translations.
- Choose the supported game allowlist before promoting third-party APK execution beyond prototype status.
- Configure Play Console signing, billing products, privacy policy, Data safety, and tester tracks.
- Supply publisher identity, support contact, public privacy-policy URL, and legal review.
- Decide whether Premium and Play Billing remain in scope for the no-server companion product.
- Replace prototype session metrics and quota storage with trusted runtime and backend sources.
- Decide the production application ID before publishing.
