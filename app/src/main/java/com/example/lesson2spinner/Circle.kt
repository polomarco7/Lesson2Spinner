package com.example.lesson2spinner

import android.graphics.Matrix
import android.graphics.Point
import kotlin.math.min

internal class Circle() {
    private var cx = 0f
    private var cy = 0f
    private var radius = 0f
    private val matrix: Matrix = Matrix()

    constructor(width: Float, height: Float) : this() {
        cx = width / 2f
        cy = height / 2f
        radius = min(cx, cy)
    }

    fun getCx(): Float {
        return cx
    }

    fun getCy(): Float {
        return cy
    }

    fun getRadius(): Float {
        return radius
    }

    fun contains(x: Float, y: Float): Boolean {
        var x = x
        var y = y
        x = cx - x
        y = cy - y
        return x * x + y * y <= radius * radius
    }

    fun rotate(angle: Float, x: Float, y: Float): Point {
        matrix.setRotate(angle, cx, cy)

        val pts = FloatArray(2)

        pts[0] = x
        pts[1] = y

        matrix.mapPoints(pts)

        return Point(pts[0].toInt(), pts[1].toInt())
    }
}