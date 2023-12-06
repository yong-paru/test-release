package com.example.finalexam3

import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class EditItemActivity : AppCompatActivity() {
    private lateinit var editTextItemPrice: EditText
    private lateinit var checkBoxSold: CheckBox
    private lateinit var buttonSaveChanges: Button
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_item)

        // UI 요소 초기화
        editTextItemPrice = findViewById(R.id.editTextItemPrice)
        checkBoxSold = findViewById(R.id.checkBoxSold)
        buttonSaveChanges = findViewById(R.id.buttonSaveChanges)

        // 인텐트에서 아이템 ID 가져오기
        val itemId = intent.getStringExtra("ITEM_ID")
        if (itemId == null) {
            Toast.makeText(this, "Item not found.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        var currentPrice: Double? = null // 현재 가격을 저장할 변수

        // Firestore에서 아이템 데이터 불러오기
        db.collection("items").document(itemId).get().addOnSuccessListener { document ->
            if (document.exists()) {
                currentPrice = document.getDouble("price") // 현재 가격 저장
                editTextItemPrice.setText(currentPrice?.toInt().toString()) // 소수점 없이 표시
                checkBoxSold.isChecked = document.getBoolean("sold") == true
            }
        }

        // 저장 버튼 클릭 리스너 설정
        buttonSaveChanges.setOnClickListener {
            val newPrice = if (editTextItemPrice.text.isNotEmpty()) {
                editTextItemPrice.text.toString().toIntOrNull() ?: currentPrice?.toInt()
            } else {
                currentPrice?.toInt()
            }
            val newSoldStatus = checkBoxSold.isChecked

            // Firestore 문서 업데이트
            newPrice?.let {
                db.collection("items").document(itemId).update(
                    "price", it,
                    "sold", newSoldStatus
                ).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // 변경 사항 저장 후 액티비티 종료
                        finish()
                    } else {
                        // 오류 처리
                        Toast.makeText(this, "Failed to update item.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}