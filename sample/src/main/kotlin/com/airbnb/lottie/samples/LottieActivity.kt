package com.airbnb.lottie.samples

import android.animation.Animator
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.createBitmap
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.model.KeyPath
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.core.graphics.get
import com.airbnb.lottie.samples.databinding.ActivityLottieBinding
import com.airbnb.lottie.value.LottieValueCallback

class LottieActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLottieBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLottieBinding.inflate(layoutInflater)
        supportActionBar?.hide()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }// 复杂 complex

        binding.animationView.setAnimation("Tiktok-3D.json")
        binding.animationView.setRepeatCount(-1)
        binding.animationView.addLottieOnCompositionLoadedListener { composition ->
            // Build KeyPath with layer name as parameter
            val keyPath = KeyPath("NAME")
            val callback = LottieValueCallback<String>("小明1")
            binding.animationView.addValueCallback(
                keyPath,
                LottieProperty.TEXT.toString(),
                callback
            )

            val keyPath1 = KeyPath("京东集团")
            val callback1 =
                LottieValueCallback<String>("京东集团-CHO")
            binding.animationView.addValueCallback(
                keyPath1,
                LottieProperty.TEXT.toString(),
                callback1
            )

            val keyPath2 = KeyPath("企业信息化部")
            val callback2 =
                LottieValueCallback<String>("人力资源部")
            binding.animationView.addValueCallback(
                keyPath2,
                LottieProperty.TEXT.toString(),
                callback2
            )
        }

        // Add animation end listener
        binding.animationView.addAnimatorListener(object :
            Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}
            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
            override fun onAnimationEnd(animation: Animator) {
//                captureAndSaveScreen()
            }
        })

        binding.animationView.playAnimation()

        binding.export.setOnClickListener {
            captureAndSaveScreen()
        }

        binding.play.setOnClickListener {
            binding.animationView.resumeAnimation()
        }

        binding.pause.setOnClickListener {
            binding.animationView.pauseAnimation()
        }
    }

    private fun captureAndSaveScreen() {
        try {
            // Capture only LottieAnimationView content
            val animationView = binding.animationView
            val screenshot = createBitmap(animationView.width, animationView.height)
            val canvas = Canvas(screenshot)
            animationView.draw(canvas)

            val croppedBitmap = cropTransparentArea(screenshot)
            val roundedBitmap = addRoundedCorners(croppedBitmap)

            saveBitmapToCache(roundedBitmap)
        } catch (e: Exception) {
            Toast.makeText(this, "Screenshot failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun cropTransparentArea(bitmap: Bitmap): Bitmap {
        // Scan the entire image to find content boundaries
        var left = bitmap.width
        var right = 0
        var top = bitmap.height
        var bottom = 0

        // Traverse the entire image to find boundaries of all non-background pixels
        for (y in 0 until bitmap.height) {
            for (x in 0 until bitmap.width) {
                val pixel = bitmap[x, y]
                if (isCardContent(pixel)) {
                    left = minOf(left, x)
                    right = maxOf(right, x)
                    top = minOf(top, y)
                    bottom = maxOf(bottom, y)
                }
            }
        }

        // Return original image if no content found
        if (left >= bitmap.width || right < 0 || top >= bitmap.height || bottom < 0) {
            return bitmap
        }

        // Precise cropping without adding margins

        val width = right - left + 1
        val height = bottom - top + 1

        return Bitmap.createBitmap(bitmap, left, top, width, height)
    }

    private fun isCardContent(pixel: Int): Boolean {
        val alpha = Color.alpha(pixel)

        // Since we only capture animation view, mainly need to filter transparent background
        // Pixels with alpha greater than a certain threshold are considered valid content
        return alpha > 30
    }

    private fun calculateCornerRadius(bitmap: Bitmap): Float {
        val minDimension = minOf(bitmap.width, bitmap.height)
        val baseRadius = (minDimension * 0.03f).coerceAtLeast(12f).coerceAtMost(40f)

        // Dynamically adjust based on image size
        return when {
            minDimension < 300 -> baseRadius * 0.5f
            minDimension > 800 -> baseRadius * 1.2f
            else -> baseRadius
        }
    }

    private fun addRoundedCorners(bitmap: Bitmap): Bitmap {
        val cornerRadius = calculateCornerRadius(bitmap)
        val roundedBitmap = createBitmap(bitmap.width, bitmap.height)
        val canvas = Canvas(roundedBitmap)

        val paint = Paint().apply {
            isAntiAlias = true
        }

        val rect = RectF(0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat())
        val path = Path().apply {
            addRoundRect(rect, cornerRadius, cornerRadius, Path.Direction.CW)
        }

        canvas.clipPath(path)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)

        return roundedBitmap
    }

    private fun saveBitmapToCache(bitmap: Bitmap) {
        val cacheDir = File(cacheDir, "screenshots")
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }

        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val filename = "screenshot_$timestamp.png"
        val file = File(cacheDir, filename)

        try {
            FileOutputStream(file).use { fos ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            }
            Toast.makeText(this, "Screenshot saved to: ${file.absolutePath}", Toast.LENGTH_LONG).show()
        } catch (e: IOException) {
            Toast.makeText(this, "Save failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
