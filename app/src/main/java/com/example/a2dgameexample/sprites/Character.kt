package com.example.a2dgameexample.sprites

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.example.a2dgameexample.R

class Character(context:Context) {

    private var run: Bitmap
    private var jump: Bitmap
    private var death: Bitmap

    private var runArr:Array<Bitmap>
    private var jumpArr:Array<Bitmap>
    private var deathArr: Array<Bitmap>


    init {
        // Load the coin sprite
        val options = BitmapFactory.Options().apply {
            inScaled = false
        }

        //Running
        run = BitmapFactory.decodeResource(context.resources, R.drawable.characterrun)
        val pieceCount = 6
        val width = run.width
        val height = run.height

        val pieceWidth = width / pieceCount

        runArr = Array(pieceCount) { Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888) }


        for(i in 0 until pieceCount) {
            runArr[i] = getScaledBitmap(Bitmap.createBitmap(run, i*pieceWidth, 0, pieceWidth, height))
        }

        //Jumping
        jump = BitmapFactory.decodeResource(context.resources, R.drawable.characterjump)
        val pieceCount2 = 8
        val width2 = jump.width
        val height2 = jump.height

        val pieceWidth2 = width2 / pieceCount2

        jumpArr = Array(pieceCount2) { Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888) }


        for(i in 0 until pieceCount2) {
            jumpArr[i] = getScaledBitmap(Bitmap.createBitmap(jump, i*pieceWidth2, 0, pieceWidth2, height2))
        }

        //Death
        death = BitmapFactory.decodeResource(context.resources, R.drawable.characterdeath)
        val pieceCount3 = 8
        val width3 = death.width
        val height3 = death.height

        val pieceWidth3 = width3 / pieceCount3

        deathArr = Array(pieceCount3) { Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888) }


        for(i in 0 until pieceCount3) {
            deathArr[i] = getScaledBitmap(Bitmap.createBitmap(death, i*pieceWidth3, 0, pieceWidth3, height3))
        }
    }



    public fun getRunBitmap(x:Int): Bitmap{
        return runArr[x]
    }

    public fun getJumpBitmap(x:Int): Bitmap {
        return jumpArr[x]
    }

    public fun getDeathBitmap(x:Int): Bitmap {
        return deathArr[x]
    }



    private fun getScaledBitmap(bitmap: Bitmap) : Bitmap {
        return Bitmap.createScaledBitmap(bitmap, bitmap.width*4, bitmap.height*4, false )
    }
}