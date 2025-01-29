package com.example.notesapp2

import android.content.ContentValues.TAG
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class NoteAdapter(private var notes: List<Note>, private val itemClickListener: ItemClickListener, private var dbHelper: NotesDbHelper) :
    RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {
    class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val headingTextView: TextView = itemView.findViewById(R.id.notetitle)
        val bodyTextView: TextView = itemView.findViewById(R.id.bodyofview)
        val noteSpinner: Spinner = itemView.findViewById(R.id.spinner)
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
        val spinnerOptions = arrayOf("Delete")
        val spinnerAdapter = ArrayAdapter(
            holder.itemView.context,
            R.layout.spinner_item,
            spinnerOptions
        )
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        holder.noteSpinner.adapter = spinnerAdapter
        //holder.noteSpinner.onItemSelectedListener = //object : AdapterView.OnItemSelectedListener {
            //override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
              //  val selectedItem = parent.getItemAtPosition(pos)
                //Log.i(TAG, "onItemSelected: $selectedItem")
                //if (selectedItem == 1)
                //{
                  // val id = currentNote.id
                    //dbHelper.deleteNote(id)

                //}
            //}override fun onNothingSelected(parent: AdapterView<*>) {
            //}
        //}
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