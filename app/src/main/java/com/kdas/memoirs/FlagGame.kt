package com.kdas.memoirs

import androidx.compose.foundation.Image
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
import androidx.compose.ui.res.painterResource
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
fun FlagGame(onBackClick: () -> Unit) {
    val gridSize = 4
    val totalSquares = gridSize * gridSize
    val flagPairs = totalSquares / 2
    
    val flagList = listOf(
        R.drawable.flag_usa,
        R.drawable.flag_uk,
        R.drawable.flag_france,
        R.drawable.flag_germany,
        R.drawable.flag_italy,
        R.drawable.flag_japan,
        R.drawable.flag_canada,
        R.drawable.flag_india
    )
    
    // State
    var gridFlags by remember { mutableStateOf(List(totalSquares) { R.drawable.ic_launcher_background }) }
    var assignedFlags by remember { mutableStateOf(List(totalSquares) { R.drawable.ic_launcher_background }) }
    var revealed by remember { mutableStateOf(List(totalSquares) { false }) }
    var matchedFlags by remember { mutableStateOf(mutableSetOf<Int>()) }
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
        val pairs = (flagList.take(flagPairs) + flagList.take(flagPairs)).shuffled()
        assignedFlags = pairs
        gridFlags = List(totalSquares) { R.drawable.ic_launcher_background }
        revealed = List(totalSquares) { false }
        matchedFlags = mutableSetOf()
        clickedIndices = listOf()
        correctScore = 0
        incorrectScore = 0
        isPlayActive = false
        isBlinking = false
    }

    fun showFlagsTemporarily() {
        gridFlags = assignedFlags
        scope.launch {
            delay(playDelay)
            gridFlags = List(totalSquares) { R.drawable.ic_launcher_background }
            isPlayActive = false
        }
    }

    fun handleSquareClick(index: Int) {
        if (revealed[index] || isPlayActive) return
        val newRevealed = revealed.toMutableList()
        newRevealed[index] = true
        revealed = newRevealed
        val newGridFlags = gridFlags.toMutableList()
        newGridFlags[index] = assignedFlags[index]
        gridFlags = newGridFlags
        val newClicked = clickedIndices + index
        clickedIndices = newClicked
        
        if (newClicked.size % 2 == 0) {
            val idx1 = newClicked[newClicked.size - 2]
            val idx2 = newClicked[newClicked.size - 1]
            val flag1 = assignedFlags[idx1]
            val flag2 = assignedFlags[idx2]
            if (flag1 == flag2 && idx1 != idx2 && !matchedFlags.contains(flag1)) {
                matchedFlags = (matchedFlags + flag1).toMutableSet()
                correctScore += 1
                if (matchedFlags.size == flagPairs) {
                    showConfetti = true
                    scope.launch {
                        delay(4000)
                        showConfetti = false
                        isPlayActive = false
                    }
                }
            } else if (flag1 != flag2 && !matchedFlags.contains(flag1) && !matchedFlags.contains(flag2)) {
                incorrectScore += 1
            }
            if (flag1 != flag2 || matchedFlags.contains(flag1)) {
                scope.launch {
                    delay(1200)
                    val tempRevealed = revealed.toMutableList()
                    val tempGridFlags = gridFlags.toMutableList()
                    if (!matchedFlags.contains(flag1)) {
                        tempRevealed[idx1] = false
                        tempGridFlags[idx1] = R.drawable.ic_launcher_background
                    }
                    if (!matchedFlags.contains(flag2)) {
                        tempRevealed[idx2] = false
                        tempGridFlags[idx2] = R.drawable.ic_launcher_background
                    }
                    revealed = tempRevealed
                    gridFlags = tempGridFlags
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
                    showFlagsTemporarily()
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
                                            color = Color.Gray,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .border(2.dp, Color.DarkGray, RoundedCornerShape(8.dp))
                                        .clickable {
                                            handleSquareClick(idx)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        painter = painterResource(id = gridFlags[idx]),
                                        contentDescription = "Flag",
                                        modifier = Modifier.size(48.dp)
                                    )
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