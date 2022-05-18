package com.daniel.proyectofinal

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.fragment.app.Fragment
import com.daniel.proyectofinal.classes.CustomEventTarget
import com.daniel.proyectofinal.classes.Promise
import com.daniel.proyectofinal.databinding.ActivityMainBinding
import com.daniel.proyectofinal.fragments.RecipesFragment
import com.daniel.proyectofinal.fragments.RegisterFragment
import com.daniel.proyectofinal.models.Recipe
import com.daniel.proyectofinal.models.User
import com.google.android.gms.tasks.Task as GoogleTask
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.util.*
import kotlin.concurrent.schedule
import kotlin.concurrent.scheduleAtFixedRate


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

    if (this.fireAuth.currentUser == null)
      this.goToFragment(RegisterFragment())
    else
      this.goToFragment(RecipesFragment())
  }





  // Functions

  fun snackbar(message: String, duration: Int = 2000) {
    Snackbar.make(this.binding.root, message, duration).show()
  }

  fun setTimeout(callback: () -> Any?, time: Long = 0): Timer {
    val timer = Timer()
    timer.schedule(time) { callback() }

    return timer
  }

  fun setInterval(callback: () -> Any?, time: Long = 0): Timer {
    val timer = Timer()
    timer.scheduleAtFixedRate(time, time) { callback() }

    return timer
  }

  // Launch intent to let the the user choose a file
  fun selectFile(accept: String = "*/*"): Promise<Uri> {
    if (this.resultLauncherEventTarget != null) return Promise.resolve(Uri.EMPTY) as Promise<Uri>

    val intent = Intent().apply {
      this.type = accept
      this.action = Intent.ACTION_GET_CONTENT
    }

    val intent2 = Intent.createChooser(intent, "Select File from here...")

    resultLauncher.launch(intent2)

    val eventTarget = CustomEventTarget<Uri>()
    this.resultLauncherEventTarget = eventTarget

    return Promise({ resolve, reject ->
      eventTarget.setListener(resolve)
    })
  }

  fun login(email: String, password: String) {
    this.fireAuth.signInWithEmailAndPassword(email, password)
  }

  fun signOut() {
    this.fireAuth.signOut()
  }

  fun getUser(): Promise<User> {
    val userPath = "${this.usersPath}/${this.fireAuth.uid}"

    return Promise({ resolve, reject ->

      this.db.document(userPath).get()
      .addOnSuccessListener {
        val user = it.toObject(User::class.java)
        resolve(user)
      }
      .addOnFailureListener(reject)

    })
  }

  fun updateUser(user: User): GoogleTask<Void> {
    val userPath = "${this.usersPath}/${user.id}"
    return this.db.document(userPath).set(user)
  }

  fun getRecipes(): Promise<MutableList<Recipe>> {
    return Promise({ resolve, reject ->

      this.db.collection(this.recipesPath).get()
      .addOnSuccessListener {
        val recipes = it.toObjects(Recipe::class.java)
        resolve(recipes)
      }
      .addOnFailureListener(reject)

    })
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

  fun changeProfileImage(user: User, image: Uri): Promise<Any?> {
    return this.uploadFile(image, "images/${this.fireAuth.currentUser?.uid}")
    .then({
      val copyUser = user.copy(image = it.toString())
      this.updateUser(copyUser)
    })
  }

  fun uploadFile(file: Uri, path: String): Promise<Uri> {
    val imageRef = this.storage.child(path)

    return Promise({ resolve, reject ->
      imageRef.putFile(file)
      .addOnFailureListener(reject)
      .addOnSuccessListener {
        imageRef.downloadUrl.addOnSuccessListener(resolve)
      }
    })
  }

  fun goToFragment(fragmentInstance: Fragment) {
    this.supportFragmentManager.beginTransaction().apply {
      this.replace(R.id.nav_host_fragment_content_main, fragmentInstance)
      this.commit()
    }
  }

}
