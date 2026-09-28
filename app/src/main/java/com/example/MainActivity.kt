package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.entity.TransactionEntity
import com.example.ui.screens.add.AddTransactionScreen
import com.example.ui.screens.analytics.AnalyticsScreen
import com.example.ui.screens.auth.LoginScreen
import com.example.ui.screens.auth.PinLockScreen
import com.example.ui.screens.auth.RegisterScreen
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.onboarding.OnboardingScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.screens.transactions.TransactionsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AuthState
import com.example.ui.viewmodel.AuthViewModel
import com.example.ui.viewmodel.MainViewModel

enum class ScreenState {
    AUTH_LOGIN,
    AUTH_REGISTER,
    AUTH_PIN_LOCK,
    ONBOARDING,
    MAIN_TABS,
    ADD_TRANSACTION,
    EDIT_TRANSACTION
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as CediWiseApplication

        setContent {
            val prefManager = app.preferenceManager
            val themeMode by prefManager.themeMode.collectAsState()
            val isDark = when (themeMode) {
                "DARK" -> true
                "LIGHT" -> false
                else -> isSystemInDarkTheme()
            }

            MyApplicationTheme(darkTheme = isDark) {
                // Request POST_NOTIFICATIONS on Android 13+
                val context = LocalContext.current
                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { /* Result handled gracefully */ }

                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        if (ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                }

                CediWiseApp(app = app)
            }
        }
    }
}

@Composable
fun CediWiseApp(app: CediWiseApplication) {
    val repository = app.repository
    val preferenceManager = app.preferenceManager

    val authViewModel: AuthViewModel = viewModel {
        AuthViewModel(repository, preferenceManager)
    }
    val mainViewModel: MainViewModel = viewModel {
        MainViewModel(repository, preferenceManager)
    }

    val authState by authViewModel.authState.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()

    var currentScreen by remember { mutableStateOf(ScreenState.AUTH_LOGIN) }
    var selectedTab by remember { mutableIntStateOf(0) }
    var editingTransaction by remember { mutableStateOf<TransactionEntity?>(null) }
    var initialAddExpense by remember { mutableStateOf(true) }

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Authenticated -> {
                if (preferenceManager.isFirstLaunch()) {
                    currentScreen = ScreenState.ONBOARDING
                } else {
                    currentScreen = ScreenState.MAIN_TABS
                    mainViewModel.loadUserData()
                }
            }
            is AuthState.NeedsPin -> {
                currentScreen = ScreenState.AUTH_PIN_LOCK
            }
            is AuthState.Idle, is AuthState.Error -> {
                if (currentScreen != ScreenState.AUTH_REGISTER) {
                    currentScreen = ScreenState.AUTH_LOGIN
                }
            }
            AuthState.Loading -> { /* Keep current */ }
        }
    }

    when (currentScreen) {
        ScreenState.AUTH_LOGIN -> {
            LoginScreen(
                authViewModel = authViewModel,
                onNavigateToRegister = { currentScreen = ScreenState.AUTH_REGISTER },
                onLoginSuccess = {
                    if (preferenceManager.isFirstLaunch()) {
                        currentScreen = ScreenState.ONBOARDING
                    } else {
                        currentScreen = ScreenState.MAIN_TABS
                        mainViewModel.loadUserData()
                    }
                }
            )
        }

        ScreenState.AUTH_REGISTER -> {
            BackHandler { currentScreen = ScreenState.AUTH_LOGIN }
            RegisterScreen(
                authViewModel = authViewModel,
                onNavigateBack = { currentScreen = ScreenState.AUTH_LOGIN },
                onRegisterSuccess = {
                    currentScreen = ScreenState.ONBOARDING
                }
            )
        }

        ScreenState.AUTH_PIN_LOCK -> {
            currentUser?.let { user ->
                PinLockScreen(
                    user = user,
                    onPinSuccess = {
                        currentScreen = ScreenState.MAIN_TABS
                        mainViewModel.loadUserData()
                    },
                    onLogout = {
                        authViewModel.logout()
                        currentScreen = ScreenState.AUTH_LOGIN
                    }
                )
            }
        }

        ScreenState.ONBOARDING -> {
            currentUser?.let { user ->
                OnboardingScreen(
                    user = user,
                    repository = repository,
                    preferenceManager = preferenceManager,
                    onFinishOnboarding = {
                        currentScreen = ScreenState.MAIN_TABS
                        mainViewModel.loadUserData()
                    }
                )
            }
        }

        ScreenState.ADD_TRANSACTION -> {
            BackHandler { currentScreen = ScreenState.MAIN_TABS }
            AddTransactionScreen(
                mainViewModel = mainViewModel,
                initialIsExpense = initialAddExpense,
                existingTransaction = null,
                onNavigateBack = { currentScreen = ScreenState.MAIN_TABS }
            )
        }

        ScreenState.EDIT_TRANSACTION -> {
            BackHandler {
                editingTransaction = null
                currentScreen = ScreenState.MAIN_TABS
            }
            AddTransactionScreen(
                mainViewModel = mainViewModel,
                initialIsExpense = editingTransaction?.type == "EXPENSE",
                existingTransaction = editingTransaction,
                onNavigateBack = {
                    editingTransaction = null
                    currentScreen = ScreenState.MAIN_TABS
                }
            )
        }

        ScreenState.MAIN_TABS -> {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                bottomBar = {
                    NavigationBar(modifier = Modifier.testTag("bottom_nav_bar")) {
                        NavigationBarItem(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            icon = {
                                Icon(
                                    if (selectedTab == 0) Icons.Default.Home else Icons.Outlined.Home,
                                    contentDescription = "Home"
                                )
                            },
                            label = { Text("Home") },
                            modifier = Modifier.testTag("nav_home")
                        )
                        NavigationBarItem(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            icon = {
                                Icon(
                                    if (selectedTab == 1) Icons.Default.ReceiptLong else Icons.Outlined.ReceiptLong,
                                    contentDescription = "Transactions"
                                )
                            },
                            label = { Text("Records") },
                            modifier = Modifier.testTag("nav_transactions")
                        )
                        NavigationBarItem(
                            selected = selectedTab == 2,
                            onClick = { selectedTab = 2 },
                            icon = {
                                Icon(
                                    if (selectedTab == 2) Icons.Default.Analytics else Icons.Outlined.Analytics,
                                    contentDescription = "Analytics"
                                )
                            },
                            label = { Text("Analytics") },
                            modifier = Modifier.testTag("nav_analytics")
                        )
                        NavigationBarItem(
                            selected = selectedTab == 3,
                            onClick = { selectedTab = 3 },
                            icon = {
                                Icon(
                                    if (selectedTab == 3) Icons.Default.Settings else Icons.Outlined.Settings,
                                    contentDescription = "Settings"
                                )
                            },
                            label = { Text("Settings") },
                            modifier = Modifier.testTag("nav_settings")
                        )
                    }
                },
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = {
                            initialAddExpense = true
                            currentScreen = ScreenState.ADD_TRANSACTION
                        },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.testTag("fab_add_transaction")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Transaction")
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    when (selectedTab) {
                        0 -> HomeScreen(
                            mainViewModel = mainViewModel,
                            onNavigateToAdd = { isExpense ->
                                initialAddExpense = isExpense
                                currentScreen = ScreenState.ADD_TRANSACTION
                            },
                            onNavigateToTransactions = { selectedTab = 1 },
                            onNavigateToSettings = { selectedTab = 3 }
                        )
                        1 -> TransactionsScreen(
                            mainViewModel = mainViewModel,
                            onEditTransaction = { tx ->
                                editingTransaction = tx
                                currentScreen = ScreenState.EDIT_TRANSACTION
                            }
                        )
                        2 -> AnalyticsScreen(mainViewModel = mainViewModel)
                        3 -> SettingsScreen(
                            mainViewModel = mainViewModel,
                            authViewModel = authViewModel,
                            preferenceManager = preferenceManager,
                            onLogout = {
                                currentScreen = ScreenState.AUTH_LOGIN
                            }
                        )
                    }
                }
            }
        }
    }
}
