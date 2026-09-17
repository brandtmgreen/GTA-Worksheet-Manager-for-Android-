package com.example

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.*
import com.example.ui.theme.MyApplicationTheme

class WorksheetViewModelFactory(
    private val application: Application,
    private val settingsManager: SettingsManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WorksheetViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WorksheetViewModel(application, settingsManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class SettingsViewModelFactory(private val settingsManager: SettingsManager) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(settingsManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val settingsManager = SettingsManager(applicationContext)

        setContent {
            MyApplicationTheme {
                val navController = rememberNavController()

                val worksheetViewModel: WorksheetViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                    factory = WorksheetViewModelFactory(application, settingsManager)
                )

                val settingsViewModel: SettingsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                    factory = SettingsViewModelFactory(settingsManager)
                )

                NavHost(
                    navController = navController,
                    startDestination = "home",
                    modifier = Modifier.fillMaxSize()
                ) {
                    composable("home") {
                        HomeScreen(
                            viewModel = worksheetViewModel,
                            onNavigateToOcr = { navController.navigate("ocr_scan") },
                            onNavigateToTemplates = { navController.navigate("templates") },
                            onNavigateToSettings = { navController.navigate("settings") },
                            onNavigateToEditor = { navController.navigate("editor") },
                            onNavigateToDetail = { id -> navController.navigate("detail/$id") }
                        )
                    }

                    composable("ocr_scan") {
                        OcrScanScreen(
                            viewModel = worksheetViewModel,
                            onNavigateBack = { navController.popBackStack() },
                            onPopulateWorksheet = {
                                navController.navigate("editor") {
                                    popUpTo("ocr_scan") { inclusive = true }
                                }
                            }
                        )
                    }

                    composable("templates") {
                        TemplateLibraryScreen(
                            onNavigateBack = { navController.popBackStack() },
                            onTemplateSelected = { template ->
                                worksheetViewModel.startNewWorksheet(template)
                                navController.navigate("editor")
                            },
                            onScanForTemplate = { template ->
                                navController.navigate("ocr_scan")
                            }
                        )
                    }

                    composable("editor") {
                        WorksheetEditorScreen(
                            viewModel = worksheetViewModel,
                            onNavigateBack = { navController.popBackStack() },
                            onNavigateToOcr = { navController.navigate("ocr_scan") },
                            onNavigateToDetail = { id ->
                                navController.navigate("detail/$id") {
                                    popUpTo("editor") { inclusive = true }
                                }
                            }
                        )
                    }

                    composable(
                        route = "detail/{id}",
                        arguments = listOf(navArgument("id") { type = NavType.IntType })
                    ) { backStackEntry ->
                        val sheetId = backStackEntry.arguments?.getInt("id") ?: 0
                        WorksheetDetailScreen(
                            worksheetId = sheetId,
                            viewModel = worksheetViewModel,
                            settingsViewModel = settingsViewModel,
                            onNavigateBack = { navController.popBackStack() },
                            onNavigateToEdit = { sheet ->
                                worksheetViewModel.loadWorksheetForEditing(sheet)
                                navController.navigate("editor")
                            }
                        )
                    }

                    composable("settings") {
                        SettingsScreen(
                            viewModel = settingsViewModel,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}
