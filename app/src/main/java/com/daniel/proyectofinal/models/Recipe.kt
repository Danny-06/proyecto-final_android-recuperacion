package com.daniel.proyectofinal.models

import java.io.Serializable

data class Recipe(
  val id: String = "",
  val name: String = "",
  val thumbnail: String = "",
  val ingredients: String = "",
  val steps: String = "",
  val date: Long = 0
): Serializable
