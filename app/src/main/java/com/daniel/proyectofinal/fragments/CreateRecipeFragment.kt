package com.daniel.proyectofinal.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.daniel.proyectofinal.MainActivity
import com.daniel.proyectofinal.classes.Promise
import com.daniel.proyectofinal.databinding.FragmentCreateRecipeBinding
import com.daniel.proyectofinal.models.Recipe
import com.squareup.picasso.Picasso
import java.util.*


class CreateRecipeFragment : Fragment() {

  private lateinit var binding: FragmentCreateRecipeBinding
  private lateinit var activity: MainActivity

  private var recipe = Recipe()

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
    this.activity = this.getActivity() as MainActivity

    this.binding = FragmentCreateRecipeBinding.inflate(layoutInflater)
    return this.binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    this.binding.selectThumbnailBtn.setOnClickListener {
      this.selectThumbnail()
    }

    this.binding.createRecipeBtn.setOnClickListener {
      this.createRecipe()
    }
  }



  // Functions

  private fun createRecipe() {
    val recipeName  = this.binding.recipeNameInput.editText?.text.toString()
    val ingredients = this.binding.ingredientsInput.editText?.text.toString()
    val steps       = this.binding.stepsInput.editText?.text.toString()

    this.recipe = this.recipe.copy(name = recipeName, ingredients = ingredients, steps = steps)

    this.activity.addRecipe(this.recipe)
    .addOnFailureListener {
      this.activity.snackbar("Something went wrong. Try again later.")
    }
    .addOnSuccessListener {
      this.activity.goToFragment(RecipesFragment())
    }
  }

  private fun selectThumbnail() {
    this.activity.selectFile("image/*")
    .thenP({ imageUri ->
      this.activity.uploadFile(imageUri, "images/recipeImage - ${Calendar.getInstance().timeInMillis}")
    })
    .then({ imageFirebaseUri ->
      this.recipe = this.recipe.copy(thumbnail = imageFirebaseUri.toString())
      Picasso.get().load(this.recipe.thumbnail).into(this.binding.recipeThumbnail)
    })
  }

}
