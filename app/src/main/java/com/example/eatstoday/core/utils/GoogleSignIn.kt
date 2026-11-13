package com.example.eatstoday.core.utils

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential

/** Kết quả mở hộp chọn tài khoản Google. */
sealed interface GoogleSignInResult {
    data class Success(val idToken: String) : GoogleSignInResult
    data object Cancelled : GoogleSignInResult
    data class Failure(val message: String) : GoogleSignInResult
}

object GoogleSignIn {
    /**
     * Mã web client do plugin google-services sinh ra từ google-services.json.
     * Chỉ có khi đã bật đăng nhập Google trong Firebase trước khi tải file, nên đọc theo tên để thiếu thì không lỗi build.
     */
    private fun webClientId(context: Context): String? {
        val id = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
        return if (id == 0) null else context.getString(id)
    }

    /** [activityContext] phải là Activity để hệ thống hiện được hộp chọn tài khoản. */
    suspend fun requestIdToken(activityContext: Context): GoogleSignInResult {
        val clientId = webClientId(activityContext)
            ?: return GoogleSignInResult.Failure("Chưa bật đăng nhập Google trong Firebase cho ứng dụng này")
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(GetSignInWithGoogleOption.Builder(clientId).build())
            .build()
        return try {
            val credential = CredentialManager.create(activityContext).getCredential(activityContext, request).credential
            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                GoogleSignInResult.Success(GoogleIdTokenCredential.createFrom(credential.data).idToken)
            } else {
                GoogleSignInResult.Failure("Không nhận được tài khoản Google")
            }
        } catch (e: GetCredentialCancellationException) {
            GoogleSignInResult.Cancelled
        } catch (e: NoCredentialException) {
            GoogleSignInResult.Failure("Máy chưa có tài khoản Google nào. Hãy thêm tài khoản trong Cài đặt của máy")
        } catch (e: GetCredentialException) {
            GoogleSignInResult.Failure("Không đăng nhập Google được, hãy thử lại")
        }
    }
}
