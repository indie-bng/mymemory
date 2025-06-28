package com.kdas.memoirs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import nl.dionsegijn.konfetti.compose.KonfettiView
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.models.Shape
import nl.dionsegijn.konfetti.core.models.Size
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import java.util.concurrent.TimeUnit

@Composable
fun ColourGame(onBackClick: () -> Unit) {
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
    var showConfetti by remember { mutableStateOf(false) }
    var playDelay by remember { mutableStateOf(8000L) }
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
            delay(playDelay)
            gridColors = List(totalSquares) { Color.Gray }
            isPlayActive = false
        }
    }

    fun handleSquareClick(index: Int) {
        if (revealed[index] || isPlayActive) return
        val newRevealed = revealed.toMutableList()
        newRevealed[index] = true
        revealed = newRevealed
        val newGridColors = gridColors.toMutableList()
        newGridColors[index] = assignedColors[index]
        gridColors = newGridColors
        val newClicked = clickedIndices + index
        clickedIndices = newClicked
        
        if (newClicked.size % 2 == 0) {
            val idx1 = newClicked[newClicked.size - 2]
            val idx2 = newClicked[newClicked.size - 1]
            val color1 = assignedColors[idx1]
            val color2 = assignedColors[idx2]
            if (color1 == color2 && idx1 != idx2 && !matchedColors.contains(color1)) {
                matchedColors = (matchedColors + color1).toMutableSet()
                correctScore += 1
                if (matchedColors.size == colorPairs) {
                    showConfetti = true
                    scope.launch {
                        delay(4000)
                        showConfetti = false
                        isPlayActive = false
                    }
                }
            } else if (color1 != color2 && !matchedColors.contains(color1) && !matchedColors.contains(color2)) {
                incorrectScore += 1
            }
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
        if (newClicked.size == totalSquares) {
            isBlinking = true
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Back button
            Button(
                onClick = onBackClick,
                modifier = Modifier.padding(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
            ) {
                Text("BACK")
            }
            
            // Top Row for SMART/LAZY buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { playDelay = 6000L },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFEB3B)),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Feeling SMART", color = Color.Black)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { playDelay = 10000L },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Feeling LAZZY", color = Color.White)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            
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
    
    if (showConfetti) {
        KonfettiView(
            modifier = Modifier.fillMaxSize(),
            parties = listOf(
                Party(
                    emitter = Emitter(duration = 2, TimeUnit.SECONDS).max(300),
                    position = Position.Relative(0.5, 0.0),
                    spread = 360,
                    colors = colorList.map { it.value.toInt() },
                    shapes = listOf(Shape.Circle, Shape.Square),
                    size = listOf(Size.SMALL, Size.LARGE)
                )
            )
        )
    }
} 