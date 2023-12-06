package com.example.finalexam3

import android.app.DatePickerDialog
import android.content.Intent
import android.icu.util.Calendar
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Locale

class RegisterActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var birthdate: Calendar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = Firebase.auth
        birthdate = Calendar.getInstance()

        findViewById<Button>(R.id.btnBirthdate).setOnClickListener {
            DatePickerDialog(this, dateSetListener, birthdate.get(Calendar.YEAR), birthdate.get(Calendar.MONTH), birthdate.get(Calendar.DAY_OF_MONTH)).show()
        }

        findViewById<Button>(R.id.register)?.setOnClickListener {
            val email = findViewById<EditText>(R.id.email)?.text.toString()
            val password = findViewById<EditText>(R.id.password)?.text.toString()
            val passwordConfirm = findViewById<EditText>(R.id.passwordConfirm)?.text.toString()
            val name = findViewById<EditText>(R.id.name)?.text.toString()

            if (validateForm(email, password, passwordConfirm, name)) {
                createAccount(email, password)
            }
        }
    }

    private val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
        birthdate.set(Calendar.YEAR, year)
        birthdate.set(Calendar.MONTH, month)
        birthdate.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        updateDateInView()
    }

    private fun updateDateInView() {
        val format = "MM/dd/yyyy"
        val sdf = SimpleDateFormat(format, Locale.US)
        findViewById<TextView>(R.id.tvBirthdate).text = "Birthdate: ${sdf.format(birthdate.time)}"
    }

    private fun validateForm(email: String, password: String, passwordConfirm: String, name: String): Boolean {
        // 비밀번호와 비밀번호 확인이 같은지 확인
        if (password != passwordConfirm) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            return false
        }

        // 추가적인 유효성 검사 (예: 이메일 형식 검사, 비밀번호 강도 검사 등)를 여기에 추가할 수 있습니다.

        return true
    }

    private fun createAccount(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    Toast.makeText(baseContext, "Registration successful.", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(baseContext, "Registration failed.", Toast.LENGTH_SHORT).show()
                }
            }
    }
}