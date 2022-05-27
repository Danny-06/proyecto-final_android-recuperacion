package com.daniel.proyectofinal.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import com.daniel.proyectofinal.MainActivity
import com.daniel.proyectofinal.R
import com.daniel.proyectofinal.classes.Promise
import com.daniel.proyectofinal.databinding.FragmentRecipeDetailsBinding
import com.daniel.proyectofinal.models.Recipe
import com.daniel.proyectofinal.models.User
import com.squareup.picasso.Picasso


class RecipeDetailsFragment : Fragment() {

  private lateinit var binding: FragmentRecipeDetailsBinding
  private lateinit var activity: MainActivity

  private lateinit var user: User

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
    this.activity = this.getActivity() as MainActivity

    this.binding = FragmentRecipeDetailsBinding.inflate(layoutInflater)
    return this.binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    this.activity.getUser()
    .thenP({ user ->
      if (user == null) {
        return@thenP Promise.reject<Recipe>()
      }

      this.user = user
      this.getRecipeFromArguments()
    })
    .then({ recipe ->
      this.binding.apply {
        this.recipeName.text = recipe.name
        if (recipe.thumbnail.isEmpty()) {
          this.recipeThumbnail.setImageResource(R.drawable.image_placeholder)
        } else {
          Picasso.get().load(recipe.thumbnail).into(this.recipeThumbnail)
        }

        this.ingredients.text = recipe.ingredients
        this.steps.text = recipe.steps
      }
    })
  }

  // Functions

  private fun alert(message: String, callback: () -> Unit = { }) {
    AlertDialog.Builder(this.requireContext(), androidx.appcompat.R.style.Theme_AppCompat_Dialog_Alert)
    .setTitle("Error")
    .setMessage(message)
    .setPositiveButton("Ok") { _, _ -> callback() }
    .setCancelable(false)
    .show()
  }

  private fun handleRecipeNotFound() {
    this.alert("This task doesn't exist or was deleted. You will be redirected to Main Page.") {
      this.activity.goToFragment(RecipesFragment())
    }
  }

  private fun getRecipeFromArguments(): Promise<Recipe> {
    return Promise({ resolve, reject ->
      this.arguments?.apply {
        val userID   = this.getString("userID")
        val recipeID = this.getString("recipeID")

        if (userID == null || recipeID == null) {
          this@RecipeDetailsFragment.handleRecipeNotFound()
          reject("No recipe found")
          return@apply
        }

        this@RecipeDetailsFragment
        .activity.getRecipe(User(id = userID), recipeID)
        .then({ recipe -> resolve(recipe) })
        .catch(reject)
      }
    })
  }

}
