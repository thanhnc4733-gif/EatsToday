package com.example.eatstoday.data.repository

import com.example.eatstoday.data.local.SocialDao
import com.example.eatstoday.data.model.AppNotification
import com.example.eatstoday.data.model.Comment
import com.example.eatstoday.data.model.Friend
import com.example.eatstoday.data.model.FriendStatus
import com.example.eatstoday.data.model.Post
import com.example.eatstoday.data.model.PostWithCount
import kotlinx.coroutines.flow.Flow

/** Mô phỏng trên thiết bị: chưa có backend nên không đồng bộ với người dùng khác. */
class SocialRepository(private val dao: SocialDao) {
    val posts: Flow<List<PostWithCount>> = dao.posts()
    val friends: Flow<List<Friend>> = dao.friends()
    val notifications: Flow<List<AppNotification>> = dao.notifications()

    fun post(id: Long): Flow<Post?> = dao.post(id)
    fun comments(postId: Long): Flow<List<Comment>> = dao.comments(postId)
    fun friend(id: Long): Flow<Friend?> = dao.friend(id)

    suspend fun createPost(post: Post) {
        dao.insertPost(post)
    }

    suspend fun toggleLike(postId: Long) {
        val post = dao.postOnce(postId) ?: return
        val liked = !post.likedByMe
        dao.updatePost(post.copy(likedByMe = liked, likes = (post.likes + if (liked) 1 else -1).coerceAtLeast(0)))
    }

    /** Mỗi người chỉ có một điểm: chấm lại sẽ thay điểm cũ, không cộng thêm lượt. */
    suspend fun rate(postId: Long, stars: Int) {
        val post = dao.postOnce(postId) ?: return
        val value = stars.coerceIn(1, 5)
        val count = if (post.myRating == 0) post.ratingCount + 1 else post.ratingCount
        val total = post.rating * post.ratingCount - post.myRating + value
        dao.updatePost(post.copy(myRating = value, ratingCount = count, rating = total / count))
    }

    suspend fun addComment(postId: Long, author: String, avatar: String, text: String, rating: Int?) {
        dao.insertComment(
            Comment(
                postId = postId,
                authorName = author,
                avatarText = avatar,
                text = text,
                rating = rating?.takeIf { it > 0 },
                createdAt = System.currentTimeMillis(),
            ),
        )
    }

    suspend fun setFriendStatus(id: Long, status: FriendStatus) = dao.setFriendStatus(id, status)
    suspend fun markNotificationsRead() = dao.markAllRead()
}
