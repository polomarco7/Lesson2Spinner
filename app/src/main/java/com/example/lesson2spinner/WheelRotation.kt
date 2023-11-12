package com.example.lesson2spinner

import android.os.CountDownTimer

internal class WheelRotation(private val duration: Long, countDownInterval: Long) :
    CountDownTimer(duration, countDownInterval) {
    private val ROTATE_SCALE_FACTOR = 2f
    private val thresholdSlow: Long = (duration * SLOW_FACTOR).toLong()
    private var rotationListener: RotationListener? = null
    private var maxAngle = 0f
    private var angle = 1f

    fun setMaxAngle(maxAngle: Float): WheelRotation {
        this.maxAngle = maxAngle
        return this
    }

    fun setListener(l: RotationListener?): WheelRotation {
        rotationListener = l
        return this
    }

    override fun onTick(millisUntilFinished: Long) {
        if (rotationListener == null) {
            return
        }
        if (millisUntilFinished <= thresholdSlow) {
            angle = maxAngle * (millisUntilFinished.toFloat() / duration.toFloat())
            rotationListener!!.onRotate(angle)
        } else if (angle < maxAngle) {
            rotationListener!!.onRotate(angle)
            angle *= ROTATE_SCALE_FACTOR
            if (angle > maxAngle) {
                angle = maxAngle
            }
        } else {
            rotationListener!!.onRotate(angle)
        }
    }

    override fun onFinish() {
        if (rotationListener == null) {
            return
        }
        rotationListener!!.onStop()
    }

    interface RotationListener {
        fun onRotate(angle: Float)
        fun onStop()
    }

    companion object {
        private const val SLOW_FACTOR = 2f / 3f
        fun init(millisInFuture: Long, countDownInterval: Long): WheelRotation {
            return WheelRotation(millisInFuture, countDownInterval)
        }
    }
}
