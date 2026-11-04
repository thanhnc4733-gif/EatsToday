package com.example.eatstoday.feature.social.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eatstoday.data.model.Comment
import com.example.eatstoday.data.model.Friend
import com.example.eatstoday.data.model.FriendStatus
import com.example.eatstoday.data.model.Post
import com.example.eatstoday.data.model.PostWithCount
import com.example.eatstoday.data.model.User
import com.example.eatstoday.data.repository.RecipeRepository
import com.example.eatstoday.data.repository.SocialRepository
import com.example.eatstoday.data.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private fun <T> Flow<T>.asState(vm: ViewModel, initial: T): StateFlow<T> =
    stateIn(vm.viewModelScope, SharingStarted.WhileSubscribed(5_000), initial)

/** Dùng cho Bảng tin, Đánh giá và Gửi bạn bè. */
class PostViewModel(
    savedStateHandle: SavedStateHandle,
    private val socialRepository: SocialRepository,
    private val userRepository: UserRepository,
    private val recipeRepository: RecipeRepository,
) : ViewModel() {
    private val postId: Long = savedStateHandle.get<Long>("postId") ?: -1L
    private val shareRecipeId: Int = savedStateHandle.get<Int>("recipeId") ?: 0
    private var publishing = false

    val shareRecipeName: String = recipeRepository.recipe(shareRecipeId)?.name ?: "Món tự nấu"

    val user: StateFlow<User> = userRepository.user.asState(this, User())
    val posts: StateFlow<List<PostWithCount>> = socialRepository.posts.asState(this, emptyList())
    val post: StateFlow<Post?> = socialRepository.post(postId).asState(this, null)
    val comments: StateFlow<List<Comment>> = socialRepository.comments(postId).asState(this, emptyList())
    val friends: StateFlow<List<Friend>> = socialRepository.friends
        .map { list -> list.filter { it.status == FriendStatus.FRIEND } }
        .asState(this, emptyList())

    fun toggleLike(id: Long) {
        viewModelScope.launch { socialRepository.toggleLike(id) }
    }

    fun rate(stars: Int) {
        viewModelScope.launch { socialRepository.rate(postId, stars) }
    }

    fun sendComment(text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            val user = userRepository.user.first()
            val myRating = post.value?.myRating
            socialRepository.addComment(postId, user.name, user.avatarText, trimmed, myRating)
        }
    }

    fun publish(photoPath: String?, message: String, allowRating: Boolean, onDone: () -> Unit) {
        if (publishing) return
        publishing = true
        viewModelScope.launch {
            val user = userRepository.user.first()
            socialRepository.createPost(
                Post(
                    authorName = user.name,
                    avatarText = user.avatarText,
                    isMine = true,
                    recipeId = shareRecipeId.takeIf { it > 0 },
                    recipeName = shareRecipeName,
                    photoPath = photoPath,
                    message = message.trim(),
                    allowRating = allowRating,
                    createdAt = System.currentTimeMillis(),
                ),
            )
            onDone()
        }
    }
}

class FriendsViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: SocialRepository,
) : ViewModel() {
    private val friendId: Long = savedStateHandle.get<Long>("friendId") ?: -1L

    val all: StateFlow<List<Friend>> = repository.friends.asState(this, emptyList())
    val friend: StateFlow<Friend?> = repository.friend(friendId).asState(this, null)

    fun sendRequest(id: Long) = setStatus(id, FriendStatus.REQUEST_SENT)
    fun accept(id: Long) = setStatus(id, FriendStatus.FRIEND)
    fun decline(id: Long) = setStatus(id, FriendStatus.SUGGESTED)
    fun cancelRequest(id: Long) = setStatus(id, FriendStatus.SUGGESTED)
    fun remove(id: Long) = setStatus(id, FriendStatus.SUGGESTED)

    private fun setStatus(id: Long, status: FriendStatus) {
        viewModelScope.launch { repository.setFriendStatus(id, status) }
    }
}
