package com.daniel.proyectofinal.models

import java.io.Serializable

data class User(
  val id: String = "",
  val name: String = "",
  val image: String = ""
): Serializable
