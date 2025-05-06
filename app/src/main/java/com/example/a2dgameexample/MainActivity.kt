package com.example.a2dgameexample

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
        setContent {
            NavigationSystem()
        }
    }
}


@Composable
fun NavigationSystem() {
    val navController = rememberNavController()
    val viewModel: GameViewModel = viewModel()

    NavHost(navController = navController, startDestination = "PlayScreen", builder = {
        composable("PlayScreen") {
            PlayScreen(navController, viewModel)
        }
        composable("GameScreen") {
            GameScreen(navController, viewModel)
        }
    })


}

@Composable
fun GameScreen(navController: NavController, viewModel: GameViewModel) {
    val context = LocalContext.current
    val gameState by viewModel.gameState.collectAsState() // Use collectAsState()
    val gameView = remember { mutableStateOf<GameView?>(null) }
    val lifecycleScope = rememberCoroutineScope()
    val finalScore by viewModel.finalScore.collectAsState()
    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = {
                val view = GameView(it).apply {
                    setViewModel(viewModel)
                    setGameState(gameState)
                }
                gameView.value = view
                view
            },
            modifier = Modifier.fillMaxSize(),
            update = { view ->
                // This will be called whenever gameState changes
                view.setGameState(gameState)
            }
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(20.dp)
        ) {
            Image (
                painter = painterResource(id = R.drawable.pausebutton),
                contentDescription = null,
                modifier = Modifier
                    .height(70.dp)
                    .width(70.dp)
                    .clickable {
                        viewModel.pauseGame()
                        //navController.navigate("PauseMenu")
                    }
            )
        }

        if(gameState == GameStates.PAUSED) {
            Dialog(onDismissRequest = { /* Prevent dismissing by tapping outside */ }) {
                Surface( // Use Surface for the dialog background
                    modifier = Modifier.wrapContentSize(),
                    shape = RoundedCornerShape(20.dp), // Rounded corners for the dialog
                    color = Color.White
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Game Paused",
                            color = Color.Black,
                            fontSize = 60.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.play),
                                contentDescription = null,
                                modifier = Modifier
                                    .height(100.dp)
                                    .width(100.dp)
                                    .clickable {
                                        viewModel.startGame()
                                    }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Image(
                                painter = painterResource(id = R.drawable.restart),
                                contentDescription = null,
                                modifier = Modifier
                                    .height(100.dp)
                                    .width(100.dp)
                                    .clickable {
                                        viewModel.restartGame()
                                    }
                            )
                            Spacer(modifier = Modifier.width((8.dp)))
                            Image(
                                painter = painterResource(id = R.drawable.home),
                                contentDescription = null,
                                modifier = Modifier
                                    .height(100.dp)
                                    .width(100.dp)
                                    .clickable {
                                        viewModel.restartGame()
                                        navController.navigate(("PlayScreen"))
                                    }
                            )
                        }
                    }
                }
            }
        }

        if(gameState == GameStates.GAME_OVER) {
            Dialog(onDismissRequest = { /* Prevent dismissing by tapping outside */ }) {
                Surface(
                    modifier = Modifier.wrapContentSize(),
                    shape = RoundedCornerShape(20.dp),
                    color = Color.DarkGray
                ) {
                    Column(
                        modifier = Modifier.padding(25.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Game Over",
                            color = Color.Red,
                            fontSize = 64.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "Final Score: $finalScore",
                            color = Color.White,
                            fontSize = 40.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Button(
                                onClick = { viewModel.restartGame() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF4CAF50)
                                ),
                                modifier = Modifier.size(width = 180.dp, height = 70.dp)
                            ) {
                                Text(
                                    text = "Replay",
                                    color = Color.White,
                                    fontSize = 24.sp,
                                    fontFamily = FontFamily.Cursive,
                                    fontWeight = FontWeight.W900
                                )
                            }

                            Button(
                                onClick = { navController.navigate("PlayScreen") },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFF44336)
                                ),
                                modifier = Modifier.size(width = 180.dp, height = 70.dp)
                            ) {
                                Text(
                                    text = "Main Menu",
                                    color = Color.White,
                                    fontSize = 24.sp,
                                    fontFamily = FontFamily.Cursive,
                                    fontWeight = FontWeight.W900
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PlayScreen(navController: NavController, viewModel: GameViewModel) {
    Box( Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.newbackground),
            contentDescription = "Background Image of Game",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Ghost Runner",
                color = Color.White,
                fontSize = 75.sp,
                fontFamily = FontFamily.Cursive,
                fontWeight = FontWeight.W900
            )

            Spacer(modifier = Modifier.height(25.dp))

            Button (
                modifier = Modifier
                    .height(100.dp)
                    .width(200.dp),
                shape = RoundedCornerShape(topStart = 50.dp, bottomEnd = 50.dp),
                enabled = true,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                ),
                onClick = {
                    viewModel.startGame()
                    navController.navigate("GameScreen") {
                        launchSingleTop = true
                    }
                }
            ) {
                Text (
                    text = "Play",
                    color = Color.Black,
                    fontSize = 55.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}


