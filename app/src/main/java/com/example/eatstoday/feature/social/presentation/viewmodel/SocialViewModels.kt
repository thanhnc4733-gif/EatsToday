package com.example.eatstoday.feature.social.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eatstoday.data.model.FeedComment
import com.example.eatstoday.data.model.FeedPost
import com.example.eatstoday.data.model.User
import com.example.eatstoday.data.remote.SocialException
import com.example.eatstoday.data.repository.FeedRepository
import com.example.eatstoday.data.repository.RecipeRepository
import com.example.eatstoday.data.repository.UserRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private fun <T> Flow<T>.asState(vm: ViewModel, initial: T): StateFlow<T> =
    stateIn(vm.viewModelScope, SharingStarted.WhileSubscribed(5_000), initial)

data class FeedUiState(
    val signedIn: Boolean = true,
    val loading: Boolean = true,
    val myUid: String? = null,
    val posts: List<FeedPost> = emptyList(),
    val error: String? = null,
)

data class PostUiState(
    val loading: Boolean = true,
    val post: FeedPost? = null,
    val error: String? = null,
)

/** Dùng cho Bảng tin, Đánh giá và màn hình đăng món; dữ liệu đồng bộ qua Firestore. */
@OptIn(ExperimentalCoroutinesApi::class)
class PostViewModel(
    savedStateHandle: SavedStateHandle,
    private val feedRepository: FeedRepository,
    private val userRepository: UserRepository,
    recipeRepository: RecipeRepository,
) : ViewModel() {
    private val postId: String = savedStateHandle.get<String>("postId").orEmpty()
    private val shareRecipeId: Int = savedStateHandle.get<Int>("recipeId") ?: 0
    private val reload = MutableStateFlow(0)

    val shareRecipeName: String = recipeRepository.recipe(shareRecipeId)?.name ?: "Món tự nấu"
    val myUid: String? get() = feedRepository.currentUid

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message
    private val _publishing = MutableStateFlow(false)
    val publishing: StateFlow<Boolean> = _publishing

    val user: StateFlow<User> = userRepository.user.asState(this, User())

    val feed: StateFlow<FeedUiState> = reload.flatMapLatest { feedRepository.uid }.flatMapLatest { uid ->
        if (uid == null) {
            flowOf(FeedUiState(signedIn = false, loading = false))
        } else {
            feedRepository.feed(uid)
                .map { FeedUiState(loading = false, myUid = uid, posts = it) }
                .onStart { emit(FeedUiState(myUid = uid)) }
                .catch { emit(FeedUiState(loading = false, myUid = uid, error = it.message)) }
        }
    }.asState(this, FeedUiState())

    val post: StateFlow<PostUiState> = reload.flatMapLatest {
        if (postId.isEmpty()) {
            flowOf(PostUiState(loading = false))
        } else {
            feedRepository.post(postId)
                .map { PostUiState(loading = false, post = it) }
                .catch { emit(PostUiState(loading = false, error = it.message)) }
        }
    }.asState(this, PostUiState())

    val comments: StateFlow<List<FeedComment>> = reload.flatMapLatest {
        if (postId.isEmpty()) flowOf(emptyList()) else feedRepository.comments(postId).catch { emit(emptyList()) }
    }.asState(this, emptyList())

    init {
        // Bài cũ của mình hiện cho cả bạn mới kết bạn.
        viewModelScope.launch { runCatching { feedRepository.syncAudience() } }
    }

    fun photoOf(postId: String): String? = feedRepository.photoOf(postId)

    suspend fun loadPhoto(post: FeedPost): String? = runCatching { feedRepository.loadPhoto(post) }.getOrNull()

    fun retry() {
        reload.value++
    }

    fun clearMessage() {
        _message.value = null
    }

    private fun perform(success: String? = null, action: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                action()
                success?.let { _message.value = it }
            } catch (e: SocialException) {
                _message.value = e.message
            }
        }
    }

    private suspend fun myName(): String = userRepository.user.first().name

    fun toggleLike(item: FeedPost) = perform { feedRepository.toggleLike(item) }

    fun rate(stars: Int) {
        val current = post.value.post ?: return
        perform { feedRepository.rate(current, stars, myName()) }
    }

    fun sendComment(text: String) {
        val current = post.value.post ?: return
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return
        perform { feedRepository.comment(current, trimmed, current.ratingOf(myUid).takeIf { it > 0 }, myName()) }
    }

    fun deletePost(onDone: () -> Unit) {
        val current = post.value.post ?: return
        perform {
            feedRepository.delete(current)
            onDone()
        }
    }

    fun publish(photoPath: String?, message: String, allowRating: Boolean, onDone: () -> Unit) {
        if (_publishing.value) return
        _publishing.value = true
        viewModelScope.launch {
            try {
                feedRepository.publish(
                    authorName = myName(),
                    recipeId = shareRecipeId.takeIf { it > 0 },
                    recipeName = shareRecipeName,
                    photoPath = photoPath,
                    message = message.trim(),
                    allowRating = allowRating,
                )
                onDone()
            } catch (e: SocialException) {
                _message.value = e.message
            } finally {
                _publishing.value = false
            }
        }
    }
}
