package seifemadhamdy.dicer.ui

import android.os.Bundle
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import seifemadhamdy.dicer.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        WindowCompat.getInsetsController(window, window.decorView).apply {
            systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

            hide(WindowInsetsCompat.Type.systemBars())
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onResume() {
        super.onResume()
        binding.diceView.onResume()
    }

    override fun onStart() {
        super.onStart()
        binding.windmillAppCompatImageView.rotateIndefinitely()
    }

    override fun onPause() {
        super.onPause()
        binding.diceView.onPause()
    }

    private fun ImageView.rotateIndefinitely(duration: Long = 60000) {
        animate().withLayer().rotationBy(360f).apply {
            interpolator = LinearInterpolator()
            this.duration = duration

            withEndAction {
                this@rotateIndefinitely.rotateIndefinitely()
            }

            start()
        }
    }
}