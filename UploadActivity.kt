package com.example.finalexam3

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.finalexam3.databinding.ActivityUploadBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class UploadActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUploadBinding
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUploadBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonPost.setOnClickListener {
            uploadPost()
        }

        binding.buttonBack.setOnClickListener {
            finish()
        }
    }

    private fun uploadPost() {
        val title = binding.editTextItemTitle.text.toString().trim()
        val price = binding.editTextItemPrice.text.toString().trim().toIntOrNull() ?: 0
        val description = binding.editTextItemDescription.text.toString().trim()
        val userEmail = Firebase.auth.currentUser?.email ?: "Unknown"

        val item = hashMapOf(
            "name" to title,
            "price" to price,
            "description" to description,
            "userEmail" to userEmail, // 사용자 이메일 추가
            "sold" to false
        )

        db.collection("items").add(item)
            .addOnSuccessListener {
                finish() // 업로드 후 화면 종료
            }
            .addOnFailureListener { e ->
                // 실패 시 처리
            }
    }
}