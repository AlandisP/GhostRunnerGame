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
import com.example.a2dgameexample.sprites.Obstacle
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
    private var jumpTics = 0
    private var deathTics = 0
    private var charAnimation = 0
    private var charJumpAnimation = 0
    private var charDeathAnimation = 0
    private var complete = false

    //Obstacle Stuff
    private var obstacleVelocity = 15f
    private var collision = false
    private var obX = 600f
    private var obY = floor
    private var pixelObstacle: Obstacle
    private var obType: Bitmap

    // Coin(s)
    private val paintCoin = Paint().apply{color = Color.YELLOW}
    private var coinX = w/2f + 150f
    private var coinY = h.toFloat()-30f
    private var coinRadius = 30f
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
        pixelObstacle = Obstacle(context)
        obType = pixelObstacle.getRock()
        generateObstacle()

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
        obX = 600f
        obY = floor
        score = 0
        coinX = w / 2f + 100f
        coinY = h - 30f
        jump = false
        collision = false
        complete = false
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
                checkObstacleCollisions2()
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
                    updateJumpAnimations()
                    //gravity = 0f
                    y -= velocity
                    velocity -= gravity
                    if (velocity < -1 * jumpHeight) {
                        velocity = jumpHeight
                        jump = false
                    }
                }
            }
            if(collision) {
                updateDeathAnimations()
                if(complete) {
                    setGameState(GameStates.GAME_OVER)
                    viewModel?.gameOver(score)
                }
            }
            Thread.sleep(16) // ~60 FPS
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
        obX-=obstacleVelocity
        if(obX < 0-obType.width) {
            //Random Float
            generateObstacle()
            obX=w.toFloat()
        }

    }


    private fun checkObstacleCollisions2() {
        val closestX = x.coerceIn(obX, obX + obType.width)
        val closestY = y.coerceIn(obY, obY + obType.height)

        // Calculate the distance between the circle's center and the closest point
        val distanceX = x - closestX
        val distanceY = y - closestY

        val distance = distanceX * distanceX + distanceY * distanceY
        val radiSquared = radius*radius
        if(distance <= radiSquared) {
            handleCollisions()
        }

    }

    private fun generateObstacle() {
        val randomNum = Random.nextInt(6)
        if (randomNum == 0) {
            obType = pixelObstacle.getRock()
        } else if(randomNum == 1) {
            obType = pixelObstacle.getSign()
        } else if(randomNum == 2) {
            obType = pixelObstacle.getFlower()
        } else if(randomNum == 3) {
            obType = pixelObstacle.getSpike()
        } else {
            obType = pixelObstacle.getSpikeTwo()
        }

    }

    private fun handleCollisions() {
        collision = true

    }


    private fun updateJumpAnimations() {
        jumpTics++
        if(jumpTics == speed) {
            if(charJumpAnimation < 7) {
                charJumpAnimation++
            } else {
                charJumpAnimation = 0
            }
        }
        if(jumpTics > speed) {
            jumpTics = 0
        }
    }

    private fun updateDeathAnimations() {
        deathTics++
        if(deathTics == speed) {
            if(charDeathAnimation < 7) {
                charDeathAnimation++
            } else{
                complete = true
                charDeathAnimation = 0
            }
        }
        if(deathTics > speed) {
            deathTics = 0
        }
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
            val randomY = Random.nextFloat()*(400f-h.toFloat())+h.toFloat()
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
        val pixelX = x - pixelChar.getRunBitmap(charAnimation).width/2f
        val pixelY = (y+radius) - pixelChar.getRunBitmap(charAnimation).height
        canvas.drawBitmap(scaledBitmap,0f, 0f, null)

        //Circle(Character)
        //canvas.drawCircle(x.toFloat(), y.toFloat(), radius, paint)
        if(gameState == GameStates.RUNNING) {
            if(jump) {
                canvas.drawBitmap(pixelChar.getJumpBitmap(charJumpAnimation), pixelX, pixelY, null)
            } else if(collision) {
                canvas.drawBitmap(pixelChar.getDeathBitmap(charDeathAnimation), pixelX, pixelY, null)
            }else {
                canvas.drawBitmap(pixelChar.getRunBitmap(charAnimation), pixelX, pixelY, null)
            }
        }


        //Rectangle(Obstacle(s))
        //canvas.drawRect(obstacleLeft,obstacleTop,obstacleRight,obstacleBottom, Paint().apply { color =  Color.BLUE })
        canvas.drawBitmap(obType, obX, obY-obType.height, null)
        //canvas.drawCircle(500f, 700f, 20f, paint)
        //canvas.drawCircle(coinX, coinY, coinRadius, paintCoin)
        //Keeps Track of the Score
        canvas.drawText("${score}", w/2f, 100f, Paint().apply {
            textSize = 65f
            color = Color.WHITE })
        val pixelCoinX = coinX - pixelCoin.getBitmap(coinAnimation).width/2f
        val pixelCoinY = (coinY+coinRadius) - pixelCoin.getBitmap(coinAnimation).height

        canvas.drawBitmap(pixelCoin.getBitmap(coinAnimation), pixelCoinX, pixelCoinY, null)

        Log.d("Dimensions", "height=$h, width=$w")

    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}
}
