package com.example.purrfectstudy_anelka

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeActivity : AppCompatActivity() {

    private lateinit var tvDateHeader: TextView
    private lateinit var tvMainTimer: TextView
    private lateinit var scrollSubjects: NestedScrollView
    private lateinit var containerSubjects: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        tvDateHeader = findViewById(R.id.tvDateHeader)
        tvMainTimer = findViewById(R.id.tvMainTimer)
        scrollSubjects = findViewById(R.id.scrollSubjects)
        scrollSubjects.enableMouseWheelScroll()
        containerSubjects = findViewById(R.id.containerSubjects)

        // 1. Set Tanggal Dinamis
        val currentDate = SimpleDateFormat("EEE, MM/dd", Locale.ENGLISH).format(Date())
        tvDateHeader.text = currentDate

        // 2. 2 Tab Navigasi Horizontal (Timer | Statistics)
        findViewById<TextView>(R.id.tvTabTimer).setOnClickListener {
            scrollSubjects.smoothScrollTo(0, 0)
        }

        findViewById<TextView>(R.id.tvTabStatistics).setOnClickListener {
            startActivity(Intent(this, StatisticsActivity::class.java))
            @Suppress("DEPRECATION")
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        // 3. Tombol "+ Edit subject"
        findViewById<TextView>(R.id.btnEditSubject).setOnClickListener {
            showAddSubjectDialog()
        }

        // 4. Bottom Navigation Bar dengan transisi halus
        findViewById<FrameLayout>(R.id.navHome).setOnClickListener {
            scrollSubjects.smoothScrollTo(0, 0)
        }

        findViewById<LinearLayout>(R.id.navTodo).setOnClickListener {
            val intent = Intent(this, TodoActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            startActivity(intent)
            @Suppress("DEPRECATION")
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        findViewById<LinearLayout>(R.id.navCalendar).setOnClickListener {
            val intent = Intent(this, CalendarActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            startActivity(intent)
            @Suppress("DEPRECATION")
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        findViewById<LinearLayout>(R.id.navGroups).setOnClickListener {
            val intent = Intent(this, GroupsListActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            startActivity(intent)
            @Suppress("DEPRECATION")
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        findViewById<FrameLayout>(R.id.btnQuickSettings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
            @Suppress("DEPRECATION")
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }

    override fun onResume() {
        super.onResume()
        loadAllStudyTimesAndSubjects()
    }

    private fun openFocusTimer(subjectName: String) {
        val intent = Intent(this, FocusTimerActivity::class.java)
        intent.putExtra("EXTRA_SUBJECT", subjectName)
        startActivity(intent)
        @Suppress("DEPRECATION")
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    private fun showAddSubjectDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_subject, null)
        val etNewSubject = dialogView.findViewById<EditText>(R.id.etNewSubjectName)
        val btnSave = dialogView.findViewById<Button>(R.id.btnSaveSubject)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        btnSave.setOnClickListener {
            val subjectName = etNewSubject.text.toString().trim()
            if (subjectName.isNotEmpty()) {
                addSubjectToPrefs(subjectName)
                loadAllStudyTimesAndSubjects()
                dialog.dismiss()
            } else {
                Toast.makeText(this, "Nama mata pelajaran tidak boleh kosong", Toast.LENGTH_SHORT).show()
            }
        }
        dialog.show()
    }

    private fun addSubjectToPrefs(name: String) {
        val prefs = getSharedPreferences(StudyRepository.PREFS_NAME, Context.MODE_PRIVATE)
        val deletedDefaults = prefs.getStringSet("DELETED_DEFAULT_SUBJECTS_SET", emptySet())?.toMutableSet() ?: mutableSetOf()
        val customSet = prefs.getStringSet("CUSTOM_SUBJECTS_SET", emptySet())?.toMutableSet() ?: mutableSetOf()

        if (StudyRepository.defaultHomeSubjects.contains(name)) {
            deletedDefaults.remove(name)
        } else {
            customSet.add(name)
        }

        prefs.edit()
            .putStringSet("DELETED_DEFAULT_SUBJECTS_SET", deletedDefaults)
            .putStringSet("CUSTOM_SUBJECTS_SET", customSet)
            .apply()
    }

    private fun loadAllStudyTimesAndSubjects() {
        StudyRepository.ensureDailyResetWithoutLosingConfig(this)
        val prefs = getSharedPreferences(StudyRepository.PREFS_NAME, Context.MODE_PRIVATE)

        val activeNames = StudyRepository.getActiveHomeSubjects(this)
        val deletedDefaults = prefs.getStringSet("DELETED_DEFAULT_SUBJECTS_SET", emptySet()) ?: emptySet()

        containerSubjects.removeAllViews()
        val inflater = LayoutInflater.from(this)
        var grandTotal = 0

        for (subjectName in activeNames) {
            val isCustom = !StudyRepository.defaultHomeSubjects.contains(subjectName) || deletedDefaults.contains(subjectName)
            val secs = prefs.getInt("time_$subjectName", 0)
            grandTotal += secs

            val rowView = inflater.inflate(R.layout.item_subject, containerSubjects, false)
            val btnPlay = rowView.findViewById<View>(R.id.btnPlaySubject)
            val tvName = rowView.findViewById<TextView>(R.id.tvSubjectName)
            val tvTime = rowView.findViewById<TextView>(R.id.tvSubjectTime)
            val btnMore = rowView.findViewById<TextView>(R.id.btnMoreSubject)

            tvName.text = subjectName
            tvTime.text = StudyRepository.formatSecondsToTime(secs)

            setupSubjectRowActions(rowView, btnPlay, btnMore, subjectName, isCustom)
            containerSubjects.addView(rowView)
        }

        for ((key, value) in prefs.all) {
            if (key.startsWith("time_") && value is Int) {
                val sub = key.removePrefix("time_")
                if (!activeNames.contains(sub)) {
                    grandTotal += value
                }
            }
        }

        tvMainTimer.text = StudyRepository.formatSecondsToTime(grandTotal)
    }

    private fun setupSubjectRowActions(
        rowView: View,
        btnPlay: View,
        btnMore: TextView,
        subjectName: String,
        isCustom: Boolean
    ) {
        val launchSession = View.OnClickListener {
            openFocusTimer(subjectName)
        }
        rowView.setOnClickListener(launchSession)
        btnPlay.setOnClickListener(launchSession)

        val showSubjectMenu: (View) -> Unit = { anchor ->
            val popup = PopupMenu(this, anchor)
            popup.menu.add(0, 1, 0, "Edit")
            popup.menu.add(0, 2, 1, "Hapus")

            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    1 -> {
                        showEditSubjectDialog(subjectName, isCustom)
                        true
                    }
                    2 -> {
                        showDeleteSubjectDialog(subjectName, isCustom)
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }

        btnMore.setOnClickListener { anchor -> showSubjectMenu(anchor) }
        rowView.setOnLongClickListener { anchor ->
            showSubjectMenu(anchor)
            true
        }
    }

    private fun showEditSubjectDialog(oldName: String, isCustom: Boolean) {
        val editView = LayoutInflater.from(this).inflate(R.layout.dialog_add_subject, null)
        val etEdit = editView.findViewById<EditText>(R.id.etNewSubjectName)
        val btnSubmit = editView.findViewById<Button>(R.id.btnSaveSubject)

        etEdit.setText(oldName)
        btnSubmit.text = "Simpan Perubahan"

        val editDialog = AlertDialog.Builder(this)
            .setView(editView)
            .create()

        btnSubmit.setOnClickListener {
            val newName = etEdit.text.toString().trim()
            if (newName.isNotEmpty() && newName != oldName) {
                val prefs = getSharedPreferences(StudyRepository.PREFS_NAME, Context.MODE_PRIVATE)
                val oldTime = prefs.getInt("time_$oldName", 0)

                val deletedDefaults = prefs.getStringSet("DELETED_DEFAULT_SUBJECTS_SET", emptySet())?.toMutableSet() ?: mutableSetOf()
                val customSet = prefs.getStringSet("CUSTOM_SUBJECTS_SET", emptySet())?.toMutableSet() ?: mutableSetOf()

                if (isCustom) {
                    customSet.remove(oldName)
                } else {
                    deletedDefaults.add(oldName)
                }

                if (StudyRepository.defaultHomeSubjects.contains(newName)) {
                    deletedDefaults.remove(newName)
                } else {
                    customSet.add(newName)
                }

                prefs.edit()
                    .remove("time_$oldName")
                    .putInt("time_$newName", oldTime)
                    .putStringSet("DELETED_DEFAULT_SUBJECTS_SET", deletedDefaults)
                    .putStringSet("CUSTOM_SUBJECTS_SET", customSet)
                    .apply()

                editDialog.dismiss()
                loadAllStudyTimesAndSubjects()
            } else if (newName == oldName) {
                editDialog.dismiss()
            }
        }
        editDialog.show()
    }

    private fun showDeleteSubjectDialog(subjectName: String, isCustom: Boolean) {
        AlertDialog.Builder(this)
            .setTitle("Hapus Mapel")
            .setMessage("Yakin ingin menghapus mapel $subjectName?")
            .setPositiveButton("Hapus") { _, _ ->
                val prefs = getSharedPreferences(StudyRepository.PREFS_NAME, Context.MODE_PRIVATE)
                val deletedDefaults = prefs.getStringSet("DELETED_DEFAULT_SUBJECTS_SET", emptySet())?.toMutableSet() ?: mutableSetOf()
                val customSet = prefs.getStringSet("CUSTOM_SUBJECTS_SET", emptySet())?.toMutableSet() ?: mutableSetOf()

                if (isCustom) {
                    customSet.remove(subjectName)
                } else {
                    deletedDefaults.add(subjectName)
                }

                prefs.edit()
                    .remove("time_$subjectName")
                    .putStringSet("DELETED_DEFAULT_SUBJECTS_SET", deletedDefaults)
                    .putStringSet("CUSTOM_SUBJECTS_SET", customSet)
                    .apply()

                loadAllStudyTimesAndSubjects()
            }
            .setNegativeButton("Batal", null)
            .show()
    }
}
