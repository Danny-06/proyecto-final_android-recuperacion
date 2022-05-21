package com.daniel.proyectofinal.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.daniel.proyectofinal.MainActivity
import com.daniel.proyectofinal.R
import com.daniel.proyectofinal.classes.RecyclerViewAdapter
import com.daniel.proyectofinal.databinding.FragmentRecipesBinding
import com.daniel.proyectofinal.models.Recipe
import java.util.ArrayList


class RecipesFragment : Fragment() {

  private lateinit var binding: FragmentRecipesBinding
  private lateinit var activity: MainActivity

  private lateinit var adapter: RecyclerViewAdapter<Recipe>

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
    this.activity = this.getActivity() as MainActivity

    this.binding = FragmentRecipesBinding.inflate(layoutInflater)
    return this.binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    this.binding.addRecipe.setOnClickListener {
      this.activity.onSignOut()
    }
  }

  private fun displayRecipes() {
    this.activity.getAllRecipes()
    .then<Unit>({ recipes ->
      recipes as ArrayList
      this.binding.recipesRecycler.adapter = RecyclerViewAdapter(R.layout.item_recipe, recipes)
    })
  }

}
