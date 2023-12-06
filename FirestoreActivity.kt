package com.example.finalexam3

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.CheckBox
import android.widget.Spinner
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class FirestoreActivity : AppCompatActivity() {
    private lateinit var adapter: MyAdapter
    private val db: FirebaseFirestore = Firebase.firestore
    private val itemsCollectionRef = db.collection("items")

    private lateinit var spinnerSort: Spinner
    private lateinit var checkBoxOnSale: CheckBox
    private lateinit var checkBoxSold: CheckBox
    private val recyclerViewItems by lazy { findViewById<RecyclerView>(R.id.recyclerViewItems) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_firestore)

        // Adapter 초기화
        adapter = MyAdapter(this, emptyList())
        // 아이템 클릭 시 다이얼로그 표시
        adapter.onItemClick = { item ->
            showItemDetailDialog(item)
        }

        recyclerViewItems.layoutManager = LinearLayoutManager(this)
        recyclerViewItems.adapter = adapter

        spinnerSort = findViewById(R.id.spinnerSort)
        checkBoxOnSale = findViewById(R.id.checkBoxOnSale)
        checkBoxSold = findViewById(R.id.checkBoxSold)
        checkBoxOnSale.setOnCheckedChangeListener { _, _ -> updateList() }
        checkBoxSold.setOnCheckedChangeListener { _, _ -> updateList() }

        setupSpinner()
        updateList()  // 리스트 아이템 초기 업데이트

        findViewById<Button>(R.id.buttonUpload).setOnClickListener {
            val intent = Intent(this, UploadActivity::class.java)
            startActivity(intent)
        }
    }

    // 아이템 상세 정보를 표시하는 다이얼로그를 보여주는 함수
    private fun showItemDetailDialog(item: Item) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_item_detail, null)
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setView(dialogView)

        // 다이얼로그에 아이템 정보 설정
        dialogView.findViewById<TextView>(R.id.textViewDetailTitle).text = item.name
        dialogView.findViewById<TextView>(R.id.textViewDetailPrice).text = item.price.toString()
        dialogView.findViewById<TextView>(R.id.textViewDetailuserEmail).text = item.userEmail
        dialogView.findViewById<TextView>(R.id.textViewDetailDescription).text = item.description

        // 판매 상태를 "판매중" 또는 "판매완료"로 설정
        val soldStatusText = if (item.sold) "판매완료" else "판매중"
        dialogView.findViewById<TextView>(R.id.textViewDetailSold).text = soldStatusText


        val currentUserId = Firebase.auth.currentUser?.email
        // 본인 게시물인 경우
        dialogView.findViewById<Button>(R.id.buttonEditItem).apply {
            visibility = View.VISIBLE
            setOnClickListener {
                val editIntent = Intent(this@FirestoreActivity, EditItemActivity::class.java)
                editIntent.putExtra("ITEM_ID", item.id) // 아이템 ID를 인텐트에 추가
                startActivity(editIntent)
            }
        }

        dialogBuilder.setPositiveButton("Close") { dialog, _ -> dialog.dismiss() }
        dialogBuilder.create().show()
    }

    private fun setupSpinner() {
        val options = arrayOf("이름 오름차순", "이름 내림차순", "가격 오름차순", "가격 내림차순")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, options)
        spinnerSort.adapter = adapter
        spinnerSort.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                updateList()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
        checkBoxOnSale.setOnCheckedChangeListener { _, _ -> updateList() }
        checkBoxSold.setOnCheckedChangeListener { _, _ -> updateList() }
    }

    private fun updateList() {
        var query: Query = itemsCollectionRef

        // 정렬
        when (spinnerSort.selectedItem.toString()) {
            "이름 오름차순" -> query = query.orderBy("name", Query.Direction.ASCENDING)
            "이름 내림차순" -> query = query.orderBy("name", Query.Direction.DESCENDING)
            "가격 오름차순" -> query = query.orderBy("price", Query.Direction.ASCENDING)
            "가격 내림차순" -> query = query.orderBy("price", Query.Direction.DESCENDING)
        }

        val filterOnSale = checkBoxOnSale.isChecked
        val filterSold = checkBoxSold.isChecked

        if (filterOnSale && filterSold) {
            // 두 체크박스 모두 체크된 경우, 모든 아이템을 표시합니다.
        } else if (filterOnSale) {
            // 판매중인 아이템만 필터링
            query = query.whereEqualTo("sold", false)
        } else if (filterSold) {
            // 판매완료된 아이템만 필터링
            query = query.whereEqualTo("sold", true)
        }

        // 쿼리 실행
        query.get().addOnSuccessListener { documents ->
            val items = documents.map { doc -> Item(doc) }
            adapter.updateList(items)
        }.addOnFailureListener { e ->
            Snackbar.make(recyclerViewItems, "Error: ${e.message}", Snackbar.LENGTH_LONG).show()
        }
    }

    // 기타 필요한 메소드들 ...
}