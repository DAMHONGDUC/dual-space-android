# GitNexus Engineering Plan

> Task: Sửa các điểm nghẽn của trải nghiệm game thủ farm nhiều tài khoản.
> Evidence verified at commit 4f40e69926ef64c789059fda954e0b74687e3d2d; GitNexus index 1 commit behind, refresh skipped because this is a compact source-verified plan.
> Evidence provenance schema 2; global dirty digest ae59bab6acd4ceba40660b36a2d65df54e77372f7709562210ac9d7c53d6aa34; cited-path manifest 17 sorted entries; exact generated plan path excluded.

## Objective (§1)

Giảm cold-start và RAM, rút ngắn luồng mở acc, làm danh sách nhiều acc dễ dùng, đồng thời chỉ tăng số copy sau khi runtime vượt qua compatibility gate.

## Current Behaviour (§2–3)

- [verified] `ParallelAppApplication.attachBaseContext/onCreate` khởi tạo The Universe trước khi workspace hiển thị (`parallel_app_application.kt:8-27`).
- [verified] `WorkspaceViewModel.init` đồng thời refresh premium, publish shortcut và kiểm tra readiness của mọi session (`workspace_view_model.kt:107-111`).
- [verified] Mọi lần `launchSession` đều tạo pending state và bắt buộc qua dialog xác nhận (`workspace_view_model.kt:135-150`, `workspace_screen.kt:65-75`).
- [verified] Giới hạn hiện tại là 2 virtual users/game (`game_copy_limits.kt:3-8`).
- [verified] Tablet layout đẩy toàn bộ account buttons sang mép phải bằng spacer + end-aligned column (`game_library.kt:203-223`).
- [verified] Nhấn Home giữ nguyên PID và resume task; nhấn Back làm game gọi `System.exit(0)`, process proxy chết và game phải nạp lại engine/resources.

## Findings (§4–5)

- [graph] `query(search_query="workspace cold startup...")` nối `WorkspaceScreen` với `addSession`, `GameCopyLimits`, launch dialog và runtime; index chậm hơn HEAD 1 commit.
- [graph] `context(WorkspaceViewModel)` cho thấy một consumer trực tiếp là `parallel_app.kt`; `impact(..., maxDepth=1)` báo LOW, direct=1.
- [verified] Đợt test emulator: cold-start 11–15 giây, khoảng 500 MB cho hai process app; 30 lần luân phiên có 0 crash/ANR nhưng phát sinh AppOps SecurityException lặp lại.

## Proposed Changes (§6)

1. **Startup/performance:** thêm timing quanh `ParallelAppApplication`, `MainActivity` và `WorkspaceViewModel.init`; chuyển premium, shortcut refresh và readiness scan khỏi critical render path, cache readiness và chỉ làm mới khi resume/session thay đổi.
2. **Mở acc nhanh:** thêm setting “Xác nhận trước khi mở”; mặc định bật, nhưng cho phép tắt. Shortcut và account button đi thẳng tới `launch`, còn trạng thái unavailable vẫn mở hướng dẫn thay vì bỏ qua.
3. **Workspace nhiều acc:** đổi `gameRow/accountButton` sang grid/list responsive, hiển thị tên acc là thông tin chính, thêm sort theo gần nhất/tên và indicator đang chạy; không thêm launch-all trong đợt này.
4. **Giữ game warm:** coi Home/Recents là luồng chuyển acc chính; dùng `TheUniverseCore.isRunningApplication(packageName, userId)` để nhận biết session còn sống, bring task cũ lên foreground và hiển thị `Đang chạy/Đang tạm dừng`. Thêm hướng dẫn “Thu nhỏ để chuyển acc”; `Back = thu nhỏ` chỉ là opt-in thử nghiệm, không hook `System.exit` mặc định.
5. **Compatibility trước scale:** chuẩn hóa kết quả/lỗi trong `TheUniverseVirtualGameRuntime`, thêm regression test cho AppOps/package identity; chỉ tăng `GameCopyLimits` sau khi 5–10 virtual users qua test launch, restart, notification, login và uninstall độc lập.

## Implementation Sequence (§7)

1. Ghi baseline bằng Macrobenchmark hoặc startup timing: cold/warm start, PSS, readiness duration; đặt budget cold-start ≤3 giây và workspace process ≤250 MB trên emulator chuẩn.
2. Tách initialization không chặn first frame, thêm cache/invalidation và unit tests; đo lại trước khi tiếp tục.
3. Thêm launch-confirmation preference theo feature settings, nối vào `WorkspaceViewModel.launchSession`, cập nhật dialog/settings/UI tests.
4. Refactor `gameLibrary` responsive và thêm sort/running state; giữ touch target ≥48dp, kiểm tra phone/tablet và dark/light.
5. Thêm warm-session flow và test ba nhánh: Home giữ PID, mở lại reuse task, Back/System.exit được ghi nhận là stopped và tải lại có chủ đích.
6. Chạy compatibility matrix cho runtime; nếu đạt gate, nâng giới hạn theo capability thay vì hằng số cố định và cập nhật allocation/migration tests.

## Test Strategy (§8)

- Cập nhật `game_copy_limits_test.kt`: allocate/reuse/N slots và từ chối khi runtime capability hết chỗ.
- Cập nhật `launch_confirmation_dialog_test.kt`: mặc định hỏi, opt-out mở thẳng, unavailable không bypass cảnh báo.
- Cập nhật `game_library_test.kt`: sort, running indicator, nhiều hơn 2 acc, phone/tablet semantics.
- Thêm instrumentation cho warm resume: Home → PID không đổi → mở lại không tạo process/Activity mới; Back → EXIT_SELF → session chuyển stopped.
- Thêm instrumentation cho virtual users: dữ liệu độc lập, restart, launch xen kẽ, uninstall một acc không ảnh hưởng acc khác, không có AppOps exception mới.
- Chạy `./gradlew :app:testProdDebugUnitTest`, `./gradlew :app:connectedProdDebugAndroidTest`, `./gradlew :app:lintProdDebug`.

## Implementation Context (§11)

```yaml
implementation_context:
  task_summary: 'Optimize startup, reduce launch friction, improve multi-account workspace, then safely scale copies.'
  evidence_provenance: {"schema_version":2,"head_commit":"4f40e69926ef64c789059fda954e0b74687e3d2d","generated_plan_path":"docs/plans/2026-09-13-gitnexus-plan-gamer-farm-ux-fixes.md","global_dirty_digest":{"algorithm":"sha256","canonicalization":"gitnexus-evidence-provenance-v2 NUL-framed UTF-8 records","value":"ae59bab6acd4ceba40660b36a2d65df54e77372f7709562210ac9d7c53d6aa34"},"cited_path_manifest":[{"path":"README.md","object_kind":{"head":"regular","index":"regular","worktree":"regular","untracked":"absent"},"state":"clean","rename_from":null,"rename_to":null,"head_digest":"sha256:2fa92b9cc367781f63f07d3404c89266ddf658b8a038f72847d8b036090ad520","index_digest":"sha256:2fa92b9cc367781f63f07d3404c89266ddf658b8a038f72847d8b036090ad520","worktree_digest":"sha256:2fa92b9cc367781f63f07d3404c89266ddf658b8a038f72847d8b036090ad520","untracked_digest":"absent"},{"path":"app/src/androidTest/java/com/duplicateapp/gamespace/features/workspace/presentation/components/game_library_test.kt","object_kind":{"head":"regular","index":"regular","worktree":"regular","untracked":"absent"},"state":"clean","rename_from":null,"rename_to":null,"head_digest":"sha256:e0081fcbe5198d3ca6beef39a5a72f735a0b5cb301cce375d4009e22e0b16b8b","index_digest":"sha256:e0081fcbe5198d3ca6beef39a5a72f735a0b5cb301cce375d4009e22e0b16b8b","worktree_digest":"sha256:e0081fcbe5198d3ca6beef39a5a72f735a0b5cb301cce375d4009e22e0b16b8b","untracked_digest":"absent"},{"path":"app/src/androidTest/java/com/duplicateapp/gamespace/features/workspace/presentation/components/launch_confirmation_dialog_test.kt","object_kind":{"head":"regular","index":"regular","worktree":"regular","untracked":"absent"},"state":"clean","rename_from":null,"rename_to":null,"head_digest":"sha256:27b2c023194ce5fa518e68d5caf05dbd69ece4de0a888cad01a22cb6c7f5c90e","index_digest":"sha256:27b2c023194ce5fa518e68d5caf05dbd69ece4de0a888cad01a22cb6c7f5c90e","worktree_digest":"sha256:27b2c023194ce5fa518e68d5caf05dbd69ece4de0a888cad01a22cb6c7f5c90e","untracked_digest":"absent"},{"path":"app/src/main/java/com/duplicateapp/gamespace/features/virtualization/data/the_universe_virtual_game_runtime.kt","object_kind":{"head":"regular","index":"regular","worktree":"regular","untracked":"absent"},"state":"clean","rename_from":null,"rename_to":null,"head_digest":"sha256:190a19d5fc4901a62e9d61fe44bcd673c87d8fff382213a03281df890523a8a0","index_digest":"sha256:190a19d5fc4901a62e9d61fe44bcd673c87d8fff382213a03281df890523a8a0","worktree_digest":"sha256:190a19d5fc4901a62e9d61fe44bcd673c87d8fff382213a03281df890523a8a0","untracked_digest":"absent"},{"path":"app/src/main/java/com/duplicateapp/gamespace/features/workspace/domain/game_copy_limits.kt","object_kind":{"head":"regular","index":"regular","worktree":"regular","untracked":"absent"},"state":"clean","rename_from":null,"rename_to":null,"head_digest":"sha256:21b3774e7d033c1431c5f39ff94fbc172af6db06060745ebbddd9827e46e6b59","index_digest":"sha256:21b3774e7d033c1431c5f39ff94fbc172af6db06060745ebbddd9827e46e6b59","worktree_digest":"sha256:21b3774e7d033c1431c5f39ff94fbc172af6db06060745ebbddd9827e46e6b59","untracked_digest":"absent"},{"path":"app/src/main/java/com/duplicateapp/gamespace/features/workspace/presentation/components/game_library.kt","object_kind":{"head":"regular","index":"regular","worktree":"regular","untracked":"absent"},"state":"clean","rename_from":null,"rename_to":null,"head_digest":"sha256:5bb458a678eabf4545256862d7731f422c5d2bac037e9f3fff8a84f91180120d","index_digest":"sha256:5bb458a678eabf4545256862d7731f422c5d2bac037e9f3fff8a84f91180120d","worktree_digest":"sha256:5bb458a678eabf4545256862d7731f422c5d2bac037e9f3fff8a84f91180120d","untracked_digest":"absent"},{"path":"app/src/main/java/com/duplicateapp/gamespace/features/workspace/presentation/components/launch_confirmation_dialog.kt","object_kind":{"head":"regular","index":"regular","worktree":"regular","untracked":"absent"},"state":"clean","rename_from":null,"rename_to":null,"head_digest":"sha256:1a5339b754c159dac4a5c11d8ea492447a34cf877f3a6296bf418f28fddf8260","index_digest":"sha256:1a5339b754c159dac4a5c11d8ea492447a34cf877f3a6296bf418f28fddf8260","worktree_digest":"sha256:1a5339b754c159dac4a5c11d8ea492447a34cf877f3a6296bf418f28fddf8260","untracked_digest":"absent"},{"path":"app/src/main/java/com/duplicateapp/gamespace/features/workspace/presentation/workspace_screen.kt","object_kind":{"head":"regular","index":"regular","worktree":"regular","untracked":"absent"},"state":"clean","rename_from":null,"rename_to":null,"head_digest":"sha256:822d29b61390ab30232ee3aa01220ae256ece533fbcc970f2cc9b2b01969016f","index_digest":"sha256:822d29b61390ab30232ee3aa01220ae256ece533fbcc970f2cc9b2b01969016f","worktree_digest":"sha256:822d29b61390ab30232ee3aa01220ae256ece533fbcc970f2cc9b2b01969016f","untracked_digest":"absent"},{"path":"app/src/main/java/com/duplicateapp/gamespace/features/workspace/presentation/workspace_view_model.kt","object_kind":{"head":"regular","index":"regular","worktree":"regular","untracked":"absent"},"state":"clean","rename_from":null,"rename_to":null,"head_digest":"sha256:1aa2f8d0739df9bf3f3529edd8ae35265d43a98a1144bbddf54855f78d4c4698","index_digest":"sha256:1aa2f8d0739df9bf3f3529edd8ae35265d43a98a1144bbddf54855f78d4c4698","worktree_digest":"sha256:1aa2f8d0739df9bf3f3529edd8ae35265d43a98a1144bbddf54855f78d4c4698","untracked_digest":"absent"},{"path":"app/src/main/java/com/duplicateapp/gamespace/features/workspace/presentation/workspace_view_model_factory.kt","object_kind":{"head":"regular","index":"regular","worktree":"regular","untracked":"absent"},"state":"clean","rename_from":null,"rename_to":null,"head_digest":"sha256:ee9d8c6f1b1bf0997d9e00d2eae86b53614b4aa5e1a40cddf3676443206869b5","index_digest":"sha256:ee9d8c6f1b1bf0997d9e00d2eae86b53614b4aa5e1a40cddf3676443206869b5","worktree_digest":"sha256:ee9d8c6f1b1bf0997d9e00d2eae86b53614b4aa5e1a40cddf3676443206869b5","untracked_digest":"absent"},{"path":"app/src/main/java/com/duplicateapp/gamespace/main_activity.kt","object_kind":{"head":"regular","index":"regular","worktree":"regular","untracked":"absent"},"state":"clean","rename_from":null,"rename_to":null,"head_digest":"sha256:3173fc0e99abebec936fdc68e174e02fe95ba6968b952602f09e89921f03b3c2","index_digest":"sha256:3173fc0e99abebec936fdc68e174e02fe95ba6968b952602f09e89921f03b3c2","worktree_digest":"sha256:3173fc0e99abebec936fdc68e174e02fe95ba6968b952602f09e89921f03b3c2","untracked_digest":"absent"},{"path":"app/src/main/java/com/duplicateapp/gamespace/parallel_app_application.kt","object_kind":{"head":"regular","index":"regular","worktree":"regular","untracked":"absent"},"state":"clean","rename_from":null,"rename_to":null,"head_digest":"sha256:6f0c9e4efd15c46b183f0f9e8497abbf09112d6c25355e88eb7f42f3cde4ce41","index_digest":"sha256:6f0c9e4efd15c46b183f0f9e8497abbf09112d6c25355e88eb7f42f3cde4ce41","worktree_digest":"sha256:6f0c9e4efd15c46b183f0f9e8497abbf09112d6c25355e88eb7f42f3cde4ce41","untracked_digest":"absent"},{"path":"app/src/test/java/com/duplicateapp/gamespace/features/workspace/domain/game_copy_limits_test.kt","object_kind":{"head":"regular","index":"regular","worktree":"regular","untracked":"absent"},"state":"clean","rename_from":null,"rename_to":null,"head_digest":"sha256:4adf9e2f54d7013af4b744eda718ed51317490a5e9a5e9cc1611a4d9bcd6f800","index_digest":"sha256:4adf9e2f54d7013af4b744eda718ed51317490a5e9a5e9cc1611a4d9bcd6f800","worktree_digest":"sha256:4adf9e2f54d7013af4b744eda718ed51317490a5e9a5e9cc1611a4d9bcd6f800","untracked_digest":"absent"},{"path":"packages/the_universe/core/src/main/java/com/duplicateapp/theuniverse/TheUniverseCore.java","object_kind":{"head":"regular","index":"regular","worktree":"regular","untracked":"absent"},"state":"clean","rename_from":null,"rename_to":null,"head_digest":"sha256:abf543b0cfd94fb2cc5c043a6cc29e9554c4f57e4d33ad3fc6a8f4ef210da08b","index_digest":"sha256:abf543b0cfd94fb2cc5c043a6cc29e9554c4f57e4d33ad3fc6a8f4ef210da08b","worktree_digest":"sha256:abf543b0cfd94fb2cc5c043a6cc29e9554c4f57e4d33ad3fc6a8f4ef210da08b","untracked_digest":"absent"},{"path":"packages/the_universe/core/src/main/java/com/duplicateapp/theuniverse/core/system/BProcessManagerService.java","object_kind":{"head":"regular","index":"regular","worktree":"regular","untracked":"absent"},"state":"clean","rename_from":null,"rename_to":null,"head_digest":"sha256:bc4ec5b349bf912f82089b23e0293391bfeff9f9bcbe716fc292fe9cd444095f","index_digest":"sha256:bc4ec5b349bf912f82089b23e0293391bfeff9f9bcbe716fc292fe9cd444095f","worktree_digest":"sha256:bc4ec5b349bf912f82089b23e0293391bfeff9f9bcbe716fc292fe9cd444095f","untracked_digest":"absent"},{"path":"packages/the_universe/core/src/main/java/com/duplicateapp/theuniverse/core/system/ProcessRecord.java","object_kind":{"head":"regular","index":"regular","worktree":"regular","untracked":"absent"},"state":"clean","rename_from":null,"rename_to":null,"head_digest":"sha256:608871bcebf672fa3e7156c682ccd6eef0596cffe2b32995100ff1281ef45012","index_digest":"sha256:608871bcebf672fa3e7156c682ccd6eef0596cffe2b32995100ff1281ef45012","worktree_digest":"sha256:608871bcebf672fa3e7156c682ccd6eef0596cffe2b32995100ff1281ef45012","untracked_digest":"absent"},{"path":"packages/the_universe/core/src/main/java/com/duplicateapp/theuniverse/core/system/am/ActivityStack.java","object_kind":{"head":"regular","index":"regular","worktree":"regular","untracked":"absent"},"state":"clean","rename_from":null,"rename_to":null,"head_digest":"sha256:b70806e11494cbdc859794ce67207c3abab4db7db82fe767e0932c79a6572583","index_digest":"sha256:b70806e11494cbdc859794ce67207c3abab4db7db82fe767e0932c79a6572583","worktree_digest":"sha256:b70806e11494cbdc859794ce67207c3abab4db7db82fe767e0932c79a6572583","untracked_digest":"absent"}]}
  files_to_modify:
    - file: app/src/main/java/com/duplicateapp/gamespace/parallel_app_application.kt
      symbols: [ParallelAppApplication.attachBaseContext, ParallelAppApplication.onCreate]
      intended_change: 'Instrument and remove nonessential work from startup critical path.'
    - file: app/src/main/java/com/duplicateapp/gamespace/features/workspace/presentation/workspace_view_model.kt
      symbols: [WorkspaceViewModel.init, WorkspaceViewModel.launchSession, WorkspaceViewModel.addSession]
      intended_change: 'Lazy refreshes, optional confirmation, capability-based copy allocation.'
    - file: app/src/main/java/com/duplicateapp/gamespace/features/workspace/presentation/components/game_library.kt
      symbols: [gameLibrary, gameRow, accountButton]
      intended_change: 'Responsive sortable multi-account presentation with running state.'
    - file: app/src/main/java/com/duplicateapp/gamespace/features/virtualization/data/the_universe_virtual_game_runtime.kt
      symbols: [TheUniverseVirtualGameRuntime.launch]
      intended_change: 'Normalize compatibility diagnostics and validate package identity behavior.'
  tests:
    - file: app/src/test/java/com/duplicateapp/gamespace/features/workspace/domain/game_copy_limits_test.kt
      scenarios: ['N slots -> allocate next free', 'capacity reached -> reject']
    - file: app/src/androidTest/java/com/duplicateapp/gamespace/features/workspace/presentation/components/launch_confirmation_dialog_test.kt
      scenarios: ['confirmation on -> dialog', 'confirmation off -> direct launch', 'unavailable -> guidance']
    - file: app/src/androidTest/java/com/duplicateapp/gamespace/features/workspace/presentation/components/game_library_test.kt
      scenarios: ['many accounts -> responsive ordered list', 'running session -> visible semantic status']
  verification_commands: ['./gradlew :app:testProdDebugUnitTest', './gradlew :app:connectedProdDebugAndroidTest', './gradlew :app:lintProdDebug']
  assumptions: ['Measure on one pinned emulator/device profile before and after each performance change.', 'Do not raise copy limit until runtime compatibility gate passes.']
  open_questions: ['Target maximum accounts per game: 5, 10, or runtime-dependent?', 'Should launch confirmation opt-out apply globally or per account?', 'Should Back-as-minimize be enabled only for an explicit per-game compatibility allowlist?']
  avoid: ['Do not hook System.exit or suppress a game-requested shutdown by default.', 'Do not initialize ads/premium/runtime scans on the first-frame critical path.', 'Do not add launch-all or automation/macros.', 'Do not change packages/the_universe public Java reflection contracts without a separate impact pass.']
```

## Assumptions and Open Questions (§12)

- [assumed] Home/Recents warm resume là hành vi mặc định; `Back = thu nhỏ` chỉ bật theo từng game sau test tương thích.
- [assumed] Mục tiêu tối thiểu hợp lý là 5 acc/game; chốt con số sau compatibility matrix.
- Chọn launch-confirmation setting global hay per-account; đề xuất global để giữ bản đầu nhỏ.
- Launch-all, auto-click, macro và chạy game nền chủ động được hoãn vì khác phạm vi và tăng rủi ro policy/anti-cheat.

## Definition of Done (§13)

- Cold-start p50 ≤3 giây, không block first frame bởi premium/shortcut/readiness; PSS workspace ≤250 MB trên profile đo cố định.
- Người dùng có thể tắt xác nhận và mở acc bằng một tap; unavailable state vẫn an toàn.
- 5+ acc hiển thị/sort rõ trên phone và tablet, không tràn, đầy đủ accessibility semantics.
- Home → mở lại giữ cùng PID và không cold-load; Back/System.exit được hiển thị stopped thay vì giả vờ đang chạy.
- 30 vòng xen kẽ không crash/ANR, không tăng AppOps exception, dữ liệu và uninstall độc lập.
- Ba lệnh test/lint ở §8 đều pass.


