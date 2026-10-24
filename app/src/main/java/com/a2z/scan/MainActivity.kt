package com.a2z.scan

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.a2z.scan.ui.A2ZScanTheme
import com.a2z.scan.ui.CameraPreview
import com.a2z.scan.ui.DocumentsScreen
import com.a2z.scan.ui.OcrScreen
import com.a2z.scan.ui.PdfToolsScreen
import com.a2z.scan.ui.ScanDashboard
import com.a2z.scan.ui.ScanScreen
import com.a2z.scan.ui.SettingsScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            A2ZScanTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = "dashboard") {
                        composable("dashboard") {
                            ScanDashboard { route ->
                                navController.navigate(route)
                            }
                        }
                        composable("pdf_tools") {
                            PdfToolsScreen {
                                navController.popBackStack()
                            }
                        }
                        composable("scan") {
                            ScanScreen(
                                onBack = { navController.popBackStack() },
                                onNavigateToFiles = { navController.navigate("files") }
                            )
                        }
                        composable("ocr") {
                            OcrScreen {
                                navController.popBackStack()
                            }
                        }
                        composable("files") {
                            DocumentsScreen {
                                navController.popBackStack()
                            }
                        }
                        composable("camera") {
                            CameraPreview(
                                onImageCaptured = { _ ->
                                    navController.navigate("scan")
                                },
                                onError = { _ ->
                                    navController.popBackStack()
                                },
                                onClose = {
                                    navController.popBackStack()
                                }
                            )
                        }
                        composable("settings") {
                            SettingsScreen {
                                navController.popBackStack()
                            }
                        }
                    }
                }
            }
        }
    }
}
