package com.example.eatstoday.data.local

import com.example.eatstoday.data.model.AppNotification
import com.example.eatstoday.data.model.CollectionRecipe
import com.example.eatstoday.data.model.Comment
import com.example.eatstoday.data.model.Favorite
import com.example.eatstoday.data.model.Friend
import com.example.eatstoday.data.model.FriendStatus
import com.example.eatstoday.data.model.PantryItem
import com.example.eatstoday.data.model.Post
import com.example.eatstoday.data.model.RecipeCollection

/** Ghi dữ liệu mẫu đúng một lần ở lần chạy đầu; người dùng sửa xóa bình thường sau đó. */
suspend fun seedDatabase(db: AppDatabase) {
    val now = System.currentTimeMillis()
    val minute = 60_000L
    val hour = 60 * minute

    val pantry = db.pantryDao()
    listOf(
        PantryItem(name = "Trứng", quantity = 6.0, unit = "quả"),
        PantryItem(name = "Cà chua", quantity = 3.0, unit = "quả"),
        PantryItem(name = "Hành lá", quantity = 1.0, unit = "bó"),
        PantryItem(name = "Thịt heo", isAvailable = false),
        PantryItem(name = "Cá", isAvailable = false),
        PantryItem(name = "Đậu hũ", isAvailable = false),
    ).forEach { pantry.insert(it) }

    val social = db.socialDao()
    listOf(
        Friend(name = "Mi", avatarText = "Mi", cookedCount = 24, cookingNow = "Canh chua cá"),
        Friend(name = "Hà", avatarText = "Hà", cookedCount = 18),
        Friend(name = "Tú", avatarText = "Tú", cookedCount = 7),
        Friend(name = "Lan", avatarText = "Lan", cookedCount = 31),
        Friend(name = "Minh Tú", avatarText = "MT", status = FriendStatus.REQUEST_RECEIVED, note = "Bạn chung: Hà"),
        Friend(name = "Bảo Ngọc", avatarText = "BN", status = FriendStatus.REQUEST_RECEIVED, note = "Gửi 1 giờ trước"),
        Friend(name = "Lan Anh", avatarText = "LA", status = FriendStatus.SUGGESTED, note = "Bạn chung: Mi, Hà"),
    ).forEach { social.insertFriend(it) }

    val miPost = social.insertPost(
        Post(
            authorName = "Mi", avatarText = "Mi", recipeId = 3, recipeName = "Canh chua cá",
            likes = 12, rating = 4.5, ratingCount = 2, createdAt = now - 5 * minute,
        ),
    )
    social.insertPost(
        Post(
            authorName = "Hà", avatarText = "Hà", recipeId = 4, recipeName = "Gà kho gừng",
            likes = 3, rating = 4.0, ratingCount = 1, createdAt = now - hour,
        ),
    )
    social.insertComment(
        Comment(
            postId = miPost, authorName = "Hà", avatarText = "Hà", rating = 5,
            text = "Nhìn hấp dẫn thế, cho mình xin công thức!", createdAt = now - 4 * minute,
        ),
    )
    social.insertComment(
        Comment(
            postId = miPost, authorName = "Tú", avatarText = "Tú", rating = 4,
            text = "Cuối tuần mình nấu thử.", createdAt = now - 3 * minute,
        ),
    )

    social.insertNotification(AppNotification(actor = "Hà", message = "chấm 5 sao món của bạn", createdAt = now - 2 * minute))
    social.insertNotification(AppNotification(actor = "Tú", message = "bình luận: \"Cuối tuần mình nấu thử\"", createdAt = now - hour))
    social.insertNotification(AppNotification(actor = "Lan", message = "gửi bạn một món", createdAt = now - 24 * hour))

    val recipe = db.recipeDao()
    val weekend = recipe.addCollection(RecipeCollection(name = "Cuối tuần"))
    val quick = recipe.addCollection(RecipeCollection(name = "Nấu nhanh"))
    listOf(3, 4).forEachIndexed { index, id -> recipe.addFavorite(Favorite(id, now - index)) }
    recipe.addToCollection(CollectionRecipe(weekend, 3))
    recipe.addToCollection(CollectionRecipe(quick, 4))
}
