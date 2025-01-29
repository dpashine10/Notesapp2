package com.example.notesapp2

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.notesapp2.R.menu.custom_menu
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

class mainpage : AppCompatActivity(),NoteAdapter.ItemClickListener {
    private lateinit var auth: FirebaseAuth
    private lateinit var addnote: FloatingActionButton
    private lateinit var viewallnotes: RecyclerView
    private lateinit var dbHelper: NotesDbHelper
    private lateinit var noteAdapter: NoteAdapter
    private lateinit var store: FirebaseFirestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_mainpage)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        auth = Firebase.auth
        store=Firebase.firestore
        supportActionBar?.title = "All Notes"
        addnote = findViewById(R.id.createanote)
        dbHelper = NotesDbHelper(applicationContext,auth)
        viewallnotes = findViewById(R.id.recycleview)
        addnote.setOnClickListener {
            //val uniqueId = dbHelper.getNextAvailableUniqueId()
            val intent = Intent(this, individualnote::class.java)
            intent.putExtra("NOTE_ID", "")
            Log.d("mainpage", "Intent data: ${intent.extras}")
            startActivity(intent)
        }
        noteAdapter = NoteAdapter(emptyList(),this,dbHelper)
        viewallnotes.adapter = noteAdapter
        viewallnotes.layoutManager = LinearLayoutManager(this)
        viewallnotes.setHasFixedSize(true)

        fetchNotesAndDisplay() // Fetch and display notes
        logAllNotesData()
        updateAllNotesToFirebase()
    }
    private fun updateAllNotesToFirebase() {
        val user = auth.currentUser
        if (user != null) {
            val userId = user.uid
            val notes = dbHelper.getAllNotes()
            for (note in notes) {
                Log.i("all notes",  ": id=${note.id} heading=${note.heading} body=${note.body} ")
                val db = Firebase.firestore
                db.collection("users") // Collection for all users
                    .document(userId) // Document for the current user
                    .collection("notes") // Collection for the user's notes
                    .document(note.id.toString())
                    .set(note)
                    .addOnSuccessListener {
                        Log.d("Firebase", "Note added/updated with ID: ${note.id} for user: $userId")
                    }
                    .addOnFailureListener { e ->
                        Log.w("Firebase", "Error adding/updating note", e)
                    }
            }
        } else {
            // Handle case where user is not authenticated
            Log.w("Firebase", "User not authenticated. Cannot update notes.")
        }
    }
    override fun onResume() {
        super.onResume()
        fetchNotesAndDisplay()
    }
    private fun fetchNotesAndDisplay() {
        val user = auth.currentUser
        if (user != null) {
            val notes = dbHelper.getAllNotes() // Get notes from the database
            noteAdapter.updateNotes(notes) // Update the adapter with the notes
        } else {
            Toast.makeText(this,"Nothing to display",Toast.LENGTH_SHORT).show()
            noteAdapter.updateNotes(emptyList())
        }
    }
    override fun onItemClick(noteId: String) {
        val intent = Intent(this, individualnote::class.java)
        intent.putExtra("NOTE_ID", noteId)
        Log.d("openinganote", "Note ID: $noteId")
        startActivity(intent)
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(custom_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.logout -> {
                auth.signOut()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
                return true
            }
            R.id.deleteall ->{
                val uid = auth.currentUser?.uid ?: "default_notes"
                val db = dbHelper.writableDatabase
                db.execSQL("DELETE FROM $uid")
                db.close()
                finish()
                return true
            }
            R.id.update ->{
                updateAllNotesToFirebase()
                return true
            }
            //add an option to update all notes to firebase
            else -> return super.onOptionsItemSelected(item)
        }
    }
    private fun logAllNotesData() {
        val notes = dbHelper.getAllNotes()
        for (note in notes) {
            Log.d("mainpage", "Note ID: ${note.id}, Heading: ${note.heading}, Body: ${note.body}")
        }
    }
}
