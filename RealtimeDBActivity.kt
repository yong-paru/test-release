package com.example.finalexam3

import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class RealtimeDBActivity : AppCompatActivity() {
    private val database = Firebase.database
    private val itemsRef = database.getReference("items")
    private var adapter: MyAdapter? = null
    private val editID by lazy { findViewById<EditText>(R.id.editID) }
    private val recyclerViewItems by lazy { findViewById<RecyclerView>(R.id.recyclerViewItems) }
    private val editItemName by lazy { findViewById<EditText>(R.id.editItemName)}
    private val editPrice by lazy {findViewById<EditText>(R.id.editPrice)}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_realtime_db)

        // recyclerview setup
        recyclerViewItems.layoutManager = LinearLayoutManager(this)
        adapter = MyAdapter(this, emptyList())
        adapter?.setOnItemClickListener {
            queryItem(it)
        }
        recyclerViewItems.adapter = adapter

        findViewById<Button>(R.id.buttonSearch)?.setOnClickListener {
            // 검색 로직 구현
        }

        findViewById<Button>(R.id.buttonUpload)?.setOnClickListener {
            // 업로드 로직 구현
        }

        itemsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val items = mutableListOf<Item>()
                for (child in dataSnapshot.children) {
                    items.add(Item(child.key ?: "", child.value as Map<*, *>))
                }
                adapter?.updateList(items)
            }
            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
            }
        })

        val query = itemsRef.orderByChild("price")
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (child in dataSnapshot.children) {
                    println("${child.key} - ${child.value}")
                }
            }
            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
            }
        })
    }

    private fun addItem() {
        val name = editItemName.text.toString()
        if (name.isEmpty()) {
            Snackbar.make(editItemName, "Input name!", Snackbar.LENGTH_SHORT).show()
            return
        }
        val price = editPrice.text.toString().toInt()
        val itemID = editID.text.toString()

        val itemMap = hashMapOf(
            "name" to name,
            "price" to price
        )
    }

    private fun queryItem(itemID: String) {
        itemsRef.child(itemID).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val map = dataSnapshot.value as Map<*, *>
                editID.setText(itemID)
                editID.isEnabled = true
                editItemName.setText(map["name"].toString())
                editPrice.setText(map["price"].toString())
            }
            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
            }
        })
    }

    private fun updatePrice() {
        val itemID = editID.text.toString()
        val price = editPrice.text.toString().toInt()
        if (itemID.isEmpty()) {
            Snackbar.make(editID, "Input ID!", Snackbar.LENGTH_SHORT).show()
            return
        }
        itemsRef.child(itemID).child("price").setValue(price)
            .addOnSuccessListener { queryItem(itemID) }

        // or

        /*
        val itemMap = hashMapOf(
            "price" to price
        )
        itemsRef.child(itemID).updateChildren(itemMap as Map<String, Any>)
            .addOnSuccessListener { queryItem(itemID) }

        */
    }

    private fun deleteItem() {
        val itemID = editID.text.toString()
        if (itemID.isEmpty()) {
            Snackbar.make(editID, "Input ID!", Snackbar.LENGTH_SHORT).show()
            return
        }
        itemsRef.child(itemID).removeValue()
            .addOnSuccessListener {  }
    }

    private fun incrPrice() {
        val itemID = editID.text.toString()
        if (itemID.isEmpty()) {
            Snackbar.make(editID, "Input ID!", Snackbar.LENGTH_SHORT).show()
            return
        }

        itemsRef.child(itemID).child("price")
            .runTransaction(object : Transaction.Handler {
                override fun doTransaction(mutableData: MutableData): Transaction.Result {
                    var p = mutableData.value.toString().toIntOrNull() ?: 0
                    p++
                    mutableData.value = p
                    return Transaction.success(mutableData)
                }

                override fun onComplete(
                    databaseError: DatabaseError?,
                    committed: Boolean,
                    currentData: DataSnapshot?
                ) {
                    // Transaction completed
                    queryItem(itemID)
                }
            })
    }
}