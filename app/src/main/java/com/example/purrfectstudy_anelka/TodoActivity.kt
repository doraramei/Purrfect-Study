package com.example.purrfectstudy_anelka

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView

class TodoActivity : AppCompatActivity() {

    private data class WeekDayItem(
        val dayName: String,
        val dayNum: Int,
        val fullDateTitle: String
    )

    private data class SubTaskItem(
        val id: String,
        val parentCategory: String,
        val title: String
    )

    private val weekDays = listOf(
        WeekDayItem("Mon", 9, "Monday, 9 November"),
        WeekDayItem("Tue", 10, "Tuesday, 10 November"),
        WeekDayItem("Wed", 11, "Wednesday, 11 November"),
        WeekDayItem("Thu", 12, "Thursday, 12 November"),
        WeekDayItem("Fri", 13, "Friday, 13 November"),
        WeekDayItem("Sat", 14, "Saturday, 14 November"),
        WeekDayItem("Sun", 15, "Sunday, 15 November")
    )

    private var selectedDayIndex = 3 // Default aktif: Thu 12

    private lateinit var layoutWeekDays: LinearLayout
    private lateinit var tvPlannerFullDate: TextView
    private lateinit var tvPlannerProgress: TextView
    private lateinit var containerTasks: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_todo)

        layoutWeekDays = findViewById(R.id.layoutWeekDays)
        tvPlannerFullDate = findViewById(R.id.tvPlannerFullDate)
        tvPlannerProgress = findViewById(R.id.tvPlannerProgress)
        containerTasks = findViewById(R.id.containerTasks)
        findViewById<NestedScrollView>(R.id.scrollTodo).enableMouseWheelScroll()

        findViewById<TextView>(R.id.btnPlannerMore).setOnClickListener { anchor ->
            val popup = PopupMenu(this, anchor)
            popup.menu.add(0, 1, 0, "Tambah Kategori Kustom")
            popup.menu.add(0, 2, 1, "Centang Semua Sub-Tugas")
            popup.menu.add(0, 3, 2, "Reset Centang Hari Ini")
            popup.setOnMenuItemClickListener { item ->
                val dayNum = weekDays[selectedDayIndex].dayNum
                val prefs = getSharedPreferences(StudyRepository.PREFS_NAME, Context.MODE_PRIVATE)
                when (item.itemId) {
                    1 -> {
                        showAddCustomCategoryDialog()
                        true
                    }
                    2 -> {
                        val allIds = loadSubTasksForDay(dayNum).map { it.id }.toSet()
                        prefs.edit().putStringSet("TODO_CHECKED_V2_$dayNum", allIds).apply()
                        renderTasks()
                        true
                    }
                    3 -> {
                        prefs.edit().remove("TODO_CHECKED_V2_$dayNum").apply()
                        renderTasks()
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }

        // Bottom Navigation dengan transisi halus
        findViewById<LinearLayout>(R.id.navHome).setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(intent)
            @Suppress("DEPRECATION")
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
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

        renderWeekBar()
        renderTasks()
    }

    override fun onResume() {
        super.onResume()
        renderTasks()
    }

    private fun renderWeekBar() {
        layoutWeekDays.removeAllViews()
        val scale = resources.displayMetrics.density

        weekDays.forEachIndexed { idx, item ->
            val isSelected = idx == selectedDayIndex

            val slotContainer = FrameLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f)
                isClickable = true
                isFocusable = true
                setOnClickListener {
                    selectedDayIndex = idx
                    renderWeekBar()
                    renderTasks()
                }
            }

            val dayCol = LinearLayout(this).apply {
                layoutParams = FrameLayout.LayoutParams(
                    (46 * scale).toInt(),
                    (54 * scale).toInt(),
                    Gravity.CENTER
                )
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                if (isSelected) {
                    setBackgroundResource(R.drawable.bg_planner_day_selected)
                } else {
                    setBackgroundColor(Color.TRANSPARENT)
                }
            }

            val tvDayName = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                gravity = Gravity.CENTER
                textAlignment = View.TEXT_ALIGNMENT_CENTER
                text = item.dayName
                textSize = 11.5f
                setTextColor(Color.parseColor(if (isSelected) "#D4D4D4" else "#757575"))
            }

            val tvDayNum = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                gravity = Gravity.CENTER
                textAlignment = View.TEXT_ALIGNMENT_CENTER
                text = item.dayNum.toString()
                textSize = 15f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                setTextColor(Color.parseColor(if (isSelected) "#FFFFFF" else "#1F1F1F"))
            }

            dayCol.addView(tvDayName)
            dayCol.addView(tvDayNum)
            slotContainer.addView(dayCol)
            layoutWeekDays.addView(slotContainer)
        }
    }

    private fun getParentCategories(): List<String> {
        val homeSubjects = StudyRepository.getActiveHomeSubjects(this)
        val prefs = getSharedPreferences(StudyRepository.PREFS_NAME, Context.MODE_PRIVATE)
        val customCats = prefs.getStringSet("TODO_CUSTOM_CATEGORIES_SET", emptySet()) ?: emptySet()
        return (homeSubjects + customCats.sorted()).distinct()
    }

    private fun loadSubTasksForDay(dayNum: Int): List<SubTaskItem> {
        val prefs = getSharedPreferences(StudyRepository.PREFS_NAME, Context.MODE_PRIVATE)
        val raw = prefs.getString("TODO_SUBTASKS_V2_$dayNum", "") ?: ""
        if (raw.isEmpty()) return emptyList()
        return raw.split("||").mapNotNull { entry ->
            val parts = entry.split("::")
            if (parts.size == 3) {
                SubTaskItem(parts[0], parts[1], parts[2])
            } else {
                null
            }
        }
    }

    private fun writeSubTasksForDay(dayNum: Int, items: List<SubTaskItem>) {
        val prefs = getSharedPreferences(StudyRepository.PREFS_NAME, Context.MODE_PRIVATE)
        val encoded = items.joinToString("||") { "${it.id}::${it.parentCategory}::${it.title}" }
        prefs.edit().putString("TODO_SUBTASKS_V2_$dayNum", encoded).apply()
    }

    private fun renderTasks() {
        val currentDay = weekDays[selectedDayIndex]
        tvPlannerFullDate.text = currentDay.fullDateTitle

        val dayNum = currentDay.dayNum
        val prefs = getSharedPreferences(StudyRepository.PREFS_NAME, Context.MODE_PRIVATE)
        val checkedSet = prefs.getStringSet("TODO_CHECKED_V2_$dayNum", emptySet())?.toMutableSet() ?: mutableSetOf()

        val parentCategories = getParentCategories()
        val allSubTasks = loadSubTasksForDay(dayNum)
        val subByParent = allSubTasks.groupBy { it.parentCategory }

        containerTasks.removeAllViews()
        val scale = resources.displayMetrics.density

        val checkboxTint = ColorStateList(
            arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf()),
            intArrayOf(Color.parseColor("#1F1F1F"), Color.parseColor("#BDBDBD"))
        )

        for (category in parentCategories) {
            val wrapper = LinearLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                orientation = LinearLayout.VERTICAL
                setPadding((20 * scale).toInt(), (4 * scale).toInt(), (16 * scale).toInt(), 0)
            }

            // Header Mapel (Induk dari Home / Kategori Kustom)
            val parentRow = LinearLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    (48 * scale).toInt()
                )
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }

            val dot = View(this).apply {
                layoutParams = LinearLayout.LayoutParams((8 * scale).toInt(), (8 * scale).toInt()).apply {
                    rightMargin = (10 * scale).toInt()
                }
                background = android.graphics.drawable.GradientDrawable().apply {
                    shape = android.graphics.drawable.GradientDrawable.OVAL
                    setColor(Color.parseColor("#1F1F1F"))
                }
            }

            val tvParentName = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                text = category
                textSize = 15f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                setTextColor(Color.parseColor("#1F1F1F"))
            }

            val btnAddSub = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams((36 * scale).toInt(), (36 * scale).toInt())
                text = "+"
                textSize = 20f
                gravity = Gravity.CENTER
                setTextColor(Color.parseColor("#1F1F1F"))
                isClickable = true
                isFocusable = true
                setOnClickListener {
                    showAddSubTaskDialog(dayNum, category)
                }
            }

            parentRow.addView(dot)
            parentRow.addView(tvParentName)
            parentRow.addView(btnAddSub)
            wrapper.addView(parentRow)

            // Daftar Sub-Tugas (Anak) Menjorok ke Dalam (marginStart="28dp")
            val children = subByParent[category] ?: emptyList()
            for (child in children) {
                val childRow = LinearLayout(this).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        marginStart = (28 * scale).toInt()
                    }
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER_VERTICAL
                    setPadding(0, (4 * scale).toInt(), 0, (4 * scale).toInt())
                    isClickable = true
                    isFocusable = true
                    setOnLongClickListener {
                        showDeleteSubTaskDialog(dayNum, child)
                        true
                    }
                }

                val isChecked = checkedSet.contains(child.id)
                val cb = CheckBox(this).apply {
                    this.isChecked = isChecked
                    buttonTintList = checkboxTint
                }

                val tvChildLabel = TextView(this).apply {
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                        marginStart = (4 * scale).toInt()
                    }
                    text = child.title
                    textSize = 14f
                    setTextColor(Color.parseColor(if (isChecked) "#8E8E93" else "#1F1F1F"))
                    paintFlags = if (isChecked) paintFlags or Paint.STRIKE_THRU_TEXT_FLAG else paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                }

                cb.setOnCheckedChangeListener { _, checked ->
                    val latest = prefs.getStringSet("TODO_CHECKED_V2_$dayNum", emptySet())?.toMutableSet() ?: mutableSetOf()
                    if (checked) latest.add(child.id) else latest.remove(child.id)
                    prefs.edit().putStringSet("TODO_CHECKED_V2_$dayNum", latest).apply()

                    tvChildLabel.setTextColor(Color.parseColor(if (checked) "#8E8E93" else "#1F1F1F"))
                    tvChildLabel.paintFlags = if (checked) {
                        tvChildLabel.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                    } else {
                        tvChildLabel.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                    }
                    updateProgressText(allSubTasks.size, latest.count { id -> allSubTasks.any { it.id == id } })
                }

                childRow.addView(cb)
                childRow.addView(tvChildLabel)
                wrapper.addView(childRow)
            }

            val divider = View(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    (1 * scale).toInt()
                ).apply {
                    topMargin = (4 * scale).toInt()
                }
                setBackgroundColor(Color.parseColor("#EFEFEF"))
            }
            wrapper.addView(divider)
            containerTasks.addView(wrapper)
        }

        val doneCount = allSubTasks.count { checkedSet.contains(it.id) }
        updateProgressText(allSubTasks.size, doneCount)
    }

    private fun updateProgressText(totalCount: Int, checkedCount: Int) {
        tvPlannerProgress.text = "$checkedCount / $totalCount"
    }

    private fun showAddSubTaskDialog(dayNum: Int, parentCategory: String) {
        val etSub = EditText(this).apply {
            hint = "Contoh: Latihan 20 soal $parentCategory"
            setPadding(48, 32, 48, 32)
        }

        AlertDialog.Builder(this)
            .setTitle("Tambah Sub-Tugas ($parentCategory)")
            .setView(etSub)
            .setPositiveButton("Tambah") { _, _ ->
                val title = etSub.text.toString().replace("::", " ").replace("||", " ").trim()
                if (title.isNotEmpty()) {
                    val existing = loadSubTasksForDay(dayNum).toMutableList()
                    existing.add(SubTaskItem("sub_${System.currentTimeMillis()}", parentCategory, title))
                    writeSubTasksForDay(dayNum, existing)
                    renderTasks()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun showDeleteSubTaskDialog(dayNum: Int, item: SubTaskItem) {
        AlertDialog.Builder(this)
            .setTitle("Hapus Sub-Tugas")
            .setMessage("Hapus '${item.title}' dari ${item.parentCategory}?")
            .setPositiveButton("Hapus") { _, _ ->
                val updated = loadSubTasksForDay(dayNum).filter { it.id != item.id }
                writeSubTasksForDay(dayNum, updated)
                val prefs = getSharedPreferences(StudyRepository.PREFS_NAME, Context.MODE_PRIVATE)
                val checked = prefs.getStringSet("TODO_CHECKED_V2_$dayNum", emptySet())?.toMutableSet() ?: mutableSetOf()
                checked.remove(item.id)
                prefs.edit().putStringSet("TODO_CHECKED_V2_$dayNum", checked).apply()
                renderTasks()
                Toast.makeText(this, "Sub-tugas dihapus", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun showAddCustomCategoryDialog() {
        val etCat = EditText(this).apply {
            hint = "Contoh: Target Tryout / Review Mingguan"
            setPadding(48, 32, 48, 32)
        }

        AlertDialog.Builder(this)
            .setTitle("Tambah Kategori Kustom")
            .setView(etCat)
            .setPositiveButton("Tambah") { _, _ ->
                val catName = etCat.text.toString().trim()
                if (catName.isNotEmpty()) {
                    val prefs = getSharedPreferences(StudyRepository.PREFS_NAME, Context.MODE_PRIVATE)
                    val cats = prefs.getStringSet("TODO_CUSTOM_CATEGORIES_SET", emptySet())?.toMutableSet() ?: mutableSetOf()
                    cats.add(catName)
                    prefs.edit().putStringSet("TODO_CUSTOM_CATEGORIES_SET", cats).apply()
                    renderTasks()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }
}
