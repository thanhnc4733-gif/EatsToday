package com.example.eatstoday

import android.content.Context
import androidx.room.withTransaction
import com.example.eatstoday.data.local.AppDatabase
import com.example.eatstoday.data.local.LocalDataStore
import com.example.eatstoday.data.local.PostPhotoStore
import com.example.eatstoday.core.utils.FeedNotifier
import com.example.eatstoday.data.remote.PostRemoteSource
import com.example.eatstoday.data.repository.FeedRepository
import com.example.eatstoday.data.local.seedDatabase
import com.example.eatstoday.data.remote.FirebaseAuthSource
import com.example.eatstoday.data.remote.FriendRemoteSource
import com.example.eatstoday.data.repository.FriendRepository
import com.example.eatstoday.data.repository.PantryRepository
import com.example.eatstoday.data.repository.RecipeRepository
import com.example.eatstoday.data.repository.SocialRepository
import com.example.eatstoday.data.repository.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.retryWhen
import com.example.eatstoday.data.model.RequestStatus

/** Khởi tạo thủ công các phụ thuộc dùng chung trong toàn ứng dụng. */
class AppContainer(context: Context) {
    private val appContext = context.applicationContext
    private val database = AppDatabase.create(appContext)
    private val dataStore = LocalDataStore(appContext)

    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    val recipeRepository = RecipeRepository(database.recipeDao())
    val pantryRepository = PantryRepository(database.pantryDao(), database.shoppingDao())
    val socialRepository = SocialRepository(database.socialDao())
    val friendRepository = FriendRepository(FriendRemoteSource())
    val userRepository = UserRepository(dataStore, FirebaseAuthSource(), friendRepository)
    val feedRepository = FeedRepository(PostRemoteSource(), friendRepository, PostPhotoStore(appContext))

    /** Hiện thông báo hệ thống khi bạn bè đăng món, bình luận hay chấm sao (lúc app đang chạy). */
    private val feedNotifier = FeedNotifier(appContext, feedRepository, userRepository)

    init {
        feedNotifier.start(applicationScope)
        // Mỗi khi danh sách bạn bè đổi (kết bạn, hủy kết bạn), cập nhật người xem các bài của mình.
        applicationScope.launch {
            friendRepository.requests
                .map { list -> list.filter { it.status == RequestStatus.ACCEPTED }.map { it.id }.toSet() }
                .distinctUntilChanged()
                .retryWhen { _, _ -> delay(30_000); true }
                .collect { runCatching { feedRepository.syncAudience() } }
        }
        applicationScope.launch {
            if (!dataStore.isSeeded()) {
                // Giao dịch đảm bảo không ghi dở dang nếu ứng dụng bị dừng giữa chừng.
                database.withTransaction { seedDatabase(database) }
                dataStore.markSeeded()
            }
            // Bản cũ từng nạp sẵn bạn bè, bài đăng và thông báo giả: xóa một lần trên máy đã cài bản cũ.
            if (!dataStore.isSampleSocialRemoved()) {
                database.withTransaction {
                    val social = database.socialDao()
                    social.deleteCommentsOnOthersPosts()
                    social.deleteOthersPosts()
                    social.deleteAllNotifications()
                    social.deleteAllFriends()
                }
                dataStore.markSampleSocialRemoved()
            }
        }
    }
}
