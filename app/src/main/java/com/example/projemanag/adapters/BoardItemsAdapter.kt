package com.example.projemanag.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.projemanag.R
import com.example.projemanag.models.Board

class BoardItemsAdapter(
    private val context: Context,
    private val items: ArrayList<Board>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var onClickListener: OnClickListener? = null

    inner class BoardViewHolder(view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return BoardViewHolder(
            LayoutInflater.from(context).inflate(R.layout.item_board, parent, false)
        )
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = items[position]
        if (holder is BoardViewHolder) {
            Glide
                .with(context)
                .load(model.image)
                .centerCrop()
                .placeholder(R.drawable.ic_board_place_holder)
                .into(holder.itemView.findViewById(R.id.iv_board_image))

            holder.itemView.findViewById<TextView>(R.id.tv_name).text = model.name
            holder.itemView.findViewById<TextView>(R.id.tv_created_by).text =
                "Created by: ${model.createdBy}"

            holder.itemView.setOnClickListener {
                if (onClickListener != null) {
                    onClickListener!!.onClick(position = position, model = model)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    interface OnClickListener {
        fun onClick(position: Int, model: Board)
    }
}