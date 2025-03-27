package com.example.projemanag.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.projemanag.R
import com.example.projemanag.databinding.ActivityCreateBoardBinding
import com.example.projemanag.firebase.FirestoreHandler
import com.example.projemanag.models.Board
import com.example.projemanag.utils.Constants
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.IOException

class CreateBoardActivity : BaseActivity() {

    private var binding: ActivityCreateBoardBinding? = null
    private var mSelectedImageFileUri: Uri? = null

    private lateinit var mUsername: String
    private var mBoardImageUrl: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityCreateBoardBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        setupActionBar()

        if (intent.hasExtra(Constants.NAME)) {
            mUsername = intent.getStringExtra(Constants.NAME)!!
        }

        binding?.btnCreate?.setOnClickListener {
            if (mSelectedImageFileUri != null) {
                uploadBoardImage()
            } else {
                showProgressDialog()
                createBoard()
            }
        }
    }

    private fun setupActionBar() {
        setSupportActionBar(binding!!.toolbarCreateBoardActivity)

        val actionBar = supportActionBar

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
            actionBar.title = resources.getString(R.string.create_board_title)
        }

        binding!!.toolbarCreateBoardActivity.setNavigationOnClickListener { onBackPressed() }

        binding?.ivBoardImage?.setOnClickListener {
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
    }

    private fun createBoard() {
        val assignedUsers = ArrayList<String>()
        assignedUsers.add(getCurrentUserID())

        val board = Board(
            name = binding?.etBoardName?.text.toString(),
            image = mBoardImageUrl,
            createdBy = mUsername,
            assignedTo = assignedUsers,
        )

        FirestoreHandler().createBoard(activity = this, board = board)

    }

    private fun uploadBoardImage() {
        showProgressDialog()

        val sRef: StorageReference =
            FirebaseStorage.getInstance().reference.child(
                "BOARD_IMAGE" + System.currentTimeMillis() + "." + Constants.getFileExtension(
                    activity = this,
                    uri = mSelectedImageFileUri!!
                )
            )

        sRef.putFile(mSelectedImageFileUri!!)
            .addOnSuccessListener { taskSnapshot ->
                Log.d(
                    "Firebase Board Image URL",
                    taskSnapshot.metadata!!.reference!!.downloadUrl.toString()
                )

                taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener { uri ->
                    Log.d(
                        "Downloadable Board Image URL",
                        uri.toString()
                    )
                    mBoardImageUrl = uri.toString()

                    createBoard()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
                hideProgressDialog()
            }
    }

    fun boardCreatedSuccessfully() {
        hideProgressDialog()
        setResult(RESULT_OK)
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
                    .placeholder(R.drawable.ic_board_place_holder)
                    .into(binding?.ivBoardImage!!)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}