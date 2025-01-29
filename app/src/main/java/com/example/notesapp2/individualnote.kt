package com.example.notesapp2

import android.annotation.SuppressLint
import android.content.ContentValues
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.notesapp2.NotesDbHelper.Companion.DATABASE_NAME
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

class individualnote : AppCompatActivity() {
    private lateinit var heading: EditText
    private lateinit var mainbody: EditText
    private lateinit var auth: FirebaseAuth
    private lateinit var store: FirebaseFirestore
    private var isNoteModified = false
    private lateinit var dbHelper: NotesDbHelper
    private lateinit var noteId: String

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_individualnote)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        onBackPressedDispatcher.addCallback(this) {
            checkNoteContentAndHandleBackPress()
        }
        auth = Firebase.auth
        store = Firebase.firestore
        var user =
            auth.currentUser // You might not need this line if you're not using Firebase Authentication here
        heading = findViewById(R.id.heading)
        mainbody = findViewById(R.id.write)
        setTitle(heading.text.toString())
        dbHelper = NotesDbHelper(applicationContext, auth)
        noteId = intent.getStringExtra("NOTE_ID") ?: ""
        if (noteId.isNotBlank()) {
            // Retrieve existing note data
            val note = dbHelper.getNoteById(noteId.toInt())
            if (note != null) {
                if (note.heading.isNotBlank() || note.heading.isNotBlank()) {
                    heading.setText(note.heading)
                    mainbody.setText(note.body)
                    setTitle(note.heading)
                }
                if (note.heading.isBlank() and note.body.isNotBlank()){
                    val head = "Untitled"
                    heading.setText(head)
                    mainbody.setText(note.body)
                    setTitle(note.heading)
                }
            }
        }
        heading.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                isNoteModified = true
                setTitle(heading.text.toString())
                saveNoteToDatabase() // Consider removing this call to avoid frequent saves
            }

            override fun afterTextChanged(s: Editable?) {}
        })
        mainbody.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                isNoteModified = true
                saveNoteToDatabase() // Consider removing this call to avoid frequent saves
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    fun deleteanote(noteId: Int) {
        deleteNoteFromFirebase(noteId)
        val db = dbHelper.writableDatabase
        val whereClause = "${NoteContract.NoteEntry.COLUMN_NAME_ID} = ?"
        val whereArgs = arrayOf(noteId.toString())
        db.delete(DATABASE_NAME, whereClause, whereArgs) // Use DATABASE_NAME
        deleteNoteFromFirebase(noteId)
        db.close()
        Log.i("Data deleted", "checkNoteContentAndHandleBackPress: Data delete cause empty")
        finish() //Close the activity

    }

    private fun checkNoteContentAndHandleBackPress() {
        val headingText = heading.text.toString()
        val bodyText = mainbody.text.toString()

        if (headingText.isEmpty() and bodyText.isEmpty()) {
            deleteanote(noteId.toInt())
        } else {
            saveNoteToDatabase()
            finish()
        }
    }

    private fun deleteNoteFromFirebase(noteId: Int) {
        val user = auth.currentUser
        if (user != null) {
            val userId = user.uid
            val db = Firebase.firestore
            db.collection("users")
                .document(userId)
                .collection("notes")
                .document(noteId.toString())
                .delete()
                .addOnSuccessListener {
                    Log.d(
                        "Firebase",
                        "Note deleted with ID: $noteId for user: $userId"
                    )
                }
                .addOnFailureListener { e -> Log.w("Firebase", "Error deleting note", e) }
        } else {
            Log.w("Firebase", "User not authenticated. Cannot delete note.")
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menuforanote, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.share -> {
                //workhere
                return true
            }

            R.id.lockanote -> {
                //workhere
                return true
            }

            R.id.deletefrominside -> {
                deleteanote(noteId.toInt())
                return true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun saveNoteToDatabase() {
        if (noteId.isBlank()) {
            // Handle case where noteId is missing (e.g., show an error message)
            Log.e("individualnote", "Note ID is missing. Creating it.")
            noteId = dbHelper.getNextAvailableUniqueId().toString()
            val newRowId = dbHelper.addValue(noteId.toInt(),heading.text.toString(), mainbody.text.toString())
            if (newRowId != -1L) {
                noteId = newRowId.toString() // Update noteId with the newly generated ID
                Log.d("individualnote", "New note inserted with ID: $noteId")
            } else {
                Log.e("individualnote", "Failed to insert new note")
            }
        }
        else {
            if (heading.text.isBlank() and mainbody.text.isBlank()) {
                //deleteanote(noteId.toInt())
                Log.d("savenote", "An empty note was detected")
                return
            }
            val db = dbHelper.writableDatabase
            val tablename = dbHelper.userUid

            Log.d("individualnote", "Table name: ${dbHelper.userUid}")
            Log.d("individualnote", "Note ID: $noteId")

            val values = ContentValues().apply {
                put(NoteContract.NoteEntry.COLUMN_NAME_ID, noteId.toInt())
                put(NoteContract.NoteEntry.COLUMN_NAME_HEADING, heading.text.toString())
                put(NoteContract.NoteEntry.COLUMN_NAME_BODY, mainbody.text.toString())
            }
            val affectedRows = db.update(
                tablename,
                values,
                "${NoteContract.NoteEntry.COLUMN_NAME_ID} = ?",
                arrayOf(noteId)// Where clause
            )
            if (affectedRows == 0) {
                // Handle case where update failed (e.g., note not found)
                Log.e("individualnote", "Failed to update note with ID: $noteId")
            } else {
                Log.d("individualnote", "Note updated successfully with ID: $noteId")
            }
        }
    }
}