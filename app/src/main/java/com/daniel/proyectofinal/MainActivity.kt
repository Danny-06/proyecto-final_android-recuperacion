package com.daniel.proyectofinal

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.fragment.app.Fragment
import com.daniel.proyectofinal.classes.CustomEventTarget
import com.daniel.proyectofinal.databinding.ActivityMainBinding
import com.daniel.proyectofinal.fragments.LoginFragment
import com.daniel.proyectofinal.models.Recipe
import com.daniel.proyectofinal.models.User
import com.google.android.gms.tasks.Task as GoogleTask
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage


class MainActivity : AppCompatActivity() {

  private lateinit var binding: ActivityMainBinding
  private val fireAuth = Firebase.auth
  private val db = Firebase.firestore
  private val storage = FirebaseStorage.getInstance().reference

  private val usersPath = "users"
  private val recipesPath get() = "${this.usersPath}/${this.fireAuth.uid}/recipes"

  private var resultLauncherEventTarget: CustomEventTarget<Uri>? = null

  private val resultLauncher = registerForActivityResult(StartActivityForResult()) {
    if (it.resultCode != Activity.RESULT_OK) return@registerForActivityResult

    val activityResult = it

    if (activityResult.resultCode == Activity.RESULT_OK) {
      val data = activityResult.data?.data
      this.resultLauncherEventTarget?.listener?.invoke(data)
    }

    this.resultLauncherEventTarget = null
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    this.binding = ActivityMainBinding.inflate(layoutInflater)
    this.setContentView(this.binding.root)

    this.goToFragment(LoginFragment())
  }





  // Functions

  fun snackbar(message: String, duration: Int = 2000) {
    Snackbar.make(this.binding.root, message, duration).show()
  }

  // Launch intent to let the the user choose a file
  fun selectFile(accept: String = "*/*"): CustomEventTarget<Uri>? {
    if (this.resultLauncherEventTarget != null) return null

    val intent = Intent().apply {
      this.type = accept
      this.action = Intent.ACTION_GET_CONTENT
    }

    val intent2 = Intent.createChooser(intent, "Select File from here...")

    resultLauncher.launch(intent2)

    val eventTarget = CustomEventTarget<Uri>()
    this.resultLauncherEventTarget = eventTarget

    return eventTarget
  }

  fun login(email: String, password: String) {
    this.fireAuth.signInWithEmailAndPassword(email, password)
  }

  fun signOut() {
    this.fireAuth.signOut()
  }

  fun getUser(): CustomEventTarget<User> {
    val userEventTarget = CustomEventTarget<User>()

    val userPath = "${this.usersPath}/${this.fireAuth.uid}"

    this.db.document(userPath).get()
    .addOnSuccessListener {
      val user = it.toObject(User::class.java)
      userEventTarget.listener(user)
    }

    return userEventTarget
  }

  fun updateUser(user: User): GoogleTask<Void> {
    val userPath = "${this.usersPath}/${user.id}"
    return this.db.document(userPath).set(user)
  }

  fun getRecipes(): CustomEventTarget<MutableList<Recipe>> {
    val recipesEventTarget = CustomEventTarget<MutableList<Recipe>>()

    this.db.collection(this.recipesPath).get()
    .addOnSuccessListener {
      val recipes = it.toObjects(Recipe::class.java)
      recipesEventTarget.listener(recipes)
    }

    return recipesEventTarget
  }

  fun addRecipe(recipe: Recipe): GoogleTask<DocumentReference> {
    return this.db.collection(this.recipesPath).add(recipe)
    .addOnSuccessListener {
      val recipeCopy = recipe.copy(id = it.id)
      this.updateRecipe(recipeCopy)
    }
  }

  fun updateRecipe(recipe: Recipe): GoogleTask<Void> {
    val recipePath = "${this.recipesPath}/${recipe.id}"
    return this.db.document(recipePath).set(recipe)
  }

  fun deleteRecipe(recipe: Recipe): GoogleTask<Void> {
    val recipePath = "${this.recipesPath}/${recipe.id}"
    return this.db.document(recipePath).delete()
  }

  fun changeProfileImage(user: User, image: Uri) {
    this.uploadFile(image, "images/${this.fireAuth.currentUser?.uid}")
    .setListener {
      val copyUser = user.copy(image = it.toString())
      this.updateUser(copyUser)
    }
  }

  fun uploadFile(file: Uri, path: String): CustomEventTarget<Uri> {

    val imageRef = this.storage.child(path)

    val customEventTarget = CustomEventTarget<Uri>()

    imageRef
    .putFile(file)
    .addOnFailureListener() {
      this.snackbar("There was an error uploading the file")
      return@addOnFailureListener
    }
    .addOnSuccessListener {
      imageRef.downloadUrl.addOnSuccessListener {
        customEventTarget.listener(it)
      }
    }

    return customEventTarget

  }

  fun goToFragment(fragmentInstance: Fragment) {
    this.supportFragmentManager.beginTransaction().apply {
      this.replace(R.id.nav_host_fragment_content_main, fragmentInstance)
      this.commit()
    }
  }

}
