package com.gzuev.android.notes

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.kernel.pdf.PdfWriter
import android.widget.Toast
import com.itextpdf.kernel.pdf.PdfDocument
import java.io.File
import java.io.FileOutputStream

class EditActivity : AppCompatActivity() {

    private lateinit var titleEditText: EditText
    private lateinit var descriptionEditText: EditText
    private lateinit var categorySpinner: Spinner
    private lateinit var saveButton: Button
    private lateinit var deleteButton: Button
    private lateinit var shareButton: Button
    private lateinit var pdfButton: Button
    private lateinit var dbHelper: MyDbHelper
    private var noteId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)

        titleEditText = findViewById(R.id.titleEditText)
        descriptionEditText = findViewById(R.id.descriptionEditText)
        categorySpinner = findViewById(R.id.categorySpinner)
        saveButton = findViewById(R.id.saveButton)
        deleteButton = findViewById(R.id.deleteButton)
        shareButton = findViewById(R.id.shareButton)
        pdfButton = findViewById(R.id.pdfButton)

        dbHelper = MyDbHelper(this)

        val categories = dbHelper.getAllCategories()
        if (categories.isEmpty()) {
            Toast.makeText(this, "No categories found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        val categoryAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        categorySpinner.adapter = categoryAdapter

        noteId = intent.getLongExtra(MyIntentConstants.EXTRA_NOTE_ID, -1)

        if (noteId != -1L) {
            val note = dbHelper.getNoteById(noteId)
            if (note != null) {
                titleEditText.setText(note.title)
                descriptionEditText.setText(note.description)
                val categoryPosition = categories.indexOf(note.category)
                if (categoryPosition != -1) {
                    categorySpinner.setSelection(categoryPosition)
                }
            }
        }

        saveButton.setOnClickListener {
            val title = titleEditText.text.toString()
            val description = descriptionEditText.text.toString()
            val category = categorySpinner.selectedItem.toString()

            if (noteId == -1L) {
                dbHelper.addNote(title, description, category)
            } else {
                dbHelper.updateNote(noteId, title, description, category)
            }
            setResult(RESULT_OK)
            finish()
        }

        deleteButton.setOnClickListener {
            if (noteId != -1L) {
                dbHelper.deleteNote(noteId)
                setResult(RESULT_OK)
                finish()
            }
        }

        shareButton.setOnClickListener {
            val text = "${titleEditText.text}\n${descriptionEditText.text}"
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, text)
                type = "text/plain"
            }
            startActivity(Intent.createChooser(sendIntent, null))
        }

        pdfButton.setOnClickListener {
            val text = "${titleEditText.text}\n${descriptionEditText.text}"
            createPdf(text)
        }
    }

    private fun createPdf(text: String) {
        try {
            val path = getExternalFilesDir(null)
            val file = File(path, "note_${System.currentTimeMillis()}.pdf")

            // Создаем PdfWriter и PdfDocument
            val pdfWriter = PdfWriter(FileOutputStream(file))
            val pdfDocument = PdfDocument(pdfWriter)
            val document = Document(pdfDocument)

            document.add(Paragraph(text)) // Добавляем текст в документ

            document.close() // Закрываем документ
            Toast.makeText(this, "PDF created: ${file.absolutePath}", Toast.LENGTH_SHORT).show()

            // Открываем PDF-файл
            openPdf(file)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error creating PDF: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openPdf(file: File) {
        val uri: Uri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        startActivity(Intent.createChooser(intent, "Open PDF"))
    }
}
