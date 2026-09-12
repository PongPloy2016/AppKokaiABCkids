package com.pongploydev.priyankvex.paintapp.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pongploydev.priyankvex.paintapp.R
import kotlinx.coroutines.delay

class AbcMatchGameActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AbcMatchGameScreen(onBack = { finish() })
        }
    }
}

private val MonsterAlphabet = listOf(
    R.drawable.words_a, R.drawable.words_b, R.drawable.words_c, R.drawable.words_d,
    R.drawable.words_e, R.drawable.words_f, R.drawable.words_g, R.drawable.words_h,
    R.drawable.words_i, R.drawable.words_j, R.drawable.words_k, R.drawable.words_l,
    R.drawable.words_m, R.drawable.words_n, R.drawable.words_o, R.drawable.words_p,
    R.drawable.words_q, R.drawable.words_r, R.drawable.words_s, R.drawable.words_t,
    R.drawable.words_u, R.drawable.words_v, R.drawable.words_w, R.drawable.words_x,
    R.drawable.words_y, R.drawable.words_z
)

enum class GameState { START, PLAYING, WON }

@Composable
fun AbcMatchGameScreen(onBack: () -> Unit) {
    var gameState by remember { mutableStateOf(GameState.START) }
    var timeSeconds by remember { mutableIntStateOf(0) }
    var timerRunning by remember { mutableStateOf(false) }

    LaunchedEffect(timerRunning) {
        if (timerRunning) {
            while (true) {
                delay(1000)
                timeSeconds++
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Gradient Background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFFE0F7FA), Color(0xFFB2EBF2))
                    )
                )
        )

        when (gameState) {
            GameState.START -> {
                StartScreen(onStart = {
                    gameState = GameState.PLAYING
                    timerRunning = true
                })
            }
            GameState.PLAYING -> {
                GameGrid(
                    onWin = {
                        gameState = GameState.WON
                        timerRunning = false
                    },
                    timeSeconds = timeSeconds
                )
            }
            GameState.WON -> {
                WinScreen(
                    timeSeconds = timeSeconds,
                    onRestart = {
                        gameState = GameState.START
                        timeSeconds = 0
                    },
                    onHome = onBack
                )
            }
        }
    }
}

@Composable
fun StartScreen(onStart: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        // Large Background Letter
        Text(
            "F",
            fontSize = 400.sp,
            fontWeight = FontWeight.Black,
            color = Color.White.copy(alpha = 0.3f),
            modifier = Modifier.graphicsLayer { rotationZ = -10f }
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Title card
            Card(
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(6.dp, Color(0xFF81D4FA)),
                modifier = Modifier
                    .padding(16.dp)
                    .shadow(12.dp, RoundedCornerShape(32.dp))
            ) {
                Text(
                    "เกมจับคู่ตัวอักษร",
                    modifier = Modifier.padding(horizontal = 40.dp, vertical = 16.dp),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF0277BD)
                )
            }

            Spacer(modifier = Modifier.height(100.dp))

            // Start Button (Golden style)
            Button(
                onClick = onStart,
                modifier = Modifier
                    .height(80.dp)
                    .width(220.dp),
                shape = RoundedCornerShape(40.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC107)),
                border = BorderStroke(4.dp, Color.White),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 10.dp)
            ) {
                Text("เริ่มเล่น", fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color.White)
            }
        }
    }
}

data class GameCard(val id: Int, val iconRes: Int, var isMatched: Boolean = false)

@Composable
fun GameGrid(onWin: () -> Unit, timeSeconds: Int) {
    var cards by remember {
        mutableStateOf(generateCards())
    }
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    var hintIndices by remember { mutableStateOf<List<Int>>(emptyList()) }

    fun checkWin() {
        if (cards.all { it.isMatched }) {
            onWin()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Timer Progress Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(16.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White.copy(alpha = 0.6f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction = (timeSeconds % 60) / 60f)
                        .fillMaxHeight()
                        .background(
                            Brush.horizontalGradient(listOf(Color(0xFF66BB6A), Color(0xFF43A047)))
                        )
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text("⭐", fontSize = 24.sp)
        }

        // Game Board
        Card(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFDE7).copy(alpha = 0.9f)),
            border = BorderStroke(4.dp, Color(0xFFFFD54F))
        ) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 75.dp),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                itemsIndexed(cards) { index, card ->
                    val isHint = index in hintIndices
                    val isSelected = selectedIndex == index

                    Box(
                        modifier = Modifier
                            .aspectRatio(0.85f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (card.isMatched) Color.Transparent
                                else if (isSelected) Color(0xFFFFECB3)
                                else if (isHint) Color(0xFFE1F5FE)
                                else Color.White
                            )
                            .border(
                                width = if (card.isMatched) 0.dp else 2.dp,
                                color = if (isSelected) Color(0xFFFFB300) else if (isHint) Color(0xFF03A9F4) else Color(0xFFE0E0E0),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable(enabled = !card.isMatched) {
                                if (selectedIndex == null) {
                                    selectedIndex = index
                                } else {
                                    val firstIdx = selectedIndex!!
                                    if (firstIdx != index && cards[firstIdx].iconRes == cards[index].iconRes) {
                                        val newList = cards.toMutableList()
                                        newList[firstIdx] = newList[firstIdx].copy(isMatched = true)
                                        newList[index] = newList[index].copy(isMatched = true)
                                        cards = newList
                                        selectedIndex = null
                                        hintIndices = emptyList()
                                        checkWin()
                                    } else {
                                        selectedIndex = index
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (!card.isMatched) {
                            Image(
                                painter = painterResource(id = card.iconRes),
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(4.dp)
                                    .scale(if (isHint) 1.15f else 1f)
                            )
                        }
                    }
                }
            }
        }

        // Footer Actions
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp, bottom = 10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            GameActionCircle(
                icon = Icons.Default.Refresh,
                label = "Refresh",
                color = Color(0xFFEF5350),
                onClick = {
                    cards = generateCards()
                    selectedIndex = null
                    hintIndices = emptyList()
                }
            )

            GameActionCircle(
                icon = Icons.Default.Search,
                label = "Find",
                color = Color(0xFF66BB6A),
                onClick = {
                    val unmatched = cards.filter { !it.isMatched }
                    for (c1 in unmatched) {
                        val pair = unmatched.filter { it.iconRes == c1.iconRes }
                        if (pair.size >= 2) {
                            hintIndices = cards.indices.filter { cards[it].iconRes == c1.iconRes && !cards[it].isMatched }.take(2)
                            break
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun GameActionCircle(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, color: Color, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            modifier = Modifier
                .size(68.dp)
                .clickable { onClick() },
            shape = CircleShape,
            color = color,
            border = BorderStroke(3.dp, Color.White),
            shadowElevation = 6.dp
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.padding(16.dp),
                tint = Color.White
            )
        }
        Text(
            label,
            fontSize = 14.sp,
            color = Color(0xFF006064),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 6.dp)
        )
    }
}

@Composable
fun WinScreen(timeSeconds: Int, onRestart: () -> Unit, onHome: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(40.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(8.dp, Color(0xFF81D4FA)),
            modifier = Modifier.padding(32.dp)
        ) {
            Column(
                modifier = Modifier.padding(40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "เยี่ยมมาก!",
                    fontSize = 40.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF0288D1)
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                Text(
                    "เวลาที่คุณใช้: $timeSeconds วินาที",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray
                )

                Spacer(modifier = Modifier.height(40.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    // Home Button
                    FloatingActionButton(
                        onClick = onHome,
                        containerColor = Color(0xFFFF7043),
                        contentColor = Color.White,
                        shape = CircleShape
                    ) {
                        Icon(Icons.Default.Home, "Home")
                    }

                    // Share Button
                    FloatingActionButton(
                        onClick = { /* Share */ },
                        containerColor = Color(0xFF66BB6A),
                        contentColor = Color.White,
                        shape = CircleShape
                    ) {
                        Icon(Icons.Default.Share, "Share")
                    }

                    // Restart Button
                    FloatingActionButton(
                        onClick = onRestart,
                        containerColor = Color(0xFF26C6DA),
                        contentColor = Color.White,
                        shape = CircleShape
                    ) {
                        Icon(Icons.Default.Refresh, "Restart")
                    }
                }
            }
        }
    }
}

private fun generateCards(): List<GameCard> {
    // 5x6 = 30 cards
    val selectedLetters = MonsterAlphabet.shuffled().take(15)
    val cardIcons = (selectedLetters + selectedLetters).shuffled()
    return cardIcons.mapIndexed { index, icon -> GameCard(index, icon) }
}
