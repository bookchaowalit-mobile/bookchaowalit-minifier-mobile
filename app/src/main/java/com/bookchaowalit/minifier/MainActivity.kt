package com.bookchaowalit.minifier

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                MainApp()
            }
        }
    }
}

@Composable
fun MainApp() {
    var selectedTab by remember { mutableIntStateOf(0) }
    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(selected = selectedTab == 0, onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Home, "Home") }, label = { Text("Home") })
                NavigationBarItem(selected = selectedTab == 1, onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Explore, "Explore") }, label = { Text("Explore") })
                NavigationBarItem(selected = selectedTab == 2, onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Person, "Profile") }, label = { Text("Profile") })
            }
        }
    ) { padding ->
        when (selectedTab) {
            0 -> HomeScreen(Modifier.padding(padding))
            1 -> ExploreScreen(Modifier.padding(padding))
            2 -> ProfileScreen(Modifier.padding(padding))
        }
    }
}

@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center) {
        Text("Minifier", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text("Minifier — Mobile app (compose)", fontSize = 16.sp)
    }
}

@Composable
fun ExploreScreen(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Explore Minifier")
    }
}

@Composable
fun ProfileScreen(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center) {
        Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(80.dp))
        Spacer(Modifier.height(16.dp))
        Text("Chaowalit Greepoke", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text("bookchaowalit.com")
    }
}
