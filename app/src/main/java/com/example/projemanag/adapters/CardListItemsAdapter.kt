package com.example.projemanag.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.projemanag.R
import com.example.projemanag.activities.TaskListActivity
import com.example.projemanag.models.Card
import com.example.projemanag.models.SelectedMembers

open class CardListItemsAdapter(
    private val context: Context,
    private var list: ArrayList<Card>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var onClickListener: OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.item_card,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val model = list[position]

        if (holder is MyViewHolder) {
            if (model.labelColor.isNotEmpty()) {
                holder.itemView.findViewById<View>(R.id.view_label_color).visibility = View.VISIBLE
                holder.itemView.findViewById<View>(R.id.view_label_color)
                    .setBackgroundColor(Color.parseColor(model.labelColor))
            } else {
                holder.itemView.findViewById<View>(R.id.view_label_color).visibility = View.GONE
            }

            holder.itemView.findViewById<TextView>(R.id.tv_card_name).text = model.name

            if ((context as TaskListActivity).mAssignedMemberDetailList.isNotEmpty()) {
                val selectedMembersList: ArrayList<SelectedMembers> = ArrayList()

                for (i in context.mAssignedMemberDetailList.indices) {
                    for (j in model.assignedTo) {
                        if (context.mAssignedMemberDetailList[i].id == j) {
                            val selectedMember = SelectedMembers(
                                id = context.mAssignedMemberDetailList[i].id,
                                image = context.mAssignedMemberDetailList[i].image
                            )
                            selectedMembersList.add(selectedMember)
                        }
                    }
                }

                if (selectedMembersList.isNotEmpty()) {
                    if (
                        selectedMembersList.size == 1 &&
                        selectedMembersList[0].id == model.createdBy
                    ) {
                        holder.itemView.findViewById<RecyclerView>(R.id.rv_card_selected_members_list)
                            .visibility = View.GONE
                    } else {
                        holder.itemView.findViewById<RecyclerView>(R.id.rv_card_selected_members_list)
                            .visibility = View.VISIBLE
                        holder.itemView.findViewById<RecyclerView>(R.id.rv_card_selected_members_list)
                            .layoutManager = GridLayoutManager(context, 4)

                        val adapter = CardMemberListItemsAdapter(
                            context = context,
                            list = selectedMembersList,
                            assignedMembers = false
                        )
                        holder.itemView.findViewById<RecyclerView>(R.id.rv_card_selected_members_list)
                            .adapter = adapter

                        adapter.setOnClickListener(onClickListener = object :
                            CardMemberListItemsAdapter.OnClickListener {
                            override fun onClick() {
                                if (onClickListener != null) {
                                    onClickListener!!.onClick(position = position)
                                }
                            }
                        })
                    }
                } else {
                    holder.itemView.findViewById<RecyclerView>(R.id.rv_card_selected_members_list)
                        .visibility = View.GONE
                }
            }

            holder.itemView.setOnClickListener {
                if (onClickListener != null) {
                    onClickListener!!.onClick(position = position)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    interface OnClickListener {
        fun onClick(position: Int)
    }

    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view)
}