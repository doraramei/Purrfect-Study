package com.example.purrfectstudy_anelka

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView

class GroupDetailActivity : AppCompatActivity() {

    private var groupIndex: Int = 0
    private var groupIdCode: String = ""
    private var currentGroup: StudyRepository.StudyGroup? = null

    private lateinit var tvDetailGroupName: TextView
    private lateinit var tvDetailGroupSubtitle: TextView

    private lateinit var tvTabChat: TextView
    private lateinit var tvTabGroupInfo: TextView
    private lateinit var indicatorTabChat: View
    private lateinit var indicatorTabGroupInfo: View

    private lateinit var layoutChatSection: LinearLayout
    private lateinit var scrollChatMessages: ScrollView
    private lateinit var containerChatMessages: LinearLayout
    private lateinit var etChatMessage: EditText

    private lateinit var scrollGroupInfoSection: NestedScrollView
    private lateinit var tvGroupDescription: TextView
    private lateinit var btnEditGroupRules: TextView
    private lateinit var tvInfoGroupId: TextView
    private lateinit var tvInfoInviteLink: TextView
    private lateinit var tvInfoCreator: TextView
    private lateinit var tvInfoVisibility: TextView
    private lateinit var tvInfoCapacity: TextView
    private lateinit var tvInfoMembersHeader: TextView
    private lateinit var containerInfoMembers: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_detail)

        groupIndex = intent.getIntExtra("EXTRA_GROUP_INDEX", 0)
        groupIdCode = intent.getStringExtra("EXTRA_GROUP_ID") ?: ""

        tvDetailGroupName = findViewById(R.id.tvDetailGroupName)
        tvDetailGroupSubtitle = findViewById(R.id.tvDetailGroupSubtitle)

        tvTabChat = findViewById(R.id.tvTabChat)
        tvTabGroupInfo = findViewById(R.id.tvTabGroupInfo)
        indicatorTabChat = findViewById(R.id.indicatorTabChat)
        indicatorTabGroupInfo = findViewById(R.id.indicatorTabGroupInfo)

        layoutChatSection = findViewById(R.id.layoutChatSection)
        scrollChatMessages = findViewById(R.id.scrollChatMessages)
        containerChatMessages = findViewById(R.id.containerChatMessages)
        etChatMessage = findViewById(R.id.etChatMessage)

        scrollGroupInfoSection = findViewById(R.id.scrollGroupInfoSection)
        scrollGroupInfoSection.enableMouseWheelScroll()
        tvGroupDescription = findViewById(R.id.tvGroupDescription)
        btnEditGroupRules = findViewById(R.id.btnEditGroupRules)
        tvInfoGroupId = findViewById(R.id.tvInfoGroupId)
        tvInfoInviteLink = findViewById(R.id.tvInfoInviteLink)
        tvInfoCreator = findViewById(R.id.tvInfoCreator)
        tvInfoVisibility = findViewById(R.id.tvInfoVisibility)
        tvInfoCapacity = findViewById(R.id.tvInfoCapacity)
        tvInfoMembersHeader = findViewById(R.id.tvInfoMembersHeader)
        containerInfoMembers = findViewById(R.id.containerInfoMembers)

        loadAndBindGroup()

        findViewById<TextView>(R.id.btnGroupBack).setOnClickListener {
            finishWithFade()
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finishWithFade()
            }
        })

        findViewById<TextView>(R.id.btnStartStudy).setOnClickListener {
            val group = currentGroup ?: return@setOnClickListener
            val startIntent = Intent(this, FocusTimerActivity::class.java).apply {
                putExtra("EXTRA_GROUP_INDEX", groupIndex)
                putExtra("EXTRA_GROUP_NAME", group.name)
                putExtra("EXTRA_SUBJECT", "Group Study • Sirkel")
            }
            startActivity(startIntent)
            @Suppress("DEPRECATION")
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        findViewById<LinearLayout>(R.id.tabChat).setOnClickListener { switchTab(true) }
        findViewById<LinearLayout>(R.id.tabGroupInfo).setOnClickListener { switchTab(false) }

        findViewById<TextView>(R.id.btnSendChat).setOnClickListener { sendMessage() }
        etChatMessage.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendMessage()
                true
            } else {
                false
            }
        }

        btnEditGroupRules.setOnClickListener { showEditDescriptionDialog() }

        findViewById<TextView>(R.id.btnCopyInviteLink).setOnClickListener {
            val linkText = tvInfoInviteLink.text.toString()
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("Group Invite Link", linkText))
            Toast.makeText(this, "Link disalin: $linkText", Toast.LENGTH_SHORT).show()
        }

        switchTab(true)
    }

    override fun onResume() {
        super.onResume()
        loadAndBindGroup()
    }

    private fun finishWithFade() {
        finish()
        @Suppress("DEPRECATION")
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    private fun loadAndBindGroup() {
        val allGroups = StudyRepository.getStudyGroups(this)
        val matchedIndex = when {
            groupIdCode.isNotEmpty() -> allGroups.indexOfFirst { it.idCode == groupIdCode }
            groupIndex in allGroups.indices -> groupIndex
            else -> 0
        }.let { if (it >= 0) it else 0 }

        val myNickname = StudyRepository.getUserNickname(this)
        val resolvedGroup = allGroups.getOrNull(matchedIndex) ?: StudyRepository.StudyGroup(
            idCode = groupIdCode.ifEmpty { "#PS-2027" },
            name = intent.getStringExtra("EXTRA_GROUP_NAME") ?: "Study Group",
            visibility = intent.getStringExtra("EXTRA_GROUP_PRIVACY") ?: "Public",
            activeMembers = intent.getIntExtra("EXTRA_GROUP_MEMBER_COUNT", 1),
            maxMembers = intent.getIntExtra("EXTRA_GROUP_MAX_MEMBERS", 20),
            creator = intent.getStringExtra("EXTRA_GROUP_CREATOR") ?: myNickname,
            description = intent.getStringExtra("EXTRA_GROUP_DESCRIPTION")
                ?: "Fokus belajar bareng & jaga konsistensi setiap hari.",
            members = listOf(StudyRepository.MemberSlot(myNickname, "me", "0:00:00"))
        )

        groupIndex = matchedIndex
        groupIdCode = resolvedGroup.idCode
        currentGroup = resolvedGroup

        tvDetailGroupName.text = resolvedGroup.name
        tvDetailGroupSubtitle.text = "${resolvedGroup.activeMembers} / ${resolvedGroup.maxMembers} members • ${resolvedGroup.idCode}"

        renderChatMessages()
        renderGroupInfo()
    }

    private fun switchTab(isChat: Boolean) {
        if (isChat) {
            layoutChatSection.visibility = View.VISIBLE
            scrollGroupInfoSection.visibility = View.GONE
            tvTabChat.setTextColor(Color.parseColor("#1F1F1F"))
            tvTabChat.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD))
            indicatorTabChat.setBackgroundColor(Color.parseColor("#1F1F1F"))

            tvTabGroupInfo.setTextColor(Color.parseColor("#757575"))
            tvTabGroupInfo.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL))
            indicatorTabGroupInfo.setBackgroundColor(Color.TRANSPARENT)
        } else {
            layoutChatSection.visibility = View.GONE
            scrollGroupInfoSection.visibility = View.VISIBLE
            tvTabGroupInfo.setTextColor(Color.parseColor("#1F1F1F"))
            tvTabGroupInfo.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD))
            indicatorTabGroupInfo.setBackgroundColor(Color.parseColor("#1F1F1F"))

            tvTabChat.setTextColor(Color.parseColor("#757575"))
            tvTabChat.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL))
            indicatorTabChat.setBackgroundColor(Color.TRANSPARENT)
        }
    }

    private fun sendMessage() {
        val text = etChatMessage.text.toString().trim()
        if (text.isEmpty()) return
        val group = currentGroup ?: return
        StudyRepository.addGroupChatMessage(this, group.idCode, text)
        etChatMessage.setText("")
        renderChatMessages()
    }

    private fun renderChatMessages() {
        val group = currentGroup ?: return
        val messages = StudyRepository.getGroupChatMessages(this, group.idCode)
        containerChatMessages.removeAllViews()

        val scale = resources.displayMetrics.density
        val maxBubbleWidth = (resources.displayMetrics.widthPixels * 0.76).toInt()

        for (msg in messages) {
            val row = LinearLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = (12 * scale).toInt() }
                orientation = LinearLayout.VERTICAL
                gravity = if (msg.isMine) Gravity.END else Gravity.START
            }

            if (!msg.isMine) {
                val tvSender = TextView(this).apply {
                    text = msg.sender
                    textSize = 11.5f
                    setTextColor(Color.parseColor("#757575"))
                    setPadding((4 * scale).toInt(), 0, (4 * scale).toInt(), (4 * scale).toInt())
                }
                row.addView(tvSender)
            }

            val bubble = LinearLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                orientation = LinearLayout.VERTICAL
                background = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadius = 14f * scale
                    setColor(Color.parseColor(if (msg.isMine) "#1F1F1F" else "#F3F3F3"))
                }
                setPadding((14 * scale).toInt(), (10 * scale).toInt(), (14 * scale).toInt(), (9 * scale).toInt())
            }

            val tvBody = TextView(this).apply {
                text = msg.message
                textSize = 14f
                maxWidth = maxBubbleWidth
                setLineSpacing(2f * scale, 1f)
                setTextColor(Color.parseColor(if (msg.isMine) "#FFFFFF" else "#1F1F1F"))
            }

            val tvTime = TextView(this).apply {
                text = msg.timeText
                textSize = 10.5f
                gravity = Gravity.END
                setTextColor(Color.parseColor(if (msg.isMine) "#BDBDBD" else "#757575"))
                setPadding(0, (4 * scale).toInt(), 0, 0)
            }

            bubble.addView(tvBody)
            bubble.addView(tvTime)
            row.addView(bubble)
            containerChatMessages.addView(row)
        }

        scrollChatMessages.post {
            scrollChatMessages.fullScroll(View.FOCUS_DOWN)
        }
    }

    private fun renderGroupInfo() {
        val group = currentGroup ?: return
        val myNickname = StudyRepository.getUserNickname(this)
        tvGroupDescription.text = group.description

        val isOwner = group.creator.equals(myNickname, ignoreCase = true)
        btnEditGroupRules.visibility = if (isOwner) View.VISIBLE else View.GONE

        tvInfoGroupId.text = group.idCode
        val cleanCode = group.idCode.removePrefix("#")
        tvInfoInviteLink.text = "purrfect.study/g/$cleanCode"

        tvInfoCreator.text = group.creator
        tvInfoVisibility.text = group.visibility
        tvInfoCapacity.text = "${group.activeMembers} / ${group.maxMembers} members"

        val membersList = group.members
        tvInfoMembersHeader.text = "Members (${group.activeMembers})"
        containerInfoMembers.removeAllViews()

        val scale = resources.displayMetrics.density
        for ((idx, member) in membersList.withIndex()) {
            val row = LinearLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(0, (10 * scale).toInt(), 0, (10 * scale).toInt())
            }

            val ivAvatar = ImageView(this).apply {
                layoutParams = LinearLayout.LayoutParams((36 * scale).toInt(), (36 * scale).toInt()).apply {
                    marginEnd = (12 * scale).toInt()
                }
                scaleType = ImageView.ScaleType.FIT_CENTER
                setImageResource(resolveAvatarDrawable(member.iconType))
                contentDescription = member.name
            }

            val nameCol = LinearLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }

            val tvMemberName = TextView(this).apply {
                text = member.name
                textSize = 14f
                setTextColor(Color.parseColor("#1F1F1F"))
                if (member.name.equals(myNickname, ignoreCase = true)) {
                    setTypeface(null, Typeface.BOLD)
                }
            }
            nameCol.addView(tvMemberName)

            if (member.name.equals(group.creator, ignoreCase = true)) {
                val tvAdminBadge = TextView(this).apply {
                    val lp = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply { marginStart = (8 * scale).toInt() }
                    layoutParams = lp
                    text = "Admin"
                    textSize = 10.5f
                    setTypeface(null, Typeface.BOLD)
                    setTextColor(Color.WHITE)
                    background = GradientDrawable().apply {
                        shape = GradientDrawable.RECTANGLE
                        cornerRadius = 6f * scale
                        setColor(Color.parseColor("#1F1F1F"))
                    }
                    setPadding((7 * scale).toInt(), (2 * scale).toInt(), (7 * scale).toInt(), (2 * scale).toInt())
                }
                nameCol.addView(tvAdminBadge)
            }

            val tvDuration = TextView(this).apply {
                text = member.durationText
                textSize = 12.5f
                fontFeatureSettings = "tnum"
                setTextColor(Color.parseColor("#757575"))
            }

            row.addView(ivAvatar)
            row.addView(nameCol)
            row.addView(tvDuration)
            containerInfoMembers.addView(row)

            if (idx < membersList.lastIndex) {
                val divider = View(this).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        (1 * scale).toInt()
                    )
                    setBackgroundColor(Color.parseColor("#EFEFEF"))
                }
                containerInfoMembers.addView(divider)
            }
        }
    }

    private fun resolveAvatarDrawable(iconType: String): Int {
        return when (iconType) {
            "me" -> StudyRepository.getSelectedMascotDrawable(this)
            "sleep" -> R.drawable.ic_cat_sleep
            "orange" -> R.drawable.ic_cat_orange
            "cyber" -> R.drawable.ic_cat_cyber
            "wizard" -> R.drawable.ic_cat_wizard
            "white" -> R.drawable.ic_cat_white
            else -> R.drawable.ic_desk
        }
    }

    private fun showEditDescriptionDialog() {
        val group = currentGroup ?: return
        val etEdit = EditText(this).apply {
            setText(group.description)
            hint = "Tulis deskripsi & aturan grup..."
            maxLines = 4
        }
        val wrapper = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 24, 48, 12)
            addView(etEdit)
        }

        AlertDialog.Builder(this)
            .setTitle("Edit About & Group Rules")
            .setView(wrapper)
            .setPositiveButton("Simpan") { _, _ ->
                val newDesc = etEdit.text.toString().trim()
                StudyRepository.updateGroupDescription(this, group.idCode, newDesc)
                loadAndBindGroup()
                Toast.makeText(this, "Deskripsi grup diperbarui", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Batal", null)
            .show()
    }
}
