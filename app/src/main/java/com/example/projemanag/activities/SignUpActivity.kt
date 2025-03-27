package com.example.projemanag.activities

import android.os.Bundle
import android.text.TextUtils
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import com.example.projemanag.R
import com.example.projemanag.databinding.ActivitySignUpBinding
import com.example.projemanag.firebase.FirestoreHandler
import com.example.projemanag.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class SignUpActivity : BaseActivity() {

    private var binding: ActivitySignUpBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
        )

        setupActionBar()

        binding?.btnSignUp?.setOnClickListener { createUser() }
    }

    private fun setupActionBar() {
        setSupportActionBar(binding?.toolbarSignUpActivity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_black_color_back_24dp)
        }

        binding?.toolbarSignUpActivity?.setNavigationOnClickListener { onBackPressed() }
    }

    private fun createUser() {
        val name: String = binding?.etName?.text.toString().trim { it <= ' ' }
        val email: String = binding?.etEmail?.text.toString().trim { it <= ' ' }
        val password: String = binding?.etPassword?.text.toString().trim { it <= ' ' }

        if (isFormValid(name, email, password)) {
            showProgressDialog()

            FirebaseAuth.getInstance()
                .createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    hideProgressDialog()
                    if (task.isSuccessful) {
                        val firebaseUser: FirebaseUser = task.result.user!!
                        val registeredEmail = firebaseUser.email!!
                        val user = User(
                            id = firebaseUser.uid,
                            name = name,
                            email = registeredEmail
                        )
                        FirestoreHandler().createUser(activity = this, userInfo = user)
                    } else {
                        Toast.makeText(this, task.exception!!.message, Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    fun userRegisteredSuccess() {
        Toast.makeText(this, "Successful sign up", Toast.LENGTH_SHORT).show()
        hideProgressDialog()
        FirebaseAuth.getInstance().signOut()
        finish()
    }

    private fun isFormValid(name: String, email: String, password: String): Boolean {
        return when {
            TextUtils.isEmpty(name) -> {
                showErrorSnackBar(message = "Please enter a name")
                false
            }

            TextUtils.isEmpty(email) -> {
                showErrorSnackBar(message = "Please enter an email")
                false
            }

            TextUtils.isEmpty(password) -> {
                showErrorSnackBar(message = "Please enter a password")
                false
            }

            else -> true
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}