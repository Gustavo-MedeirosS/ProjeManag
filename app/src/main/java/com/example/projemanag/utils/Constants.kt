package com.example.projemanag.utils

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.webkit.MimeTypeMap

object Constants {

    const val USERS = "users"
    const val BOARDS = "boards"
    const val IMAGE = "image"
    const val NAME = "name"
    const val EMAIL = "email"
    const val MOBILE = "mobile"
    const val ASSIGNED_TO = "assignedTo"
    const val DOCUMENT_ID = "documentId"
    const val TASK_LIST = "taskList"
    const val BOARD_DETAIL = "boardDetail"
    const val ID = "id"
    const val READ_STORAGE_REQUEST_CODE = 1
    const val PICK_IMAGE_REQUEST_CODE = 1
    const val TASK_LIST_ITEM_POSITION = "taskListItemPosition"
    const val CARD_LIST_ITEM_POSITION = "cardListItemPosition"
    const val BOARD_MEMBERS_LIST = "board_member_list"
    const val SELECT = "Select"
    const val UNSELECT = "UnSelect"

    fun getFileExtension(activity: Activity, uri: Uri): String? {
        return MimeTypeMap.getSingleton()
            .getExtensionFromMimeType(activity.contentResolver.getType(uri))
    }

    fun showImageChooser(activity: Activity) {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        activity.startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST_CODE)
    }

}