package com.example.eatstoday.data.remote

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.tasks.await

/** Thông tin tài khoản lấy từ Firebase sau khi đăng nhập. */
data class AuthAccount(val uid: String, val name: String?, val email: String?)

/** Lỗi đã được dịch sang câu tiếng Việt để hiển thị thẳng cho người dùng. */
class AuthException(message: String, val field: Field = Field.GENERAL) : Exception(message) {
    enum class Field { EMAIL, PASSWORD, GENERAL }
}

/** Lớp duy nhất gọi Firebase Authentication; phần còn lại của app không phụ thuộc Firebase. */
class FirebaseAuthSource(private val auth: FirebaseAuth = FirebaseAuth.getInstance()) {

    val currentAccount: AuthAccount? get() = auth.currentUser?.toAccount()

    suspend fun signIn(email: String, password: String): AuthAccount = call {
        auth.signInWithEmailAndPassword(email.trim(), password).await().user!!.toAccount()
    }

    suspend fun register(name: String, email: String, password: String): AuthAccount = call {
        val user = auth.createUserWithEmailAndPassword(email.trim(), password).await().user!!
        user.updateProfile(UserProfileChangeRequest.Builder().setDisplayName(name.trim()).build()).await()
        AuthAccount(user.uid, name.trim(), user.email)
    }

    suspend fun signInWithGoogle(idToken: String): AuthAccount = call {
        auth.signInWithCredential(GoogleAuthProvider.getCredential(idToken, null)).await().user!!.toAccount()
    }

    suspend fun sendPasswordReset(email: String) = call {
        auth.sendPasswordResetEmail(email.trim()).await()
        Unit
    }

    fun signOut() = auth.signOut()

    private fun FirebaseUser.toAccount() = AuthAccount(uid, displayName, email)

    private suspend fun <T> call(block: suspend () -> T): T = try {
        block()
    } catch (e: FirebaseAuthWeakPasswordException) {
        throw AuthException("Mật khẩu quá yếu, hãy dùng ít nhất 8 ký tự có cả chữ và số", AuthException.Field.PASSWORD)
    } catch (e: FirebaseAuthUserCollisionException) {
        throw AuthException("Email này đã có tài khoản", AuthException.Field.EMAIL)
    } catch (e: FirebaseAuthInvalidUserException) {
        throw AuthException("Email hoặc mật khẩu không đúng", AuthException.Field.PASSWORD)
    } catch (e: FirebaseAuthInvalidCredentialsException) {
        throw AuthException("Email hoặc mật khẩu không đúng", AuthException.Field.PASSWORD)
    } catch (e: FirebaseNetworkException) {
        throw AuthException("Không có kết nối mạng, hãy thử lại")
    } catch (e: FirebaseTooManyRequestsException) {
        throw AuthException("Bạn thử quá nhiều lần, hãy đợi một lúc rồi thử lại")
    } catch (e: AuthException) {
        throw e
    } catch (e: Exception) {
        throw AuthException("Không thực hiện được, hãy thử lại sau")
    }
}
