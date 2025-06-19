package com.example.memoirs

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.memoirs.ui.theme.MemoirsTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

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
                        MemoryGame()
                    }
                }
            }
        }
    }
}

@Composable
fun MemoryGame() {
    val gridSize = 4
    val totalSquares = gridSize * gridSize
    val colorPairs = totalSquares / 2
    val colorList = listOf(
        Color(0xFFE57373), // Red
        Color(0xFF64B5F6), // Blue
        Color(0xFF81C784), // Green
        Color(0xFFFFD54F), // Yellow
        Color(0xFFBA68C8), // Purple
        Color(0xFF000000), // Black
        Color(0xFFA1887F), // Brown
        Color(0xFF4DD0E1)  // Cyan
    )
    // State
    var gridColors by remember { mutableStateOf(List(totalSquares) { Color.Gray }) }
    var assignedColors by remember { mutableStateOf(List(totalSquares) { Color.Gray }) }
    var revealed by remember { mutableStateOf(List(totalSquares) { false }) }
    var matchedColors by remember { mutableStateOf(mutableSetOf<Color>()) }
    var clickedIndices by remember { mutableStateOf(listOf<Int>()) }
    var correctScore by remember { mutableStateOf(0) }
    var incorrectScore by remember { mutableStateOf(0) }
    var isPlayActive by remember { mutableStateOf(false) }
    var isBlinking by remember { mutableStateOf(false) }
    var blinkOn by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    // Blinking effect for PLAY button
    LaunchedEffect(isBlinking) {
        while (isBlinking) {
            blinkOn = !blinkOn
            delay(500)
        }
        blinkOn = true
    }

    fun resetGame() {
        // Shuffle and assign color pairs
        val pairs = (colorList.take(colorPairs) + colorList.take(colorPairs)).shuffled()
        assignedColors = pairs
        gridColors = List(totalSquares) { Color.Gray }
        revealed = List(totalSquares) { false }
        matchedColors = mutableSetOf()
        clickedIndices = listOf()
        correctScore = 0
        incorrectScore = 0
        isPlayActive = false
        isBlinking = false
    }

    fun showColorsTemporarily() {
        gridColors = assignedColors
        scope.launch {
            delay(12000)
            gridColors = List(totalSquares) { Color.Gray }
            isPlayActive = false
        }
    }

    fun handleSquareClick(index: Int) {
        if (revealed[index] || isPlayActive || matchedColors.size == colorPairs) return
        // Reveal the clicked square
        val newRevealed = revealed.toMutableList()
        newRevealed[index] = true
        revealed = newRevealed
        val newGridColors = gridColors.toMutableList()
        newGridColors[index] = assignedColors[index]
        gridColors = newGridColors
        val newClicked = clickedIndices + index
        clickedIndices = newClicked
        // Check for pair
        if (newClicked.size % 2 == 0) {
            val idx1 = newClicked[newClicked.size - 2]
            val idx2 = newClicked[newClicked.size - 1]
            val color1 = assignedColors[idx1]
            val color2 = assignedColors[idx2]
            if (color1 == color2 && idx1 != idx2 && !matchedColors.contains(color1)) {
                // Success
                matchedColors.add(color1)
                correctScore += 1
            } else if (color1 != color2 && !matchedColors.contains(color1) && !matchedColors.contains(color2)) {
                // Failure
                incorrectScore += 1
            }
            // Hide after short delay if not matched
            if (color1 != color2 || matchedColors.contains(color1)) {
                scope.launch {
                    delay(1200)
                    val tempRevealed = revealed.toMutableList()
                    val tempGridColors = gridColors.toMutableList()
                    if (!matchedColors.contains(color1)) {
                        tempRevealed[idx1] = false
                        tempGridColors[idx1] = Color.Gray
                    }
                    if (!matchedColors.contains(color2)) {
                        tempRevealed[idx2] = false
                        tempGridColors[idx2] = Color.Gray
                    }
                    revealed = tempRevealed
                    gridColors = tempGridColors
                }
            }
        }
        // Check if all squares are clicked
        if (newClicked.size == totalSquares) {
            isBlinking = true
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        // PLAY Button
        Button(
            onClick = {
                resetGame()
                showColorsTemporarily()
            },
            enabled = !isPlayActive,
            modifier = Modifier
                .padding(8.dp)
                .then(if (isBlinking && !blinkOn) Modifier else Modifier)
        ) {
            Text("PLAY")
        }
        Spacer(modifier = Modifier.height(24.dp))
        // Grid
        Box(
            modifier = Modifier
                .size(320.dp)
                .background(Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            Column {
                for (row in 0 until gridSize) {
                    Row {
                        for (col in 0 until gridSize) {
                            val idx = row * gridSize + col
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .padding(4.dp)
                                    .background(
                                        color = gridColors[idx],
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .border(2.dp, Color.DarkGray, RoundedCornerShape(8.dp))
                                    .clickable {
                                        handleSquareClick(idx)
                                    },
                                contentAlignment = Alignment.Center
                            ) {}
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        // Score
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("CORRECT: $correctScore", fontSize = 20.sp, color = Color(0xFF388E3C), modifier = Modifier.padding(8.dp))
            Text("INCORRECT: $incorrectScore", fontSize = 20.sp, color = Color(0xFFD32F2F), modifier = Modifier.padding(8.dp))
        }
    }
}

