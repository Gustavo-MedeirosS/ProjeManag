package com.example.projemanag.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.projemanag.R

class LabelColorListItemsAdapter(
    private val context: Context,
    private val list: ArrayList<String>,
    private val mSelectedColor: String
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var onItemClickListener: OnItemClickListener? = null

    inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(
            view = LayoutInflater.from(context).inflate(
                R.layout.item_label_color,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = list[position]

        if (holder is MyViewHolder) {
            holder.itemView.findViewById<View>(R.id.view_main)
                .setBackgroundColor(Color.parseColor(item))

            if (item == mSelectedColor) {
                holder.itemView.findViewById<ImageView>(R.id.iv_selected_color).visibility =
                    View.VISIBLE
            } else {
                holder.itemView.findViewById<ImageView>(R.id.iv_selected_color).visibility =
                    View.GONE
            }

            holder.itemView.setOnClickListener {
                if (onItemClickListener != null) {
                    onItemClickListener!!.onClick(position = position, color = item)
                }
            }
        }
    }

    fun setOnItemClickListener(onItemClickListener: OnItemClickListener) {
        this.onItemClickListener = onItemClickListener
    }

    override fun getItemCount(): Int {
        return list.size
    }

    interface OnItemClickListener {
        fun onClick(position: Int, color: String)
    }
}