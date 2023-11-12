package com.example.lesson2spinner

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.signature.ObjectKey
import com.example.lesson2spinner.databinding.ActivityMainBinding
import java.util.Random


class MainActivity : AppCompatActivity(), SpinningWheelView.OnRotationListener<String?> {
    private lateinit var binding: ActivityMainBinding

    private val RAND_MIN_TIME_MS = 1000
    private val RAND_MAX_TIME_MS = 4000

    private var lastItem: String? = null
    private var colorsList: MutableList<String>? = null
    private var rnd: Random? = null
    private val imageList = listOf("Orange", "Green", "Blue")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.wheel.setOnRotationListener(this)

        rnd = Random()
        resetWheel()

        binding.startBtn.setOnClickListener {
            spinWheel()
        }

        binding.resetBtn.setOnClickListener {
            binding.wheel.setText("")
            Glide
                .with(this)
                .clear(binding.image)
        }
    }

    private fun resetWheel() {
        val arrayResources = resources.getStringArray(R.array.colors)
        colorsList = ArrayList(arrayResources.size)
        (colorsList as ArrayList<String>).addAll(listOf(*arrayResources))
        binding.wheel.setItems(R.array.colors)
    }

    private fun spinWheel() {
        binding.wheel.setItems(colorsList)
        if (colorsList!!.size > 0) {
            val randomTime: Int =
                rnd!!.nextInt(RAND_MAX_TIME_MS - RAND_MIN_TIME_MS + 1) + RAND_MIN_TIME_MS
            binding.wheel.rotate(50f, randomTime.toLong(), 50)
        } else {
            binding.wheel.visibility = View.INVISIBLE
            Toast.makeText(this@MainActivity, "Wheel is empty!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRotation() {
    }

    override fun onStopRotation(item: String?) {
        var item = item
        if (item == null || "" == item) {
            item = colorsList!![0]
        }
        lastItem = item

        if (item in imageList) {
            Glide
                .with(this)
                .load("https://loremflickr.com/320/240/dog")
                .signature(ObjectKey(System.currentTimeMillis().toString()))
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(binding.image)
        } else {
            binding.wheel.setText("Random Text")
        }
    }
}