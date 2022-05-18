package com.daniel.proyectofinal.classes


class Promise<T> {

  private enum class States {
    PENDING,
    FULFILLED,
    REJECTED
  }

  constructor(executor: (resolve: (T?) -> Unit, reject: (Any?) -> Unit) -> Unit) {
    try {
      executor(this::resolveExec, this::rejectExec)
    } catch (error: Error) {
      this.rejectExec(error)
    }
  }

  companion object {
    fun resolve(value: Any?): Promise<Any?> {
      return Promise({ resolve, reject -> resolve(value) })
    }

    fun reject(reason: Any?): Promise<Any?> {
      return Promise({ resolve, reject -> reject(reason) })
    }
  }

  private var isResolveRejectInvoked = false
  private var state = Promise.States.PENDING
  private var result: Any? = Unit

  private val defaultOnRejected: (Any?) -> Unit = { it }

  private var thenCallbacks: MutableList<(T?) -> Any?> = mutableListOf()
  private var catchCallbacks: MutableList<(Any?) -> Any?> = mutableListOf()

  private fun resolveExec(value: T?) {
    if (this.isResolveRejectInvoked) return
    this.isResolveRejectInvoked = true

    if (value !is Promise<*>) {
      this.resolve(value)
      return
    }

    if (value == this) {
      this.reject(Error("Chaining cycle detected for promise #<Promise>"))
    }
    else {
      this.then({ this.resolve(it) }, { this.reject(it) })
    }
  }

  private fun rejectExec(reason: Any?) {
    if (this.isResolveRejectInvoked) return
    this.isResolveRejectInvoked = true

    if (reason !is Promise<*>) {
      this.reject(reason)
      return
    }

    this.then({ this.resolve(it) }, { this.reject(it) })
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
            val reason = onRejected(this.result)
            resolve(reason)
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

  fun invokePendingThenCallbacks() {
    this.thenCallbacks.forEach({ it(this.result as T?) })
  }

  fun invokePendingCatchCallbacks() {
    this.catchCallbacks.forEach({ it(this.result) })
  }

}
