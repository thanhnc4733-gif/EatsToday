# EatsToday

Ứng dụng Android nấu ăn viết bằng Kotlin và Jetpack Compose, xây dựng từ bộ thiết kế EatsToday. Phiên bản này không có backend, toàn bộ dữ liệu nằm trên thiết bị.

## Chạy dự án

1. Mở thư mục này bằng Android Studio (Ladybug trở lên) và chờ Gradle Sync. Cần JDK 17 trở lên.
2. Chọn thiết bị API 24 trở lên rồi bấm Run.
3. Bấm **Đăng ký tài khoản**, đăng nhập bằng `demo@eatstoday.vn` / `Eats1234`, hoặc bấm **Bắt đầu** để dùng ngay. Mã đặt lại mật khẩu thử nghiệm là `1234`.

Chạy kiểm thử đơn vị: `./gradlew testDebugUnitTest`. Tạo APK thử: `./gradlew assembleDebug`.

## Công nghệ

Compose BOM 2024.12.01, Material 3, Navigation Compose 2.8.5, Lifecycle 2.8.7, Coroutines 1.9.0, Room 2.6.1 (KSP), DataStore 1.1.1, Coil 2.7.0. AGP 8.7.3, Kotlin 2.0.21, Gradle 8.9, minSdk 24, targetSdk 35.

## Kiến trúc

`Screen → ViewModel → Repository → Data Source (dữ liệu mẫu, DataStore, Room)`

| Thư mục | Nội dung |
| --- | --- |
| `navigation/` | Route, đồ thị điều hướng, thanh điều hướng dưới |
| `data/model` | Recipe, Ingredient, CookingStep, ShoppingItem, User, Friend, Post, Comment, AppNotification |
| `data/repository` | Recipe, Pantry, Social, User |
| `data/local` | Room, DataStore, dữ liệu mẫu |
| `feature/*` | Màn hình và ViewModel theo từng nhóm chức năng |
| `core/` | Thành phần giao diện dùng chung, tiện ích, quyền camera |
| `ui/theme` | Màu, kiểu chữ, kích thước, theme |

Room lưu yêu thích, lịch sử nấu, bộ sưu tập, tủ lạnh, đi chợ, bạn bè, bài đăng, bình luận, thông báo. DataStore lưu đăng nhập, hồ sơ, sở thích, cài đặt, trạng thái Premium.

## Thay đổi so với thiết kế

- **Thanh điều hướng:** các bản thiết kế có nhiều bộ tab khác nhau, nên ứng dụng dùng bộ 5 tab trong kế hoạch: Trang chủ, Khám phá, Tủ lạnh, Bạn bè, Tôi. Đi chợ và Đã lưu mở từ Trang chủ; Yêu thích mở từ Tôi.
- **Màn hình bổ sung ngoài 31 thiết kế:** danh sách nguyên liệu, thêm/sửa nguyên liệu, nấu xong, sửa hồ sơ, lịch sử nấu.
- **Camera:** nút chụp mở ứng dụng camera của hệ thống thay vì khung ngắm ngay trong ứng dụng, nên không có nút "Đổi cam".
- **Sở thích:** bỏ chữ "Bước 1/3" vì chỉ có một bước.
- **Trạng thái mất mạng:** hiện ở tab Bạn bè, vì đây là khu vực sẽ cần mạng khi có backend.

## Cần bổ sung

- **Font:** thiết kế dùng Be Vietnam Pro nhưng hiện đang dùng font hệ thống. Tải font từ Google Fonts, chép vào `res/font/` các file `be_vietnam_pro_regular.ttf`, `_semibold.ttf`, `_bold.ttf`, rồi đổi `AppFontFamily` trong `ui/theme/Type.kt` thành `FontFamily(Font(R.font.be_vietnam_pro_regular, FontWeight.Normal), Font(R.font.be_vietnam_pro_semibold, FontWeight.SemiBold), Font(R.font.be_vietnam_pro_bold, FontWeight.Bold))`.
- **Ảnh món ăn:** thiết kế chỉ có khung giữ chỗ nên ứng dụng cũng vậy, trừ ảnh người dùng chụp hoặc chọn.
- **Giá Premium:** đang để `[Giá/năm]` và `[Giá/tháng]` như thiết kế.
- **Công thức mẫu:** 10 món do lập trình viên soạn làm dữ liệu thử, nên kiểm tra lại định lượng trước khi phát hành.

## Giới hạn của bản không có backend

- Quyền vị trí, thông báo và micro mới chỉ được xin, chưa có chức năng nào dùng đến. Trước khi đưa lên Google Play cần làm các chức năng đó hoặc bỏ quyền trong `AndroidManifest.xml`.
- Đăng ký chỉ lưu một tài khoản trên máy; đăng ký email mới sẽ thay tài khoản cũ.
- Đăng nhập, đặt lại mật khẩu và Google/Apple là mô phỏng, không phải xác thực thật. Mã 4 số không được gửi qua email.
- Bài đăng, bình luận, kết bạn chỉ nằm trên máy, không đồng bộ với người khác. Nhắn tin, mã QR, tìm trong danh bạ chưa hoạt động.
- Premium chỉ đổi trạng thái cục bộ, không có thanh toán.
- Bộ đếm giờ vẫn đúng sau khi xoay màn hình hoặc quay lại ứng dụng, nhưng chỉ kêu khi ứng dụng đang mở; chưa có thông báo nền.
- Chuỗi giao diện đang viết trực tiếp trong code, cần chuyển sang `strings.xml` nếu làm đa ngôn ngữ.

## Danh sách kiểm thử thủ công

- [ ] Home → chi tiết món → bật yêu thích → đóng rồi mở lại ứng dụng: vẫn còn yêu thích
- [ ] Tìm "bun bo" (không dấu): ra Bún bò Huế; lọc theo thời gian, độ khó, chế độ ăn
- [ ] Nấu một món: đếm giờ, tạm dừng, xoay màn hình, thoát rồi quay lại khi đang đếm
- [ ] Hoàn thành → chụp ảnh → gửi: bài mới hiện ở Bạn bè; từ chối quyền camera vẫn thoát được
- [ ] Tủ lạnh: thêm, sửa, xóa; nhập tên rỗng hoặc số âm phải báo lỗi
- [ ] Chi tiết → thêm đi chợ → đánh dấu đã mua → cập nhật vào tủ lạnh
- [ ] Bấm liên tục các nút Hoàn thành, Gửi, Thêm đi chợ: không tạo bản ghi trùng
- [ ] Đổi cỡ chữ và giao diện tối trong Cài đặt; đăng xuất rồi đăng nhập lại
- [ ] Kiểm tra trên màn hình nhỏ và bật bàn phím ở các màn hình có ô nhập
