package com.gzuev.android.notes

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class MyDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase?) {
        val createNotesTable = "CREATE TABLE $TABLE_NOTES ($COLUMN_ID INTEGER PRIMARY KEY, $COLUMN_TITLE TEXT, $COLUMN_DESCRIPTION TEXT, $COLUMN_CATEGORY TEXT)"
        db?.execSQL(createNotesTable)

        val createCategoriesTable = "CREATE TABLE $TABLE_CATEGORIES ($COLUMN_ID INTEGER PRIMARY KEY, $COLUMN_CATEGORY_NAME TEXT, $COLUMN_CATEGORY_COLOR TEXT)"
        db?.execSQL(createCategoriesTable)
    }

    fun addNote(title: String, description: String, category: String) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TITLE, title)
            put(COLUMN_DESCRIPTION, description)
            put(COLUMN_CATEGORY, category)
        }
        db.insert(TABLE_NOTES, null, values)
    }

    fun getAllNotes(): ArrayList<ListItem> {
        val db = readableDatabase
        val cursor = db.query(TABLE_NOTES, null, null, null, null, null, "$COLUMN_ID DESC")
        val notes = ArrayList<ListItem>()

        // Проверяем индексы колонок
        val columnIdIndex = cursor.getColumnIndex(COLUMN_ID)
        val columnTitleIndex = cursor.getColumnIndex(COLUMN_TITLE)
        val columnDescriptionIndex = cursor.getColumnIndex(COLUMN_DESCRIPTION)
        val columnCategoryIndex = cursor.getColumnIndex(COLUMN_CATEGORY)

        // Если хотя бы один индекс возвращает -1, пропускаем обработку
        if (columnIdIndex != -1 && columnTitleIndex != -1 && columnDescriptionIndex != -1 && columnCategoryIndex != -1) {
            while (cursor.moveToNext()) {
                val id = cursor.getLong(columnIdIndex)
                val title = cursor.getString(columnTitleIndex)
                val description = cursor.getString(columnDescriptionIndex)
                val category = cursor.getString(columnCategoryIndex)
                notes.add(ListItem(id, title, description, category))
            }
        }

        cursor.close()
        return notes
    }

    fun getNotesByCategory(category: String): ArrayList<ListItem> {
        val db = readableDatabase
        val cursor = db.query(TABLE_NOTES, null, "$COLUMN_CATEGORY = ?", arrayOf(category), null, null, "$COLUMN_ID DESC")
        val notes = ArrayList<ListItem>()

        // Проверяем индексы колонок
        val columnIdIndex = cursor.getColumnIndex(COLUMN_ID)
        val columnTitleIndex = cursor.getColumnIndex(COLUMN_TITLE)
        val columnDescriptionIndex = cursor.getColumnIndex(COLUMN_DESCRIPTION)
        val columnCategoryIndex = cursor.getColumnIndex(COLUMN_CATEGORY)

        if (columnIdIndex != -1 && columnTitleIndex != -1 && columnDescriptionIndex != -1 && columnCategoryIndex != -1) {
            while (cursor.moveToNext()) {
                val id = cursor.getLong(columnIdIndex)
                val title = cursor.getString(columnTitleIndex)
                val description = cursor.getString(columnDescriptionIndex)
                val category = cursor.getString(columnCategoryIndex)
                notes.add(ListItem(id, title, description, category))
            }
        }

        cursor.close()
        return notes
    }

    fun getNoteById(id: Long): ListItem? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_NOTES,
            null,
            "$COLUMN_ID = ?",
            arrayOf(id.toString()),
            null, null, null
        )

        // Проверка наличия данных
        if (cursor.moveToFirst()) {
            val columnIdIndex = cursor.getColumnIndex(COLUMN_ID)
            val columnTitleIndex = cursor.getColumnIndex(COLUMN_TITLE)
            val columnDescriptionIndex = cursor.getColumnIndex(COLUMN_DESCRIPTION)
            val columnCategoryIndex = cursor.getColumnIndex(COLUMN_CATEGORY)

            // Если хотя бы один индекс -1, пропускаем обработку
            if (columnIdIndex != -1 && columnTitleIndex != -1 && columnDescriptionIndex != -1 && columnCategoryIndex != -1) {
                val title = cursor.getString(columnTitleIndex)
                val description = cursor.getString(columnDescriptionIndex)
                val category = cursor.getString(columnCategoryIndex)
                cursor.close()
                return ListItem(id, title, description, category)
            }
        }

        cursor.close()
        return null
    }


    fun searchNotes(query: String): ArrayList<ListItem> {
        val db = readableDatabase
        val cursor = db.query(TABLE_NOTES, null, "$COLUMN_TITLE LIKE ? OR $COLUMN_DESCRIPTION LIKE ?", arrayOf("%$query%", "%$query%"), null, null, "$COLUMN_ID DESC")
        val notes = ArrayList<ListItem>()

        // Проверяем индексы колонок
        val columnIdIndex = cursor.getColumnIndex(COLUMN_ID)
        val columnTitleIndex = cursor.getColumnIndex(COLUMN_TITLE)
        val columnDescriptionIndex = cursor.getColumnIndex(COLUMN_DESCRIPTION)

        if (columnIdIndex != -1 && columnTitleIndex != -1 && columnDescriptionIndex != -1) {
            while (cursor.moveToNext()) {
                val id = cursor.getLong(columnIdIndex)
                val title = cursor.getString(columnTitleIndex)
                val description = cursor.getString(columnDescriptionIndex)
                notes.add(ListItem(id, title, description, ""))
            }
        }

        cursor.close()
        return notes
    }

    fun updateNote(id: Long, title: String, description: String, category: String) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TITLE, title)
            put(COLUMN_DESCRIPTION, description)
            put(COLUMN_CATEGORY, category)
        }
        db.update(TABLE_NOTES, values, "$COLUMN_ID = ?", arrayOf(id.toString()))
    }

    fun deleteNote(id: Long) {
        val db = writableDatabase
        db.delete(TABLE_NOTES, "$COLUMN_ID = ?", arrayOf(id.toString()))
    }

    fun getAllCategories(): List<String> {
        val db = readableDatabase
        val cursor = db.query(TABLE_CATEGORIES, arrayOf(COLUMN_CATEGORY_NAME), null, null, null, null, null)
        val categories = mutableListOf<String>()

        // Проверяем индекс колонки
        val columnCategoryNameIndex = cursor.getColumnIndex(COLUMN_CATEGORY_NAME)

        if (columnCategoryNameIndex != -1) {
            while (cursor.moveToNext()) {
                categories.add(cursor.getString(columnCategoryNameIndex))
            }
        }

        cursor.close()
        return categories
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NOTES")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_CATEGORIES")
        onCreate(db)
    }

    companion object {
        const val DATABASE_NAME = "notes_db"
        const val DATABASE_VERSION = 1
        const val TABLE_NOTES = "notes"
        const val COLUMN_ID = "_id"
        const val COLUMN_TITLE = "title"
        const val COLUMN_DESCRIPTION = "description"
        const val COLUMN_CATEGORY = "category"
        const val TABLE_CATEGORIES = "categories"
        const val COLUMN_CATEGORY_NAME = "category_name"
        const val COLUMN_CATEGORY_COLOR = "category_color"
    }
}
