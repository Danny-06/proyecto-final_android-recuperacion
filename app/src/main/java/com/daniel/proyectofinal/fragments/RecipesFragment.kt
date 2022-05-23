package com.daniel.proyectofinal.fragments

import android.os.Bundle
import android.text.Html
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
      this.displayRecipes(recipes)
    })
  }

  // Functions

  private fun displayRecipes(recipes: MutableList<RecipeWithUserData>) {
    this.adapter = RecyclerViewAdapter(R.layout.item_recipe, recipes as ArrayList)
    this.binding.recipesRecycler.adapter = this.adapter
    this.binding.recipesRecycler.layoutManager = LinearLayoutManager(this.context, RecyclerView.VERTICAL, false)

    this.adapter.setOnItemClickListener { view, recipe, index ->

    }
    this.adapter.setOnBindViewHolderListener { view, recipeWithUserData, index ->
      val binding = ItemRecipeBinding.bind(view)

      val userName = recipeWithUserData.user.name
      val recipeName = recipeWithUserData.recipe.name
      val recipeThumbnail = recipeWithUserData.recipe.thumbnail

      binding.name.text = recipeName
      binding.author.text =
        if (this.user.name == userName)
          Html.fromHtml("<b><i><u>Own Recipe</u></i></b>", Html.FROM_HTML_MODE_COMPACT)
        else
          Html.fromHtml("<u>Author:</u> <i>${userName}</i>", Html.FROM_HTML_MODE_COMPACT)

      Picasso.get().load(recipeThumbnail).into(binding.thumbnail)
    }
  }

}
