package com.example.projemanag.dialogs

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.projemanag.R
import com.example.projemanag.adapters.LabelColorListItemsAdapter

abstract class LabelColorListDialog(
    context: Context,
    private var list: ArrayList<String>,
    private val title: String = "",
    private val mSelectedColor: String = ""
) : Dialog(context) {

    private var adapter: LabelColorListItemsAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val view = LayoutInflater.from(context).inflate(R.layout.dialog_list, null)

        setContentView(view)
        setCancelable(true)
        setCanceledOnTouchOutside(true)

        setUpRecyclerView(view = view)
    }

    @SuppressLint("CutPasteId")
    private fun setUpRecyclerView(view: View) {
        view.findViewById<TextView>(R.id.tvTitle).text = title
        view.findViewById<RecyclerView>(R.id.rvList).layoutManager = LinearLayoutManager(context)

        adapter = LabelColorListItemsAdapter(
            context = context,
            list = list,
            mSelectedColor = mSelectedColor
        )

        view.findViewById<RecyclerView>(R.id.rvList).adapter = adapter

        adapter!!.setOnItemClickListener(onItemClickListener = object :
            LabelColorListItemsAdapter.OnItemClickListener {
            override fun onClick(position: Int, color: String) {
                dismiss()
                onItemSelected(color = color)
            }
        })
    }

    protected abstract fun onItemSelected(color: String)
}