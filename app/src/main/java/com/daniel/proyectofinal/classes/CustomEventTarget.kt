package com.daniel.proyectofinal.classes


class CustomEventTarget<T> {

  val listener
  get() = this._listener

  private var _listener: (T?) -> Unit = { }

  fun setListener(listener: (T?) -> Unit) {
    this._listener = listener
  }

}
