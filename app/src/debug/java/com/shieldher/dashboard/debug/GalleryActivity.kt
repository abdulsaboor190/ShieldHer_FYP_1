package com.shieldher.dashboard.debug

import android.os.Bundle
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.shieldher.R
import com.shieldher.dashboard.common.BadgeStatus
import com.shieldher.dashboard.common.ConfirmDialog
import com.shieldher.dashboard.common.EmptyStateView
import com.shieldher.dashboard.common.ErrorStateView
import com.shieldher.dashboard.common.EvidenceCardModel
import com.shieldher.dashboard.common.EvidenceCardView
import com.shieldher.dashboard.common.SectionHeader
import com.shieldher.dashboard.common.StatusBadge

class GalleryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val rootScrollView = ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
        }

        val container = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
            orientation = LinearLayout.VERTICAL
        }

        // Three panels on 3 different surface tokens:
        // Panel 1: @color/sh_card (White)
        // Panel 2: @color/sh_background (Off-white)
        // Panel 3: @color/sh_surface_light (Pale mint)
        container.addView(createPanel("Surface 1: sh_card (White)", R.color.sh_card))
        container.addView(createPanel("Surface 2: sh_background (Off-White)", R.color.sh_background))
        container.addView(createPanel("Surface 3: sh_surface_light (Pale Mint)", R.color.sh_surface_light))

        rootScrollView.addView(container)
        setContentView(rootScrollView)
    }

    private fun createPanel(title: String, backgroundColorRes: Int): LinearLayout {
        val panel = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(ContextCompat.getColor(context, backgroundColorRes))
            val pad = resources.getDimensionPixelSize(R.dimen.sh_space_16)
            setPadding(pad, pad, pad, pad)
        }

        // Panel header
        val header = TextView(this).apply {
            text = title
            setTextAppearance(R.style.ShieldHer_TextAppearance_Title)
            val padV = resources.getDimensionPixelSize(R.dimen.sh_space_8)
            setPadding(0, padV, 0, padV)
        }
        panel.addView(header)

        // 1. Six StatusBadge states
        val badgeHeader = SectionHeader(this).apply { text = "Status Badges" }
        panel.addView(badgeHeader)

        for (status in BadgeStatus.entries) {
            val badge = StatusBadge(this).apply {
                layoutParams = LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply {
                    topMargin = resources.getDimensionPixelSize(R.dimen.sh_space_4)
                    bottomMargin = resources.getDimensionPixelSize(R.dimen.sh_space_4)
                }
                setStatus(status)
            }
            panel.addView(badge)
        }

        // 2. Three EvidenceCards with synthetic data
        val cardHeader = SectionHeader(this).apply { text = "Evidence Cards" }
        panel.addView(cardHeader)

        val card1 = EvidenceCardView(this).apply {
            layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
                bottomMargin = resources.getDimensionPixelSize(R.dimen.sh_space_12)
            }
            bind(EvidenceCardModel("Example item 1: Chat screenshot", "Received: 16 Nov 2026, 10:00", BadgeStatus.UNCHANGED))
        }
        val card2 = EvidenceCardView(this).apply {
            layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
                bottomMargin = resources.getDimensionPixelSize(R.dimen.sh_space_12)
            }
            bind(EvidenceCardModel("Example item 2: Voice message audio", "Received: 16 Nov 2026, 11:30", BadgeStatus.CHANGED))
        }
        val card3 = EvidenceCardView(this).apply {
            layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
                bottomMargin = resources.getDimensionPixelSize(R.dimen.sh_space_12)
            }
            bind(EvidenceCardModel("Example item 3: Exported report", "Received: 16 Nov 2026, 12:45", BadgeStatus.MISSING))
        }
        panel.addView(card1)
        panel.addView(card2)
        panel.addView(card3)

        // 3. SectionHeader component demo
        val sectionComponentDemo = SectionHeader(this).apply { text = "Section Header Component" }
        panel.addView(sectionComponentDemo)

        // 4. EmptyStateView & ErrorStateView
        val emptyState = EmptyStateView(this).apply {
            layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
            setContent(
                icon = R.drawable.ic_nav_evidence,
                message = R.string.empty_generic,
                actionText = R.string.nav_home,
                onAction = {}
            )
        }
        val errorState = ErrorStateView(this).apply {
            layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
            setContent(
                message = R.string.error_generic,
                canRetry = true,
                onRetry = {}
            )
        }
        panel.addView(emptyState)
        panel.addView(errorState)

        // 5. CircularProgressIndicator
        val loadingHeader = SectionHeader(this).apply { text = "Loading Indicator" }
        panel.addView(loadingHeader)

        val progressContext = ContextThemeWrapper(this, R.style.ShieldHer_Widget_LoadingIndicator)
        val progress = CircularProgressIndicator(progressContext).apply {
            layoutParams = LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply {
                val pad = resources.getDimensionPixelSize(R.dimen.sh_space_8)
                setPadding(pad, pad, pad, pad)
            }
            isIndeterminate = true
        }
        panel.addView(progress)

        // 6. Buttons
        val buttonsHeader = SectionHeader(this).apply { text = "Buttons" }
        panel.addView(buttonsHeader)

        val btnPrimary = MaterialButton(ContextThemeWrapper(this, R.style.ShieldHer_Button_Primary), null, 0).apply {
            text = "Primary Button"
            layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
                bottomMargin = resources.getDimensionPixelSize(R.dimen.sh_space_8)
            }
        }
        val btnSecondary = MaterialButton(ContextThemeWrapper(this, R.style.ShieldHer_Button_Secondary), null, 0).apply {
            text = "Secondary Button"
            layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
                bottomMargin = resources.getDimensionPixelSize(R.dimen.sh_space_8)
            }
        }
        val btnDestructive = MaterialButton(ContextThemeWrapper(this, R.style.ShieldHer_Button_Destructive), null, 0).apply {
            text = "Destructive Button"
            layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
                bottomMargin = resources.getDimensionPixelSize(R.dimen.sh_space_8)
            }
        }
        val btnShowDialog = MaterialButton(ContextThemeWrapper(this, R.style.ShieldHer_Button_Primary), null, 0).apply {
            text = "Show delete dialog"
            layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
                bottomMargin = resources.getDimensionPixelSize(R.dimen.sh_space_8)
            }
            setOnClickListener {
                ConfirmDialog.show(
                    context = this@GalleryActivity,
                    title = R.string.dialog_delete_title,
                    message = R.string.dialog_delete_message,
                    confirmText = R.string.common_delete,
                    destructive = true,
                    onConfirm = {}
                )
            }
        }
        panel.addView(btnPrimary)
        panel.addView(btnSecondary)
        panel.addView(btnDestructive)
        panel.addView(btnShowDialog)

        // 7. Typography samples
        val typoHeader = SectionHeader(this).apply { text = "Typography Scale" }
        panel.addView(typoHeader)

        val styles = listOf(
            "Title (22sp bold)" to R.style.ShieldHer_TextAppearance_Title,
            "Heading (18sp bold)" to R.style.ShieldHer_TextAppearance_Heading,
            "Body (16sp)" to R.style.ShieldHer_TextAppearance_Body,
            "BodySmall (14sp)" to R.style.ShieldHer_TextAppearance_BodySmall,
            "Caption (12sp)" to R.style.ShieldHer_TextAppearance_Caption
        )

        for ((label, styleRes) in styles) {
            val tvMain = TextView(this).apply {
                text = "$label - Main text"
                setTextAppearance(styleRes)
                setTextColor(ContextCompat.getColor(context, R.color.sh_text))
            }
            val tvSec = TextView(this).apply {
                text = "$label - Secondary text"
                setTextAppearance(styleRes)
                setTextColor(ContextCompat.getColor(context, R.color.sh_text_secondary))
                setPadding(0, 0, 0, resources.getDimensionPixelSize(R.dimen.sh_space_8))
            }
            panel.addView(tvMain)
            panel.addView(tvSec)
        }

        return panel
    }
}
