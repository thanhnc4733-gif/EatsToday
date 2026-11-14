package com.example.eatstoday.data.local

import com.example.eatstoday.data.model.CollectionRecipe
import com.example.eatstoday.data.model.Favorite
import com.example.eatstoday.data.model.PantryItem
import com.example.eatstoday.data.model.RecipeCollection

/**
 * Ghi dữ liệu mẫu đúng một lần ở lần chạy đầu; người dùng sửa xóa bình thường sau đó.
 * Không tạo bạn bè, bài đăng hay thông báo giả: những phần này chỉ có khi có người dùng thật.
 */
suspend fun seedDatabase(db: AppDatabase) {
    val now = System.currentTimeMillis()

    val pantry = db.pantryDao()
    listOf(
        PantryItem(name = "Trứng", quantity = 6.0, unit = "quả"),
        PantryItem(name = "Cà chua", quantity = 3.0, unit = "quả"),
        PantryItem(name = "Hành lá", quantity = 1.0, unit = "bó"),
        PantryItem(name = "Thịt heo", isAvailable = false),
        PantryItem(name = "Cá", isAvailable = false),
        PantryItem(name = "Đậu hũ", isAvailable = false),
    ).forEach { pantry.insert(it) }

    val recipe = db.recipeDao()
    val weekend = recipe.addCollection(RecipeCollection(name = "Cuối tuần"))
    val quick = recipe.addCollection(RecipeCollection(name = "Nấu nhanh"))
    listOf(3, 4).forEachIndexed { index, id -> recipe.addFavorite(Favorite(id, now - index)) }
    recipe.addToCollection(CollectionRecipe(weekend, 3))
    recipe.addToCollection(CollectionRecipe(quick, 4))
}
