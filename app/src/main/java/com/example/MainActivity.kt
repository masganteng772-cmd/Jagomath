package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.auth.signOutUser
import com.example.data.LocalGuestMathRepository
import com.example.data.MathProgressRepository
import com.example.data.MathRepository
import com.example.ui.AppScreen
import com.example.ui.DashboardUiState
import com.example.ui.MainTab
import com.example.ui.MathAppViewModel
import com.example.ui.screens.AchievementsScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.PracticeHubScreen
import com.example.ui.screens.ProgressScreen
import com.example.ui.screens.QuizResultScreen
import com.example.ui.screens.QuizScreen
import com.example.ui.screens.TableGuideScreen
import com.example.ui.theme.EqMathTheme
import com.example.ui.theme.EqTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EqMathTheme {
                EqMathRoot()
            }
        }
    }
}

@Composable
private fun EqMathRoot() {
    var currentUser by remember { mutableStateOf(Firebase.auth.currentUser) }
    var isGuestMode by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            currentUser = auth.currentUser
            if (auth.currentUser != null) {
                isGuestMode = false
            }
        }
        Firebase.auth.addAuthStateListener(listener)
        onDispose { Firebase.auth.removeAuthStateListener(listener) }
    }

    val user = currentUser
    when {
        user != null -> {
            val uid = user.uid
            val defaultName = user.displayName?.trim().takeUnless { it.isNullOrBlank() } ?: "Juara Cilik"
            key(uid) {
                MathAppHost(
                    sessionKey = uid,
                    defaultUserName = defaultName,
                    isGuestMode = false,
                    onExitSession = { credentialManager, scope ->
                        signOutUser(credentialManager = credentialManager, scope = scope)
                    }
                )
            }
        }

        isGuestMode -> {
            key("guest_dev_session") {
                MathAppHost(
                    sessionKey = "guest_dev_session",
                    defaultUserName = "Tamu Cilik",
                    isGuestMode = true,
                    onExitSession = { _, _ ->
                        isGuestMode = false
                    }
                )
            }
        }

        else -> {
            AuthScreen(
                onAuthSuccess = {
                    isGuestMode = false
                    currentUser = Firebase.auth.currentUser
                },
                onContinueAsGuest = {
                    isGuestMode = true
                }
            )
        }
    }
}

private data class NavDestination(
    val tab: MainTab,
    val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

private val navDestinations = listOf(
    NavDestination(MainTab.HOME, R.string.nav_home, Icons.Filled.Home, Icons.Outlined.Home, "nav_tab_home"),
    NavDestination(MainTab.PRACTICE, R.string.nav_practice, Icons.Filled.Calculate, Icons.Outlined.Calculate, "nav_tab_practice"),
    NavDestination(MainTab.ACHIEVEMENTS, R.string.nav_achievements, Icons.Filled.EmojiEvents, Icons.Outlined.EmojiEvents, "nav_tab_achievements"),
    NavDestination(MainTab.PROGRESS, R.string.nav_progress, Icons.Filled.Insights, Icons.Outlined.Insights, "nav_tab_progress")
)

@Composable
private fun MathAppHost(
    sessionKey: String,
    defaultUserName: String,
    isGuestMode: Boolean,
    onExitSession: (CredentialManager, kotlinx.coroutines.CoroutineScope) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    val viewModel: MathAppViewModel = viewModel(
        key = sessionKey,
        factory = viewModelFactory {
            initializer {
                val repo: MathRepository = if (isGuestMode) {
                    LocalGuestMathRepository(guestUid = sessionKey)
                } else {
                    val app = checkNotNull(this[APPLICATION_KEY]) {
                        "APPLICATION_KEY missing from CreationExtras"
                    }
                    val databaseId = app.getString(R.string.firestore_database_id)
                    val db = FirebaseFirestore.getInstance(databaseId)
                    MathProgressRepository(db)
                }
                MathAppViewModel(
                    repository = repo,
                    currentUserId = sessionKey,
                    defaultUserName = defaultUserName
                )
            }
        }
    )

    val dashboardState by viewModel.dashboardState.collectAsStateWithLifecycle()
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val practiceConfig by viewModel.practiceConfig.collectAsStateWithLifecycle()
    val quizState by viewModel.quizState.collectAsStateWithLifecycle()
    val resultState by viewModel.resultState.collectAsStateWithLifecycle()
    val statusBanner by viewModel.statusBanner.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(statusBanner) {
        statusBanner?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearStatusBanner()
        }
    }

    when (val screen = currentScreen) {
        AppScreen.Quiz -> {
            QuizScreen(
                state = quizState,
                onSubmitAnswer = viewModel::submitAnswer,
                onKeypadDigit = viewModel::onKeypadDigit,
                onKeypadBackspace = viewModel::onKeypadBackspace,
                onKeypadClear = viewModel::onKeypadClear,
                onSubmitKeypad = viewModel::submitKeypadAnswer,
                onNextQuestion = viewModel::nextQuestionOrFinish,
                onToggleHint = viewModel::toggleVisualHint,
                onToggleInputMode = viewModel::toggleQuizInputMode,
                onExitQuiz = { viewModel.backToHome(MainTab.HOME) }
            )
        }

        AppScreen.QuizResult -> {
            QuizResultScreen(
                state = resultState,
                onPlayAgain = { viewModel.startQuiz(resultState.config) },
                onBackHome = { viewModel.backToHome(MainTab.HOME) }
            )
        }

        is AppScreen.TableGuide -> {
            TableGuideScreen(
                initialOperation = screen.initialOperation,
                onBack = { viewModel.backToHome(MainTab.HOME) },
                onPracticeTable = { op, tableNum ->
                    viewModel.startQuickMode(op, tableNum)
                }
            )
        }

        is AppScreen.Tabs -> {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                val useNavigationRail = maxWidth >= 600.dp

                Scaffold(
                    containerColor = MaterialTheme.colorScheme.background,
                    contentWindowInsets = WindowInsets.safeDrawing,
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                    bottomBar = {
                        if (!useNavigationRail) {
                            Column {
                                HorizontalDivider(
                                    thickness = 1.dp,
                                    color = EqTheme.colors.cardBorder
                                )
                                NavigationBar(
                                    windowInsets = WindowInsets.navigationBars,
                                    containerColor = MaterialTheme.colorScheme.surface
                                ) {
                                    navDestinations.forEach { dest ->
                                        val selected = screen.activeTab == dest.tab
                                        val label = stringResource(dest.labelRes)
                                        NavigationBarItem(
                                            selected = selected,
                                            onClick = { viewModel.selectTab(dest.tab) },
                                            icon = {
                                                Icon(
                                                    imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                                    contentDescription = null
                                                )
                                            },
                                            label = { Text(label) },
                                            colors = NavigationBarItemDefaults.colors(
                                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                            ),
                                            modifier = Modifier.testTag(dest.testTag)
                                        )
                                    }
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        if (useNavigationRail) {
                            NavigationRail(
                                containerColor = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.fillMaxHeight()
                            ) {
                                Spacer(modifier = Modifier.height(16.dp))
                                navDestinations.forEach { dest ->
                                    val selected = screen.activeTab == dest.tab
                                    val label = stringResource(dest.labelRes)
                                    NavigationRailItem(
                                        selected = selected,
                                        onClick = { viewModel.selectTab(dest.tab) },
                                        icon = {
                                            Icon(
                                                imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                                contentDescription = null
                                            )
                                        },
                                        label = { Text(label) },
                                        colors = NavigationRailItemDefaults.colors(
                                            selectedIconColor = MaterialTheme.colorScheme.primary,
                                            selectedTextColor = MaterialTheme.colorScheme.primary,
                                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                        ),
                                        modifier = Modifier.testTag(dest.testTag)
                                    )
                                }
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        ) {
                            when (val dash = dashboardState) {
                                DashboardUiState.Loading -> {
                                    LoadingDashboardState()
                                }

                                is DashboardUiState.Error -> {
                                    ErrorDashboardState(
                                        message = dash.message,
                                        onRetry = viewModel::ensureProfileInitialized
                                    )
                                }

                                is DashboardUiState.Success -> {
                                    when (screen.activeTab) {
                                        MainTab.HOME -> HomeScreen(
                                            profile = dash.profile,
                                            tableMasteries = dash.tableMasteries,
                                            unlockedAchievements = dash.unlockedAchievements,
                                            rewardDashboard = dash.rewardDashboard,
                                            onStartDailyChallenge = viewModel::startDailyChallenge,
                                            onStartOperation = { op ->
                                                viewModel.updatePracticeConfig { it.copy(operation = op, focusTable = 0) }
                                                viewModel.selectTab(MainTab.PRACTICE)
                                            },
                                            onOpenTableGuide = { viewModel.openTableGuide() },
                                            onOpenAchievements = { viewModel.selectTab(MainTab.ACHIEVEMENTS) }
                                        )

                                        MainTab.PRACTICE -> PracticeHubScreen(
                                            config = practiceConfig,
                                            tableMasteries = dash.tableMasteries,
                                            onUpdateConfig = viewModel::updatePracticeConfig,
                                            onStartQuiz = viewModel::startConfiguredQuiz
                                        )

                                        MainTab.ACHIEVEMENTS -> AchievementsScreen(
                                            profile = dash.profile,
                                            tableMasteries = dash.tableMasteries,
                                            unlockedAchievements = dash.unlockedAchievements
                                        )

                                        MainTab.PROGRESS -> ProgressScreen(
                                            profile = dash.profile,
                                            recentSessions = dash.recentSessions,
                                            tableMasteries = dash.tableMasteries,
                                            isGuestMode = isGuestMode,
                                            onPracticeTable = { op, tableNum ->
                                                viewModel.startQuickMode(op, tableNum)
                                            },
                                            onUpdateDisplayName = viewModel::updateStudentName,
                                            onSignOut = {
                                                onExitSession(credentialManager, scope)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingDashboardState() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .semantics(mergeDescendants = true) {
                liveRegion = LiveRegionMode.Polite
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 3.5.dp
            )
            Text(
                text = stringResource(R.string.loading_data),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ErrorDashboardState(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Assertive }
        ) {
            Icon(
                imageVector = Icons.Rounded.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(44.dp)
            )
            Text(
                text = stringResource(R.string.error_generic),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Button(
                onClick = onRetry,
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .testTag("retry_dashboard_button")
            ) {
                Text(stringResource(R.string.retry_button))
            }
        }
    }
}
