package com.daniel.proyectofinal.models

import java.io.Serializable

data class Recipe(
  val id: String = "",
  val name: String = "",
  val thumbnail: String = "",
  val ingredients: MutableList<String> = mutableListOf(),
  val steps: MutableList<String> = mutableListOf()
): Serializable
