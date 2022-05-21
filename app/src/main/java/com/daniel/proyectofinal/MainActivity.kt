package com.daniel.proyectofinal

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.daniel.proyectofinal.classes.CustomEventTarget
import com.daniel.proyectofinal.classes.Promise
import com.daniel.proyectofinal.databinding.ActivityMainBinding
import com.daniel.proyectofinal.databinding.ToolbarBinding
import com.daniel.proyectofinal.fragments.LoginFragment
import com.daniel.proyectofinal.fragments.RecipesFragment
import com.daniel.proyectofinal.fragments.RegisterFragment
import com.daniel.proyectofinal.models.Recipe
import com.daniel.proyectofinal.models.User
import com.google.android.gms.tasks.Task as GoogleTask
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import java.util.*


class MainActivity : AppCompatActivity() {

  private lateinit var binding: ActivityMainBinding
  private lateinit var toolbar: ToolbarBinding

  val fireAuth = Firebase.auth
  val db = Firebase.firestore
  val storage = FirebaseStorage.getInstance().reference

  private val usersPath = "users"
  private val userPath get() = "${this.usersPath}/${this.fireAuth.uid}"
  private val userRecipesPath get() = "${this.usersPath}/${this.fireAuth.uid}/recipes"

  private var resultLauncherEventTarget: CustomEventTarget<Uri>? = null

  private val resultLauncher = registerForActivityResult(StartActivityForResult()) {
    if (it.resultCode == Activity.RESULT_OK) {
      val activityResult = it

      val data = activityResult.data?.data
      this.resultLauncherEventTarget?.listener?.invoke(data)
    }

    this.resultLauncherEventTarget = null
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    this.binding = ActivityMainBinding.inflate(layoutInflater)
    this.setContentView(this.binding.root)

    this.toolbar = this.binding.toolbar

    // Check if user is logged in the application
    if (this.fireAuth.currentUser == null)
      this.goToFragment(RegisterFragment())
    else
      this.goToFragment(RecipesFragment())
  }




  // Functions

  fun snackbar(message: String, duration: Int = 2000) {
    Snackbar.make(this.binding.root, message, duration).show()
  }

  // Launch intent to let the the user choose a file
  fun selectFile(accept: String = "*/*"): Promise<Uri?> {
    if (this.resultLauncherEventTarget != null) return Promise.reject() as Promise<Uri?>

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

  fun register(email: String, password: String): GoogleTask<AuthResult> {
    return this.fireAuth.createUserWithEmailAndPassword(email, password)
  }

  fun login(email: String, password: String): GoogleTask<AuthResult> {
    return this.fireAuth.signInWithEmailAndPassword(email, password)
  }

  fun signOut() {
    this.fireAuth.signOut()
    this.goToFragment(LoginFragment())
  }

  fun onRegisterOrLogin() {
    this.goToFragment(RecipesFragment())
    this.toolbar.profileImageParent.isVisible = true

    this.getUser()
    .then({ user ->
      this.toolbar.profileImageParent.visibility = View.VISIBLE
      Picasso.get().load(user!!.image).into(this.toolbar.profileImage)
    })
  }

  fun onSignOut() {
    this.signOut()
    this.goToFragment(LoginFragment())
    this.toolbar.profileImageParent.visibility = View.INVISIBLE
  }

  fun getUser(): Promise<User> {
    return Promise({ resolve, reject ->

      this.db.document(this.userPath).get()
      .addOnFailureListener(reject)
      .addOnSuccessListener {
        val user = it.toObject(User::class.java)
        resolve(user)
      }

    })
  }

  fun getUsers(): Promise<MutableList<User>> {
    return Promise({ resolve, reject ->

      this.db.collection(this.usersPath).get()
      .addOnFailureListener(reject)
      .addOnSuccessListener {
        val users = it.toObjects(User::class.java)
        resolve(users)
      }

    })
  }

  fun addUser(user: User): GoogleTask<Void> {
    return this.updateUser(user)
  }

  fun updateUser(user: User): GoogleTask<Void> {
    val userPath = "${this.usersPath}/${user.id}"
    return this.db.document(userPath).set(user)
  }

  fun getRecipes(): Promise<MutableList<Recipe>> {
    return this.getRecipes(this.userRecipesPath)
  }

  fun getRecipes(path: String): Promise<MutableList<Recipe>> {
    return Promise({ resolve, reject ->

      this.db.collection(path).get()
      .addOnFailureListener(reject)
      .addOnSuccessListener {
        val recipes = it.toObjects(Recipe::class.java)
        resolve(recipes)
      }

    })
  }

  // Recipes from all of the users
  fun getAllRecipes(): Promise<MutableList<Recipe>> {
    return Promise({ resolve, reject ->

      var userRecipeCounter = 0
      val recipes: MutableList<Recipe> = mutableListOf()

      this.getUsers()
      .then({ users ->

        users!!.forEach { user ->
          this.getRecipes("${this.usersPath}/${user.id}/recipes")
          .then({ userRecipes ->
            recipes.addAll(userRecipes as Collection<Recipe>)
            userRecipeCounter++

            if (userRecipeCounter == users.size) resolve(recipes)
          })
        }

      })

    })
  }

  fun addRecipe(recipe: Recipe): GoogleTask<DocumentReference> {
    return this.db.collection(this.userRecipesPath).add(recipe)
    .addOnSuccessListener {
      val recipeCopy = recipe.copy(id = it.id)
      this.updateRecipe(recipeCopy)
    }
  }

  fun updateRecipe(recipe: Recipe): GoogleTask<Void> {
    val recipePath = "${this.userRecipesPath}/${recipe.id}"
    return this.db.document(recipePath).set(recipe)
  }

  fun deleteRecipe(recipe: Recipe): GoogleTask<Void> {
    val recipePath = "${this.userRecipesPath}/${recipe.id}"
    return this.db.document(recipePath).delete()
  }

  fun changeProfileImage(user: User, image: Uri): Promise<Any?> {
    return this.uploadFile(image, "images/profile - ${Calendar.getInstance().timeInMillis}")
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

  fun goToFragment(fragment: Fragment, bundle: Bundle? = null) {
    fragment.arguments = bundle

    this.supportFragmentManager.beginTransaction()
    .replace(R.id.nav_host_fragment_content_main, fragment)
    .commit()
  }

}
