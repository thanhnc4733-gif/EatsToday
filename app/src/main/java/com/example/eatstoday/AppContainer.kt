package com.example.eatstoday

import android.content.Context
import androidx.room.withTransaction
import com.example.eatstoday.data.local.AppDatabase
import com.example.eatstoday.data.local.LocalDataStore
import com.example.eatstoday.data.local.seedDatabase
import com.example.eatstoday.data.remote.FirebaseAuthSource
import com.example.eatstoday.data.repository.PantryRepository
import com.example.eatstoday.data.repository.RecipeRepository
import com.example.eatstoday.data.repository.SocialRepository
import com.example.eatstoday.data.repository.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/** Khởi tạo thủ công các phụ thuộc dùng chung trong toàn ứng dụng. */
class AppContainer(context: Context) {
    private val appContext = context.applicationContext
    private val database = AppDatabase.create(appContext)
    private val dataStore = LocalDataStore(appContext)

    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    val recipeRepository = RecipeRepository(database.recipeDao())
    val pantryRepository = PantryRepository(database.pantryDao(), database.shoppingDao())
    val socialRepository = SocialRepository(database.socialDao())
    val userRepository = UserRepository(dataStore, FirebaseAuthSource())

    init {
        applicationScope.launch {
            if (!dataStore.isSeeded()) {
                // Giao dịch đảm bảo không ghi dở dang nếu ứng dụng bị dừng giữa chừng.
                database.withTransaction { seedDatabase(database) }
                dataStore.markSeeded()
            }
        }
    }
}
