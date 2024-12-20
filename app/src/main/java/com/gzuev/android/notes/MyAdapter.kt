package com.gzuev.android.notes

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import android.widget.TextView

class MyAdapter(private var notesList: List<ListItem>, private val itemClickListener: (ListItem) -> Unit) :
    RecyclerView.Adapter<MyAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.rc_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val note = notesList[position]
        holder.titleTextView.text = note.title

        // Устанавливаем цвет фона в зависимости от цвета категории
        val categoryColor = if (note.categoryColor.isNotEmpty()) {
            Color.parseColor(note.categoryColor)
        } else {
            Color.WHITE // Цвет по умолчанию, если categoryColor пустой
        }
        holder.itemView.setBackgroundColor(categoryColor)

        holder.itemView.setOnClickListener { itemClickListener(note) }
    }

    override fun getItemCount(): Int = notesList.size

    fun updateList(newList: List<ListItem>) {
        notesList = newList
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
    }
}
