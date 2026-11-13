# Room và Compose đã kèm consumer rules; không cần rule riêng.

# Credential Manager tìm lớp Play Services bằng reflection, cần giữ lại khi bật rút gọn mã.
-if class androidx.credentials.CredentialManager
-keep class androidx.credentials.playservices.** { *; }
