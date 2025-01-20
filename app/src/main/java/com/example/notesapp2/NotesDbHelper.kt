package com.example.notesapp2

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class NotesDbHelper(context: Context, private val auth: FirebaseAuth) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 1
        private val auth = Firebase.auth
        val DATABASE_NAME = auth.currentUser?.uid ?: "default_notes" // Use UID or default name
    }

    val userUid = auth.currentUser?.uid ?: "default_notes"

    override fun onCreate(db: SQLiteDatabase) {
        // Create table with user's UID as table name
        val createTableQuery = "CREATE TABLE $userUid (" +
                "${NoteContract.NoteEntry.COLUMN_NAME_ID} INTEGER PRIMARY KEY," +
                "${NoteContract.NoteEntry.COLUMN_NAME_HEADING} TEXT," +
                "${NoteContract.NoteEntry.COLUMN_NAME_BODY} TEXT)"
        db.execSQL(createTableQuery)
    }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Handle database upgrades if needed
        db.execSQL("DROP TABLE IF EXISTS $userUid")
        onCreate(db)
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onUpgrade(db, oldVersion, newVersion)
    }

    fun addValue(noteId: Int ,heading: String, body: String): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(NoteContract.NoteEntry.COLUMN_NAME_HEADING, noteId)
            put(NoteContract.NoteEntry.COLUMN_NAME_HEADING, heading)
            put(NoteContract.NoteEntry.COLUMN_NAME_BODY, body)
        }
        val newRowId = db.insert(userUid,null, values ) // Use userUid
        db.close()
        return newRowId
    }

    fun getNextAvailableUniqueId(): Int {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT MAX(id) FROM $userUid", null)
        var nextId = 0
        if (cursor.moveToFirst() && !cursor.isNull(0)) {
            nextId = cursor.getInt(0) + 1
        }
        cursor.close()
        db.close()
        return nextId
    }

    fun getAllNotes(): List<Note> {
        val notes = mutableListOf<Note>()
        val db = readableDatabase
        val cursor = db.query(
            userUid, // Use DATABASE_NAME
            null,
            null,
            null,
            null,
            null,
            null
        )
        with(cursor) {
            while (moveToNext()) {
                val note = Note(
                    id = getInt(getColumnIndexOrThrow(NoteContract.NoteEntry.COLUMN_NAME_ID)),
                    heading = getString(getColumnIndexOrThrow(NoteContract.NoteEntry.COLUMN_NAME_HEADING)),
                    body = getString(getColumnIndexOrThrow(NoteContract.NoteEntry.COLUMN_NAME_BODY))
                )
                notes.add(note)
            }
        }
        cursor.close()
        cursor.close()
        db.close()
        return notes
    }

    fun getNoteById(noteId: Int): Note? {
        val db = readableDatabase
        val cursor = db.query(
            userUid, // Use DATABASE_NAME
            null,
            "${NoteContract.NoteEntry.COLUMN_NAME_ID} = ?",
            arrayOf(noteId.toString()),
            null,
            null,
            null
        )
        var note: Note? = null
        with(cursor) {
            if (moveToFirst()) {
                val id = getInt(getColumnIndexOrThrow(NoteContract.NoteEntry.COLUMN_NAME_ID))
                val heading =
                    getString(getColumnIndexOrThrow(NoteContract.NoteEntry.COLUMN_NAME_HEADING))
                val body = getString(getColumnIndexOrThrow(NoteContract.NoteEntry.COLUMN_NAME_BODY))
                note = Note(id, heading, body)
            }
        }
        cursor.close()
        db.close()
        return note
    }
    fun deleteNote(NoteId: Int) {
        val db = writableDatabase
        db.delete(userUid, "id = ?", arrayOf(NoteId.toString()))
        db.close() // Close the database
    }
}

//const val SQL_CREATE_ENTRIES =
//  "CREATE TABLE ${NoteContract.NoteEntry.TABLE_NAME} (" +
//        "${NoteContract.NoteEntry.COLUMN_NAME_ID} INTEGER PRIMARY KEY AUTOINCREMENT," + // Auto-increment ID
//      "${NoteContract.NoteEntry.COLUMN_NAME_HEADING} TEXT," +
//    "${NoteContract.NoteEntry.COLUMN_NAME_BODY} TEXT)"
//const val SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS ${NoteContract.NoteEntry.TABLE_NAME}"