package com.example.eatstoday.navigation

import android.net.Uri

object AppRoute {
    const val WELCOME = "welcome"
    const val SIGN_IN = "sign_in"
    const val REGISTER = "register"
    const val PERMISSIONS_INTRO = "permissions_intro"
    const val PERMISSIONS = "permissions"
    const val PREFERENCES = "preferences"
    const val FORGOT = "forgot"
    const val PASSWORD_DONE = "password_done"

    const val HOME = "home"
    const val SEARCH = "search"
    const val RECIPE_DETAIL = "recipe_detail/{recipeId}"
    const val COOKING = "cooking/{recipeId}"
    const val COOKING_DONE = "cooking_done/{recipeId}"
    const val SAVED = "saved"
    const val FAVORITES = "favorites"

    const val PANTRY = "pantry"
    const val INGREDIENTS = "ingredients"
    const val INGREDIENT_FORM = "ingredient_form?itemId={itemId}"
    const val SHOPPING = "shopping"

    const val FEED = "feed"
    const val REVIEW = "review/{postId}"
    const val CAMERA = "camera/{recipeId}"
    const val SHARE = "share/{recipeId}?path={path}"
    const val FRIENDS = "friends"
    const val ADD_FRIEND = "add_friend"
    const val REQUESTS = "requests"
    const val FRIEND_PROFILE = "friend_profile/{friendId}"

    const val PROFILE = "profile"
    const val EDIT_PROFILE = "edit_profile"
    const val HISTORY = "history"
    const val SETTINGS = "settings"
    const val NOTIFICATIONS = "notifications"
    const val PREMIUM = "premium"
    const val PREMIUM_SUCCESS = "premium_success"

    fun recipeDetail(id: Int) = "recipe_detail/$id"
    fun cooking(id: Int) = "cooking/$id"
    fun cookingDone(id: Int) = "cooking_done/$id"
    fun ingredientForm(itemId: Long = -1L) = "ingredient_form?itemId=$itemId"
    fun review(postId: String) = "review/${Uri.encode(postId)}"
    /** recipeId = 0 nghĩa là món không gắn với công thức nào. */
    fun camera(recipeId: Int = 0) = "camera/$recipeId"
    fun share(recipeId: Int, path: String) = "share/$recipeId?path=${Uri.encode(path)}"
    fun friendProfile(uid: String) = "friend_profile/${Uri.encode(uid)}"
}
