package com.helpyourself.com.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Point
import android.view.View

/**
 * Extension function to capture a screenshot of a view
 */
fun View.takeScreenshot(): Bitmap {
    val bitmap = Bitmap.createBitmap(this.width, this.height, Bitmap.Config.RGB_565)
    val canvas = Canvas(bitmap)
    this.draw(canvas)
    return bitmap
}

/**
 * Extension function to get the center point of a view
 */
fun View.center(): Point {
    val location = IntArray(2)
    this.getLocationInWindow(location)
    return Point(
        location[0] + this.width / 2,
        location[1] + this.height / 2
    )
} 