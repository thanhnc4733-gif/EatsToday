# EatsToday

Ứng dụng Android nấu ăn viết bằng Kotlin và Jetpack Compose, xây dựng từ bộ thiết kế EatsToday. Phiên bản này không có backend, toàn bộ dữ liệu nằm trên thiết bị.

## Chạy dự án

1. Mở thư mục này bằng Android Studio (Ladybug trở lên) và chờ Gradle Sync. Cần JDK 17 trở lên.
2. Chọn thiết bị API 24 trở lên rồi bấm Run.
3. Bấm **Đăng ký tài khoản** để tạo tài khoản Firebase, hoặc **Bắt đầu** để dùng ngay không cần tài khoản.

Chạy kiểm thử đơn vị: `./gradlew testDebugUnitTest`. Tạo APK thử: `./gradlew assembleDebug`.

## Đăng nhập (Firebase Authentication)

- Đăng ký, đăng nhập bằng email/mật khẩu và quên mật khẩu (gửi link qua email thật) chạy qua Firebase Authentication, project `eatstoday-bee61`.
- Đăng nhập Google dùng Credential Manager. Cần bật nhà cung cấp Google trong Firebase **trước** khi tải `app/google-services.json`, và thêm SHA-1 của máy build vào Firebase.
- Mỗi thành viên build trên máy riêng phải thêm SHA-1 debug của máy mình (`.\gradlew signingReport`) vào Firebase thì nút Google mới chạy.
- Code gọi Firebase nằm gọn trong `data/remote/FirebaseAuthSource.kt`; `core/utils/GoogleSignIn.kt` lo phần chọn tài khoản Google.

## Kết bạn (Cloud Firestore)

- Mỗi tài khoản có hồ sơ `users/{uid}` (tên, mã bạn bè 6 ký tự). Hồ sơ được tạo khi đăng nhập hoặc khi mở màn Bạn bè lần đầu.
- Tìm bạn bằng mã bạn bè, email, hoặc quét mã QR (`eatstoday://friend/MÃ`) bằng trình quét của Google Play Services, không cần xin quyền camera.
- Lời mời nằm ở `friendRequests/{người gửi}_{người nhận}`, cập nhật theo thời gian thực. Chấp nhận thì lời mời trở thành quan hệ bạn bè; xóa là hủy lời mời hoặc hủy kết bạn.
- Tra cứu theo mã và email dùng `friendCodes/{MÃ}` và `emails/{email}`, chỉ đọc được từng bản ghi, không ai liệt kê được danh sách người dùng. Hồ sơ công khai không chứa email.
- **Luật bảo mật** ở file `firestore.rules`. Mỗi khi sửa, dán lại toàn bộ file vào Firebase Console > Firestore Database > Rules > Publish.
- Code: `data/remote/FriendRemoteSource.kt`, `data/repository/FriendRepository.kt`, `feature/social/presentation/viewmodel/FriendsViewModel.kt`, `core/utils/FriendQr.kt`.
- Người dùng chưa đăng nhập ("Bắt đầu") không kết bạn được; app mời họ đăng nhập.
- Chưa có: thông báo khi có lời mời kết bạn mới.

## Bảng tin và thông báo (Cloud Firestore)

- Bài đăng nằm ở `posts/{id}`. Lúc đăng, app ghi danh sách `visibleTo` gồm tác giả và bạn bè hiện có; bảng tin của mỗi người là các bài có uid của mình trong `visibleTo`, cập nhật theo thời gian thực.
- Thích (`likedBy`), chấm sao (`ratings` theo uid, chấm lại thì thay điểm cũ) và số bình luận lưu ngay trong bài. Bình luận ở `posts/{id}/comments`.
- Thông báo ở `users/{uid}/notifications`: khi bạn bè đăng món (báo cho mọi bạn bè), bình luận hoặc chấm sao món của mình (báo cho tác giả). Thông báo được ghi trong cùng một lô với thao tác, nên không có thông báo "lạc".
- Khi app đang chạy, `core/utils/FeedNotifier.kt` hiện thông báo trên thanh trạng thái (kênh "Bạn bè"), tôn trọng công tắc Thông báo trong Cài đặt và quyền thông báo. Khi app đã tắt hẳn thì cần Firebase Cloud Messaging và Cloud Functions (gói Blaze), chưa làm.
- Ảnh chụp món được thu nhỏ (cạnh dài 1080 px), nén JPEG dưới 700 KB rồi lưu ở `postPhotos/{id}` trên Firestore (không cần Firebase Storage, vốn đòi gói Blaze). Máy bạn bè tải ảnh một lần rồi lưu lại trong bộ nhớ app.
- Mỗi khi danh sách bạn bè thay đổi, app cập nhật `visibleTo` của các bài mình đã đăng: bạn mới kết bạn thấy cả bài cũ, người đã hủy kết bạn không còn thấy.
- Code: `data/remote/PostRemoteSource.kt`, `data/repository/FeedRepository.kt`, `data/model/FeedModels.kt`, `feature/social/presentation/viewmodel/SocialViewModels.kt`, `feature/social/presentation/screen/FeedScreens.kt`.

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

Room lưu yêu thích, lịch sử nấu, bộ sưu tập, tủ lạnh, đi chợ. Bạn bè, bài đăng, bình luận và thông báo nằm trên Cloud Firestore. DataStore lưu đăng nhập, hồ sơ, sở thích, cài đặt, trạng thái Premium.

## Thay đổi so với thiết kế

- **Thanh điều hướng:** các bản thiết kế có nhiều bộ tab khác nhau, nên ứng dụng dùng bộ 5 tab trong kế hoạch: Trang chủ, Khám phá, Tủ lạnh, Bạn bè, Tôi. Đi chợ và Đã lưu mở từ Trang chủ; Yêu thích mở từ Tôi.
- **Màn hình bổ sung ngoài 31 thiết kế:** danh sách nguyên liệu, thêm/sửa nguyên liệu, nấu xong, sửa hồ sơ, lịch sử nấu.
- **Camera:** nút chụp mở ứng dụng camera của hệ thống thay vì khung ngắm ngay trong ứng dụng, nên không có nút "Đổi cam".
- **Sở thích:** bỏ chữ "Bước 1/3" vì chỉ có một bước.
- **Trạng thái mất mạng:** hiện ở tab Bạn bè, vì đây là khu vực sẽ cần mạng khi có backend.

## Cần bổ sung

- **Font:** thiết kế dùng Be Vietnam Pro nhưng hiện đang dùng font hệ thống. Tải font từ Google Fonts, chép vào `res/font/` các file `be_vietnam_pro_regular.ttf`, `_semibold.ttf`, `_bold.ttf`, rồi đổi `AppFontFamily` trong `ui/theme/Type.kt` thành `FontFamily(Font(R.font.be_vietnam_pro_regular, FontWeight.Normal), Font(R.font.be_vietnam_pro_semibold, FontWeight.SemiBold), Font(R.font.be_vietnam_pro_bold, FontWeight.Bold))`.
- **Ảnh món ăn:** tất cả là ảnh chụp thật trong `res/drawable-nodpi`. 12 ảnh lấy từ Wikimedia Commons (xem mục Nguồn ảnh); đã được cắt về khung 640×400. Muốn thay ảnh, chép đè đúng tên file.
- **Giá Premium:** đang để `[Giá/năm]` và `[Giá/tháng]` như thiết kế.
- **Công thức mẫu:** 16 món do lập trình viên soạn làm dữ liệu thử, nên kiểm tra lại định lượng trước khi phát hành.

## Giới hạn của bản không có backend

- Quyền vị trí, thông báo và micro mới chỉ được xin, chưa có chức năng nào dùng đến. Trước khi đưa lên Google Play cần làm các chức năng đó hoặc bỏ quyền trong `AndroidManifest.xml`.
- Đăng nhập Apple chưa hỗ trợ (cần tài khoản Apple Developer). Hồ sơ, cài đặt, bài đăng và dữ liệu nấu ăn vẫn chỉ lưu trên máy; riêng bạn bè đã đồng bộ qua Firestore.
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

## Nguồn ảnh

Các ảnh dưới đây lấy từ Wikimedia Commons, đã được cắt khung. Ảnh CC BY-SA khi chỉnh sửa vẫn giữ nguyên giấy phép CC BY-SA.

| File | Ảnh gốc | Tác giả | Giấy phép |
| --- | --- | --- | --- |
| canh_chua_ca.jpg | [Canhchua2.jpg](https://commons.wikimedia.org/wiki/File:Canhchua2.jpg) | Jason Hutchens | CC BY 2.0 |
| canh_trung_ca.jpg | [Tomato and egg soup.jpg](https://commons.wikimedia.org/wiki/File:Tomato_and_egg_soup.jpg) | NNU-10-HanRongrong | CC BY 3.0 |
| dau_hu_sot_ca_chua.jpg | [Tomato-sauced tofu.jpg](https://commons.wikimedia.org/wiki/File:Tomato-sauced_tofu.jpg) | Mickey Đại Phát | CC BY-SA 4.0 |
| thit_kho_trung.jpg | [Vietnamese thit kho, braised pork belly and boiled eggs](https://commons.wikimedia.org/wiki/File:Vietnamese_thit_kho,_braised_pork_belly_and_boiled_eggs_-_47693228262.jpg) | HungryHuy | CC BY 2.0 |
| goi_cuon.jpg | [Gỏi cuốn.jpg](https://commons.wikimedia.org/wiki/File:G%E1%BB%8Fi_cu%E1%BB%91n.jpg) | Tran Hai Duong | CC0 |
| mi_y_sot_ca_chua.jpg | [Spaghetti with Tomato Sauce](https://commons.wikimedia.org/wiki/File:Liat_Portal_for_Foodie_Disorder_-_Spaghetti_with_Tomato_Sauce.jpg) | HaJunkiyada | CC BY-SA 4.0 |
| pho_bo.jpg | [Pho Bo by Banh & Mee](https://commons.wikimedia.org/wiki/File:Pho_Bo_by_Banh_%26_Mee_in_Kirkgate_Market.jpg) | Andy Li | CC0 |
| com_chien_trung.jpg | [Egg Fried Rice.jpg](https://commons.wikimedia.org/wiki/File:Egg_Fried_Rice.jpg) | Gaurav Dhwaj Khadka | CC BY-SA 4.0 |
| banh_xeo.jpg | [Crispy Vietnamese Pancake (Banh Xeo)](https://commons.wikimedia.org/wiki/File:Crispy_Vietnamese_Pancake_(Banh_Xeo)_-_Xin_Chao,_Eastbourne_2026-08-14_(2).jpg) | Andy Li | CC0 |
| ca_kho_to.jpg | [Cá kho tộ.JPG](https://commons.wikimedia.org/wiki/File:C%C3%A1_kho_t%E1%BB%99.JPG) | Binh Giang | Public domain |
| rau_muong_xao_toi.jpg | [Rau muống xào tỏi.jpg](https://commons.wikimedia.org/wiki/File:Rau_mu%E1%BB%91ng_x%C3%A0o_t%E1%BB%8Fi.jpg) | Andy Wright | CC BY 2.0 |
| che_chuoi.jpg | [Banana, tapioca, coconut creme Vietnamese pudding](https://commons.wikimedia.org/wiki/File:Banana,_tapioca,_coconut_creme_Vietnamese_pudding_(che_chuoi).jpg) | HungryHuy | CC BY 2.0 |

4 ảnh còn lại (bún bò Huế, bò sốt vang, gà kho gừng, trứng chiên cà chua) do nhóm tự thêm; nhóm cần tự ghi nguồn nếu không phải ảnh tự chụp.
