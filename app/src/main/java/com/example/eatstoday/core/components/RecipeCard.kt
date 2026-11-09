package com.example.eatstoday.core.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.eatstoday.data.model.Recipe
import com.example.eatstoday.ui.theme.Dimens

fun Recipe.summary(): String = "$cookingTime phút · ${difficulty.label}"

@Composable
fun RecipeCard(
    recipe: Recipe,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    imageHeight: Dp = 110.dp,
) {
    EatsCard(onClick = onClick, modifier = modifier, contentPadding = 8.dp) {
        FoodImage(
            label = if (recipe.image != null) recipe.name else if (recipe.isPremium) "Ảnh món · Premium" else "Ảnh món",
            modifier = Modifier.fillMaxWidth().height(imageHeight),
            imageName = recipe.image,
        )
        Text(
            recipe.name,
            style = MaterialTheme.typography.titleSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            recipe.summary(),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** Lưới hai cột đặt được bên trong Column cuộn. */
@Composable
fun RecipeGrid(recipes: List<Recipe>, onRecipeClick: (Recipe) -> Unit, imageHeight: Dp = 110.dp) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        recipes.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                row.forEach { recipe ->
                    RecipeCard(
                        recipe = recipe,
                        onClick = { onRecipeClick(recipe) },
                        modifier = Modifier.weight(1f),
                        imageHeight = imageHeight,
                    )
                }
                if (row.size == 1) androidx.compose.foundation.layout.Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun RecipeRating(
    rating: Int,
    modifier: Modifier = Modifier,
    starSize: Dp = 18.dp,
    onRate: ((Int) -> Unit)? = null,
) {
    Row(
        modifier = modifier.semantics { contentDescription = "$rating trên 5 sao" },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        (1..5).forEach { star ->
            val iconModifier = if (onRate != null) {
                Modifier.size(Dimens.TouchTarget).clickable { onRate(star) }
            } else {
                Modifier.size(starSize)
            }
            Icon(
                imageVector = if (star <= rating) Icons.Filled.Star else Icons.Outlined.StarBorder,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = iconModifier,
            )
        }
    }
}
