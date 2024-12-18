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

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        // Пример обновления базы данных: удаление старых таблиц и создание новых
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NOTES")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_CATEGORIES")
        onCreate(db)
    }

    fun addCategory(categoryName: String, categoryColor: String) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_CATEGORY_NAME, categoryName)
            put(COLUMN_CATEGORY_COLOR, categoryColor)
        }
        db.insert(TABLE_CATEGORIES, null, values)
    }

    fun getAllNotes(): ArrayList<ListItem> {
        val db = readableDatabase
        val cursor = db.rawQuery("""
            SELECT n.*, c.$COLUMN_CATEGORY_COLOR
            FROM $TABLE_NOTES n
            JOIN $TABLE_CATEGORIES c ON n.$COLUMN_CATEGORY = c.$COLUMN_CATEGORY_NAME
            """, null)

        val notes = ArrayList<ListItem>()

        if (cursor.moveToFirst()) {
            do {
                val idIndex = cursor.getColumnIndex(COLUMN_ID)
                val titleIndex = cursor.getColumnIndex(COLUMN_TITLE)
                val descriptionIndex = cursor.getColumnIndex(COLUMN_DESCRIPTION)
                val categoryIndex = cursor.getColumnIndex(COLUMN_CATEGORY)
                val colorIndex = cursor.getColumnIndex(COLUMN_CATEGORY_COLOR)

                // Проверяем индексы перед использованием
                if (idIndex != -1 && titleIndex != -1 && descriptionIndex != -1 && categoryIndex != -1 && colorIndex != -1) {
                    val id = cursor.getLong(idIndex)
                    val title = cursor.getString(titleIndex)
                    val description = cursor.getString(descriptionIndex)
                    val category = cursor.getString(categoryIndex)
                    val categoryColor = cursor.getString(colorIndex)

                    notes.add(ListItem(id, title, description, category, categoryColor))
                }
            } while (cursor.moveToNext())
        }

        cursor.close()
        return notes
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

    fun getNoteById(id: Long): ListItem? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_NOTES,
            null,
            "$COLUMN_ID = ?",
            arrayOf(id.toString()),
            null, null, null
        )

        if (cursor.moveToFirst()) {
            val idIndex = cursor.getColumnIndex(COLUMN_ID)
            val titleIndex = cursor.getColumnIndex(COLUMN_TITLE)
            val descriptionIndex = cursor.getColumnIndex(COLUMN_DESCRIPTION)
            val categoryIndex = cursor.getColumnIndex(COLUMN_CATEGORY)

            if (idIndex != -1 && titleIndex != -1 && descriptionIndex != -1 && categoryIndex != -1) {
                val title = cursor.getString(titleIndex)
                val description = cursor.getString(descriptionIndex)
                val category = cursor.getString(categoryIndex)
                val categoryColor = getCategoryColorByName(category)
                cursor.close()
                return ListItem(id, title, description, category, categoryColor)
            }
        }

        cursor.close()
        return null
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

    fun searchNotes(query: String): ArrayList<ListItem> {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_NOTES,
            null,
            "$COLUMN_TITLE LIKE ? OR $COLUMN_DESCRIPTION LIKE ?",
            arrayOf("%$query%", "%$query%"),
            null,
            null,
            "$COLUMN_ID DESC"
        )

        val notes = ArrayList<ListItem>()

        if (cursor.moveToFirst()) {
            do {
                val idIndex = cursor.getColumnIndex(COLUMN_ID)
                val titleIndex = cursor.getColumnIndex(COLUMN_TITLE)
                val descriptionIndex = cursor.getColumnIndex(COLUMN_DESCRIPTION)
                val categoryIndex = cursor.getColumnIndex(COLUMN_CATEGORY)

                if (idIndex != -1 && titleIndex != -1 && descriptionIndex != -1 && categoryIndex != -1) {
                    val id = cursor.getLong(idIndex)
                    val title = cursor.getString(titleIndex)
                    val description = cursor.getString(descriptionIndex)
                    val category = cursor.getString(categoryIndex)
                    val categoryColor = getCategoryColorByName(category)
                    notes.add(ListItem(id, title, description, category, categoryColor))
                }
            } while (cursor.moveToNext())
        }

        cursor.close()
        return notes
    }

    fun getNotesByCategory(category: String): ArrayList<ListItem> {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_NOTES,
            null,
            "$COLUMN_CATEGORY = ?",
            arrayOf(category),
            null,
            null,
            "$COLUMN_ID DESC"
        )

        val notes = ArrayList<ListItem>()

        if (cursor.moveToFirst()) {
            do {
                val idIndex = cursor.getColumnIndex(COLUMN_ID)
                val titleIndex = cursor.getColumnIndex(COLUMN_TITLE)
                val descriptionIndex = cursor.getColumnIndex(COLUMN_DESCRIPTION)

                if (idIndex != -1 && titleIndex != -1 && descriptionIndex != -1) {
                    val id = cursor.getLong(idIndex)
                    val title = cursor.getString(titleIndex)
                    val description = cursor.getString(descriptionIndex)
                    val categoryColor = getCategoryColorByName(category)
                    notes.add(ListItem(id, title, description, category, categoryColor))
                }
            } while (cursor.moveToNext())
        }

        cursor.close()
        return notes
    }

    private fun getCategoryColorByName(categoryName: String): String {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_CATEGORIES,
            arrayOf(COLUMN_CATEGORY_COLOR),
            "$COLUMN_CATEGORY_NAME = ?",
            arrayOf(categoryName),
            null, null, null
        )

        var categoryColor = ""
        if (cursor.moveToFirst()) {
            val colorIndex = cursor.getColumnIndex(COLUMN_CATEGORY_COLOR)
            if (colorIndex != -1) {
                categoryColor = cursor.getString(colorIndex)
            }
        }

        cursor.close()
        return categoryColor
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
