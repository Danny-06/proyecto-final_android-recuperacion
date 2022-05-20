package com.daniel.proyectofinal.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.daniel.proyectofinal.MainActivity
import com.daniel.proyectofinal.databinding.FragmentLoginBinding


class LoginFragment : Fragment() {

  private lateinit var binding: FragmentLoginBinding
  private lateinit var activity: MainActivity

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
    this.activity = this.getActivity() as MainActivity

    this.binding = FragmentLoginBinding.inflate(layoutInflater)
    return this.binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    this.binding.loginBtn.setOnClickListener({
      this.login()
    })

    this.binding.goToRegister.setOnClickListener({
      this.activity.goToFragment(RegisterFragment())
    })
  }

  // Functions

  fun login() {
    val email    = this.binding.email.editText?.text.toString()
    val password = this.binding.password.editText?.text.toString()

    if (email.isEmpty()) {
      this.activity.snackbar("Email field cannot be empty.")
      return
    }

    if (password.isEmpty()) {
      this.activity.snackbar("Password field cannot be empty.")
      return
    }

    this.activity.login(email, password)
    .addOnFailureListener {
      this.activity.snackbar("There was an error when trying to login the account. Check if the email provided is valid.", 4000)
    }
    .addOnSuccessListener {
      this.activity.goToFragment(RecipesFragment())
    }
  }

}
