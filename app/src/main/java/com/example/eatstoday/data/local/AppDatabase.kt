package com.example.eatstoday.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.eatstoday.data.model.AppNotification
import com.example.eatstoday.data.model.CollectionRecipe
import com.example.eatstoday.data.model.Comment
import com.example.eatstoday.data.model.CookHistory
import com.example.eatstoday.data.model.Favorite
import com.example.eatstoday.data.model.Friend
import com.example.eatstoday.data.model.PantryItem
import com.example.eatstoday.data.model.Post
import com.example.eatstoday.data.model.RecipeCollection
import com.example.eatstoday.data.model.ShoppingItem

@Database(
    entities = [
        Favorite::class, CookHistory::class, RecipeCollection::class, CollectionRecipe::class,
        PantryItem::class, ShoppingItem::class, Friend::class, Post::class, Comment::class,
        AppNotification::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun recipeDao(): RecipeDao
    abstract fun pantryDao(): PantryDao
    abstract fun shoppingDao(): ShoppingDao
    abstract fun socialDao(): SocialDao

    companion object {
        fun create(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, "eatstoday.db").build()
    }
}
