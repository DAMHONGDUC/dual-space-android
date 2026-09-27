# Plan: Play readiness và doanh thu

> Task: Đưa Parallel Game Space lên Google Play và làm đường doanh thu chạy thật.
> Evidence verified tại branch `codex/gamer-farm-ux-fixes`, HEAD `4c97d46`, working tree sạch.
> `./gradlew :app:testProdDebugUnitTest :app:lintProdRelease` — BUILD SUCCESSFUL, 0 lint error, 76 warning.

## Objective (§1)

Giữ nguyên engine ảo hoá (`the-universe-core`) và sản phẩm 5-copy, nhưng thu bề mặt
manifest xuống mức một app bình thường, sửa các lỗi khiến bản release âm thầm mất
doanh thu, rồi hoàn tất hồ sơ store.

## Quyết định nền: KHÔNG gỡ `the-universe-core` (§2)

Đợt review đầu tiên kết luận phải gỡ engine. Kết luận đó **sai ở bước suy diễn**:
danh sách 329 permission là hệ quả của manifest merge, không phải của việc dùng
engine, và manifest merger gỡ được nó mà không đụng tới engine.

Thí nghiệm đã chạy và đã revert — chèn `tools:node="remove"` cho 150 permission vào
`app/src/main/AndroidManifest.xml` rồi chạy lại `:app:processProdReleaseMainManifest`:

| | trước | sau |
|---|---|---|
| tổng khai báo `uses-permission` | 329 | 116 |
| `android.permission.*` | 105 | 15 |
| SMS / Call Log / MANAGE_EXTERNAL_STORAGE | có | **hết** |

Phần 116 còn lại chủ yếu là `com.google.android.gms.permission.*` (53, do SDK ads
khai báo) và permission badge của OEM launcher — vô hại, và cũng gỡ được nốt.

Thêm một dữ kiện thị trường cắt ngược lại kết luận cũ: danh mục app cloner **đang có
mặt trên Play** (Parallel Space 100M+, Clone App 5M+, MultiSpace). Danh mục này không
bị cấm; cái bị chặn là bề mặt quyền và component, và đó là thứ sửa được.

## Rủi ro còn lại sau khi thu manifest (§3)

Đây là các mục cần phán đoán, không phải lỗi kỹ thuật — liệt kê để quyết định có ý thức:

1. [verified] `ProxyVpnService` vẫn còn trong manifest hợp nhất, trong khi
   `ParallelAppClientConfiguration.isUseVpnNetwork()` trả `false`
   (`the_universe_runtime_bootstrap.kt:25`). Component chết nhưng kích hoạt VPN policy
   của Play → gỡ bằng `tools:node="remove"`.
2. [verified] Engine dùng `com.github.tiann:FreeReflection` (bypass hidden-API) và JNI
   hook native (`packages/the_universe/core/src/main/cpp/JniHook/`). Đây là vùng xám
   của Device and Network Abuse, không phải vi phạm hiển nhiên.
3. [verified] `packages/the_universe` là bản đổi tên của BlackBox. Tính tới 2026-09-16,
   toàn bộ namespace nhận diện đã được đổi sang `com.dd.the.universe` — gồm cả
   `top.niunaijun.jnihook` của tác giả gốc; dấu vết còn lại chỉ là chuỗi `"niunaijun"`
   trong `BPackageManagerService.java:760` và tên thư mục dữ liệu `theuniverse`.
   Repo chỉ có `LICENSE` MIT đứng tên publisher, không NOTICE, không attribution.
   **Việc đổi tên không giải quyết nghĩa vụ giấy phép — nó chỉ làm nguồn gốc khó truy
   hơn.** Cần xác minh giấy phép gốc của BlackBox và bổ sung attribution trước khi bán;
   đây là mục pháp lý, không phải mục kỹ thuật, và không tự biến mất theo thời gian.
4. 166 activity / 113 service / 56 provider proxy vẫn còn — làm chậm review, không tự
   động reject.

## Giai đoạn 0 — Khoá bề mặt manifest (§4)

1. Thêm block `tools:node="remove"` vào `app/src/main/AndroidManifest.xml`, sinh từ
   whitelist thay vì liệt kê tay. Giữ lại: `INTERNET`, `ACCESS_NETWORK_STATE`,
   `WAKE_LOCK`, `VIBRATE`, `POST_NOTIFICATIONS`, `USE_FINGERPRINT`,
   `com.android.vending.BILLING`, `com.google.android.gms.permission.AD_ID`,
   `ACCESS_ADSERVICES_*`.
2. Gỡ `ProxyVpnService` và các component không dùng khác bằng cùng cơ chế.
3. **Thêm test chặn hồi quy**: một unit test đọc manifest hợp nhất của `prodRelease` và
   fail nếu xuất hiện permission ngoài whitelist. Đây là mục quan trọng nhất của giai
   đoạn — nếu không có nó, một lần bump `the-universe-core` là SMS quay lại im lặng.
4. Bổ sung `NOTICE` + attribution cho code vendored, sửa `LICENSE` cho đúng thực tế.
5. Xoá code chết của nhánh managed-profile: `AndroidProfileGameLauncher`,
   `AndroidProfileProvisioner`, `ParallelAppDeviceAdminReceiver`,
   `PolicyComplianceActivity`, `ProvisioningModeActivity` — không class nào được khai
   báo trong manifest, và factory đã wire sang `VirtualizedGameLauncher`
   (`workspace_view_model_factory.kt:27`).
6. Viết lại `docs/store-readiness.md` và `docs/google-play-launch-plan.md`: cả hai đang
   mô tả kiến trúc managed-profile không còn tồn tại trong code.

**Định nghĩa xong:** merged manifest của `prodRelease` có ≤20 `android.permission.*`,
test hồi quy pass, và `assembleProdRelease` chạy được.

## Giai đoạn 1 — Doanh thu (§5)

Nhóm này độc lập với giai đoạn 0, làm song song được.

1. `app/build.gradle.kts:20` — bỏ fallback `ADMOB_APP_ID` về ID test của Google. Cho
   Gradle **fail build release** khi thiếu `ADMOB_APP_ID`, `ADMOB_BANNER_AD_UNIT_ID`
   hoặc `REVENUECAT_GOOGLE_API_KEY`. Hiện tại thiếu key thì banner return sớm
   (`banner_ad.kt:20`) và nút mua báo unavailable — release ra mắt với doanh thu bằng 0
   mà không có tín hiệu nào.
2. `workspace_view_model.kt` — gọi `refreshPremiumAccess()` trong
   `onWorkspaceResumed()`. Hiện chỉ gọi trong `onWorkspaceReady()` (chạy một lần), nên
   sub hết hạn hoặc mua ở máy khác không được nhận cho tới khi restart app.
3. `banner_ad.kt:24` — đổi `AdSize.BANNER` cố định sang anchored adaptive banner. Cùng
   vị trí, eCPM cao hơn rõ rệt; thay đổi khoảng 3 dòng.
4. Gate số copy theo Premium: free 2 copy/game, Premium 5 + tắt quảng cáo. Hiện
   `GameCopyLimits.maximumCopiesPerGame = 5` cho mọi user và Premium chỉ tắt quảng cáo
   (`premium_access.kt`) — đây là toàn bộ đề nghị mua hàng, quá yếu so với đối thủ vốn
   bán chính bằng số lượng clone.

## Giai đoạn 2 — Chất lượng trước khi mở review (§6)

1. Sửa mâu thuẫn số copy trong chuỗi hiển thị: `game_already_added` nói 5 copies,
   `onboarding_profiles` và `about_description` nói 2 (`values/strings.xml:20,60,141`).
   Số này phải sinh từ `GameCopyLimits`, không hardcode trong chuỗi.
2. Lối thoát khi gỡ copy thất bại: `workspace_view_model.kt:287` — nếu
   `gameCopyRemover.remove` trả `false` thì session không bị xoá khỏi repo, chỉ hiện
   snackbar. User kẹt vĩnh viễn; cần nút "gỡ khỏi danh sách" cưỡng bức.
3. i18n: 9 locale thiếu 27/144 chuỗi, và nội dung đã cũ (bản Đức còn ghi "2 Kopien").
   Hoặc dịch xong, hoặc cắt `locales_config.xml` còn `en` + `vi` cho tới khi dịch xong —
   quảng cáo 11 ngôn ngữ rồi hiện tiếng Anh là điểm trừ khi review.
4. `AppLogger` log mức INFO trong release kèm package name game user cài
   (`app_logger.kt`). Gate bằng `BuildConfig.DEBUG` hoặc `-assumenosideeffects`.
5. R8 gần như vô hiệu: `packages/the_universe/core/consumer-rules.pro` có
   `-keep class android.** {*;}` và `-keep class com.android.** {*;}`, kéo theo cả app.
   Thu hẹp keep rule về đúng vùng engine cần reflection.

## Giai đoạn 3 — Hồ sơ store (§7)

1. Upload signing key + Play App Signing.
2. `docs/privacy-policy.md` còn là draft, còn đoạn "replace this paragraph", và mô tả
   managed profile — kiến trúc không còn đúng. Viết lại theo hành vi thật, publish HTTPS.
3. Data safety khai từ AAB thật: Firebase Auth, RevenueCat, AdMob.
4. Icon 512px, feature graphic 1024x500, screenshot phone + tablet, mô tả ngắn/dài.
5. Closed testing: tài khoản cá nhân tạo sau 2023-11-13 cần 12 tester giữ liên tục 14
   ngày trước khi xin production.
6. Ghi chú cho reviewer: giải thích app làm gì, tại sao có nhiều component proxy, và
   user gỡ copy + dữ liệu bằng cách nào.

## Thứ tự thực thi (§8)

Giai đoạn 0 và 1 chạy song song — 0 chặn việc lên store, 1 chặn doanh thu, không phụ
thuộc nhau. Giai đoạn 2 sau khi 0 xong (xoá code chết trước rồi mới refactor chuỗi).
Giai đoạn 3 cuối, vì Data safety phải khai từ AAB cuối cùng.

Gate bắt buộc giữa 0 và 3: test hồi quy permission phải tồn tại và pass. Không có nó,
mọi thứ còn lại đều có thể bị vô hiệu bởi một lần cập nhật engine.

## Không làm (§9)

Macro, auto-click, fake GPS, giả device identity, bypass anti-cheat, ẩn root, can thiệp
quảng cáo, tải code thực thi. Các mục này mâu thuẫn với ranh giới sản phẩm và làm tăng
mạnh rủi ro chính sách lẫn rủi ro khoá tài khoản developer.
