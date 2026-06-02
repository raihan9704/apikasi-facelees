package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.GalleryScreen
import com.example.ui.screens.LandingScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.StudioScreen
import com.example.ui.theme.*
import com.example.viewmodel.StudioViewModel
import com.example.viewmodel.StudioViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                // Initialize clean repository context & Viewmodel
                val viewModel: StudioViewModel = viewModel(
                    factory = StudioViewModelFactory(application)
                )

                val currentScreen by viewModel.currentScreen.collectAsState()

                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val isWideScreen = maxWidth > 600.dp

                    Scaffold(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(NavyBackground),
                        bottomBar = {
                            if (!isWideScreen) {
                                BottomNavigationBarLayout(
                                    currentScreen = currentScreen,
                                    onNavigate = { viewModel.navigateTo(it) },
                                    modifier = Modifier.testTag("mobile_bottom_nav")
                                )
                            }
                        }
                    ) { innerPadding ->
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            if (isWideScreen) {
                                NavigationSidebarRailLayout(
                                    currentScreen = currentScreen,
                                    onNavigate = { viewModel.navigateTo(it) },
                                    modifier = Modifier.testTag("tablet_nav_rail")
                                )
                            }

                            // Dynamic smooth transition crossfade mapping screens
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .windowInsetsPadding(WindowInsets.statusBars)
                            ) {
                                when (currentScreen) {
                                    "landing" -> LandingScreen(viewModel = viewModel)
                                    "studio" -> StudioScreen(viewModel = viewModel)
                                    "gallery" -> GalleryScreen(viewModel = viewModel)
                                    "settings" -> SettingsScreen(viewModel = viewModel)
                                    else -> LandingScreen(viewModel = viewModel)
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
fun BottomNavigationBarLayout(
    currentScreen: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier.height(64.dp),
        containerColor = DeepCardNavy,
        tonalElevation = 8.dp
    ) {
        val navItems = listOf(
            NavItem("landing", Icons.Filled.Home, "Home"),
            NavItem("studio", Icons.Filled.AutoAwesome, "AI Studio"),
            NavItem("gallery", Icons.Filled.PhotoLibrary, "Gallery"),
            NavItem("settings", Icons.Filled.Settings, "Settings")
        )

        navItems.forEach { item ->
            val isSelected = currentScreen == item.screenId
            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(item.screenId) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        tint = if (isSelected) TechOrange else TextGray,
                        modifier = Modifier.size(20.dp)
                    )
                },
                label = {
                    Text(
                        item.label,
                        color = if (isSelected) TextWhite else TextGray,
                        fontSize = 9.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = ActiveCardNavy
                )
            )
        }
    }
}

@Composable
fun NavigationSidebarRailLayout(
    currentScreen: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationRail(
        modifier = modifier.width(76.dp),
        containerColor = DeepCardNavy,
        header = {
            Icon(
                Icons.Filled.AutoAwesome,
                contentDescription = "FaceLess AI Logo",
                tint = TechOrange,
                modifier = Modifier
                    .size(36.dp)
                    .padding(vertical = 12.dp)
            )
        }
    ) {
        val navItems = listOf(
            NavItem("landing", Icons.Filled.Home, "Home"),
            NavItem("studio", Icons.Filled.AutoAwesome, "AI Studio"),
            NavItem("gallery", Icons.Filled.PhotoLibrary, "Gallery"),
            NavItem("settings", Icons.Filled.Settings, "Settings")
        )

        navItems.forEach { item ->
            val isSelected = currentScreen == item.screenId
            NavigationRailItem(
                selected = isSelected,
                onClick = { onNavigate(item.screenId) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        tint = if (isSelected) TechOrange else TextGray,
                        modifier = Modifier.size(22.dp)
                    )
                },
                label = {
                    Text(
                        item.label,
                        color = if (isSelected) TextWhite else TextGray,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationRailItemDefaults.colors(
                    indicatorColor = ActiveCardNavy
                )
            )
        }
    }
}

data class NavItem(
    val screenId: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val label: String
)
