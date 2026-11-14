package com.example.eatstoday.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.eatstoday.data.model.AppNotification
import com.example.eatstoday.data.model.CollectionRecipe
import com.example.eatstoday.data.model.CollectionWithCount
import com.example.eatstoday.data.model.Comment
import com.example.eatstoday.data.model.CookHistory
import com.example.eatstoday.data.model.Favorite
import com.example.eatstoday.data.model.Friend
import com.example.eatstoday.data.model.FriendStatus
import com.example.eatstoday.data.model.PantryItem
import com.example.eatstoday.data.model.Post
import com.example.eatstoday.data.model.PostWithCount
import com.example.eatstoday.data.model.RecipeCollection
import com.example.eatstoday.data.model.ShoppingItem
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {
    @Query("SELECT * FROM favorites ORDER BY addedAt DESC")
    fun favorites(): Flow<List<Favorite>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addFavorite(favorite: Favorite)

    @Query("DELETE FROM favorites WHERE recipeId = :recipeId")
    suspend fun removeFavorite(recipeId: Int)

    @Query("SELECT * FROM cook_history ORDER BY cookedAt DESC")
    fun history(): Flow<List<CookHistory>>

    @Insert
    suspend fun addHistory(history: CookHistory)

    @Query(
        "SELECT c.*, (SELECT COUNT(*) FROM collection_recipes r WHERE r.collectionId = c.id) AS recipeCount " +
            "FROM collections c ORDER BY c.id",
    )
    fun collections(): Flow<List<CollectionWithCount>>

    @Query("SELECT * FROM collection_recipes")
    fun collectionRecipes(): Flow<List<CollectionRecipe>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addCollection(collection: RecipeCollection): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addToCollection(item: CollectionRecipe)

    @Query("DELETE FROM collection_recipes WHERE collectionId = :collectionId AND recipeId = :recipeId")
    suspend fun removeFromCollection(collectionId: Long, recipeId: Int)
}

@Dao
interface PantryDao {
    @Query("SELECT * FROM pantry_items ORDER BY name COLLATE NOCASE")
    fun items(): Flow<List<PantryItem>>

    @Query("SELECT * FROM pantry_items WHERE id = :id")
    suspend fun item(id: Long): PantryItem?

    @Query("SELECT * FROM pantry_items WHERE name = :name COLLATE NOCASE LIMIT 1")
    suspend fun itemByName(name: String): PantryItem?

    @Insert
    suspend fun insert(item: PantryItem): Long

    @Update
    suspend fun update(item: PantryItem)

    @Delete
    suspend fun delete(item: PantryItem)
}

@Dao
interface ShoppingDao {
    @Query("SELECT * FROM shopping_items ORDER BY isBought, id")
    fun items(): Flow<List<ShoppingItem>>

    @Query("SELECT * FROM shopping_items WHERE name = :name COLLATE NOCASE AND isBought = 0 LIMIT 1")
    suspend fun pendingByName(name: String): ShoppingItem?

    @Query("SELECT * FROM shopping_items WHERE isBought = 1")
    suspend fun bought(): List<ShoppingItem>

    @Insert
    suspend fun insert(item: ShoppingItem): Long

    @Update
    suspend fun update(item: ShoppingItem)

    @Delete
    suspend fun delete(item: ShoppingItem)

    @Query("DELETE FROM shopping_items WHERE isBought = 1")
    suspend fun deleteBought()
}

@Dao
interface SocialDao {
    @Query(
        "SELECT p.*, (SELECT COUNT(*) FROM comments c WHERE c.postId = p.id) AS commentCount " +
            "FROM posts p ORDER BY p.createdAt DESC",
    )
    fun posts(): Flow<List<PostWithCount>>

    @Query("SELECT * FROM posts WHERE id = :id")
    fun post(id: Long): Flow<Post?>

    @Query("SELECT * FROM posts WHERE id = :id")
    suspend fun postOnce(id: Long): Post?

    @Insert
    suspend fun insertPost(post: Post): Long

    @Update
    suspend fun updatePost(post: Post)

    @Query("SELECT * FROM comments WHERE postId = :postId ORDER BY createdAt")
    fun comments(postId: Long): Flow<List<Comment>>

    @Insert
    suspend fun insertComment(comment: Comment): Long

    @Query("SELECT * FROM friends ORDER BY name COLLATE NOCASE")
    fun friends(): Flow<List<Friend>>

    @Query("SELECT * FROM friends WHERE id = :id")
    fun friend(id: Long): Flow<Friend?>

    @Insert
    suspend fun insertFriend(friend: Friend): Long

    @Query("UPDATE friends SET status = :status WHERE id = :id")
    suspend fun setFriendStatus(id: Long, status: FriendStatus)

    @Query("SELECT * FROM notifications ORDER BY createdAt DESC")
    fun notifications(): Flow<List<AppNotification>>

    @Insert
    suspend fun insertNotification(notification: AppNotification): Long

    @Query("UPDATE notifications SET isRead = 1 WHERE isRead = 0")
    suspend fun markAllRead()

    @Query("DELETE FROM friends")
    suspend fun deleteAllFriends()

    @Query("DELETE FROM notifications")
    suspend fun deleteAllNotifications()

    @Query("DELETE FROM comments WHERE postId IN (SELECT id FROM posts WHERE isMine = 0)")
    suspend fun deleteCommentsOnOthersPosts()

    @Query("DELETE FROM posts WHERE isMine = 0")
    suspend fun deleteOthersPosts()
}
