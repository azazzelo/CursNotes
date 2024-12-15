package com.gzuev.android.notes

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.appcompat.widget.SearchView
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MyAdapter
    private lateinit var notesList: ArrayList<ListItem>
    private lateinit var dbHelper: MyDbHelper
    private lateinit var searchView: SearchView
    private lateinit var categorySpinner: Spinner
    private lateinit var categories: List<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerView)
        searchView = findViewById(R.id.searchView)
        categorySpinner = findViewById(R.id.categorySpinner)
        dbHelper = MyDbHelper(this)

        notesList = dbHelper.getAllNotes()
        adapter = MyAdapter(notesList) { note -> onNoteClick(note) }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        categories = dbHelper.getAllCategories() // Get categories from DB
        val categoryAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        categorySpinner.adapter = categoryAdapter

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    notesList = dbHelper.searchNotes(query)
                    adapter.updateList(notesList)
                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null) {
                    notesList = dbHelper.searchNotes(newText)
                    adapter.updateList(notesList)
                }
                return false
            }
        })

        categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val category = categories[position]
                notesList = dbHelper.getNotesByCategory(category)
                adapter.updateList(notesList)
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {
                notesList = dbHelper.getAllNotes()
                adapter.updateList(notesList)
            }
        }

        val fab: FloatingActionButton = findViewById(R.id.fab)
        fab.setOnClickListener {
            val intent = Intent(this, EditActivity::class.java)
            startActivityForResult(intent, 1)
        }
    }

    private fun onNoteClick(note: ListItem) {
        val intent = Intent(this, EditActivity::class.java)
        intent.putExtra(MyIntentConstants.EXTRA_NOTE_ID, note.id)
        startActivityForResult(intent, 2)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 || requestCode == 2) {
            notesList = dbHelper.getAllNotes()
            adapter.updateList(notesList)
        }
    }
}
