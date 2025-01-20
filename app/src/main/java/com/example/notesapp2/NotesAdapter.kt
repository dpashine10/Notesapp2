package com.example.notesapp2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class NoteAdapter(private var notes: List<Note>, private val itemClickListener: ItemClickListener) :
    RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {
    class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val headingTextView: TextView = itemView.findViewById(R.id.notetitle)
        val bodyTextView: TextView = itemView.findViewById(R.id.bodyofview)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.allnoteslayout, parent, false)
        return NoteViewHolder(itemView)
    }
    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val currentNote = notes[position]
        holder.headingTextView.text = currentNote.heading
        val body = currentNote.body.take(15) // Take first 10 characters of body
        holder.bodyTextView.text = body

        holder.itemView.setOnClickListener {
            itemClickListener.onItemClick(currentNote.id.toString()) // Pass the correct note ID
        }
    }

    override fun getItemCount(): Int {
        return notes.size
    }

    fun updateNotes(newNotes: List<Note>) {
        notes = newNotes
        notifyDataSetChanged()
    }

    interface ItemClickListener {
        fun onItemClick(noteId: String)
    }
}