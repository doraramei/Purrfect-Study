package com.example.purrfectstudy_anelka

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.max
import kotlin.random.Random

object StudyRepository {

    const val PREFS_NAME = "PurrfectStudyPrefs"

    val defaultHomeSubjects = listOf(
        "Literasi B.Ing",
        "Matematika",
        "Summary"
    )

    data class StudySession(
        val id: String,
        val subject: String,
        val startTimeMillis: Long,
        val endTimeMillis: Long,
        val startMinFrom5Am: Int,
        val durationSeconds: Int
    )

    data class CalendarAgenda(
        val id: String,
        val title: String,
        val timeRange: String,
        val note: String,
        val isDone: Boolean = false
    )

    data class MemberSlot(
        val name: String,
        val iconType: String,
        val durationText: String
    )

    data class GroupChatMessage(
        val sender: String,
        val message: String,
        val timeText: String,
        val isMine: Boolean
    )

    data class StudyGroup(
        val idCode: String,
        val name: String,
        val visibility: String,
        val activeMembers: Int,
        val maxMembers: Int,
        val creator: String,
        val description: String,
        val members: List<MemberSlot>
    )

    data class StudiconItem(
        val key: String,
        val name: String,
        val drawableRes: Int,
        val coinPrice: Int,
        val idrPriceLabel: String,
        val isProExclusive: Boolean = false
    )

    val studiconCatalog = listOf(
        StudiconItem(
            key = "default",
            name = "Black Scholar Cat",
            drawableRes = R.drawable.ic_cat_default,
            coinPrice = 0,
            idrPriceLabel = "Gratis",
            isProExclusive = false
        ),
        StudiconItem(
            key = "sleep",
            name = "Matcha Sleepy Cat",
            drawableRes = R.drawable.ic_cat_sleep,
            coinPrice = 150,
            idrPriceLabel = "Rp 9.000",
            isProExclusive = false
        ),
        StudiconItem(
            key = "orange",
            name = "Coffee Tabby Cat",
            drawableRes = R.drawable.ic_cat_orange,
            coinPrice = 250,
            idrPriceLabel = "Rp 12.000",
            isProExclusive = false
        ),
        StudiconItem(
            key = "cyber",
            name = "Cyber Glasses Cat",
            drawableRes = R.drawable.ic_cat_cyber,
            coinPrice = 400,
            idrPriceLabel = "Rp 15.000",
            isProExclusive = false
        ),
        StudiconItem(
            key = "wizard",
            name = "Wizard Exam Cat",
            drawableRes = R.drawable.ic_cat_wizard,
            coinPrice = 0,
            idrPriceLabel = "Rp 19.000",
            isProExclusive = true
        ),
        StudiconItem(
            key = "white",
            name = "Mochi White Cat",
            drawableRes = R.drawable.ic_cat_white,
            coinPrice = 300,
            idrPriceLabel = "Rp 12.000",
            isProExclusive = false
        )
    )

    // --- Pengaturan Profil & Preferensi Pengguna ---

    fun isLoggedIn(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean("is_logged_in", false)
    }

    fun setLoggedIn(context: Context, loggedIn: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putBoolean("is_logged_in", loggedIn).apply()
    }

    fun getUserNickname(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString("USER_NICKNAME", "syilanel")?.takeIf { it.isNotBlank() } ?: "syilanel"
    }

    fun setUserNickname(context: Context, nickname: String) {
        val clean = nickname.replace("::", " ").replace("||", " ").trim().ifEmpty { "syilanel" }
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putString("USER_NICKNAME", clean).apply()
    }

    fun getStatusMessage(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString("USER_STATUS_MESSAGE", "Fokus raih target hari ini") ?: "Fokus raih target hari ini"
    }

    fun setStatusMessage(context: Context, status: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putString("USER_STATUS_MESSAGE", status.trim()).apply()
    }

    fun getUserCategory(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString("USER_CATEGORY", "Kelas 12") ?: "Kelas 12"
    }

    fun setUserCategory(context: Context, category: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putString("USER_CATEGORY", category).apply()
    }

    fun getThemeMode(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString("APP_THEME_MODE", "Monokrom Terang") ?: "Monokrom Terang"
    }

    fun setThemeMode(context: Context, mode: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putString("APP_THEME_MODE", mode).apply()
    }

    fun getUserRegion(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString("USER_REGION", "Indonesia") ?: "Indonesia"
    }

    fun setUserRegion(context: Context, region: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putString("USER_REGION", region).apply()
    }

    fun getUserLanguage(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString("USER_LANGUAGE", "Indonesia") ?: "Indonesia"
    }

    fun setUserLanguage(context: Context, lang: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putString("USER_LANGUAGE", lang).apply()
    }

    // --- Studicon Store & PurrCoins ---

    fun getPurrCoins(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt("PURR_COINS", 500)
    }

    fun addPurrCoins(context: Context, amount: Int) {
        if (amount <= 0) return
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val current = prefs.getInt("PURR_COINS", 500)
        prefs.edit().putInt("PURR_COINS", current + amount).apply()
    }

    fun spendPurrCoins(context: Context, amount: Int): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val current = prefs.getInt("PURR_COINS", 500)
        if (current < amount) return false
        prefs.edit().putInt("PURR_COINS", current - amount).apply()
        return true
    }

    fun isPurrfectPassActive(context: Context): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean("IS_PURRFECT_PASS_PRO", false)
    }

    fun activatePurrfectPass(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val owned = getOwnedStudiconKeys(context).toMutableSet()
        studiconCatalog.forEach { owned.add(it.key) }
        prefs.edit()
            .putBoolean("IS_PURRFECT_PASS_PRO", true)
            .putStringSet("OWNED_STUDICONS_SET", owned)
            .apply()
    }

    fun getOwnedStudiconKeys(context: Context): Set<String> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val saved = prefs.getStringSet("OWNED_STUDICONS_SET", emptySet()) ?: emptySet()
        val result = saved.toMutableSet()
        result.add("default")
        if (isPurrfectPassActive(context)) {
            studiconCatalog.forEach { result.add(it.key) }
        }
        return result
    }

    fun unlockStudicon(context: Context, key: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val owned = getOwnedStudiconKeys(context).toMutableSet()
        owned.add(key)
        prefs.edit().putStringSet("OWNED_STUDICONS_SET", owned).apply()
    }

    fun getEquippedStudiconKey(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString("SELECTED_CAT_MASCOT", "default") ?: "default"
    }

    fun equipStudicon(context: Context, key: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putString("SELECTED_CAT_MASCOT", key).apply()
    }

    fun getEquippedStudiconName(context: Context): String {
        val key = getEquippedStudiconKey(context)
        return studiconCatalog.firstOrNull { it.key == key }?.name ?: "Black Scholar Cat"
    }

    fun getSelectedMascotDrawable(context: Context): Int {
        val savedKey = getEquippedStudiconKey(context)
        return studiconCatalog.firstOrNull { it.key == savedKey }?.drawableRes ?: R.drawable.ic_cat_default
    }

    // --- Harian & Mata Pelajaran ---

    fun ensureDailyResetWithoutLosingConfig(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val currentDate = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        val savedDate = prefs.getString("LAST_STUDY_DATE", "")
        if (savedDate != currentDate) {
            val editor = prefs.edit()
            for (key in prefs.all.keys) {
                if (key.startsWith("time_")) {
                    editor.remove(key)
                }
            }
            editor.putString("LAST_STUDY_DATE", currentDate).apply()
        }
    }

    fun getActiveHomeSubjects(context: Context): List<String> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val deletedDefaults = prefs.getStringSet("DELETED_DEFAULT_SUBJECTS_SET", emptySet()) ?: emptySet()
        val customSet = prefs.getStringSet("CUSTOM_SUBJECTS_SET", emptySet()) ?: emptySet()

        val result = mutableListOf<String>()
        for (sub in defaultHomeSubjects) {
            if (!deletedDefaults.contains(sub)) {
                result.add(sub)
            }
        }
        for (sub in customSet.sorted()) {
            if (!defaultHomeSubjects.contains(sub) || deletedDefaults.contains(sub)) {
                result.add(sub)
            }
        }
        return result
    }

    // --- Grup & Obrolan ---

    fun getStudyGroups(context: Context): List<StudyGroup> {
        val myNickname = getUserNickname(context)
        val baseGroups = mutableListOf(
            StudyGroup(
                idCode = "#PS-2027",
                name = "road to ptn 2027",
                visibility = "Public",
                activeMembers = 12,
                maxMembers = 30,
                creator = myNickname,
                description = "Target lolos SNBT 2027! Wajib belajar minimal 2 jam/hari, matikan distraksi, dan saling semangati di chat.",
                members = listOf(
                    MemberSlot(myNickname, "me", "0:00:00"),
                    MemberSlot("nadia", "desk", "0:42:10"),
                    MemberSlot("rafi", "sleep", "0:15:00"),
                    MemberSlot("kenji", "desk", "1:10:00"),
                    MemberSlot("alma", "orange", "0:55:20"),
                    MemberSlot("bagas", "desk", "0:00:00"),
                    MemberSlot("citra", "desk", "0:28:40"),
                    MemberSlot("dimas", "desk", "0:00:00"),
                    MemberSlot("elsa", "white", "0:18:05"),
                    MemberSlot("fajar", "desk", "0:00:00"),
                    MemberSlot("zahra", "sleep", "0:05:12"),
                    MemberSlot("gilang", "desk", "0:00:00")
                )
            ),
            StudyGroup(
                idCode = "#PS-1827",
                name = "study buddies 27",
                visibility = "Public",
                activeMembers = 8,
                maxMembers = 20,
                creator = "tania",
                description = "Ruang belajar santai tapi konsisten setiap malam. Share target harianmu sebelum mulai timer!",
                members = listOf(
                    MemberSlot(myNickname, "me", "0:00:00"),
                    MemberSlot("tania", "orange", "0:34:15"),
                    MemberSlot("bima", "desk", "0:00:00"),
                    MemberSlot("nabila", "desk", "0:22:00"),
                    MemberSlot("arya", "white", "1:04:30"),
                    MemberSlot("keisha", "desk", "0:00:00"),
                    MemberSlot("rehan", "sleep", "0:12:00"),
                    MemberSlot("siti", "desk", "0:00:00")
                )
            ),
            StudyGroup(
                idCode = "#PS-2799",
                name = "kami pejuang PTN'27",
                visibility = "Private",
                activeMembers = 14,
                maxMembers = 30,
                creator = "deva",
                description = "Grup privat pejuang PTN 2027. Fokus drill soal PK, PM, dan Literasi setiap hari.",
                members = listOf(
                    MemberSlot(myNickname, "me", "0:00:00"),
                    MemberSlot("deva", "desk", "1:12:00"),
                    MemberSlot("rani", "orange", "0:48:10"),
                    MemberSlot("akbar", "desk", "0:00:00"),
                    MemberSlot("bella", "desk", "0:30:00"),
                    MemberSlot("candra", "desk", "0:00:00"),
                    MemberSlot("dina", "white", "0:54:20"),
                    MemberSlot("erik", "desk", "0:00:00"),
                    MemberSlot("fira", "desk", "0:19:00"),
                    MemberSlot("galih", "desk", "0:00:00"),
                    MemberSlot("zahra", "sleep", "0:08:40"),
                    MemberSlot("hendra", "desk", "0:00:00"),
                    MemberSlot("intan", "desk", "0:26:15"),
                    MemberSlot("joko", "desk", "0:00:00")
                )
            ),
            StudyGroup(
                idCode = "#PS-4821",
                name = "SIRKEL SIRKEL AN",
                visibility = "Public",
                activeMembers = 16,
                maxMembers = 20,
                creator = "Mayy",
                description = "Sirkel belajar bareng tiap sore & malam. Dilarang spam saat anggota lain sedang fokus.",
                members = listOf(
                    MemberSlot(myNickname, "me", "0:00:00"),
                    MemberSlot("Mayy", "desk", "0:00:00"),
                    MemberSlot("napss", "desk", "0:00:00"),
                    MemberSlot("dinda", "desk", "0:00:00"),
                    MemberSlot("naya", "desk", "0:00:00"),
                    MemberSlot("rara", "desk", "0:00:00"),
                    MemberSlot("aulia", "desk", "0:00:00"),
                    MemberSlot("Rizky", "desk", "0:00:00"),
                    MemberSlot("salsa", "desk", "0:00:00"),
                    MemberSlot("farhan", "desk", "0:00:00"),
                    MemberSlot("zahra", "sleep", "0:00:00"),
                    MemberSlot("dhiya", "desk", "0:00:00"),
                    MemberSlot("putri", "desk", "0:00:00"),
                    MemberSlot("annisa", "desk", "0:00:00"),
                    MemberSlot("fauzan", "desk", "0:00:00"),
                    MemberSlot("yuni", "desk", "0:00:00")
                )
            ),
            StudyGroup(
                idCode = "#PS-3055",
                name = "sirkel utbkuy",
                visibility = "Public",
                activeMembers = 10,
                maxMembers = 20,
                creator = "naufal",
                description = "Fokus review materi & pembahasan tryout UTBK mingguan. Konsisten adalah kunci!",
                members = listOf(
                    MemberSlot(myNickname, "me", "0:00:00"),
                    MemberSlot("mira", "desk", "0:35:00"),
                    MemberSlot("naufal", "orange", "1:02:10"),
                    MemberSlot("oliv", "desk", "0:00:00"),
                    MemberSlot("prama", "desk", "0:14:20"),
                    MemberSlot("qori", "white", "0:47:00"),
                    MemberSlot("rizal", "desk", "0:00:00"),
                    MemberSlot("sekar", "desk", "0:00:00"),
                    MemberSlot("tiara", "sleep", "0:09:15"),
                    MemberSlot("umar", "desk", "0:00:00")
                )
            )
        )

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val rawCustom = prefs.getString("CUSTOM_GROUPS_V2", "") ?: ""
        if (rawCustom.isNotEmpty()) {
            for (entry in rawCustom.split("||")) {
                val parts = entry.split("::")
                if (parts.size >= 4) {
                    val idCode = parts[0]
                    val gName = parts[1]
                    val maxM = parts[2].toIntOrNull() ?: 20
                    val vis = parts[3]
                    val desc = parts.getOrNull(4)?.takeIf { it.isNotBlank() }
                        ?: "Fokus belajar bareng & jaga konsistensi setiap hari."
                    baseGroups.add(
                        StudyGroup(
                            idCode = idCode,
                            name = gName,
                            visibility = vis,
                            activeMembers = 1,
                            maxMembers = maxM,
                            creator = myNickname,
                            description = desc,
                            members = listOf(MemberSlot(myNickname, "me", "0:00:00"))
                        )
                    )
                }
            }
        }
        return baseGroups.map { group ->
            val savedDesc = prefs.getString("GROUP_DESC_${group.idCode}", null)
            if (!savedDesc.isNullOrBlank()) group.copy(description = savedDesc) else group
        }
    }

    fun generateUniqueGroupId(context: Context): String {
        val existingIds = getStudyGroups(context).map { it.idCode }.toSet()
        var candidate: String
        do {
            val num = Random.nextInt(1000, 9999)
            candidate = "#PS-$num"
        } while (existingIds.contains(candidate))
        return candidate
    }

    fun addCustomGroup(
        context: Context,
        idCode: String,
        name: String,
        maxMembers: Int,
        visibility: String,
        description: String = ""
    ) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val rawCustom = prefs.getString("CUSTOM_GROUPS_V2", "") ?: ""
        val cleanName = name.replace("::", " ").replace("||", " ").trim()
        if (cleanName.isEmpty()) return
        val cleanDesc = description.replace("::", " ").replace("||", " ").trim()
            .ifEmpty { "Fokus belajar bareng & jaga konsistensi setiap hari." }
        val newEntry = "$idCode::$cleanName::$maxMembers::$visibility::$cleanDesc"
        val updated = if (rawCustom.isEmpty()) newEntry else "$rawCustom||$newEntry"
        prefs.edit().putString("CUSTOM_GROUPS_V2", updated).apply()
    }

    fun updateGroupDescription(context: Context, idCode: String, newDescription: String) {
        val cleanDesc = newDescription.replace("::", " ").replace("||", " ").trim()
            .ifEmpty { "Fokus belajar bareng & jaga konsistensi setiap hari." }
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString("GROUP_DESC_$idCode", cleanDesc).apply()
    }

    fun getGroupChatMessages(context: Context, idCode: String): List<GroupChatMessage> {
        val myNickname = getUserNickname(context)
        val seeded = when (idCode) {
            "#PS-2027" -> listOf(
                GroupChatMessage("nadia", "Hari ini aku target kelar 2 paket PK dulu ya teman-teman!", "08:15", false),
                GroupChatMessage("rafi", "Gass! Jangan lupa nyalain timer fokus biar ke-track di statistik.", "08:22", false),
                GroupChatMessage(myNickname, "Siap, aku baru mulai sesi Literasi B.Ing pagi ini.", "08:30", true)
            )
            "#PS-1827" -> listOf(
                GroupChatMessage("tania", "Selamat datang di Study Buddies! Jam fokus malam kita mulai 19.30 ya.", "19:10", false),
                GroupChatMessage("arya", "Siap tan, hari ini lanjut review TPS penalaran umum.", "19:18", false)
            )
            "#PS-2799" -> listOf(
                GroupChatMessage("deva", "Reminder TO Akbar minggu ini: target minimal 650+!", "13:05", false),
                GroupChatMessage("dina", "Noted dev, lagi ngebut bahas soal PM kemarin nih.", "13:12", false),
                GroupChatMessage(myNickname, "Ikut join pembahasan sore nanti ya!", "13:20", true)
            )
            "#PS-4821" -> listOf(
                GroupChatMessage("Mayy", "Halo guys, sesi sore udah dibuka yaa. Fokus 50 menit istirahat 10 menit.", "15:45", false),
                GroupChatMessage("Rizky", "Oke Mayy, izin silent dulu sambil ngerjain summary.", "15:48", false),
                GroupChatMessage("salsa", "Semangat semuanya!", "15:52", false)
            )
            "#PS-3055" -> listOf(
                GroupChatMessage("naufal", "Pembahasan TO Literasi kemarin sudah ku-rangkum di catatan ya.", "10:02", false),
                GroupChatMessage("qori", "Mantap naufal, lanjut timer 1 jam dulu buat review!", "10:11", false)
            )
            else -> listOf(
                GroupChatMessage(myNickname, "Selamat datang di grup belajar! Yuk mulai fokus bareng.", "09:00", true)
            )
        }

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val raw = prefs.getString("GROUP_CHAT_$idCode", "") ?: ""
        if (raw.isEmpty()) return seeded

        val saved = raw.split("||").mapNotNull { entry ->
            val parts = entry.split("::")
            if (parts.size >= 4) {
                val isMine = parts[3] == "1"
                GroupChatMessage(
                    sender = if (isMine) myNickname else parts[0],
                    message = parts[1],
                    timeText = parts[2],
                    isMine = isMine
                )
            } else {
                null
            }
        }
        return seeded + saved
    }

    fun addGroupChatMessage(context: Context, idCode: String, message: String) {
        val cleanMsg = message.replace("::", " ").replace("||", " ").trim()
        if (cleanMsg.isEmpty()) return
        val myNickname = getUserNickname(context)
        val nowTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val raw = prefs.getString("GROUP_CHAT_$idCode", "") ?: ""
        val entry = "$myNickname::$cleanMsg::$nowTime::1"
        val updated = if (raw.isEmpty()) entry else "$raw||$entry"
        prefs.edit().putString("GROUP_CHAT_$idCode", updated).apply()
    }

    // --- Agenda Kalender per tanggal ---

    fun getAgendasForDate(context: Context, dateKey: String): List<CalendarAgenda> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val raw = prefs.getString("CAL_AGENDAS_$dateKey", "") ?: ""
        if (raw.isEmpty()) return emptyList()
        return raw.split("||").mapNotNull { entry ->
            val parts = entry.split("::")
            if (parts.size >= 5) {
                CalendarAgenda(
                    id = parts[0],
                    title = parts[1],
                    timeRange = parts[2],
                    note = parts[3],
                    isDone = parts[4] == "1"
                )
            } else {
                null
            }
        }
    }

    private fun writeAgendasForDate(context: Context, dateKey: String, agendas: List<CalendarAgenda>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val encoded = agendas.joinToString("||") { a ->
            val cleanTitle = a.title.replace("::", " ").replace("||", " ")
            val cleanTime = a.timeRange.replace("::", " ").replace("||", " ")
            val cleanNote = a.note.replace("::", " ").replace("||", " ")
            "${a.id}::$cleanTitle::$cleanTime::$cleanNote::${if (a.isDone) "1" else "0"}"
        }
        prefs.edit().putString("CAL_AGENDAS_$dateKey", encoded).apply()
    }

    fun addAgendaForDate(context: Context, dateKey: String, agenda: CalendarAgenda) {
        val list = getAgendasForDate(context, dateKey).toMutableList()
        list.add(agenda)
        writeAgendasForDate(context, dateKey, list)
    }

    fun toggleAgendaDone(context: Context, dateKey: String, agendaId: String, isDone: Boolean) {
        val list = getAgendasForDate(context, dateKey).map {
            if (it.id == agendaId) it.copy(isDone = isDone) else it
        }
        writeAgendasForDate(context, dateKey, list)
    }

    fun deleteAgendaForDate(context: Context, dateKey: String, agendaId: String) {
        val list = getAgendasForDate(context, dateKey).filter { it.id != agendaId }
        writeAgendasForDate(context, dateKey, list)
    }

    // --- Statistik & Sesi Belajar Real-Time (Tanpa Data Dummy Palsu) ---

    fun todayDateKey(): String {
        val cal = Calendar.getInstance()
        return formatDateKey(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        )
    }

    fun recordRealStudySession(context: Context, subject: String, durationSeconds: Int) {
        if (durationSeconds <= 0) return
        ensureDailyResetWithoutLosingConfig(context)

        val cleanSubject = subject.replace("::", " ").replace("||", " ").trim().ifEmpty { "Focus Study" }
        val endMillis = System.currentTimeMillis()
        val startMillis = endMillis - durationSeconds * 1000L

        val endCal = Calendar.getInstance().apply { timeInMillis = endMillis }
        val dateKey = formatDateKey(
            endCal.get(Calendar.YEAR),
            endCal.get(Calendar.MONTH),
            endCal.get(Calendar.DAY_OF_MONTH)
        )
        val startMinFrom5Am = millisToMinutesFrom5Am(startMillis)

        val session = StudySession(
            id = "rec_$endMillis",
            subject = cleanSubject,
            startTimeMillis = startMillis,
            endTimeMillis = endMillis,
            startMinFrom5Am = startMinFrom5Am,
            durationSeconds = durationSeconds
        )
        saveCustomSession(context, dateKey, session)

        // Sinkronkan total waktu harian pada Beranda & hadiah +10 PurrCoins
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val keyTime = "time_$cleanSubject"
        val existingSeconds = prefs.getInt(keyTime, 0)
        prefs.edit().putInt(keyTime, existingSeconds + durationSeconds).apply()
        addPurrCoins(context, 10)
    }

    @Suppress("UNUSED_PARAMETER")
    fun getSessionsForDate(
        context: Context,
        dateKey: String,
        todayYear: Int,
        todayMonth: Int,
        todayDay: Int
    ): List<StudySession> {
        return loadCustomSessions(context, dateKey).sortedBy { it.startTimeMillis }
    }

    fun loadCustomSessions(context: Context, dateKey: String): List<StudySession> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val raw = prefs.getString("STATS_CUSTOM_SESSIONS_$dateKey", "") ?: ""
        if (raw.isEmpty()) return emptyList()

        return raw.split("||").mapNotNull { entry ->
            val parts = entry.split("::")
            when {
                parts.size >= 6 -> {
                    val startMin = parts[2].toIntOrNull() ?: return@mapNotNull null
                    val durSecs = parts[3].toIntOrNull() ?: return@mapNotNull null
                    val startMillis = parts[4].toLongOrNull() ?: return@mapNotNull null
                    val endMillis = parts[5].toLongOrNull() ?: return@mapNotNull null
                    StudySession(parts[0], parts[1], startMillis, endMillis, startMin, durSecs)
                }
                parts.size == 4 -> {
                    val startMin = parts[2].toIntOrNull() ?: return@mapNotNull null
                    val durSecs = parts[3].toIntOrNull() ?: return@mapNotNull null
                    val startMillis = minutesFrom5AmToDateMillis(dateKey, startMin)
                    val endMillis = startMillis + durSecs * 1000L
                    StudySession(parts[0], parts[1], startMillis, endMillis, startMin, durSecs)
                }
                else -> null
            }
        }
    }

    fun saveCustomSession(context: Context, dateKey: String, session: StudySession) {
        val list = loadCustomSessions(context, dateKey).toMutableList()
        list.add(session)
        writeCustomSessions(context, dateKey, list)
    }

    fun writeCustomSessions(context: Context, dateKey: String, sessions: List<StudySession>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val encoded = sessions.joinToString("||") {
            val cleanSub = it.subject.replace("::", " ").replace("||", " ")
            "${it.id}::$cleanSub::${it.startMinFrom5Am}::${it.durationSeconds}::${it.startTimeMillis}::${it.endTimeMillis}"
        }
        prefs.edit().putString("STATS_CUSTOM_SESSIONS_$dateKey", encoded).apply()
    }

    fun deleteSession(context: Context, dateKey: String, session: StudySession) {
        val remaining = loadCustomSessions(context, dateKey).filter { it.id != session.id }
        writeCustomSessions(context, dateKey, remaining)

        if (dateKey == todayDateKey()) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val keyTime = "time_${session.subject}"
            val current = prefs.getInt(keyTime, 0)
            if (current > 0) {
                prefs.edit().putInt(keyTime, max(0, current - session.durationSeconds)).apply()
            }
        }
    }

    // --- Utilitas Tanggal & Timestamp ---

    fun minutesFrom5AmToDateMillis(dateKey: String, startMinFrom5Am: Int): Long {
        val cal = Calendar.getInstance()
        val parsed = runCatching {
            SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse(dateKey)
        }.getOrNull()
        if (parsed != null) {
            cal.time = parsed
        }
        cal.set(Calendar.HOUR_OF_DAY, 5)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        cal.add(Calendar.MINUTE, startMinFrom5Am)
        return cal.timeInMillis
    }

    fun millisToMinutesFrom5Am(millis: Long): Int {
        val cal = Calendar.getInstance().apply { timeInMillis = millis }
        val hour24 = cal.get(Calendar.HOUR_OF_DAY)
        val minute = cal.get(Calendar.MINUTE)
        val minsFromMidnight = hour24 * 60 + minute
        return if (hour24 >= 5) minsFromMidnight - 5 * 60 else minsFromMidnight + 19 * 60
    }

    fun formatMillisAmPm(millis: Long): String {
        return SimpleDateFormat("a h:mm", Locale.ENGLISH).format(Date(millis))
    }

    fun formatHourCutoffLabel(millis: Long): String {
        return SimpleDateFormat("h a", Locale.ENGLISH).format(Date(millis))
    }

    fun formatDateKey(year: Int, monthZeroBased: Int, day: Int): String {
        return String.format(Locale.ENGLISH, "%04d-%02d-%02d", year, monthZeroBased + 1, day)
    }

    fun formatSecondsToTime(totalSeconds: Int): String {
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val secs = totalSeconds % 60
        return String.format(Locale.ENGLISH, "%d:%02d:%02d", hours, minutes, secs)
    }

    fun formatHourMinuteShort(totalSeconds: Int): String {
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        return String.format(Locale.ENGLISH, "%d:%02d", hours, minutes)
    }

    fun formatMinutesFrom5Am(minFrom5Am: Int): String {
        val totalMins24 = (5 * 60 + minFrom5Am) % (24 * 60)
        val hour24 = totalMins24 / 60
        val minute = totalMins24 % 60
        val amPm = if (hour24 < 12) "AM" else "PM"
        val hour12 = when {
            hour24 == 0 -> 12
            hour24 > 12 -> hour24 - 12
            else -> hour24
        }
        return String.format(Locale.ENGLISH, "%s %d:%02d", amPm, hour12, minute)
    }
}
