# Plan: Cải thiện engine `com.dd.the.universe`

> Phạm vi: `packages/the_universe` — core 521 file / 59.341 dòng Java, 42 file native /
> 880 dòng, reflection 931 dòng, compiler 550 dòng.
> Quét tại branch `codex/gamer-farm-ux-fixes` sau hai lần rename namespace.
> Bổ sung cho [plan Play readiness](2026-09-16-plan-play-readiness-and-revenue.md);
> plan kia lo bề mặt manifest, plan này lo ruột engine.

## Trạng thái thực thi (cập nhật 2026-09-16)

**Đã làm xong** (build `assembleProdRelease` + `testProdDebugUnitTest` + `lintProdRelease` đều pass):

| việc | kết quả |
|---|---|
| Gỡ `LogSender` + `sendLogs` + `startLogcat` + `getDeviceInfoString` | 343 dòng, hết đường gửi log ra ngoài |
| Gỡ `ShellUtils` (có nhánh `su`) | xoá cả file |
| Gỡ giả lập Build phía Java, thay bằng `VirtualDeviceIdentity` | 220 dòng → 71, giữ nguyên Android ID mỗi clone |
| Gỡ `ProxyVpnService` khỏi manifest + `initVpnService` | hết `BIND_VPN_SERVICE` |
| Gỡ `LauncherActivity` + layout + config | thoát phụ thuộc ConstraintLayout |
| 50 provider proxy → `exported="false"` | chặn lỗ hổng §1b-5a |
| Engine hết phụ thuộc androidx (trừ annotation) | thay shim bằng API nền tảng, minSdk 29 |
| Thu gọn permission trong manifest engine | theo whitelist 47 mục cho sản phẩm game |

Kết quả trên manifest hợp nhất của `prodRelease`:

| | trước | sau |
|---|---|---|
| `uses-permission` | 329 | 92 |
| `android.permission.*` (unique) | 105 | 48 |
| SMS / Call Log / MANAGE_EXTERNAL_STORAGE / VPN / QUERY_ALL_PACKAGES | có | **hết** |
| provider proxy `exported="true"` | 50 | **0** |

Tổng: 576 file đổi, +2.209 / −4.232 dòng.

### Hai đính chính so với bản quét trước

1. **`AntiDetection.cpp` KHÔNG phải code chết.** Dòng 243 có
   `__attribute__((constructor)) void install_antidetection_hooks()` — chạy tự động khi
   nạp `.so`. Nó hook `access`/`stat`/`lstat`/`fopen`/`open`/`readlink`/`opendir` để che
   đường dẫn root, node emulator và thư mục của cloner đối thủ khỏi app khách. **Đang
   sống, chưa đụng tới.**
2. **Giả lập thuộc tính thiết bị cũng KHÔNG chết.** `VirtualSpoof.cpp:97` cũng có
   `__attribute__((constructor))`, gọi `install_property_get_hook()` hook
   `__system_property_get`. Vì đường Java cấu hình nó chưa bao giờ được gọi, mọi bản
   clone đọc được **giá trị hardcode**: `ro.product.model = Pixel 6`,
   `ro.build.fingerprint` của Pixel chạy Android 12, `ro.serialno = 1A2B3C4D5E6F`.

   Ngoài chuyện mâu thuẫn tài liệu, đây nhiều khả năng là **nguồn lỗi thật**: game chạy
   trên Samsung/Android 15 nhưng đọc được fingerprint Pixel/Android 12 sẽ đi nhầm nhánh
   dò năng lực đồ hoạ. Rất có thể đây là thứ mà sáu lớp vá crash ở mục 7 đang vá triệu
   chứng. Thêm nữa, serial hardcode giống hệt nhau trên mọi máy khiến các bản clone
   **không hề được cách ly** ở trường này — ngược hẳn mục đích.

   Chưa gỡ, vì đây là đổi hành vi runtime và cần kiểm trên thiết bị thật. Xem câu hỏi 1.

## Bản đồ hiện trạng

| package | file | dòng | vai trò |
|---|---|---|---|
| `fake` | 130 | 23.310 | proxy các system service cho app clone |
| `universeproxy` | 221 | — | tầng mirror phản chiếu API ẩn |
| `core` | 62 | 12.514 | AM/PM/account service nội bộ |
| `utils` | 51 | 8.133 | tiện ích + vá lỗi |
| `app` | 6 | 2.210 | `BActivityThread` và vòng đời process ảo |
| `android` | 12 | — | class đặt trong package `android` để chạm API package-private |

## Nhóm A — Gỡ trước, rủi ro cao nhất (§1)

1. **`LogSender` gửi logcat ra endpoint bên thứ ba.**
   `TheUniverseCore.sendLogs()` (dòng 2119–2160) chạy `logcat -d -v threadtime`, ghép
   với `getDeviceInfoString()` (model, brand, build ID, security patch) rồi POST lên
   `https://logs-sender-api.vercel.app/api/{chatId}/upload` — hạ tầng không thuộc
   publisher. Hiện **đang ngủ** vì `ParallelAppClientConfiguration.getLogSenderChatId()`
   trả `""`, nhưng code vẫn nằm trong APK phát hành và chỉ cách việc kích hoạt đúng một
   dòng config. Đây là thu thập dữ liệu không khai báo → sai Data safety.
   **Việc cần làm:** xoá `utils/LogSender.java`, `sendLogs()`, `getDeviceInfoString()`,
   `LogSendListener`, và `getLogSenderChatId()` khỏi `ClientConfiguration`. App đã có
   `AndroidDiagnosticReporter` — người dùng tự bấm chia sẻ, nội dung tự xem được trước.

2. **`ShellUtils` có nhánh thực thi `su`.**
   `ShellUtils.java:66` — `Runtime.exec(isRoot ? "su" : "sh")`. Ba call site duy nhất
   đều truyền `isRoot=false` và chỉ chạy `logcat` (`TheUniverseCore.java:1315,1334,1335`),
   tức nhánh root là code chết. Xoá cùng lúc với mục 1, vì cả ba call site đều phục vụ
   `sendLogs`.

3. **Cụm né phát hiện mâu thuẫn với ranh giới sản phẩm đã công bố.**
   `cpp/Utils/AntiDetection.cpp` giữ danh sách đường dẫn root/Magisk, device node của
   emulator, và thư mục dữ liệu của các cloner đối thủ. `utils/StackTraceFilter.java`
   lọc frame chứa `xposed`/`virtual`/`hook`/`the.universe` khỏi stack trace.
   `utils/UIDSpoofingHelper.java` cùng họ.
   `DECISIONS.md` viết "root tooling, and anti-cheat bypass are out of scope";
   `docs/google-play-launch-plan.md` viết "do not add ... device-identity spoofing,
   anti-cheat bypass, root hiding". **Code đang làm đúng thứ hai tài liệu nói không làm.**
   Đây là rủi ro Play thật hơn danh sách permission nhiều. Phải chọn một:
   gỡ cụm này, hoặc bỏ lời tuyên bố ranh giới trong tài liệu. Giữ cả hai là tự mâu thuẫn,
   và mâu thuẫn đó sẽ lộ ra đúng lúc bị review.

## Nhóm A2 — Lỗ hổng IPC (§1b)

Phát hiện ở vòng quét thứ hai. Đây là nhóm nặng nhất trong toàn bộ engine, vì nó là lỗ
hổng bảo mật thật chứ không chỉ rủi ro chính sách.

**Bối cảnh:** manifest engine khai báo **301 component `exported="true"`** — 150
activity, 100 service, 50 provider, 1 receiver. Toàn bộ chạy trong process riêng của
chính app (`android:process=":p0"` … `":p49"`), tức **cùng UID**. Process cùng UID không
cần `exported="true"` để gọi nhau. Nói cách khác: **301 component này mở ra ngoài mà
không cần mở.**

5a. **`ProxyContentProvider` cho phép app bất kỳ chiếm quyền engine.**
   `proxy/ProxyContentProvider.java:26` — `call()` với method `"_Black_|_init_process_"`
   nhận `AppConfig` Parcelable từ caller, gọi `initProcess(appConfig)`, rồi **trả
   `IBinder` của `currentActivityThread()` về cho caller**. Không có một dòng
   `getCallingUid()`, `getCallingPackage()` hay `checkPermission()` nào trong toàn bộ
   package `proxy/`. Bất kỳ app nào trên máy cũng gọi được, khởi tạo process ảo với cấu
   hình tuỳ ý, và cầm được handle điều khiển engine.

5b. **`ProxyActivity` là Intent redirection (CWE-926).**
   `proxy/ProxyActivity.java:33-38` — lấy Intent lồng `record.mTarget` từ Intent của
   caller rồi `startActivity(record.mTarget)` **bằng danh tính và quyền của app host**.
   Google Play App Security Improvement quét và cảnh báo đúng mẫu này. `ProxyService`,
   `ProxyBroadcastReceiver`, `ProxyJobService` cùng dạng.

5c. **`IntentSanitizer` không phải lớp phòng vệ.** Tên gợi ý sanitize bảo mật, nhưng hai
   hàm public duy nhất là `sanitizeClassExtrasForIpc` và `restoreSanitizedClassExtras` —
   chỉ xử lý ClassLoader khi marshal qua IPC. Nó không chặn được 5a hay 5b. Đổi tên để
   khỏi tạo cảm giác an toàn giả.

   **Việc cần làm, theo thứ tự:**
   - Đặt `exported="false"` cho toàn bộ 50 provider proxy. Đây là sửa một thuộc tính,
     rủi ro thấp nhất, chặn được 5a ngay.
   - Với activity/service proxy còn phải exported vì lý do kỹ thuật: thêm
     `android:permission` bằng một permission `signature`-level tự định nghĩa.
   - Thêm kiểm tra `Binder.getCallingUid() == Process.myUid()` ở đầu mọi entry point
     trong `proxy/`, cho tất cả các lớp kể cả đã đặt `exported="false"` — phòng thủ
     nhiều lớp, vì đây là bề mặt tấn công duy nhất lộ ra ngoài app.
   - Smoke test sau mỗi bước: một component proxy bị khoá nhầm sẽ làm hỏng vòng đời
     process ảo, và không có test nào bắt được (xem mục 5).

5d. **Giả lập danh tính thiết bị: phần lớn là code chết, phần sống thì hợp lý.**
   `fake/device/DeviceSpoofManager.applyToCurrentProcess()` — thứ duy nhất áp dụng
   fingerprint giả (mặc định Pixel 6, có sẵn preset "Samsung Galaxy S26+"), serial giả,
   và hook native `NativeCore.setDeviceSpoof()` — **không được gọi từ bất cứ đâu**.
   `DeviceBuildSpoofer`, `getSamsungPreset`, `DeviceSpoofManager.getProfile` đều là code
   chết, nhưng vẫn nằm trong APK phát hành.
   Ngược lại `getAndroidIdForCurrentUser()` **đang sống** và là chức năng chính đáng:
   mỗi bản clone cần một Android ID riêng, nếu không game sẽ nối được các tài khoản với
   nhau. Đây là cách ly, không phải giả mạo.
   **Việc cần làm:** xoá nhánh giả Build (`DeviceBuildSpoofer`, các trường Build trong
   `DeviceSpoofProfile`, `getSamsungPreset`, `applyToCurrentProcess`, và hook native
   `setDeviceSpoof`); giữ Android ID theo từng user. Làm vậy thì lời tuyên bố "no
   device-identity spoofing" trong tài liệu trở thành đúng, và mục 3 nhẹ đi một nửa.

## Nhóm B — Rủi ro chức năng lớn nhất: Android 15+ (§2)

4. **Engine dừng ở API 34.** Nhánh `SDK_INT` cao nhất trong toàn bộ core là
   `Build.VERSION_CODES.UPSIDE_DOWN_CAKE` (34, Android 14). Không có một dòng nào xử lý
   API 35 / 36 / 37. Trong khi `app/build.gradle.kts` đặt `targetSdk = 37`, tức app
   **tự nguyện nhận mọi behaviour change** của các bản Android mà engine chưa từng thấy.
   Với một engine ảo hoá sống bằng hidden API và hook, đây là nguồn lỗi số một.

   **Việc cần làm, theo thứ tự:**
   - Dựng ma trận thiết bị/emulator API 29, 33, 34, 35, 36, 37; chạy kịch bản
     thêm copy → mở → nhận notification → đăng nhập → khởi động lại → gỡ.
   - Ghi lại API nào hỏng ở đâu **trước khi** sửa. Hiện chưa ai biết con số này.
   - Chỉ sau khi có bảng đó mới quyết định: vá theo version, hay hạ `targetSdk` cho
     tiến trình ảo trong khi giữ `targetSdk` cao cho app host.

5. **Engine có 0 test trên 59k dòng.** Không có lưới an toàn nào cho mục 4, cũng như cho
   mọi thay đổi sau này. Bắt đầu hẹp: `IntentResolver`, `ComponentResolver`, `BPackage`,
   `TrieTree`, `Md5Utils` là logic thuần, test được không cần thiết bị — đó là chỗ đặt
   viên gạch đầu tiên, không phải tầng hook.

## Nhóm B2 — Nguyên nhân gốc: engine được thiết kế để hỏng im lặng (§2b)

Phát hiện ở vòng quét thứ ba. Mục này **giải thích tại sao mục 4 không đo được** và tại
sao `Compatibility Center` của app chỉ báo lỗi chung chung. Bốn tầng che lỗi chồng lên
nhau:

| tầng | hành vi khi API biến mất | vị trí |
|---|---|---|
| 1. mirror | `catch (Throwable ignored) { return null; }` | `TheUniverseReflection.java:102,116,187` |
| 2. mirror | `catch (ClassNotFoundException) { printStackTrace(); } return null;` | `TheUniverseReflection.java:168` |
| 3. call site | 202 chỗ `BRXxx.get().method()` không null-check | toàn `core/` |
| 4. stack trace | lọc bỏ mọi frame chứa `the.universe`/`hook`/`virtual` | `utils/StackTraceFilter.java` |

Cộng thêm 129 `catch` rỗng và 191 `printStackTrace()`. Kết quả: khi Android 15/16/17 đổi
hoặc bỏ một hidden API trong số **221 mirror class**, engine trả `null`, một trong 202
call site ném NPE ở chỗ không liên quan, rồi stack trace bị lọc sạch frame engine. Người
dùng thấy "game không mở được"; anh không thấy gì cả.

**Đây là việc phải làm trước mục 4, không phải song song.** Đo ma trận Android bằng một
engine hỏng im lặng thì chỉ thu được "hỏng", không thu được "hỏng ở đâu".

6a. **Thêm sổ ghi lỗi mirror.** Trong `TheUniverseReflection`, mỗi lần resolve thất bại
   thì ghi lại `{class, method/field, SDK_INT}` vào một ring buffer thay vì nuốt. Không
   đổi hành vi runtime — vẫn trả `null` — chỉ thêm ghi nhận.
6b. **Nối sổ đó vào `Compatibility Center`** của app. Biến "game không mở được" thành
   "`android.app.ActivityThread#currentActivityThread` không tồn tại trên SDK 36".
   Màn hình này đã có sẵn, hiện chỉ thiếu dữ liệu thật để hiển thị.
6c. **Tắt `StackTraceFilter` ở debug build** — ít nhất là khi tự chẩn đoán.

## Nhóm B3 — Nguy cơ mất dữ liệu người dùng (§2c)

7a. **Trạng thái engine được lưu bằng `android.os.Parcel` ghi thẳng ra đĩa.**
   `core/system/pm/Settings.java:114-137` — `parcel.writeInt(mCurrUid)`,
   `parcel.writeMap(mAppIds)`, rồi `BzFileUtils.writeParcelToOutput()`. Khi đọc:
   `parcel.unmarshall(bytes)`. **Không có magic number, không có trường version.**

   Tài liệu Android nói rõ Parcel không phải cơ chế serialize đa dụng và không được lưu
   xuống đĩa, vì định dạng nhị phân của nó không cam kết ổn định giữa các phiên bản OS.

7b. **Đường phục hồi là xoá trắng.** `Settings.java:157-168` — bắt mọi `Exception` thì
   `getUidConf().delete()`, `mCurrUid = 0`, `mAppIds.clear()`. `mAppIds` là ánh xạ
   package → uid của các bản clone. Mất nó nghĩa là **người dùng mất toàn bộ tài khoản
   game đang đăng nhập**, đúng thứ duy nhất họ trả tiền để giữ.

   Kịch bản: user cập nhật Android → định dạng Parcel đổi → `unmarshall` ném exception →
   engine xoá sạch state → mở app thấy trống. Không cảnh báo, không sao lưu, không log.

   **Việc cần làm:** đổi sang định dạng tự mô tả có version (JSON hoặc Protobuf) kèm
   đường di trú từ Parcel cũ; giữ `AtomicFile` (phần này đang đúng); khi đọc hỏng thì
   đổi tên file thành `.corrupt` thay vì `delete()`, và báo cho người dùng thay vì im
   lặng. Đây là việc nên làm **trước** khi có nhiều người dùng thật, vì sau đó mỗi lỗi
   là một tài khoản game mất thật.

## Nhóm C — Chất lượng code (§3)

6. **Lỗi bị nuốt trên diện rộng: 1.223 khối `catch`, 191 `printStackTrace()`, 129 khối
   `catch` rỗng hoàn toàn.** Khi một bản clone hỏng, engine không phát ra tín hiệu nào —
   đây chính là lý do `Compatibility Center` của app luôn hiển thị chung chung.
   Trái với quy ước repo (`AGENTS.md` → engineering-conventions: log ở mọi catch).
   **Việc cần làm:** thay `printStackTrace()` bằng `Slog` (đã có sẵn), và cho `catch`
   rỗng ít nhất một dòng log kèm ngữ cảnh. Làm theo package, không làm một lượt.

7. **Sáu lớp vá crash chồng nhau**: `CrashMonitor` (666), `NativeCrashPrevention` (487),
   `DexCrashPrevention`, `DexFileRecovery`, `SimpleCrashFix`, `SocialMediaAppCrashPrevention`,
   cộng `QQUtils` và `HackAppUtils` vá riêng cho QQ/Facebook/Instagram/WhatsApp.
   Đây là chữa triệu chứng theo từng app, và với sản phẩm định vị **game** thì phần lớn
   là nợ không dùng tới. Gộp về một bề mặt báo lỗi duy nhất, nối vào `Compatibility
   Center`; bỏ các vá cho app mạng xã hội nếu không nằm trong phạm vi sản phẩm.

8. **`util/` và `utils/` tồn tại song song** (374 dòng vs 8.133). Gộp làm một.

8b. **Ba dependency chết trong `core/build.gradle`**, xác nhận bằng đếm import:
   `androidx.appcompat:appcompat:1.2.0` (0 lần dùng), `com.google.android.material:material:1.3.0`
   (0 lần), `com.moandjiezana.toml:toml4j:0.7.2` (0 lần). Hai cái đầu còn kéo theo cả bộ
   resource và mục manifest riêng — gỡ chúng làm nhẹ APK và bớt nhiễu cho việc thu gọn
   manifest ở plan kia. Dòng `implementation fileTree(dir: "libs", ...)` trỏ vào thư mục
   `core/libs` **không tồn tại**; xoá luôn.
   Dependency thật sự đang dùng chỉ còn `the-universe-reflection`, `the-universe-compiler`
   và `com.github.tiann:FreeReflection:3.2.2` (5 lần dùng, trong `TheUniverseCore:1665-1674`).

9. **File quá lớn**: `TheUniverseCore` 2.315, `BAccountManagerService` 1.807,
   `BActivityThread` 1.533, `IConnectivityManagerProxy` 1.524, `BPackageManagerService`
   1.200. Chỉ tách khi đã có test ở mục 5 — tách trước khi có test là đổi rủi ro lấy
   thẩm mỹ.

## Nhóm D — Sửa lại một điểm trong plan kia (§4)

10. Plan Play readiness ghi "thu hẹp keep rule `-keep class android.** {*;}`". Cần nói
    rõ hơn: rule đó đang bảo vệ 12 class thật trong package `android/`
    (`ActivityThread`, `ServiceManager`, `PackageParser`, `IContentProvider`…) dùng để
    chạm API package-private. Không xoá trắng — thu về danh sách class đích danh, và
    chỉ làm sau khi có smoke test trên thiết bị.

## Thứ tự thực thi (§5)

1. **Mục 5a trước hết** — đặt `exported="false"` cho 50 provider proxy. Sửa một thuộc
   tính, chặn lỗ hổng nghiêm trọng nhất, rủi ro hồi quy thấp nhất trong cả plan.
2. **Nhóm A và 5d** — thuần xoá code chết, không cần test, giảm ngay bề mặt rủi ro.
3. **Mục 4 (ma trận Android)** song song — đây là việc đo, chưa phải việc sửa, và kết
   quả của nó quyết định khối lượng thật của mọi thứ còn lại.
4. **Mục 5 (test logic thuần)** làm nền.
5. **Phần còn lại của 5a–5c** (permission signature, kiểm tra caller UID) — cần lưới an
   toàn ở bước 4 trước, vì khoá nhầm một component proxy sẽ làm hỏng vòng đời process ảo.
6. Mục 6 theo package, mục 7–9 sau cùng.

## Câu hỏi cần anh quyết

1. **Hai hook native đang sống** (`AntiDetection.cpp`, `VirtualSpoof.cpp`): gỡ, hay giữ
   và sửa lại tài liệu cho khớp? Riêng `VirtualSpoof` tôi nghiêng về gỡ kể cả bỏ qua
   yếu tố chính sách, vì fingerprint hardcode nhiều khả năng đang **gây lỗi** chứ không
   giúp gì.
2. **Phạm vi app**: engine đang gánh nhiều vá cho Facebook/Instagram/WhatsApp/QQ. Sản
   phẩm là **game space** — có cắt phần này không, hay vẫn muốn clone được app chat?
3. **Whitelist permission**: danh sách 47 mục tôi chọn theo hướng game (giữ CAMERA,
   RECORD_AUDIO, vị trí, media, tài khoản, billing; bỏ SMS, call log, danh bạ, lịch,
   cảm biến cơ thể, overlay). Anh nên soát lại — nếu một game cần quyền tôi đã bỏ thì
   bản clone của nó sẽ không xin được quyền đó.

## Kết quả kiểm trên thiết bị (2026-09-16, Android 16 / SDK 36)

Emulator `Pixel_9_Pro`, ảnh `android-36.1 google_apis_playstore arm64-v8a`.

### Các thay đổi ở trên đều an toàn

App cài được, khởi động được, thêm copy được, mở game được. Danh sách permission rút
gọn xác nhận đúng trên máy. 50 provider `exported="false"` không làm hỏng vòng đời
process ảo — nghi vấn lớn nhất của đợt refactor đã được loại bỏ.

### Nhưng tìm ra lỗi nghiêm trọng hơn tất cả những gì đã liệt kê

**Lần mở đầu tiên của một bản copy vừa thêm luôn thất bại, và app báo là thành công.**

Tái hiện 2/2 lần, với hai game khác nhau (`com.duplicateapp.samplegame`,
`com.panda.cocototo`). Chuỗi sự kiện:

```
W/TheUniverseManager: Using fallback isInstalled check ... due to service failures
I/ActivityManager:    Killing 9631:...:the_universe (adj 905): Sync transaction while frozen
W/BActivityManager:   ActivityManager service died, clearing cache and retrying 1/3 … 3/3
W/TheUniverseManager: Service died: activity_manager ×3 / package_manager ×2
I/ParallelApp:        success=virtual_game_launch      ← BÁO THÀNH CÔNG
I/ParallelApp:        success=record_session_opened
UI:                   "Running"
topResumedActivity:   MainActivity                     ← game không hề mở
```

**Nguyên nhân gốc — hai lỗi độc lập chồng lên nhau:**

1. **Cached Apps Freezer giết process server.** Process `:the_universe` nằm ở adj 905
   (cached) nên bị Android đóng băng. Host gọi binder **đồng bộ** vào đó, và Android
   giết tiến trình bị đóng băng khi nhận transaction đồng bộ
   (`Sync transaction while frozen`, `signal 9`). Cơ chế này siết dần từ Android 14 và
   là đúng loại thay đổi nền tảng mà engine — vốn dừng ở API 34 — không xử lý.
   Đường cài-rồi-mở của lần đầu đủ dài để process bị đóng băng giữa chừng; lần mở sau
   (đã cài rồi) thì đủ nhanh nên thoát.

2. **`launchApk` trả `true` mà không kiểm tra gì.** `TheUniverseCore.java:1094`:

   ```java
   startActivity(launchIntentForPackage, userId);   // trả void
   return true;                                     // luôn luôn
   ```

   Hàm này trả lời "tôi có tìm thấy launch intent không", chứ không phải "app có chạy
   không". Mọi lỗi phía dưới — service chết, binder hỏng — đều bị nuốt. Đây chính là
   §2b được chứng minh bằng thực nghiệm, không còn là suy luận.

**Thứ tự sửa:**

- Sửa (2) trước: cho `startActivity` trả trạng thái, `launchApk` trả đúng trạng thái đó,
  và xác nhận process ảo thực sự lên trước khi báo `Opened`. Việc này không sửa được
  lỗi, nhưng biến "im lặng sai" thành "báo lỗi đúng" — và là điều kiện cần để đo mọi
  thứ còn lại.
- Rồi sửa (1): giữ process `:the_universe` khỏi trạng thái cached — host bind vào một
  service trong process đó và giữ kết nối, hoặc dùng foreground service; chuyển các lời
  gọi chịu được bất đồng bộ sang `oneway`.
- Thêm test hồi quy cho đúng kịch bản này: thêm copy mới → mở lần đầu → phải mở được.

### Xác nhận thêm về rủi ro Android 15+

Trên chính API 36, logcat cho thấy tầng mirror đang hỏng im lặng đúng như mô tả:

```
NoSuchMethodException: dalvik.system.VMRuntime.setHiddenApiExemptions
Could not access ResourcesManager overlay field: No field mDisableOverlayLoading
Could not access WindowManager leak field: No field mIgnoreWindowLeaks
```

Mục đầu nghĩa là **cơ chế mở khoá hidden API (FreeReflection) không còn hoạt động trên
Android 16**. Engine vẫn chạy tiếp như không có gì.

## Chưa xác minh

Chưa chạy gì trên thiết bị thật. Build, unit test và lint đều pass, nhưng những thứ sau
**chỉ thiết bị mới trả lời được**:

- 50 provider chuyển sang `exported="false"` có làm hỏng vòng đời process ảo không
  (đã lập luận là không, vì `:pN` cùng UID, nhưng chưa chạy thật).
- Việc thu gọn permission có làm bản clone nào mất chức năng không.
- Gỡ `LauncherActivity` có ảnh hưởng luồng mở game không (config đã tắt từ trước).

Kịch bản smoke test tối thiểu: thêm copy → mở game → nhận notification → đăng nhập →
tắt mở lại app → gỡ copy.
