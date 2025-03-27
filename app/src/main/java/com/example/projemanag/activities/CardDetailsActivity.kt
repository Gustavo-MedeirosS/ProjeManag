package com.example.projemanag.activities

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.recyclerview.widget.GridLayoutManager
import com.example.projemanag.R
import com.example.projemanag.adapters.CardMemberListItemsAdapter
import com.example.projemanag.databinding.ActivityCardDetailsBinding
import com.example.projemanag.dialogs.LabelColorListDialog
import com.example.projemanag.dialogs.MembersListDialog
import com.example.projemanag.firebase.FirestoreHandler
import com.example.projemanag.models.Board
import com.example.projemanag.models.Card
import com.example.projemanag.models.SelectedMembers
import com.example.projemanag.models.Task
import com.example.projemanag.models.User
import com.example.projemanag.utils.Constants
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class CardDetailsActivity : BaseActivity() {

    private var binding: ActivityCardDetailsBinding? = null
    private lateinit var mBoardDetails: Board
    private var mTaskListPosition: Int = -1
    private var mCardPosition: Int = -1
    private var mSelectedColor: String = ""
    private lateinit var mMembersDetailList: ArrayList<User>
    private var mSelectedDueDateMilliSeconds: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityCardDetailsBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        getIntentData()
        setupActionBar()

        binding?.etNameCardDetails?.setText(mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].name)
        binding?.etNameCardDetails?.setSelection(binding?.etNameCardDetails?.text.toString().length)

        mSelectedColor = mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].labelColor

        if (mSelectedColor.isNotEmpty()) {
            setColor()
        }

        binding?.btnUpdateCardDetails?.setOnClickListener {
            if (binding?.etNameCardDetails?.text.toString().isNotEmpty()) {
                updateCardDetails()
            } else {
                Toast.makeText(this, "Please enter a card name", Toast.LENGTH_SHORT).show()
            }
        }

        binding?.tvSelectLabelColor?.setOnClickListener {
            labelColorListDialog()
        }

        binding?.tvSelectMembers?.setOnClickListener {
            membersListDialog()
        }

        setUpSelectedMembersList()

        mSelectedDueDateMilliSeconds =
            mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].dueDate

        if (mSelectedDueDateMilliSeconds >= 0) {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
            val selectedDate = sdf.format(Date(mSelectedDueDateMilliSeconds))
            binding?.tvSelectDueDate?.text = selectedDate
        }

        binding?.tvSelectDueDate?.setOnClickListener {
            showDataPicker()
        }
    }

    private fun setupActionBar() {
        setSupportActionBar(binding!!.toolbarCardDetailsActivity)

        val actionBar = supportActionBar

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
            actionBar.title = mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].name
        }

        binding!!.toolbarCardDetailsActivity.setNavigationOnClickListener { onBackPressed() }
    }

    private fun getIntentData() {
        if (intent.hasExtra(Constants.BOARD_DETAIL)) {
            mBoardDetails = intent.getParcelableExtra(Constants.BOARD_DETAIL)!!
        }
        if (intent.hasExtra(Constants.TASK_LIST_ITEM_POSITION)) {
            mTaskListPosition = intent.getIntExtra(Constants.TASK_LIST_ITEM_POSITION, -1)
        }
        if (intent.hasExtra(Constants.CARD_LIST_ITEM_POSITION)) {
            mCardPosition = intent.getIntExtra(Constants.CARD_LIST_ITEM_POSITION, -1)
        }
        if (intent.hasExtra(Constants.BOARD_MEMBERS_LIST)) {
            mMembersDetailList = intent.getParcelableArrayListExtra(Constants.BOARD_MEMBERS_LIST)!!
        }
    }

    private fun membersListDialog() {
        val cardAssignedMembersList =
            mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].assignedTo

        if (cardAssignedMembersList.isNotEmpty()) {
            for (i in mMembersDetailList.indices) {
                for (j in cardAssignedMembersList) {
                    if (mMembersDetailList[i].id == j) {
                        mMembersDetailList[i].selected = true
                    }
                }
            }
        } else {
            for (i in mMembersDetailList.indices) {
                mMembersDetailList[i].selected = false
            }
        }

        val listDialog = object : MembersListDialog(
            context = this,
            list = mMembersDetailList,
            title = resources.getString(R.string.str_select_member)
        ) {
            override fun onItemSelected(user: User, action: String) {
                if (action == Constants.SELECT) {
                    if (!mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].assignedTo.contains(
                            user.id
                        )
                    ) {
                        mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].assignedTo.add(
                            user.id
                        )
                    }
                } else {
                    mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].assignedTo.remove(
                        user.id
                    )

                    for (i in mMembersDetailList.indices) {
                        if (mMembersDetailList[i].id == user.id) {
                            mMembersDetailList[i].selected = false
                        }
                    }
                }

                setUpSelectedMembersList()
            }
        }

        listDialog.show()
    }

    private fun setUpSelectedMembersList() {
        val cardAssignedMembersList =
            mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].assignedTo

        val selectedMembersList = ArrayList<SelectedMembers>()

        for (i in mMembersDetailList.indices) {
            for (j in cardAssignedMembersList) {
                if (mMembersDetailList[i].id == j) {
                    val selectedMember = SelectedMembers(
                        id = mMembersDetailList[i].id,
                        image = mMembersDetailList[i].image
                    )
                    selectedMembersList.add(selectedMember)
                }
            }
        }

        if (selectedMembersList.isNotEmpty()) {
            selectedMembersList.add(SelectedMembers(id = "", image = ""))
            binding?.tvSelectMembers?.visibility = View.GONE
            binding?.rvSelectedMembersList?.visibility = View.VISIBLE
            binding?.rvSelectedMembersList?.layoutManager = GridLayoutManager(this, 6)

            val adapter = CardMemberListItemsAdapter(
                context = this,
                list = selectedMembersList,
                assignedMembers = true
            )
            binding?.rvSelectedMembersList?.adapter = adapter

            adapter.setOnClickListener(object : CardMemberListItemsAdapter.OnClickListener {
                override fun onClick() {
                    membersListDialog()
                }
            })
        } else {
            binding?.tvSelectMembers?.visibility = View.VISIBLE
            binding?.rvSelectedMembersList?.visibility = View.GONE
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_delete_card, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_delete_card -> {
                alertDialogForDeleteCard(
                    cardName = mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].name
                )
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun colorsList(): ArrayList<String> {
        val colorsList = ArrayList<String>()
        colorsList.add("#43C86F")
        colorsList.add("#0C90F1")
        colorsList.add("#F72400")
        colorsList.add("#7A8089")
        colorsList.add("#D57C1D")
        colorsList.add("#770000")
        colorsList.add("#0022F8")

        return colorsList
    }

    private fun setColor() {
        binding?.tvSelectLabelColor?.text = ""
        binding?.tvSelectLabelColor?.setBackgroundColor(Color.parseColor(mSelectedColor))
    }

    fun addUpdateTaskListSuccessful() {
        hideProgressDialog()
        setResult(RESULT_OK)
        finish()
    }

    private fun updateCardDetails() {
        val card = Card(
            name = binding?.etNameCardDetails?.text.toString(),
            createdBy = mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].createdBy,
            assignedTo = mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition].assignedTo,
            labelColor = mSelectedColor,
            dueDate = mSelectedDueDateMilliSeconds
        )

        val taskList: ArrayList<Task> = mBoardDetails.taskList
        taskList.removeAt(taskList.size - 1)
        mBoardDetails.taskList[mTaskListPosition].cards[mCardPosition] = card

        showProgressDialog()
        FirestoreHandler().addUpdateTaskList(activity = this, board = mBoardDetails)
    }

    private fun deleteCard() {
        val cardsList: ArrayList<Card> = mBoardDetails.taskList[mTaskListPosition].cards
        cardsList.removeAt(mCardPosition)

        val taskList: ArrayList<Task> = mBoardDetails.taskList
        taskList.removeAt(taskList.size - 1)
        taskList[mTaskListPosition].cards = cardsList

        showProgressDialog()
        FirestoreHandler().addUpdateTaskList(activity = this, board = mBoardDetails)
    }

    private fun alertDialogForDeleteCard(cardName: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(resources.getString(R.string.alert))
        builder.setMessage(
            resources.getString(
                R.string.confirmation_message_to_delete_card,
                cardName
            )
        )
        builder.setIcon(android.R.drawable.ic_dialog_alert)

        builder.setPositiveButton(resources.getString(R.string.yes)) { dialogInterface, _ ->
            dialogInterface.dismiss()
            deleteCard()
        }
        builder.setNegativeButton(resources.getString(R.string.no)) { dialogInterface, _ ->
            dialogInterface.dismiss()
        }

        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    private fun labelColorListDialog() {
        val colorList: ArrayList<String> = colorsList()

        val listDialog = object : LabelColorListDialog(
            context = this@CardDetailsActivity,
            list = colorList,
            title = resources.getString(R.string.str_select_label_color),
            mSelectedColor = mSelectedColor
        ) {
            override fun onItemSelected(color: String) {
                mSelectedColor = color
                setColor()
            }
        }

        listDialog.show()
    }

    private fun showDataPicker() {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)


        val dpd = DatePickerDialog(
            this,
            DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                val sDayOfMonth = if (dayOfMonth < 10) "0$dayOfMonth" else "$dayOfMonth"

                val sMonthOfYear =
                    if ((monthOfYear + 1) < 10) "0${monthOfYear + 1}" else "${monthOfYear + 1}"

                val selectedDate = "$sDayOfMonth/$sMonthOfYear/$year"

                binding?.tvSelectDueDate?.text = selectedDate

                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)

                val theDate = sdf.parse(selectedDate)

                mSelectedDueDateMilliSeconds = theDate!!.time
            },
            year,
            month,
            day
        )
        dpd.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}