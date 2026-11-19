package com.example.purrfectstudy_anelka

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.TextUtils
import android.view.Gravity
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView

class StudiconStoreActivity : AppCompatActivity() {

    private lateinit var tvPurrCoinsBalance: TextView
    private lateinit var tvProBannerDesc: TextView
    private lateinit var btnPurrfectPass: TextView
    private lateinit var gridStudiconCatalog: GridLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_studicon_store)

        findViewById<TextView>(R.id.btnStoreBack).setOnClickListener {
            finish()
            @Suppress("DEPRECATION")
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        findViewById<NestedScrollView>(R.id.scrollStudiconStore).enableMouseWheelScroll()

        tvPurrCoinsBalance = findViewById(R.id.tvPurrCoinsBalance)
        tvProBannerDesc = findViewById(R.id.tvProBannerDesc)
        btnPurrfectPass = findViewById(R.id.btnPurrfectPass)
        gridStudiconCatalog = findViewById(R.id.gridStudiconCatalog)

        btnPurrfectPass.setOnClickListener {
            if (StudyRepository.isPurrfectPassActive(this)) {
                Toast.makeText(this, "Purrfect Pass (Pro) sudah aktif!", Toast.LENGTH_SHORT).show()
            } else {
                showActivateProDialog()
            }
        }

        renderStore()
    }

    override fun onResume() {
        super.onResume()
        renderStore()
    }

    private fun renderStore() {
        val coins = StudyRepository.getPurrCoins(this)
        val isPro = StudyRepository.isPurrfectPassActive(this)
        val ownedKeys = StudyRepository.getOwnedStudiconKeys(this)
        val equippedKey = StudyRepository.getEquippedStudiconKey(this)

        tvPurrCoinsBalance.text = "$coins PurrCoins"

        if (isPro) {
            tvProBannerDesc.text = "Seluruh koleksi Studicon eksklusif telah terbuka penuh."
            btnPurrfectPass.text = "Pro Active ✓"
        } else {
            tvProBannerDesc.text = "Buka semua koleksi Studicon eksklusif termasuk Wizard Exam Cat tanpa batas."
            btnPurrfectPass.text = "Aktifkan Pro • Rp 29.000"
        }

        gridStudiconCatalog.removeAllViews()
        val scale = resources.displayMetrics.density

        for (item in StudyRepository.studiconCatalog) {
            val isEquipped = item.key == equippedKey
            val isOwned = ownedKeys.contains(item.key)

            val card = LinearLayout(this).apply {
                layoutParams = GridLayout.LayoutParams().apply {
                    width = 0
                    height = GridLayout.LayoutParams.WRAP_CONTENT
                    columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                    val m = (6 * scale).toInt()
                    setMargins(m, m, m, m)
                }
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER_HORIZONTAL
                setPadding((12 * scale).toInt(), (16 * scale).toInt(), (12 * scale).toInt(), (14 * scale).toInt())
                background = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadius = 12f * scale
                    setColor(Color.WHITE)
                    if (isEquipped) {
                        setStroke((2 * scale).toInt(), Color.parseColor("#1F1F1F"))
                    } else {
                        setStroke((1 * scale).toInt(), Color.parseColor("#EFEFEF"))
                    }
                }
            }

            val ivPreview = ImageView(this).apply {
                layoutParams = LinearLayout.LayoutParams((76 * scale).toInt(), (76 * scale).toInt()).apply {
                    bottomMargin = (10 * scale).toInt()
                }
                scaleType = ImageView.ScaleType.FIT_CENTER
                setImageResource(item.drawableRes)
                contentDescription = item.name
            }

            val tvName = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                gravity = Gravity.CENTER
                maxLines = 1
                ellipsize = TextUtils.TruncateAt.END
                text = item.name
                textSize = 13.5f
                setTypeface(null, Typeface.BOLD)
                setTextColor(Color.parseColor("#1F1F1F"))
            }

            val priceLabel = when {
                isEquipped -> "Sedang Dipakai"
                isOwned -> "Owned"
                item.isProExclusive -> "Pro Exclusive • ${item.idrPriceLabel}"
                else -> "${item.coinPrice} Coins / ${item.idrPriceLabel}"
            }

            val tvPrice = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = (4 * scale).toInt()
                    bottomMargin = (12 * scale).toInt()
                }
                gravity = Gravity.CENTER
                maxLines = 1
                ellipsize = TextUtils.TruncateAt.END
                text = priceLabel
                textSize = 11.5f
                setTextColor(Color.parseColor("#757575"))
            }

            val btnAction = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    (34 * scale).toInt()
                )
                gravity = Gravity.CENTER
                textSize = 12.5f
                setTypeface(null, Typeface.BOLD)
                isClickable = true
                isFocusable = true

                when {
                    isEquipped -> {
                        text = "Equipped"
                        setTextColor(Color.parseColor("#757575"))
                        background = GradientDrawable().apply {
                            shape = GradientDrawable.RECTANGLE
                            cornerRadius = 17f * scale
                            setColor(Color.parseColor("#F0F0F0"))
                        }
                        setOnClickListener {
                            Toast.makeText(this@StudiconStoreActivity, "${item.name} sudah dipakai!", Toast.LENGTH_SHORT).show()
                        }
                    }
                    isOwned -> {
                        text = "Equip"
                        setTextColor(Color.WHITE)
                        background = GradientDrawable().apply {
                            shape = GradientDrawable.RECTANGLE
                            cornerRadius = 17f * scale
                            setColor(Color.parseColor("#1F1F1F"))
                        }
                        setOnClickListener {
                            StudyRepository.equipStudicon(this@StudiconStoreActivity, item.key)
                            Toast.makeText(this@StudiconStoreActivity, "Maskot ${item.name} dipakai!", Toast.LENGTH_SHORT).show()
                            renderStore()
                        }
                    }
                    else -> {
                        text = "Buy"
                        setTextColor(Color.parseColor("#1F1F1F"))
                        background = GradientDrawable().apply {
                            shape = GradientDrawable.RECTANGLE
                            cornerRadius = 17f * scale
                            setColor(Color.WHITE)
                            setStroke((1.5f * scale).toInt(), Color.parseColor("#1F1F1F"))
                        }
                        setOnClickListener {
                            showBuyStudiconDialog(item)
                        }
                    }
                }
            }

            card.addView(ivPreview)
            card.addView(tvName)
            card.addView(tvPrice)
            card.addView(btnAction)
            gridStudiconCatalog.addView(card)
        }
    }

    private fun showActivateProDialog() {
        AlertDialog.Builder(this)
            .setTitle("Aktifkan Purrfect Pass (Pro)")
            .setMessage("Buka semua 6 Studicon eksklusif selamanya dengan Purrfect Pass (Simulasi In-App Rp 29.000)?")
            .setPositiveButton("Aktifkan Pro") { _, _ ->
                StudyRepository.activatePurrfectPass(this)
                Toast.makeText(this, "Purrfect Pass (Pro) aktif! Semua Studicon terbuka.", Toast.LENGTH_SHORT).show()
                renderStore()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun showBuyStudiconDialog(item: StudyRepository.StudiconItem) {
        val currentCoins = StudyRepository.getPurrCoins(this)
        if (item.isProExclusive) {
            AlertDialog.Builder(this)
                .setTitle("Beli ${item.name}")
                .setMessage("${item.name} termasuk item Pro Exclusive.\nBeli satuan (${item.idrPriceLabel}) atau aktifkan Purrfect Pass (Pro)?")
                .setPositiveButton("Beli ${item.idrPriceLabel}") { _, _ ->
                    StudyRepository.unlockStudicon(this, item.key)
                    Toast.makeText(this, "Pembelian berhasil! ${item.name} kini Owned.", Toast.LENGTH_SHORT).show()
                    renderStore()
                }
                .setNeutralButton("Aktifkan Pro") { _, _ ->
                    showActivateProDialog()
                }
                .setNegativeButton("Batal", null)
                .show()
        } else {
            AlertDialog.Builder(this)
                .setTitle("Beli ${item.name}")
                .setMessage("Saldo kamu: $currentCoins PurrCoins.\nHarga: ${item.coinPrice} Coins atau ${item.idrPriceLabel}.")
                .setPositiveButton("Bayar ${item.coinPrice} Coins") { _, _ ->
                    if (StudyRepository.spendPurrCoins(this, item.coinPrice)) {
                        StudyRepository.unlockStudicon(this, item.key)
                        Toast.makeText(this, "${item.name} dibeli! Klik Equip untuk memakai.", Toast.LENGTH_SHORT).show()
                        renderStore()
                    } else {
                        Toast.makeText(this, "PurrCoins tidak cukup! Gunakan pembelian ${item.idrPriceLabel}.", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNeutralButton("Beli ${item.idrPriceLabel}") { _, _ ->
                    StudyRepository.unlockStudicon(this, item.key)
                    Toast.makeText(this, "Pembelian in-app berhasil! ${item.name} kini Owned.", Toast.LENGTH_SHORT).show()
                    renderStore()
                }
                .setNegativeButton("Batal", null)
                .show()
        }
    }
}
