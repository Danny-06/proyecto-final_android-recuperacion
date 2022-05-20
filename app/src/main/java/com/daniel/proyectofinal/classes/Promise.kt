package com.daniel.proyectofinal.classes

import java.util.*
import kotlin.concurrent.schedule


class Promise<T> {

  constructor(executor: (resolve: (T?) -> Unit, reject: (Any?) -> Unit) -> Unit) {
    try {
      executor(this::resolveExec, this::rejectExec)
    } catch (error: Error) {
      this.rejectExec(error)
    }
  }

  private enum class States {
    PENDING,
    FULFILLED,
    REJECTED
  }

  companion object {
    fun resolve(value: Any? = Unit): Promise<Any?> {
      return Promise({ resolve, reject -> resolve(value) })
    }

    fun reject(reason: Any? = Unit): Promise<Any?> {
      return Promise({ resolve, reject -> reject(reason) })
    }
  }

  fun then(onFulfilled: (T?) -> Any?, onRejected: (Any?) -> Any? = this.defaultOnRejected): Promise<Any?> {

    if (this.state != Promise.States.PENDING)
      return Promise({ resolve, reject ->

        if (this.state == Promise.States.FULFILLED) {
          val value = onFulfilled(this.result as T?)
          resolve(value)
        }
        else {
          if (onRejected == this.defaultOnRejected) {
            reject(this.result)
          }
          else {
            val value = onRejected(this.result)
            resolve(value)
          }
        }

      })

    return Promise({ resolve, reject ->
      this.thenCallbacks.add({ resolve(onFulfilled(it)) })

      if (onRejected == this.defaultOnRejected) {
        this.catchCallbacks.add({ reject(it) })
      } else {
        this.catchCallbacks.add({ resolve(onRejected(it)) })
      }
    })

  }

  fun catch(onRejected: (Any?) -> Any?): Promise<Any?> {
    return this.then({ it }, onRejected)
  }

  fun finally(onFinally: (Any?) -> Any?): Promise<Any?> {
    this.then(onFinally, onFinally)

    return Promise.resolve(this)
  }

  private var isResolveRejectInvoked = false
  private var state = Promise.States.PENDING
  private var result: Any? = Unit

  private val defaultOnRejected: (Any?) -> Unit = { it }

  private val thenCallbacks: MutableList<(T?) -> Any?> = mutableListOf()
  private val catchCallbacks: MutableList<(Any?) -> Any?> = mutableListOf()

  private fun resolveExec(value: T?) {
    if (this.isResolveRejectInvoked) return
    this.isResolveRejectInvoked = true

    if (value == this) {
      this.reject(Error("Chaining cycle detected for promise #<Promise>"))
      return
    }

    if (value is Promise<*>) {
      value.then({ this.resolve(it as T?) }, { this.reject(it) })
    }
    else {
      this.resolve(value)
    }
  }

  private fun rejectExec(reason: Any?) {
    if (this.isResolveRejectInvoked) return
    this.isResolveRejectInvoked = true

    if (reason == this) {
      this.reject(reason)
      return
    }

    if (reason is Promise<*>) {
      reason.then({ this.resolve(it as T?) }, { this.reject(it) })
    }
    else {
      this.reject(reason)
    }
  }

  private fun resolve(value: T?) {
    this.state = Promise.States.FULFILLED
    this.result = value
    this.invokePendingThenCallbacks()
  }

  private fun reject(reason: Any?) {
    this.state = Promise.States.REJECTED
    this.result = reason
    this.invokePendingCatchCallbacks()
  }

  private fun invokePendingThenCallbacks() {
//    this.setTimeout({
      this.thenCallbacks.forEach({ it(this.result as T?) })
//    })
  }

  private fun invokePendingCatchCallbacks() {
//    this.setTimeout({
      this.catchCallbacks.forEach({ it(this.result) })
//    })
  }

  private fun setTimeout(callback: () -> Any?, time: Long = 0): Timer {
    val timer = Timer()
    timer.schedule(time) { callback() }

    return timer
  }

}
