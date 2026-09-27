# GitNexus Engineering Plan

> Task: Replace play-time quota/reward monetization with ads for Free and ad-free Premium; improve launch reliability and account UX.
> Evidence verified at commit 999d917948b2ed9105f35e165dfa4b4f16e0a2a6; GitNexus index refreshed this session with `--index-only --pdg`.
> Evidence provenance schema 2; global dirty digest 0a9c85780067d9afcd0764f307b60891e3cee927ee11eaeb5ec7826d10fd82cd; cited-path manifest 15 sorted entries; exact generated plan path excluded.

## 1. Objective

Ship a Play-safe game-account launcher where Free users see non-disruptive banner ads and Premium users see no ads, with no play-time quota or rewarded-ad flow.

## 2. Current Behaviour

- [verified] `WorkspaceViewModel.launch` blocks Free launches after a locally tracked monthly quota and starts quota timing after `GameLauncher.launch` succeeds.
- [verified] `settingsDialog` exposes quota, rewarded ads, and Premium as unlimited play time.
- [verified] `gameLibrary` emphasizes package names and generic profile labels instead of the stored session name.
- [verified] profile compatibility is represented by only three provisioning states and launch failures are shown through generic snackbar strings.

## 3. Relevant Architecture

- [verified] Feature dependencies follow `presentation -> domain <- data`; monetization crosses ads, premium, and workspace features.
- [verified] `WorkspaceViewModel` currently owns workspace, settings, profile, auth, purchase, quota, and reward orchestration.

## 4. GitNexus Findings

- [graph] `impact(WorkspaceViewModel, upstream)` reports one direct consumer and LOW risk.
- [graph] `impact(PremiumAccess, upstream)` reports eight direct consumers and MEDIUM risk; all `hasUnlimitedPlayTime` uses must migrate together.
- [graph] `impact(RewardedAdManager, upstream)` reports one direct consumer and LOW risk.
- [graph] Compose top-level function callers were unresolved; source search confirms `workspaceScreen` calls both `gameLibrary` and `settingsDialog`.

## 5. Statement-Level PDG Findings

The initial `launch` probe was ambiguous across seven methods; source verification establishes the load-bearing order: quota guard -> starting mutation -> launcher call -> opened/failed mutation. Removing the quota guard must preserve launch state ordering and error mapping.

## 6. Proposed Changes

1. Replace rewarded ads and quota configuration with an adaptive banner ad surface owned by the ads feature.
2. Reduce `PremiumAccess` to the single product benefit `removesAds`; update paywall copy and state checks.
3. Remove quota state, launch gating, rewarded-ad actions, quota tests, resources, and documentation.
4. Add a compatibility status model and diagnostic panel with actionable profile/game recovery.
5. Present stored session names as the primary account identity and remove developer-facing package labels.
6. Consolidate screen state and split factory construction from ViewModel orchestration where practical without changing public Java APIs.

## 7. Implementation Sequence

1. Monetization domain/config migration: Premium means no ads; Free remains unrestricted.
2. Ads UI migration: adaptive banner below the library, never in the launch path; Premium hides it.
3. Workspace UI migration: remove quota, show account names, add compatibility/recovery UI.
4. Architecture cleanup: consolidate screen state and reduce unrelated dependencies from `WorkspaceViewModel`.
5. Update localized resources, tests, README, store-readiness, and launch plan.
6. Run scoped unit tests, Android lint, compile, GitNexus detect-changes, and UI checklist.

## 8. Test Strategy

- Premium inactive -> banner eligible; Premium active -> banner absent.
- Free user -> launch is never quota-gated.
- Game row -> stored account names are visible and launch callbacks retain session IDs.
- Missing managed profile/game -> diagnostic gives the matching action.
- Verify with `./gradlew :app:testProdDebugUnitTest`, `./gradlew :app:lintProdDebug`, and `./gradlew :app:compileProdDebugKotlin`.

## 9. Risk and Impact Analysis

- MEDIUM: `PremiumAccess` has eight direct dependents; migrate atomically.
- MEDIUM: AdMob lifecycle must destroy `AdView` and must not render for Premium.
- MEDIUM: deleting quota persistence leaves harmless old preferences; do not add destructive migration.
- HIGH external: managed-profile behavior still requires certified physical-device validation.

## 10. Files Expected to Change

| Area | Reason |
| --- | --- |
| premium/ads | Change entitlement meaning and replace rewarded ad implementation |
| workspace presentation/domain | Remove quota state, add diagnostics, show account identity |
| resources/tests/docs/build config | Keep contracts, copy, verification, and release guidance aligned |

## 11. Reusable Implementation Context

```yaml
implementation_context:
  task_summary: "Replace quota/reward monetization with Free ads and ad-free Premium; improve account and compatibility UX."
  acceptance_criteria:
    - "Free launch has no time limit"
    - "Free UI can show a non-disruptive banner ad"
    - "Premium UI never creates the banner ad"
    - "No rewarded-ad or quota UI remains"
    - "Stored session names identify accounts"
  evidence_provenance:
    schema_version: 2
    head_commit: "999d917948b2ed9105f35e165dfa4b4f16e0a2a6"
    generated_plan_path: "docs/plans/2026-09-11-gitnexus-plan-ad-funded-premium-redesign.md"
    global_dirty_digest:
      algorithm: "sha256"
      canonicalization: "gitnexus-evidence-provenance-v2 NUL-framed UTF-8 records"
      value: "0a9c85780067d9afcd0764f307b60891e3cee927ee11eaeb5ec7826d10fd82cd"
    cited_path_manifest: []
  primary_symbols:
    - { symbol: "WorkspaceViewModel", file: "app/src/main/java/com/duplicateapp/gamespace/features/workspace/presentation/workspace_view_model.kt", lines: "52-375", role: "orchestration" }
    - { symbol: "PremiumAccess", file: "app/src/main/java/com/duplicateapp/gamespace/features/premium/domain/premium_access.kt", lines: "8-14", role: "entitlement semantics" }
    - { symbol: "gameLibrary", file: "app/src/main/java/com/duplicateapp/gamespace/features/workspace/presentation/components/game_library.kt", lines: "45-84", role: "main UI" }
  related_symbols:
    - { symbol: "settingsDialog", relationship: "called-by workspaceScreen", relevance: "paywall copy" }
    - { symbol: "RewardedAdManager", relationship: "constructed-by WorkspaceViewModel.Factory", relevance: "remove/replace" }
  execution_path:
    - "User selects stored session"
    - "ViewModel marks starting and invokes GameLauncher"
    - "Launcher opens profile activity or returns a typed failure"
    - "UI shows readiness/recovery and Free-only banner outside launch flow"
  pdg_constraints:
    - description: "Preserve starting -> launcher -> opened/failed state order while removing quota guard."
      affected_statements: ["workspace_view_model.kt:134-157"]
      implementation_consequence: "Do not move ads into the launch callback."
  architectural_patterns:
    - pattern: "presentation -> domain <- data"
      example_location: "docs/rules/architecture.md"
      usage_guidance: "Keep Android AdView implementation in ads data/presentation boundary."
  files_to_modify:
    - { file: "app/src/main/java/com/duplicateapp/gamespace/features/premium/domain/premium_access.kt", symbols: ["PremiumAccess"], intended_change: "Remove unlimited-play semantic" }
    - { file: "app/src/main/java/com/duplicateapp/gamespace/features/workspace/presentation/workspace_view_model.kt", symbols: ["WorkspaceViewModel"], intended_change: "Remove quota/reward orchestration" }
    - { file: "app/src/main/java/com/duplicateapp/gamespace/features/workspace/presentation/components/game_library.kt", symbols: ["gameLibrary"], intended_change: "Account identity and ad placement" }
  tests:
    - file: "app/src/androidTest/java/com/duplicateapp/gamespace/features/workspace/presentation/components/game_library_test.kt"
      scenarios: ["named sessions render and launch by id", "Premium hides ad slot"]
    - file: "app/src/androidTest/java/com/duplicateapp/gamespace/features/workspace/presentation/components/settings_dialog_test.kt"
      scenarios: ["Free copy describes ads", "Premium copy describes ad-free"]
  verification_commands:
    - "./gradlew :app:testProdDebugUnitTest"
    - "./gradlew :app:compileProdDebugKotlin"
    - "./gradlew :app:lintProdDebug"
  risks: ["AdView lifecycle", "Premium direct consumers", "OEM profile variance"]
  assumptions: ["AdMob banner unit will be configured by publisher through protected environment"]
  open_questions: []
  avoid: ["Do not show interstitials in the launch path", "Do not touch packages/the_universe public Java APIs", "Do not read production configuration"]
```

## 12. Assumptions and Open Questions

- [assumed] Publisher will provide a production AdMob banner unit ID; local builds use Google's banner test ID.
- Physical OEM validation, Play Console configuration, signing, screenshots, and pricing experiments remain publisher-operated verification work.

## 13. Definition of Done

- Free launches are unrestricted and can display only non-disruptive ads.
- Premium removes every ad surface.
- Quota/reward code and copy are absent from production source.
- Account names and actionable compatibility status are visible.
- Scoped tests, compile, and lint pass; docs describe the new model.
