package com.example.purrfectstudy_anelka

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StatisticsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistics)

        val btnBack = findViewById<TextView>(R.id.btnBackStats)
        btnBack?.setOnClickListener {
            finish()
        }

        val currentDateFormatted = SimpleDateFormat("EEE, MMM dd, yyyy", Locale.getDefault()).format(Date())
        findViewById<TextView>(R.id.tvDailyDateHeader)?.text = currentDateFormatted

        loadDynamicStatistics()
    }

    private fun loadDynamicStatistics() {
        val prefs = getSharedPreferences("PurrfectStudyPrefs", Context.MODE_PRIVATE)

        val defaultSubjects = listOf("Matematika", "Bahasa Indonesia", "Bahasa Inggris")
        val customSet = prefs.getStringSet("CUSTOM_SUBJECTS_SET", emptySet()) ?: emptySet()

        val allSubjects = mutableMapOf<String, Int>()
        var grandTotalSeconds = 0

        for (sub in defaultSubjects) {
            val secs = prefs.getInt("time_$sub", 0)
            if (secs > 0) allSubjects[sub] = secs
            grandTotalSeconds += secs
        }

        for (sub in customSet) {
            val secs = prefs.getInt("time_$sub", 0)
            if (secs > 0) allSubjects[sub] = secs
            grandTotalSeconds += secs
        }

        val totalTimeStr = formatSecondsToTime(grandTotalSeconds)
        findViewById<TextView>(R.id.tvStatTotalStudy)?.text = totalTimeStr
        findViewById<TextView>(R.id.tvStatMaxFocus)?.text = totalTimeStr
        findViewById<TextView>(R.id.tvMascotTimer)?.text = totalTimeStr
        findViewById<TextView>(R.id.tvDonutTotal)?.text = totalTimeStr
        findViewById<TextView>(R.id.tvCalToday)?.text = "18\n$totalTimeStr"

        val containerBreakdown = findViewById<LinearLayout>(R.id.containerBreakdown)
        containerBreakdown?.removeAllViews()

        val scale = resources.displayMetrics.density

        for ((name, secs) in allSubjects) {
            val percentage = if (grandTotalSeconds > 0) (secs * 100) / grandTotalSeconds else 0
            val timeFormatted = formatSecondsToTime(secs)

            val rowLayout = LinearLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { setMargins(0, (4 * scale).toInt(), 0, (4 * scale).toInt()) }
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }

            val colorDot = View(this).apply {
                layoutParams = LinearLayout.LayoutParams((10 * scale).toInt(), (10 * scale).toInt()).apply {
                    rightMargin = (8 * scale).toInt()
                }
                background = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    setColor(Color.BLACK)
                }
            }

            val tvSubName = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                text = name
                textSize = 13f
                setTextColor(Color.BLACK)
            }

            val tvRatio = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                text = "$timeFormatted - $percentage%"
                textSize = 12f
                setTextColor(Color.parseColor("#666666"))
            }

            rowLayout.addView(colorDot)
            rowLayout.addView(tvSubName)
            rowLayout.addView(tvRatio)
            containerBreakdown.addView(rowLayout)
        }

        val containerTimeline = findViewById<LinearLayout>(R.id.containerTimeline)
        containerTimeline?.removeAllViews()

        for ((name, secs) in allSubjects) {
            val timeFormatted = formatSecondsToTime(secs)

            val timelineCard = LinearLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { setMargins(0, 0, 0, (8 * scale).toInt()) }
                orientation = LinearLayout.VERTICAL
                background = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    setColor(Color.parseColor("#FAFAFA"))
                }
                setPadding((12 * scale).toInt(), (12 * scale).toInt(), (12 * scale).toInt(), (12 * scale).toInt())
            }

            val tvTimeRange = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                text = "Active Study Session ($timeFormatted)"
                textSize = 11f
                setTextColor(Color.parseColor("#888888"))
            }

            val subRow = LinearLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                    topMargin = (4 * scale).toInt()
                }
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }

            val indicator = View(this).apply {
                layoutParams = LinearLayout.LayoutParams((4 * scale).toInt(), (24 * scale).toInt()).apply {
                    rightMargin = (8 * scale).toInt()
                }
                setBackgroundColor(Color.BLACK)
            }

            val tvSub = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                text = name
                textSize = 14f
                setTypeface(null, Typeface.BOLD)
                setTextColor(Color.BLACK)
            }

            val tvDur = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                text = timeFormatted
                textSize = 13f
                setTextColor(Color.BLACK)
            }

            subRow.addView(indicator)
            subRow.addView(tvSub)
            subRow.addView(tvDur)

            timelineCard.addView(tvTimeRange)
            timelineCard.addView(subRow)
            containerTimeline.addView(timelineCard)
        }
    }

    private fun formatSecondsToTime(totalSeconds: Int): String {
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val secs = totalSeconds % 60
        return String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, secs)
    }
}