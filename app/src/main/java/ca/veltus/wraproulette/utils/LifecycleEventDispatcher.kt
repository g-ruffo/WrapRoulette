package ca.veltus.wraproulette.utils

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner


class LifecycleEventDispatcher(
    lifecycleOwner: LifecycleOwner,
    val onCreate: () -> Unit = {},
    val onStart: () -> Unit = {},
    val onResume: () -> Unit = {},
    val onPause: () -> Unit = {},
    val onStop: () -> Unit = {},
    val onDestroy: () -> Unit = {}
) : DefaultLifecycleObserver {

    init {
        lifecycleOwner.lifecycle.addObserver(this)
    }

    override fun onCreate(owner: LifecycleOwner) = onCreate()
    override fun onStart(owner: LifecycleOwner) = onStart()
    override fun onResume(owner: LifecycleOwner) = onResume()
    override fun onPause(owner: LifecycleOwner) = onPause()
    override fun onStop(owner: LifecycleOwner) = onStop()
    override fun onDestroy(owner: LifecycleOwner) = onDestroy()
}