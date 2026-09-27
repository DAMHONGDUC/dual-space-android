# Core release implementation progress

This is an execution record, not a replacement for the immutable
`2026-09-26-gitnexus-plan-app-core-release-improvements.md`.
The application is **not release-ready** and the full plan is **not complete**.

## Decisions preserved

- Application identity is now `com.dd.dual.space`; the user's identity commits are preserved.
- Engine Java and JNI compatibility contracts remain under `com.dd.the.universe`.
- Privacy Lock protects the workspace only. Guest games and Recent Apps must not be claimed as protected.
- Settings should provide email contact for support and account deletion requests. The destination email is still required from the owner; no address has been invented.
- Public deletion/support resources and publisher details remain release inputs. An email action alone does not establish Play compliance.
- No new subscription service, pricing change, store submission, or Git push has been performed.

## Implemented changes and remaining evidence

| Plan item | Implementation | Remaining work |
| --- | --- | --- |
| C01 | Guest-resume sequence acknowledgement instead of dispatch-only success | Device E2E; bound blocking Binder calls, not just the polling loop |
| C02 | Install defaults to failure; persistence failure cannot return success | Fault injection and transaction rollback in C07 |
| C03 | Main guest launch/install checks no longer fall back to host state | Device outage scenarios; unused fallback helpers still exist |
| C04 | Composite user/process keys; identity-checked death cleanup; reserved slots included | Multi-user process lifecycle and PID-reuse tests on Android |
| C05 | Existing initialization wait is bounded | **Unresolved:** provider initialization and other IPC still occur under the process registry lock |
| C06 | Synchronized cache; monotonic retry clock; stale death callback cannot clear a replacement; no health-check ping; death-link failure returns no service | Real Binder death/reconnect and concurrency tests; acquisition can still block |
| C12 | Java and native longest directory-prefix matching; suffix preserved; custom Java map independent of global trie | Android filesystem integration; Java virtual-path bypass still needs isolation review |
| C13 | Blacklisted Java paths return the whole original path | Dedicated Android blacklist regression coverage |
| C14 | Platform uncaught-exception handler retained | Device crash-reporting verification |
| C16–C17 | Automatic native property hook and unbounded property-copy implementation removed; JNI setter signature retained as a no-op | Device compatibility smoke test |
| C18 | Native rules own strings; JNI rule buffers released; rule updates synchronized | JNI allocation-failure and device stress coverage |
| C19 | Unsafe native replacement allocation removed; thread-local result lifetime documented; optional open mode read only when required; missing original function fails safely | Android open/O_TMPFILE smoke tests; dormant wrappers were not activated |

## Remaining implementation backlog

1. C07–C11: transactional installation/update, complete split/ABI extraction, failure-aware deletion, safe package/UID recovery, and workspace JSON corruption recovery.
2. C15: persisted per-install virtual identity with safe multi-process access and compatibility migration.
3. C20–C24: workspace operation serialization, readiness/error mapping, entitlement refresh, purchase outcome handling, and explicit offering/price presentation.
4. C25–C29: API 29 authentication compatibility, immediate workspace secure-window updates, auth/busy cleanup, shortcut lifecycle, and force-update result handling.
5. C30–C31: companion request validation/body limits and trustworthy device-run verdicts.
6. C32–C37: release configuration validation, locale parity, production logging, exported proxy validation, support/deletion entry point, and accurate release/privacy copy.
7. G01–G08: device/OEM compatibility, guest-isolation assessment, license review, signed release and 16 KB verification, sandbox billing, Play Console setup, performance measurements, and expanded core tests. Unit tests and debug native builds do not satisfy these gates.

Optional O-items remain deferred. Free/paid copy limits have not been changed.

## Verification performed on 2026-09-27

- Nine selected core JUnit tests passed: install result (1), trie (4), IO map (1), crash handler (1), stale process death (1), Binder cache (1).
- Eight selected app tests passed in the renamed `devDebug` variant: launch acknowledgement (2) and virtualized launcher (6).
- `scripts/test_native_io.sh` passed with AddressSanitizer and UndefinedBehaviorSanitizer, including owned-input mutation, longest prefix, sibling paths, repeated suffix, null input, replacement rules, and four concurrent workers.
- Native ownership test failed against the pre-fix implementation and passed after the fix. Earlier Java path and crash-handler tests also reproduced their pre-fix failures.
- `:the-universe-core:externalNativeBuildDebug` passed for arm64-v8a and armeabi-v7a. The build printed `fcntl(): Bad file descriptor`; runtime impact is unverified.
- The first app test command used an ambiguous task name; rerunning `:app:testDevDebugUnitTest` succeeded. The first native host command used a runtime without JNI headers; using the installed OpenJDK header directory succeeded.
- Core lint initially reported three `InlinedApi` warnings. API guards were corrected for receiver flags and batched location extras; the rerun produced an empty issues report. No lint suppression was added.
- Core lint is configured to check only `NewApi` and `InlinedApi`; a clean result is **not** a full static-analysis pass.
- Java compilation still reports the Java 8 annotation-processor/source 17 mismatch and deprecated/unchecked API notes. Gradle also reports deprecations.
- No device E2E, sandbox purchase, signed release bundle, or Play review has been completed by this execution.

## Local commits

- `6844b17`: fail-closed installation result.
- `94594c3`: guest launch acknowledgement.
- `dcf64b3`: Java path matching.
- `938734c`: preserve platform crash handling.
- `8c4ab5e`: process identity and virtual-user cleanup; C05 explicitly remains open.
- `f83d60c`: Binder cache synchronization and stale callback protection.
- `ac258ed`: native path ownership, sanitizer tests, and removal of automatic property spoofing.

GitNexus gates use the pinned 1.6.12 distribution with schema-4 runner receipts.
Its graph has known dispatch/receiver-resolution and process-trace limits;
zero reported affected flows is not proof of no runtime impact.

## Branch `release/core-remaining-items` (2026-09-27, second pass)

The application is still **not release-ready**. This pass closed app-layer items only; engine items and all G-gates remain open.

| Item | Change | Evidence | Remaining |
| --- | --- | --- | --- |
| C32 | `preProdReleaseBuild` depends on `validateProdReleaseConfiguration`; blank keys, Google test AdMob IDs, or missing `google-services.json` fail with key names only | Ran against this machine: fails listing 5 problems; `devDebug`/`devRelease` still build | Owner supplies production values |
| C23, C27 | `PurchaseOutcome` separates activated/not activated/pending/cancelled/failed; auth, restore, sign-out release the busy flag in `finally`; restore failure no longer says "not found" | `PurchaseOutcomeTest` (3) | Sandbox purchase (G05) |
| C11 | Per-record session parsing; invalid, duplicate, negative-user and unknown-enum records are skipped; original payload is backed up once under `sessions_recovery_backup`; state is published only after `commit()` succeeds | `PersistentWorkspaceRepositoryTest` (3) on emulator API 36 | Recovery UI to re-import the backup |
| C34 | Release builds log only event names and error types | Compiles in `devRelease` | Verify on release artifact |
| C29 | `startUpdateFlowForResult == false`, start exceptions, and check failures fail open instead of looping or blocking | Compiles | Device test with internal app sharing |
| C25, C26 | API 29 uses `BIOMETRIC_WEAK or DEVICE_CREDENTIAL` (lock gates UI only, no keys); FLAG_SECURE follows the toggle immediately through a preference listener | Compiles | API 29 device test |
| C28 | Blank sessions skipped, launcher limit respected, removed pinned shortcuts disabled | `AndroidWorkspaceShortcutPublisherTest` (2) on emulator | — |
| C20, C22 | Per-copy in-flight set blocks launch/delete races; results for deleted copies are dropped; entitlement refreshes on resume, latest wins, unknown never overwrites known | Compiles | ViewModel test harness does not exist yet |
| C33 | All 10 locales match English keys; copy limit text uses `GameCopyLimits.maximumCopiesPerGame` (was hard-coded "2" while the code allows 5) | Resource merge passes | Native-speaker review |
| C36 | Settings → Delete account: confirmation, Firebase deletion, re-authentication with the same Google account when required, RevenueCat logout | Compiles | Public web deletion URL and support email (owner) |
| C37 | Privacy policy rewritten for the virtualization engine, diagnostics, and deletion | Doc only | Owner legal fields and HTTPS URL |

Test infrastructure finding: all 13 Compose UI tests fail on the API 36 emulator with `NoSuchMethodException: InputManager.getInstance`. This comes from the transitive Espresso version, not from these changes; data-layer instrumented tests pass.

Still open: C05, C07–C10, C15, C21, C24 (needs the owner's choice of products to sell), C30, C31, C35, G01–G08.

## Third pass: engine and remaining code items (2026-09-27)

Every code item in C01–C37 now has a change on this branch. The release still depends on owner setup and on G01, G03, G05 and G06, which need real devices, legal review or console access.

| Item | Change | Evidence |
| --- | --- | --- |
| C35 | Proxy activities, services and receiver are no longer exported; job proxies keep `BIND_JOB_SERVICE` | `ProxyComponentExposureTest` fails on the old manifest and passes now; guest launch E2E passes |
| C07 | Package binaries are staged beside the live directory and swapped in with renames; interrupted swaps are recovered at boot; only a version change stops every copy | `PackageDirectoryTransactionTest` (5); device test: adding copy 2 keeps copy 1 running with its data |
| C08 | Missing splits and failed library copies fail the install; libraries are always overwritten and length-checked; URI installs throw on short reads | Covered by the transaction tests and E2E install |
| C09 | Partial data removal keeps the copy registered, so its slot is never reused and it can be retried | Device test with a read-only directory: uninstall fails, retry succeeds |
| C10 | Unreadable `uid.conf` is preserved, not deleted; the uid allocator is advanced past loaded app ids; broken packages are quarantined; a missing original game no longer deletes its copies; the reused-uid lookup returns a real uid | `PackageRecoveryTest` (4) |
| C05 | Process initialization runs outside `mProcessLock`; concurrent starts share one process; waits are bounded; failures free the slot | `ProcessStartConcurrencyTest` (4), stable over 3 runs; E2E passes |
| C15 | New virtual users get a random persisted Android ID from the server process; users with existing copies keep the legacy value | `VirtualDeviceIdentityTest` (3) |
| C15 (found while testing) | Guests read the **host app's real ANDROID_ID** through `Settings.Secure`: the two proxy classes meant to hook it never injected. The settings provider stub now answers `GET_secure android_id` per user | E2E asserts each copy's ID differs from the host, from the other copy, and from the legacy value |
| Running state | `isRunningApplication` read a local service that only exists in the server process, so it always returned false; it now asks the server over Binder | E2E `isRunning` assertion |
| C21 | Runtime failures carry `FailureKind`; the UI shows engine unavailable, install failed, timed out, or game missing instead of "permission denied" | `VirtualizedGameLauncherTest` (+4) |
| C24 | Offer is chosen annual → monthly → lifetime regardless of remote order; the button shows price and period plus a renewal note; purchase buys only the displayed package identifier | `PremiumOfferTest` (3), `SettingsDialogTest.upgradeButtonShowsThePriceItWillCharge` |
| C30 | Companion server rejects foreign Host/Origin, requires a custom header on POST, and caps bodies at 16 KB | `test_request_guard.py` (5) |
| C31 | Exact package and focus matching, cleared log buffers, crash-buffer and ANR detection, PID continuity, failed log reads no longer pass | `test_device_checks.py` (4); focus parser checked against real API 36 output |
| Release build | R8 full mode crashed the app at startup (WorkManager's Room constructor) and broke the engine (`universeproxy` interfaces lost their annotations). Both keep rules added; `scripts/release_smoke.sh` guards startup | Signed `devRelease` starts, adds and opens a guest from the UI on the emulator |
| Test infra | Espresso pinned to 3.7 so Compose UI tests run on API 36 | 27/27 instrumented tests pass |
| G04 (partial) | All packaged `.so` files have 16 KB LOAD alignment and the APK passes `zipalign -c -P 16` | Checked on the `devRelease` APK; a signed prod AAB is still required |

Test totals at the end of this pass: core JVM 31, app JVM 27 per flavor, instrumented 27 (including 6 engine E2E), companion Python 15, release smoke passed. All runs were on the Pixel 9 Pro API 36 emulator; no physical or OEM device was used.
