package com.daniel.proyectofinal.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.daniel.proyectofinal.MainActivity
import com.daniel.proyectofinal.databinding.FragmentRegisterBinding
import com.daniel.proyectofinal.models.User
import java.util.*


class RegisterFragment : Fragment() {

  private lateinit var binding: FragmentRegisterBinding
  private lateinit var activity: MainActivity

  private var userImage: String = ""

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
    this.activity = this.getActivity() as MainActivity

    this.binding = FragmentRegisterBinding.inflate(layoutInflater)
    return this.binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    this.binding.changeProfileImageBtn.setOnClickListener({
      this.activity.selectFile("image/*")
      .then({
        if (it == null) return@then null

        this.activity.uploadFile(it, "images/profile - ${Calendar.getInstance().timeInMillis}")
      })
      .then({
        this.userImage = it.toString()
        this.activity.snackbar(this.userImage)
      })
    })
  }

}
