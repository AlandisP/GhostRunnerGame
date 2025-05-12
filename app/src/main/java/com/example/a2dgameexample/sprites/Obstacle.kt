package com.example.a2dgameexample.sprites

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.example.a2dgameexample.R

class Obstacle(context:Context) {

    private var rock: Bitmap
    private var spike1: Bitmap
    private var spike2: Bitmap
    private var deathFlower: Bitmap
    private var sign: Bitmap


    init {
        val options = BitmapFactory.Options().apply {
            inScaled = false
        }
        rock = BitmapFactory.decodeResource(context.resources,R.drawable.rock)
        spike1 = BitmapFactory.decodeResource(context.resources,R.drawable.spikes)
        spike2 = BitmapFactory.decodeResource(context.resources,R.drawable.spike2)
        deathFlower = BitmapFactory.decodeResource(context.resources,R.drawable.deathflower)
        sign = BitmapFactory.decodeResource(context.resources,R.drawable.stopsign)
    }


    private fun getScaledBitmap(bitmap: Bitmap,width: Int, height: Int) : Bitmap {
        return Bitmap.createScaledBitmap(bitmap, bitmap.width*width, bitmap.height*height, false)
    }

    public fun getRock(): Bitmap {
        return getScaledBitmap(rock, 6,8)
    }

    public fun getSpike(): Bitmap {
        return getScaledBitmap(spike1, 4, 8)
    }

    public fun getSpikeTwo(): Bitmap {
        return getScaledBitmap(spike2, 4, 6)
    }

    public fun getFlower(): Bitmap  {
        return getScaledBitmap(deathFlower, 4, 7)
    }

    public fun getSign(): Bitmap {
        return getScaledBitmap(sign, 6, 12)
    }
}