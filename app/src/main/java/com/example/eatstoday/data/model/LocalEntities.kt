package com.example.eatstoday.data.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class Favorite(
    @PrimaryKey val recipeId: Int,
    val addedAt: Long,
)

@Entity(tableName = "cook_history")
data class CookHistory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val recipeId: Int,
    val recipeName: String,
    val cookedAt: Long,
)

@Entity(tableName = "collections", indices = [Index(value = ["name"], unique = true)])
data class RecipeCollection(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
)

@Entity(tableName = "collection_recipes", primaryKeys = ["collectionId", "recipeId"])
data class CollectionRecipe(
    val collectionId: Long,
    val recipeId: Int,
)

data class CollectionWithCount(
    @Embedded val collection: RecipeCollection,
    val recipeCount: Int,
)

@Entity(tableName = "pantry_items", indices = [Index(value = ["name"], unique = true)])
data class PantryItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val quantity: Double? = null,
    val unit: String = "",
    val isAvailable: Boolean = true,
)

@Entity(tableName = "shopping_items")
data class ShoppingItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val quantity: String,
    val category: String = IngredientCategory.OTHER,
    val recipeName: String? = null,
    val isBought: Boolean = false,
)

enum class FriendStatus { FRIEND, REQUEST_RECEIVED, REQUEST_SENT, SUGGESTED }

@Entity(tableName = "friends")
data class Friend(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val avatarText: String,
    val cookedCount: Int = 0,
    val specialty: String = "Món Việt",
    val status: FriendStatus = FriendStatus.FRIEND,
    val note: String = "",
    val cookingNow: String? = null,
)

@Entity(tableName = "posts")
data class Post(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val authorName: String,
    val avatarText: String,
    val isMine: Boolean = false,
    val recipeId: Int? = null,
    val recipeName: String,
    val photoPath: String? = null,
    val message: String = "",
    val likes: Int = 0,
    val likedByMe: Boolean = false,
    val rating: Double = 0.0,
    val ratingCount: Int = 0,
    val myRating: Int = 0,
    val allowRating: Boolean = true,
    val createdAt: Long,
)

data class PostWithCount(
    @Embedded val post: Post,
    val commentCount: Int,
)

@Entity(tableName = "comments", indices = [Index("postId")])
data class Comment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val postId: Long,
    val authorName: String,
    val avatarText: String,
    val text: String,
    val rating: Int? = null,
    val createdAt: Long,
)

@Entity(tableName = "notifications")
data class AppNotification(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val actor: String,
    val message: String,
    val createdAt: Long,
    val isRead: Boolean = false,
)
