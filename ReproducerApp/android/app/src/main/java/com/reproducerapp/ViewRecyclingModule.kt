package com.reproducerapp

import android.content.Context
import android.content.Intent
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.internal.featureflags.ReactNativeFeatureFlags

/** Reads and sets the enableViewRecycling feature flag, which MainApplication applies at launch. */
class ViewRecyclingModule(context: ReactApplicationContext) : NativeViewRecyclingSpec(context) {

  override fun isEnabled(): Boolean = ReactNativeFeatureFlags.enableViewRecycling()

  override fun setEnabledAndRestart(enabled: Boolean) {
    val context = reactApplicationContext
    setEnabled(context, enabled)
    // Feature flags can only be set before React Native starts, so restart the app.
    val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)!!
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
    context.startActivity(intent)
    Runtime.getRuntime().exit(0)
  }

  companion object {
    private const val PREFS = "repro"
    private const val KEY = "enableViewRecycling"

    fun isEnabled(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(KEY, true)

    fun setEnabled(context: Context, enabled: Boolean) {
      context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean(KEY, enabled).commit()
    }
  }
}
