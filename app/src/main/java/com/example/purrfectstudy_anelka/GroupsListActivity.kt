package com.example.purrfectstudy_anelka

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView

class GroupsListActivity : AppCompatActivity() {

    private lateinit var tvGroupsCount: TextView
    private lateinit var containerGroupsList: LinearLayout
    private lateinit var layoutSearchBar: LinearLayout
    private lateinit var etSearchGroup: EditText

    private var currentFilterQuery = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_groups_list)

        tvGroupsCount = findViewById(R.id.tvGroupsCount)
        containerGroupsList = findViewById(R.id.containerGroupsList)
        layoutSearchBar = findViewById(R.id.layoutSearchBar)
        etSearchGroup = findViewById(R.id.etSearchGroup)
        findViewById<NestedScrollView>(R.id.scrollGroupsList).enableMouseWheelScroll()

        findViewById<ImageView>(R.id.btnSearchGroup).setOnClickListener {
            if (layoutSearchBar.visibility == View.VISIBLE) {
                layoutSearchBar.visibility = View.GONE
                etSearchGroup.setText("")
                currentFilterQuery = ""
                renderGroupsList()
            } else {
                layoutSearchBar.visibility = View.VISIBLE
                etSearchGroup.requestFocus()
            }
        }

        findViewById<TextView>(R.id.btnCloseSearch).setOnClickListener {
            layoutSearchBar.visibility = View.GONE
            etSearchGroup.setText("")
            currentFilterQuery = ""
            renderGroupsList()
        }

        etSearchGroup.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                currentFilterQuery = s?.toString()?.trim() ?: ""
                renderGroupsList()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        findViewById<TextView>(R.id.btnAddGroup).setOnClickListener {
            showCreateGroupDialog()
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
        findViewById<LinearLayout>(R.id.navCalendar).setOnClickListener {
            val intent = Intent(this, CalendarActivity::class.java)
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

        renderGroupsList()
    }

    override fun onResume() {
        super.onResume()
        renderGroupsList()
    }

    private fun renderGroupsList() {
        val allGroups = StudyRepository.getStudyGroups(this)
        val filtered = if (currentFilterQuery.isEmpty()) {
            allGroups.mapIndexed { idx, g -> idx to g }
        } else {
            allGroups.mapIndexed { idx, g -> idx to g }.filter { (_, g) ->
                g.name.contains(currentFilterQuery, ignoreCase = true) ||
                    g.idCode.contains(currentFilterQuery, ignoreCase = true)
            }
        }

        tvGroupsCount.text = if (currentFilterQuery.isEmpty()) {
            "${allGroups.size} groups"
        } else {
            "${filtered.size} of ${allGroups.size} groups"
        }

        containerGroupsList.removeAllViews()
        val scale = resources.displayMetrics.density

        if (filtered.isEmpty()) {
            val tvEmpty = TextView(this).apply {
                text = "Grup dengan nama/ID '$currentFilterQuery' tidak ditemukan."
                textSize = 13.5f
                setTextColor(Color.parseColor("#757575"))
                setPadding((20 * scale).toInt(), (24 * scale).toInt(), (20 * scale).toInt(), (24 * scale).toInt())
            }
            containerGroupsList.addView(tvEmpty)
            return
        }

        for ((originalIndex, group) in filtered) {
            val wrapper = LinearLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                orientation = LinearLayout.VERTICAL
                setPadding((20 * scale).toInt(), 0, (20 * scale).toInt(), 0)
                isClickable = true
                isFocusable = true
                setOnClickListener {
                    openGroupDetail(originalIndex, group)
                }
            }

            val row = LinearLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(0, (16 * scale).toInt(), 0, (16 * scale).toInt())
            }

            val textCol = LinearLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                orientation = LinearLayout.VERTICAL
            }

            val tvGroupName = TextView(this).apply {
                text = group.name
                textSize = 16f
                setTextColor(Color.parseColor("#1F1F1F"))
            }

            // Baris metadata: ID Grup + Badge Public/Private + Jumlah Members
            val metaRow = LinearLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { topMargin = (5 * scale).toInt() }
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }

            val tvBadge = TextView(this).apply {
                text = group.visibility
                textSize = 11f
                setTextColor(Color.parseColor(if (group.visibility == "Private") "#1F1F1F" else "#6E6A63"))
                background = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadius = 6f * scale
                    setColor(Color.parseColor("#F2F2F2"))
                }
                setPadding((6 * scale).toInt(), (2 * scale).toInt(), (6 * scale).toInt(), (2 * scale).toInt())
            }

            val tvMeta = TextView(this).apply {
                text = " ${group.idCode} • ${group.activeMembers} / ${group.maxMembers} members"
                textSize = 12.5f
                setTextColor(Color.parseColor("#757575"))
            }

            metaRow.addView(tvBadge)
            metaRow.addView(tvMeta)

            textCol.addView(tvGroupName)
            textCol.addView(metaRow)

            val ivChat = ImageView(this).apply {
                layoutParams = LinearLayout.LayoutParams((22 * scale).toInt(), (22 * scale).toInt())
                setImageResource(R.drawable.ic_chat_bubble)
                contentDescription = "Chat ${group.name}"
                isClickable = true
                isFocusable = true
                setOnClickListener {
                    openGroupDetail(originalIndex, group)
                }
            }

            val divider = View(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    (1 * scale).toInt()
                )
                setBackgroundColor(Color.parseColor("#EFEFEF"))
            }

            row.addView(textCol)
            row.addView(ivChat)
            wrapper.addView(row)
            wrapper.addView(divider)
            containerGroupsList.addView(wrapper)
        }
    }

    private fun openGroupDetail(originalIndex: Int, group: StudyRepository.StudyGroup) {
        val intent = Intent(this, GroupDetailActivity::class.java).apply {
            putExtra("EXTRA_GROUP_INDEX", originalIndex)
            putExtra("EXTRA_GROUP_ID", group.idCode)
            putExtra("EXTRA_GROUP_NAME", group.name)
            putExtra("EXTRA_GROUP_PRIVACY", group.visibility)
            putExtra("EXTRA_GROUP_MAX_MEMBERS", group.maxMembers)
            putExtra("EXTRA_GROUP_MEMBER_COUNT", group.activeMembers)
            putExtra("EXTRA_GROUP_CREATOR", group.creator)
            putExtra("EXTRA_GROUP_DESCRIPTION", group.description)
        }
        startActivity(intent)
        @Suppress("DEPRECATION")
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    private fun showCreateGroupDialog() {
        val generatedId = StudyRepository.generateUniqueGroupId(this)
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 28, 48, 12)
        }

        val myNickname = StudyRepository.getUserNickname(this)
        val tvIdInfo = TextView(this).apply {
            text = "ID Grup Otomatis: $generatedId • Pembuat: $myNickname"
            textSize = 12.5f
            setTextColor(Color.parseColor("#757575"))
            setPadding(0, 0, 0, 14)
        }

        val etName = EditText(this).apply {
            hint = "Nama grup baru (cth: pejuang snbt)"
        }

        val etDesc = EditText(this).apply {
            hint = "Deskripsi / Rules grup (opsional)"
            maxLines = 3
        }

        val tvMaxLabel = TextView(this).apply {
            text = "Batas Maksimal Anggota:"
            textSize = 12.5f
            setTextColor(Color.parseColor("#1F1F1F"))
            setPadding(0, 18, 0, 4)
        }
        val maxOptions = arrayOf("10", "20", "30")
        val spMax = Spinner(this).apply {
            adapter = ArrayAdapter(this@GroupsListActivity, android.R.layout.simple_spinner_dropdown_item, maxOptions)
            setSelection(1) // default 20
        }

        val tvVisLabel = TextView(this).apply {
            text = "Visibilitas Grup:"
            textSize = 12.5f
            setTextColor(Color.parseColor("#1F1F1F"))
            setPadding(0, 14, 0, 4)
        }
        val visOptions = arrayOf("Public", "Private")
        val spVis = Spinner(this).apply {
            adapter = ArrayAdapter(this@GroupsListActivity, android.R.layout.simple_spinner_dropdown_item, visOptions)
        }

        container.addView(tvIdInfo)
        container.addView(etName)
        container.addView(etDesc)
        container.addView(tvMaxLabel)
        container.addView(spMax)
        container.addView(tvVisLabel)
        container.addView(spVis)

        AlertDialog.Builder(this)
            .setTitle("Buat Grup Belajar")
            .setView(container)
            .setPositiveButton("Buat") { _, _ ->
                val name = etName.text.toString().trim()
                val desc = etDesc.text.toString().trim()
                val maxMembers = maxOptions[spMax.selectedItemPosition].toIntOrNull() ?: 20
                val visibility = visOptions[spVis.selectedItemPosition]
                if (name.isNotEmpty()) {
                    StudyRepository.addCustomGroup(this, generatedId, name, maxMembers, visibility, desc)
                    currentFilterQuery = ""
                    etSearchGroup.setText("")
                    layoutSearchBar.visibility = View.GONE
                    renderGroupsList()
                    Toast.makeText(this, "Grup '$name' ($generatedId) dibuat!", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }
}
