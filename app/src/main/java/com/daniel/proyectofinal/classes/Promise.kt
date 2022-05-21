package com.daniel.proyectofinal.classes

import android.os.Handler
import android.os.Looper
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

  fun <S>thenP(onFulfilled: (T) -> Promise<S>, onRejected: (Any?) -> Any? = this.defaultOnRejected): Promise<S> {
    return this.then(onFulfilled as (T) -> S, onRejected)
  }

  fun <S>then(onFulfilled: (T) -> S = { it as S }, onRejected: (Any?) -> Any? = this.defaultOnRejected): Promise<S> {

    if (this.state != Promise.States.PENDING)
      return Promise({ resolve, reject ->

        val onFulfilledWrapper = this.cW(onFulfilled as (Any?) -> Any?, reject)
        val onRejectedWrapper  = this.cW(onRejected, reject)

        if (this.state == Promise.States.FULFILLED) {
          val value = onFulfilledWrapper(this.result as T?)
          resolve(value)
        }
        else {
          if (onRejected == this.defaultOnRejected) {
            reject(this.result)
          }
          else {
            val value = onRejectedWrapper(this.result)
            resolve(value)
          }
        }

      })

    return Promise({ resolve, reject ->
      val onFulfilledWrapper = this.cW(onFulfilled as (Any?) -> Any?, reject)
      val onRejectedWrapper  = this.cW(onRejected, reject)

      this.thenCallbacks.add({ resolve(onFulfilledWrapper(it)) })

      if (onRejected == this.defaultOnRejected) {
        this.catchCallbacks.add({ reject(it) })
      } else {
        this.catchCallbacks.add({ resolve(onRejectedWrapper(it)) })
      }
    })

  }

  fun <S>catch(onRejected: (Any?) -> S): Promise<S> {
    return this.then(onRejected = onRejected)
  }

  fun <S>catchP(onRejected: (Any?) -> Promise<S>): Promise<S> {
    return this.catch(onRejected as (Any?) -> S)
  }

  fun finally(onFinally: () -> Unit): Promise<T> {
    this.then({ onFinally() }, { onFinally() })

    return Promise.resolve(this) as Promise<T>
  }

  private var isResolveRejectInvoked = false
  private var state = Promise.States.PENDING
  private var result: Any? = Unit

  private val defaultOnRejected: (Any?) -> Any? = { }

  private val thenCallbacks: MutableList<(T?) -> Any?> = mutableListOf()
  private val catchCallbacks: MutableList<(Any?) -> Any?> = mutableListOf()


  // Callback Wrapper
  private fun cW(callback: (Any?) -> Any?, reject: (Any?) -> Unit): (Any?) -> Any? {
    return fun(data: Any?): Any? {
      try {
        return callback(data)
      } catch (error: Throwable) {
        reject(error)
      }

      return Unit
    }
  }

  private fun resolveExec(value: Promise<T?>) {
    if (this.isResolveRejectInvoked) return
    this.isResolveRejectInvoked = true

    if (value == this) {
      this.reject(Exception("Chaining cycle detected for promise #<Promise>"))
    } else {
      value.then({ this.resolve(it) }, { this.reject(it) })
    }
  }

  private fun resolveExec(value: T?) {
    if (this.isResolveRejectInvoked) return
    this.isResolveRejectInvoked = true

    this.resolve(value)
  }

  private fun rejecExec(reason: Promise<Any?>) {
    if (this.isResolveRejectInvoked) return
    this.isResolveRejectInvoked = true

    if (reason == this) {
      this.reject(reason)
    } else {
      reason.then({ this.resolve(it as T?) }, { this.reject(it) })
    }
  }

  private fun rejectExec(reason: Any?) {
    if (this.isResolveRejectInvoked) return
    this.isResolveRejectInvoked = true

    this.reject(reason)
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
    this.setTimeout({
      this.thenCallbacks.forEach({ it(this.result as T?) })
    })
  }

  private fun invokePendingCatchCallbacks() {
    this.setTimeout({
      this.catchCallbacks.forEach({ it(this.result) })
    })
  }

  private fun setTimeout(callback: () -> Any?, time: Long = 0) {
    val handler = Handler(Looper.getMainLooper())
    val runnable = Runnable({ callback() })

    handler.postDelayed(runnable, time)
  }

}
