package seifemadhamdy.dicer.view

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.app.ActivityManager
import android.content.Context
import android.graphics.PixelFormat
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.animation.LinearInterpolator
import seifemadhamdy.dicer.util.Dice
import seifemadhamdy.dicer.view.renderer.DiceRenderer

class DiceView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    GLSurfaceView(context, attrs) {
    private val diceRenderer by lazy {
        DiceRenderer(
            context
        )
    }

    private var motionEventX = 0f
    private var motionEventY = 0f

    init {
        holder.setFormat(PixelFormat.RGBA_8888)

        setEGLContextClientVersion(
            ((context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager?)?.deviceConfigurationInfo?.glEsVersion)?.toDoubleOrNull()
                ?.toInt() ?: 2
        )

        setEGLConfigChooser(8, 8, 8, 8, 16, 0)
        setRenderer(diceRenderer)
        setZOrderOnTop(true)
    }

    private fun MotionEvent.saveCoordinates() {
        motionEventX = x
        motionEventY = y
    }

    override fun performClick(): Boolean {
        super.performClick()
        Dice.roll().show()
        return true
    }

    override fun onTouchEvent(motionEvent: MotionEvent): Boolean {
        when (motionEvent.action) {
            MotionEvent.ACTION_DOWN -> {
                motionEvent.saveCoordinates()
            }

            MotionEvent.ACTION_MOVE -> {
                diceRenderer.apply {
                    angleX += (motionEventX - motionEvent.x) / 2f
                    angleY += (motionEventY - motionEvent.y) / 2f
                }

                motionEvent.saveCoordinates()
            }

            MotionEvent.ACTION_UP -> {
                performClick()
            }
        }

        return true
    }

    private fun Dice.Faces.show(shouldAnimate: Boolean = true) {
        diceRenderer.apply {
            if (shouldAnimate) {
                AnimatorSet().apply {
                    play(ValueAnimator.ofFloat(angleX, this@show.angleX).apply {
                        addUpdateListener {
                            angleX = it.animatedValue as Float
                        }
                    }).with(ValueAnimator.ofFloat(angleY, this@show.angleY).apply {
                        addUpdateListener {
                            angleY = it.animatedValue as Float
                        }
                    })

                    interpolator = LinearInterpolator()
                    duration = 600
                }.start()
            } else {
                angleX = this@show.angleX
                angleY = this@show.angleY
            }
        }
    }
}