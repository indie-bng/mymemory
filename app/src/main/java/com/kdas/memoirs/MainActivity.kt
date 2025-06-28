package com.kdas.memoirs

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kdas.memoirs.ui.theme.MemoirsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MemoirsTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        GameApp()
                    }
                }
            }
        }
    }
}

@Composable
fun GameApp() {
    var currentScreen by remember { mutableStateOf("main") }
    
    when (currentScreen) {
        "main" -> MainScreen(
            onColoursClick = { currentScreen = "colours" },
            onFlagsClick = { currentScreen = "flags" },
            onBoxesClick = { currentScreen = "boxes" }
        )
        "colours" -> ColourGame(
            onBackClick = { currentScreen = "main" }
        )
        "flags" -> FlagGame(
            onBackClick = { currentScreen = "main" }
        )
        "boxes" -> BoxesGame(
            onBackClick = { currentScreen = "main" }
        )
    }
}

@Composable
fun MainScreen(
    onColoursClick: () -> Unit,
    onFlagsClick: () -> Unit,
    onBoxesClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = "MEMORY GAME",
            fontSize = 32.sp,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 48.dp)
        )
        
        Button(
            onClick = onColoursClick,
            modifier = Modifier
                .width(200.dp)
                .height(100.dp)
                .padding(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373))
        ) {
            Text("COLOURS", fontSize = 20.sp, color = Color.White)
        }
        
        Button(
            onClick = onFlagsClick,
            modifier = Modifier
                .width(200.dp)
                .height(100.dp)
                .padding(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF64B5F6))
        ) {
            Text("FLAGS", fontSize = 20.sp, color = Color.White)
        }
        
        Button(
            onClick = onBoxesClick,
            modifier = Modifier
                .width(200.dp)
                .height(100.dp)
                .padding(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF81C784))
        ) {
            Text("BOXES", fontSize = 20.sp, color = Color.White)
        }
    }
} 