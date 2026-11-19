package com.example.purrfectstudy_anelka

import android.view.InputDevice
import android.view.MotionEvent
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.RecyclerView

private fun handleVerticalMouseWheel(event: MotionEvent, onDeltaY: (Int) -> Unit): Boolean {
    if (event.action == MotionEvent.ACTION_SCROLL && event.isFromSource(InputDevice.SOURCE_CLASS_POINTER)) {
        val vScroll = event.getAxisValue(MotionEvent.AXIS_VSCROLL)
        if (vScroll != 0f) {
            onDeltaY((-vScroll * 180).toInt())
            return true
        }
    }
    return false
}

/** Mengaktifkan gulir roda mouse pada NestedScrollView di emulator tanpa memengaruhi sentuhan di HP. */
fun NestedScrollView.enableMouseWheelScroll() {
    setOnGenericMotionListener { _, event ->
        handleVerticalMouseWheel(event) { deltaY ->
            smoothScrollBy(0, deltaY)
        }
    }
}

/** Mengaktifkan gulir roda mouse pada RecyclerView di emulator tanpa memengaruhi sentuhan di HP. */
fun RecyclerView.enableMouseWheelScroll() {
    setOnGenericMotionListener { _, event ->
        handleVerticalMouseWheel(event) { deltaY ->
            smoothScrollBy(0, deltaY)
        }
    }
}
