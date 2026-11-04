package com.example.eatstoday.navigation

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.eatstoday.core.components.BackTopBar
import com.example.eatstoday.core.components.LocalSnackbar
import com.example.eatstoday.data.local.RecipeSampleData
import com.example.eatstoday.data.repository.UserRepository
import com.example.eatstoday.feature.auth.presentation.viewmodel.AuthViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.eatstoday.core.utils.AppViewModelProvider
import com.example.eatstoday.feature.auth.presentation.screen.ForgotPasswordScreen
import com.example.eatstoday.feature.auth.presentation.screen.NewPasswordScreen
import com.example.eatstoday.feature.auth.presentation.screen.PreferencesScreen
import com.example.eatstoday.feature.auth.presentation.screen.RegisterScreen
import com.example.eatstoday.feature.auth.presentation.screen.ResultScreen
import com.example.eatstoday.feature.auth.presentation.screen.SignInScreen
import com.example.eatstoday.feature.auth.presentation.screen.VerifyCodeScreen
import com.example.eatstoday.feature.auth.presentation.screen.WelcomeScreen
import com.example.eatstoday.feature.pantry.presentation.screen.FridgeScreen
import com.example.eatstoday.feature.pantry.presentation.screen.IngredientFormScreen
import com.example.eatstoday.feature.pantry.presentation.screen.IngredientListScreen
import com.example.eatstoday.feature.premium.presentation.screen.PaywallScreen
import com.example.eatstoday.feature.profile.presentation.screen.CookHistoryScreen
import com.example.eatstoday.feature.profile.presentation.screen.EditProfileScreen
import com.example.eatstoday.feature.profile.presentation.screen.NotificationsScreen
import com.example.eatstoday.feature.profile.presentation.screen.PermissionsScreen
import com.example.eatstoday.feature.profile.presentation.screen.ProfileScreen
import com.example.eatstoday.feature.profile.presentation.screen.SettingsScreen
import com.example.eatstoday.feature.recipe.presentation.screen.CookingCompletedScreen
import com.example.eatstoday.feature.recipe.presentation.screen.CookingScreen
import com.example.eatstoday.feature.recipe.presentation.screen.FavoritesScreen
import com.example.eatstoday.feature.recipe.presentation.screen.HomeScreen
import com.example.eatstoday.feature.recipe.presentation.screen.RecipeDetailScreen
import com.example.eatstoday.feature.recipe.presentation.screen.SavedScreen
import com.example.eatstoday.feature.recipe.presentation.screen.SearchScreen
import com.example.eatstoday.feature.shopping.presentation.screen.ShoppingScreen
import com.example.eatstoday.feature.social.presentation.screen.AddFriendScreen
import com.example.eatstoday.feature.social.presentation.screen.CameraScreen
import com.example.eatstoday.feature.social.presentation.screen.FeedScreen
import com.example.eatstoday.feature.social.presentation.screen.FriendListScreen
import com.example.eatstoday.feature.social.presentation.screen.FriendProfileScreen
import com.example.eatstoday.feature.social.presentation.screen.RequestsScreen
import com.example.eatstoday.feature.social.presentation.screen.ReviewScreen
import com.example.eatstoday.feature.social.presentation.screen.ShareScreen
import kotlinx.coroutines.flow.first

/** Mở một tab và giữ trạng thái của tab đó như quy ước Bottom Navigation. */
private fun NavHostController.navigateToTab(route: String) {
    navigate(route) {
        popUpTo(AppRoute.HOME) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

/**
 * Kết thúc một luồng (chia sẻ xong, nâng cấp xong) rồi mở tab. Không lưu lại các màn hình của luồng,
 * nếu không lần sau mở tab sẽ quay lại đúng màn hình đã hoàn tất.
 */
private fun NavHostController.finishFlowTo(route: String) {
    navigate(route) {
        popUpTo(AppRoute.HOME)
        launchSingleTop = true
    }
}

/** Xóa toàn bộ lịch sử rồi mở màn hình mới, dùng khi đăng nhập và đăng xuất. */
private fun NavHostController.resetTo(route: String) {
    navigate(route) {
        popUpTo(graph.id) { inclusive = true }
        launchSingleTop = true
    }
}

private val RoutesWithoutBack = setOf(
    AppRoute.WELCOME,
    AppRoute.HOME,
    AppRoute.PASSWORD_DONE,
    AppRoute.PREMIUM_SUCCESS,
    AppRoute.PERMISSIONS_INTRO,
)

private fun intArg(name: String) = navArgument(name) { type = NavType.IntType }
private fun longArg(name: String) = navArgument(name) { type = NavType.LongType }
private fun NavBackStackEntry.recipeIdArg(): Int = arguments?.getInt("recipeId") ?: 0

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AppNavigation(userRepository: UserRepository) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    var startDestination by rememberSaveable { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) {
        if (startDestination == null) {
            startDestination = if (userRepository.isLoggedIn.first()) AppRoute.HOME else AppRoute.WELCOME
        }
    }

    Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
        val start = startDestination ?: return@Surface
        val backStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = backStackEntry?.destination?.route
        val currentTab = tabForRoute(currentRoute)
        val showBack = currentRoute !in RoutesWithoutBack && navController.previousBackStackEntry != null
        val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
        CompositionLocalProvider(LocalSnackbar provides snackbarHostState) {
            Scaffold(
                containerColor = MaterialTheme.colorScheme.background,
                snackbarHost = { SnackbarHost(snackbarHostState) },
                topBar = {
                    // Đi qua dispatcher để màn hình có BackHandler (như chế độ nấu) vẫn được hỏi xác nhận.
                    if (showBack) BackTopBar(onBack = { backDispatcher?.onBackPressed() })
                },
                bottomBar = {
                    if (currentTab != null) {
                        EatsBottomBar(currentTab) { navController.navigateToTab(it.route) }
                    }
                },
            ) { innerPadding ->
                NavHost(
                    navController = navController,
                    startDestination = start,
                    modifier = Modifier.padding(innerPadding).consumeWindowInsets(innerPadding),
                ) {
                    authGraph(navController)
                    recipeGraph(navController)
                    pantryGraph(navController)
                    socialGraph(navController)
                    profileGraph(navController)
                }
            }
        }
    }
}

private fun NavGraphBuilder.authGraph(nav: NavHostController) {
    composable(AppRoute.WELCOME) {
        WelcomeScreen(
            onStart = { nav.navigate(AppRoute.PREFERENCES) },
            onSignIn = { nav.navigate(AppRoute.SIGN_IN) },
            onRegister = { nav.navigate(AppRoute.REGISTER) },
        )
    }
    composable(AppRoute.SIGN_IN) {
        val authViewModel: AuthViewModel = viewModel(factory = AppViewModelProvider.Factory)
        SignInScreen(
            onBack = { nav.popBackStack() },
            onSignedIn = {
                authViewModel.checkPermissionIntro { firstTime ->
                    nav.resetTo(if (firstTime) AppRoute.PERMISSIONS_INTRO else AppRoute.HOME)
                }
            },
            onForgot = { nav.navigate(AppRoute.FORGOT) },
            onRegister = { nav.navigate(AppRoute.REGISTER) },
            viewModel = authViewModel,
        )
    }
    composable(AppRoute.REGISTER) {
        RegisterScreen(
            onRegistered = { nav.resetTo(AppRoute.PREFERENCES) },
            onSignIn = {
                nav.navigate(AppRoute.SIGN_IN) {
                    popUpTo(AppRoute.REGISTER) { inclusive = true }
                }
            },
        )
    }
    composable(AppRoute.PREFERENCES) {
        PreferencesScreen(onDone = { nav.resetTo(AppRoute.PERMISSIONS_INTRO) })
    }
    composable(AppRoute.PERMISSIONS_INTRO) {
        val authViewModel: AuthViewModel = viewModel(factory = AppViewModelProvider.Factory)
        PermissionsScreen(
            isOnboarding = true,
            onDone = { authViewModel.markPermissionsSeen { nav.resetTo(AppRoute.HOME) } },
        )
    }
    composable(AppRoute.PERMISSIONS) {
        PermissionsScreen(isOnboarding = false, onDone = { nav.popBackStack() })
    }
    composable(AppRoute.FORGOT) {
        ForgotPasswordScreen(
            onBack = { nav.popBackStack() },
            onCodeSent = { email -> nav.navigate(AppRoute.verify(email)) },
        )
    }
    composable(AppRoute.VERIFY, listOf(navArgument("email") { type = NavType.StringType })) { entry ->
        val email = entry.arguments?.getString("email").orEmpty()
        VerifyCodeScreen(
            email = email,
            onBack = { nav.popBackStack() },
            onVerified = { nav.navigate(AppRoute.newPassword(email)) },
        )
    }
    composable(AppRoute.NEW_PASSWORD, listOf(navArgument("email") { type = NavType.StringType })) { entry ->
        NewPasswordScreen(
            email = entry.arguments?.getString("email").orEmpty(),
            onDone = {
                nav.navigate(AppRoute.PASSWORD_DONE) { popUpTo(AppRoute.SIGN_IN) }
            },
        )
    }
    composable(AppRoute.PASSWORD_DONE) {
        ResultScreen(
            title = "Đã đổi mật khẩu",
            message = "Bạn có thể đăng nhập bằng mật khẩu mới ngay bây giờ.",
            actionLabel = "Về đăng nhập",
            onAction = { nav.popBackStack(AppRoute.SIGN_IN, inclusive = false) },
        )
    }
}

private fun NavGraphBuilder.recipeGraph(nav: NavHostController) {
    val openRecipe: (Int) -> Unit = { id -> nav.navigate(AppRoute.recipeDetail(id)) }
    composable(AppRoute.HOME) {
        HomeScreen(
            onRecipeClick = openRecipe,
            onSearch = { nav.navigateToTab(AppRoute.SEARCH) },
            onFridge = { nav.navigateToTab(AppRoute.PANTRY) },
            onShopping = { nav.navigate(AppRoute.SHOPPING) },
            onSaved = { nav.navigate(AppRoute.SAVED) },
            onNotifications = { nav.navigate(AppRoute.NOTIFICATIONS) },
            onProfile = { nav.navigateToTab(AppRoute.PROFILE) },
        )
    }
    composable(AppRoute.SEARCH) { SearchScreen(onRecipeClick = openRecipe) }
    composable(AppRoute.RECIPE_DETAIL, listOf(intArg("recipeId"))) {
        RecipeDetailScreen(
            onBack = { nav.popBackStack() },
            onStartCooking = { id -> nav.navigate(AppRoute.cooking(id)) },
            onUpgrade = { nav.navigate(AppRoute.PREMIUM) },
        )
    }
    composable(AppRoute.COOKING, listOf(intArg("recipeId"))) {
        CookingScreen(
            onExit = { nav.popBackStack() },
            onCompleted = { id ->
                nav.navigate(AppRoute.cookingDone(id)) { popUpTo(AppRoute.COOKING) { inclusive = true } }
            },
        )
    }
    composable(AppRoute.COOKING_DONE, listOf(intArg("recipeId"))) { entry ->
        val id = entry.recipeIdArg()
        CookingCompletedScreen(
            recipeName = RecipeSampleData.recipes.firstOrNull { it.id == id }?.name.orEmpty(),
            onTakePhoto = { nav.navigate(AppRoute.camera(id)) },
            onHome = { nav.finishFlowTo(AppRoute.HOME) },
        )
    }
    composable(AppRoute.SAVED) { SavedScreen(onRecipeClick = openRecipe) }
    composable(AppRoute.FAVORITES) { FavoritesScreen(onRecipeClick = openRecipe) }
}

private fun NavGraphBuilder.pantryGraph(nav: NavHostController) {
    composable(AppRoute.PANTRY) {
        FridgeScreen(
            onRecipeClick = { id -> nav.navigate(AppRoute.recipeDetail(id)) },
            onManageIngredients = { nav.navigate(AppRoute.INGREDIENTS) },
            onAddIngredient = { nav.navigate(AppRoute.ingredientForm()) },
            onShopping = { nav.navigate(AppRoute.SHOPPING) },
            onSeeAllRecipes = { nav.navigateToTab(AppRoute.SEARCH) },
        )
    }
    composable(AppRoute.INGREDIENTS) {
        IngredientListScreen(
            onBack = { nav.popBackStack() },
            onAdd = { nav.navigate(AppRoute.ingredientForm()) },
            onEdit = { id -> nav.navigate(AppRoute.ingredientForm(id)) },
        )
    }
    composable(
        AppRoute.INGREDIENT_FORM,
        listOf(
            navArgument("itemId") {
                type = NavType.LongType
                defaultValue = -1L
            },
        ),
    ) {
        IngredientFormScreen(onBack = { nav.popBackStack() })
    }
    composable(AppRoute.SHOPPING) {
        ShoppingScreen(onFindRecipes = { nav.navigateToTab(AppRoute.SEARCH) })
    }
}

private fun NavGraphBuilder.socialGraph(nav: NavHostController) {
    composable(AppRoute.FEED) {
        FeedScreen(
            onPostClick = { id -> nav.navigate(AppRoute.review(id)) },
            onFriends = { nav.navigate(AppRoute.FRIENDS) },
            onCamera = { nav.navigate(AppRoute.camera()) },
            onSaved = { nav.navigate(AppRoute.SAVED) },
        )
    }
    composable(AppRoute.REVIEW, listOf(longArg("postId"))) {
        ReviewScreen(onBack = { nav.popBackStack() })
    }
    composable(AppRoute.CAMERA, listOf(intArg("recipeId"))) { entry ->
        val id = entry.recipeIdArg()
        CameraScreen(
            onClose = { nav.popBackStack() },
            onPhotoReady = { path -> nav.navigate(AppRoute.share(id, path)) },
        )
    }
    composable(
        AppRoute.SHARE,
        listOf(
            intArg("recipeId"),
            navArgument("path") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            },
        ),
    ) { entry ->
        ShareScreen(
            photoPath = entry.arguments?.getString("path"),
            onRetake = { nav.popBackStack() },
            onPublished = { nav.finishFlowTo(AppRoute.FEED) },
        )
    }
    composable(AppRoute.FRIENDS) {
        FriendListScreen(
            onBack = { nav.popBackStack() },
            onFriendClick = { id -> nav.navigate(AppRoute.friendProfile(id)) },
            onAdd = { nav.navigate(AppRoute.ADD_FRIEND) },
            onRequests = { nav.navigate(AppRoute.REQUESTS) },
        )
    }
    composable(AppRoute.ADD_FRIEND) { AddFriendScreen(onBack = { nav.popBackStack() }) }
    composable(AppRoute.REQUESTS) {
        RequestsScreen(
            onBack = { nav.popBackStack() },
            onFriendClick = { id -> nav.navigate(AppRoute.friendProfile(id)) },
        )
    }
    composable(AppRoute.FRIEND_PROFILE, listOf(longArg("friendId"))) {
        FriendProfileScreen(
            onBack = { nav.popBackStack() },
            onSendDish = { nav.navigate(AppRoute.camera()) },
        )
    }
}

private fun NavGraphBuilder.profileGraph(nav: NavHostController) {
    composable(AppRoute.PROFILE) {
        ProfileScreen(
            onUpgrade = { nav.navigate(AppRoute.PREMIUM) },
            onEdit = { nav.navigate(AppRoute.EDIT_PROFILE) },
            onFriends = { nav.navigate(AppRoute.FRIENDS) },
            onFavorites = { nav.navigate(AppRoute.FAVORITES) },
            onShopping = { nav.navigate(AppRoute.SHOPPING) },
            onHistory = { nav.navigate(AppRoute.HISTORY) },
            onSettings = { nav.navigate(AppRoute.SETTINGS) },
            onLoggedOut = { nav.resetTo(AppRoute.WELCOME) },
        )
    }
    composable(AppRoute.EDIT_PROFILE) { EditProfileScreen(onBack = { nav.popBackStack() }) }
    composable(AppRoute.HISTORY) {
        CookHistoryScreen(
            onBack = { nav.popBackStack() },
            onRecipeClick = { id -> nav.navigate(AppRoute.recipeDetail(id)) },
        )
    }
    composable(AppRoute.SETTINGS) {
        SettingsScreen(
            onPermissions = { nav.navigate(AppRoute.PERMISSIONS) },
            onLoggedOut = { nav.resetTo(AppRoute.WELCOME) },
        )
    }
    composable(AppRoute.NOTIFICATIONS) { NotificationsScreen(onBack = { nav.popBackStack() }) }
    composable(AppRoute.PREMIUM) {
        PaywallScreen(
            onClose = { nav.popBackStack() },
            onActivated = {
                nav.navigate(AppRoute.PREMIUM_SUCCESS) { popUpTo(AppRoute.PREMIUM) { inclusive = true } }
            },
        )
    }
    composable(AppRoute.PREMIUM_SUCCESS) {
        ResultScreen(
            title = "Chào mừng Premium",
            message = "Công thức đặc biệt đã mở khóa. Đây là trạng thái mô phỏng, chưa có giao dịch thật.",
            actionLabel = "Khám phá công thức Premium",
            onAction = { nav.finishFlowTo(AppRoute.SEARCH) },
        )
    }
}
