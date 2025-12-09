package com.greencopper.interfacekit.ui

import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.annotation.SuppressLint
import android.content.res.Resources
import android.text.Spannable
import android.text.method.BaseMovementMethod
import android.text.style.ClickableSpan
import android.view.*
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.TextView
import com.greencopper.interfacekit.R
import com.greencopper.interfacekit.color.removeAlpha
import kotlin.math.cos

/** Changes any DP value to its PX value depending on the density. Be aware that this is restricted by a Float size. **/
public fun Number.pxToDp(): Float = this.toFloat() / Resources.getSystem().displayMetrics.density
public fun Number.dpToPx(): Int = (this.toFloat() * Resources.getSystem().displayMetrics.density).toInt()

public fun Number.pxToSp(): Float = this.toFloat() / Resources.getSystem().displayMetrics.scaledDensity
public fun Number.spToPx(): Float = this.toFloat() * Resources.getSystem().displayMetrics.scaledDensity

/** Set margins for this ViewGroup with dp values */
public fun ViewGroup.MarginLayoutParams.setDpMargins(
    left: Int = 0,
    top: Int = 0,
    right: Int = 0,
    bottom: Int = 0,
) {
    this.setMargins(left.dpToPx(), top.dpToPx(), right.dpToPx(), bottom.dpToPx())
}

@SuppressLint("NewApi")
public fun View.setShadowColor(color: Int) {
    outlineSpotShadowColor = color.removeAlpha()
    outlineAmbientShadowColor = color.removeAlpha()
}

public fun playScalingAnimationOnEvent(motionEvent: MotionEvent, target: View) {
    when (motionEvent.action) {
        MotionEvent.ACTION_DOWN -> target.playAnimation(R.animator.reduce_size, target)
        MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> target.playAnimation(
            R.animator.regain_size,
            target
        )
    }
}

public fun View.playAnimation(id: Int, rootTarget: View): AnimatorSet? =
    context?.let {
        (AnimatorInflater.loadAnimator(it, id) as? AnimatorSet)
            ?.apply {
                setTarget(rootTarget)
                start()
            }
    }

//We use cardUseCompatPadding to make sure the card view has enough room to display its shadow
//but it adds some extra padding on top of our own margin, so we calculate and remove some of this padding
//The formula below comes from the internets : https://stackoverflow.com/questions/34656252/cardview-cardusecompatpadding
public fun calculateCardViewCompatPadding(resources: Resources): Int {
    val elevation = resources.getDimension(R.dimen.widget_image_cardview_elevation)
    val radius = resources.getDimension(R.dimen.card_corner_radius)
    val cos45 = cos(Math.toRadians(45.0))
    return (elevation * 1.5 + (1 - cos45) * radius).toInt()
}

public class ClickableLinkMovementMethod : BaseMovementMethod() {
    override fun onTouchEvent(
        widget: TextView, buffer: Spannable,
        event: MotionEvent,
    ): Boolean {
        val action = event.action
        if (action == MotionEvent.ACTION_UP) {
            var x = event.x.toInt()
            var y = event.y.toInt()
            x -= widget.totalPaddingLeft
            y -= widget.totalPaddingTop
            x += widget.scrollX
            y += widget.scrollY
            val layout = widget.layout
            val line = layout.getLineForVertical(y)
            val off = layout.getOffsetForHorizontal(line, x.toFloat())
            val links = buffer.getSpans(off, off, ClickableSpan::class.java)
            if (links.isNotEmpty()) {
                links[0].onClick(widget)
                return true
            }
        }
        return super.onTouchEvent(widget, buffer, event)
    }
}

public fun View.disableAccessibleClickAction() {
    isClickable = false
    accessibilityDelegate = object : View.AccessibilityDelegate() {
        override fun onInitializeAccessibilityNodeInfo(host: View, info: AccessibilityNodeInfo) {
            info.removeAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_CLICK)
            info.isClickable = false
            super.onInitializeAccessibilityNodeInfo(host, info)
        }
    }
}
