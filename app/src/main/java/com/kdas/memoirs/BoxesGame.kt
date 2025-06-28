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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random
import nl.dionsegijn.konfetti.compose.KonfettiView
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.models.Shape
import nl.dionsegijn.konfetti.core.models.Size
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import java.util.concurrent.TimeUnit

@Composable
fun BoxesGame(onBackClick: () -> Unit) {
    val gridSize = 4
    val totalSquares = gridSize * gridSize
    
    // State
    var gridColors by remember { mutableStateOf(List(totalSquares) { Color.Gray }) }
    var gridMarkers by remember { mutableStateOf(List(totalSquares) { "" }) }
    var coloredPositions by remember { mutableStateOf(setOf<Int>()) }
    var isPlayActive by remember { mutableStateOf(false) }
    var isBlinking by remember { mutableStateOf(false) }
    var blinkOn by remember { mutableStateOf(true) }
    var correctScore by remember { mutableStateOf(0) }
    var incorrectScore by remember { mutableStateOf(0) }
    var totalColoredBoxes by remember { mutableStateOf(0) }
    var playDelay by remember { mutableStateOf(2000L) }
    var showConfetti by remember { mutableStateOf(false) }
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
        // Generate random number of boxes to color (3 to 7)
        val numColoredBoxes = Random.nextInt(3, 8) // 3 to 7 inclusive
        totalColoredBoxes = numColoredBoxes
        
        // Randomly select positions for colored boxes
        val positions = (0 until totalSquares).toMutableList()
        val selectedPositions = mutableSetOf<Int>()
        
        repeat(numColoredBoxes) {
            if (positions.isNotEmpty()) {
                val randomIndex = Random.nextInt(positions.size)
                val position = positions.removeAt(randomIndex)
                selectedPositions.add(position)
            }
        }
        
        coloredPositions = selectedPositions
        
        // Reset grid
        gridColors = List(totalSquares) { Color.Gray }
        gridMarkers = List(totalSquares) { "" }
        correctScore = 0
        incorrectScore = 0
        isPlayActive = false
        isBlinking = false
        showConfetti = false
    }

    fun showColoredBoxesTemporarily() {
        // Color the selected boxes blue
        val newGridColors = gridColors.toMutableList()
        coloredPositions.forEach { position ->
            newGridColors[position] = Color(0xFF2196F3) // Blue color
        }
        gridColors = newGridColors
        
        scope.launch {
            delay(playDelay)
            // Turn back to gray
            gridColors = List(totalSquares) { Color.Gray }
            isPlayActive = false
        }
    }

    fun handleSquareClick(index: Int) {
        if (isPlayActive || gridMarkers[index] != "") return // Don't allow clicks during play or on already marked squares
        
        val newGridMarkers = gridMarkers.toMutableList()
        
        if (coloredPositions.contains(index)) {
            // Correct guess - show green tick
            newGridMarkers[index] = "✓"
            correctScore += 1
        } else {
            // Incorrect guess - show red cross
            newGridMarkers[index] = "✗"
            incorrectScore += 1
        }
        
        gridMarkers = newGridMarkers
        
        // Check if all colored boxes have been found
        val foundColoredBoxes = gridMarkers.count { it == "✓" }
        if (foundColoredBoxes == totalColoredBoxes) {
            isBlinking = true
            // End the game - prevent further clicks
            isPlayActive = true
            // Show confetti
            showConfetti = true
            scope.launch {
                delay(4000)
                showConfetti = false
            }
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
                    onClick = { playDelay = 1000L },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFEB3B)),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Feeling SMART", color = Color.Black)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { playDelay = 3000L },
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
                    showColoredBoxesTemporarily()
                },
                // enabled = !isPlayActive,
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
                                ) {
                                    if (gridMarkers[idx] != "") {
                                        Text(
                                            text = gridMarkers[idx],
                                            fontSize = 48.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (gridMarkers[idx] == "✓") Color.Green else Color.Red
                                        )
                                    }
                                }
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
            
            // Show total colored boxes info
            if (totalColoredBoxes > 0) {
                Text(
                    text = "Find $totalColoredBoxes colored boxes",
                    fontSize = 16.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 8.dp)
                )
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
                    colors = listOf(0xFFE57373.toInt(), 0xFF64B5F6.toInt(), 0xFF81C784.toInt(), 0xFFFFD54F.toInt(), 0xFFBA68C8.toInt(), 0xFF000000.toInt(), 0xFFA1887F.toInt(), 0xFF4DD0E1.toInt()),
                    shapes = listOf(Shape.Circle, Shape.Square),
                    size = listOf(Size.SMALL, Size.LARGE)
                )
            )
        )
    }
} 