package com.example.projemanag.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.projemanag.R
import com.example.projemanag.adapters.TaskListItemsAdapter
import com.example.projemanag.databinding.ActivityTaskListBinding
import com.example.projemanag.firebase.FirestoreHandler
import com.example.projemanag.models.Board
import com.example.projemanag.models.Card
import com.example.projemanag.models.Task
import com.example.projemanag.models.User
import com.example.projemanag.utils.Constants

class TaskListActivity : BaseActivity() {

    private var binding: ActivityTaskListBinding? = null
    private lateinit var mBoardDetails: Board
    private lateinit var mBoardDocumentId: String
    lateinit var mAssignedMemberDetailList: ArrayList<User>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityTaskListBinding.inflate(layoutInflater)
        setContentView(binding?.root)


        if (intent.hasExtra(Constants.DOCUMENT_ID)) {
            mBoardDocumentId = intent.getStringExtra(Constants.DOCUMENT_ID)!!
        }

        showProgressDialog()
        FirestoreHandler().getBoardDetails(activity = this, documentId = mBoardDocumentId)
    }

    fun cardDetails(taskListPosition: Int, cardPosition: Int) {
        val intent = Intent(this, CardDetailsActivity::class.java)
        intent.putExtra(Constants.BOARD_DETAIL, mBoardDetails)
        intent.putExtra(Constants.TASK_LIST_ITEM_POSITION, taskListPosition)
        intent.putExtra(Constants.CARD_LIST_ITEM_POSITION, cardPosition)
        intent.putExtra(Constants.BOARD_MEMBERS_LIST, mAssignedMemberDetailList)
        startActivityForResult(intent, CARD_DETAILS_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (
            resultCode == RESULT_OK &&
            requestCode == MEMBERS_REQUEST_CODE ||
            requestCode == CARD_DETAILS_REQUEST_CODE
        ) {
            showProgressDialog()
            FirestoreHandler().getBoardDetails(activity = this, documentId = mBoardDocumentId)
        } else {
            Log.e("Cancelled", "cancelled")
        }
    }

    fun boardMembersDetailsList(list: ArrayList<User>) {
        mAssignedMemberDetailList = list
        hideProgressDialog()

        val addTasks = Task(title = resources.getString(R.string.add_list))

        mBoardDetails.taskList.add(addTasks)

        binding?.rvTaskList?.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding?.rvTaskList?.setHasFixedSize(true)

        val adapter = TaskListItemsAdapter(context = this, tasks = mBoardDetails.taskList)
        binding?.rvTaskList?.adapter = adapter
    }

    fun boardDetails(board: Board) {
        mBoardDetails = board

        hideProgressDialog()
        setupActionBar()

        showProgressDialog()
        FirestoreHandler().getAssignedMembersListDetails(
            activity = this,
            assignedTo = mBoardDetails.assignedTo
        )
    }

    fun addUpdateTaskListSuccessful() {
        hideProgressDialog()
        showProgressDialog()
        FirestoreHandler().getBoardDetails(activity = this, documentId = mBoardDetails.documentId)
    }

    fun createTaskList(taskListName: String) {
        val task = Task(
            title = taskListName,
            createdBy = FirestoreHandler().getCurrentUserID()
        )

        mBoardDetails.taskList.add(0, task)
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size - 1)

        showProgressDialog()

        FirestoreHandler().addUpdateTaskList(activity = this, board = mBoardDetails)
    }

    fun updateTaskList(position: Int, listName: String, model: Task) {
        val task = Task(title = listName, createdBy = model.createdBy)

        mBoardDetails.taskList[position] = task
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size - 1)

        showProgressDialog()

        FirestoreHandler().addUpdateTaskList(activity = this, board = mBoardDetails)
    }

    fun deleteTaskList(position: Int) {
        mBoardDetails.taskList.removeAt(position)
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size - 1)

        showProgressDialog()

        FirestoreHandler().addUpdateTaskList(activity = this, board = mBoardDetails)
    }

    fun addCardToTaskList(position: Int, cardName: String) {
        mBoardDetails.taskList.removeAt(mBoardDetails.taskList.size - 1)

        val cardAssignedUsersList: ArrayList<String> = ArrayList()
        cardAssignedUsersList.add(FirestoreHandler().getCurrentUserID())

        val card = Card(
            name = cardName,
            createdBy = FirestoreHandler().getCurrentUserID(),
            assignedTo = cardAssignedUsersList
        )

        val cardsList = mBoardDetails.taskList[position].cards
        cardsList.add(card)

        val task = Task(
            title = mBoardDetails.taskList[position].title,
            createdBy = mBoardDetails.taskList[position].createdBy,
            cards = cardsList
        )

        mBoardDetails.taskList[position] = task

        showProgressDialog()

        FirestoreHandler().addUpdateTaskList(activity = this, board = mBoardDetails)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_members, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_members -> {
                val intent = Intent(this, MembersActivity::class.java)
                intent.putExtra(Constants.BOARD_DETAIL, mBoardDetails)
                startActivityForResult(intent, MEMBERS_REQUEST_CODE)
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun setupActionBar() {
        setSupportActionBar(binding!!.toolbarTaskListActivity)

        val actionBar = supportActionBar

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
            actionBar.title = mBoardDetails.name
        }

        binding!!.toolbarTaskListActivity.setNavigationOnClickListener { onBackPressed() }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

    companion object {
        const val MEMBERS_REQUEST_CODE = 13
        const val CARD_DETAILS_REQUEST_CODE = 13
    }
}