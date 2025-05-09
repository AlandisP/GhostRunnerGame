package com.example.a2dgameexample

import android.app.GameState
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.DisplayMetrics
import android.util.Log
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.compose.ui.unit.sp
import androidx.core.util.Consumer
import com.example.a2dgameexample.sprites.Character
import com.example.a2dgameexample.sprites.Coin
import java.util.Timer
import kotlin.math.pow
import kotlin.random.Random


class GameView(context: Context) : SurfaceView(context), SurfaceHolder.Callback, Runnable {
    var displayMetrics: DisplayMetrics = resources.displayMetrics

    var h: Int = displayMetrics.heightPixels
    var w: Int = displayMetrics.widthPixels

    private var thread = Thread(this)
    private var running = false
    private var onGameStateChanged: Consumer<GameStates>? = null

    //speed
    private var speed = 7


    //  Ball/Character Stuff
    private val paint = Paint().apply { color = Color.RED }
    private var x:Float  = 50f
    private var y: Float  = h.toFloat()
    private val floor: Float = h.toFloat()
    private var radius = 30f;
    private var gravity:Float = 1.7f
    private var jump: Boolean = false
    private var jumpHeight = 30f
    private var velocity = jumpHeight
    private var pixelChar: Character
    private var charTics = 0
    private var charAnimation = 0

    //Obstacle Stuff
    private var obstacleArea = 50f
    private var obstacleLeft = (w/2f)-obstacleArea
    private var obstacleTop = 700f
    private var obstacleRight = (w/2f)+obstacleArea
    private var obstacleBottom = floor //This value should never change unless I want floating obstacles in later development.
    private var obstacleVelocity = 15f

    // Coin(s)
    private val paintCoin = Paint().apply{color = Color.YELLOW}
    private var coinX = w/2f + 100f
    private var coinY = h.toFloat()-30f
    private var coinRadius = 30f
    private var coinGap = 75f
    private var pixelCoin: Coin
    private var coinTics = 0;
    private var coinAnimation = 0;




    //Score
    private var score = 0;


    //game state
    private var gameState: GameStates = GameStates.NOT_PLAYING


    private var viewModel: GameViewModel? = null


    private var scaledBitmap:Bitmap

    init {
        holder.addCallback(this)

        // Load bitmap once with more efficient format
        val options = BitmapFactory.Options().apply {
            inPreferredConfig = Bitmap.Config.RGB_565 // Uses half the memory of default ARGB_8888
        }
        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.newbackgroundtwo, options)
        scaledBitmap = Bitmap.createScaledBitmap(bitmap, w, h, false)

        // Free up the original bitmap memory
        if (bitmap != scaledBitmap) {
            bitmap.recycle()
        }
        pixelCoin = Coin(context)
        pixelChar = Character(context)
    }

    fun setViewModel(vm: GameViewModel) {  // Set the view model.
        viewModel = vm
    }

    // Method to set the callback
    fun setOnGameStateChanged(callback: Consumer<GameStates>) {
        this.onGameStateChanged = callback
    }

    // Method to set game state
    fun setGameState(state: GameStates) {
        // Only handle state changes if the new state is different
        if (this.gameState != state) {
            this.gameState = state
            onGameStateChanged?.accept(state)

            when (state) {
                GameStates.RUNNING -> {
                    if (!running) {
                        running = true
                        if (thread.state == Thread.State.NEW) {
                            thread.start()
                        } else if (thread.state == Thread.State.TERMINATED) {
                            // Create a new thread if previous one is terminated
                            thread = Thread(this)
                            thread.start()
                        }
                    }
                }
                GameStates.PAUSED -> {
                    running = false
                }
                GameStates.GAME_OVER -> {
                    running = false
                    // We don't reset the game here, as we want to show the final state
                    // when the game over screen is displayed
                }
                GameStates.NOT_PLAYING -> {
                    running = false
                    resetGame()
                }
            }
        }
    }

    fun pause() {
        running = false
        try {
            thread.join()
        } catch (e: InterruptedException) {
            // Handle exception
        }
    }

    fun resume() {
        if (!running && gameState == GameStates.RUNNING) {
            running = true
            thread = Thread(this)
            thread.start()
        }
    }

    fun resetGame() {
        x = 50f
        y = h.toFloat()
        velocity = jumpHeight
        obstacleLeft = (w / 2f) - obstacleArea
        obstacleRight = (w / 2f) + obstacleArea
        obstacleTop = 700f
        score = 0
        coinX = w / 2f + 100f
        coinY = h - 30f
        jump = false
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        if (gameState == GameStates.RUNNING) {  // Start thread only if game is running
            running = true
            thread = Thread(this)
            thread.start()
        }
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        running = false
        try {
            thread.join()
        } catch (e: InterruptedException) {
            // Handle exception
        }
    }

    override fun run() {
        while (running) {
            if(gameState == GameStates.RUNNING) {
                val canvas = holder.lockCanvas()
                canvas?.let {
                    drawGame(it)
                    holder.unlockCanvasAndPost(it)
                }
                moveObstacle()
                checkObstacleCollisions()
                moveCoin()
                collectedCoin()
                updateCharAnimations()
                if (y + radius >= floor) {
                    y = floor - radius
                } else if (y < floor) {
                    y += gravity
                }
                //Jumping Mechanic
                if (jump) {
                    //gravity = 0f
                    y -= velocity
                    velocity -= gravity
                    if (velocity < -1 * jumpHeight) {
                        velocity = jumpHeight
                        jump = false
                    }
                }

                Thread.sleep(16) // ~60 FPS
            }
        }
    }


    override fun onTouchEvent(e: MotionEvent) : Boolean {
        when(e.action) {
            MotionEvent.ACTION_DOWN -> {
                jump = true
            }
        }
        return true
    }

    private fun moveObstacle() {
        obstacleLeft-=obstacleVelocity
        obstacleRight-=obstacleVelocity
        if(obstacleRight < 0) {
            //Random Float
            val randomFloat = Random.nextFloat()*(700-515)+515
            val randomFloatArea = Random.nextFloat()*(50-10)+10
            obstacleArea = randomFloatArea
            obstacleLeft=w.toFloat()-randomFloatArea
            obstacleRight=w.toFloat()+randomFloatArea
            obstacleTop = randomFloat

        }

    }

    private fun checkObstacleCollisions() {
        //  Directly Over the obstacle(Left)
        if(x - radius <= obstacleLeft-obstacleVelocity && x + radius >= obstacleLeft+obstacleVelocity && y + radius >= obstacleTop) {
            running = false
            setGameState(GameStates.GAME_OVER)
            viewModel?.gameOver(score)
        } else if(x - radius <= obstacleLeft-obstacleVelocity && x+radius >= obstacleRight-obstacleVelocity && y + radius <= obstacleTop && y + radius >= obstacleBottom) { //Within the obstacle
            running = false
            setGameState(GameStates.GAME_OVER)
            viewModel?.gameOver(score)
        } else if(x + radius <= obstacleRight+obstacleVelocity && x - radius >= obstacleRight-obstacleVelocity && y - radius >= obstacleTop) {  //Right side(Over)
            running = false
            setGameState(GameStates.GAME_OVER)
            viewModel?.gameOver(score)
        } else if(y+radius >= obstacleTop && x-radius >= obstacleLeft-obstacleVelocity && x+radius <= obstacleRight+obstacleVelocity) {
            running = false
            setGameState(GameStates.GAME_OVER)
            viewModel?.gameOver(score)
        }
    }

    private fun generateCoins(canvas: Canvas) {
//        val randomFloat = Random.nextFloat()*(700-515)+515
//        val randomNum = Random.nextInt()*(2-1)+1
//
//        if(randomNum == 1) {
//            coinY = floor.toFloat()
//        } else {
//            coinY = Random.nextFloat()*(800-500)+500
//        }

        canvas.drawCircle(coinX+coinGap, coinY, coinRadius, paintCoin)
//        canvas.drawCircle(coinX+coinGap*2, coinY-60f, coinRadius, paintCoin)
//        canvas.drawCircle(coinX+coinGap*3, coinY-60f, coinRadius, paintCoin)
//        canvas.drawCircle(coinX+coinGap*4, coinY, coinRadius, paintCoin)

    }

    private fun updateCharAnimations() {
        charTics++
        if(charTics == speed-6) {
            if(charAnimation < 5) {
                charAnimation++
            } else {
                charAnimation = 0
            }
        }
        if(charTics > speed) {
            charTics = 0
        }
    }

    private fun moveCoin() {
        coinX-=obstacleVelocity;
        if(coinX+coinRadius < -50f) {
            val randomY = Random.nextFloat()*(500f-h.toFloat())+h.toFloat()
            coinX = w.toFloat() + 95f
            coinY = randomY
        }
        coinTics++
        if (coinTics == speed) {
            if(coinAnimation < 4) {
                coinAnimation++
            } else {
                coinAnimation = 0
            }
        }
        if(coinTics > speed) {
            coinTics = 0
        }
    }

    private fun collectedCoin() {
        val ballCenterX = x
        val ballCenterY = y
        val ballRadius = radius

        val coinCenterX = coinX + coinRadius
        val coinCenterY = coinY + coinRadius
        val coinRadius = coinRadius

        val distanceSquared = (ballCenterX - coinCenterX).pow(2) + (ballCenterY - coinCenterY).pow(2)
        val sumOfRadiiSquared = (ballRadius + coinRadius).pow(2)

        if (distanceSquared <= sumOfRadiiSquared) {
            coinX = -100f
            Log.d("Collision", "Coin Hit (Air or Ground)")
            score++
        }
    }

    private fun drawGame(canvas: Canvas) {
        canvas.drawBitmap(scaledBitmap,0f, 0f, null)

        //canvas.drawColor(Color.TRANSPARENT)
        //Circle(Character)
        canvas.drawCircle(x.toFloat(), y.toFloat(), radius, paint)
        if(gameState == GameStates.RUNNING) {
            val pixelX = x - pixelChar.getRunBitmap(charAnimation).width/2f
            val pixelY = (y+radius) - pixelChar.getRunBitmap(charAnimation).height
            canvas.drawBitmap(pixelChar.getRunBitmap(charAnimation),pixelX,pixelY, null)
        }

        //Rectangle(Obstacle(s))
        canvas.drawRect(obstacleLeft,obstacleTop,obstacleRight,obstacleBottom, Paint().apply { color = Color.BLUE })
        //canvas.drawCircle(300f, 100f, 20f, paint)
        //canvas.drawCircle(coinX, coinY, coinRadius, paintCoin)
        //Keeps Track of the Score
        canvas.drawText("${score}", w/2f, 100f, Paint().apply {
            textSize = 65f
            color = Color.WHITE })

        canvas.drawBitmap(pixelCoin.getBitmap(coinAnimation), coinX, coinY, null)

        Log.d("Dimensions", "height=$h, width=$w")

    }




    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}
}
