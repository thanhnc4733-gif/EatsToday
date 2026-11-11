package com.example.eatstoday.feature.social.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.eatstoday.core.components.*
import com.example.eatstoday.core.utils.AppViewModelProvider
import com.example.eatstoday.core.utils.isOnline
import com.example.eatstoday.core.utils.relativeTime
import com.example.eatstoday.data.model.PostWithCount
import com.example.eatstoday.feature.social.presentation.viewmodel.PostViewModel
import kotlin.math.roundToInt
import com.example.eatstoday.data.local.RecipeSampleData

private fun recipeImage(recipeId: Int?): String? = RecipeSampleData.recipes.firstOrNull { it.id == recipeId }?.image

@Composable
fun OfflineContent(onRetry: () -> Unit, onSaved: () -> Unit) {
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
        EmptyState(
            "Mất mạng",
            "Không có kết nối",
            "Kiểm tra mạng rồi thử lại. Món đã lưu vẫn xem được.",
            actionLabel = "Thử lại",
            onAction = onRetry,
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            LinkButton("Xem món đã lưu", onSaved)
        }
    }
}

@Composable
fun FeedScreen(
    onPostClick: (Long) -> Unit,
    onFriends: () -> Unit,
    onCamera: () -> Unit,
    onSaved: () -> Unit,
    viewModel: PostViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val context = LocalContext.current
    var online by rememberSaveable { mutableStateOf(isOnline(context)) }
    if (!online) {
        OfflineContent(onRetry = { online = isOnline(context) }, onSaved = onSaved)
        return
    }
    val posts by viewModel.posts.collectAsStateWithLifecycle()
    val user by viewModel.user.collectAsStateWithLifecycle()
    ScreenColumn {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Bạn bè đang nấu", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.weight(1f))
            IconButton(onClick = onCamera) { Icon(Icons.Outlined.CameraAlt, contentDescription = "Chụp món") }
            IconButton(onClick = onFriends) { Icon(Icons.Outlined.People, contentDescription = "Bạn bè") }
        }
        if (posts.isEmpty()) {
            EmptyState("Chưa có bài", "Chưa có bài đăng", "Nấu một món rồi chụp ảnh để chia sẻ nhé.")
        }
        posts.forEach { item ->
            PostCard(item, user.avatarPath, onOpen = { onPostClick(item.post.id) }, onLike = { viewModel.toggleLike(item.post.id) })
        }
    }
}

@Composable
private fun PostCard(item: PostWithCount, myAvatarPath: String?, onOpen: () -> Unit, onLike: () -> Unit) {
    val post = item.post
    EatsCard(onClick = onOpen, modifier = Modifier.fillMaxWidth(), contentPadding = 12.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                UserAvatar(post.avatarText, photoPath = myAvatarPath.takeIf { post.isMine })
                Column(Modifier.weight(1f)) {
                    Text(post.authorName, style = MaterialTheme.typography.titleSmall)
                    Text(
                        "vừa nấu · ${relativeTime(post.createdAt)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            FoodImage("Ảnh món của ${post.authorName}", Modifier.fillMaxWidth().height(210.dp), photoPath = post.photoPath, imageName = recipeImage(post.recipeId))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(post.recipeName, style = MaterialTheme.typography.labelMedium, modifier = Modifier.weight(1f))
                RecipeRating(post.rating.roundToInt())
            }
            if (post.message.isNotEmpty()) Text(post.message, style = MaterialTheme.typography.bodyMedium)
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onLike) {
                    Icon(
                        imageVector = if (post.likedByMe) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = if (post.likedByMe) "Bỏ thích" else "Thích",
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
                Text(
                    "${post.likes} lượt thích · ${item.commentCount} bình luận",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                if (post.allowRating && !post.isMine) {
                    Text("Chấm sao", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
fun ReviewScreen(
    onBack: () -> Unit,
    viewModel: PostViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val post by viewModel.post.collectAsStateWithLifecycle()
    val comments by viewModel.comments.collectAsStateWithLifecycle()
    val user by viewModel.user.collectAsStateWithLifecycle()
    var draft by rememberSaveable { mutableStateOf("") }
    val current = post
    ScreenColumn(
        bottomBar = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                CustomTextField(draft, { draft = it }, "Viết bình luận...", modifier = Modifier.weight(1f))
                PrimaryButton(
                    "Gửi",
                    {
                        viewModel.sendComment(draft)
                        draft = ""
                    },
                    modifier = Modifier.width(90.dp),
                    enabled = draft.isNotBlank(),
                )
            }
        },
    ) {
        if (current == null) {
            EatsLoading(Modifier.height(200.dp))
            return@ScreenColumn
        }
        FoodImage("Ảnh món của ${current.authorName}", Modifier.fillMaxWidth().height(230.dp), photoPath = current.photoPath, radius = 24.dp, imageName = recipeImage(current.recipeId))
        Column {
            Text(current.recipeName, style = MaterialTheme.typography.titleMedium)
            Text(
                "${current.authorName} · ${relativeTime(current.createdAt)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (current.message.isNotEmpty()) Text(current.message, style = MaterialTheme.typography.bodyMedium)
        if (current.allowRating && !current.isMine) {
            Text(
                if (current.myRating > 0) "Bạn đã chấm ${current.myRating} sao, chạm để đổi" else "Chạm để chấm sao",
                style = MaterialTheme.typography.bodySmall,
            )
            RecipeRating(current.myRating, onRate = viewModel::rate)
        }
        Text(
            "Điểm trung bình: %.1f (%d lượt)".format(current.rating, current.ratingCount),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        ChipRow {
            listOf("Ngon quá", "Thèm quá", "Muốn thử").forEach { text ->
                EatsChip(text, false, { viewModel.sendComment(text) })
            }
        }
        EatsCard(Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (comments.isEmpty()) {
                    Text("Chưa có bình luận.", style = MaterialTheme.typography.bodySmall)
                }
                comments.forEach { comment ->
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        UserAvatar(comment.avatarText, photoPath = user.avatarPath.takeIf { comment.authorName == user.name })
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(comment.authorName, style = MaterialTheme.typography.labelMedium)
                                comment.rating?.let { RecipeRating(it, starSize = 14.dp) }
                            }
                            Text(comment.text, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}
