package com.reproducerapp

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.ViewGroup
import com.facebook.react.module.annotations.ReactModule
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.ViewGroupManager
import com.facebook.react.uimanager.ViewManagerDelegate
import com.facebook.react.viewmanagers.TransitionContainerManagerDelegate
import com.facebook.react.viewmanagers.TransitionContainerManagerInterface

/**
 * Keeps a child that React removes on screen while it animates out, the way react-native-screens,
 * LayoutTransition and fragment transitions do: startViewTransition(child) before the removal, and
 * endViewTransition(child) when the exit animation ends.
 */
class TransitionContainer(context: Context) : ViewGroup(context) {

  fun removeViewWithExitAnimation(index: Int) {
    val child = getChildAt(index)
    startViewTransition(child)
    removeViewAt(index)
    Log.d(TAG, "removed view ${child.id} with an exit animation, its parent is ${child.parent}")

    // Stands in for the exit animation.
    Handler(Looper.getMainLooper())
        .postDelayed(
            {
              endViewTransition(child)
              Log.d(
                  TAG,
                  "exit animation of view ${child.id} ended, attached to window: " +
                      "$isAttachedToWindow, its parent is ${child.parent}",
              )
            },
            EXIT_ANIMATION_MS,
        )
  }

  override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) = Unit

  companion object {
    const val TAG = "TransitionContainer"
    const val EXIT_ANIMATION_MS = 1000L
  }
}

@ReactModule(name = TransitionContainerManager.NAME)
class TransitionContainerManager :
    ViewGroupManager<TransitionContainer>(),
    TransitionContainerManagerInterface<TransitionContainer> {

  private val delegate = TransitionContainerManagerDelegate(this)

  override fun getDelegate(): ViewManagerDelegate<TransitionContainer> = delegate

  override fun getName(): String = NAME

  override fun createViewInstance(context: ThemedReactContext): TransitionContainer =
      TransitionContainer(context)

  override fun removeViewAt(parent: TransitionContainer, index: Int) {
    parent.removeViewWithExitAnimation(index)
  }

  companion object {
    const val NAME = "TransitionContainer"
  }
}
