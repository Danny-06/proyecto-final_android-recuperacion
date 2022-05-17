package com.daniel.proyectofinal.classes


class CustomEventTarget {

  val listener
  get() = this._listener

  private var _listener: (Any?) -> Unit = { }

  fun setListener(listener: (Any?) -> Unit) {
    this._listener = listener
  }

}
