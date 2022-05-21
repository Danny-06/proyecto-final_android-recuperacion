package com.daniel.proyectofinal.classes

import android.os.Handler
import android.os.Looper


interface TimeoutHandler {
  fun cancel()
}

fun setTimeout(callback: () -> Any?, time: Long = 0): TimeoutHandler {
  val handler = Handler(Looper.getMainLooper())
  val runnable = Runnable({ callback() })

  handler.postDelayed(runnable, time)

  return object: TimeoutHandler {
    override fun cancel() {
      handler.removeCallbacks(runnable)
    }
  }
}

fun setInterval(callback: () -> Any?, time: Long = 0): TimeoutHandler {
  val handler = Handler(Looper.getMainLooper())

  var runnable: Runnable? = null
  runnable = Runnable({
    callback()
    handler.postDelayed(runnable!!, time)
  })

  handler.postDelayed(runnable, time)

  return object: TimeoutHandler {
    override fun cancel() {
      handler.removeCallbacks(runnable)
    }
  }
}
