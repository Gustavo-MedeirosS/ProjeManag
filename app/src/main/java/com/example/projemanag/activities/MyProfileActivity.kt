package com.example.projemanag.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.projemanag.R
import com.example.projemanag.databinding.ActivityMyProfileBinding
import com.example.projemanag.firebase.FirestoreHandler
import com.example.projemanag.models.User
import com.example.projemanag.utils.Constants
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.IOException

class MyProfileActivity : BaseActivity() {

    private var binding: ActivityMyProfileBinding? = null
    private var mSelectedImageFileUri: Uri? = null
    private var mProfileImageUrl: String = ""
    private lateinit var mUserDetails: User

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMyProfileBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        setupActionBar()

        FirestoreHandler().loadUserData(activity = this@MyProfileActivity)

        binding?.ivUserImageMyProfile?.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_MEDIA_IMAGES
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                Constants.showImageChooser(activity = this)
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_MEDIA_IMAGES),
                    Constants.READ_STORAGE_REQUEST_CODE
                )
            }
        }

        binding?.btnUpdate?.setOnClickListener {
            updateUserProfileData()
        }
    }

    private fun setupActionBar() {
        setSupportActionBar(binding!!.toolbarMyProfileActivity)

        val actionBar = supportActionBar

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
            actionBar.title = resources.getString(R.string.my_profile)
        }

        binding!!.toolbarMyProfileActivity.setNavigationOnClickListener { onBackPressed() }
    }

    @SuppressLint("SetTextI18n")
    fun setUserDataInUI(user: User) {
        mUserDetails = user

        Glide
            .with(this)
            .load(user.image)
            .centerCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(findViewById(R.id.iv_user_image_my_profile))

        binding?.etName?.setText(user.name)
        binding?.etEmail?.setText(user.email)
        if (user.mobile != 0L) {
            binding?.etMobile?.setText(user.mobile.toString())
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

    private fun updateUserProfileData() {
        val user = HashMap<String, Any>()
        var anyChangesMade = false

        if (mSelectedImageFileUri != null) {
            uploadUserImage()
        }

        if (mProfileImageUrl.isNotEmpty() && mProfileImageUrl != mUserDetails.image) {
            user[Constants.IMAGE] = mProfileImageUrl
            anyChangesMade = true
        }

        if (
            binding?.etName?.text.toString().trim().isNotEmpty() &&
            binding?.etName?.text.toString() != mUserDetails.name
        ) {
            user[Constants.NAME] = binding?.etName?.text.toString().trim()
            anyChangesMade = true
        }

        if (binding?.etMobile?.text.toString().trim() != mUserDetails.mobile.toString()) {
            user[Constants.MOBILE] = binding?.etMobile?.text.toString().trim().toLong()
            anyChangesMade = true
        }

        if (anyChangesMade) {
            showProgressDialog()
            FirestoreHandler().updateUserProfileData(activity = this@MyProfileActivity, user = user)
        }
    }

    private fun uploadUserImage() {
        showProgressDialog()

        val sRef: StorageReference =
            FirebaseStorage.getInstance().reference.child(
                "USER_IMAGE" + System.currentTimeMillis() + "." + Constants.getFileExtension(
                    activity = this,
                    uri = mSelectedImageFileUri!!
                )
            )

        sRef.putFile(mSelectedImageFileUri!!)
            .addOnSuccessListener { taskSnapshot ->
                Log.d(
                    "Firebase Image URL",
                    taskSnapshot.metadata!!.reference!!.downloadUrl.toString()
                )

                taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener { uri ->
                    Log.d(
                        "Downloadable Image URL",
                        uri.toString()
                    )
                    mProfileImageUrl = uri.toString()

                    updateUserProfileData()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
                hideProgressDialog()
            }

    }

    fun profileUpdateSuccess() {
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == Constants.READ_STORAGE_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Constants.showImageChooser(activity = this)
            }
        } else {
            Toast.makeText(
                this,
                "Ops, you denied the permission for storage. You can allow it in App Settings",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (
            resultCode == Activity.RESULT_OK &&
            requestCode == Constants.PICK_IMAGE_REQUEST_CODE &&
            data!!.data != null
        ) {
            mSelectedImageFileUri = data.data
            try {
                Glide
                    .with(this)
                    .load(mSelectedImageFileUri)
                    .centerCrop()
                    .placeholder(R.drawable.ic_user_place_holder)
                    .into(findViewById(R.id.iv_user_image_my_profile))
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}