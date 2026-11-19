package com.example.purrfectstudy_anelka

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView

class SettingsActivity : AppCompatActivity() {

    private lateinit var tvValNickname: TextView
    private lateinit var tvValStatus: TextView
    private lateinit var tvValStudicon: TextView
    private lateinit var tvValCategory: TextView
    private lateinit var tvValTheme: TextView
    private lateinit var tvValRegion: TextView
    private lateinit var tvValLanguage: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        findViewById<TextView>(R.id.btnSettingsBack).setOnClickListener {
            finish()
            @Suppress("DEPRECATION")
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        findViewById<NestedScrollView>(R.id.scrollSettings).enableMouseWheelScroll()

        tvValNickname = findViewById(R.id.tvValNickname)
        tvValStatus = findViewById(R.id.tvValStatus)
        tvValStudicon = findViewById(R.id.tvValStudicon)
        tvValCategory = findViewById(R.id.tvValCategory)
        tvValTheme = findViewById(R.id.tvValTheme)
        tvValRegion = findViewById(R.id.tvValRegion)
        tvValLanguage = findViewById(R.id.tvValLanguage)

        findViewById<LinearLayout>(R.id.rowNickname).setOnClickListener {
            showEditDialog(
                title = "Edit Nickname",
                current = StudyRepository.getUserNickname(this),
                hint = "Masukkan nickname (disinkronkan ke grup)"
            ) { newVal ->
                StudyRepository.setUserNickname(this, newVal)
                refreshValues()
                Toast.makeText(this, "Nickname disinkronkan: ${StudyRepository.getUserNickname(this)}", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<LinearLayout>(R.id.rowStatusMessage).setOnClickListener {
            showEditDialog(
                title = "Status Message",
                current = StudyRepository.getStatusMessage(this),
                hint = "Tulis target atau motivasi belajar..."
            ) { newVal ->
                StudyRepository.setStatusMessage(this, newVal)
                refreshValues()
            }
        }

        findViewById<LinearLayout>(R.id.rowStudiconSettings).setOnClickListener {
            startActivity(Intent(this, StudiconStoreActivity::class.java))
            @Suppress("DEPRECATION")
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        findViewById<LinearLayout>(R.id.rowCategory).setOnClickListener {
            val options = arrayOf("SMP / Sederajat", "Kelas 10", "Kelas 11", "Kelas 12", "Gap Year / Alumni", "Mahasiswa", "Umum")
            showOptionPicker("Pilih Category", options, StudyRepository.getUserCategory(this)) { picked ->
                StudyRepository.setUserCategory(this, picked)
                refreshValues()
            }
        }

        findViewById<LinearLayout>(R.id.rowThemeSettings).setOnClickListener {
            val options = arrayOf("Monokrom Terang", "Monokrom Gelap")
            showOptionPicker("Theme Settings", options, StudyRepository.getThemeMode(this)) { picked ->
                StudyRepository.setThemeMode(this, picked)
                refreshValues()
                Toast.makeText(this, "Tema dipilih: $picked", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<LinearLayout>(R.id.rowRegion).setOnClickListener {
            val options = arrayOf("Indonesia", "Singapore", "Malaysia", "Japan", "South Korea", "Global")
            showOptionPicker("Select Region", options, StudyRepository.getUserRegion(this)) { picked ->
                StudyRepository.setUserRegion(this, picked)
                refreshValues()
            }
        }

        findViewById<LinearLayout>(R.id.rowLanguage).setOnClickListener {
            val options = arrayOf("Indonesia", "English")
            showOptionPicker("Select Language", options, StudyRepository.getUserLanguage(this)) { picked ->
                StudyRepository.setUserLanguage(this, picked)
                refreshValues()
            }
        }

        findViewById<LinearLayout>(R.id.rowBlockedUsers).setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Blocked Users")
                .setMessage("Saat ini tidak ada pengguna yang diblokir (0 people).")
                .setPositiveButton("Tutup", null)
                .show()
        }

        findViewById<LinearLayout>(R.id.rowTerms).setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Terms of Service")
                .setMessage("Purrfect Study digunakan untuk mencatat waktu fokus, agenda, target planner, dan diskusi grup belajar secara sehat dan suportif.")
                .setPositiveButton("Mengerti", null)
                .show()
        }

        findViewById<LinearLayout>(R.id.rowPrivacy).setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Privacy Policy")
                .setMessage("Seluruh catatan belajar, jadwal kalender, dan preferensi akun Anda tersimpan aman secara lokal di perangkat Anda.")
                .setPositiveButton("Tutup", null)
                .show()
        }

        findViewById<LinearLayout>(R.id.rowAppVersion).setOnClickListener {
            Toast.makeText(this, "Purrfect Study v810.1.18 • Terbaru", Toast.LENGTH_SHORT).show()
        }

        findViewById<TextView>(R.id.rowSignOut).setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Sign Out")
                .setMessage("Keluar dari akun ${StudyRepository.getUserNickname(this)}?")
                .setPositiveButton("Sign out") { _, _ ->
                    StudyRepository.setLoggedIn(this, false)
                    val intent = Intent(this, LoginActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    }
                    startActivity(intent)
                    @Suppress("DEPRECATION")
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    finish()
                }
                .setNegativeButton("Batal", null)
                .show()
        }

        findViewById<TextView>(R.id.rowDeleteAccount).setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Delete Account")
                .setMessage("Semua riwayat belajar, grup kustom, koin, dan pengaturan akan dihapus permanen. Lanjutkan?")
                .setPositiveButton("Hapus") { _, _ ->
                    getSharedPreferences(StudyRepository.PREFS_NAME, Context.MODE_PRIVATE)
                        .edit().clear().apply()
                    Toast.makeText(this, "Akun dan data lokal telah dihapus", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, LoginActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    }
                    startActivity(intent)
                    @Suppress("DEPRECATION")
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    finish()
                }
                .setNegativeButton("Batal", null)
                .show()
        }

        refreshValues()
    }

    override fun onResume() {
        super.onResume()
        refreshValues()
    }

    private fun refreshValues() {
        tvValNickname.text = StudyRepository.getUserNickname(this)
        tvValStatus.text = StudyRepository.getStatusMessage(this)
        tvValStudicon.text = StudyRepository.getEquippedStudiconName(this)
        tvValCategory.text = StudyRepository.getUserCategory(this)
        tvValTheme.text = StudyRepository.getThemeMode(this)
        tvValRegion.text = StudyRepository.getUserRegion(this)
        tvValLanguage.text = StudyRepository.getUserLanguage(this)
    }

    private fun showEditDialog(title: String, current: String, hint: String, onSave: (String) -> Unit) {
        val etInput = EditText(this).apply {
            setText(current)
            this.hint = hint
        }
        val wrapper = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 24, 48, 12)
            addView(etInput)
        }
        AlertDialog.Builder(this)
            .setTitle(title)
            .setView(wrapper)
            .setPositiveButton("Simpan") { _, _ ->
                onSave(etInput.text.toString())
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun showOptionPicker(title: String, options: Array<String>, current: String, onPick: (String) -> Unit) {
        val checkedIdx = options.indexOf(current).let { if (it >= 0) it else 0 }
        AlertDialog.Builder(this)
            .setTitle(title)
            .setSingleChoiceItems(options, checkedIdx) { dialog, which ->
                onPick(options[which])
                dialog.dismiss()
            }
            .setNegativeButton("Batal", null)
            .show()
    }
}
