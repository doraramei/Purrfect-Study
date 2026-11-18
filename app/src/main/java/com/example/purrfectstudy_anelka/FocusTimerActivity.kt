package com.example.purrfectstudy_anelka

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FocusTimerActivity : AppCompatActivity() {

    private var sessionSeconds = 0 // Waktu yang dihitung khusus sesi ini
    private var isRunning = true
    private lateinit var handler: Handler
    private lateinit var runnable: Runnable
    private lateinit var subjectName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_focus_timer)

        // 1. Ambil Nama Mapel yang dikirim dari Home
        subjectName = intent.getStringExtra("EXTRA_SUBJECT") ?: "Focus timer"
        val tvTitle = findViewById<TextView>(R.id.tvSubjectTitle)
        tvTitle.text = subjectName

        // 2. Tombol Kembali (Simpan waktu dulu sebelum keluar)
        val btnBack = findViewById<TextView>(R.id.btnBack)
        btnBack.setOnClickListener {
            saveStudyTimeAndExit()
        }

        // 3. Logika Timer Berjalan
        val tvRoomTimer = findViewById<TextView>(R.id.tvRoomTimer)
        val tvMyMemberTimer = findViewById<TextView>(R.id.tvMyMemberTimer)
        val btnPauseTimer = findViewById<Button>(R.id.btnPauseTimer)

        handler = Handler(Looper.getMainLooper())
        runnable = object : Runnable {
            override fun run() {
                if (isRunning) {
                    sessionSeconds++

                    val hours = sessionSeconds / 3600
                    val minutes = (sessionSeconds % 3600) / 60
                    val secs = sessionSeconds % 60

                    val timeFormatted = String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, secs)
                    tvRoomTimer.text = timeFormatted
                    tvMyMemberTimer.text = timeFormatted
                }
                handler.postDelayed(this, 1000)
            }
        }
        handler.post(runnable)

        // 4. Tombol Pause / Play
        btnPauseTimer.setOnClickListener {
            isRunning = !isRunning
            if (isRunning) {
                btnPauseTimer.text = "⏸"
            } else {
                btnPauseTimer.text = "▶"
            }
        }
    }

    // Fungsi untuk menyimpan waktu belajar ke SharedPreferences saat keluar
    private fun saveStudyTimeAndExit() {
        if (sessionSeconds > 0) {
            val prefs = getSharedPreferences("PurrfectStudyPrefs", Context.MODE_PRIVATE)

            // Cek Reset Otomatis Jam 12 Malam (Ganti Hari)
            val currentDate = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
            val savedDate = prefs.getString("LAST_STUDY_DATE", "")

            val editor = prefs.edit()
            if (savedDate != currentDate) {
                // Jika sudah ganti hari, reset semua catatan waktu jadi 0
                editor.clear()
                editor.putString("LAST_STUDY_DATE", currentDate)
            }

            // Tambahkan waktu sesi ini ke total waktu mapel tersebut
            val keyTime = "time_$subjectName"
            val existingSeconds = prefs.getInt(keyTime, 0)
            val totalSecondsForSubject = existingSeconds + sessionSeconds

            editor.putInt(keyTime, totalSecondsForSubject)
            editor.apply()
        }
        finish()
    }

    // Tangani juga jika user menekan tombol back HP fisik
    override fun onBackPressed() {
        saveStudyTimeAndExit()
        super.onBackPressed()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(runnable)
    }
}