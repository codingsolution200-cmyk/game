package com.example.game.presentation.ui.game

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.TextView

object DiceAnimationHelper {

    fun animateRollStart(rollButton: View, diceFace: View) {
        rollButton.animate()
            .scaleX(0.94f)
            .scaleY(0.94f)
            .setDuration(80)
            .withEndAction {
                rollButton.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(180)
                    .setInterpolator(DecelerateInterpolator())
                    .start()
            }
            .start()

        diceFace.animate()
            .rotationBy(18f)
            .setDuration(120)
            .withEndAction {
                diceFace.animate()
                    .rotation(0f)
                    .setDuration(180)
                    .setInterpolator(DecelerateInterpolator())
                    .start()
            }
            .start()
    }

    fun spinDiceFace(diceText: TextView, newFace: String, durationMs: Long) {
        val flipOut = ObjectAnimator.ofFloat(diceText, "scaleX", 1f, 0.12f).apply {
            duration = durationMs / 2
            interpolator = AccelerateDecelerateInterpolator()
        }
        flipOut.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                diceText.text = newFace
                AnimatorSet().apply {
                    playTogether(
                        ObjectAnimator.ofFloat(diceText, "scaleX", 0.12f, 1f),
                        ObjectAnimator.ofFloat(diceText, "scaleY", 0.88f, 1.08f, 1f),
                        ObjectAnimator.ofFloat(diceText, "rotation", -10f, 8f, 0f)
                    )
                    duration = durationMs / 2
                    interpolator = AccelerateDecelerateInterpolator()
                    start()
                }
            }
        })
        flipOut.start()
    }

    fun showFinalDice(diceText: TextView, finalFace: String) {
        val flipOut = ObjectAnimator.ofFloat(diceText, "scaleX", 1f, 0.08f).apply {
            duration = 90
            interpolator = AccelerateDecelerateInterpolator()
        }
        flipOut.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                diceText.text = finalFace
                AnimatorSet().apply {
                    playTogether(
                        ObjectAnimator.ofFloat(diceText, "scaleX", 0.08f, 1.42f, 1f),
                        ObjectAnimator.ofFloat(diceText, "scaleY", 0.85f, 1.42f, 1f),
                        ObjectAnimator.ofFloat(diceText, "rotation", -18f, 12f, -6f, 0f),
                        ObjectAnimator.ofFloat(diceText, "translationY", -10f, 0f)
                    )
                    duration = 380
                    interpolator = DecelerateInterpolator()
                    start()
                }
            }
        })
        flipOut.start()
    }
}
