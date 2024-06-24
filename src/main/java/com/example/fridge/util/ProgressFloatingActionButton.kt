package com.example.fridge.util

import android.content.Context
import android.graphics.drawable.AnimatedVectorDrawable
import android.util.AttributeSet
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import com.example.fridge.R
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ProgressFloatingActionButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FloatingActionButton(context, attrs, defStyleAttr) {

    fun showProgress() {
        setImageResource(R.drawable.ic_progress)
        val rotateAnimation = android.view.animation.AnimationUtils.loadAnimation(context,
            R.drawable.progress_animation
        )
        startAnimation(rotateAnimation)
    }

    fun showCheckAnimation() {
        clearAnimation()
        setImageResource(R.drawable.check_animation)
        val drawable = drawable
        if (drawable is AnimatedVectorDrawableCompat) {
            drawable.start()
        } else if (drawable is AnimatedVectorDrawable) {
            drawable.start()
        }
    }

    fun resetIcon() {
        clearAnimation()
        setImageResource(R.drawable.baseline_add_24)
    }
}
