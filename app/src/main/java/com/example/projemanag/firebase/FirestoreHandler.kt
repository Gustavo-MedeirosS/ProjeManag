package com.example.projemanag.firebase

import android.app.Activity
import android.util.Log
import android.widget.Toast
import com.example.projemanag.activities.CardDetailsActivity
import com.example.projemanag.activities.CreateBoardActivity
import com.example.projemanag.activities.MainActivity
import com.example.projemanag.activities.MembersActivity
import com.example.projemanag.activities.MyProfileActivity
import com.example.projemanag.activities.SignInActivity
import com.example.projemanag.activities.SignUpActivity
import com.example.projemanag.activities.TaskListActivity
import com.example.projemanag.models.Board
import com.example.projemanag.models.User
import com.example.projemanag.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class FirestoreHandler {

    private val mFirestore = FirebaseFirestore.getInstance()

    fun createUser(activity: SignUpActivity, userInfo: User) {
        mFirestore.collection(Constants.USERS)
            .document(getCurrentUserID())
            .set(userInfo, SetOptions.merge())
            .addOnSuccessListener { activity.userRegisteredSuccess() }
            .addOnFailureListener { e ->
                Log.e(
                    activity.javaClass.simpleName,
                    "Error writing document: ${e.printStackTrace()}"
                )
            }
    }

    fun createBoard(activity: CreateBoardActivity, board: Board) {
        mFirestore.collection(Constants.BOARDS)
            .document()
            .set(board, SetOptions.merge())
            .addOnSuccessListener {
                Toast.makeText(activity, "Board created succesfully", Toast.LENGTH_SHORT).show()
                activity.boardCreatedSuccessfully()
            }
            .addOnFailureListener { exception ->
                activity.hideProgressDialog()
                Log.e(
                    activity.javaClass.simpleName,
                    "Error saving board:",
                    exception
                )
            }
    }

    fun loadUserData(activity: Activity, readBoards: Boolean = false) {
        mFirestore.collection(Constants.USERS)
            .document(getCurrentUserID())
            .get()
            .addOnSuccessListener { document ->
                val loggedInUser = document.toObject(User::class.java)

                if (loggedInUser != null) {
                    when (activity) {
                        is SignInActivity -> {
                            activity.signInSuccess(user = loggedInUser)
                        }

                        is MainActivity -> {
                            activity.updateNavigationUserDetails(
                                user = loggedInUser,
                                readBoards = readBoards
                            )
                        }

                        is MyProfileActivity -> {
                            activity.setUserDataInUI(user = loggedInUser)
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                when (activity) {
                    is SignInActivity -> {
                        activity.hideProgressDialog()
                    }

                    is MainActivity -> {
                        activity.hideProgressDialog()
                    }
                }
                Log.e(
                    activity.javaClass.simpleName,
                    "Error writing document: ${e.printStackTrace()}"
                )
            }
    }

    fun getBoards(activity: MainActivity) {
        mFirestore.collection(Constants.BOARDS)
            .whereArrayContains(Constants.ASSIGNED_TO, getCurrentUserID())
            .get()
            .addOnSuccessListener { document ->
                Log.d("Boards from FireStore", document.documents.toString())
                val boards = ArrayList<Board>()
                document.documents.forEach { d ->
                    val board = d.toObject(Board::class.java)!!
                    board.documentId = d.id
                    boards.add(board)
                }

                activity.populateBoardsListToUI(boards = boards)
                activity.hideProgressDialog()
            }
            .addOnFailureListener { exception ->
                Log.e("Boards from FireStore", "Error when fetching boards:", exception)
                activity.hideProgressDialog()
            }
    }

    fun getBoardDetails(activity: TaskListActivity, documentId: String) {
        mFirestore.collection(Constants.BOARDS)
            .document(documentId)
            .get()
            .addOnSuccessListener { document ->
                Log.d("Board details", document.toString())

                val board = document.toObject(Board::class.java)!!
                board.documentId = document.id

                activity.boardDetails(board = board)
            }
            .addOnFailureListener { exception ->
                Log.e("Board details", "Error when fetching board details:", exception)
                activity.hideProgressDialog()
            }
    }

    fun addUpdateTaskList(activity: Activity, board: Board) {
        val taskListHashMap = HashMap<String, Any>()
        taskListHashMap[Constants.TASK_LIST] = board.taskList

        mFirestore.collection(Constants.BOARDS)
            .document(board.documentId)
            .update(taskListHashMap)
            .addOnSuccessListener {
                Log.d("Update board", "Board updated successfully")
                if (activity is TaskListActivity) {
                    activity.addUpdateTaskListSuccessful()
                } else if (activity is CardDetailsActivity) {
                    activity.addUpdateTaskListSuccessful()
                }
            }
            .addOnFailureListener { exception ->
                if (activity is TaskListActivity) {
                    activity.hideProgressDialog()
                } else if (activity is CardDetailsActivity) {
                    activity.hideProgressDialog()
                }
                Log.e("Update board", "Error when updating board", exception)
            }
    }

    fun updateUserProfileData(activity: MyProfileActivity, user: HashMap<String, Any>) {
        mFirestore.collection(Constants.USERS)
            .document(getCurrentUserID())
            .update(user)
            .addOnSuccessListener {
                Log.d(activity.javaClass.simpleName, "Profile data updated successfully")
                Toast.makeText(activity, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                activity.profileUpdateSuccess()
            }
            .addOnFailureListener { e ->
                activity.hideProgressDialog()
                Log.e(activity.javaClass.simpleName, "Error when updating user profile:", e)
                Toast.makeText(activity, "Error when updating profile", Toast.LENGTH_SHORT).show()
            }
    }

    fun getCurrentUserID(): String {
        val currentUser = FirebaseAuth.getInstance().currentUser
        var currentUserId = ""

        if (currentUser != null) {
            currentUserId = currentUser.uid
        }

        return currentUserId
    }

    fun getAssignedMembersListDetails(activity: Activity, assignedTo: ArrayList<String>) {
        mFirestore.collection(Constants.USERS)
            .whereIn(Constants.ID, assignedTo)
            .get()
            .addOnSuccessListener { document ->
                Log.d("Get Assigned Members Success", document.documents.toString())
                val usersList: ArrayList<User> = ArrayList()

                document.documents.forEach { userDocument ->
                    val user = userDocument.toObject(User::class.java)!!
                    usersList.add(user)
                }
                if (activity is MembersActivity) {
                    activity.setupMembersList(list = usersList)
                } else if (activity is TaskListActivity) {
                    activity.boardMembersDetailsList(list = usersList)
                }
            }
            .addOnFailureListener { exception ->
                if (activity is MembersActivity) {
                    activity.hideProgressDialog()
                } else if (activity is TaskListActivity) {
                    activity.hideProgressDialog()
                }
                Log.d("Get Assigned Members Failure", exception.message!!)
            }
    }

    fun getMemberDetails(activity: MembersActivity, email: String) {
        mFirestore.collection(Constants.USERS)
            .whereEqualTo(Constants.EMAIL, email)
            .get()
            .addOnSuccessListener { document ->
                if (document.documents.isNotEmpty()) {
                    val user = document.documents[0].toObject(User::class.java)!!
                    activity.memberDetails(user = user)
                } else {
                    activity.hideProgressDialog()
                    activity.showErrorSnackBar(message = "No such member found")
                }
            }
            .addOnFailureListener { exception ->
                Log.d("Get User by email", "Failure when searching user by email", exception)
            }
    }

    fun assignMemberToBoard(activity: MembersActivity, board: Board, user: User) {
        val assignedToHashMap = HashMap<String, Any>()
        assignedToHashMap[Constants.ASSIGNED_TO] = board.assignedTo

        mFirestore.collection(Constants.BOARDS)
            .document(board.documentId)
            .update(assignedToHashMap)
            .addOnSuccessListener {
                activity.memberAssignSuccess(user = user)
            }
            .addOnFailureListener { exception ->
                activity.hideProgressDialog()
                Log.d("Assign Members", "Failure when assigning user to board", exception)
            }
    }
}