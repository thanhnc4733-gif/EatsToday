package com.example.eatstoday.data.model

enum class Difficulty(val label: String) { EASY("Dễ"), MEDIUM("Vừa"), HARD("Khó") }

enum class MealType(val label: String) { BREAKFAST("Sáng"), LUNCH("Trưa"), DINNER("Tối"), SNACK("Ăn vặt") }

enum class DietTag(val label: String) { VEGETARIAN("Chay"), LOW_CALORIE("Ít calo"), GLUTEN_FREE("Không gluten") }

data class Recipe(
    val id: Int,
    val name: String,
    /** Tên ảnh trong res/drawable; null thì hiển thị khung ảnh giữ chỗ như thiết kế. */
    val image: String? = null,
    val description: String,
    val cookingTime: Int,
    val servings: Int,
    val rating: Double,
    val difficulty: Difficulty,
    val meals: Set<MealType>,
    val diets: Set<DietTag> = emptySet(),
    val cuisine: String = "Món Việt",
    val isPremium: Boolean = false,
    val ingredients: List<Ingredient>,
    val steps: List<CookingStep>,
)

data class Ingredient(
    val id: Int,
    val name: String,
    val quantity: String,
    /** Nhóm hiển thị trong danh sách đi chợ. */
    val category: String = IngredientCategory.VEGETABLE,
    /** Gia vị cơ bản không tính là "thiếu" khi gợi ý món từ tủ lạnh. */
    val isStaple: Boolean = false,
    val isAvailable: Boolean = false,
)

object IngredientCategory {
    const val PROTEIN = "Thịt & hải sản"
    const val VEGETABLE = "Rau củ & gia vị"
    const val OTHER = "Khác"
    val all = listOf(PROTEIN, VEGETABLE, OTHER)
}

data class CookingStep(
    val id: Int,
    val title: String,
    val description: String,
    /** Thời lượng hẹn giờ (giây); null nếu bước này không cần hẹn giờ. */
    val duration: Int? = null,
)
