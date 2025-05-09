package com.example.a2dgameexample.sprites

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.core.graphics.createBitmap
import com.example.a2dgameexample.R

class Coin(context: Context) {

    private var coin: Bitmap

    private var coinarr: Array<Bitmap>

    init {
        // Load the coin sprite
        val options = BitmapFactory.Options().apply {
            inScaled = false
        }


        coin = BitmapFactory.decodeResource(context.resources, R.drawable.coinspritesheet)
        val pieceCount = 5
        val width = coin.width
        val height = coin.height

        val pieceWidth = width / pieceCount

        coinarr = Array(pieceCount) { Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888) }


        // If you have a spritesheet with multiple frames, you would split it here
        // For now, we're assuming a single frame
        for(i in 0 until pieceCount) {
            coinarr[i] = getScaledBitmap(Bitmap.createBitmap(coin, i*pieceWidth, 0, pieceWidth, height))


        }
    }


    public fun getSprite(x:Int): Bitmap{
        return coinarr[x]
    }




    public fun getBitmap(x: Int) : Bitmap {
        return coinarr[x]
    }

    private fun getScaledBitmap(bitmap: Bitmap) : Bitmap {
        return Bitmap.createScaledBitmap(bitmap, bitmap.width*3, bitmap.height*3, false )
    }



}