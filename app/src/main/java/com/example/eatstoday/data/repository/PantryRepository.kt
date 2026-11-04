package com.example.eatstoday.data.repository

import com.example.eatstoday.data.local.PantryDao
import com.example.eatstoday.data.local.ShoppingDao
import com.example.eatstoday.data.model.Ingredient
import com.example.eatstoday.data.model.PantryItem
import com.example.eatstoday.data.model.ShoppingItem
import kotlinx.coroutines.flow.Flow

class PantryRepository(
    private val pantryDao: PantryDao,
    private val shoppingDao: ShoppingDao,
) {
    val pantryItems: Flow<List<PantryItem>> = pantryDao.items()
    val shoppingItems: Flow<List<ShoppingItem>> = shoppingDao.items()

    suspend fun pantryItem(id: Long): PantryItem? = pantryDao.item(id)

    /** Trả về false nếu tên đã tồn tại ở một nguyên liệu khác. */
    suspend fun savePantryItem(item: PantryItem): Boolean {
        val duplicate = pantryDao.itemByName(item.name)
        if (duplicate != null && duplicate.id != item.id) return false
        if (item.id == 0L) pantryDao.insert(item) else pantryDao.update(item)
        return true
    }

    suspend fun setAvailable(item: PantryItem, available: Boolean) = pantryDao.update(item.copy(isAvailable = available))
    suspend fun deletePantryItem(item: PantryItem) = pantryDao.delete(item)

    /** Trả về số món thực sự được thêm; món đang chờ mua sẽ không bị thêm trùng. */
    suspend fun addToShopping(ingredients: List<Ingredient>, recipeName: String): Int {
        var added = 0
        ingredients.forEach { ingredient ->
            if (shoppingDao.pendingByName(ingredient.name) == null) {
                shoppingDao.insert(
                    ShoppingItem(
                        name = ingredient.name,
                        quantity = ingredient.quantity,
                        category = ingredient.category,
                        recipeName = recipeName,
                    ),
                )
                added++
            }
        }
        return added
    }

    suspend fun addShoppingItem(name: String, quantity: String, category: String): Boolean {
        if (shoppingDao.pendingByName(name) != null) return false
        shoppingDao.insert(ShoppingItem(name = name, quantity = quantity, category = category))
        return true
    }

    suspend fun setBought(item: ShoppingItem, bought: Boolean) = shoppingDao.update(item.copy(isBought = bought))
    suspend fun deleteShoppingItem(item: ShoppingItem) = shoppingDao.delete(item)

    /** Chuyển các món đã mua vào tủ lạnh rồi xóa khỏi danh sách đi chợ. Trả về số món đã chuyển. */
    suspend fun moveBoughtToPantry(): Int {
        val bought = shoppingDao.bought()
        bought.forEach { item ->
            val existing = pantryDao.itemByName(item.name)
            if (existing == null) {
                pantryDao.insert(PantryItem(name = item.name, unit = item.quantity))
            } else {
                pantryDao.update(existing.copy(isAvailable = true))
            }
        }
        shoppingDao.deleteBought()
        return bought.size
    }
}
