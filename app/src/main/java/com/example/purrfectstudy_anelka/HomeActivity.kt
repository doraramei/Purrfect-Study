package com.example.purrfectstudy_anelka

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeActivity : AppCompatActivity() {

    private lateinit var tvMatematikaTime: TextView
    private lateinit var tvBindoTime: TextView
    private lateinit var tvBingTime: TextView
    private lateinit var tvMainTimer: TextView
    private lateinit var containerSubjects: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Inisialisasi Views
        tvMatematikaTime = findViewById(R.id.tvMatematikaTime)
        tvBindoTime = findViewById(R.id.tvBindoTime)
        tvBingTime = findViewById(R.id.tvBingTime)
        tvMainTimer = findViewById(R.id.tvMainTimer)
        containerSubjects = findViewById(R.id.containerSubjects)

        // 1. Set Tanggal Otomatis Real-Life
        val tvDate = findViewById<TextView>(R.id.tvDateHeader)
        val currentDate = SimpleDateFormat("EEE, MMM dd, yyyy", Locale.getDefault()).format(Date())
        tvDate?.text = currentDate

        // 2. Navigasi ke Tab Statistics
        val tvTabStatistics = findViewById<TextView>(R.id.tvTabStatistics)
        tvTabStatistics?.setOnClickListener {
            val intent = Intent(this, StatisticsActivity::class.java)
            startActivity(intent)
        }

        // 3. Pasang fungsi menu titik tiga untuk 3 Mapel Default
        setupSubjectMenu(
            findViewById(R.id.layoutMatematika),
            findViewById(R.id.tvMatematikaName),
            tvMatematikaTime,
            findViewById(R.id.btnMoreMatematika),
            containerSubjects,
            "Matematika",
            false
        )
        setupSubjectMenu(
            findViewById(R.id.layoutBindo),
            findViewById(R.id.tvBindoName),
            tvBindoTime,
            findViewById(R.id.btnMoreBindo),
            containerSubjects,
            "Bahasa Indonesia",
            false
        )
        setupSubjectMenu(
            findViewById(R.id.layoutBing),
            findViewById(R.id.tvBingName),
            tvBingTime,
            findViewById(R.id.btnMoreBing),
            containerSubjects,
            "Bahasa Inggris",
            false
        )

        // 4. Logika Tambah Mapel Baru via Tombol "+ Edit subject"
        val btnEditSubject = findViewById<Button>(R.id.btnEditSubject)

        btnEditSubject?.setOnClickListener {
            val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_subject, null)
            val etNewSubject = dialogView.findViewById<EditText>(R.id.etNewSubjectName)
            val btnSave = dialogView.findViewById<Button>(R.id.btnSaveSubject)

            val dialog = AlertDialog.Builder(this)
                .setView(dialogView)
                .create()

            btnSave.setOnClickListener {
                val subjectName = etNewSubject.text.toString().trim()
                if (subjectName.isNotEmpty()) {
                    // Simpan nama mapel custom ke SharedPreferences supaya tidak hilang
                    saveCustomSubjectToPrefs(subjectName)

                    // Buat dan tampilkan UI mapel baru tersebut
                    loadAllStudyTimesAndCustomSubjects()
                    dialog.dismiss()
                }
            }
            dialog.show()
        }
    }

    // Dipanggil otomatis saat kembali ke halaman Home
    override fun onResume() {
        super.onResume()
        loadAllStudyTimesAndCustomSubjects()
    }

    // Simpan nama mapel custom ke SharedPreferences
    private fun saveCustomSubjectToPrefs(name: String) {
        val prefs = getSharedPreferences("PurrfectStudyPrefs", Context.MODE_PRIVATE)
        val customSet = prefs.getStringSet("CUSTOM_SUBJECTS_SET", mutableSetOf()) ?: mutableSetOf()
        val mutableSet = customSet.toMutableSet()
        mutableSet.add(name)
        prefs.edit().putStringSet("CUSTOM_SUBJECTS_SET", mutableSet).apply()
    }

    // Muat ulang semua waktu belajar dan muat kembali mapel custom yang pernah dibuat
    private fun loadAllStudyTimesAndCustomSubjects() {
        val prefs = getSharedPreferences("PurrfectStudyPrefs", Context.MODE_PRIVATE)

        // Cek Reset Jam 12 Malam (Ganti Hari)
        val currentDate = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        val savedDate = prefs.getString("LAST_STUDY_DATE", "")
        if (savedDate != currentDate) {
            prefs.edit().clear().putString("LAST_STUDY_DATE", currentDate).apply()
        }

        // 1. Hitung waktu 3 Mapel Default
        val mathSecs = prefs.getInt("time_Matematika", 0)
        val bindoSecs = prefs.getInt("time_Bahasa Indonesia", 0)
        val bingSecs = prefs.getInt("time_Bahasa Inggris", 0)

        tvMatematikaTime.text = formatSecondsToTime(mathSecs)
        tvBindoTime.text = formatSecondsToTime(bindoSecs)
        tvBingTime.text = formatSecondsToTime(bingSecs)

        var grandTotal = mathSecs + bindoSecs + bingSecs

        // 2. Bersihkan view custom mapel lama sebelum dirender ulang agar tidak menumpuk duplikat
        val viewsToRemove = mutableListOf<View>()
        for (i in 0 until containerSubjects.childCount) {
            val child = containerSubjects.getChildAt(i)
            if (child.tag == "CUSTOM_SUBJECT_VIEW") {
                viewsToRemove.add(child)
            }
        }
        for (v in viewsToRemove) {
            containerSubjects.removeView(v)
        }

        // 3. Muat kembali mapel custom dari SharedPreferences
        val customSet = prefs.getStringSet("CUSTOM_SUBJECTS_SET", emptySet()) ?: emptySet()
        val btnEditSubject = findViewById<Button>(R.id.btnEditSubject)
        val btnEditIndex = containerSubjects.indexOfChild(btnEditSubject)

        var insertIndex = if (btnEditIndex != -1) btnEditIndex else containerSubjects.childCount

        for (subjectName in customSet) {
            val customSecs = prefs.getInt("time_$subjectName", 0)
            grandTotal += customSecs

            // Buat baris item mapel custom secara dinamis
            val scale = resources.displayMetrics.density
            val itemLayout = LinearLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    (50 * scale).toInt()
                )
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                tag = "CUSTOM_SUBJECT_VIEW"
            }

            val dot = View(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    (16 * scale).toInt(),
                    (16 * scale).toInt()
                ).apply { rightMargin = (12 * scale).toInt() }
            }
            dot.background = ContextCompat.getDrawable(this@HomeActivity, R.drawable.ic_play_circle)

            val tvName = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                text = subjectName
                textSize = 16f
                setTextColor(Color.BLACK)
            }

            val tvTime = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                text = formatSecondsToTime(customSecs)
                setTextColor(Color.parseColor("#666666"))
                setPadding(0, 0, (12 * scale).toInt(), 0)
            }

            val btnMore = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                text = "⋮"
                textSize = 20f
                setTextColor(Color.parseColor("#666666"))
                setPadding((8 * scale).toInt(), 0, (8 * scale).toInt(), 0)
                isClickable = true
                isFocusable = true
            }

            // Pasang fungsi klik & menu untuk mapel custom ini
            setupSubjectMenu(itemLayout, tvName, tvTime, btnMore, containerSubjects, subjectName, true)

            itemLayout.addView(dot)
            itemLayout.addView(tvName)
            itemLayout.addView(tvTime)
            itemLayout.addView(btnMore)

            containerSubjects.addView(itemLayout, insertIndex)
            insertIndex++
        }

        // Update timer utama di bagian atas Home
        tvMainTimer.text = formatSecondsToTime(grandTotal)
    }

    private fun formatSecondsToTime(totalSeconds: Int): String {
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val secs = totalSeconds % 60
        return String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, secs)
    }

    // Fungsi pembantu untuk mengatur Klik Mapel & Popup Menu (Edit & Hapus)
    private fun setupSubjectMenu(
        itemLayout: LinearLayout,
        tvName: TextView,
        tvTime: TextView,
        btnMore: TextView,
        container: LinearLayout,
        subjectKeyName: String,
        isCustom: Boolean
    ) {
        // Aksi saat baris mapel diklik -> Buka Focus Timer dengan membawa nama mapel
        itemLayout.isClickable = true
        itemLayout.isFocusable = true
        itemLayout.setOnClickListener {
            val intent = Intent(this, FocusTimerActivity::class.java)
            intent.putExtra("EXTRA_SUBJECT", tvName.text.toString())
            startActivity(intent)
        }

        // Aksi saat tombol titik tiga (⋮) diklik
        btnMore.setOnClickListener { view ->
            val popup = android.widget.PopupMenu(this, view)
            popup.menu.add(0, 1, 0, "Edit")
            popup.menu.add(0, 2, 1, "Hapus")

            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    1 -> {
                        // Menu Edit Nama Mapel
                        val editView = LayoutInflater.from(this).inflate(R.layout.dialog_add_subject, null)
                        val etEdit = editView.findViewById<EditText>(R.id.etNewSubjectName)
                        val btnSubmit = editView.findViewById<Button>(R.id.btnSaveSubject)

                        etEdit.setText(tvName.text.toString())
                        btnSubmit.text = "Simpan Perubahan"

                        val editDialog = AlertDialog.Builder(this)
                            .setView(editView)
                            .create()

                        btnSubmit.setOnClickListener {
                            val newName = etEdit.text.toString().trim()
                            if (newName.isNotEmpty()) {
                                val prefs = getSharedPreferences("PurrfectStudyPrefs", Context.MODE_PRIVATE)
                                val oldTime = prefs.getInt("time_$subjectKeyName", 0)

                                // Pindahkan data waktu ke nama key baru
                                prefs.edit().remove("time_$subjectKeyName")
                                    .putInt("time_$newName", oldTime).apply()

                                if (isCustom) {
                                    val customSet = prefs.getStringSet("CUSTOM_SUBJECTS_SET", mutableSetOf()) ?: mutableSetOf()
                                    val mutableSet = customSet.toMutableSet()
                                    mutableSet.remove(subjectKeyName)
                                    mutableSet.add(newName)
                                    prefs.edit().putStringSet("CUSTOM_SUBJECTS_SET", mutableSet).apply()
                                }

                                tvName.text = newName
                                editDialog.dismiss()
                                loadAllStudyTimesAndCustomSubjects()
                            }
                        }
                        editDialog.show()
                        true
                    }
                    2 -> {
                        // Menu Hapus Mapel
                        AlertDialog.Builder(this)
                            .setTitle("Hapus Mapel")
                            .setMessage("Yakin ingin menghapus mapel ${tvName.text}?")
                            .setPositiveButton("Hapus") { _, _ ->
                                val prefs = getSharedPreferences("PurrfectStudyPrefs", Context.MODE_PRIVATE)
                                prefs.edit().remove("time_$subjectKeyName").apply()

                                if (isCustom) {
                                    val customSet = prefs.getStringSet("CUSTOM_SUBJECTS_SET", mutableSetOf()) ?: mutableSetOf()
                                    val mutableSet = customSet.toMutableSet()
                                    mutableSet.remove(subjectKeyName)
                                    prefs.edit().putStringSet("CUSTOM_SUBJECTS_SET", mutableSet).apply()
                                }

                                container.removeView(itemLayout)
                                loadAllStudyTimesAndCustomSubjects()
                            }
                            .setNegativeButton("Batal", null)
                            .show()
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }
    }
}