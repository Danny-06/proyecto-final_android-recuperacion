package com.daniel.proyectofinal.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.daniel.proyectofinal.MainActivity
import com.daniel.proyectofinal.R
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
  }

}
