package com.daniel.proyectofinal.fragments

import android.os.Bundle
import android.text.Html
import android.text.Spanned
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
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
import java.util.ArrayList


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

    this.binding.addRecipe.setOnClickListener {
      this.activity.goToFragment(CreateRecipeFragment())
    }

    this.activity.getUser()
    .thenP({ user ->
      if (user == null) {
        return@thenP Promise.reject()
      }

      this.user = user
      this.activity.getAllRecipes()
    })
    .then({ recipes ->
      if (recipes.size != 0)
        this.displayRecipes(recipes)
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

      this.activity.goToFragment(RecipeDetailsFragment(), Bundle().apply {
        this.putString("recipeID", recipeWithUserData.recipe.id)
      })

    }
    this.adapter.setOnBindViewHolderListener { view, recipeWithUserData, index ->
      val binding = ItemRecipeBinding.bind(view)

      val userName = recipeWithUserData.user.name
      val recipeName = recipeWithUserData.recipe.name
      val recipeThumbnail = recipeWithUserData.recipe.thumbnail

      binding.name.text =
        if (recipeName.isNotEmpty())
          recipeName
        else
          this.getHTMLFromString("<i>NO NAME WAS FOUND</i>")

      binding.author.text =
        if (this.user.name == userName)
          this.getHTMLFromString("<b><i><u>Own Recipe</u></i></b>")
        else
          this.getHTMLFromString("<u>Author:</u> <i>${userName}</i>")

      if (recipeThumbnail.isNotEmpty())
        Picasso.get().load(recipeThumbnail).into(binding.thumbnail)
      else
        binding.thumbnail.setImageResource(R.drawable.image_placeholder)
    }
  }

  private fun getHTMLFromString(text: String): Spanned {
    return Html.fromHtml(text, Html.FROM_HTML_MODE_COMPACT)
  }

}
