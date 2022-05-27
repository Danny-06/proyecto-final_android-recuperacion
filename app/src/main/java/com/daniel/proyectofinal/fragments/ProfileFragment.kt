package com.daniel.proyectofinal.fragments

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.NavHostFragment
import com.daniel.proyectofinal.MainActivity
import com.daniel.proyectofinal.R
import com.daniel.proyectofinal.classes.Promise
import com.daniel.proyectofinal.databinding.FragmentProfileBinding
import com.daniel.proyectofinal.models.User
import com.squareup.picasso.Picasso


class ProfileFragment : Fragment() {

  private lateinit var binding: FragmentProfileBinding
  private lateinit var activity: MainActivity

  private lateinit var user: User

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
    this.activity = this.getActivity() as MainActivity

    this.binding = FragmentProfileBinding.inflate(layoutInflater)
    return this.binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    this.handleUserData()

    this.binding.changeProfileImageBtn.setOnClickListener {
      this.changeProfileImage()
    }

    this.binding.seeOwnRecipesBtn.setOnClickListener {
      this.goToOwnRecipes()
    }

    this.binding.seeAllRecipesBtn.setOnClickListener {
      this.activity.goToFragment(RecipesFragment())
    }

    this.binding.singOutBtn.setOnClickListener {
      this.activity.onSignOut()
    }
  }

  // Functions

  private fun handleUserData() {
    this.activity.getUser()
    .thenP({ user ->
      if (user == null) return@thenP Promise.reject()

      this.user = user

      this.binding.userName.text = this.user.name

      if (this.user.image.isEmpty())
        this.binding.profileImage.setImageResource(R.drawable.user_placeholder)
      else
        Picasso.get().load(this.user.image).into(this.binding.profileImage)

      this.activity.getRecipes()
    })
    .then({ recipes ->
      this.binding.recipesCount.text = "Recipes Made: ${recipes.size}"
    })
  }

  private fun changeProfileImage() {
    if (!this::user.isInitialized) return

    this.activity.selectFile("image/*")
    .thenP({ imageUri ->
      this.activity.changeProfileImage(this.user, imageUri)
    })
    .then({ downloadUri ->
      val request = Picasso.get().load(downloadUri)
      request.into(this.binding.profileImage)
      request.into(this.activity.toolbar.profileImage)
    })
  }

  private fun goToOwnRecipes() {
    this.activity.goToFragment(RecipesFragment(), Bundle().apply {
      this.putBoolean("own-recipes", true)
    })
  }

}
