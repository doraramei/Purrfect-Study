package com.example.purrfectstudy_anelka

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2

class FocusTimerActivity : AppCompatActivity() {

    private var sessionSeconds = 0
    private var isRunning = true
    private var isSaved = false

    private lateinit var handler: Handler
    private lateinit var runnable: Runnable
    private lateinit var subjectName: String

    private lateinit var tvRoomTimer: TextView
    private lateinit var ivBannerMascot: ImageView
    private lateinit var viewPagerGroups: ViewPager2

    private lateinit var groupsList: List<StudyRepository.StudyGroup>
    private lateinit var pagerAdapter: GroupPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_focus_timer)

        subjectName = intent.getStringExtra("EXTRA_SUBJECT") ?: "Focus timer"
        val initialGroupName = intent.getStringExtra("EXTRA_GROUP_NAME")
        val initialGroupIndexArg = intent.getIntExtra("EXTRA_GROUP_INDEX", -1)

        findViewById<TextView>(R.id.tvSubjectTitle).text = "Focus timer"

        val tvBadge = findViewById<TextView>(R.id.tvActiveSubjectBadge)
        if (subjectName != "Focus timer") {
            tvBadge.text = subjectName
            tvBadge.visibility = View.VISIBLE
        }

        tvRoomTimer = findViewById(R.id.tvRoomTimer)
        ivBannerMascot = findViewById(R.id.ivBannerMascot)
        viewPagerGroups = findViewById(R.id.viewPagerGroups)

        groupsList = StudyRepository.getStudyGroups(this)
        val initialIndex = when {
            initialGroupIndexArg in groupsList.indices -> initialGroupIndexArg
            initialGroupName != null -> groupsList.indexOfFirst { it.name == initialGroupName }.let { if (it >= 0) it else 3 }
            else -> groupsList.indexOfFirst { it.name == "SIRKEL SIRKEL AN" }.let { if (it >= 0) it else 0 }
        }

        pagerAdapter = GroupPagerAdapter(groupsList)
        viewPagerGroups.adapter = pagerAdapter
        viewPagerGroups.setCurrentItem(initialIndex, false)

        applySavedMascot()

        findViewById<TextView>(R.id.btnBack).setOnClickListener {
            saveStudyTimeAndExit()
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                saveStudyTimeAndExit()
            }
        })

        ivBannerMascot.setOnClickListener { openStudiconStore() }

        tvRoomTimer.setOnClickListener {
            isRunning = !isRunning
            val statusMsg = if (isRunning) "Timer dilanjutkan" else "Timer dijeda"
            Toast.makeText(this, statusMsg, Toast.LENGTH_SHORT).show()
        }

        findViewById<TextView>(R.id.btnAllowedApps).setOnClickListener {
            showAllowedAppsDialog()
        }

        // Bottom Navigation Bar dengan transisi halus
        findViewById<LinearLayout>(R.id.navHome).setOnClickListener {
            saveStudyTime()
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
            saveStudyTime()
            val intent = Intent(this, TodoActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            startActivity(intent)
            @Suppress("DEPRECATION")
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
            @Suppress("DEPRECATION")
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
        findViewById<LinearLayout>(R.id.navCalendar).setOnClickListener {
            saveStudyTime()
            val intent = Intent(this, CalendarActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            startActivity(intent)
            @Suppress("DEPRECATION")
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
            @Suppress("DEPRECATION")
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
        findViewById<FrameLayout>(R.id.navGroups).setOnClickListener {
            saveStudyTime()
            val intent = Intent(this, GroupsListActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            startActivity(intent)
            @Suppress("DEPRECATION")
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
            @Suppress("DEPRECATION")
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
        findViewById<FrameLayout>(R.id.btnQuickSettings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
            @Suppress("DEPRECATION")
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        handler = Handler(Looper.getMainLooper())
        runnable = object : Runnable {
            override fun run() {
                if (isRunning) {
                    sessionSeconds++
                    val formatted = StudyRepository.formatSecondsToTime(sessionSeconds)
                    tvRoomTimer.text = formatted
                    pagerAdapter.updateActiveTimers(formatted)
                }
                handler.postDelayed(this, 1000)
            }
        }
        handler.post(runnable)
    }

    override fun onResume() {
        super.onResume()
        groupsList = StudyRepository.getStudyGroups(this)
        pagerAdapter.updateGroups(groupsList)
        applySavedMascot()
    }

    private fun openStudiconStore() {
        startActivity(Intent(this, StudiconStoreActivity::class.java))
        @Suppress("DEPRECATION")
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    private fun applySavedMascot() {
        val resId = StudyRepository.getSelectedMascotDrawable(this)
        ivBannerMascot.setImageResource(resId)
        pagerAdapter.updateMascot(resId)
    }

    private fun showAllowedAppsDialog() {
        val allowedApps = arrayOf(
            "• Purrfect Study (Timer & Catatan)",
            "• Kamus & Penerjemah",
            "• Kalkulator Ilmiah",
            "• Google Docs / PDF Reader"
        )
        AlertDialog.Builder(this)
            .setTitle("Allowed Apps")
            .setItems(allowedApps, null)
            .setPositiveButton("Tutup", null)
            .show()
    }

    private fun saveStudyTime() {
        if (!isSaved && sessionSeconds > 0) {
            isSaved = true
            StudyRepository.recordRealStudySession(this, subjectName, sessionSeconds)
        }
    }

    private fun saveStudyTimeAndExit() {
        saveStudyTime()
        finish()
        @Suppress("DEPRECATION")
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(runnable)
    }

    inner class GroupPagerAdapter(
        private var groups: List<StudyRepository.StudyGroup>
    ) : RecyclerView.Adapter<GroupPagerAdapter.GroupViewHolder>() {

        private val boundHolders = mutableSetOf<GroupViewHolder>()

        inner class GroupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val tvGroupTitle: TextView = itemView.findViewById(R.id.tvGroupTitle)
            val layoutPageDots: LinearLayout = itemView.findViewById(R.id.layoutPageDots)
            val tvGroupSubtitle: TextView = itemView.findViewById(R.id.tvGroupSubtitle)
            val gridMembers: GridLayout = itemView.findViewById(R.id.gridMembers)
            var ivMyMemberMascot: ImageView? = null
            var tvMyMemberTimer: TextView? = null
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_group_page, parent, false)
            return GroupViewHolder(view)
        }

        override fun getItemCount(): Int = groups.size

        override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
            boundHolders.add(holder)
            val group = groups[position]
            val scale = resources.displayMetrics.density

            holder.tvGroupTitle.text = group.name
            val studyingCount = group.members.count {
                it.iconType == "me" || (it.durationText != "0:00:00" && it.iconType != "sleep")
            }
            holder.tvGroupSubtitle.text = "$studyingCount members Studying • ${group.idCode}"

            // Render titik indikator tepat di sebelah kanan judul grup
            holder.layoutPageDots.removeAllViews()
            for (i in groups.indices) {
                val isSelected = i == position
                val size = ((if (isSelected) 7 else 5) * scale).toInt()
                val dot = View(this@FocusTimerActivity).apply {
                    layoutParams = LinearLayout.LayoutParams(size, size).apply {
                        setMargins((3 * scale).toInt(), 0, (3 * scale).toInt(), 0)
                    }
                    background = GradientDrawable().apply {
                        shape = GradientDrawable.OVAL
                        setColor(Color.parseColor(if (isSelected) "#1F1F1F" else "#D1D1D6"))
                    }
                }
                holder.layoutPageDots.addView(dot)
            }

            // Render Grid 4 Kolom (dengan kolom pengisi jika member < 4 agar lebar kolom rata 4)
            holder.gridMembers.removeAllViews()
            val currentMascotRes = StudyRepository.getSelectedMascotDrawable(this@FocusTimerActivity)
            val formattedTime = StudyRepository.formatSecondsToTime(sessionSeconds)

            group.members.forEach { slot ->
                val isMe = slot.iconType == "me"

                val cell = LinearLayout(this@FocusTimerActivity).apply {
                    layoutParams = GridLayout.LayoutParams().apply {
                        width = 0
                        height = GridLayout.LayoutParams.WRAP_CONTENT
                        columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                        val m = (4 * scale).toInt()
                        setMargins(m, m, m, m)
                    }
                    orientation = LinearLayout.VERTICAL
                    gravity = Gravity.CENTER
                    setPadding((6 * scale).toInt(), (10 * scale).toInt(), (6 * scale).toInt(), (10 * scale).toInt())
                    if (isMe) {
                        setBackgroundResource(R.drawable.bg_active_member_card)
                        isClickable = true
                        isFocusable = true
                        setOnClickListener { openStudiconStore() }
                    }
                }

                val iv = ImageView(this@FocusTimerActivity).apply {
                    val imgSize = ((if (isMe) 48 else 44) * scale).toInt()
                    layoutParams = LinearLayout.LayoutParams(imgSize, imgSize).apply {
                        gravity = Gravity.CENTER_HORIZONTAL
                        bottomMargin = (6 * scale).toInt()
                    }
                    scaleType = ImageView.ScaleType.FIT_CENTER
                    when (slot.iconType) {
                        "me" -> setImageResource(currentMascotRes)
                        "sleep" -> setImageResource(R.drawable.ic_cat_sleep)
                        "orange" -> setImageResource(R.drawable.ic_cat_orange)
                        "cyber" -> setImageResource(R.drawable.ic_cat_cyber)
                        "wizard" -> setImageResource(R.drawable.ic_cat_wizard)
                        "white" -> setImageResource(R.drawable.ic_cat_white)
                        else -> setImageResource(R.drawable.ic_desk)
                    }
                }

                val tvName = TextView(this@FocusTimerActivity).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    gravity = Gravity.CENTER
                    textAlignment = View.TEXT_ALIGNMENT_CENTER
                    ellipsize = TextUtils.TruncateAt.END
                    maxLines = 1
                    text = slot.name
                    textSize = 11.5f
                    if (isMe) {
                        setTypeface(null, Typeface.BOLD)
                        setTextColor(Color.parseColor("#1F1F1F"))
                    } else {
                        setTextColor(Color.parseColor("#757575"))
                    }
                }

                val tvTimer = TextView(this@FocusTimerActivity).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    gravity = Gravity.CENTER
                    textAlignment = View.TEXT_ALIGNMENT_CENTER
                    text = if (isMe) formattedTime else slot.durationText
                    textSize = 11.5f
                    fontFeatureSettings = "tnum"
                    setPadding(0, (2 * scale).toInt(), 0, 0)
                    setTextColor(Color.parseColor(if (isMe) "#757575" else "#8E8E93"))
                }

                if (isMe) {
                    holder.ivMyMemberMascot = iv
                    holder.tvMyMemberTimer = tvTimer
                }

                cell.addView(iv)
                cell.addView(tvName)
                cell.addView(tvTimer)
                holder.gridMembers.addView(cell)
            }

            // Tambahkan placeholder tak terlihat bila jumlah member < 4 (misal grup baru 1 member) agar tetap rapi di kolom 1 dari 4
            if (group.members.size < 4) {
                for (c in group.members.size until 4) {
                    val emptyCell = View(this@FocusTimerActivity).apply {
                        layoutParams = GridLayout.LayoutParams().apply {
                            width = 0
                            height = (90 * scale).toInt()
                            columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                            val m = (4 * scale).toInt()
                            setMargins(m, m, m, m)
                        }
                    }
                    holder.gridMembers.addView(emptyCell)
                }
            }
        }

        override fun onViewRecycled(holder: GroupViewHolder) {
            super.onViewRecycled(holder)
            boundHolders.remove(holder)
        }

        fun updateActiveTimers(formatted: String) {
            for (h in boundHolders) {
                h.tvMyMemberTimer?.text = formatted
            }
        }

        fun updateMascot(resId: Int) {
            for (h in boundHolders) {
                h.ivMyMemberMascot?.setImageResource(resId)
            }
        }

        fun updateGroups(newGroups: List<StudyRepository.StudyGroup>) {
            groups = newGroups
            notifyDataSetChanged()
        }
    }
}
