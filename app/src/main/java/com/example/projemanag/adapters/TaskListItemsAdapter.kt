package com.example.projemanag.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.projemanag.R
import com.example.projemanag.activities.TaskListActivity
import com.example.projemanag.models.Task
import java.util.Collections

class TaskListItemsAdapter(
    private val context: Context,
    private val tasks: ArrayList<Task>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var mPositionDraggedFrom = -1
    private var mPositionDraggedTo = -1

    inner class TasksViewHolder(view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_task, parent, false)

        val layoutParams = LinearLayout.LayoutParams(
            (parent.width * 0.7).toInt(), LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins((15.toDp()).toPx(), 0, (40.toDp()).toPx(), 0)

        view.layoutParams = layoutParams

        return TasksViewHolder(view = view)
    }

    @SuppressLint("CutPasteId")
    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        @SuppressLint("RecyclerView") position: Int
    ) {
        val model = tasks[position]

        if (holder is TasksViewHolder) {
            if (position == tasks.size - 1) {
                holder.itemView.findViewById<TextView>(R.id.tv_add_task_list).visibility =
                    View.VISIBLE
                holder.itemView.findViewById<LinearLayout>(R.id.ll_task_item).visibility = View.GONE
            } else {
                holder.itemView.findViewById<TextView>(R.id.tv_add_task_list).visibility = View.GONE
                holder.itemView.findViewById<LinearLayout>(R.id.ll_task_item).visibility =
                    View.VISIBLE
            }

            holder.itemView.findViewById<TextView>(R.id.tv_task_list_title).text = model.title

            holder.itemView.findViewById<TextView>(R.id.tv_add_task_list).setOnClickListener {
                holder.itemView.findViewById<TextView>(R.id.tv_add_task_list).visibility = View.GONE
                holder.itemView.findViewById<CardView>(R.id.cv_add_task_list_name).visibility =
                    View.VISIBLE
            }

            holder.itemView.findViewById<ImageButton>(R.id.ib_close_list_name).setOnClickListener {
                holder.itemView.findViewById<TextView>(R.id.tv_add_task_list).visibility =
                    View.VISIBLE
                holder.itemView.findViewById<CardView>(R.id.cv_add_task_list_name).visibility =
                    View.GONE
            }

            holder.itemView.findViewById<ImageButton>(R.id.ib_done_list_name).setOnClickListener {
                val listName =
                    holder.itemView.findViewById<EditText>(R.id.et_task_list_name).text.toString()

                if (listName.isNotEmpty()) {
                    if (context is TaskListActivity) {
                        context.createTaskList(taskListName = listName)
                    }
                } else {
                    Toast.makeText(context, "Please enter list name", Toast.LENGTH_SHORT).show()
                }
            }

            holder.itemView.findViewById<ImageButton>(R.id.ib_edit_list_name).setOnClickListener {
                holder.itemView.findViewById<EditText>(R.id.et_edit_task_list_name)
                    .setText(model.title)
                holder.itemView.findViewById<LinearLayout>(R.id.ll_title_view).visibility =
                    View.GONE
                holder.itemView.findViewById<CardView>(R.id.cv_edit_task_list_name).visibility =
                    View.VISIBLE
            }

            holder.itemView.findViewById<ImageButton>(R.id.ib_close_editable_view)
                .setOnClickListener {
                    holder.itemView.findViewById<LinearLayout>(R.id.ll_title_view).visibility =
                        View.VISIBLE
                    holder.itemView.findViewById<CardView>(R.id.cv_edit_task_list_name).visibility =
                        View.GONE
                }

            holder.itemView.findViewById<ImageButton>(R.id.ib_done_edit_list_name)
                .setOnClickListener {
                    val listName =
                        holder.itemView.findViewById<EditText>(R.id.et_edit_task_list_name).text.toString()

                    if (listName.isNotEmpty()) {
                        if (context is TaskListActivity) {
                            context.updateTaskList(
                                position = position,
                                listName = listName,
                                model = model
                            )
                        }
                    } else {
                        Toast.makeText(context, "Please enter a list name", Toast.LENGTH_SHORT)
                            .show()
                    }
                }

            holder.itemView.findViewById<ImageButton>(R.id.ib_delete_list)
                .setOnClickListener {
                    displayAlertDialogBeforDeletingList(position = position, title = model.title)
                }

            holder.itemView.findViewById<TextView>(R.id.tv_add_card)
                .setOnClickListener {
                    holder.itemView.findViewById<TextView>(R.id.tv_add_card).visibility = View.GONE
                    holder.itemView.findViewById<CardView>(R.id.cv_add_card).visibility =
                        View.VISIBLE
                }

            holder.itemView.findViewById<ImageButton>(R.id.ib_close_card_name)
                .setOnClickListener {
                    holder.itemView.findViewById<TextView>(R.id.tv_add_card).visibility =
                        View.VISIBLE
                    holder.itemView.findViewById<CardView>(R.id.cv_add_card).visibility = View.GONE
                }

            holder.itemView.findViewById<ImageButton>(R.id.ib_done_card_name).setOnClickListener {
                val cardName =
                    holder.itemView.findViewById<EditText>(R.id.et_card_name).text.toString()

                if (cardName.isNotEmpty()) {
                    if (context is TaskListActivity) {
                        context.addCardToTaskList(position = position, cardName = cardName)
                    }
                } else {
                    Toast.makeText(context, "Please enter a card name", Toast.LENGTH_SHORT).show()
                }
            }

            // Cards RecyclerView
            holder.itemView.findViewById<RecyclerView>(R.id.rv_card_list).layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            holder.itemView.findViewById<RecyclerView>(R.id.rv_card_list).setHasFixedSize(true)

            val adapter = CardListItemsAdapter(context = context, list = model.cards)
            holder.itemView.findViewById<RecyclerView>(R.id.rv_card_list).adapter = adapter

            adapter.setOnClickListener(onClickListener = object :
                CardListItemsAdapter.OnClickListener {
                override fun onClick(cardPosition: Int) {
                    if (context is TaskListActivity) {
                        context.cardDetails(
                            taskListPosition = position,
                            cardPosition = cardPosition
                        )
                    }
                }
            })

            val dividerItemDecoration =
                DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
            holder.itemView.findViewById<RecyclerView>(R.id.rv_card_list)
                .addItemDecoration(dividerItemDecoration)

            val helper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP or ItemTouchHelper.DOWN,
                0
            ) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    dragged: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    val draggedPosition = dragged.adapterPosition
                    val targetPosition = target.adapterPosition

                    if (mPositionDraggedFrom == -1) {
                        mPositionDraggedFrom = draggedPosition
                    }
                    mPositionDraggedTo = targetPosition
                    Collections.swap(tasks[position].cards, draggedPosition, targetPosition)

                    adapter.notifyItemMoved(draggedPosition, targetPosition)
                    return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

                }

                override fun clearView(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder
                ) {
                    super.clearView(recyclerView, viewHolder)

                    if (
                        mPositionDraggedFrom != -1 &&
                        mPositionDraggedTo != -1 &&
                        mPositionDraggedFrom != mPositionDraggedTo
                    ) {
                        (context as TaskListActivity).updateCardsInTaskList(
                            taskListPosition = position,
                            cards = tasks[position].cards
                        )
                    }

                    mPositionDraggedFrom = -1
                    mPositionDraggedTo = -1
                }
            })

            helper.attachToRecyclerView(holder.itemView.findViewById(R.id.rv_card_list))
        }
    }

    private fun displayAlertDialogBeforDeletingList(position: Int, title: String) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Alert")
        builder.setMessage("Are you sure you want to delete $title?")
        builder.setIcon(android.R.drawable.ic_dialog_alert)
        builder.setPositiveButton("Yes") { dialog, _ ->
            dialog.dismiss()
            if (context is TaskListActivity) {
                context.deleteTaskList(position = position)
            }
        }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }

        val dialog: AlertDialog = builder.create()
        dialog.setCancelable(false)
        dialog.show()
    }

    override fun getItemCount(): Int {
        return tasks.size
    }

    private fun Int.toDp(): Int = (this / Resources.getSystem().displayMetrics.density).toInt()

    private fun Int.toPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()
}