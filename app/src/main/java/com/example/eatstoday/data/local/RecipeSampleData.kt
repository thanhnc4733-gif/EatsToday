package com.example.eatstoday.data.local

import com.example.eatstoday.data.model.CookingStep
import com.example.eatstoday.data.model.DietTag
import com.example.eatstoday.data.model.Difficulty
import com.example.eatstoday.data.model.Ingredient
import com.example.eatstoday.data.model.IngredientCategory.OTHER
import com.example.eatstoday.data.model.IngredientCategory.PROTEIN
import com.example.eatstoday.data.model.IngredientCategory.VEGETABLE
import com.example.eatstoday.data.model.MealType
import com.example.eatstoday.data.model.Recipe

/** Công thức mẫu đi kèm ứng dụng ở phiên bản chưa có backend. */
object RecipeSampleData {

    private fun ingredients(vararg items: Ingredient) =
        items.mapIndexed { index, item -> item.copy(id = index + 1) }

    private fun item(name: String, quantity: String, category: String = VEGETABLE, staple: Boolean = false) =
        Ingredient(id = 0, name = name, quantity = quantity, category = category, isStaple = staple)

    private fun steps(vararg items: Triple<String, String, Int?>) =
        items.mapIndexed { index, (title, description, minutes) ->
            CookingStep(index + 1, title, description, minutes?.times(60))
        }

    val recipes: List<Recipe> = listOf(
        Recipe(
            id = 1,
            name = "Bún bò Huế",
            description = "Nước dùng cay nhẹ, thơm sả và mắm ruốc.",
            cookingTime = 45,
            servings = 4,
            rating = 4.8,
            difficulty = Difficulty.MEDIUM,
            meals = setOf(MealType.BREAKFAST, MealType.LUNCH),
            ingredients = ingredients(
                item("Thịt bò bắp", "500 g", PROTEIN),
                item("Giò heo", "1 cái", PROTEIN),
                item("Sả", "5 cây"),
                item("Mắm ruốc", "2 muỗng"),
                item("Bún tươi", "1 kg", OTHER),
                item("Hành lá", "1 bó"),
                item("Muối", "1 muỗng", staple = true),
            ),
            steps = steps(
                Triple("Sơ chế", "Rửa giò heo và bắp bò, chần qua nước sôi rồi rửa lại cho sạch.", 5),
                Triple("Ninh nước dùng", "Ninh giò, bắp bò và sả đập dập với lửa nhỏ. Hớt bọt thường xuyên để nước dùng trong.", 30),
                Triple("Pha mắm ruốc", "Hòa mắm ruốc với nước, để lắng rồi chỉ lấy phần nước trong cho vào nồi.", null),
                Triple("Nêm nếm", "Vớt thịt ra thái lát, nêm muối cho vừa ăn.", null),
                Triple("Trình bày", "Trụng bún, xếp thịt và hành lá vào tô, chan nước dùng thật nóng.", null),
            ),
        ),
        Recipe(
            id = 2,
            name = "Bò sốt vang",
            description = "Thịt bò mềm trong sốt vang đậm đà, hợp ăn cùng bánh mì.",
            cookingTime = 60,
            servings = 4,
            rating = 4.9,
            difficulty = Difficulty.HARD,
            meals = setOf(MealType.DINNER),
            cuisine = "Món Âu",
            isPremium = true,
            ingredients = ingredients(
                item("Thịt bò nạm", "700 g", PROTEIN),
                item("Cà rốt", "2 củ"),
                item("Hành tây", "1 củ"),
                item("Vang đỏ", "200 ml", OTHER),
                item("Sốt cà chua", "3 muỗng", OTHER),
                item("Muối", "1 muỗng", staple = true),
            ),
            steps = steps(
                Triple("Ướp thịt", "Thái thịt miếng vuông, ướp với vang đỏ và muối.", 15),
                Triple("Xào săn", "Xào hành tây thơm rồi cho thịt vào đảo săn mặt ngoài.", 5),
                Triple("Hầm", "Thêm sốt cà chua và nước xâm xấp, hầm lửa nhỏ đến khi thịt mềm.", 35),
                Triple("Thêm cà rốt", "Cho cà rốt vào hầm cùng đến khi vừa chín, nêm lại cho vừa ăn.", 5),
            ),
        ),
        Recipe(
            id = 3,
            name = "Canh chua cá",
            description = "Vị chua thanh từ me và cà chua, ăn với cơm nóng.",
            cookingTime = 30,
            servings = 4,
            rating = 4.7,
            difficulty = Difficulty.EASY,
            meals = setOf(MealType.LUNCH, MealType.DINNER),
            diets = setOf(DietTag.LOW_CALORIE, DietTag.GLUTEN_FREE),
            ingredients = ingredients(
                item("Cá", "500 g", PROTEIN),
                item("Cà chua", "2 quả"),
                item("Dứa", "1/4 quả"),
                item("Đậu bắp", "100 g"),
                item("Giá đỗ", "100 g"),
                item("Me chua", "1 muỗng", staple = true),
                item("Nước mắm", "2 muỗng", staple = true),
            ),
            steps = steps(
                Triple("Sơ chế cá", "Làm sạch cá, cắt khúc và ướp một ít nước mắm.", 10),
                Triple("Nấu nước", "Đun sôi nước với me chua, cho cà chua và dứa vào.", 5),
                Triple("Nấu cá", "Thả cá vào nồi, nấu lửa vừa đến khi cá chín.", 8),
                Triple("Thêm rau", "Thêm đậu bắp và giá đỗ, nêm vừa ăn rồi tắt bếp.", 2),
            ),
        ),
        Recipe(
            id = 4,
            name = "Gà kho gừng",
            description = "Gà săn, sốt sánh và ấm vị gừng.",
            cookingTime = 35,
            servings = 3,
            rating = 4.6,
            difficulty = Difficulty.EASY,
            meals = setOf(MealType.LUNCH, MealType.DINNER),
            diets = setOf(DietTag.GLUTEN_FREE),
            ingredients = ingredients(
                item("Thịt gà", "600 g", PROTEIN),
                item("Gừng", "1 củ"),
                item("Hành tím", "2 củ"),
                item("Nước mắm", "2 muỗng", staple = true),
                item("Đường", "1 muỗng", staple = true),
            ),
            steps = steps(
                Triple("Ướp gà", "Chặt gà miếng vừa ăn, ướp với nước mắm, đường và hành tím.", 15),
                Triple("Xào gừng", "Phi thơm gừng thái sợi rồi cho gà vào đảo săn.", 5),
                Triple("Kho", "Thêm ít nước, kho lửa nhỏ đến khi nước sánh lại.", 15),
            ),
        ),
        Recipe(
            id = 5,
            name = "Trứng chiên cà chua",
            description = "Món nhanh gọn cho ngày bận rộn.",
            cookingTime = 15,
            servings = 2,
            rating = 4.5,
            difficulty = Difficulty.EASY,
            meals = setOf(MealType.BREAKFAST, MealType.DINNER),
            diets = setOf(DietTag.VEGETARIAN, DietTag.GLUTEN_FREE),
            ingredients = ingredients(
                item("Trứng", "3 quả", PROTEIN),
                item("Cà chua", "2 quả"),
                item("Hành lá", "2 nhánh"),
                item("Muối", "1 nhúm", staple = true),
            ),
            steps = steps(
                Triple("Chuẩn bị", "Đánh tan trứng với muối, thái cà chua múi cau.", null),
                Triple("Xào cà", "Xào cà chua đến khi mềm và ra nước.", 4),
                Triple("Chiên trứng", "Đổ trứng vào chảo, đảo nhẹ đến khi vừa chín, rắc hành lá.", 3),
            ),
        ),
        Recipe(
            id = 6,
            name = "Canh trứng cà",
            description = "Canh nhẹ bụng, nấu xong trong một lần đun.",
            cookingTime = 20,
            servings = 3,
            rating = 4.4,
            difficulty = Difficulty.EASY,
            meals = setOf(MealType.LUNCH, MealType.DINNER),
            diets = setOf(DietTag.VEGETARIAN, DietTag.LOW_CALORIE, DietTag.GLUTEN_FREE),
            ingredients = ingredients(
                item("Trứng", "2 quả", PROTEIN),
                item("Cà chua", "3 quả"),
                item("Hành lá", "2 nhánh"),
                item("Rau mùi", "1 nhúm"),
                item("Muối", "1 nhúm", staple = true),
            ),
            steps = steps(
                Triple("Xào cà chua", "Xào cà chua cho mềm rồi thêm nước, đun sôi.", 8),
                Triple("Thả trứng", "Đánh tan trứng, rót thành dòng nhỏ vào nồi và khuấy nhẹ.", 2),
                Triple("Hoàn thành", "Nêm vừa ăn, thêm hành lá và rau mùi rồi tắt bếp.", null),
            ),
        ),
        Recipe(
            id = 7,
            name = "Đậu hũ sốt cà chua",
            description = "Món chay đơn giản, đậu mềm thấm sốt.",
            cookingTime = 15,
            servings = 2,
            rating = 4.5,
            difficulty = Difficulty.EASY,
            meals = setOf(MealType.LUNCH, MealType.DINNER),
            diets = setOf(DietTag.VEGETARIAN, DietTag.LOW_CALORIE, DietTag.GLUTEN_FREE),
            ingredients = ingredients(
                item("Đậu hũ", "2 bìa", PROTEIN),
                item("Cà chua", "2 quả"),
                item("Hành lá", "2 nhánh"),
                item("Muối", "1 nhúm", staple = true),
            ),
            steps = steps(
                Triple("Chiên đậu", "Cắt đậu miếng vuông, chiên vàng các mặt.", 5),
                Triple("Làm sốt", "Xào cà chua thành sốt, nêm vừa ăn.", 4),
                Triple("Om", "Cho đậu vào om cùng sốt, rắc hành lá.", 3),
            ),
        ),
        Recipe(
            id = 8,
            name = "Thịt kho trứng",
            description = "Thịt mềm, trứng đậm vị, nước kho óng màu.",
            cookingTime = 60,
            servings = 4,
            rating = 4.8,
            difficulty = Difficulty.MEDIUM,
            meals = setOf(MealType.DINNER),
            diets = setOf(DietTag.GLUTEN_FREE),
            ingredients = ingredients(
                item("Thịt heo", "600 g", PROTEIN),
                item("Trứng", "4 quả", PROTEIN),
                item("Nước dừa", "500 ml", OTHER),
                item("Nước mắm", "3 muỗng", staple = true),
                item("Đường", "1 muỗng", staple = true),
            ),
            steps = steps(
                Triple("Luộc trứng", "Luộc chín trứng rồi bóc vỏ.", 10),
                Triple("Ướp thịt", "Thái thịt miếng lớn, ướp nước mắm và đường.", 15),
                Triple("Kho", "Đảo săn thịt, thêm nước dừa và trứng, kho lửa nhỏ đến khi thịt mềm.", 35),
            ),
        ),
        Recipe(
            id = 9,
            name = "Gỏi cuốn tôm thịt",
            description = "Cuốn tươi mát, không cần bật bếp lâu.",
            cookingTime = 30,
            servings = 4,
            rating = 4.7,
            difficulty = Difficulty.MEDIUM,
            meals = setOf(MealType.SNACK, MealType.LUNCH),
            diets = setOf(DietTag.LOW_CALORIE),
            ingredients = ingredients(
                item("Tôm", "300 g", PROTEIN),
                item("Thịt heo", "200 g", PROTEIN),
                item("Bánh tráng", "1 xấp", OTHER),
                item("Bún tươi", "200 g", OTHER),
                item("Xà lách", "1 cây"),
            ),
            steps = steps(
                Triple("Luộc", "Luộc thịt và tôm đến khi chín, thái lát mỏng.", 15),
                Triple("Chuẩn bị", "Rửa sạch rau, nhúng nhanh bánh tráng qua nước.", null),
                Triple("Cuốn", "Xếp rau, bún, thịt, tôm lên bánh tráng rồi cuốn chặt tay.", null),
            ),
        ),
        Recipe(
            id = 10,
            name = "Mì Ý sốt cà chua",
            description = "Món mì Ý đơn giản, ai cũng làm được.",
            cookingTime = 25,
            servings = 2,
            rating = 4.6,
            difficulty = Difficulty.EASY,
            meals = setOf(MealType.LUNCH, MealType.DINNER),
            diets = setOf(DietTag.VEGETARIAN),
            cuisine = "Món Âu",
            ingredients = ingredients(
                item("Mì spaghetti", "200 g", OTHER),
                item("Cà chua", "4 quả"),
                item("Tỏi", "3 tép"),
                item("Dầu ô liu", "2 muỗng", staple = true),
                item("Muối", "1 nhúm", staple = true),
            ),
            steps = steps(
                Triple("Luộc mì", "Luộc mì trong nước sôi có muối theo thời gian trên bao bì.", 10),
                Triple("Làm sốt", "Phi tỏi với dầu ô liu, thêm cà chua băm và nấu sệt.", 10),
                Triple("Trộn", "Trộn mì với sốt, nêm vừa ăn rồi dọn ra đĩa.", null),
            ),
        ),
    )
}
