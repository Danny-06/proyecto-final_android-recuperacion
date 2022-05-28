package com.daniel.proyectofinal.fragments

import android.os.Bundle
import android.text.Html
import android.text.Spanned
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.daniel.proyectofinal.MainActivity
import com.daniel.proyectofinal.MainActivity.RecipeWithUserData
import com.daniel.proyectofinal.R
import com.daniel.proyectofinal.classes.Promise
import com.daniel.proyectofinal.classes.RecyclerViewAdapter
import com.daniel.proyectofinal.databinding.FragmentRecipesBinding
import com.daniel.proyectofinal.databinding.ItemRecipeBinding
import com.daniel.proyectofinal.models.Recipe
import com.daniel.proyectofinal.models.User
import com.squareup.picasso.Picasso
import java.text.DateFormat
import java.util.*


class RecipesFragment : Fragment() {

  private lateinit var binding: FragmentRecipesBinding
  private lateinit var activity: MainActivity

  private lateinit var user: User

  private lateinit var adapter: RecyclerViewAdapter<RecipeWithUserData>

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
    this.activity = this.getActivity() as MainActivity

    this.binding = FragmentRecipesBinding.inflate(layoutInflater)
    return this.binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    val fragment = this.activity.supportFragmentManager.fragments[0]
    this.activity.currentFragment = fragment

    val navController = NavHostFragment.findNavController(fragment)

    // Clear navigation stack
    navController.popBackStack(R.id.createRecipeFragment, true)
    navController.popBackStack(R.id.recipeDetailsFragment, true)

    this.binding.addRecipe.setOnClickListener {
      this.goToCreateRecipe()
    }

    this.activity.getUser()
    .thenP({ user ->
      if (user == null) {
        return@thenP Promise.reject()
      }

      this.user = user
      if (this.arguments?.getBoolean("own-recipes") == true) {
        this.binding.title.text = "Your Recipes"
        this.activity.getRecipesWithUserData(this.user)
      }
      else
        this.activity.getAllRecipes()
    })
    .then({ recipes ->
      // Sort by most recent recipe
      recipes.sortBy { -it.recipe.date }

      if (recipes.size != 0) {
        this.binding.recipesRecycler.isVisible = true
        this.binding.noRecipesMessage.isVisible = false
        this.displayRecipes(recipes)
      }
      else {
        this.binding.recipesRecycler.isVisible = false
        this.binding.noRecipesMessage.isVisible = true
      }
    })
  }

  // Functions

  private fun displayRecipes(recipes: MutableList<RecipeWithUserData>) {
    this.adapter = RecyclerViewAdapter(R.layout.item_recipe, recipes as ArrayList)
    this.binding.recipesRecycler.adapter = this.adapter
    this.binding.recipesRecycler.layoutManager = LinearLayoutManager(this.context, RecyclerView.VERTICAL, false)

    this.adapter.setOnItemClickListener { view, recipeWithUserData, index ->
      this.goToFragmentDetails(recipeWithUserData)
    }
    this.adapter.setOnItemLongClickListener { view, recipeWithUserData, index ->
      val user = recipeWithUserData.user
      val recipe = recipeWithUserData.recipe

      if (this.user.id != user.id) {
        this.activity.snackbar("You cannot delete a recipe that is not yours.")
        return@setOnItemLongClickListener true
      }

      this.deleteRecipe(user.id, recipe.id, index, recipes)

      true
    }
    this.adapter.setOnBindViewHolderListener { view, recipeWithUserData, index ->
      this.setRecipesLayout(recipeWithUserData, view)
    }
  }


  private fun deleteRecipe(userId: String, recipeId: String, index: Int, recipes: MutableList<RecipeWithUserData>) {
    AlertDialog.Builder(this.requireContext(), androidx.appcompat.R.style.Theme_AppCompat_Dialog_Alert)
    .setTitle("Remove Operation")
    .setMessage("Are you sure you want to remove this recipe?")
    .setPositiveButton("Ok") { _, _ ->
      this.activity.deleteRecipe(recipeId, userId)
      .addOnSuccessListener {
        recipes.removeAt(index)
        this.adapter.notifyItemRemoved(index)
      }
    }
    .setNegativeButton("Cancel") { _, _ -> }
    .setCancelable(true)
    .show()
  }

  private fun setRecipesLayout(recipeWithUserData: RecipeWithUserData, view: View) {
    val binding = ItemRecipeBinding.bind(view)

    val user = recipeWithUserData.user
    val recipe = recipeWithUserData.recipe

    binding.name.text =
      if (recipe.name.isNotEmpty())
        recipe.name
      else
        this.getHTMLFromString("<i>NO NAME WAS FOUND</i>")

    binding.author.text =
      if (this.user.name == user.name)
        this.getHTMLFromString("<b><i><u>Own Recipe</u></i></b>")
      else
        this.getHTMLFromString("<b><u>Author</u>:</b> <i>${user.name}</i>")

    binding.date.text = DateFormat.getDateInstance().format(recipe.date)

    if (recipe.thumbnail.isNotEmpty())
      Picasso.get().load(recipe.thumbnail).into(binding.thumbnail)
    else
      binding.thumbnail.setImageResource(R.drawable.image_placeholder)
  }

  private fun getHTMLFromString(text: String): Spanned {
    return Html.fromHtml(text, Html.FROM_HTML_MODE_COMPACT)
  }

  private fun goToFragmentDetails(recipeWithUserData: RecipeWithUserData) {
    this.goToRecipeDetails(Bundle().apply {
      this.putString("userID", recipeWithUserData.user.id)
      this.putString("recipeID", recipeWithUserData.recipe.id)
    })
  }

  private fun goToCreateRecipe(bundle: Bundle? = null) {
    NavHostFragment
    .findNavController(this)
    .navigate(R.id.action_recipesFragment_to_createRecipeFragment, bundle)
  }

  private fun goToProfile(bundle: Bundle? = null) {
    NavHostFragment
    .findNavController(this)
    .navigate(R.id.action_recipesFragment_to_profileFragment, bundle)
  }

  private fun goToRecipeDetails(bundle: Bundle? = null) {
    NavHostFragment
    .findNavController(this)
    .navigate(R.id.action_recipesFragment_to_recipeDetailsFragment, bundle)
  }

}
