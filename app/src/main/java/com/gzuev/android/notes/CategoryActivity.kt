package com.gzuev.android.notes

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class CategoryActivity : AppCompatActivity() {

    private lateinit var categoryNameEditText: EditText
    private lateinit var categoryColorEditText: EditText
    private lateinit var saveButton: Button
    private lateinit var dbHelper: MyDbHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category)

        categoryNameEditText = findViewById(R.id.categoryNameEditText)
        categoryColorEditText = findViewById(R.id.categoryColorEditText)
        saveButton = findViewById(R.id.saveButton)

        dbHelper = MyDbHelper(this)

        saveButton.setOnClickListener {
            val categoryName = categoryNameEditText.text.toString()
            val categoryColor = categoryColorEditText.text.toString()

            if (categoryName.isNotEmpty() && categoryColor.isNotEmpty()) {
                dbHelper.addCategory(categoryName, categoryColor)
                setResult(RESULT_OK)
                finish()
            } else {
                Toast.makeText(this, "Please enter both category name and color", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
