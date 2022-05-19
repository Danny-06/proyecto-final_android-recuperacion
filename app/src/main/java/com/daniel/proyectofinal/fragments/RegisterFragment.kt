package com.daniel.proyectofinal.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.daniel.proyectofinal.MainActivity
import com.daniel.proyectofinal.R
import com.daniel.proyectofinal.classes.Promise
import com.daniel.proyectofinal.databinding.FragmentRegisterBinding
import com.daniel.proyectofinal.models.User
import com.squareup.picasso.Picasso
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
      .then({ fileSystemUri ->
        if (fileSystemUri == null) return@then Promise.reject(null)

        this.activity.uploadFile(fileSystemUri, "images/profile - ${Calendar.getInstance().timeInMillis}")
      })
      .then({ imgUri ->
        this.userImage = imgUri.toString()
        Picasso.get().load(this.userImage).into(this.binding.profileImage)
      })
    })

    this.binding.registerBtn.setOnClickListener({
      val userName = this.binding.userName.editText?.text.toString()
      val email    = this.binding.email.editText?.text.toString()
      val password = this.binding.password.editText?.text.toString()

      this.activity.register(email, password)
      .addOnSuccessListener {
        val uid = this.activity.fireAuth.currentUser?.uid.toString()
        val user = User(uid, userName, this.userImage)
        this.activity.addUser(user)
      }
      .addOnFailureListener {
        this.activity.snackbar("There was an error when trying to register the account. Try again later", 4000)
      }
    })
  }

}
