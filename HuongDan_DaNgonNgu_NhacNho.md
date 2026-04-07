# HƯỚNG DẪN TRIỂN KHAI TÍNH NĂNG ĐA NGÔN NGỮ VÀ NHẮC NHỞ HỌC TẬP

Dưới đây là tài liệu mô tả chi tiết về cách thức hoạt động và cấu trúc file của 2 tính năng chính: **Đa Ngôn Ngữ (Localization)** và **Nhắc Nhở Học Tập (Study Reminder)**. Bạn có thể sử dụng tài liệu này để tham khảo, làm báo cáo hoặc giải trình cách thức code hoạt động.

---

## 1. TÍNH NĂNG ĐA NGÔN NGỮ (MULTI-LANGUAGE SUPPORT)

### Mục Tiêu Chức Năng
Cho phép toàn bộ ứng dụng chuyển đổi tự động giữa Tiếng Việt và Tiếng Anh chỉ với một nút bấm trong mục "Cài đặt" mà không cần khởi động lại toàn bộ app (trực tiếp khởi chạy lại từ HomeActivity). Khi ngôn ngữ được thay đổi, Data, Bottom Navigation, các nhãn thông báo và lý thuyết cũng tự động đảo ngữ theo ngôn ngữ mới.

### Các Thành Phần Cốt Lõi (Architecture)

#### a) Quản lý đa ngôn ngữ (LanguageManager.java)
- **Vị trí:** `com.example.waviapp.utils.LanguageManager`
- **Nhiệm vụ:**
  - `setLanguage(Context, String)`: Ghi nhận sự lựa chọn của người dùng (`vi` hoặc `en`) vào bộ nhớ tạm bằng `SharedPreferences`.
  - `getLanguage(Context)`: Trích xuất ngôn ngữ mặc định mỗi khi ứng dụng khởi chạy (Mặc định được đặt là `vi`).
  - `setLocale(Context)`: Thiết lập và áp đặt Locale mới cho `Configuration` của thiết bị, qua đó "lừa" Android chọc vào đúng thư mục `values` hoặc `values-en`.
  - `applyToApp(Context)`: Khiến ngôn ngữ được gán trực tiếp lên Cấp ứng dụng (`ApplicationContext`) ngay từ lúc khởi động app trước cả khi gọi Activity đầu tiên.

#### b) Thiết lập tài nguyên ngữ nghĩa (strings.xml)
- **Tiếng Việt:** `res/values/strings.xml`
- **Tiếng Anh:** `res/values-en/strings.xml`
- Cấu trúc khóa độc lập: Mã nguồn Java và file Layout XML không bao giờ chứa chuỗi cố định (hard-coded string). Mọi văn bản, ví dụ chữ "phút", được tham chiếu thông qua `@string/auto_minute` hoặc `getString(R.string.auto_minute)`. Do đó, tuỳ theo Locale hiện tại, Android tự gọi ra dữ liệu tương ứng.

#### c) Lớp cơ sở trừu tượng (BaseActivity.java)
- **Vị trí:** `com.example.waviapp.BaseActivity`
- Thay vì để mọi Activity kế thừa trực tiếp từ `AppCompatActivity`, tất cả Activity của ứng dụng (như `HomeActivity`, `ExamActivity`, `TheoryActivity`, v.v.) hiện tại đều kế thừa `BaseActivity`.
- **Logic bên trong:** Ghi đè phương thức `attachBaseContext(Context)`. Phương thức này luôn được Android gọi khi một màn hình mới khởi tạo ra giao diện → Tại đây ta nhúng phương thức `LanguageManager.setLocale()` để đảm bảo Locale áp dụng vào Context mới.

#### d) Kích hoạt thay đổi trong SettingsActivity.java
Khi ấn chọn tiếng Anh ở hộp thoại `SettingsActivity`:
1. Gọi `LanguageManager.setLanguage()` lưu "en".
2. Gọi `Intent` chỉ định hướng tới `HomeActivity`, đính kèm cờ `Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK` để:
   - Xóa bỏ lịch sử chồng chéo của Activity hiện tại (back stack)
   - Khởi động lại luồng app từ đầu bằng tiếng Anh. Toàn bộ UI sẽ chớp 1 nhịp và trở lại tiếng Anh 100%.

---

## 2. TÍNH NĂNG NHẮC NHỞ HỌC TẬP (STUDY REMINDER)

### Mục Tiêu Chức Năng
Cho phép người dùng chọn một khung giờ thông qua BottomSheet, đặt báo thức, và hiển thị Push Notification hệ thống kèm rung chuông tại thời điểm đó hàng ngày để nhắc nhở học ngôn ngữ.

### Các Thành Phần Cốt Lõi (Architecture)

#### a) Giao diện Hẹn Giờ (StudyReminderBottomSheet.java)
- **Nhiệm vụ:** Hiển thị khung giờ thời gian thực (`TimePicker`) và công tắc Bật/Tắt hẹn giờ (`Switch`).
- **Luồng hoạt động nội bộ:**
  - Đọc `SharedPreferences` để đối chiếu xem hôm qua người dùng có đặt báo thức không (đọc ra biến bool `isReminderEnabled` và giờ phút cũ).
  - Trạng thái `Switch`: Nếu người dùng Gạt Công tắc Tắt -> Disable ô chọn giờ (`timePicker.setEnabled(false)`). Nếu Bật -> Kích hoạt khung chọn giờ.
  - Xử lý Cấp Quyền (Android 13+): Nếu HĐH từ version 13 (`TIRAMISU`), Android sẽ chặn mọi thông báo. Do đó, logic đã được thêm vào nút Switch để tự động gọi `requestPermissions(POST_NOTIFICATIONS)` nếu người dùng vừa cấp quyền bật thông báo mà chưa xin phép HĐH.
  - **Lưu Thiết lập (btnSave):** Khi ấn "lưu", nếu công tắc tắt → Gọi hàm `cancelAlarm()`. Nếu công tắc bật → Gọi hàm `setAlarm()`.

#### b) Thuật toán Đặt Báo Thức (AlarmManager)
- Khai thác Class `AlarmManager` gốc của Android để đẩy một `PendingIntent` trỏ tới bộ thu `AlarmReceiver`.
- Đồng hồ được cấu hình qua đối tượng `Calendar`. Nếu giờ người dùng chọn nhỏ hơn giờ hiện tại của hệ thống, thuật toán tự động cộng thêm 1 ngày (`calendar.add(Calendar.DATE, 1);`) để hẹn lịch qua vào ngày mai.

#### c) Bộ đánh thức khi Điện thoại Tắt Màn Hình (AlarmReceiver.java)
- Đây là một `BroadcastReceiver`, chạy dưới nền và hoàn toàn độc lập với hệ thống UI. Tuổi thọ của Broadcast này sẽ được Android "gọi dậy" đúng khung giờ đã đăng ký trong `AlarmManager`.
- Khi hàm `onReceive()` được kích hoạt, nó sử dụng `NotificationManager` kết hợp với `NotificationCompat.Builder` bắn ra một Push Notification xuất hiện lên khay thông báo trên cùng của hệ điều hành.
- **Notification Channel:** Đối với các thiết bị từ Android 8.0 trở lên, đòi hỏi phải tạo ra một Kênh Thông Báo (`NotificationChannel`) với tần số ưu tiên `IMPORTANCE_HIGH`. Kênh `STUDY_REMINDER_CHANNEL` được tạo tự động nếu nó chưa tồn tại.

#### d) AndroidManifest.xml Integration
- Cấp quyền truy cập trong Manifest là yếu tố quan trọng nhất. 
- Yêu cầu ứng dụng cho phép gửi Notifications: `<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />`
- Cho phép cấp quyền báo thức chính xác (Cần cho Android 12+): `<uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" />`
- Receiver bắt buộc phải khai báo đăng ký bên trong khối `<application>`: `<receiver android:name=".utils.AlarmReceiver" android:exported="false" />`

---
Tài liệu biên soạn dựa trên tiến trình thay đổi refactoring app và tích hợp Firebase / Native Features cho WAVI.
