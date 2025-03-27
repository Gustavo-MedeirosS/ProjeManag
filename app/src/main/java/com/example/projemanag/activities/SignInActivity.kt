package com.example.projemanag.activities

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import com.example.projemanag.R
import com.example.projemanag.databinding.ActivitySignInBinding
import com.example.projemanag.firebase.FirestoreHandler
import com.example.projemanag.models.User
import com.google.firebase.auth.FirebaseAuth

class SignInActivity : BaseActivity() {

    private var binding: ActivitySignInBinding? = null
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        auth = FirebaseAuth.getInstance()

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
        )

        setupActionBar()

        binding?.btnSignIn?.setOnClickListener { signInUser() }
    }

    private fun setupActionBar() {
        setSupportActionBar(binding?.toolbarSignInActivity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_black_color_back_24dp)
        }

        binding?.toolbarSignInActivity?.setNavigationOnClickListener { onBackPressed() }
    }

    private fun signInUser() {
        val email: String = binding?.etEmail?.text.toString().trim { it <= ' ' }
        val password: String = binding?.etPassword?.text.toString().trim { it <= ' ' }
        if (isFormValid(email, password)) {
            showProgressDialog()
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    hideProgressDialog()
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        FirestoreHandler().loadUserData(activity = this@SignInActivity)
                    } else {
                        Toast.makeText(this, "Authentication failed", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    fun signInSuccess(user: User) {
        Toast.makeText(this, "Authentication well done", Toast.LENGTH_SHORT).show()
        hideProgressDialog()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun isFormValid(email: String, password: String): Boolean {
        return when {
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