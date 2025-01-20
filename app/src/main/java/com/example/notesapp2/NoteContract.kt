package com.example.notesapp2
import android.provider.BaseColumns
object NoteContract {
    object NoteEntry : BaseColumns {
        const val COLUMN_NAME_ID = "id" // PK
        const val COLUMN_NAME_HEADING = "heading"
        const val COLUMN_NAME_BODY = "body"
    }
}