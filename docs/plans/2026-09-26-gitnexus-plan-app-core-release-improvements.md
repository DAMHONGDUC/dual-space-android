# Kế hoạch cải thiện core và chuẩn bị phát hành

> Ngày kiểm tra: 2026-09-26. Mục tiêu: gom toàn bộ phát hiện đã xác minh và rủi ro chưa kiểm chứng vào một kế hoạch thực thi.
> Mã nguồn tại HEAD `043f7197d781c316e639566c138cd7fd214726d5`, nhánh `codex/gamer-farm-ux-fixes`; cây làm việc sạch trước khi tạo tài liệu.
> GitNexus chậm 31 commit, còn tên package cũ. Không làm mới vì chưa xác minh được danh tính phiên bản bộ phân tích hiện hành; mọi kết luận chính dựa trên mã nguồn, không dựa vào phạm vi ảnh hưởng của đồ thị cũ.
> Dấu vết bằng chứng: phiên bản 2; mã băm toàn bộ thay đổi `0a9c85780067d9afcd0764f307b60891e3cee927ee11eaeb5ec7826d10fd82cd`; 71 đường dẫn được ghim trong §11, chỉ loại trừ chính tài liệu này.

## 1. Mục tiêu

Chưa đủ bằng chứng để phát hành production. Ưu tiên tính đúng của clone, không mất dữ liệu, đóng bề mặt IPC không cần thiết, rồi mới tối ưu doanh thu và hồ sơ Play.

Đây là kế hoạch, không phải bản sửa lỗi. Không có mã nguồn, test hay cấu hình được chỉnh trong lượt này. Kế hoạch này là backlog hợp nhất thay cho phần việc còn mở của `docs/plans/2026-09-16-plan-engine-core-improvements.md` và `docs/plans/2026-09-16-plan-play-readiness-and-revenue.md`; hai tài liệu cũ chỉ còn giá trị lịch sử.

Không thể chứng minh “không còn bất kỳ lỗi nào” bằng scan. Điểm dừng là đã kiểm tra các luồng trọng yếu, đối chiếu bằng chứng cũ, đưa mọi phát hiện còn mở vào đây và chỉ rõ những phần cần thiết bị/thử nghiệm thực tế. Không coi test pass hoặc không có cạnh đồ thị là bằng chứng an toàn tuyệt đối.

Quy ước: `[verified]` = hành vi mã nguồn đã đọc; không mặc nhiên có nghĩa đã tái hiện trên máy Android. `[inferred]` = hậu quả suy ra từ đường đi đã đọc. `[graph]` = bằng chứng đồ thị cũ. `[assumed]` = cần kiểm chứng. P1 xử lý trước phát hành; P2 ưu tiên kế tiếp; P3 tối ưu tùy chọn. Không tự gán P0 khi chưa chứng minh tác động khẩn cấp.

## 2. Hành vi hiện tại

1. [verified] `WorkspaceViewModel` yêu cầu `VirtualizedGameLauncher` kiểm tra/cài/mở copy; `TheUniverseVirtualGameRuntime` chuyển tiếp tới engine. UI ghi nhận đang chạy sau kết quả thành công, chưa xác nhận Activity guest thực sự hiển thị.
2. [verified] Engine dùng package dùng chung và dữ liệu theo virtual user. Điều này bắt buộc cập nhật package phải nhất quán với tất cả copy, không chỉ copy đang cài.
3. [verified] Workspace lưu danh sách JSON trong SharedPreferences; engine lưu UID/package bằng Parcel. Nhiều đường lỗi chuyển thành thành công hoặc danh sách rỗng.
4. [verified] Premium dùng RevenueCat, đăng nhập dùng Firebase, quảng cáo có consent gate. Consent gate hiện có không phải phát hiện lỗi của đợt này.
5. [verified] minSdk=29, target/compileSdk=37; cả flavor dev/prod dùng engine. targetSdk cao không chứng minh engine tương thích với phiên bản Android tương ứng.

## 3. Kiến trúc và ràng buộc

[verified] Theo `docs/rules/architecture.md`: `presentation -> domain <- data`, liên feature qua domain; engine Java/JNI giữ hợp đồng Android, AIDL và reflection. Theo `docs/rules/naming.md`: Kotlin snake_case, public Java UpperCamelCase, không đổi khóa serialized như một phần rename.

Không đổi public API boolean/Parcelable đột ngột. Bổ sung trạng thái nội bộ có cấu trúc và adapter tương thích trước; nếu đổi AIDL/Parcel phải có kế hoạch phiên bản và kiểm tra mọi caller. Một lỗi không được “sửa” bằng fallback sang bản game gốc hay xóa dữ liệu người dùng.

## 4. Bằng chứng GitNexus và phạm vi kiểm tra

- [graph] `list_repos`: chỉ mục tại `4a555635e86d340489fd5766f005a92710979d1c`, chậm 31 commit so với HEAD.
- [graph] `context(name=launchApk)`: trả đường dẫn namespace cũ và bỏ một caller do không giải được kiểu receiver. `impact(target=launchApk,direction=upstream,maxDepth=3)` không có caller đã giải được: trạng thái **không biết**, không phải không có ảnh hưởng.
- [verified] Chuỗi caller hiện hành được kiểm tra trực tiếp: ViewModel → launcher → runtime → TheUniverseCore → service/package/process manager. Các test launcher hiện dùng runtime giả; test runtime trên Android chưa mở guest để kiểm tra cách ly thực tế.
- [inferred] Đồ thị cũ không đủ cho kết luận toàn repo. Không dùng clusters/processes cũ để xác nhận kiến trúc hiện hành; khi triển khai thay đổi API rộng cần xác minh runner, làm mới chỉ mục một lần rồi kiểm tra lại caller.

Đã kiểm tra: cài/mở/gỡ, process/service Binder, cấu hình và migration, IO Java/JNI, các hook native liên quan, manifest/proxy, workspace lifecycle, auth/Premium/ads, khóa riêng tư, shortcut/update, resource, cấu hình build, test và hai test companion.

Chưa chứng minh: mọi hook framework trên mọi Android/OEM, khai thác IPC bằng app ngoài, guest chống gian lận/Play Integrity, bản AAB cuối đã ký, thiết bị thật, cấu hình console và giao dịch production. Không truy cập bí mật hay dữ liệu production.

## 5. Ràng buộc điều khiển và dữ liệu

- [graph] PDG `controls` của `launchApk` trong chỉ mục cũ cho thấy nhánh intent null trả false; nhánh còn lại gọi startActivity rồi trả true. [verified] Mã hiện tại `packages/the_universe/core/src/main/java/com/dd/the/universe/TheUniverseCore.java:1094` vẫn có khoảng trống xác nhận kết quả này. Phải giữ nhánh chặn permission/intent, bổ sung xác nhận khởi chạy; không bỏ guard để “tăng tỷ lệ mở”.
- [verified] `packages/the_universe/core/src/main/java/com/dd/the/universe/core/system/BProcessManagerService.java:54` lập khóa từ userId+appId, nhưng `:80` lưu `record.buid` chỉ appId; các đường chết/kill đọc khóa từ record. Phải thống nhất khóa map, không đổi mọi nghĩa của buid một cách máy móc vì field được truyền cho client.
- [verified] `packages/the_universe/core/src/main/java/com/dd/the/universe/core/system/pm/BPackageManagerService.java:795` đi qua tạo thư mục/copy rồi cập nhật state. Việc xóa shared directory xảy ra trước khi biết giao dịch thành công. Ràng buộc mới: chuẩn bị → kiểm tra → chuyển đổi nguyên tử → ghi nhận; lỗi không làm hỏng bản đang chạy.
- [verified] `IOCore.redirectPath` phụ thuộc kết quả `TrieTree.search`; kết quả đó phải là prefix đường dẫn dài nhất, có ranh giới segment. Không dùng thay thế tất cả chuỗi hoặc trả prefix thay cho đường dẫn gốc.

Không tạo cạnh PDG giả cho các hàm chưa có lát cắt hiện hành; các ràng buộc còn lại là phân tích mã nguồn trực tiếp.

## 6. Backlog hợp nhất

Mỗi mục gồm đường dẫn mã nguồn, bằng chứng, hướng sửa và điều kiện nghiệm thu; không tự động coi tất cả là lỗi đã tái hiện trên thiết bị.

### A. Core: kết quả, vòng đời và bảo toàn dữ liệu

| ID | Ưu tiên / bằng chứng | Vấn đề và hướng sửa | Test nghiệm thu |
| --- | --- | --- | --- |
| C01 | P1 [verified], `packages/the_universe/core/src/main/java/com/dd/the/universe/TheUniverseCore.java:1094`, `app/src/main/java/com/duplicateapp/gamespace/features/virtualization/data/the_universe_virtual_game_runtime.kt` | `launchApk` trả true sau lệnh startActivity kiểu void; không chứng minh guest đã chạy. Thêm trạng thái requested/started/failed, timeout có giới hạn và xác nhận đúng user/package; UI chỉ báo mở thành công khi có tín hiệu xác nhận. | Intent null, service chết, server frozen, Activity không khởi tạo → không hiện running; mở thành công phải đúng guest và virtual user. |
| C02 | P1 [verified], `packages/the_universe/core/src/main/java/com/dd/the/universe/entity/pm/InstallResult.java:12`, `packages/the_universe/core/src/main/java/com/dd/the/universe/core/system/pm/BPackageManagerService.java:685` và `:815` | Kết quả mặc định success=true; catch Throwable có thể trả success chưa đổi. Mặc định thất bại, chỉ set success sau commit; lỗi có mã và nguyên nhân, không nuốt cancellation/fatal error tùy tiện. | Tiêm lỗi parse/copy/persist → failure, không có package cài dở; đường thành công vẫn tương thích Parcelable hiện có. |
| C03 | P1 [verified], `packages/the_universe/core/src/main/java/com/dd/the/universe/fake/frameworks/BPackageManager.java:602` và `:644` | Fallback isInstalled kiểm tra app gốc qua host PM, bỏ userId; fallback launch có thể lấy intent host. Đổi thành unavailable/unknown, không thay bản clone bằng bản gốc; giữ retry có giới hạn. | Host có game nhưng user2 chưa cài, service hỏng → không báo user2 đã cài; uninstall không bị host package làm sai hậu kiểm. |
| C04 | P1 [verified], `packages/the_universe/core/src/main/java/com/dd/the/universe/core/system/BProcessManagerService.java:54`, `:80`, `:195`, `:228`; `packages/the_universe/core/src/main/java/com/dd/the/universe/core/system/user/BUserHandle.java:136` | Map ghi theo UID tổng hợp nhưng cleanup dùng appId từ record; virtual user >0 để lại record hoặc xóa nhầm nhóm. Thêm/chuẩn hóa khóa map riêng, kiểm tra tất cả đường remove/death/kill/reuse. | Cùng package user1/user2, kill từng copy và killAll → không còn record chết, không xóa user khác; khởi động lại không trả binder cũ. |
| C05 | P1 [verified] về cấu trúc, [inferred] về treo; `packages/the_universe/core/src/main/java/com/dd/the/universe/core/system/BProcessManagerService.java:54` | Có wait không timeout và IPC đồng bộ trong vùng khóa; map/list dùng khóa không nhất quán. Tách trạng thái khởi tạo khỏi global lock, giới hạn thời gian, một quy ước đồng bộ và cleanup trong finally. | Provider không trả lời, client chết giữa init, hai launch đồng thời → không treo toàn engine; timeout dọn slot, không double-allocation. |
| C06 | P1 [verified] về cấu trúc, `packages/the_universe/core/src/main/java/com/dd/the/universe/fake/frameworks/TheUniverseManager.java` | Cache service không đồng bộ; rate limit có thể trả cache chết; death callback cũ có thể xóa cache mới. Dùng generation/so sánh binder trong callback, monotonic clock, single-flight reconnect và kiểm tra sống có giới hạn. | Binder A chết sau khi B được gắn → B không bị xóa; đổi giờ máy không chặn reconnect; không tự bật vòng retry vô hạn. |
| C07 | P1 [verified], `packages/the_universe/core/src/main/java/com/dd/the/universe/core/system/pm/installer/CreatePackageExecutor.java:13`, `packages/the_universe/core/src/main/java/com/dd/the/universe/core/system/pm/BPackageManagerService.java:795` | Tạo package xóa thư mục chung, trong khi cài user hiện tại chỉ kill user đó; thất bại có thể hỏng copy khác. Stage riêng và rollback, phối hợp toàn package, không xóa bản tốt trước commit. | Nâng cấp khi user2 đang chạy; hết đĩa/process death ở từng bước → bản cũ vẫn dùng được hoặc recovery rõ ràng, dữ liệu user giữ nguyên. |
| C08 | P1/P2 theo đường vào [verified], `packages/the_universe/core/src/main/java/com/dd/the/universe/core/system/pm/installer/CopyExecutor.java`, `packages/the_universe/core/src/main/java/com/dd/the/universe/utils/NativeUtils.java` | Nhánh storage có thể bỏ qua split thiếu/lỗi native rồi vẫn thành công; chọn ABI/copy .so thiếu hậu kiểm. Host installFromDevice hiện dùng FLAG_SYSTEM, không khẳng định mọi lỗi storage đều chạm UI chính. Validate toàn bộ tập APK/ABI trước commit, đóng stream bằng scope, không dùng kích thước file làm bằng chứng nội dung giống nhau. | APK split thiếu, ABI không hỗ trợ, hai lib cùng tên/kích thước nhưng khác nội dung, copy lỗi → lỗi rõ và rollback; test riêng SYSTEM và STORAGE. |
| C09 | P1 [verified], `packages/the_universe/core/src/main/java/com/dd/the/universe/core/system/pm/installer/RemoveUserExecutor.java:15`, `packages/the_universe/core/src/main/java/com/dd/the/universe/core/system/pm/BPackageManagerService.java:558`, `packages/the_universe/core/src/main/java/com/dd/the/universe/utils/BzFileUtils.java:160` | Kết quả deleteDir không biểu thị thành công đầy đủ; executor trả 0 và PM tiếp tục cập nhật state. Dùng trạng thái xóa có cấu trúc, tombstone/retry; không tái sử dụng userId khi dữ liệu cũ chưa xóa. UI cho ẩn mục lỗi chỉ với cảnh báo còn dữ liệu, không giả thành đã xóa. | Một file không xóa được → trạng thái pending/error; user mới không thấy dữ liệu cũ; retry xóa được và cập nhật UI đúng. |
| C10 | P1 [verified], `packages/the_universe/core/src/main/java/com/dd/the/universe/core/system/pm/Settings.java:114` và `:195` | UID Parcel thiếu schema rõ, lỗi đọc có thể reset; lỗi package config đi tới xóa thư mục app. Quarantine và backup, migration có phiên bản, phân biệt corruption/IO tạm thời; không coi mọi Throwable là dữ liệu cần xóa. | Cắt cụt config, schema mới, permission/IO lỗi → không xóa dữ liệu gốc; UID ổn định sau restart/migrate; thử recovery lặp lại. |
| C11 | P1 [verified], `app/src/main/java/com/duplicateapp/gamespace/features/workspace/data/persistent_workspace_repository.kt:106` | Một record JSON/enum lỗi làm cả danh sách thành rỗng; lần ghi sau có thể đè dữ liệu còn cứu được, legacy migration cũng có rủi ro. Backup, đọc từng record, validate ID/userId/duplicate và hiển thị recovery; công bố state sau khi lưu thành công. | Một record hỏng trong nhiều record, enum lạ, ID trùng/âm, storage lỗi → giữ record hợp lệ và bản gốc; không tự ghi đè danh sách rỗng. |
| C12 | P1 [verified + tái hiện JVM], `packages/the_universe/core/src/main/java/com/dd/the/universe/utils/TrieTree.java:68`, `packages/the_universe/core/src/main/java/com/dd/the/universe/core/IOCore.java:72` | Trie trả prefix đầu tiên, không kiểm tra segment; `/game2` khớp `/game`, `/game/lib` mất rule riêng. Dùng longest valid path prefix và thay đúng phần đầu, snapshot rule nhất quán. | Rule cha/con theo mọi thứ tự, sibling chung prefix, prefix lặp ở cuối đường dẫn, trailing slash → ánh xạ chính xác và giữ suffix. |
| C13 | P1 [verified], `packages/the_universe/core/src/main/java/com/dd/the/universe/core/IOCore.java:67` và `:141` | Blacklist Pictures trả chính prefix thay vì path, làm mất tên file. Chốt nghĩa blacklist là không redirect, trả nguyên path; không dùng blacklist như alias. | `/sdcard/Pictures/a.png` và đường dẫn emulated giữ đủ tên file; `Pictures2` không bị ảnh hưởng. |
| C14 | P1 [verified], `packages/the_universe/core/src/main/java/com/dd/the/universe/utils/SimpleCrashFix.java:40` | Global uncaught handler bỏ qua lỗi dựa vào chuỗi rộng như Context; return không phục hồi thread đã chết. Chỉ xử lý fallback tại call site, ghi lỗi đã lọc dữ liệu rồi delegate/terminate đúng; bỏ “swallow crash” toàn cục. | Lỗi chứa Context trên main/background thread → không tiếp tục UI giả đang khỏe; crash record và delegate đúng một lần. |
| C15 | P2 [verified], `packages/the_universe/core/src/main/java/com/dd/the/universe/fake/device/VirtualDeviceIdentity.java` | Fallback ID md5 theo userId cố định có thể giống trên nhiều máy; multi-process prefs và lỗi trở về user0 làm mờ scope. Giữ ID cũ khi migrate, tạo ID ngẫu nhiên ổn định theo installation+virtual user trong owner duy nhất; lỗi trả trạng thái rõ. | Restart giữ ID; user khác khác ID; migration không đổi ID đã có; failure không biến user2 thành user0. Không quảng cáo đây là cách ly bảo mật. |

### B. Native và giới hạn sản phẩm

| ID | Ưu tiên / bằng chứng | Vấn đề và hướng sửa | Test nghiệm thu |
| --- | --- | --- | --- |
| C16 | P1 [verified], `packages/the_universe/core/src/main/cpp/Utils/VirtualSpoof.cpp:97`, `packages/the_universe/core/src/main/cpp/Android.mk` | Xóa Java spoof chưa xóa native property spoof: constructor vẫn gọi DobbyHook, giá trị mặc định cố định. Theo hướng sản phẩm không giả thiết bị, bỏ nhánh này khỏi artifact sau impact check; không biến nó thành tính năng né anti-cheat. | Release binary không cài property hook ngoài thiết kế; runtime đọc property không bị thay bằng Pixel6 cố định; JNI registration không bị thiếu symbol. |
| C17 | P1 nếu còn nhánh spoof [verified], `packages/the_universe/core/src/main/cpp/Utils/VirtualSpoof.cpp:25` | strcpy từ chuỗi cấu hình vào buffer caller không có kiểm tra độ dài. Ưu tiên giải quyết cùng C16; nếu buộc giữ tạm, giới hạn theo hợp đồng API và từ chối đầu vào dài, xử lý null hợp lệ. Chưa chứng minh khai thác từ app ngoài. | Chuỗi rỗng, đúng giới hạn, vượt PROP_VALUE_MAX → không overflow với sanitizer; không chỉ sửa bằng truncate tùy tiện mọi property. |
| C18 | P2 [verified], `packages/the_universe/core/src/main/cpp/UniverseNativeCore.cpp:107`, `packages/the_universe/core/src/main/cpp/IO.cpp`, `packages/the_universe/core/src/main/cpp/IO.h` | GetStringUTFChars dùng cho rule không Release, raw pointer được giữ lâu dài. Sao chép vào vùng nhớ có owner, RAII cho JNI string; không chỉ thêm Release gây dangling pointer. | Thêm nhiều rule/reinit → không tăng leak tuyến tính, rule còn hợp lệ sau GC; cleanup theo vòng đời engine. |
| C19 | P2, lỗi tiềm ẩn [verified], `packages/the_universe/core/src/main/cpp/IO.cpp:22`, `packages/the_universe/core/src/main/cpp/Hook/FileSystemHook.cpp` | Helper dùng strlen trên malloc chưa khởi tạo; char* redirect còn vấn đề ownership, callback open đọc vararg vô điều kiện. Chưa thấy caller hiện hành của char* redirect; FileSystemHook.init chưa gắn các callback này. Xóa nhánh chết sau kiểm tra symbol hoặc sửa/test trước khi kích hoạt. | Harness ASan/UBSan, open không O_CREAT, rule dài → không UB/leak; không ghi đây là crash đã quan sát trong luồng hiện tại. |

### C. Trạng thái app, Premium và riêng tư

| ID | Ưu tiên / bằng chứng | Vấn đề và hướng sửa | Test nghiệm thu |
| --- | --- | --- | --- |
| C20 | P1 [verified] về thiếu điều phối, `app/src/main/java/com/duplicateapp/gamespace/features/workspace/presentation/workspace_view_model.kt:178` và `:276` | Launch/delete có thể chồng nhau, không in-flight gate theo copy; kết quả cũ có thể ghi running sau khi xóa. Dùng operation state/generation, tuần tự hóa theo copy và package khi thay binary, reconcile từ runtime. | Double tap, launch rồi delete, đổi selection khi delete, process death → không session ma, không chạy copy đã xóa, selection nhất quán với repository. |
| C21 | P2 [verified], `app/src/main/java/com/duplicateapp/gamespace/features/workspace/data/virtualized_game_launcher.kt` | Readiness luôn Ready; mọi launch failure bị gán permissionDenied, install failure thành gameNotInstalled. Truyền typed failure/retryability từ runtime, readiness phản ánh service/ABI/package thật; không mở dialog quyền cho lỗi binder. | Inject service unavailable, missing source APK, timeout, permission denial → thông điệp và hành động đúng từng loại. Phụ thuộc C01–C03. |
| C22 | P1 [verified], `app/src/main/java/com/duplicateapp/gamespace/features/workspace/presentation/workspace_view_model.kt:153`, `app/src/main/java/com/duplicateapp/gamespace/features/premium/data/revenue_cat_premium_repository.kt` | Refresh entitlement chỉ lúc ready; resume không refresh, callback lỗi có thể làm UI mất trạng thái trước. Thêm listener/refresh có dedupe, quy tắc cache/offline/unknown; reconcile sau purchase/restore/account change. | Hết hạn, refund, mua máy khác, offline, callback đến sai thứ tự → entitlement không bị ghi bởi yêu cầu cũ, không vĩnh viễn giữ dữ liệu stale. |
| C23 | P1 [verified], `app/src/main/java/com/duplicateapp/gamespace/features/workspace/presentation/workspace_view_model.kt:456`, `app/src/main/java/com/duplicateapp/gamespace/features/premium/data/revenue_cat_purchase_manager.kt:34` | Purchase callback luôn hiện premium_activated dù access inactive; userCancelled dùng đường unavailable. Phân biệt active/pending/no entitlement/cancel/error; chỉ thông báo kích hoạt khi entitlement đúng. | Callback không entitlement, pending, cancel, error, active → trạng thái và snackbar tương ứng; không báo đã mua thành công khi chưa có quyền. |
| C24 | P1 [verified], `app/src/main/java/com/duplicateapp/gamespace/features/premium/data/revenue_cat_purchase_manager.kt:26`, `app/src/main/java/com/duplicateapp/gamespace/features/workspace/presentation/components/settings_dialog.kt` | Tự chọn availablePackages.firstOrNull, UI chưa thể hiện offer/giá/chu kỳ tương ứng. Dùng lựa chọn sản phẩm có chủ đích, giá bản địa hóa và điều khoản từ SDK; không phụ thuộc thứ tự remote offering. | Đổi thứ tự monthly/yearly, offering trống, trial, giá locale khác → mua đúng SKU đã hiển thị; restore và quản lý subscription dễ thấy. |
| C25 | P1 [verified], `app/src/main/java/com/duplicateapp/gamespace/features/privacy/data/biometric_privacy_lock_coordinator.kt:48`, `app/build.gradle.kts:33` | BIOMETRIC_STRONG kết hợp DEVICE_CREDENTIAL không được AndroidX hỗ trợ trên API29 trong khi app hỗ trợ API29. Thiết kế nhánh API29 dùng phương án hỗ trợ đúng với yêu cầu bảo mật, kiểm tra canAuthenticate; không âm thầm hạ mức bảo mật. | Android10 có PIN/có biometric/không enrollment, Android11+ → bật/mở khóa đúng, không prompt exception, có hướng dẫn recovery. [Nguồn AndroidX](https://developer.android.com/reference/androidx/biometric/BiometricPrompt.PromptInfo.Builder). |
| C26 | P2 [verified], `app/src/main/java/com/duplicateapp/gamespace/main_activity.kt:78` và `:106`, `app/src/main/java/com/duplicateapp/gamespace/features/workspace/presentation/workspace_view_model.kt` | FLAG_SECURE cập nhật ở lifecycle, không ngay khi đổi toggle; cửa sổ còn mở có thể giữ trạng thái cũ. Observe lock setting trực tiếp ở Activity, đồng bộ flag ngay; mô tả rõ khóa workspace, không khóa toàn bộ guest. | Bật/tắt trong cùng resume, recents, rotation, cold start và shortcut → flag/UI nhất quán; kiểm thử guest recents riêng ở G02. |
| C27 | P2 [verified] về đường lỗi, `app/src/main/java/com/duplicateapp/gamespace/features/workspace/presentation/workspace_view_model.kt:425`, `app/src/main/java/com/duplicateapp/gamespace/features/auth/data/firebase_auth_repository.kt`, `app/src/main/java/com/duplicateapp/gamespace/features/premium/data/revenue_cat_premium_repository.kt` | Auth/restore thiếu finally cho busy flag; logout không phối hợp kết quả Firebase/RevenueCat, callback cũ có thể khôi phục entitlement nhầm identity. Dùng state machine/generation, finally, giữ cancellation semantics; lỗi RC logout phải hiển thị/retry. | Throw tại mỗi suspend step, hủy sign-in, sign-out trong refresh → không kẹt busy, không gán purchase của identity cũ cho tài khoản mới. |
| C28 | P2 [verified], `app/src/main/java/com/duplicateapp/gamespace/features/workspace/data/android_workspace_shortcut_publisher.kt:18` | Dựng ShortcutInfo ngoài try; tên rỗng/corrupt có thể thoát catch. Giới hạn cố định4 không theo launcher; chỉ thay dynamic list không vô hiệu shortcut pinned đã xóa. Validate đầu vào, bao phủ builder, dùng maxShortcutCount và disable/remove theo ID; tùy chọn nhãn riêng tư. | Tên trắng, dữ liệu cũ, launcher limit thấp, pinned session bị xóa → không crash/launch nhầm, nhãn lỗi rõ. |
| C29 | P2 [verified] về code, [inferred] về kẹt, `app/src/main/java/com/duplicateapp/gamespace/features/update/data/force_update_coordinator.kt` | Không xử lý boolean startUpdateFlowForResult; thất bại check chỉ log, có thể giữ required/pending cũ. State machine có retry và đường hỗ trợ, chốt policy khi offline; không vòng lặp ép cập nhật vô hạn. | start=false, check fail sau required=true, user cancel, process recreation và in-progress → UI có trạng thái/hành động hợp lệ. |

### D. Kiểm thử, release và bề mặt dữ liệu

| ID | Ưu tiên / bằng chứng | Vấn đề và hướng sửa | Test nghiệm thu |
| --- | --- | --- | --- |
| C30 | P2, chỉ công cụ nội bộ [verified], `tools/play_test_companion/play_test_companion/server.py:39` | Local POST thiếu kiểm tra Origin/Host/token, một số endpoint không cần body; Content-Length không giới hạn. Bind loopback là tốt nhưng không đủ tự thân. Thêm token phiên/CSRF, allowlist Host/Origin, giới hạn payload và lỗi có kiểm soát. Chưa làm PoC trên browser cụ thể. | POST từ origin lạ/Host giả, thiếu token, length quá lớn → từ chối, không chạy adb/campaign; UI hợp lệ vẫn hoạt động. |
| C31 | P1 cho độ tin cậy gate [verified], `tools/play_test_companion/play_test_companion/adb_runner.py:43`, `tools/android_test_companion/src/main/java/com/duplicateapp/testcompanion/domain/test_scenario.kt` | Package/foreground kiểm tra substring, log chỉ tìm FATAL EXCEPTION trong cửa sổ không gắn run/PID; lệnh log lỗi có thể thành pass, test UI companion chưa kiểm tra guest. Parse exact package/focused activity, kiểm tra return code, mốc log/PID và native crash/ANR/freezer; kết quả unknown không thành passed. Monkey chỉ chạy dữ liệu thử nghiệm vì có thể kích hoạt thao tác thật. | Cài package có cùng prefix, app ở background, logcat fail, native crash/ANR, clone mở sai user → run không pass; scenario clone phải kiểm tra identity từ bên trong guest. |
| C32 | P1 [verified], `app/build.gradle.kts:15` | Release cho phép key trống và AdMob App ID test fallback; build được không chứng minh doanh thu cấu hình đúng. Validate cấu hình release theo feature được bật, giữ dev dùng test ID; không in giá trị bí mật. | Bỏ từng key hoặc dùng test ID khi release monetization bật → build fail với tên key, không có giá trị; debug/test vẫn build, owner xác nhận môi trường sandbox. |
| C33 | P2 [verified + đếm resource], `app/src/main/res/values/strings.xml`, `app/src/main/res/values-vi/strings.xml` và locale khác | 9 locale có117/144 string, thiếu27 mỗi locale; mô tả 2 copy và giới hạn5 không nhất quán. Dùng tham số từ domain limit, dịch đầy đủ hoặc thu hẹp locale công bố. | So sánh key/placeholder/plural mọi locale, screenshot font lớn/RTL nếu hỗ trợ; mọi màn nói đúng giới hạn hiện hành. |
| C34 | P2 [verified], `app/src/main/java/com/duplicateapp/gamespace/core/logging/app_logger.kt` | INFO log cả payload trong release, có package game user chọn. Giữ diagnostic tối thiểu với allowlist/redaction và correlation ID, tắt log hành vi chi tiết release; không chuyển nguyên payload sang analytics. | Release không có tên tài khoản/ID/installed-package inventory/token; lỗi vẫn có mã nguyên nhân đủ hỗ trợ, logging không đổi flow. |
| C35 | P1, bề mặt cần harden [verified], `packages/the_universe/core/src/main/AndroidManifest.xml:163`, `packages/the_universe/core/src/main/java/com/dd/the/universe/proxy/ProxyActivity.java:25` | Nhiều proxy exported; Activity lấy nested target intent và gọi startActivity chưa có xác thực route ở lớp này. Audit từng activity/service/receiver, đóng entry không cần, broker/token hoặc validated explicit route cho entry cần; không dựa Binder.getCallingUid trong Activity để xác thực app gọi. Chưa chứng minh mọi component đều khai thác được. | App ngoài gửi intent giả/null/parcel lạ, redirect sang component ngoài/nhạy cảm → bị chặn, không crash; PendingIntent/guest hợp lệ vẫn chạy. JobService có BIND_JOB_SERVICE phải giữ hợp đồng hệ thống. |
| C36 | P1, thiếu luồng [verified], `app/src/main/java/com/duplicateapp/gamespace/features/auth/data/firebase_auth_repository.kt`, `app/src/main/java/com/duplicateapp/gamespace/features/workspace/presentation/components/settings_dialog.kt` | Có Firebase sign-in tạo tài khoản nhưng chỉ sign-out, chưa có đường xóa account. Thiết kế yêu cầu xóa trong app và web, reauth, xử lý dữ liệu liên quan theo retention; phân biệt xóa account, xóa clone và hủy subscription. Không tự xóa giao dịch phải lưu hay hứa xóa Firebase là đủ. [Yêu cầu Play](https://support.google.com/googleplay/android-developer/answer/13327111?hl=en). | Tài khoản mới/cũ, cần reauth, offline và retry → yêu cầu có trạng thái rõ; web hoạt động không cần cài app, dữ liệu liên quan được xử lý theo policy. |
| C37 | P1 [verified], `docs/privacy-policy.md:13`, `docs/store-readiness.md`, `docs/google-play-launch-plan.md` | Policy còn placeholder và mô tả managed profile không đúng engine hiện tại; hướng dẫn release cũ dễ dẫn tới khai báo sai. Viết lại hành vi thực, dữ liệu từng SDK/clone, retention/xóa/liên hệ, publish HTTPS và đồng bộ Data safety từ artifact cuối. | Không còn placeholder, liên kết mở được, mô tả và khai báo khớp hành vi network/storage đã kiểm tra; publisher chịu trách nhiệm xác nhận nội dung pháp lý. |

### E. Các cửa kiểm chứng bắt buộc, chưa phải lỗi đã chứng minh

| ID | Mức | Công việc / bằng chứng cần đạt |
| --- | --- | --- |
| G01 | P1 | Ma trận Android/OEM/ABI: cold install, warm launch, force-stop, reboot, server kill/freeze, WebView/login, services/receivers, permission denial, update game. Historical SDK36 emulator từng bị server cached/frozen; cần tái hiện có timestamp/PID, không khẳng định chỉ xảy ra lần đầu hoặc Android35+ luôn hỏng. Không dùng thiếu nhánh BuildCompat hoặc một hidden-API log để kết luận toàn engine không hỗ trợ. |
| G02 | P1 | Chứng minh tách dữ liệu bằng guest fixture chạy thật: preferences/SQLite/files/cache/native IO/cookie/Android ID, user1/user2, delete/recreate. Test hiện có `app/src/androidTest/java/com/duplicateapp/gamespace/features/virtualization/data/the_universe_virtual_game_runtime_test.kt` viết marker từ host, chưa chứng minh IO trong guest. Engine cùng Linux UID không phải ranh giới OS sandbox; không quảng cáo cách ly bảo mật tuyệt đối, khóa host không mặc nhiên che guest recents. |
| G03 | P1 | Xác minh provenance và giấy phép toàn bộ code vendored/native/prebuilt, giữ copyright/NOTICE đúng nghĩa vụ. Namespace cũ là đầu mối, không đủ chứng minh chính xác upstream/license. Không khẳng định đổi tên loại bỏ nghĩa vụ hoặc tự tuyên bố vi phạm pháp lý. |
| G04 | P1 | Kiểm tra AAB đã ký, merged manifest prodRelease, exported/permission allowlist theo mục đích, symbol/R8/reflection và mọi .so ở 16KB. `packages/the_universe/core/src/main/cpp/Android.mk` đã có max-page-size=16384: không mở lại lỗi “thiếu flag”; vẫn phải kiểm tra ELF/zip và chạy thiết bị 16KB. [Hướng dẫn Android](https://developer.android.com/guide/practices/page-sizes). Ký ngoài Gradle có thể hợp lệ; thiếu signingConfig không chứng minh owner chưa có key. |
| G05 | P1 | Sandbox thật cho purchase/restore/renewal/refund/pending/cancel/identity transfer và consent ads; kiểm tra config do owner xác nhận, không đọc production secrets. Checklist PENDING_SETUP không chứng minh console chưa thiết lập. Không bấm mua thật hay upload store trong kế hoạch này. |
| G06 | P1 | Hồ sơ Play: target API/permission declaration/foreground service specialUse nếu có trong artifact cuối, Data safety, privacy/deletion URL, reviewer instructions, ảnh và listing. Xác minh yêu cầu tester/production access trực tiếp trên loại tài khoản hiện tại; test companion không thay thế Play Console hay chứng minh opt-in liên tục. |
| G07 | P2 | Hiệu năng và ổn định: đo cold/warm launch, RAM nhiều copy, bộ nhớ native, battery/background và app size trên cùng fixture. Chưa có benchmark để kết luận engine chậm do số component hay R8 gần như vô hiệu. |
| G08 | P1 | Bổ sung regression ở module engine: hiện tìm thấy JarManagerTest trong main, không phải suite core. Chọn test seam/runner phù hợp rồi chứng minh test mới đỏ trước sửa, xanh sau sửa; không dùng 12 unit test host để duyệt release engine. |

### F. Cải tiến tùy chọn và phần đã xử lý

1. O01 — P3: adaptive banner và lifecycle pause/resume/destroy trong `app/src/main/java/com/duplicateapp/gamespace/features/ads/presentation/banner_ad.kt`; đo trên thiết bị trước/sau, không hứa tăng eCPM.
2. O02 — P3: free2/paid5 là lựa chọn sản phẩm, không phải entitlement đang thiếu theo spec. Chỉ quyết sau kiểm chứng giá trị/chi phí và có grandfathering, không khóa các copy đã có khi subscription hết hạn.
3. O03 — P3: health/compatibility screen, retry và xuất diagnostic đã redaction, phục vụ C01/C21; không thêm root hiding, fake GPS, bypass anti-cheat hay macro vào phạm vi.
4. O04 — P3: thu hẹp `packages/the_universe/core/consumer-rules.pro` bằng usage report và reflection tests, không xóa keep hàng loạt. Keep android/com.android không đồng nghĩa giữ toàn bộ package app.
5. O05 — P3: dọn nhánh managed-profile/VPN/hook chết sau kiểm tra caller và artifact, giảm warning native có kiểm soát; không refactor lớn trước khi có regression nền.

[verified theo kiểm tra mã nguồn ở HEAD] Không mở lại thành việc chưa làm: loại log upload/shell đã xử lý; Java spoof đã thay bằng VirtualDeviceIdentity; VPN init/manifest và LauncherActivity không còn trong luồng cũ; proxy content provider đã chuyển không exported; dependency/core permission đã giảm. G04 phải kiểm tra artifact cuối để tránh hồi quy, không lặp lại checklist dựa trên manifest cũ.

Đính chính quan trọng: `packages/the_universe/core/src/main/cpp/Utils/AntiDetection.cpp:230` chỉ mở/đóng libc và log; constructor có chạy nhưng không DobbyHook các callback file. `packages/the_universe/core/src/main/cpp/Hook/FileSystemHook.cpp` cũng chưa gắn callback đang được bàn tới. Không gọi đây là root hiding đang hoạt động. Trái lại VirtualSpoof có DobbyHook thật (C16). Không kết luận mất mọi login từ việc reset UID config, không lấy tổng exported làm số lỗ hổng, và không gọi emulator là thiết bị thật.

## 7. Thứ tự triển khai

1. **Nền test và hợp đồng kết quả:** bổ sung fixture/fault injection, định nghĩa launch/install/uninstall state tương thích API; C01–C03, C21, C31 và phần đầu G08. Chưa sửa mọi hook cùng lúc.
2. **Vòng đời/process:** C04–C06 và C20; test multi-user, timeout, binder generation. Phải xong trước khi dùng E2E launch để đánh giá các thay đổi tiếp theo.
3. **Không mất dữ liệu:** C07–C11, C15; transaction staging/rollback, tombstone, backup/migration. Giữ format cũ đọc được, không reset dữ liệu để làm test xanh.
4. **Đường dẫn/native:** C12–C19; longest-prefix/blacklist trước, ownership JNI và loại spoof sau impact check; không kích hoạt hook đang chết để tiện thử.
5. **IPC/riêng tư:** C25–C28, C34–C36; audit entry points theo loại, khóa đúng phạm vi, xóa account có thiết kế xác nhận. Không đóng mọi exported đồng loạt.
6. **Premium và trải nghiệm:** C22–C24, C29, C33; có sandbox offer và contract entitlement trước. O01/O02 tách quyết định, không block core bằng thử nghiệm giá.
7. **Công cụ/release:** C30, C32, C37, G03–G06. Chạy trên artifact cuối, owner xác nhận cấu hình/pháp lý; snapshot/golden/manifest baseline cập nhật một lần ở cuối nhóm thay đổi tương ứng.
8. **Đóng vòng:** G01/G02/G07/G08 trên matrix đã thống nhất; lỗi mới quay lại cùng backlog với reproduction. Chỉ ký duyệt release khi hết P1 và mọi gate P1 có bằng chứng; P2 trì hoãn phải có owner/lý do.

Mỗi bước nên là PR/commit độc lập với test liên quan, conventional commit không scope ngoặc, không AI co-author; không push/upload khi chưa được yêu cầu. Không áp đặt tiếp tục đúng30 commit cho kế hoạch mới.

## 8. Chiến lược kiểm thử và kết quả lượt scan

[verified, đã chạy] 12 test hiện có pass: VirtualizedGameLauncherTest6, PremiumAccessTest3, ForceUpdatePolicyTest3. Đây là kiểm tra phạm vi host đã chọn, không phải toàn suite hoặc chứng nhận release.

```sh
JAVA_HOME='/Applications/Android Studio.app/Contents/jbr/Contents/Home' ./gradlew :app:testProdDebugUnitTest --tests '*VirtualizedGameLauncherTest' --tests '*PremiumAccessTest' --tests '*ForceUpdatePolicyTest' --offline
```

[verified, đã chạy] Harness Java tạm ngoài repo compile trực tiếp TrieTree hiện hành: rule `/data/data/game` và `/data/data/game/lib` cho kết quả `/data/data/game` khi tìm library; `/data/data/game2/files/x` cũng khớp `/data/data/game`; Pictures trả prefix. Tái hiện hành vi matcher, chưa chạy IO trên Android. Harness không được coi là regression đã commit.

[verified, đã chạy] Kiểm kê strings: en/vi144; de/es/fr/hi/in/ja/ko/pt-rBR/zh-rCN117, mỗi locale thiếu27. Build có cảnh báo deprecation Gradle10, không có lỗi ở lệnh test trên; chưa xác định warning đến từ plugin hay code dự án, đưa vào cleanup sau triage, không tự nâng Gradle.

Các test hiện có để mở rộng:

- `app/src/test/java/com/duplicateapp/gamespace/features/workspace/data/virtualized_game_launcher_test.kt`: unavailable/timeout và phân loại lỗi C01–C03/C21.
- `app/src/test/java/com/duplicateapp/gamespace/features/premium/domain/premium_access_test.kt`: contract unknown/offline/active; thêm test orchestration riêng cho purchase/refresh, không ép domain test gọi SDK.
- `app/src/test/java/com/duplicateapp/gamespace/features/update/domain/force_update_policy_test.kt`: giữ policy; thêm coordinator fake cho start=false/network failure.
- `app/src/androidTest/java/com/duplicateapp/gamespace/features/workspace/data/persistent_workspace_repository_test.kt`: corruption/partial migration/write failure, không chỉ happy migration.
- `app/src/androidTest/java/com/duplicateapp/gamespace/features/workspace/data/android_workspace_shortcut_publisher_test.kt`: invalid label, quota, pinned deletion.
- `app/src/androidTest/java/com/duplicateapp/gamespace/features/virtualization/data/the_universe_virtual_game_runtime_test.kt`: guest fixture thực sự launch, không chỉ host marker.

Test mới đề xuất, chưa tồn tại: Java `TrieTreeTest`, `InstallTransactionTest`, `ProcessLifecycleTest`, `SettingsRecoveryTest` trong module core sau khi thiết lập runner; Kotlin `workspace_view_model_test.kt`, `revenue_cat_purchase_manager_test.kt`, `biometric_privacy_lock_coordinator_test.kt`; Python `test_server_security.py`, `test_adb_runner.py`. Mọi test dùng dữ liệu tạm/fixture, không user game production. Scope command theo class/module thay đổi, không chạy cả repo chỉ để kiểm một sửa nhỏ.

Thiết bị: API29 cho biometric; ít nhất một Android hiện hành/target-supported, một OEM thật, một ABI64/page16KB; thêm API/ABI còn tuyên bố hỗ trợ. Test gián đoạn tại mỗi bước transaction, guest login dữ liệu giả, 2–5 copy, nhiều lần restart/kill. Chưa có bằng chứng cho ma trận này trong lượt scan, vì vậy gate vẫn mở.

## 9. Rủi ro và phạm vi ảnh hưởng

1. Transaction/UID/path là vùng nguy hiểm nhất: caller gồm install/update/uninstall và tất cả virtual user. Rollback phải giữ cả metadata lẫn APK/libs; rollback binary không được rollback dữ liệu guest một cách tùy tiện.
2. Map key không đồng nghĩa client identity: audit ProcessRecord/AppConfig/BUserHandle trước đổi buid, giữ Binder/Parcelable contracts. Không có danh sách caller d=1 đáng tin từ chỉ mục cũ; source chain §4 là tối thiểu, executor phải kiểm tra caller mới trước thay API.
3. IPC hardening có thể làm PendingIntent, JobScheduler và activity proxy hợp lệ hỏng. Bảo vệ theo loại component và đường vào thực, có app attacker fixture độc lập.
4. Native ownership và removal có thể gây dangling pointer hoặc missing JNI symbol. Kiểm tra cả ABI đóng gói, không chỉ JVM test.
5. Thay entitlement/cache/identity có rủi ro khóa nhầm người đã mua; không suy ra inactive từ network error, không cấp quyền vô hạn từ stale cache. Chốt policy offline trước triển khai.
6. Logger phải cân bằng khả năng chẩn đoán với tối thiểu dữ liệu. Không log token, account ID, nguyên danh sách game hoặc nội dung guest để làm “đủ log”.

## 10. Các tệp dự kiến thay đổi

Danh sách đường dẫn chính xác và trách nhiệm nằm ở từng C01–C37 và `files_to_modify` trong §11. Ưu tiên nhóm nhỏ, không đổi tất cả trong một commit.

| Nhóm | Phạm vi | Phụ thuộc |
| --- | --- | --- |
| Runtime/process | core launch, framework clients, PM/process manager, app runtime/launcher/ViewModel | C01–C06, C20–C21 |
| Dữ liệu/IO | installers, Settings, workspace repository, TrieTree/IOCore, virtual identity | C07–C15 |
| Native | VirtualSpoof, IO ownership, JNI bridge và danh sách nguồn Android.mk | C16–C19 |
| Sản phẩm | auth, Premium, privacy, shortcuts, update, strings/logger | C22–C29, C33–C36 |
| Phát hành | app build config, manifests/proxy, policy/store docs, test companions | C30–C32, C35–C37 và G01–G08 |

## 11. Ngữ cảnh triển khai có thể tái sử dụng

```json
{
  "implementation_context": {
    "task_summary": "Kế hoạch hợp nhất cải thiện core và release; chỉ lập kế hoạch, chưa sửa code.",
    "acceptance_criteria": [
      "C01–C37 được xử lý hoặc có quyết định rõ",
      "Đóng mọi P1 và gate P1 trước production",
      "Giữ dữ liệu/ID/hợp đồng Android-JNI-reflection",
      "Không suy ra zero bug từ scan/test pass"
    ],
    "evidence_provenance": {
      "schema_version": 2,
      "head_commit": "043f7197d781c316e639566c138cd7fd214726d5",
      "generated_plan_path": "docs/plans/2026-09-26-gitnexus-plan-app-core-release-improvements.md",
      "global_dirty_digest": {
        "algorithm": "sha256",
        "canonicalization": "gitnexus-evidence-provenance-v2 NUL-framed UTF-8 records",
        "value": "0a9c85780067d9afcd0764f307b60891e3cee927ee11eaeb5ec7826d10fd82cd"
      },
      "cited_path_manifest": [
        {
          "path": "app/build.gradle.kts",
          "object_kind": {
            "head": "regular",
            "index": "regular",
            "worktree": "regular",
            "untracked": "absent"
          },
          "state": "clean",
          "rename_from": null,
          "rename_to": null,
          "head_digest": "sha256:22ce032cba1271c131692205d8849addf0cfa36c5f0097235e59cfd76d7ce165",
          "index_digest": "sha256:22ce032cba1271c131692205d8849addf0cfa36c5f0097235e59cfd76d7ce165",
          "worktree_digest": "sha256:22ce032cba1271c131692205d8849addf0cfa36c5f0097235e59cfd76d7ce165",
          "untracked_digest": "absent"
        },
        {
          "path": "app/src/androidTest/java/com/duplicateapp/gamespace/features/virtualization/data/the_universe_virtual_game_runtime_test.kt",
          "object_kind": {
            "head": "regular",
            "index": "regular",
            "worktree": "regular",
            "untracked": "absent"
          },
          "state": "clean",
          "rename_from": null,
          "rename_to": null,
          "head_digest": "sha256:a307b39f94eea2851a333c17ab290bacea5c7437560540a5b73e41403eb79468",
          "index_digest": "sha256:a307b39f94eea2851a333c17ab290bacea5c7437560540a5b73e41403eb79468",
          "worktree_digest": "sha256:a307b39f94eea2851a333c17ab290bacea5c7437560540a5b73e41403eb79468",
          "untracked_digest": "absent"
        },
        {
          "path": "app/src/androidTest/java/com/duplicateapp/gamespace/features/workspace/data/android_workspace_shortcut_publisher_test.kt",
          "object_kind": {
            "head": "regular",
            "index": "regular",
            "worktree": "regular",
            "untracked": "absent"
          },
          "state": "clean",
          "rename_from": null,
          "rename_to": null,
          "head_digest": "sha256:52c7a4954860b11d838cdc3ca180cc07fedf49f1691bcc6af8ad92f9bdc15c21",
          "index_digest": "sha256:52c7a4954860b11d838cdc3ca180cc07fedf49f1691bcc6af8ad92f9bdc15c21",
          "worktree_digest": "sha256:52c7a4954860b11d838cdc3ca180cc07fedf49f1691bcc6af8ad92f9bdc15c21",
          "untracked_digest": "absent"
        },
        {
          "path": "app/src/androidTest/java/com/duplicateapp/gamespace/features/workspace/data/persistent_workspace_repository_test.kt",
          "object_kind": {
            "head": "regular",
            "index": "regular",
            "worktree": "regular",
            "untracked": "absent"
          },
          "state": "clean",
          "rename_from": null,
          "rename_to": null,
          "head_digest": "sha256:6b438fd91ba4c629da2cf05a9025b45e360d0f9edd2eaac284c021be3572dc94",
          "index_digest": "sha256:6b438fd91ba4c629da2cf05a9025b45e360d0f9edd2eaac284c021be3572dc94",
          "worktree_digest": "sha256:6b438fd91ba4c629da2cf05a9025b45e360d0f9edd2eaac284c021be3572dc94",
          "untracked_digest": "absent"
        },
        {
          "path": "app/src/main/java/com/duplicateapp/gamespace/core/logging/app_logger.kt",
          "object_kind": {
            "head": "regular",
            "index": "regular",
            "worktree": "regular",
            "untracked": "absent"
          },
          "state": "clean",
          "rename_from": null,
          "rename_to": null,
          "head_digest": "sha256:e9fa9ba233292e94463b12928d12875939595c212f1e05f4c99361f21cf1247c",
          "index_digest": "sha256:e9fa9ba233292e94463b12928d12875939595c212f1e05f4c99361f21cf1247c",
          "worktree_digest": "sha256:e9fa9ba233292e94463b12928d12875939595c212f1e05f4c99361f21cf1247c",
          "untracked_digest": "absent"
        },
        {
          "path": "app/src/main/java/com/duplicateapp/gamespace/features/ads/presentation/banner_ad.kt",
          "object_kind": {
            "head": "regular",
            "index": "regular",
            "worktree": "regular",
            "untracked": "absent"
          },
          "state": "clean",
          "rename_from": null,
          "rename_to": null,
          "head_digest": "sha256:b985f5515ba24bd1d6d7935aa627788b4979b5fdca5b1b16a5a75faa1582d63e",
          "index_digest": "sha256:b985f5515ba24bd1d6d7935aa627788b4979b5fdca5b1b16a5a75faa1582d63e",
          "worktree_digest": "sha256:b985f5515ba24bd1d6d7935aa627788b4979b5fdca5b1b16a5a75faa1582d63e",
          "untracked_digest": "absent"
        },
        {
          "path": "app/src/main/java/com/duplicateapp/gamespace/features/auth/data/firebase_auth_repository.kt",
          "object_kind": {
            "head": "regular",
            "index": "regular",
            "worktree": "regular",
            "untracked": "absent"
          },
          "state": "clean",
          "rename_from": null,
          "rename_to": null,
          "head_digest": "sha256:391d7b13f7392541829375de1080e98a7908ceadb32926c04be3cd9baac77314",
          "index_digest": "sha256:391d7b13f7392541829375de1080e98a7908ceadb32926c04be3cd9baac77314",
          "worktree_digest": "sha256:391d7b13f7392541829375de1080e98a7908ceadb32926c04be3cd9baac77314",
          "untracked_digest": "absent"
        },
        {
          "path": "app/src/main/java/com/duplicateapp/gamespace/features/premium/data/revenue_cat_premium_repository.kt",
          "object_kind": {
            "head": "regular",
            "index": "regular",
            "worktree": "regular",
            "untracked": "absent"
          },
          "state": "clean",
          "rename_from": null,
          "rename_to": null,
          "head_digest": "sha256:971b4c3d2d396111a167f8cb108809205694521d09d500d241b3dda40ccca3ee",
          "index_digest": "sha256:971b4c3d2d396111a167f8cb108809205694521d09d500d241b3dda40ccca3ee",
          "worktree_digest": "sha256:971b4c3d2d396111a167f8cb108809205694521d09d500d241b3dda40ccca3ee",
          "untracked_digest": "absent"
        },
        {
          "path": "app/src/main/java/com/duplicateapp/gamespace/features/premium/data/revenue_cat_purchase_manager.kt",
          "object_kind": {
            "head": "regular",
            "index": "regular",
            "worktree": "regular",
            "untracked": "absent"
          },
          "state": "clean",
          "rename_from": null,
          "rename_to": null,
          "head_digest": "sha256:9d86d48c1104ae2bb4c9e281f99d45f32a05d3cb56628d2f984ab80e674375cb",
          "index_digest": "sha256:9d86d48c1104ae2bb4c9e281f99d45f32a05d3cb56628d2f984ab80e674375cb",
          "worktree_digest": "sha256:9d86d48c1104ae2bb4c9e281f99d45f32a05d3cb56628d2f984ab80e674375cb",
          "untracked_digest": "absent"
        },
        {
          "path": "app/src/main/java/com/duplicateapp/gamespace/features/privacy/data/biometric_privacy_lock_coordinator.kt",
          "object_kind": {
            "head": "regular",
            "index": "regular",
            "worktree": "regular",
            "untracked": "absent"
          },
          "state": "clean",
          "rename_from": null,
          "rename_to": null,
          "head_digest": "sha256:527bd4b8245433b3bddc5da6f73dffc0f14b04bc4c593dd74b12c8a2ab4cd58b",
          "index_digest": "sha256:527bd4b8245433b3bddc5da6f73dffc0f14b04bc4c593dd74b12c8a2ab4cd58b",
          "worktree_digest": "sha256:527bd4b8245433b3bddc5da6f73dffc0f14b04bc4c593dd74b12c8a2ab4cd58b",
          "untracked_digest": "absent"
        },
        {
          "path": "app/src/main/java/com/duplicateapp/gamespace/features/update/data/force_update_coordinator.kt",
          "object_kind": {
            "head": "regular",
            "index": "regular",
            "worktree": "regular",
            "untracked": "absent"
          },
          "state": "clean",
          "rename_from": null,
          "rename_to": null,
          "head_digest": "sha256:96673867a64b3fed421fb5a7f1d595ef83029a1536506861f09e9a7ed6e8a33d",
          "index_digest": "sha256:96673867a64b3fed421fb5a7f1d595ef83029a1536506861f09e9a7ed6e8a33d",
          "worktree_digest": "sha256:96673867a64b3fed421fb5a7f1d595ef83029a1536506861f09e9a7ed6e8a33d",
          "untracked_digest": "absent"
        },
        {
          "path": "app/src/main/java/com/duplicateapp/gamespace/features/virtualization/data/the_universe_virtual_game_runtime.kt",
          "object_kind": {
            "head": "regular",
            "index": "regular",
            "worktree": "regular",
            "untracked": "absent"
          },
          "state": "clean",
          "rename_from": null,
          "rename_to": null,
          "head_digest": "sha256:6598f7a402c8626ef3bfe2c301f81a8402c00ebcee2497eb8e9a292a2bad0a0f",
          "index_digest": "sha256:6598f7a402c8626ef3bfe2c301f81a8402c00ebcee2497eb8e9a292a2bad0a0f",
          "worktree_digest": "sha256:6598f7a402c8626ef3bfe2c301f81a8402c00ebcee2497eb8e9a292a2bad0a0f",
          "untracked_digest": "absent"
        },
        {
          "path": "app/src/main/java/com/duplicateapp/gamespace/features/workspace/data/android_workspace_shortcut_publisher.kt",
          "object_kind": {
            "head": "regular",
            "index": "regular",
            "worktree": "regular",
            "untracked": "absent"
          },
          "state": "clean",
          "rename_from": null,
          "rename_to": null,
          "head_digest": "sha256:fd9da3493b5ef1f84c1cafb19f9961d532bc63bb47b8be3467a5d3faea1839e9",
          "index_digest": "sha256:fd9da3493b5ef1f84c1cafb19f9961d532bc63bb47b8be3467a5d3faea1839e9",
          "worktree_digest": "sha256:fd9da3493b5ef1f84c1cafb19f9961d532bc63bb47b8be3467a5d3faea1839e9",
          "untracked_digest": "absent"
        },
        {
          "path": "app/src/main/java/com/duplicateapp/gamespace/features/workspace/data/persistent_workspace_repository.kt",
          "object_kind": {
            "head": "regular",
            "index": "regular",
            "worktree": "regular",
            "untracked": "absent"
          },
          "state": "clean",
          "rename_from": null,
          "rename_to": null,
          "head_digest": "sha256:670bfc2a316fe552f62211c7220a456dba59b7baa9d9cd88ff58969b06bb63f2",
          "index_digest": "sha256:670bfc2a316fe552f62211c7220a456dba59b7baa9d9cd88ff58969b06bb63f2",
          "worktree_digest": "sha256:670bfc2a316fe552f62211c7220a456dba59b7baa9d9cd88ff58969b06bb63f2",
          "untracked_digest": "absent"
        },
        {
          "path": "app/src/main/java/com/duplicateapp/gamespace/features/workspace/data/virtualized_game_launcher.kt",
          "object_kind": {
            "head": "regular",
            "index": "regular",
            "worktree": "regular",
            "untracked": "absent"
          },
          "state": "clean",
          "rename_from": null,
          "rename_to": null,
          "head_digest": "sha256:c210eb2a9bb6d6ad579fa601554ac96ee037f7a465eee1b728d41e0c9928f0b5",
          "index_digest": "sha256:c210eb2a9bb6d6ad579fa601554ac96ee037f7a465eee1b728d41e0c9928f0b5",
          "worktree_digest": "sha256:c210eb2a9bb6d6ad579fa601554ac96ee037f7a465eee1b728d41e0c9928f0b5",
          "untracked_digest": "absent"
        },
        {
          "path": "app/src/main/java/com/duplicateapp/gamespace/features/workspace/presentation/components/settings_dialog.kt",
          "object_kind": {
            "head": "regular",
            "index": "regular",
            "worktree": "regular",
            "untracked": "absent"
          },
          "state": "clean",
          "rename_from": null,
          "rename_to": null,
          "head_digest": "sha256:16d0664723172b8e9265975cb9b5b2a73aa37efcf770b934c2d63ffc531fb29e",
          "index_digest": "sha256:16d0664723172b8e9265975cb9b5b2a73aa37efcf770b934c2d63ffc531fb29e",
          "worktree_digest": "sha256:16d0664723172b8e9265975cb9b5b2a73aa37efcf770b934c2d63ffc531fb29e",
          "untracked_digest": "absent"
        },
        {
          "path": "app/src/main/java/com/duplicateapp/gamespace/features/workspace/presentation/workspace_view_model.kt",
          "object_kind": {
            "head": "regular",
            "index": "regular",
            "worktree": "regular",
            "untracked": "absent"
          },
          "state": "clean",
          "rename_from": null,
          "rename_to": null,
          "head_digest": "sha256:37a1d0b83bcc71770487b13e5ccafa6f942e78dd28ffaab281d73cce94bd387d",
          "index_digest": "sha256:37a1d0b83bcc71770487b13e5ccafa6f942e78dd28ffaab281d73cce94bd387d",
          "worktree_digest": "sha256:37a1d0b83bcc71770487b13e5ccafa6f942e78dd28ffaab281d73cce94bd387d",
          "untracked_digest": "absent"
        },
        {
          "path": "app/src/main/java/com/duplicateapp/gamespace/main_activity.kt",
          "object_kind": {
            "head": "regular",
            "index": "regular",
            "worktree": "regular",
            "untracked": "absent"
          },
          "state": "clean",
          "rename_from": null,
          "rename_to": null,
          "head_digest": "sha256:aef6ffc9906846b472ff09b8637c0f310e0f29b70bb86f32e78162d0c066d4f2",
          "index_digest": "sha256:aef6ffc9906846b472ff09b8637c0f310e0f29b70bb86f32e78162d0c066d4f2",
          "worktree_digest": "sha256:aef6ffc9906846b472ff09b8637c0f310e0f29b70bb86f32e78162d0c066d4f2",
          "untracked_digest": "absent"
        },
        {
          "path": "app/src/main/res/values-de/strings.xml",
          "object_kind": {
            "head": "regular",
            "index": "regular",
            "worktree": "regular",
            "untracked": "absent"
          },
          "state": "clean",
          "rename_from": null,
          "rename_to": null,
          "head_digest": "sha256:3b720a1d9d54bb9e7f89aa56638d9bb2a1fc0a5530d253b950e6884fd057498f",
          "index_digest": "sha256:3b720a1d9d54bb9e7f89aa56638d9bb2a1fc0a5530d253b950e6884fd057498f",
          "worktree_digest": "sha256:3b720a1d9d54bb9e7f89aa56638d9bb2a1fc0a5530d253b950e6884fd057498f",
          "untracked_digest": "absent"
        },
        {
          "path": "app/src/main/res/values-es/strings.xml",
          "object_kind": {
            "head": "regular",
            "index": "regular",
            "worktree": "regular",
            "untracked": "absent"
          },
          "state": "clean",
          "rename_from": null,
          "rename_to": null,
          "head_digest": "sha256:c386bc128ce4e9988cbfc63c14664e308763bf88a1d2f9b9fedbe01d67fe8841",
          "index_digest": "sha256:c386bc128ce4e9988cbfc63c14664e308763bf88a1d2f9b9fedbe01d67fe8841",
          "worktree_digest": "sha256:c386bc128ce4e9988cbfc63c14664e308763bf88a1d2f9b9fedbe01d67fe8841",
          "untracked_digest": "absent"
        },
        {
          "path": "app/src/main/res/values-fr/strings.xml",
          "object_kind": {
            "head": "regular",
            "index": "regular",
            "worktree": "regular",
            "untracked": "absent"
          },
          "state": "clean",
          "rename_from": null,
          "rename_to": null,
          "head_digest": "sha256:7f6195c426107663fb1e3a6b65d499177ba0f4f453bfffad903f107c99199508",
          "index_digest": "sha256:7f6195c426107663fb1e3a6b65d499177ba0f4f453bfffad903f107c99199508",
          "worktree_digest": "sha256:7f6195c426107663fb1e3a6b65d499177ba0f4f453bfffad903f107c99199508",
          "untracked_digest": "absent"
        },
        {
          "path": "app/src/main/res/values-hi/strings.xml",
          "object_kind": {
            "head": "regular",
            "index": "regular",
            "worktree": "regular",
            "untracked": "absent"
          },
          "state": "clean",
          "rename_from": null,
          "rename_to": null,
          "head_digest": "sha256:531dabf79c0f9962d03e00b6f99d561152a4e95f9c2d169b19f1ed32df89eb2b",
          "index_digest": "sha256:531dabf79c0f9962d03e00b6f99d561152a4e95f9c2d169b19f1ed32df89eb2b",
          "worktree_digest": "sha256:531dabf79c0f9962d03e00b6f99d561152a4e95f9c2d169b19f1ed32df89eb2b",
          "untracked_digest": "absent"
        },
        {
          "path": "app/src/main/res/values-in/strings.xml",
          "object_kind": {
            "head": "regular",
            "index": "regular",
            "worktree": "regular",
            "untracked": "absent"
          },
          "state": "clean",
          "rename_from": null,
          "rename_to": null,
          "head_digest": "sha256:e5330efca03f9797d87119af3bbd8f78ac5618022354e7b73f4ee54459aa1ee1",
          "index_digest": "sha256:e5330efca03f9797d87119af3bbd8f78ac5618022354e7b73f4ee54459aa1ee1",
          "worktree_digest": "sha256:e5330efca03f9797d87119af3bbd8f78ac5618022354e7b73f4ee54459aa1ee1",
          "untracked_digest": "absent"
        },
        {
          "path": "app/src/main/res/values-ja/strings.xml",
          "object_kind": {
            "head": "regular",
            "index": "regular",
            "worktree": "regular",
            "untracked": "absent"
          },
          "state": "clean",
          "rename_from": null,
          "rename_to": null,
          "head_digest": "sha256:87d00bd9b590afe6e2a0ae0d6ed238135c979348cb695764cce38a288a84634b",
          "index_digest": "sha256:87d00bd9b590afe6e2a0ae0d6ed238135c979348cb695764cce38a288a84634b",
          "worktree_digest": "sha256:87d00bd9b590afe6e2a0ae0d6ed238135c979348cb695764cce38a288a84634b",
          "untracked_digest": "absent"
        },
        {
          "path": "app/src/main/res/values-ko/strings.xml",
          "object_kind": {
            "head": "regular",
            "index": "regular",
            "worktree": "regular",
            "untracked": "absent"
          },
          "state": "clean",
          "rename_from": null,
          "rename_to": null,
          "head_digest": "sha256:b285f7142e108866c207900d991477eb466bd6c972e8166d65d7baa514b15969",
          "index_digest": "sha256:b285f7142e108866c207900d991477eb466bd6c972e8166d65d7baa514b15969",
          "worktree_digest": "sha256:b285f7142e108866c207900d991477eb466bd6c972e8166d65d7baa514b15969",
          "untracked_digest": "absent"
        },
        {
          "path": "app/src/main/res/values-pt-rBR/strings.xml",
          "object_kind": {
            "head": "regular",
            "index": "regular",
            "worktree": "regular",
            "untracked": "absent"
          },
          "state": "clean",
          "rename_from": null,
          "rename_to": null,
          "head_digest": "sha256:306ad3edea54254608c43d2bb731a09c1cdb769c85a282dd86cd9e9518095165",
          "index_digest": "sha256:306ad3edea54254608c43d2bb731a09c1cdb769c85a282dd86cd9e9518095165",
          "worktree_digest": "sha256:306ad3edea54254608c43d2bb731a09c1cdb769c85a282dd86cd9e9518095165",
          "untracked_digest": "absent"
        },
        {
          "path": "app/src/main/res/values-vi/strings.xml",
          "object_kind": {
            "head": "regular",
            "index": "regular",
            "worktree": "regular",
            "untracked": "absent"
          },
          "state": "clean",
          "rename_from": null,
          "rename_to": null,
          "head_digest": "sha256:063e258c35e11b69bd617a859d388aa602212e090b82128621edb58f1b2ad598",
          "index_digest": "sha256:063e258c35e11b69bd617a859d388aa602212e090b82128621edb58f1b2ad598",
          "worktree_digest": "sha256:063e258c35e11b69bd617a859d388aa602212e090b82128621edb58f1b2ad598",
          "untracked_digest": "absent"
        },
        {
          "path": "app/src/main/res/values-zh-rCN/strings.xml",
          "object_kind": {
            "head": "regular",
            "index": "regular",
            "worktree": "regular",
            "untracked": "absent"
          },
          "state": "clean",
          "rename_from": null,
          "rename_to": null,
          "head_digest": "sha256:699946ebfa923fa0ca51c734de749d551cac491bcc6103b74cfcbf42d40ed01a",
          "index_digest": "sha256:699946ebfa923fa0ca51c734de749d551cac491bcc6103b74cfcbf42d40ed01a",
          "worktree_digest": "sha256:699946ebfa923fa0ca51c734de749d551cac491bcc6103b74cfcbf42d40ed01a",
          "untracked_digest": "absent"
        },
        {
          "path": "app/src/main/res/values/strings.xml",
          "object_kind": {
            "head": "regular",
            "index": "regular",
            "worktree": "regular",
            "untracked": "absent"
          },
          "state": "clean",
          "rename_from": null,
          "rename_to": null,
          "head_digest": "sha256:55fbfca280632c2b24e5544e6e38b3af42f8f97798e75f372efcda2f6b5c61b1",
          "index_digest": "sha256:55fbfca280632c2b24e5544e6e38b3af42f8f97798e75f372efcda2f6b5c61b1",
          "worktree_digest": "sha256:55fbfca280632c2b24e5544e6e38b3af42f8f97798e75f372efcda2f6b5c61b1",
          "untracked_digest": "absent"
        },
        {
          "path": "app/src/test/java/com/duplicateapp/gamespace/features/premium/domain/premium_access_test.kt",
          "object_kind": {
            "head": "regular",
            "index": "regular",
            "worktree": "regular",
            "untracked": "absent"
          },
          "state": "clean",
          "rename_from": null,
          "rename_to": null,
          "head_digest": "sha256:ae159430388bfed3d0fd0d72a95102c1dfccfa19279d4c1c2ec781136dce14fa",
          "index_digest": "sha256:ae159430388bfed3d0fd0d72a95102c1dfccfa19279d4c1c2ec781136dce14fa",
          "worktree_digest": "sha256:ae159430388bfed3d0fd0d72a95102c1dfccfa19279d4c1c2ec781136dce14fa",
          "untracked_digest": "absent"
        },
        {
          "path": "app/src/test/java/com/duplicateapp/gamespace/features/update/domain/force_update_policy_test.kt",
          "object_kind": {
            "head": "regular",
            "index": "regular",
            "worktree": "regular",
            "untracked": "absent"
          },
          "state": "clean",
          "rename_from": null,
          "rename_to": null,
          "head_digest": "sha256:a861f83ad527b85f9c0c4755f8e334bf79c8303fdcb9069bc7d6bdfc544c308c",
          "index_digest": "sha256:a861f83ad527b85f9c0c4755f8e334bf79c8303fdcb9069bc7d6bdfc544c308c",
          "worktree_digest": "sha256:a861f83ad527b85f9c0c4755f8e334bf79c8303fdcb9069bc7d6bdfc544c308c",
          "untracked_digest": "absent"
        },
        {
          "path": "app/src/test/java/com/duplicateapp/gamespace/features/workspace/data/virtualized_game_launcher_test.kt",
          "object_kind": {
            "head": "regular",
            "index": "regular",
            "worktree": "regular",
            "untracked": "absent"
          },
          "state": "clean",
          "rename_from": null,
          "rename_to": null,
          "head_digest": "sha256:d2a50c77635fbd493e7073f31fe55ab76984b1261ec1034bdb86c32fc83ac40d",
          "index_digest": "sha256:d2a50c77635fbd493e7073f31fe55ab76984b1261ec1034bdb86c32fc83ac40d",
          "worktree_digest": "sha256:d2a50c77635fbd493e7073f31fe55ab76984b1261ec1034bdb86c32fc83ac40d",
          "untracked_digest": "absent"
        },
        {
          "path": "docs/google-play-launch-plan.md",
          "object_kind": {
            "head": "regular",
            "index": "regular",
            "worktree": "regular",
            "untracked": "absent"
          },
          "state": "clean",
          "rename_from": null,
          "rename_to": null,
          "head_digest": "sha256:83615a7fa0b4bf0bc7c59d1b51d6c474fa9a5284f88879ebf7b621c0b6d52b4d",
          "index_digest": "sha256:83615a7fa0b4bf0bc7c59d1b51d6c474fa9a5284f88879ebf7b621c0b6d52b4d",
          "worktree_digest": "sha256:83615a7fa0b4bf0bc7c59d1b51d6c474fa9a5284f88879ebf7b621c0b6d52b4d",
          "untracked_digest": "absent"
        },
        {
          "path": "docs/plans/2026-09-16-plan-engine-core-improvements.md",
          "object_kind": {
            "head": "regular",
            "index": "regular",
            "worktree": "regular",
            "untracked": "absent"
          },
          "state": "clean",
          "rename_from": null,
          "rename_to": null,
          "head_digest": "sha256:7af9e95923d9f8dc832e48d87ec2c8c9b24b6605e0460731fe5c9f7da46c769b",
          "index_digest": "sha256:7af9e95923d9f8dc832e48d87ec2c8c9b24b6605e0460731fe5c9f7da46c769b",
          "worktree_digest": "sha256:7af9e95923d9f8dc832e48d87ec2c8c9b24b6605e0460731fe5c9f7da46c769b",
          "untracked_digest": "absent"
        },
        {
          "path": "docs/plans/2026-09-16-plan-play-readiness-and-revenue.md",
          "object_kind": {
            "head": "regular",
            "index": "regular",
            "worktree": "regular",
            "untracked": "absent"
          },
          "state": "clean",
          "rename_from": null,
          "rename_to": null,
          "head_digest": "sha256:3f1e43ab630e91071b1fb1cb4551f7895c417587d697ac69e7aeca95fb6236ca",
          "index_digest": "sha256:3f1e43ab630e91071b1fb1cb4551f7895c417587d697ac69e7aeca95fb6236ca",
          "worktree_digest": "sha256:3f1e43ab630e91071b1fb1cb4551f7895c417587d697ac69e7aeca95fb6236ca",
          "untracked_digest": "absent"
        },
        {
          "path": "docs/privacy-policy.md",
          "object_kind": {
            "head": "regular",
            "index": "regular",
            "worktree": "regular",
            "untracked": "absent"
          },
          "state": "clean",
          "rename_from": null,
          "rename_to": null,
          "head_digest": "sha256:d69ac18090c0f5e01e9e714c1a86bdf2dcb54c0c9434d8a3aa6b31ee8c0b9792",
          "index_digest": "sha256:d69ac18090c0f5e01e9e714c1a86bdf2dcb54c0c9434d8a3aa6b31ee8c0b9792",
          "worktree_digest": "sha256:d69ac18090c0f5e01e9e714c1a86bdf2dcb54c0c9434d8a3aa6b31ee8c0b9792",
          "untracked_digest": "absent"
        },
        {
          "path": "docs/rules/architecture.md",
          "object_kind": {
            "head": "regular",
            "index": "regular",
            "worktree": "regular",
            "untracked": "absent"
          },
          "state": "clean",
          "rename_from": null,
          "rename_to": null,
          "head_digest": "sha256:9e358f121c522513fdb4043cca9492fbad542a907db9bfe25ffd3188df6bbfcf",
          "index_digest": "sha256:9e358f121c522513fdb4043cca9492fbad542a907db9bfe25ffd3188df6bbfcf",
          "worktree_digest": "sha256:9e358f121c522513fdb4043cca9492fbad542a907db9bfe25ffd3188df6bbfcf",
          "untracked_digest": "absent"
        },
        {
          "path": "docs/rules/naming.md",
          "object_kind": {
            "head": "regular",
            "index": "regular",
            "worktree": "regular",
            "untracked": "absent"
          },
          "state": "clean",
          "rename_from": null,
          "rename_to": null,
          "head_digest": "sha256:77eed5bbaff74f0bf23702f4b3fb5e87bd8362ff3deeb949441c25fbaabb9004",
          "index_digest": "sha256:77eed5bbaff74f0bf23702f4b3fb5e87bd8362ff3deeb949441c25fbaabb9004",
          "worktree_digest": "sha256:77eed5bbaff74f0bf23702f4b3fb5e87bd8362ff3deeb949441c25fbaabb9004",
          "untracked_digest": "absent"
        },
        {
          "path": "docs/store-readiness.md",
          "object_kind": {
            "head": "regular",
            "index": "regular",
            "worktree": "regular",
            "untracked": "absent"
          },
          "state": "clean",
          "rename_from": null,
          "rename_to": null,
          "head_digest": "sha256:0077ccfe3e5faa5df1c89cbcf194e86f00fd0430a2a72f6dae520d3e7487bc1d",
          "index_digest": "sha256:0077ccfe3e5faa5df1c89cbcf194e86f00fd0430a2a72f6dae520d3e7487bc1d",
          "worktree_digest": "sha256:0077ccfe3e5faa5df1c89cbcf194e86f00fd0430a2a72f6dae520d3e7487bc1d",
          "untracked_digest": "absent"
        },
        {
          "path": "gradlew",
          "object_kind": {
            "head": "regular",
            "index": "regular",
            "worktree": "regular",
            "untracked": "absent"
          },
          "state": "clean",
          "rename_from": null,
          "rename_to": null,
          "head_digest": "sha256:db7ebef82b9f6932b30587515ef17c657b24db0443e44a3397a3a2c896630dbc",
          "index_digest": "sha256:db7ebef82b9f6932b30587515ef17c657b24db0443e44a3397a3a2c896630dbc",
          "worktree_digest": "sha256:db7ebef82b9f6932b30587515ef17c657b24db0443e44a3397a3a2c896630dbc",
          "untracked_digest": "absent"
        },
        {
          "path": "packages/the_universe/core/consumer-rules.pro",
          "object_kind": {
            "head": "regular",
            "index": "regular",
            "worktree": "regular",
            "untracked": "absent"
          },
          "state": "clean",
          "rename_from": null,
          "rename_to": null,
          "head_digest": "sha256:02039b2c7f534c99e9f77e50ad5c497bab7fe904d2a3caeb7d6c3eb62af8c05b",
          "index_digest": "sha256:02039b2c7f534c99e9f77e50ad5c497bab7fe904d2a3caeb7d6c3eb62af8c05b",
          "worktree_digest": "sha256:02039b2c7f534c99e9f77e50ad5c497bab7fe904d2a3caeb7d6c3eb62af8c05b",
          "untracked_digest": "absent"
        },
        {
          "path": "packages/the_universe/core/src/main/AndroidManifest.xml",
          "object_kind": {
            "head": "regular",
            "index": "regular",
            "worktree": "regular",
            "untracked": "absent"
          },
          "state": "clean",
          "rename_from": null,
          "rename_to": null,
          "head_digest": "sha256:7a3c9e2c2e640ee5f7ea396005cb1d4d8e737cb2e220bf34c9b0f9015780a2d6",
          "index_digest": "sha256:7a3c9e2c2e640ee5f7ea396005cb1d4d8e737cb2e220bf34c9b0f9015780a2d6",
          "worktree_digest": "sha256:7a3c9e2c2e640ee5f7ea396005cb1d4d8e737cb2e220bf34c9b0f9015780a2d6",
          "untracked_digest": "absent"
        },
        {
          "path": "packages/the_universe/core/src/main/cpp/Android.mk",
          "object_kind": {
            "head": "regular",
            "index": "regular",
            "worktree": "regular",
            "untracked": "absent"
          },
          "state": "clean",
          "rename_from": null,
          "rename_to": null,
          "head_digest": "sha256:38c330def3d8871d07f02d48997ec210ead172eaeffb6b3e250658b66076179f",
          "index_digest": "sha256:38c330def3d8871d07f02d48997ec210ead172eaeffb6b3e250658b66076179f",
          "worktree_digest": "sha256:38c330def3d8871d07f02d48997ec210ead172eaeffb6b3e250658b66076179f",
          "untracked_digest": "absent"
        },
        {
          "path": "packages/the_universe/core/src/main/cpp/Hook/FileSystemHook.cpp",
          "object_kind": {
            "head": "regular",
            "index": "regular",
            "worktree": "regular",
            "untracked": "absent"
          },
          "state": "clean",
          "rename_from": null,
          "rename_to": null,
          "head_digest": "sha256:08c30203dcfe803e4f7c4c211ccb44792e64580c1fa1f0ec0f270537c595d700",
          "index_digest": "sha256:08c30203dcfe803e4f7c4c211ccb44792e64580c1fa1f0ec0f270537c595d700",
          "worktree_digest": "sha256:08c30203dcfe803e4f7c4c211ccb44792e64580c1fa1f0ec0f270537c595d700",
          "untracked_digest": "absent"
        },
        {
          "path": "packages/the_universe/core/src/main/cpp/IO.cpp",
          "object_kind": {
            "head": "regular",
            "index": "regular",
            "worktree": "regular",
            "untracked": "absent"
          },
          "state": "clean",
          "rename_from": null,
          "rename_to": null,
          "head_digest": "sha256:754a94f8dd574ae577ce0db6714e7711a56cf30a9b187dd88744460b4516bb4a",
          "index_digest": "sha256:754a94f8dd574ae577ce0db6714e7711a56cf30a9b187dd88744460b4516bb4a",
          "worktree_digest": "sha256:754a94f8dd574ae577ce0db6714e7711a56cf30a9b187dd88744460b4516bb4a",
          "untracked_digest": "absent"
        },
        {
          "path": "packages/the_universe/core/src/main/cpp/IO.h",
          "object_kind": {
            "head": "regular",
            "index": "regular",
            "worktree": "regular",
            "untracked": "absent"
          },
          "state": "clean",
          "rename_from": null,
          "rename_to": null,
          "head_digest": "sha256:1c717a4b2835c0ea68da1464828c57dfff5337ebc9077d63a4b579ea6467f6d3",
          "index_digest": "sha256:1c717a4b2835c0ea68da1464828c57dfff5337ebc9077d63a4b579ea6467f6d3",
          "worktree_digest": "sha256:1c717a4b2835c0ea68da1464828c57dfff5337ebc9077d63a4b579ea6467f6d3",
          "untracked_digest": "absent"
        },
        {
          "path": "packages/the_universe/core/src/main/cpp/UniverseNativeCore.cpp",
          "object_kind": {
            "head": "regular",
            "index": "regular",
            "worktree": "regular",
            "untracked": "absent"
          },
          "state": "clean",
          "rename_from": null,
          "rename_to": null,
          "head_digest": "sha256:2a9290fcc84194a5ec113ef0a04db8f7ec1848295fbd176f31b6de8d29e08e6c",
          "index_digest": "sha256:2a9290fcc84194a5ec113ef0a04db8f7ec1848295fbd176f31b6de8d29e08e6c",
          "worktree_digest": "sha256:2a9290fcc84194a5ec113ef0a04db8f7ec1848295fbd176f31b6de8d29e08e6c",
          "untracked_digest": "absent"
        },
        {
          "path": "packages/the_universe/core/src/main/cpp/Utils/AntiDetection.cpp",
          "object_kind": {
            "head": "regular",
            "index": "regular",
            "worktree": "regular",
            "untracked": "absent"
          },
          "state": "clean",
          "rename_from": null,
          "rename_to": null,
          "head_digest": "sha256:23822811a521f534a5bb7d00ce94a0f274c6229ccce6f0d353d361b28e6e16c1",
          "index_digest": "sha256:23822811a521f534a5bb7d00ce94a0f274c6229ccce6f0d353d361b28e6e16c1",
          "worktree_digest": "sha256:23822811a521f534a5bb7d00ce94a0f274c6229ccce6f0d353d361b28e6e16c1",
          "untracked_digest": "absent"
        },
        {
          "path": "packages/the_universe/core/src/main/cpp/Utils/VirtualSpoof.cpp",
          "object_kind": {
            "head": "regular",
            "index": "regular",
            "worktree": "regular",
            "untracked": "absent"
          },
          "state": "clean",
          "rename_from": null,
          "rename_to": null,
          "head_digest": "sha256:54eb0ac50f6a455b4b782273996e39535425c9a1e7616a22d3af05af0810d03e",
          "index_digest": "sha256:54eb0ac50f6a455b4b782273996e39535425c9a1e7616a22d3af05af0810d03e",
          "worktree_digest": "sha256:54eb0ac50f6a455b4b782273996e39535425c9a1e7616a22d3af05af0810d03e",
          "untracked_digest": "absent"
        },
        {
          "path": "packages/the_universe/core/src/main/java/com/dd/the/universe/TheUniverseCore.java",
          "object_kind": {
            "head": "regular",
            "index": "regular",
            "worktree": "regular",
            "untracked": "absent"
          },
          "state": "clean",
          "rename_from": null,
          "rename_to": null,
          "head_digest": "sha256:1d75117239be68f059f0de598c599485c53b7b2144b7d096f863a4bcc6bf6c12",
          "index_digest": "sha256:1d75117239be68f059f0de598c599485c53b7b2144b7d096f863a4bcc6bf6c12",
          "worktree_digest": "sha256:1d75117239be68f059f0de598c599485c53b7b2144b7d096f863a4bcc6bf6c12",
          "untracked_digest": "absent"
        },
        {
          "path": "packages/the_universe/core/src/main/java/com/dd/the/universe/core/IOCore.java",
          "object_kind": {
            "head": "regular",
            "index": "regular",
            "worktree": "regular",
            "untracked": "absent"
          },
          "state": "clean",
          "rename_from": null,
          "rename_to": null,
          "head_digest": "sha256:29165476aa9da1948859249979432c43ef2a507dbfbd6b4bd13c3f37d3948d9f",
          "index_digest": "sha256:29165476aa9da1948859249979432c43ef2a507dbfbd6b4bd13c3f37d3948d9f",
          "worktree_digest": "sha256:29165476aa9da1948859249979432c43ef2a507dbfbd6b4bd13c3f37d3948d9f",
          "untracked_digest": "absent"
        },
        {
          "path": "packages/the_universe/core/src/main/java/com/dd/the/universe/core/system/BProcessManagerService.java",
          "object_kind": {
            "head": "regular",
            "index": "regular",
            "worktree": "regular",
            "untracked": "absent"
          },
          "state": "clean",
          "rename_from": null,
          "rename_to": null,
          "head_digest": "sha256:8369ccf8260a2c9583d0407be4fdd55f881708f2ef92c38ac632627a16b670a6",
          "index_digest": "sha256:8369ccf8260a2c9583d0407be4fdd55f881708f2ef92c38ac632627a16b670a6",
          "worktree_digest": "sha256:8369ccf8260a2c9583d0407be4fdd55f881708f2ef92c38ac632627a16b670a6",
          "untracked_digest": "absent"
        },
        {
          "path": "packages/the_universe/core/src/main/java/com/dd/the/universe/core/system/pm/BPackageManagerService.java",
          "object_kind": {
            "head": "regular",
            "index": "regular",
            "worktree": "regular",
            "untracked": "absent"
          },
          "state": "clean",
          "rename_from": null,
          "rename_to": null,
          "head_digest": "sha256:9565e043c685a66a06149486da5fa78a5a26d7f5108a856dfea9303f8801dca5",
          "index_digest": "sha256:9565e043c685a66a06149486da5fa78a5a26d7f5108a856dfea9303f8801dca5",
          "worktree_digest": "sha256:9565e043c685a66a06149486da5fa78a5a26d7f5108a856dfea9303f8801dca5",
          "untracked_digest": "absent"
        },
        {
          "path": "packages/the_universe/core/src/main/java/com/dd/the/universe/core/system/pm/Settings.java",
          "object_kind": {
            "head": "regular",
            "index": "regular",
            "worktree": "regular",
            "untracked": "absent"
          },
          "state": "clean",
          "rename_from": null,
          "rename_to": null,
          "head_digest": "sha256:7f6d2271d97522ac98a27cdff4bb0e0fa96d6b71b1611880add168f7a958d572",
          "index_digest": "sha256:7f6d2271d97522ac98a27cdff4bb0e0fa96d6b71b1611880add168f7a958d572",
          "worktree_digest": "sha256:7f6d2271d97522ac98a27cdff4bb0e0fa96d6b71b1611880add168f7a958d572",
          "untracked_digest": "absent"
        },
        {
          "path": "packages/the_universe/core/src/main/java/com/dd/the/universe/core/system/pm/installer/CopyExecutor.java",
          "object_kind": {
            "head": "regular",
            "index": "regular",
            "worktree": "regular",
            "untracked": "absent"
          },
          "state": "clean",
          "rename_from": null,
          "rename_to": null,
          "head_digest": "sha256:b81e4908a0963cd476e57c54be95b0872e8f6b860d965288c4f155b2316e6392",
          "index_digest": "sha256:b81e4908a0963cd476e57c54be95b0872e8f6b860d965288c4f155b2316e6392",
          "worktree_digest": "sha256:b81e4908a0963cd476e57c54be95b0872e8f6b860d965288c4f155b2316e6392",
          "untracked_digest": "absent"
        },
        {
          "path": "packages/the_universe/core/src/main/java/com/dd/the/universe/core/system/pm/installer/CreatePackageExecutor.java",
          "object_kind": {
            "head": "regular",
            "index": "regular",
            "worktree": "regular",
            "untracked": "absent"
          },
          "state": "clean",
          "rename_from": null,
          "rename_to": null,
          "head_digest": "sha256:2b52badfe24a3b4371fbd2c4260c4d5f4d0110c2d04b449a22bc998d90abe7e4",
          "index_digest": "sha256:2b52badfe24a3b4371fbd2c4260c4d5f4d0110c2d04b449a22bc998d90abe7e4",
          "worktree_digest": "sha256:2b52badfe24a3b4371fbd2c4260c4d5f4d0110c2d04b449a22bc998d90abe7e4",
          "untracked_digest": "absent"
        },
        {
          "path": "packages/the_universe/core/src/main/java/com/dd/the/universe/core/system/pm/installer/RemoveUserExecutor.java",
          "object_kind": {
            "head": "regular",
            "index": "regular",
            "worktree": "regular",
            "untracked": "absent"
          },
          "state": "clean",
          "rename_from": null,
          "rename_to": null,
          "head_digest": "sha256:3085c15bb1f9408fabb8453d2d3fb8cac22af59679e395a180dcb7bcaf9f6c01",
          "index_digest": "sha256:3085c15bb1f9408fabb8453d2d3fb8cac22af59679e395a180dcb7bcaf9f6c01",
          "worktree_digest": "sha256:3085c15bb1f9408fabb8453d2d3fb8cac22af59679e395a180dcb7bcaf9f6c01",
          "untracked_digest": "absent"
        },
        {
          "path": "packages/the_universe/core/src/main/java/com/dd/the/universe/core/system/user/BUserHandle.java",
          "object_kind": {
            "head": "regular",
            "index": "regular",
            "worktree": "regular",
            "untracked": "absent"
          },
          "state": "clean",
          "rename_from": null,
          "rename_to": null,
          "head_digest": "sha256:0876b999b18f17a3d4399cc9cc1a9c97ffc25802853e052e78f433a94c424c32",
          "index_digest": "sha256:0876b999b18f17a3d4399cc9cc1a9c97ffc25802853e052e78f433a94c424c32",
          "worktree_digest": "sha256:0876b999b18f17a3d4399cc9cc1a9c97ffc25802853e052e78f433a94c424c32",
          "untracked_digest": "absent"
        },
        {
          "path": "packages/the_universe/core/src/main/java/com/dd/the/universe/entity/pm/InstallResult.java",
          "object_kind": {
            "head": "regular",
            "index": "regular",
            "worktree": "regular",
            "untracked": "absent"
          },
          "state": "clean",
          "rename_from": null,
          "rename_to": null,
          "head_digest": "sha256:7947105b46d5e9e6dba59aac2aa9b5d8a0d600913965437180f71852bb43339c",
          "index_digest": "sha256:7947105b46d5e9e6dba59aac2aa9b5d8a0d600913965437180f71852bb43339c",
          "worktree_digest": "sha256:7947105b46d5e9e6dba59aac2aa9b5d8a0d600913965437180f71852bb43339c",
          "untracked_digest": "absent"
        },
        {
          "path": "packages/the_universe/core/src/main/java/com/dd/the/universe/fake/device/VirtualDeviceIdentity.java",
          "object_kind": {
            "head": "regular",
            "index": "regular",
            "worktree": "regular",
            "untracked": "absent"
          },
          "state": "clean",
          "rename_from": null,
          "rename_to": null,
          "head_digest": "sha256:05126099525c4793dac3957c020b1a26eb1707ae277a1809143a3d23f295519a",
          "index_digest": "sha256:05126099525c4793dac3957c020b1a26eb1707ae277a1809143a3d23f295519a",
          "worktree_digest": "sha256:05126099525c4793dac3957c020b1a26eb1707ae277a1809143a3d23f295519a",
          "untracked_digest": "absent"
        },
        {
          "path": "packages/the_universe/core/src/main/java/com/dd/the/universe/fake/frameworks/BPackageManager.java",
          "object_kind": {
            "head": "regular",
            "index": "regular",
            "worktree": "regular",
            "untracked": "absent"
          },
          "state": "clean",
          "rename_from": null,
          "rename_to": null,
          "head_digest": "sha256:a696b4fa354a2d324c690d92edfa4ef99b887aea7bdc0d13f06364a6332ed5a0",
          "index_digest": "sha256:a696b4fa354a2d324c690d92edfa4ef99b887aea7bdc0d13f06364a6332ed5a0",
          "worktree_digest": "sha256:a696b4fa354a2d324c690d92edfa4ef99b887aea7bdc0d13f06364a6332ed5a0",
          "untracked_digest": "absent"
        },
        {
          "path": "packages/the_universe/core/src/main/java/com/dd/the/universe/fake/frameworks/TheUniverseManager.java",
          "object_kind": {
            "head": "regular",
            "index": "regular",
            "worktree": "regular",
            "untracked": "absent"
          },
          "state": "clean",
          "rename_from": null,
          "rename_to": null,
          "head_digest": "sha256:3027f5f46c6dfaea14bd2a9adb2ce217799e504b060aeeeaf87343de445a729c",
          "index_digest": "sha256:3027f5f46c6dfaea14bd2a9adb2ce217799e504b060aeeeaf87343de445a729c",
          "worktree_digest": "sha256:3027f5f46c6dfaea14bd2a9adb2ce217799e504b060aeeeaf87343de445a729c",
          "untracked_digest": "absent"
        },
        {
          "path": "packages/the_universe/core/src/main/java/com/dd/the/universe/proxy/ProxyActivity.java",
          "object_kind": {
            "head": "regular",
            "index": "regular",
            "worktree": "regular",
            "untracked": "absent"
          },
          "state": "clean",
          "rename_from": null,
          "rename_to": null,
          "head_digest": "sha256:0fbbb8f25bbbe3dcc56ebf1d9c14a104b9e931403307b1628572b82613dd2f63",
          "index_digest": "sha256:0fbbb8f25bbbe3dcc56ebf1d9c14a104b9e931403307b1628572b82613dd2f63",
          "worktree_digest": "sha256:0fbbb8f25bbbe3dcc56ebf1d9c14a104b9e931403307b1628572b82613dd2f63",
          "untracked_digest": "absent"
        },
        {
          "path": "packages/the_universe/core/src/main/java/com/dd/the/universe/utils/BzFileUtils.java",
          "object_kind": {
            "head": "regular",
            "index": "regular",
            "worktree": "regular",
            "untracked": "absent"
          },
          "state": "clean",
          "rename_from": null,
          "rename_to": null,
          "head_digest": "sha256:aae58a66c3798206ea0a56804241634155a8d71dbb83a2b997d4c343becca094",
          "index_digest": "sha256:aae58a66c3798206ea0a56804241634155a8d71dbb83a2b997d4c343becca094",
          "worktree_digest": "sha256:aae58a66c3798206ea0a56804241634155a8d71dbb83a2b997d4c343becca094",
          "untracked_digest": "absent"
        },
        {
          "path": "packages/the_universe/core/src/main/java/com/dd/the/universe/utils/NativeUtils.java",
          "object_kind": {
            "head": "regular",
            "index": "regular",
            "worktree": "regular",
            "untracked": "absent"
          },
          "state": "clean",
          "rename_from": null,
          "rename_to": null,
          "head_digest": "sha256:71d822af603f847fe740ee6009e145acf3a3d5fe5a9328deb6af009a15f31412",
          "index_digest": "sha256:71d822af603f847fe740ee6009e145acf3a3d5fe5a9328deb6af009a15f31412",
          "worktree_digest": "sha256:71d822af603f847fe740ee6009e145acf3a3d5fe5a9328deb6af009a15f31412",
          "untracked_digest": "absent"
        },
        {
          "path": "packages/the_universe/core/src/main/java/com/dd/the/universe/utils/SimpleCrashFix.java",
          "object_kind": {
            "head": "regular",
            "index": "regular",
            "worktree": "regular",
            "untracked": "absent"
          },
          "state": "clean",
          "rename_from": null,
          "rename_to": null,
          "head_digest": "sha256:6a907372ab1b5fd54614b7be1438c5a7b6b3e420b0b941608c77f1e54185a2c2",
          "index_digest": "sha256:6a907372ab1b5fd54614b7be1438c5a7b6b3e420b0b941608c77f1e54185a2c2",
          "worktree_digest": "sha256:6a907372ab1b5fd54614b7be1438c5a7b6b3e420b0b941608c77f1e54185a2c2",
          "untracked_digest": "absent"
        },
        {
          "path": "packages/the_universe/core/src/main/java/com/dd/the/universe/utils/TrieTree.java",
          "object_kind": {
            "head": "regular",
            "index": "regular",
            "worktree": "regular",
            "untracked": "absent"
          },
          "state": "clean",
          "rename_from": null,
          "rename_to": null,
          "head_digest": "sha256:36f85cdf695b726be4c46141ee909f998d2f4a3bb0540f7dc96f5c4abff59003",
          "index_digest": "sha256:36f85cdf695b726be4c46141ee909f998d2f4a3bb0540f7dc96f5c4abff59003",
          "worktree_digest": "sha256:36f85cdf695b726be4c46141ee909f998d2f4a3bb0540f7dc96f5c4abff59003",
          "untracked_digest": "absent"
        },
        {
          "path": "settings.gradle.kts",
          "object_kind": {
            "head": "regular",
            "index": "regular",
            "worktree": "regular",
            "untracked": "absent"
          },
          "state": "clean",
          "rename_from": null,
          "rename_to": null,
          "head_digest": "sha256:99d379a1d0d58ebf5f80603cf792318af948ca6da3845475a08bd45ec05f32be",
          "index_digest": "sha256:99d379a1d0d58ebf5f80603cf792318af948ca6da3845475a08bd45ec05f32be",
          "worktree_digest": "sha256:99d379a1d0d58ebf5f80603cf792318af948ca6da3845475a08bd45ec05f32be",
          "untracked_digest": "absent"
        },
        {
          "path": "tools/android_test_companion/src/main/java/com/duplicateapp/testcompanion/domain/test_scenario.kt",
          "object_kind": {
            "head": "regular",
            "index": "regular",
            "worktree": "regular",
            "untracked": "absent"
          },
          "state": "clean",
          "rename_from": null,
          "rename_to": null,
          "head_digest": "sha256:e1bfc99a5753468a0eb7792033032d478129a8e9ac3f7b32248b3defcf695fc9",
          "index_digest": "sha256:e1bfc99a5753468a0eb7792033032d478129a8e9ac3f7b32248b3defcf695fc9",
          "worktree_digest": "sha256:e1bfc99a5753468a0eb7792033032d478129a8e9ac3f7b32248b3defcf695fc9",
          "untracked_digest": "absent"
        },
        {
          "path": "tools/play_test_companion/play_test_companion/adb_runner.py",
          "object_kind": {
            "head": "regular",
            "index": "regular",
            "worktree": "regular",
            "untracked": "absent"
          },
          "state": "clean",
          "rename_from": null,
          "rename_to": null,
          "head_digest": "sha256:44c5a93e9d2c64d4f632fd03d8bc8ccf88f1b1e422c150a0ab7977ff91d2091b",
          "index_digest": "sha256:44c5a93e9d2c64d4f632fd03d8bc8ccf88f1b1e422c150a0ab7977ff91d2091b",
          "worktree_digest": "sha256:44c5a93e9d2c64d4f632fd03d8bc8ccf88f1b1e422c150a0ab7977ff91d2091b",
          "untracked_digest": "absent"
        },
        {
          "path": "tools/play_test_companion/play_test_companion/server.py",
          "object_kind": {
            "head": "regular",
            "index": "regular",
            "worktree": "regular",
            "untracked": "absent"
          },
          "state": "clean",
          "rename_from": null,
          "rename_to": null,
          "head_digest": "sha256:5013c41bcdd1935216d64c053ee4e7221a49f9103ce7961665969915657c9232",
          "index_digest": "sha256:5013c41bcdd1935216d64c053ee4e7221a49f9103ce7961665969915657c9232",
          "worktree_digest": "sha256:5013c41bcdd1935216d64c053ee4e7221a49f9103ce7961665969915657c9232",
          "untracked_digest": "absent"
        }
      ]
    },
    "primary_symbols": [
      {
        "symbol": "TheUniverseCore.launchApk",
        "file": "packages/the_universe/core/src/main/java/com/dd/the/universe/TheUniverseCore.java",
        "lines": "1094–1119",
        "role": "Kết quả khởi chạy"
      },
      {
        "symbol": "BPackageManagerService.installPackageAsUser",
        "file": "packages/the_universe/core/src/main/java/com/dd/the/universe/core/system/pm/BPackageManagerService.java",
        "lines": "685–829",
        "role": "Giao dịch cài package"
      },
      {
        "symbol": "BProcessManagerService",
        "file": "packages/the_universe/core/src/main/java/com/dd/the/universe/core/system/BProcessManagerService.java",
        "lines": "54–280",
        "role": "Process map, identity và đồng bộ"
      },
      {
        "symbol": "IOCore.redirectPath",
        "file": "packages/the_universe/core/src/main/java/com/dd/the/universe/core/IOCore.java",
        "lines": "61–102",
        "role": "Ánh xạ dữ liệu guest"
      },
      {
        "symbol": "WorkspaceViewModel",
        "file": "app/src/main/java/com/duplicateapp/gamespace/features/workspace/presentation/workspace_view_model.kt",
        "lines": "153–475",
        "role": "Điều phối UI/runtime/Premium"
      }
    ],
    "related_symbols": [
      {
        "symbol": "VirtualizedGameLauncher",
        "relationship": "CALLS runtime",
        "relevance": "Giữ nghĩa lỗi và readiness"
      },
      {
        "symbol": "TheUniverseVirtualGameRuntime",
        "relationship": "CALLS engine",
        "relevance": "Adapter public API"
      },
      {
        "symbol": "TrieTree.search",
        "relationship": "CALLED_BY IOCore",
        "relevance": "Longest segment prefix"
      },
      {
        "symbol": "Settings",
        "relationship": "persistence-of package manager",
        "relevance": "Migration và recovery"
      },
      {
        "symbol": "PersistentWorkspaceRepository",
        "relationship": "persistence-of workspace",
        "relevance": "JSON và selected session"
      },
      {
        "symbol": "RevenueCatPurchaseManager",
        "relationship": "callback-to WorkspaceViewModel",
        "relevance": "Purchase khác entitlement"
      },
      {
        "symbol": "ProxyActivity",
        "relationship": "Android entry point",
        "relevance": "IPC route validation"
      }
    ],
    "execution_path": [
      "UI chọn copy → launcher → runtime",
      "Runtime kiểm tra package theo virtual user → install nếu cần",
      "Engine process/service → guest Activity",
      "Kết quả xác nhận → UI state và persistence",
      "Delete → tombstone/cleanup → repository/shortcut reconciliation"
    ],
    "pdg_constraints": [
      {
        "description": "PDG cũ chỉ dùng tham khảo: null intent chặn launch; source hiện hành vẫn trả true sau startActivity void.",
        "affected_statements": [
          "packages/the_universe/core/src/main/java/com/dd/the/universe/TheUniverseCore.java:1094"
        ],
        "implementation_consequence": "Giữ guard và bổ sung xác nhận khởi chạy; không coi impact rỗng là không có caller."
      }
    ],
    "architectural_patterns": [
      {
        "pattern": "presentation -> domain <- data",
        "example_location": "docs/rules/architecture.md",
        "usage_guidance": "Liên feature qua domain; giữ Java API/AIDL/reflection tương thích."
      },
      {
        "pattern": "Serialized keys là hợp đồng",
        "example_location": "docs/rules/naming.md",
        "usage_guidance": "Migration có phiên bản và backup, không rename key cơ học."
      }
    ],
    "files_to_modify": [
      {
        "file": "packages/the_universe/core/src/main/java/com/dd/the/universe/TheUniverseCore.java",
        "symbols": [],
        "issue_ids": [
          "C01"
        ],
        "intended_change": [
          "C01: `launchApk` trả true sau lệnh startActivity kiểu void; không chứng minh guest đã chạy. Thêm trạng thái requested/started/failed, timeout có giới hạn và xác nhận đúng user/package; UI chỉ báo mở thành công khi có tín hiệu xác nhận."
        ]
      },
      {
        "file": "app/src/main/java/com/duplicateapp/gamespace/features/virtualization/data/the_universe_virtual_game_runtime.kt",
        "symbols": [],
        "issue_ids": [
          "C01"
        ],
        "intended_change": [
          "C01: `launchApk` trả true sau lệnh startActivity kiểu void; không chứng minh guest đã chạy. Thêm trạng thái requested/started/failed, timeout có giới hạn và xác nhận đúng user/package; UI chỉ báo mở thành công khi có tín hiệu xác nhận."
        ]
      },
      {
        "file": "packages/the_universe/core/src/main/java/com/dd/the/universe/entity/pm/InstallResult.java",
        "symbols": [],
        "issue_ids": [
          "C02"
        ],
        "intended_change": [
          "C02: Kết quả mặc định success=true; catch Throwable có thể trả success chưa đổi. Mặc định thất bại, chỉ set success sau commit; lỗi có mã và nguyên nhân, không nuốt cancellation/fatal error tùy tiện."
        ]
      },
      {
        "file": "packages/the_universe/core/src/main/java/com/dd/the/universe/core/system/pm/BPackageManagerService.java",
        "symbols": [],
        "issue_ids": [
          "C02",
          "C07",
          "C09"
        ],
        "intended_change": [
          "C02: Kết quả mặc định success=true; catch Throwable có thể trả success chưa đổi. Mặc định thất bại, chỉ set success sau commit; lỗi có mã và nguyên nhân, không nuốt cancellation/fatal error tùy tiện.",
          "C07: Tạo package xóa thư mục chung, trong khi cài user hiện tại chỉ kill user đó; thất bại có thể hỏng copy khác. Stage riêng và rollback, phối hợp toàn package, không xóa bản tốt trước commit.",
          "C09: Kết quả deleteDir không biểu thị thành công đầy đủ; executor trả 0 và PM tiếp tục cập nhật state. Dùng trạng thái xóa có cấu trúc, tombstone/retry; không tái sử dụng userId khi dữ liệu cũ chưa xóa. UI cho ẩn mục lỗi chỉ với cảnh báo còn dữ liệu, không giả thành đã xóa."
        ]
      },
      {
        "file": "packages/the_universe/core/src/main/java/com/dd/the/universe/fake/frameworks/BPackageManager.java",
        "symbols": [],
        "issue_ids": [
          "C03"
        ],
        "intended_change": [
          "C03: Fallback isInstalled kiểm tra app gốc qua host PM, bỏ userId; fallback launch có thể lấy intent host. Đổi thành unavailable/unknown, không thay bản clone bằng bản gốc; giữ retry có giới hạn."
        ]
      },
      {
        "file": "packages/the_universe/core/src/main/java/com/dd/the/universe/core/system/BProcessManagerService.java",
        "symbols": [],
        "issue_ids": [
          "C04",
          "C05"
        ],
        "intended_change": [
          "C04: Map ghi theo UID tổng hợp nhưng cleanup dùng appId từ record; virtual user >0 để lại record hoặc xóa nhầm nhóm. Thêm/chuẩn hóa khóa map riêng, kiểm tra tất cả đường remove/death/kill/reuse.",
          "C05: Có wait không timeout và IPC đồng bộ trong vùng khóa; map/list dùng khóa không nhất quán. Tách trạng thái khởi tạo khỏi global lock, giới hạn thời gian, một quy ước đồng bộ và cleanup trong finally."
        ]
      },
      {
        "file": "packages/the_universe/core/src/main/java/com/dd/the/universe/core/system/user/BUserHandle.java",
        "symbols": [],
        "issue_ids": [
          "C04"
        ],
        "intended_change": [
          "C04: Map ghi theo UID tổng hợp nhưng cleanup dùng appId từ record; virtual user >0 để lại record hoặc xóa nhầm nhóm. Thêm/chuẩn hóa khóa map riêng, kiểm tra tất cả đường remove/death/kill/reuse."
        ]
      },
      {
        "file": "packages/the_universe/core/src/main/java/com/dd/the/universe/fake/frameworks/TheUniverseManager.java",
        "symbols": [],
        "issue_ids": [
          "C06"
        ],
        "intended_change": [
          "C06: Cache service không đồng bộ; rate limit có thể trả cache chết; death callback cũ có thể xóa cache mới. Dùng generation/so sánh binder trong callback, monotonic clock, single-flight reconnect và kiểm tra sống có giới hạn."
        ]
      },
      {
        "file": "packages/the_universe/core/src/main/java/com/dd/the/universe/core/system/pm/installer/CreatePackageExecutor.java",
        "symbols": [],
        "issue_ids": [
          "C07"
        ],
        "intended_change": [
          "C07: Tạo package xóa thư mục chung, trong khi cài user hiện tại chỉ kill user đó; thất bại có thể hỏng copy khác. Stage riêng và rollback, phối hợp toàn package, không xóa bản tốt trước commit."
        ]
      },
      {
        "file": "packages/the_universe/core/src/main/java/com/dd/the/universe/core/system/pm/installer/CopyExecutor.java",
        "symbols": [],
        "issue_ids": [
          "C08"
        ],
        "intended_change": [
          "C08: Nhánh storage có thể bỏ qua split thiếu/lỗi native rồi vẫn thành công; chọn ABI/copy .so thiếu hậu kiểm. Host installFromDevice hiện dùng FLAG_SYSTEM, không khẳng định mọi lỗi storage đều chạm UI chính. Validate toàn bộ tập APK/ABI trước commit, đóng stream bằng scope, không dùng kích thước file làm bằng chứng nội dung giống nhau."
        ]
      },
      {
        "file": "packages/the_universe/core/src/main/java/com/dd/the/universe/utils/NativeUtils.java",
        "symbols": [],
        "issue_ids": [
          "C08"
        ],
        "intended_change": [
          "C08: Nhánh storage có thể bỏ qua split thiếu/lỗi native rồi vẫn thành công; chọn ABI/copy .so thiếu hậu kiểm. Host installFromDevice hiện dùng FLAG_SYSTEM, không khẳng định mọi lỗi storage đều chạm UI chính. Validate toàn bộ tập APK/ABI trước commit, đóng stream bằng scope, không dùng kích thước file làm bằng chứng nội dung giống nhau."
        ]
      },
      {
        "file": "packages/the_universe/core/src/main/java/com/dd/the/universe/core/system/pm/installer/RemoveUserExecutor.java",
        "symbols": [],
        "issue_ids": [
          "C09"
        ],
        "intended_change": [
          "C09: Kết quả deleteDir không biểu thị thành công đầy đủ; executor trả 0 và PM tiếp tục cập nhật state. Dùng trạng thái xóa có cấu trúc, tombstone/retry; không tái sử dụng userId khi dữ liệu cũ chưa xóa. UI cho ẩn mục lỗi chỉ với cảnh báo còn dữ liệu, không giả thành đã xóa."
        ]
      },
      {
        "file": "packages/the_universe/core/src/main/java/com/dd/the/universe/utils/BzFileUtils.java",
        "symbols": [],
        "issue_ids": [
          "C09"
        ],
        "intended_change": [
          "C09: Kết quả deleteDir không biểu thị thành công đầy đủ; executor trả 0 và PM tiếp tục cập nhật state. Dùng trạng thái xóa có cấu trúc, tombstone/retry; không tái sử dụng userId khi dữ liệu cũ chưa xóa. UI cho ẩn mục lỗi chỉ với cảnh báo còn dữ liệu, không giả thành đã xóa."
        ]
      },
      {
        "file": "packages/the_universe/core/src/main/java/com/dd/the/universe/core/system/pm/Settings.java",
        "symbols": [],
        "issue_ids": [
          "C10"
        ],
        "intended_change": [
          "C10: UID Parcel thiếu schema rõ, lỗi đọc có thể reset; lỗi package config đi tới xóa thư mục app. Quarantine và backup, migration có phiên bản, phân biệt corruption/IO tạm thời; không coi mọi Throwable là dữ liệu cần xóa."
        ]
      },
      {
        "file": "app/src/main/java/com/duplicateapp/gamespace/features/workspace/data/persistent_workspace_repository.kt",
        "symbols": [],
        "issue_ids": [
          "C11"
        ],
        "intended_change": [
          "C11: Một record JSON/enum lỗi làm cả danh sách thành rỗng; lần ghi sau có thể đè dữ liệu còn cứu được, legacy migration cũng có rủi ro. Backup, đọc từng record, validate ID/userId/duplicate và hiển thị recovery; công bố state sau khi lưu thành công."
        ]
      },
      {
        "file": "packages/the_universe/core/src/main/java/com/dd/the/universe/utils/TrieTree.java",
        "symbols": [],
        "issue_ids": [
          "C12"
        ],
        "intended_change": [
          "C12: Trie trả prefix đầu tiên, không kiểm tra segment; `/game2` khớp `/game`, `/game/lib` mất rule riêng. Dùng longest valid path prefix và thay đúng phần đầu, snapshot rule nhất quán."
        ]
      },
      {
        "file": "packages/the_universe/core/src/main/java/com/dd/the/universe/core/IOCore.java",
        "symbols": [],
        "issue_ids": [
          "C12",
          "C13"
        ],
        "intended_change": [
          "C12: Trie trả prefix đầu tiên, không kiểm tra segment; `/game2` khớp `/game`, `/game/lib` mất rule riêng. Dùng longest valid path prefix và thay đúng phần đầu, snapshot rule nhất quán.",
          "C13: Blacklist Pictures trả chính prefix thay vì path, làm mất tên file. Chốt nghĩa blacklist là không redirect, trả nguyên path; không dùng blacklist như alias."
        ]
      },
      {
        "file": "packages/the_universe/core/src/main/java/com/dd/the/universe/utils/SimpleCrashFix.java",
        "symbols": [],
        "issue_ids": [
          "C14"
        ],
        "intended_change": [
          "C14: Global uncaught handler bỏ qua lỗi dựa vào chuỗi rộng như Context; return không phục hồi thread đã chết. Chỉ xử lý fallback tại call site, ghi lỗi đã lọc dữ liệu rồi delegate/terminate đúng; bỏ “swallow crash” toàn cục."
        ]
      },
      {
        "file": "packages/the_universe/core/src/main/java/com/dd/the/universe/fake/device/VirtualDeviceIdentity.java",
        "symbols": [],
        "issue_ids": [
          "C15"
        ],
        "intended_change": [
          "C15: Fallback ID md5 theo userId cố định có thể giống trên nhiều máy; multi-process prefs và lỗi trở về user0 làm mờ scope. Giữ ID cũ khi migrate, tạo ID ngẫu nhiên ổn định theo installation+virtual user trong owner duy nhất; lỗi trả trạng thái rõ."
        ]
      },
      {
        "file": "packages/the_universe/core/src/main/cpp/Utils/VirtualSpoof.cpp",
        "symbols": [],
        "issue_ids": [
          "C16",
          "C17"
        ],
        "intended_change": [
          "C16: Xóa Java spoof chưa xóa native property spoof: constructor vẫn gọi DobbyHook, giá trị mặc định cố định. Theo hướng sản phẩm không giả thiết bị, bỏ nhánh này khỏi artifact sau impact check; không biến nó thành tính năng né anti-cheat.",
          "C17: strcpy từ chuỗi cấu hình vào buffer caller không có kiểm tra độ dài. Ưu tiên giải quyết cùng C16; nếu buộc giữ tạm, giới hạn theo hợp đồng API và từ chối đầu vào dài, xử lý null hợp lệ. Chưa chứng minh khai thác từ app ngoài."
        ]
      },
      {
        "file": "packages/the_universe/core/src/main/cpp/Android.mk",
        "symbols": [],
        "issue_ids": [
          "C16"
        ],
        "intended_change": [
          "C16: Xóa Java spoof chưa xóa native property spoof: constructor vẫn gọi DobbyHook, giá trị mặc định cố định. Theo hướng sản phẩm không giả thiết bị, bỏ nhánh này khỏi artifact sau impact check; không biến nó thành tính năng né anti-cheat."
        ]
      },
      {
        "file": "packages/the_universe/core/src/main/cpp/UniverseNativeCore.cpp",
        "symbols": [],
        "issue_ids": [
          "C18"
        ],
        "intended_change": [
          "C18: GetStringUTFChars dùng cho rule không Release, raw pointer được giữ lâu dài. Sao chép vào vùng nhớ có owner, RAII cho JNI string; không chỉ thêm Release gây dangling pointer."
        ]
      },
      {
        "file": "packages/the_universe/core/src/main/cpp/IO.cpp",
        "symbols": [],
        "issue_ids": [
          "C18",
          "C19"
        ],
        "intended_change": [
          "C18: GetStringUTFChars dùng cho rule không Release, raw pointer được giữ lâu dài. Sao chép vào vùng nhớ có owner, RAII cho JNI string; không chỉ thêm Release gây dangling pointer.",
          "C19: Helper dùng strlen trên malloc chưa khởi tạo; char* redirect còn vấn đề ownership, callback open đọc vararg vô điều kiện. Chưa thấy caller hiện hành của char* redirect; FileSystemHook.init chưa gắn các callback này. Xóa nhánh chết sau kiểm tra symbol hoặc sửa/test trước khi kích hoạt."
        ]
      },
      {
        "file": "packages/the_universe/core/src/main/cpp/IO.h",
        "symbols": [],
        "issue_ids": [
          "C18"
        ],
        "intended_change": [
          "C18: GetStringUTFChars dùng cho rule không Release, raw pointer được giữ lâu dài. Sao chép vào vùng nhớ có owner, RAII cho JNI string; không chỉ thêm Release gây dangling pointer."
        ]
      },
      {
        "file": "packages/the_universe/core/src/main/cpp/Hook/FileSystemHook.cpp",
        "symbols": [],
        "issue_ids": [
          "C19"
        ],
        "intended_change": [
          "C19: Helper dùng strlen trên malloc chưa khởi tạo; char* redirect còn vấn đề ownership, callback open đọc vararg vô điều kiện. Chưa thấy caller hiện hành của char* redirect; FileSystemHook.init chưa gắn các callback này. Xóa nhánh chết sau kiểm tra symbol hoặc sửa/test trước khi kích hoạt."
        ]
      },
      {
        "file": "app/src/main/java/com/duplicateapp/gamespace/features/workspace/presentation/workspace_view_model.kt",
        "symbols": [],
        "issue_ids": [
          "C20",
          "C22",
          "C23",
          "C26",
          "C27"
        ],
        "intended_change": [
          "C20: Launch/delete có thể chồng nhau, không in-flight gate theo copy; kết quả cũ có thể ghi running sau khi xóa. Dùng operation state/generation, tuần tự hóa theo copy và package khi thay binary, reconcile từ runtime.",
          "C22: Refresh entitlement chỉ lúc ready; resume không refresh, callback lỗi có thể làm UI mất trạng thái trước. Thêm listener/refresh có dedupe, quy tắc cache/offline/unknown; reconcile sau purchase/restore/account change.",
          "C23: Purchase callback luôn hiện premium_activated dù access inactive; userCancelled dùng đường unavailable. Phân biệt active/pending/no entitlement/cancel/error; chỉ thông báo kích hoạt khi entitlement đúng.",
          "C26: FLAG_SECURE cập nhật ở lifecycle, không ngay khi đổi toggle; cửa sổ còn mở có thể giữ trạng thái cũ. Observe lock setting trực tiếp ở Activity, đồng bộ flag ngay; mô tả rõ khóa workspace, không khóa toàn bộ guest.",
          "C27: Auth/restore thiếu finally cho busy flag; logout không phối hợp kết quả Firebase/RevenueCat, callback cũ có thể khôi phục entitlement nhầm identity. Dùng state machine/generation, finally, giữ cancellation semantics; lỗi RC logout phải hiển thị/retry."
        ]
      },
      {
        "file": "app/src/main/java/com/duplicateapp/gamespace/features/workspace/data/virtualized_game_launcher.kt",
        "symbols": [],
        "issue_ids": [
          "C21"
        ],
        "intended_change": [
          "C21: Readiness luôn Ready; mọi launch failure bị gán permissionDenied, install failure thành gameNotInstalled. Truyền typed failure/retryability từ runtime, readiness phản ánh service/ABI/package thật; không mở dialog quyền cho lỗi binder."
        ]
      },
      {
        "file": "app/src/main/java/com/duplicateapp/gamespace/features/premium/data/revenue_cat_premium_repository.kt",
        "symbols": [],
        "issue_ids": [
          "C22",
          "C27"
        ],
        "intended_change": [
          "C22: Refresh entitlement chỉ lúc ready; resume không refresh, callback lỗi có thể làm UI mất trạng thái trước. Thêm listener/refresh có dedupe, quy tắc cache/offline/unknown; reconcile sau purchase/restore/account change.",
          "C27: Auth/restore thiếu finally cho busy flag; logout không phối hợp kết quả Firebase/RevenueCat, callback cũ có thể khôi phục entitlement nhầm identity. Dùng state machine/generation, finally, giữ cancellation semantics; lỗi RC logout phải hiển thị/retry."
        ]
      },
      {
        "file": "app/src/main/java/com/duplicateapp/gamespace/features/premium/data/revenue_cat_purchase_manager.kt",
        "symbols": [],
        "issue_ids": [
          "C23",
          "C24"
        ],
        "intended_change": [
          "C23: Purchase callback luôn hiện premium_activated dù access inactive; userCancelled dùng đường unavailable. Phân biệt active/pending/no entitlement/cancel/error; chỉ thông báo kích hoạt khi entitlement đúng.",
          "C24: Tự chọn availablePackages.firstOrNull, UI chưa thể hiện offer/giá/chu kỳ tương ứng. Dùng lựa chọn sản phẩm có chủ đích, giá bản địa hóa và điều khoản từ SDK; không phụ thuộc thứ tự remote offering."
        ]
      },
      {
        "file": "app/src/main/java/com/duplicateapp/gamespace/features/workspace/presentation/components/settings_dialog.kt",
        "symbols": [],
        "issue_ids": [
          "C24",
          "C36"
        ],
        "intended_change": [
          "C24: Tự chọn availablePackages.firstOrNull, UI chưa thể hiện offer/giá/chu kỳ tương ứng. Dùng lựa chọn sản phẩm có chủ đích, giá bản địa hóa và điều khoản từ SDK; không phụ thuộc thứ tự remote offering.",
          "C36: Có Firebase sign-in tạo tài khoản nhưng chỉ sign-out, chưa có đường xóa account. Thiết kế yêu cầu xóa trong app và web, reauth, xử lý dữ liệu liên quan theo retention; phân biệt xóa account, xóa clone và hủy subscription. Không tự xóa giao dịch phải lưu hay hứa xóa Firebase là đủ. [Yêu cầu Play](https://support.google.com/googleplay/android-developer/answer/13327111?hl=en)."
        ]
      },
      {
        "file": "app/src/main/java/com/duplicateapp/gamespace/features/privacy/data/biometric_privacy_lock_coordinator.kt",
        "symbols": [],
        "issue_ids": [
          "C25"
        ],
        "intended_change": [
          "C25: BIOMETRIC_STRONG kết hợp DEVICE_CREDENTIAL không được AndroidX hỗ trợ trên API29 trong khi app hỗ trợ API29. Thiết kế nhánh API29 dùng phương án hỗ trợ đúng với yêu cầu bảo mật, kiểm tra canAuthenticate; không âm thầm hạ mức bảo mật."
        ]
      },
      {
        "file": "app/build.gradle.kts",
        "symbols": [],
        "issue_ids": [
          "C25",
          "C32"
        ],
        "intended_change": [
          "C25: BIOMETRIC_STRONG kết hợp DEVICE_CREDENTIAL không được AndroidX hỗ trợ trên API29 trong khi app hỗ trợ API29. Thiết kế nhánh API29 dùng phương án hỗ trợ đúng với yêu cầu bảo mật, kiểm tra canAuthenticate; không âm thầm hạ mức bảo mật.",
          "C32: Release cho phép key trống và AdMob App ID test fallback; build được không chứng minh doanh thu cấu hình đúng. Validate cấu hình release theo feature được bật, giữ dev dùng test ID; không in giá trị bí mật."
        ]
      },
      {
        "file": "app/src/main/java/com/duplicateapp/gamespace/main_activity.kt",
        "symbols": [],
        "issue_ids": [
          "C26"
        ],
        "intended_change": [
          "C26: FLAG_SECURE cập nhật ở lifecycle, không ngay khi đổi toggle; cửa sổ còn mở có thể giữ trạng thái cũ. Observe lock setting trực tiếp ở Activity, đồng bộ flag ngay; mô tả rõ khóa workspace, không khóa toàn bộ guest."
        ]
      },
      {
        "file": "app/src/main/java/com/duplicateapp/gamespace/features/auth/data/firebase_auth_repository.kt",
        "symbols": [],
        "issue_ids": [
          "C27",
          "C36"
        ],
        "intended_change": [
          "C27: Auth/restore thiếu finally cho busy flag; logout không phối hợp kết quả Firebase/RevenueCat, callback cũ có thể khôi phục entitlement nhầm identity. Dùng state machine/generation, finally, giữ cancellation semantics; lỗi RC logout phải hiển thị/retry.",
          "C36: Có Firebase sign-in tạo tài khoản nhưng chỉ sign-out, chưa có đường xóa account. Thiết kế yêu cầu xóa trong app và web, reauth, xử lý dữ liệu liên quan theo retention; phân biệt xóa account, xóa clone và hủy subscription. Không tự xóa giao dịch phải lưu hay hứa xóa Firebase là đủ. [Yêu cầu Play](https://support.google.com/googleplay/android-developer/answer/13327111?hl=en)."
        ]
      },
      {
        "file": "app/src/main/java/com/duplicateapp/gamespace/features/workspace/data/android_workspace_shortcut_publisher.kt",
        "symbols": [],
        "issue_ids": [
          "C28"
        ],
        "intended_change": [
          "C28: Dựng ShortcutInfo ngoài try; tên rỗng/corrupt có thể thoát catch. Giới hạn cố định4 không theo launcher; chỉ thay dynamic list không vô hiệu shortcut pinned đã xóa. Validate đầu vào, bao phủ builder, dùng maxShortcutCount và disable/remove theo ID; tùy chọn nhãn riêng tư."
        ]
      },
      {
        "file": "app/src/main/java/com/duplicateapp/gamespace/features/update/data/force_update_coordinator.kt",
        "symbols": [],
        "issue_ids": [
          "C29"
        ],
        "intended_change": [
          "C29: Không xử lý boolean startUpdateFlowForResult; thất bại check chỉ log, có thể giữ required/pending cũ. State machine có retry và đường hỗ trợ, chốt policy khi offline; không vòng lặp ép cập nhật vô hạn."
        ]
      },
      {
        "file": "tools/play_test_companion/play_test_companion/server.py",
        "symbols": [],
        "issue_ids": [
          "C30"
        ],
        "intended_change": [
          "C30: Local POST thiếu kiểm tra Origin/Host/token, một số endpoint không cần body; Content-Length không giới hạn. Bind loopback là tốt nhưng không đủ tự thân. Thêm token phiên/CSRF, allowlist Host/Origin, giới hạn payload và lỗi có kiểm soát. Chưa làm PoC trên browser cụ thể."
        ]
      },
      {
        "file": "tools/play_test_companion/play_test_companion/adb_runner.py",
        "symbols": [],
        "issue_ids": [
          "C31"
        ],
        "intended_change": [
          "C31: Package/foreground kiểm tra substring, log chỉ tìm FATAL EXCEPTION trong cửa sổ không gắn run/PID; lệnh log lỗi có thể thành pass, test UI companion chưa kiểm tra guest. Parse exact package/focused activity, kiểm tra return code, mốc log/PID và native crash/ANR/freezer; kết quả unknown không thành passed. Monkey chỉ chạy dữ liệu thử nghiệm vì có thể kích hoạt thao tác thật."
        ]
      },
      {
        "file": "tools/android_test_companion/src/main/java/com/duplicateapp/testcompanion/domain/test_scenario.kt",
        "symbols": [],
        "issue_ids": [
          "C31"
        ],
        "intended_change": [
          "C31: Package/foreground kiểm tra substring, log chỉ tìm FATAL EXCEPTION trong cửa sổ không gắn run/PID; lệnh log lỗi có thể thành pass, test UI companion chưa kiểm tra guest. Parse exact package/focused activity, kiểm tra return code, mốc log/PID và native crash/ANR/freezer; kết quả unknown không thành passed. Monkey chỉ chạy dữ liệu thử nghiệm vì có thể kích hoạt thao tác thật."
        ]
      },
      {
        "file": "app/src/main/res/values/strings.xml",
        "symbols": [],
        "issue_ids": [
          "C33"
        ],
        "intended_change": [
          "C33: 9 locale có117/144 string, thiếu27 mỗi locale; mô tả 2 copy và giới hạn5 không nhất quán. Dùng tham số từ domain limit, dịch đầy đủ hoặc thu hẹp locale công bố."
        ]
      },
      {
        "file": "app/src/main/res/values-vi/strings.xml",
        "symbols": [],
        "issue_ids": [
          "C33"
        ],
        "intended_change": [
          "C33: 9 locale có117/144 string, thiếu27 mỗi locale; mô tả 2 copy và giới hạn5 không nhất quán. Dùng tham số từ domain limit, dịch đầy đủ hoặc thu hẹp locale công bố."
        ]
      },
      {
        "file": "app/src/main/java/com/duplicateapp/gamespace/core/logging/app_logger.kt",
        "symbols": [],
        "issue_ids": [
          "C34"
        ],
        "intended_change": [
          "C34: INFO log cả payload trong release, có package game user chọn. Giữ diagnostic tối thiểu với allowlist/redaction và correlation ID, tắt log hành vi chi tiết release; không chuyển nguyên payload sang analytics."
        ]
      },
      {
        "file": "packages/the_universe/core/src/main/AndroidManifest.xml",
        "symbols": [],
        "issue_ids": [
          "C35"
        ],
        "intended_change": [
          "C35: Nhiều proxy exported; Activity lấy nested target intent và gọi startActivity chưa có xác thực route ở lớp này. Audit từng activity/service/receiver, đóng entry không cần, broker/token hoặc validated explicit route cho entry cần; không dựa Binder.getCallingUid trong Activity để xác thực app gọi. Chưa chứng minh mọi component đều khai thác được."
        ]
      },
      {
        "file": "packages/the_universe/core/src/main/java/com/dd/the/universe/proxy/ProxyActivity.java",
        "symbols": [],
        "issue_ids": [
          "C35"
        ],
        "intended_change": [
          "C35: Nhiều proxy exported; Activity lấy nested target intent và gọi startActivity chưa có xác thực route ở lớp này. Audit từng activity/service/receiver, đóng entry không cần, broker/token hoặc validated explicit route cho entry cần; không dựa Binder.getCallingUid trong Activity để xác thực app gọi. Chưa chứng minh mọi component đều khai thác được."
        ]
      },
      {
        "file": "docs/privacy-policy.md",
        "symbols": [],
        "issue_ids": [
          "C37"
        ],
        "intended_change": [
          "C37: Policy còn placeholder và mô tả managed profile không đúng engine hiện tại; hướng dẫn release cũ dễ dẫn tới khai báo sai. Viết lại hành vi thực, dữ liệu từng SDK/clone, retention/xóa/liên hệ, publish HTTPS và đồng bộ Data safety từ artifact cuối."
        ]
      },
      {
        "file": "docs/store-readiness.md",
        "symbols": [],
        "issue_ids": [
          "C37"
        ],
        "intended_change": [
          "C37: Policy còn placeholder và mô tả managed profile không đúng engine hiện tại; hướng dẫn release cũ dễ dẫn tới khai báo sai. Viết lại hành vi thực, dữ liệu từng SDK/clone, retention/xóa/liên hệ, publish HTTPS và đồng bộ Data safety từ artifact cuối."
        ]
      },
      {
        "file": "docs/google-play-launch-plan.md",
        "symbols": [],
        "issue_ids": [
          "C37"
        ],
        "intended_change": [
          "C37: Policy còn placeholder và mô tả managed profile không đúng engine hiện tại; hướng dẫn release cũ dễ dẫn tới khai báo sai. Viết lại hành vi thực, dữ liệu từng SDK/clone, retention/xóa/liên hệ, publish HTTPS và đồng bộ Data safety từ artifact cuối."
        ]
      }
    ],
    "tests": [
      {
        "file": "app/src/test/java/com/duplicateapp/gamespace/features/workspace/data/virtualized_game_launcher_test.kt",
        "status": "existing",
        "scenarios": [
          "service unavailable → launch → không success giả",
          "lỗi install/permission/timeout → thông báo đúng loại"
        ]
      },
      {
        "file": "app/src/test/java/com/duplicateapp/gamespace/features/premium/domain/premium_access_test.kt",
        "status": "existing",
        "scenarios": [
          "active/unknown/offline → quyền theo policy đã chốt"
        ]
      },
      {
        "file": "app/src/test/java/com/duplicateapp/gamespace/features/update/domain/force_update_policy_test.kt",
        "status": "existing",
        "scenarios": [
          "giữ regression policy; coordinator có suite riêng cho start=false"
        ]
      },
      {
        "file": "app/src/androidTest/java/com/duplicateapp/gamespace/features/workspace/data/persistent_workspace_repository_test.kt",
        "status": "existing",
        "scenarios": [
          "JSON có một record hỏng → khôi phục phần hợp lệ, giữ bản gốc",
          "migration/restart → ID không đổi"
        ]
      },
      {
        "file": "app/src/androidTest/java/com/duplicateapp/gamespace/features/workspace/data/android_workspace_shortcut_publisher_test.kt",
        "status": "existing",
        "scenarios": [
          "tên rỗng/quota thấp → không crash",
          "xóa session có pinned shortcut → disable đúng ID"
        ]
      },
      {
        "file": "app/src/androidTest/java/com/duplicateapp/gamespace/features/virtualization/data/the_universe_virtual_game_runtime_test.kt",
        "status": "existing",
        "scenarios": [
          "launch guest user1/user2 → marker do guest viết tách biệt",
          "delete/recreate → dữ liệu cũ không xuất hiện"
        ]
      },
      {
        "file": "packages/the_universe/core/src/test/java/com/dd/the/universe/utils/TrieTreeTest.java",
        "status": "proposed_new",
        "scenarios": [
          "rule cha/con → prefix dài nhất",
          "game/game2 → không match sibling",
          "Pictures → giữ đủ đường dẫn"
        ]
      },
      {
        "file": "tools/play_test_companion/tests/test_server_security.py",
        "status": "proposed_new",
        "scenarios": [
          "Origin/Host/token không hợp lệ → không mutation"
        ]
      },
      {
        "file": "tools/play_test_companion/tests/test_adb_runner.py",
        "status": "proposed_new",
        "scenarios": [
          "package-prefix/foreground/logcat-failure/native-crash → không pass giả"
        ]
      }
    ],
    "verification_commands": [
      "JAVA_HOME='/Applications/Android Studio.app/Contents/jbr/Contents/Home' ./gradlew :app:testProdDebugUnitTest --tests '*VirtualizedGameLauncherTest' --tests '*PremiumAccessTest' --tests '*ForceUpdatePolicyTest' --offline"
    ],
    "verification_results": [
      "12 test scoped pass tại 2026-09-26",
      "Harness JVM tái hiện shortest-prefix và sibling mismatch",
      "9 locale thiếu27/144 string",
      "Chưa chạy fresh Android E2E, release bundle, billing sandbox"
    ],
    "risks": [
      "Giao dịch shared package ảnh hưởng mọi user",
      "UID map key khác client identity",
      "Exported hardening có thể phá Android contracts",
      "Thay ownership JNI có thể tạo dangling pointer",
      "Premium offline/identity policy chưa chốt"
    ],
    "assumptions": [
      "Owner xác nhận phạm vi5 copy/free-paid trước O02",
      "Chốt matrix game/OEM/API/ABI trước G01",
      "Không đổi applicationId theo namespace engine",
      "Owner xác nhận signing/config/license mà không chia sẻ secrets",
      "Privacy lock mặc định chỉ workspace",
      "Chốt retention/account deletion/offline Premium",
      "Xác minh analyzer runner trước refresh hoặc đổi API rộng"
    ],
    "open_questions": [
      "Game và máy nào phải hỗ trợ ở release đầu?",
      "Policy offline entitlement và tài khoản đổi identity?",
      "Privacy có cần che cả guest recents?",
      "Trạng thái giấy phép upstream và setup store?",
      "P2 nào owner chấp nhận trì hoãn?"
    ],
    "avoid": [
      "Không lặp lại toàn bộ discovery; reverify bằng provenance và từng thay đổi",
      "Không triển khai source/test/config trong lượt plan",
      "Không xóa dữ liệu để chữa lỗi hoặc fallback sang app gốc",
      "Không đổi public API/reflection/serialized keys thiếu migration",
      "Không kích hoạt root hiding/spoof/anti-cheat bypass",
      "Không đọc production secrets/logs/config",
      "Không push/upload/mua hàng thật khi chưa được yêu cầu",
      "Không diễn giải test host hoặc stale graph thành chứng nhận release"
    ],
    "index_refresh": {
      "status": "skipped",
      "reason": "stale analyzer provenance — source-weighted limitation",
      "index_commit": "4a555635e86d340489fd5766f005a92710979d1c",
      "commits_behind": 31,
      "runner": "node .gitnexus/run.cjs",
      "runner_identity": "Chưa xác minh hiện hành; runner có thể resolve phiên bản động",
      "load_bearing_graph": false
    },
    "issue_ids": [
      "C01",
      "C02",
      "C03",
      "C04",
      "C05",
      "C06",
      "C07",
      "C08",
      "C09",
      "C10",
      "C11",
      "C12",
      "C13",
      "C14",
      "C15",
      "C16",
      "C17",
      "C18",
      "C19",
      "C20",
      "C21",
      "C22",
      "C23",
      "C24",
      "C25",
      "C26",
      "C27",
      "C28",
      "C29",
      "C30",
      "C31",
      "C32",
      "C33",
      "C34",
      "C35",
      "C36",
      "C37"
    ],
    "verification_gates": [
      "G01",
      "G02",
      "G03",
      "G04",
      "G05",
      "G06",
      "G07",
      "G08"
    ],
    "optional_improvements": [
      "O01",
      "O02",
      "O03",
      "O04",
      "O05"
    ]
  }
}
```

## 12. Giả định, câu hỏi và giới hạn

1. [assumed] Giữ sản phẩm5 copy và engine hiện có; chưa có quyết định free2/paid5. Owner chọn mô hình trả phí trước O02; không đổi giới hạn trong bản sửa core.
2. [assumed] Chưa chốt game/OEM/API/ABI được hỗ trợ. Cần danh sách tối thiểu và tiêu chí “không hỗ trợ” rõ trước G01; không hứa chạy mọi game có anti-cheat/Play Integrity.
3. [assumed] Cần owner xác nhận namespace applicationId khi phát hành: engine `com.dd.the.universe` khác app `com.duplicateapp.gamespace`; không tự đổi applicationId vì yêu cầu rename engine cũ.
4. [assumed] Tình trạng signing, SDK key, console product/entitlement, publisher contact và license upstream chưa xác minh. Owner cung cấp trạng thái đạt/chưa đạt, không gửi secret vào hội thoại.
5. [assumed] Khóa privacy hiện là khóa workspace; nếu muốn khóa guest/recents là phạm vi thiết kế bổ sung và cần kiểm tra kiến trúc, không chỉ thêm FLAG_SECURE vào host.
6. [assumed] Chính sách xóa account/retention và offline Premium chưa quyết; cần phân biệt tài khoản app, subscription ở Play và dữ liệu game bên thứ ba.
7. Chỉ mục stale và runner có thể resolve phiên bản động; không build/install analyzer trong lượt plan. Trước sửa API rộng, xác minh runner hiện hành rồi re-index; không coi kết quả impact rỗng là green light.
8. Không có fresh device E2E, full lint/release bundle hoặc sandbox billing trong lượt này. Kết quả cũ là lịch sử; 12 test mới không thay thế chúng. Không làm khảo sát thị trường mới hay hứa tăng doanh thu trong kế hoạch audit core này.

## 13. Định nghĩa hoàn thành

- C01–C37 đều có trạng thái đã sửa/đã xác minh không áp dụng/được owner chấp nhận trì hoãn, kèm commit/test/log đã redaction; không đóng chỉ bằng lời khẳng định.
- Mọi P1 và G01–G06/G08 đã đóng bằng bằng chứng; không còn known blocker launch/data loss/IPC/account deletion/release config. P2 còn lại công khai, không gọi app “zero bug”.
- Test hồi quy tái hiện lỗi trước sửa và pass sau sửa; scoped static analysis không có finding mới; warning cũ có triage. Migration giữ dữ liệu/ID đã có, recovery không xóa bản gốc.
- Release artifact thực được kiểm tra merged manifest, signing,16KB, reflection/JNI và SDK configuration; sandbox billing/ads/consent pass; policy/deletion URL và Data safety khớp artifact.
- Review lại failure/concurrency/security paths sau thay đổi, không có phát hiện P1 mới trong vòng kiểm tra cuối đã ghi phạm vi. Nếu xuất hiện lỗi mới, tiếp tục chính backlog này thay vì tạo plan rời rạc.
- Chỉ sau các gate trên mới đề nghị closed testing/rollout có kiểm soát; không tự upload, mua hàng, thay console hoặc push Git.
