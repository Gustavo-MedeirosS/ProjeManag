package com.example.projemanag.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.projemanag.R
import com.example.projemanag.adapters.BoardItemsAdapter
import com.example.projemanag.databinding.ActivityMainBinding
import com.example.projemanag.firebase.FirestoreHandler
import com.example.projemanag.models.Board
import com.example.projemanag.models.User
import com.example.projemanag.utils.Constants
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth

class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {

    private var binding: ActivityMainBinding? = null
    private var toolbarMain: Toolbar? = null
    private var fab: FloatingActionButton? = null
    private lateinit var mUsername: String
    private var rvBoards: RecyclerView? = null
    private var tvNoBoardsAvailable: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        toolbarMain = findViewById(R.id.toolbar_main_activity)

        setupActionBar()

        binding?.navView?.setNavigationItemSelectedListener(this)

        FirestoreHandler().loadUserData(activity = this, readBoards = true)

        rvBoards = findViewById(R.id.rv_boards_list)
        tvNoBoardsAvailable = findViewById(R.id.tv_no_boards_available)

        fab = findViewById(R.id.fab_create_board)
        fab?.setOnClickListener {
            val intent = Intent(this, CreateBoardActivity::class.java)
            intent.putExtra(Constants.NAME, mUsername)
            startActivityForResult(intent, CREATE_BOARD_REQUEST_CODE)
        }
    }

    fun populateBoardsListToUI(boards: ArrayList<Board>) {
        if (boards.isNotEmpty()) {
            rvBoards?.visibility = View.VISIBLE
            tvNoBoardsAvailable?.visibility = View.GONE

            rvBoards?.layoutManager = LinearLayoutManager(this)
            rvBoards?.setHasFixedSize(true)

            val adapter = BoardItemsAdapter(context = this, items = boards)
            rvBoards?.adapter = adapter

            adapter.setOnClickListener(
                onClickListener = object : BoardItemsAdapter.OnClickListener {
                    override fun onClick(position: Int, model: Board) {
                        val intent = Intent(this@MainActivity, TaskListActivity::class.java)
                        intent.putExtra(Constants.DOCUMENT_ID, model.documentId)
                        startActivity(intent)
                    }
                }
            )
        } else {
            rvBoards?.visibility = View.GONE
            tvNoBoardsAvailable?.visibility = View.VISIBLE
        }
    }

    fun updateNavigationUserDetails(user: User, readBoards: Boolean) {
        mUsername = user.name

        Glide
            .with(this)
            .load(user.image)
            .centerCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(findViewById(R.id.iv_user_image))

        binding?.navView?.findViewById<TextView>(R.id.tv_username)?.text = user.name

        if (readBoards) {
            showProgressDialog()
            FirestoreHandler().getBoards(activity = this)
        }
    }

    private fun setupActionBar() {
        setSupportActionBar(toolbarMain)
        toolbarMain?.setNavigationIcon(R.drawable.ic_action_navigation_menu)
        toolbarMain?.setNavigationOnClickListener {
            toggleDrawer()
        }
    }

    private fun toggleDrawer() {
        if (binding != null) {
            if (isDrawerOpen()) {
                binding!!.drawerLayout.closeDrawer(GravityCompat.START)
            } else {
                binding!!.drawerLayout.openDrawer(GravityCompat.START)
            }
        }
    }

    private fun isDrawerOpen(): Boolean {
        if (binding != null) {
            return binding!!.drawerLayout.isDrawerOpen(GravityCompat.START)
        }
        return false
    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        if (isDrawerOpen()) {
            binding!!.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            doubleBackToExit()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK && requestCode == MY_PROFILE_REQUEST_CODE) {
            FirestoreHandler().loadUserData(activity = this@MainActivity)
        } else if (resultCode == RESULT_OK && requestCode == CREATE_BOARD_REQUEST_CODE) {
            FirestoreHandler().getBoards(activity = this)
        } else {
            Log.e("Cancelled", "canceled")
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_my_profile -> {
                startActivityForResult(
                    Intent(this, MyProfileActivity::class.java),
                    MY_PROFILE_REQUEST_CODE
                )
            }

            R.id.nav_sign_out -> {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, IntroActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        }

        binding!!.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
        fab = null
    }

    companion object {
        const val MY_PROFILE_REQUEST_CODE = 11
        const val CREATE_BOARD_REQUEST_CODE = 12
    }
}