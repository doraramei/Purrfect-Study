package com.example.purrfectstudy_anelka

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView

class LoginActivity : AppCompatActivity() {

    private lateinit var etLoginNickname: EditText
    private lateinit var etLoginCategory: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        findViewById<NestedScrollView>(R.id.scrollLogin).enableMouseWheelScroll()

        etLoginNickname = findViewById(R.id.etLoginNickname)
        etLoginCategory = findViewById(R.id.etLoginCategory)

        findViewById<TextView>(R.id.btnStartStudying).setOnClickListener {
            val nicknameInput = etLoginNickname.text.toString().trim()
            val categoryInput = etLoginCategory.text.toString().trim()
            val finalNickname = nicknameInput.ifEmpty { StudyRepository.getUserNickname(this) }
            val finalCategory = categoryInput.ifEmpty { "Kelas 12 / UTBK 2027" }
            completeLogin(finalNickname, finalCategory)
        }

        findViewById<TextView>(R.id.tvContinueAsSyilawaaw).setOnClickListener {
            val categoryInput = etLoginCategory.text.toString().trim().ifEmpty { "Kelas 12 / UTBK 2027" }
            completeLogin("syilawaaw", categoryInput)
        }
    }

    private fun completeLogin(nickname: String, category: String) {
        StudyRepository.setUserNickname(this, nickname)
        StudyRepository.setUserCategory(this, category)
        StudyRepository.setLoggedIn(this, true)

        startActivity(Intent(this, HomeActivity::class.java))
        @Suppress("DEPRECATION")
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }
}
