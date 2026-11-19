package com.example.purrfectstudy_anelka

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.min

class CalendarActivity : AppCompatActivity() {

    private var displayYear = 2026
    private var displayMonth = Calendar.NOVEMBER
    private var selectedYear = 2026
    private var selectedMonth = Calendar.NOVEMBER
    private var selectedDay = 19

    private val todayYear: Int
    private val todayMonth: Int
    private val todayDay: Int

    init {
        val now = Calendar.getInstance()
        todayYear = now.get(Calendar.YEAR)
        todayMonth = now.get(Calendar.MONTH)
        todayDay = now.get(Calendar.DAY_OF_MONTH)
        displayYear = todayYear
        displayMonth = todayMonth
        selectedYear = todayYear
        selectedMonth = todayMonth
        selectedDay = todayDay
    }

    private lateinit var tvCalMonthShort: TextView
    private lateinit var tvCalYear: TextView
    private lateinit var gridMonthlyCalendar: GridLayout
    private lateinit var tvSelectedCalDate: TextView
    private lateinit var tvSelectedCalTotal: TextView
    private lateinit var containerCalSessions: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)

        findViewById<NestedScrollView>(R.id.scrollCalendar).enableMouseWheelScroll()

        tvCalMonthShort = findViewById(R.id.tvCalMonthShort)
        tvCalYear = findViewById(R.id.tvCalYear)
        gridMonthlyCalendar = findViewById(R.id.gridMonthlyCalendar)
        tvSelectedCalDate = findViewById(R.id.tvSelectedCalDate)
        tvSelectedCalTotal = findViewById(R.id.tvSelectedCalTotal)
        containerCalSessions = findViewById(R.id.containerCalSessions)

        findViewById<TextView>(R.id.btnCalPrev).setOnClickListener { shiftMonth(-1) }
        findViewById<TextView>(R.id.btnCalNext).setOnClickListener { shiftMonth(1) }

        findViewById<TextView>(R.id.btnAddAgenda).setOnClickListener {
            showAddAgendaDialog()
        }

        findViewById<TextView>(R.id.btnCalMore).setOnClickListener { anchor ->
            val popup = PopupMenu(this, anchor)
            popup.menu.add(0, 1, 0, "Tambah Agenda")
            popup.menu.add(0, 2, 1, "Ke Hari Ini")
            popup.menu.add(0, 3, 2, "Buka Detailed Statistics")
            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    1 -> {
                        showAddAgendaDialog()
                        true
                    }
                    2 -> {
                        displayYear = todayYear
                        displayMonth = todayMonth
                        selectedYear = todayYear
                        selectedMonth = todayMonth
                        selectedDay = todayDay
                        renderCalendar()
                        renderSelectedDateDetails()
                        true
                    }
                    3 -> {
                        startActivity(Intent(this, StatisticsActivity::class.java))
                        @Suppress("DEPRECATION")
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
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
        findViewById<LinearLayout>(R.id.navTodo).setOnClickListener {
            val intent = Intent(this, TodoActivity::class.java)
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

        renderCalendar()
        renderSelectedDateDetails()
    }

    override fun onResume() {
        super.onResume()
        renderCalendar()
        renderSelectedDateDetails()
    }

    private fun shiftMonth(delta: Int) {
        val cal = Calendar.getInstance().apply {
            set(displayYear, displayMonth, 1)
            add(Calendar.MONTH, delta)
        }
        displayYear = cal.get(Calendar.YEAR)
        displayMonth = cal.get(Calendar.MONTH)
        val maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        selectedYear = displayYear
        selectedMonth = displayMonth
        selectedDay = min(selectedDay, maxDay)
        renderCalendar()
        renderSelectedDateDetails()
    }

    private fun renderCalendar() {
        val cal = Calendar.getInstance().apply {
            set(displayYear, displayMonth, 1)
        }
        tvCalMonthShort.text = SimpleDateFormat("MMM", Locale.ENGLISH).format(cal.time)
        tvCalYear.text = displayYear.toString()

        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        val leadingEmpty = (firstDayOfWeek - Calendar.MONDAY + 7) % 7

        gridMonthlyCalendar.removeAllViews()
        val scale = resources.displayMetrics.density

        for (i in 0 until leadingEmpty) {
            val spacer = View(this).apply {
                layoutParams = GridLayout.LayoutParams().apply {
                    width = 0
                    height = (48 * scale).toInt()
                    columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                }
            }
            gridMonthlyCalendar.addView(spacer)
        }

        for (day in 1..daysInMonth) {
            val dateKey = StudyRepository.formatDateKey(displayYear, displayMonth, day)
            val hasSessions = StudyRepository.getSessionsForDate(
                this, dateKey, todayYear, todayMonth, todayDay
            ).isNotEmpty()
            val hasAgendas = StudyRepository.getAgendasForDate(this, dateKey).isNotEmpty()
            val hasActivity = hasSessions || hasAgendas
            val isSelected = (day == selectedDay && displayMonth == selectedMonth && displayYear == selectedYear)

            val cellWrapper = LinearLayout(this).apply {
                layoutParams = GridLayout.LayoutParams().apply {
                    width = 0
                    height = (48 * scale).toInt()
                    columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                }
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                isClickable = true
                isFocusable = true
                setOnClickListener {
                    selectedYear = displayYear
                    selectedMonth = displayMonth
                    selectedDay = day
                    renderCalendar()
                    renderSelectedDateDetails()
                }
            }

            val tvNum = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams((34 * scale).toInt(), (34 * scale).toInt())
                gravity = Gravity.CENTER
                text = day.toString()
                textSize = 14f
                if (isSelected) {
                    setBackgroundResource(R.drawable.bg_calendar_day_selected)
                    setTextColor(Color.WHITE)
                    typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                } else {
                    setBackgroundColor(Color.TRANSPARENT)
                    setTextColor(Color.parseColor("#1F1F1F"))
                }
            }

            val dot = View(this).apply {
                layoutParams = LinearLayout.LayoutParams((4 * scale).toInt(), (4 * scale).toInt()).apply {
                    topMargin = (2 * scale).toInt()
                }
                if (hasActivity && !isSelected) {
                    background = GradientDrawable().apply {
                        shape = GradientDrawable.OVAL
                        setColor(Color.parseColor("#1F1F1F"))
                    }
                    visibility = View.VISIBLE
                } else {
                    visibility = View.INVISIBLE
                }
            }

            cellWrapper.addView(tvNum)
            cellWrapper.addView(dot)
            gridMonthlyCalendar.addView(cellWrapper)
        }
    }

    private fun renderSelectedDateDetails() {
        val cal = Calendar.getInstance().apply {
            set(selectedYear, selectedMonth, selectedDay)
        }
        tvSelectedCalDate.text = SimpleDateFormat("EEE, d MMM", Locale.ENGLISH).format(cal.time)

        val dateKey = StudyRepository.formatDateKey(selectedYear, selectedMonth, selectedDay)
        val agendas = StudyRepository.getAgendasForDate(this, dateKey)
        val sessions = StudyRepository.getSessionsForDate(this, dateKey, todayYear, todayMonth, todayDay)

        val totalSecs = sessions.sumOf { it.durationSeconds }
        tvSelectedCalTotal.text = "Study: ${StudyRepository.formatSecondsToTime(totalSecs)}"

        containerCalSessions.removeAllViews()
        val scale = resources.displayMetrics.density

        if (agendas.isEmpty() && sessions.isEmpty()) {
            val tvEmpty = TextView(this).apply {
                text = "No study sessions recorded"
                textSize = 14f
                setTextColor(Color.parseColor("#757575"))
                setPadding(0, (12 * scale).toInt(), 0, (12 * scale).toInt())
            }
            containerCalSessions.addView(tvEmpty)
            return
        }

        // Bagian Agenda / Jadwal Kegiatan
        if (agendas.isNotEmpty()) {
            val tvSecAgenda = TextView(this).apply {
                text = "Agenda & Schedule"
                textSize = 12.5f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                setTextColor(Color.parseColor("#757575"))
                setPadding(0, (4 * scale).toInt(), 0, (8 * scale).toInt())
            }
            containerCalSessions.addView(tvSecAgenda)

            val checkboxTint = ColorStateList(
                arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf()),
                intArrayOf(Color.parseColor("#1F1F1F"), Color.parseColor("#BDBDBD"))
            )

            for (agenda in agendas) {
                val card = LinearLayout(this).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply { bottomMargin = (8 * scale).toInt() }
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER_VERTICAL
                    background = GradientDrawable().apply {
                        shape = GradientDrawable.RECTANGLE
                        cornerRadius = 10f * scale
                        setColor(Color.parseColor("#F7F7F8"))
                        setStroke((1 * scale).toInt(), Color.parseColor("#EAEAEA"))
                    }
                    setPadding((8 * scale).toInt(), (10 * scale).toInt(), (14 * scale).toInt(), (10 * scale).toInt())
                    isClickable = true
                    isFocusable = true
                    setOnLongClickListener {
                        showDeleteAgendaDialog(dateKey, agenda)
                        true
                    }
                }

                val cb = CheckBox(this).apply {
                    isChecked = agenda.isDone
                    buttonTintList = checkboxTint
                }

                val infoCol = LinearLayout(this).apply {
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                        marginStart = (4 * scale).toInt()
                    }
                    orientation = LinearLayout.VERTICAL
                }

                val tvTitle = TextView(this).apply {
                    text = agenda.title
                    textSize = 14.5f
                    typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                    setTextColor(Color.parseColor(if (agenda.isDone) "#8E8E93" else "#1F1F1F"))
                    paintFlags = if (agenda.isDone) paintFlags or Paint.STRIKE_THRU_TEXT_FLAG else paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                }

                val tvTime = TextView(this).apply {
                    text = agenda.timeRange
                    textSize = 12f
                    setTextColor(Color.parseColor("#757575"))
                }

                infoCol.addView(tvTitle)
                if (agenda.timeRange.isNotEmpty()) infoCol.addView(tvTime)
                if (agenda.note.isNotEmpty()) {
                    val tvNote = TextView(this).apply {
                        text = agenda.note
                        textSize = 12f
                        setTextColor(Color.parseColor("#757575"))
                        setPadding(0, (2 * scale).toInt(), 0, 0)
                    }
                    infoCol.addView(tvNote)
                }

                cb.setOnCheckedChangeListener { _, isChecked ->
                    StudyRepository.toggleAgendaDone(this, dateKey, agenda.id, isChecked)
                    tvTitle.setTextColor(Color.parseColor(if (isChecked) "#8E8E93" else "#1F1F1F"))
                    tvTitle.paintFlags = if (isChecked) {
                        tvTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                    } else {
                        tvTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                    }
                }

                card.addView(cb)
                card.addView(infoCol)
                containerCalSessions.addView(card)
            }
        }

        // Bagian Riwayat Study Sessions
        if (sessions.isNotEmpty()) {
            val tvSecSessions = TextView(this).apply {
                text = "Study Sessions"
                textSize = 12.5f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                setTextColor(Color.parseColor("#757575"))
                setPadding(0, (if (agendas.isNotEmpty()) 8 * scale else 4 * scale).toInt(), 0, (8 * scale).toInt())
            }
            containerCalSessions.addView(tvSecSessions)

            for (session in sessions) {
                val card = LinearLayout(this).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply { bottomMargin = (8 * scale).toInt() }
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER_VERTICAL
                    background = GradientDrawable().apply {
                        shape = GradientDrawable.RECTANGLE
                        cornerRadius = 10f * scale
                        setColor(Color.parseColor("#F9F9F9"))
                    }
                    setPadding((14 * scale).toInt(), (12 * scale).toInt(), (14 * scale).toInt(), (12 * scale).toInt())
                }

                val bar = View(this).apply {
                    layoutParams = LinearLayout.LayoutParams((4 * scale).toInt(), (32 * scale).toInt()).apply {
                        rightMargin = (10 * scale).toInt()
                    }
                    setBackgroundColor(Color.parseColor("#1F1F1F"))
                }

                val infoCol = LinearLayout(this).apply {
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                    orientation = LinearLayout.VERTICAL
                }

                val tvSub = TextView(this).apply {
                    text = session.subject
                    textSize = 14.5f
                    typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                    setTextColor(Color.parseColor("#1F1F1F"))
                }

                val tvRange = TextView(this).apply {
                    text = "${StudyRepository.formatMillisAmPm(session.startTimeMillis)} ~ ${StudyRepository.formatMillisAmPm(session.endTimeMillis)}"
                    textSize = 12f
                    setTextColor(Color.parseColor("#757575"))
                }

                infoCol.addView(tvSub)
                infoCol.addView(tvRange)

                val tvDur = TextView(this).apply {
                    text = StudyRepository.formatSecondsToTime(session.durationSeconds)
                    textSize = 13.5f
                    typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                    fontFeatureSettings = "tnum"
                    setTextColor(Color.parseColor("#1F1F1F"))
                }

                card.addView(bar)
                card.addView(infoCol)
                card.addView(tvDur)
                containerCalSessions.addView(card)
            }
        }
    }

    private fun showAddAgendaDialog() {
        val dateKey = StudyRepository.formatDateKey(selectedYear, selectedMonth, selectedDay)
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 28, 48, 12)
        }

        val etTitle = EditText(this).apply {
            hint = "Judul Kegiatan (cth: Tryout UTBK)"
        }
        val etTime = EditText(this).apply {
            hint = "Jam (cth: 15:00 - 17:00)"
        }
        val etNote = EditText(this).apply {
            hint = "Catatan singkat (opsional)"
        }

        container.addView(etTitle)
        container.addView(etTime)
        container.addView(etNote)

        AlertDialog.Builder(this)
            .setTitle("Tambah Agenda ($dateKey)")
            .setView(container)
            .setPositiveButton("Simpan") { _, _ ->
                val title = etTitle.text.toString().trim()
                if (title.isNotEmpty()) {
                    val agenda = StudyRepository.CalendarAgenda(
                        id = "ag_${System.currentTimeMillis()}",
                        title = title,
                        timeRange = etTime.text.toString().trim().ifEmpty { "Sepanjang hari" },
                        note = etNote.text.toString().trim(),
                        isDone = false
                    )
                    StudyRepository.addAgendaForDate(this, dateKey, agenda)
                    renderCalendar()
                    renderSelectedDateDetails()
                    Toast.makeText(this, "Agenda disimpan", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun showDeleteAgendaDialog(dateKey: String, agenda: StudyRepository.CalendarAgenda) {
        AlertDialog.Builder(this)
            .setTitle("Hapus Agenda")
            .setMessage("Hapus agenda '${agenda.title}'?")
            .setPositiveButton("Hapus") { _, _ ->
                StudyRepository.deleteAgendaForDate(this, dateKey, agenda.id)
                renderCalendar()
                renderSelectedDateDetails()
                Toast.makeText(this, "Agenda dihapus", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Batal", null)
            .show()
    }
}
