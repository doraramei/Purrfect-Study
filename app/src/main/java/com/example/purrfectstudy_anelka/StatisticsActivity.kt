package com.example.purrfectstudy_anelka

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.GridLayout
import android.widget.ImageView
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
import kotlin.math.max
import kotlin.math.min

class StatisticsActivity : AppCompatActivity() {

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

    // Palet monokrom (hitam ke abu-abu, tanpa pink/terracotta)
    private val chartPalette = listOf(
        "#111111",
        "#4A4A4A",
        "#8E8E93",
        "#C7C7CC",
        "#636366"
    )

    private lateinit var tvCalendarMonthTitle: TextView
    private lateinit var gridCalendar: GridLayout
    private lateinit var tvMonthlyTotal: TextView

    private lateinit var tvSummaryDateTitle: TextView
    private lateinit var tvStatTotalStudy: TextView
    private lateinit var tvStatMaxFocus: TextView
    private lateinit var tvStatStartTime: TextView
    private lateinit var tvStatEndTime: TextView
    private lateinit var tvUpToLabel: TextView
    private lateinit var tvMorningStudyTime: TextView
    private lateinit var tvPeriodBadgeText: TextView
    private lateinit var chartCumulativeStep: CumulativeStepChartView

    private lateinit var donutBreakdown: DonutChartView
    private lateinit var containerBreakdown: LinearLayout
    private lateinit var donutRatio: DonutChartView
    private lateinit var tvRatioStudy: TextView
    private lateinit var tvRatioOther: TextView

    private lateinit var tvDailyDateHeader: TextView
    private lateinit var ivStatsMascot: ImageView
    private lateinit var tvMascotTimer: TextView
    private lateinit var hourlyActivityGrid: HourlyActivityGridView

    private lateinit var containerTimeline: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistics)

        findViewById<TextView>(R.id.btnBackStats).setOnClickListener {
            finish()
        }

        findViewById<NestedScrollView>(R.id.nestedScrollStatistics).enableMouseWheelScroll()

        tvCalendarMonthTitle = findViewById(R.id.tvCalendarMonthTitle)
        gridCalendar = findViewById(R.id.gridCalendar)
        tvMonthlyTotal = findViewById(R.id.tvMonthlyTotal)

        tvSummaryDateTitle = findViewById(R.id.tvSummaryDateTitle)
        tvStatTotalStudy = findViewById(R.id.tvStatTotalStudy)
        tvStatMaxFocus = findViewById(R.id.tvStatMaxFocus)
        tvStatStartTime = findViewById(R.id.tvStatStartTime)
        tvStatEndTime = findViewById(R.id.tvStatEndTime)
        tvUpToLabel = findViewById(R.id.tvUpToLabel)
        tvMorningStudyTime = findViewById(R.id.tvMorningStudyTime)
        tvPeriodBadgeText = findViewById(R.id.tvPeriodBadgeText)
        chartCumulativeStep = findViewById(R.id.chartCumulativeStep)

        donutBreakdown = findViewById(R.id.donutBreakdown)
        containerBreakdown = findViewById(R.id.containerBreakdown)
        donutRatio = findViewById(R.id.donutRatio)
        tvRatioStudy = findViewById(R.id.tvRatioStudy)
        tvRatioOther = findViewById(R.id.tvRatioOther)

        tvDailyDateHeader = findViewById(R.id.tvDailyDateHeader)
        ivStatsMascot = findViewById(R.id.ivStatsMascot)
        tvMascotTimer = findViewById(R.id.tvMascotTimer)
        hourlyActivityGrid = findViewById(R.id.hourlyActivityGrid)

        containerTimeline = findViewById(R.id.containerTimeline)

        findViewById<TextView>(R.id.btnPrevMonth).setOnClickListener {
            shiftMonth(-1)
        }
        findViewById<TextView>(R.id.btnNextMonth).setOnClickListener {
            shiftMonth(1)
        }

        findViewById<TextView>(R.id.btnAddTimelineSession).setOnClickListener {
            val defaultStartMin = 14 * 60 - 5 * 60
            showAddSessionDialog(selectedDateKey(), defaultStartMin)
        }

        refreshAll()
    }

    override fun onResume() {
        super.onResume()
        refreshAll()
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
        refreshAll()
    }

    private fun refreshAll() {
        ivStatsMascot.setImageResource(StudyRepository.getSelectedMascotDrawable(this))
        renderMonthlyCalendar()
        renderSelectedDayStatistics()
    }

    private fun renderMonthlyCalendar() {
        val cal = Calendar.getInstance().apply {
            set(displayYear, displayMonth, 1)
        }
        val monthShort = SimpleDateFormat("MMM", Locale.ENGLISH).format(cal.time)
        tvCalendarMonthTitle.text = SimpleDateFormat("MMM yyyy", Locale.ENGLISH).format(cal.time)

        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        val leadingEmpty = (firstDayOfWeek - Calendar.MONDAY + 7) % 7

        gridCalendar.removeAllViews()
        val scale = resources.displayMetrics.density
        val cellMargin = (2 * scale).toInt()

        for (i in 0 until leadingEmpty) {
            val spacer = View(this).apply {
                layoutParams = GridLayout.LayoutParams().apply {
                    width = 0
                    height = (46 * scale).toInt()
                    columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                    setMargins(cellMargin, cellMargin, cellMargin, cellMargin)
                }
            }
            gridCalendar.addView(spacer)
        }

        var monthlyTotalSeconds = 0

        for (day in 1..daysInMonth) {
            val key = StudyRepository.formatDateKey(displayYear, displayMonth, day)
            val sessions = StudyRepository.getSessionsForDate(this, key, todayYear, todayMonth, todayDay)
            val daySeconds = sessions.sumOf { it.durationSeconds }
            monthlyTotalSeconds += daySeconds

            val isSelected = (day == selectedDay && displayMonth == selectedMonth && displayYear == selectedYear)
            val heatColors = resolveHeatmapColors(daySeconds)

            val cell = TextView(this).apply {
                layoutParams = GridLayout.LayoutParams().apply {
                    width = 0
                    height = GridLayout.LayoutParams.WRAP_CONTENT
                    columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                    setMargins(cellMargin, cellMargin, cellMargin, cellMargin)
                }
                minHeight = (46 * scale).toInt()
                gravity = Gravity.CENTER
                setPadding(0, (6 * scale).toInt(), 0, (6 * scale).toInt())
                textSize = 12f
                typeface = if (isSelected) {
                    Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                } else {
                    Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
                }
                setTextColor(heatColors.second)
                val durLabel = if (daySeconds > 0) StudyRepository.formatHourMinuteShort(daySeconds) else "-"
                text = "$day\n$durLabel"

                background = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadius = 8f * scale
                    setColor(heatColors.first)
                    if (isSelected) {
                        setStroke((2 * scale).toInt(), Color.parseColor("#111111"))
                    }
                }

                isClickable = true
                isFocusable = true
                setOnClickListener {
                    selectedYear = displayYear
                    selectedMonth = displayMonth
                    selectedDay = day
                    refreshAll()
                }
            }
            gridCalendar.addView(cell)
        }

        val totalHours = monthlyTotalSeconds / 3600
        val totalMins = (monthlyTotalSeconds % 3600) / 60
        tvMonthlyTotal.text = String.format(Locale.ENGLISH, "%s: %dH %02dM", monthShort, totalHours, totalMins)
    }

    private fun resolveHeatmapColors(daySeconds: Int): Pair<Int, Int> {
        val hours = daySeconds / 3600f
        return when {
            daySeconds <= 0 -> Color.parseColor("#F5F5F5") to Color.parseColor("#757575")
            hours < 4f -> Color.parseColor("#E0E0E0") to Color.parseColor("#1F1F1F")
            hours < 7f -> Color.parseColor("#BDBDBD") to Color.parseColor("#1F1F1F")
            hours < 10f -> Color.parseColor("#757575") to Color.WHITE
            hours < 12f -> Color.parseColor("#424242") to Color.WHITE
            else -> Color.parseColor("#111111") to Color.WHITE
        }
    }

    private fun renderSelectedDayStatistics() {
        val selectedCal = Calendar.getInstance().apply {
            set(selectedYear, selectedMonth, selectedDay)
        }
        val selectedKey = selectedDateKey()
        val sessions = StudyRepository.getSessionsForDate(this, selectedKey, todayYear, todayMonth, todayDay)

        tvSummaryDateTitle.text = SimpleDateFormat("EEE, MMM d", Locale.ENGLISH).format(selectedCal.time)
        tvDailyDateHeader.text = SimpleDateFormat("EEE, MMM d, yyyy", Locale.ENGLISH).format(selectedCal.time)
        tvPeriodBadgeText.text = if (selectedYear == todayYear && selectedMonth == todayMonth && selectedDay == todayDay) {
            "Today"
        } else {
            SimpleDateFormat("MMM d", Locale.ENGLISH).format(selectedCal.time)
        }

        val totalSeconds = sessions.sumOf { it.durationSeconds }
        val maxFocusSeconds = sessions.maxOfOrNull { it.durationSeconds } ?: 0
        val sortedByTime = sessions.sortedBy { it.startTimeMillis }

        tvStatTotalStudy.text = StudyRepository.formatSecondsToTime(totalSeconds)
        tvStatMaxFocus.text = StudyRepository.formatSecondsToTime(maxFocusSeconds)
        tvMascotTimer.text = StudyRepository.formatSecondsToTime(totalSeconds)

        val isTodaySelected = (selectedYear == todayYear && selectedMonth == todayMonth && selectedDay == todayDay)

        if (totalSeconds > 0 && sortedByTime.isNotEmpty()) {
            val firstSession = sortedByTime.first()
            val lastSession = sortedByTime.maxByOrNull { it.endTimeMillis }!!
            tvStatStartTime.text = StudyRepository.formatMillisAmPm(firstSession.startTimeMillis)
            tvStatEndTime.text = StudyRepository.formatMillisAmPm(lastSession.endTimeMillis)

            val cutoffMillis = if (isTodaySelected) {
                max(System.currentTimeMillis(), lastSession.endTimeMillis)
            } else {
                lastSession.endTimeMillis
            }
            tvUpToLabel.text = "Up to ${StudyRepository.formatHourCutoffLabel(cutoffMillis)}"
            val upToSeconds = sortedByTime.filter { it.startTimeMillis <= cutoffMillis }.sumOf { it.durationSeconds }
            tvMorningStudyTime.text = StudyRepository.formatSecondsToTime(upToSeconds)
        } else {
            tvStatStartTime.text = "-"
            tvStatEndTime.text = "-"
            val nowMillis = System.currentTimeMillis()
            tvUpToLabel.text = "Up to ${StudyRepository.formatHourCutoffLabel(nowMillis)}"
            tvMorningStudyTime.text = "0:00:00"
        }
        chartCumulativeStep.setData(computeHourlyCumulativeSeconds(sortedByTime))

        val subjectMap = linkedMapOf<String, Int>()
        for (s in sortedByTime) {
            subjectMap[s.subject] = (subjectMap[s.subject] ?: 0) + s.durationSeconds
        }
        val sortedSubjects = subjectMap.entries.sortedByDescending { it.value }
        val subjectColorMap = mutableMapOf<String, Int>()
        val donutSlices = mutableListOf<DonutChartView.Slice>()

        sortedSubjects.forEachIndexed { idx, entry ->
            val colorInt = Color.parseColor(chartPalette[idx % chartPalette.size])
            subjectColorMap[entry.key] = colorInt
            donutSlices.add(DonutChartView.Slice(entry.key, entry.value.toFloat(), colorInt))
        }

        val centerSub = if (sortedSubjects.size == 1) sortedSubjects.first().key else "Study Total"
        donutBreakdown.setData(donutSlices, StudyRepository.formatSecondsToTime(totalSeconds), centerSub)
        renderBreakdownLegend(sortedSubjects, totalSeconds, subjectColorMap)

        val cycleTotalSeconds = 24 * 3600
        val clampedStudySeconds = min(totalSeconds, cycleTotalSeconds)
        val otherSeconds = max(0, cycleTotalSeconds - clampedStudySeconds)
        val studyPercent = if (clampedStudySeconds > 0) {
            ((clampedStudySeconds * 100.0) / cycleTotalSeconds).toInt().coerceIn(1, 100)
        } else {
            0
        }
        val otherPercent = 100 - studyPercent

        val ratioSlices = listOf(
            DonutChartView.Slice("Study", clampedStudySeconds.toFloat(), Color.parseColor("#111111")),
            DonutChartView.Slice("Other", otherSeconds.toFloat(), Color.parseColor("#E5E5EA"))
        )
        donutRatio.setData(ratioSlices, "$studyPercent%", "Study")
        tvRatioStudy.text = "${StudyRepository.formatHourMinuteShort(clampedStudySeconds)} - $studyPercent%"
        tvRatioOther.text = "${StudyRepository.formatHourMinuteShort(otherSeconds)} - $otherPercent%"

        val gridBlocks = if (totalSeconds > 0) {
            sortedByTime.map { s ->
                val colorInt = subjectColorMap[s.subject] ?: colorForSubjectName(s.subject)
                val startMin = StudyRepository.millisToMinutesFrom5Am(s.startTimeMillis)
                val durMins = max(1, (s.durationSeconds + 59) / 60)
                HourlyActivityGridView.SessionBlock(
                    startMinuteFrom5Am = startMin,
                    endMinuteFrom5Am = startMin + durMins,
                    color = colorInt
                )
            }
        } else {
            emptyList()
        }
        hourlyActivityGrid.setSessions(gridBlocks)

        renderTimeline(selectedKey, sortedByTime, subjectColorMap)
    }

    private fun renderBreakdownLegend(
        sortedSubjects: List<Map.Entry<String, Int>>,
        totalSeconds: Int,
        colorMap: Map<String, Int>
    ) {
        containerBreakdown.removeAllViews()
        val scale = resources.displayMetrics.density

        if (sortedSubjects.isEmpty()) {
            val tvEmpty = TextView(this).apply {
                text = "Belum ada catatan belajar pada tanggal ini."
                textSize = 12.5f
                setTextColor(Color.parseColor("#757575"))
            }
            containerBreakdown.addView(tvEmpty)
            return
        }

        for ((name, secs) in sortedSubjects) {
            val pct = if (totalSeconds > 0) (secs * 100) / totalSeconds else 0
            val colorInt = colorMap[name] ?: Color.parseColor("#111111")

            val row = LinearLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { setMargins(0, (3 * scale).toInt(), 0, (3 * scale).toInt()) }
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }

            val dot = View(this).apply {
                layoutParams = LinearLayout.LayoutParams((10 * scale).toInt(), (10 * scale).toInt()).apply {
                    rightMargin = (8 * scale).toInt()
                }
                background = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(colorInt)
                }
            }

            val tvName = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                text = name
                textSize = 13f
                maxLines = 1
                setTextColor(Color.parseColor("#1F1F1F"))
            }

            val tvValue = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                text = "${StudyRepository.formatSecondsToTime(secs)} • $pct%"
                textSize = 12f
                fontFeatureSettings = "tnum"
                setTextColor(Color.parseColor("#757575"))
            }

            row.addView(dot)
            row.addView(tvName)
            row.addView(tvValue)
            containerBreakdown.addView(row)
        }
    }

    private fun renderTimeline(
        dateKey: String,
        sessions: List<StudyRepository.StudySession>,
        colorMap: Map<String, Int>
    ) {
        containerTimeline.removeAllViews()
        val scale = resources.displayMetrics.density
        val sorted = sessions.sortedBy { it.startTimeMillis }

        if (sorted.isEmpty()) {
            val tvEmpty = TextView(this).apply {
                text = "No study sessions recorded"
                textSize = 13.5f
                setTextColor(Color.parseColor("#757575"))
                setPadding((4 * scale).toInt(), (10 * scale).toInt(), (4 * scale).toInt(), (10 * scale).toInt())
            }
            containerTimeline.addView(tvEmpty)
            return
        }

        for (session in sorted) {
            containerTimeline.addView(
                createTimelineSessionCard(
                    dateKey,
                    session,
                    colorMap[session.subject] ?: colorForSubjectName(session.subject),
                    scale
                )
            )
        }
    }

    private fun createTimelineSessionCard(
        dateKey: String,
        session: StudyRepository.StudySession,
        accentColor: Int,
        scale: Float
    ): View {
        val card = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, (2 * scale).toInt(), 0, (6 * scale).toInt()) }
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 10f * scale
                setColor(Color.parseColor("#F9F9F9"))
            }
            setPadding((12 * scale).toInt(), (10 * scale).toInt(), (6 * scale).toInt(), (10 * scale).toInt())
        }

        val colorBar = View(this).apply {
            layoutParams = LinearLayout.LayoutParams((4 * scale).toInt(), (36 * scale).toInt()).apply {
                rightMargin = (10 * scale).toInt()
            }
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 2f * scale
                setColor(accentColor)
            }
        }

        val infoCol = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            orientation = LinearLayout.VERTICAL
        }

        val tvSubject = TextView(this).apply {
            text = session.subject
            textSize = 14f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            setTextColor(Color.parseColor("#1F1F1F"))
        }

        val tvRange = TextView(this).apply {
            text = "${StudyRepository.formatMillisAmPm(session.startTimeMillis)} ~ ${StudyRepository.formatMillisAmPm(session.endTimeMillis)}"
            textSize = 12f
            setTextColor(Color.parseColor("#757575"))
        }

        infoCol.addView(tvSubject)
        infoCol.addView(tvRange)

        val tvDuration = TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { rightMargin = (4 * scale).toInt() }
            text = StudyRepository.formatSecondsToTime(session.durationSeconds)
            textSize = 13f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            fontFeatureSettings = "tnum"
            setTextColor(Color.parseColor("#1F1F1F"))
        }

        val btnMore = TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams((32 * scale).toInt(), (32 * scale).toInt())
            text = "⋮"
            textSize = 17f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setTextColor(Color.parseColor("#757575"))
            isClickable = true
            isFocusable = true
            setOnClickListener { anchor ->
                val popup = PopupMenu(this@StatisticsActivity, anchor)
                popup.menu.add(0, 1, 0, "Edit Sesi")
                popup.menu.add(0, 2, 1, "Hapus Sesi")
                popup.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        1 -> {
                            showEditSessionDialog(dateKey, session)
                            true
                        }
                        2 -> {
                            StudyRepository.deleteSession(this@StatisticsActivity, dateKey, session)
                            Toast.makeText(this@StatisticsActivity, "Sesi ${session.subject} dihapus", Toast.LENGTH_SHORT).show()
                            refreshAll()
                            true
                        }
                        else -> false
                    }
                }
                popup.show()
            }
        }

        card.addView(colorBar)
        card.addView(infoCol)
        card.addView(tvDuration)
        card.addView(btnMore)
        return card
    }

    @Suppress("UNUSED_PARAMETER")
    private fun showAddSessionDialog(dateKey: String, defaultStartMin: Int) {
        val scale = resources.displayMetrics.density
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding((20 * scale).toInt(), (12 * scale).toInt(), (20 * scale).toInt(), (8 * scale).toInt())
        }

        val etSubject = EditText(this).apply {
            hint = "Nama Mapel (cth: Matematika)"
            setText("Matematika")
        }
        val etDurationMin = EditText(this).apply {
            hint = "Durasi (menit), contoh: 45"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            setText("45")
        }

        container.addView(etSubject)
        container.addView(etDurationMin)

        AlertDialog.Builder(this)
            .setTitle("Tambah Catatan Sesi ($dateKey)")
            .setView(container)
            .setPositiveButton("Simpan") { _, _ ->
                val subj = etSubject.text.toString().trim().ifEmpty { "Belajar" }.replace("::", " ").replace("||", " ")
                val mins = etDurationMin.text.toString().toIntOrNull()?.coerceIn(1, 720) ?: 30
                val durSecs = mins * 60
                val endMillis = if (dateKey == StudyRepository.todayDateKey()) {
                    System.currentTimeMillis()
                } else {
                    val cal = Calendar.getInstance().apply {
                        set(selectedYear, selectedMonth, selectedDay, 14, 0, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    cal.timeInMillis + durSecs * 1000L
                }
                val startMillis = endMillis - durSecs * 1000L
                val startMin = StudyRepository.millisToMinutesFrom5Am(startMillis)

                val newSession = StudyRepository.StudySession(
                    id = "manual_${System.currentTimeMillis()}",
                    subject = subj,
                    startTimeMillis = startMillis,
                    endTimeMillis = endMillis,
                    startMinFrom5Am = startMin,
                    durationSeconds = durSecs
                )
                StudyRepository.saveCustomSession(this, dateKey, newSession)
                if (dateKey == StudyRepository.todayDateKey()) {
                    val prefs = getSharedPreferences(StudyRepository.PREFS_NAME, Context.MODE_PRIVATE)
                    prefs.edit().putInt("time_$subj", prefs.getInt("time_$subj", 0) + durSecs).apply()
                }
                refreshAll()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun showEditSessionDialog(dateKey: String, session: StudyRepository.StudySession) {
        val scale = resources.displayMetrics.density
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding((20 * scale).toInt(), (12 * scale).toInt(), (20 * scale).toInt(), (8 * scale).toInt())
        }

        val etSubject = EditText(this).apply {
            hint = "Nama Mapel"
            setText(session.subject)
        }
        val etDurationMin = EditText(this).apply {
            hint = "Durasi (menit)"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            setText(max(1, session.durationSeconds / 60).toString())
        }

        container.addView(etSubject)
        container.addView(etDurationMin)

        AlertDialog.Builder(this)
            .setTitle("Edit Sesi Belajar")
            .setView(container)
            .setPositiveButton("Simpan") { _, _ ->
                val newSubj = etSubject.text.toString().trim().ifEmpty { session.subject }.replace("::", " ").replace("||", " ")
                val newMins = etDurationMin.text.toString().toIntOrNull()?.coerceIn(1, 720)
                    ?: max(1, session.durationSeconds / 60)
                val newDurationSecs = newMins * 60

                StudyRepository.deleteSession(this, dateKey, session)
                val updated = session.copy(
                    id = if (session.id.startsWith("manual_") || session.id.startsWith("rec_")) session.id else "manual_${System.currentTimeMillis()}",
                    subject = newSubj,
                    endTimeMillis = session.startTimeMillis + newDurationSecs * 1000L,
                    durationSeconds = newDurationSecs
                )
                StudyRepository.saveCustomSession(this, dateKey, updated)
                if (dateKey == StudyRepository.todayDateKey()) {
                    val prefs = getSharedPreferences(StudyRepository.PREFS_NAME, Context.MODE_PRIVATE)
                    prefs.edit().putInt("time_$newSubj", prefs.getInt("time_$newSubj", 0) + newDurationSecs).apply()
                }
                refreshAll()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun computeHourlyCumulativeSeconds(sessions: List<StudyRepository.StudySession>): IntArray {
        val perHour = IntArray(24)
        for (s in sessions) {
            val hourIdx = (StudyRepository.millisToMinutesFrom5Am(s.startTimeMillis) / 60).coerceIn(0, 23)
            perHour[hourIdx] += s.durationSeconds
        }
        val cumulative = IntArray(24)
        var running = 0
        for (i in 0 until 24) {
            running += perHour[i]
            cumulative[i] = running
        }
        return cumulative
    }

    private fun colorForSubjectName(subject: String): Int {
        val idx = kotlin.math.abs(subject.hashCode()) % chartPalette.size
        return Color.parseColor(chartPalette[idx])
    }

    private fun selectedDateKey(): String = StudyRepository.formatDateKey(selectedYear, selectedMonth, selectedDay)
}
