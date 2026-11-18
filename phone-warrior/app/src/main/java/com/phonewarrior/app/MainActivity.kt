package com.phonewarrior.app

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.InputType
import android.text.TextUtils
import android.util.Base64
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.security.MessageDigest
import java.security.SecureRandom
import java.text.DateFormat
import java.util.Date
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import org.json.JSONArray
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    private val gold = 0xFFF0C878.toInt()
    private val ink = 0xFFF3EFFF.toInt()
    private val muted = 0xFFA79DD0.toInt()
    private val panelC = 0xFF201A48.toInt()
    private val edgeC = 0xFF332A63.toInt()
    private val bgC = 0xFF0E0B1D.toInt()
    private val red = 0xFFFF5A6E.toInt()
    private val amber = 0xFFFFB021.toInt()
    private val green = 0xFF8EE89A.toInt()

    private lateinit var btnScanTab: Button
    private lateinit var btnCleanTab: Button
    private lateinit var btnSafetyTab: Button
    private lateinit var btnPhoneTab: Button
    private lateinit var btnVaultTab: Button
    private lateinit var tabScan: LinearLayout
    private lateinit var tabClean: LinearLayout
    private lateinit var tabSafety: LinearLayout
    private lateinit var tabPhone: LinearLayout
    private lateinit var tabVault: LinearLayout
    private lateinit var vaultBox: LinearLayout
    private var vaultPinBuffer = ""
    private var vaultPinStage = ""
    private var vaultOpenKey: SecretKeySpec? = null
    private var vaultMsg = ""
    private lateinit var scanBtn: Button
    private lateinit var scanStatus: TextView
    private lateinit var resultsBox: LinearLayout
    private lateinit var disguiseBtn: Button
    private lateinit var disguiseStatus: TextView
    private lateinit var cleanList: LinearLayout

    private val prefs: SharedPreferences by lazy { getSharedPreferences("pw_state", Context.MODE_PRIVATE) }

    private fun dp(v: Int): Int =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v.toFloat(), resources.displayMetrics).toInt()

    private fun panelBg(edge: Int = edgeC): GradientDrawable {
        val d = GradientDrawable()
        d.setColor(panelC)
        d.cornerRadius = dp(14).toFloat()
        d.setStroke(dp(1), edge)
        return d
    }

    private fun tv(text: String, size: Int, color: Int, bold: Boolean = false): TextView {
        val t = TextView(this)
        t.text = text
        t.textSize = size.toFloat()
        t.setTextColor(color)
        if (bold) t.setTypeface(null, Typeface.BOLD)
        t.setLineSpacing(dp(2).toFloat(), 1.15f)
        return t
    }

    private fun card(title: String, body: String, edge: Int = edgeC, titleColor: Int = gold): LinearLayout {
        val c = LinearLayout(this)
        c.orientation = LinearLayout.VERTICAL
        c.background = panelBg(edge)
        c.setPadding(dp(14), dp(12), dp(14), dp(12))
        if (title.isNotEmpty()) {
            val t = tv(title, 15, titleColor, true)
            t.setPadding(0, 0, 0, dp(6))
            c.addView(t)
        }
        val b = tv(body, 14, ink)
        c.addView(b)
        return c
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = ScrollView(this)
        root.setBackgroundColor(bgC)

        val col = LinearLayout(this)
        col.orientation = LinearLayout.VERTICAL
        col.setPadding(dp(18), dp(18), dp(18), dp(40))
        root.addView(col, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))

        // If the app crashed last time, show the saved report so it can be copied out
        val crashFile = File(filesDir, "crash.log")
        if (crashFile.exists()) {
            val report = crashFile.readText().take(4000)
            col.addView(card(
                "\ud83d\udea8 LAST CRASH REPORT",
                report,
                red, red
            ).also { it.setPadding(dp(14), dp(12), dp(14), dp(12)) },
                LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                    .also { it.bottomMargin = dp(10) })
            val copy = vaultBtn("COPY REPORT", gold, gold)
            copy.setOnClickListener {
                try {
                    val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    cm.setPrimaryClip(ClipData.newPlainText("crash", crashFile.readText()))
                    copy.text = "Copied!"
                } catch (e: Exception) {
                    copy.text = "Copy failed: " + e.message
                }
            }
            col.addView(copy, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                .also { it.bottomMargin = dp(8) })
            val clear = vaultBtn("I'VE COPIED IT — CLEAR THIS BOX", panelC, edgeC, gold)
            clear.setOnClickListener {
                crashFile.delete()
                recreate()
            }
            col.addView(clear, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                .also { it.bottomMargin = dp(10) })
        }

        val brand = tv("🛡️ PHONE WARRIOR", 14, gold, true)
        brand.letterSpacing = 0.18f
        brand.gravity = Gravity.CENTER
        col.addView(brand)
        val byline = tv("by Redeemed Warrior", 11, muted)
        byline.gravity = Gravity.CENTER
        byline.setPadding(0, dp(2), 0, dp(4))
        col.addView(byline)

        val title = tv("Kick them out of your phone", 25, ink, true)
        title.gravity = Gravity.CENTER
        title.setPadding(0, dp(6), 0, dp(2))
        col.addView(title)

        val subtitle = tv("Stalkerware & spy-app scanner. Runs 100% on your phone — this app has NO internet permission, so nothing it finds can ever be sent anywhere.", 13, muted)
        subtitle.gravity = Gravity.CENTER
        subtitle.setPadding(0, 0, 0, dp(12))
        col.addView(subtitle)

        val tabs = LinearLayout(this)
        tabs.orientation = LinearLayout.HORIZONTAL
        btnScanTab = tabBtn("🔍 Scan")
        btnCleanTab = tabBtn("🧹 Clean")
        btnVaultTab = tabBtn("🔒 Vault")
        btnSafetyTab = tabBtn("🛟 Safety")
        btnPhoneTab = tabBtn("📱 iPhone")
        val tp = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        tp.setMargins(dp(1), 0, dp(1), 0)
        tabs.addView(btnScanTab, tp)
        tabs.addView(btnCleanTab, tp)
        tabs.addView(btnVaultTab, tp)
        tabs.addView(btnSafetyTab, tp)
        tabs.addView(btnPhoneTab, tp)
        col.addView(tabs)

        btnScanTab.setOnClickListener { showTab(0) }
        btnCleanTab.setOnClickListener { showTab(1) }
        btnVaultTab.setOnClickListener { showTab(2) }
        btnSafetyTab.setOnClickListener { showTab(3) }
        btnPhoneTab.setOnClickListener { showTab(4) }

        tabScan = buildScanTab()
        tabClean = buildCleanTab()
        tabVault = buildVaultTab()
        tabSafety = buildSafetyTab()
        tabPhone = buildIphoneTab()
        col.addView(tabScan)
        col.addView(tabClean)
        col.addView(tabVault)
        col.addView(tabSafety)
        col.addView(tabPhone)
        showTab(0)

        setContentView(root)
    }

    private fun tabBtn(label: String): Button {
        val b = Button(this, null, 0)
        b.text = label
        b.textSize = 12f
        b.setTextColor(muted)
        b.background = panelBg()
        b.setPadding(dp(2), dp(10), dp(2), dp(10))
        return b
    }

    private fun showTab(which: Int) {
        tabScan.visibility = if (which == 0) LinearLayout.VISIBLE else LinearLayout.GONE
        tabClean.visibility = if (which == 1) LinearLayout.VISIBLE else LinearLayout.GONE
        tabVault.visibility = if (which == 2) LinearLayout.VISIBLE else LinearLayout.GONE
        tabSafety.visibility = if (which == 3) LinearLayout.VISIBLE else LinearLayout.GONE
        tabPhone.visibility = if (which == 4) LinearLayout.VISIBLE else LinearLayout.GONE
        if (which == 2) renderVault()
        val entries: List<Pair<Button, Boolean>> = listOf(
            btnScanTab to (which == 0),
            btnCleanTab to (which == 1),
            btnVaultTab to (which == 2),
            btnSafetyTab to (which == 3),
            btnPhoneTab to (which == 4)
        )
        entries.forEach { (b, hot) ->
            b.setTextColor(if (hot) gold else muted)
            b.background = panelBg(if (hot) gold else edgeC)
        }
    }

    /* ============================== SCAN TAB ============================== */

    private fun buildScanTab(): LinearLayout {
        val box = LinearLayout(this)
        box.orientation = LinearLayout.VERTICAL
        box.setPadding(0, dp(14), 0, 0)

        scanBtn = Button(this, null, 0)
        scanBtn.text = "🔍 SCAN MY PHONE NOW"
        scanBtn.textSize = 16f
        scanBtn.setTypeface(null, Typeface.BOLD)
        scanBtn.setTextColor(0xFF241A05.toInt())
        scanBtn.setPadding(0, dp(14), 0, dp(14))
        box.addView(scanBtn, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).also { it.topMargin = dp(4) })

        scanStatus = tv("", 13, muted)
        scanStatus.setPadding(dp(4), dp(10), dp(4), 0)
        box.addView(scanStatus)

        resultsBox = LinearLayout(this)
        resultsBox.orientation = LinearLayout.VERTICAL
        box.addView(resultsBox)

        scanBtn.setOnClickListener { runScan() }
        return box
    }

    private fun runScan() {
        scanBtn.isEnabled = false
        scanBtn.alpha = 0.6f
        resultsBox.removeAllViews()
        scanStatus.text = "Reading app list, accessibility services, device admins and notification access…"

        Thread {
            val findings = try {
                scan()
            } catch (e: Exception) {
                runOnUiThread {
                    scanStatus.text = "Scan error: ${e.message}. Please try again."
                    scanBtn.isEnabled = true
                    scanBtn.alpha = 1f
                }
                return@Thread
            }
            runOnUiThread { renderResults(findings) }
        }.start()
    }

    private fun installerOf(pkg: String): String? = try {
        if (Build.VERSION.SDK_INT >= 30) {
            packageManager.getInstallSourceInfo(pkg).installingPackageName
        } else {
            @Suppress("DEPRECATION")
            packageManager.getInstallerPackageName(pkg)
        }
    } catch (e: Exception) {
        null
    }

    private fun hasPerm(pkg: String, perm: String): Boolean = try {
        packageManager.checkPermission(perm, pkg) == PackageManager.PERMISSION_GRANTED
    } catch (e: Exception) {
        false
    }

    private fun scan(): List<Finding> {
        val pm = packageManager
        val findings = mutableListOf<Finding>()
        val all = try {
            pm.getInstalledPackages(PackageManager.GET_PERMISSIONS)
        } catch (e: Exception) {
            emptyList<PackageInfo>()
        }
        val infoByPkg = all.associateBy { it.packageName }

        val accPkgs = mutableSetOf<String>()
        try {
            val am = getSystemService(Context.ACCESSIBILITY_SERVICE) as android.view.accessibility.AccessibilityManager
            val enabled = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
            for (s in enabled) {
                val id = s.id ?: continue
                accPkgs.add(id.substringBefore("/"))
            }
        } catch (e: Exception) {
        }

        val adminPkgs = mutableSetOf<String>()
        val dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as android.app.admin.DevicePolicyManager
        try {
            val admins = dpm.activeAdmins ?: emptyList()
            for (cn in admins) adminPkgs.add(cn.packageName)
        } catch (e: Exception) {
        }

        val notifPkgs = try {
            (Settings.Secure.getString(contentResolver, "enabled_notification_listeners") ?: "")
                .split(":")
                .map { it.substringBefore("/").trim() }
                .filter { it.isNotBlank() }
                .toSet()
        } catch (e: Exception) {
            emptySet<String>()
        }

        var ownerHit = false
        fun flag(pkg: String, label: String, severity: Int, reason: String) {
            val pi = infoByPkg[pkg]
            val shown = if (label.isNotEmpty()) label else pkg
            val existing = findings.find { it.pkg == pkg }
            if (existing == null) {
                val inst = installerOf(pkg)
                val whenInstalled = if (pi != null) Date(pi.firstInstallTime) else null
                findings.add(Finding(pkg, shown, severity, mutableListOf(reason), inst, whenInstalled))
            } else {
                if (severity > existing.severity) existing.severity = severity
                if (existing.reasons.indexOf(reason) < 0) existing.reasons.add(reason)
            }
        }

        for (pi in all) {
            val pkg = pi.packageName
            if (pkg == packageName) continue
            val app = pi.applicationInfo ?: continue
            val isSystem = (app.flags and ApplicationInfo.FLAG_SYSTEM) != 0
            if (isSystem) continue

            val label = try { app.loadLabel(pm).toString() } catch (e: Exception) { pkg }
            val low = (label + " " + pkg).lowercase()
            val perms = pi.requestedPermissions?.toSet() ?: emptySet()
            val hasLauncher = try { pm.getLaunchIntentForPackage(pkg) != null } catch (e: Exception) { false }
            val installedFromPlay = installerOf(pkg) == "com.android.vending"

            val hasLoc = perms.any { Perms.LOCATION.contains(it) }
            val hasMic = perms.contains("android.permission.RECORD_AUDIO")
            val hasCam = perms.contains("android.permission.CAMERA")
            val hasSms = perms.any { Perms.SMS.contains(it) }
            val hasContacts = perms.contains("android.permission.READ_CONTACTS")
            val hasCallLog = perms.contains("android.permission.READ_CALL_LOG")
            val sensitive = hasLoc || hasMic || hasCam || hasSms || hasContacts || hasCallLog

            if (ThreatDb.isKnownStalkerware(pkg, label)) {
                flag(pkg, label, 3, "Matches a known stalkerware / spy app signature")
                continue
            }

            if (ThreatDb.hasSevereWord(low)) {
                flag(pkg, label, 3, "Name suggests a spy / keylogger app — confirm you installed this")
            } else if (ThreatDb.hasSoftWord(low) && !installedFromPlay) {
                flag(pkg, label, 2, "Monitoring-style name AND not installed from Play Store")
            } else if (ThreatDb.hasSoftWord(low)) {
                flag(pkg, label, 1, "Monitoring-style name — double-check you installed this yourself")
            }

            if (accPkgs.contains(pkg) && !ThreatDb.isSystemPkg(pkg)) {
                if (sensitive) flag(pkg, label, 3, "Accessibility service + sensitive permissions — can read everything on screen and know your location")
                else flag(pkg, label, 2, "Runs as an Accessibility service — can read screen text and keystrokes")
            }

            if (adminPkgs.contains(pkg)) {
                flag(pkg, label, 2, "Has Device Admin rights — this blocks normal uninstall")
            }

            try {
                if (dpm.isDeviceOwnerApp(pkg) || dpm.isProfileOwnerApp(pkg)) {
                    ownerHit = true
                    flag(pkg, label, 3, "Manages the WHOLE device (device/profile owner — corporate or controller-level access)")
                }
            } catch (e: Exception) {
            }

            if (notifPkgs.contains(pkg) && !ThreatDb.isSystemPkg(pkg)) {
                flag(pkg, label, 1, "Can read your notifications (every text preview passing through)")
            }

            val grantedLoc = hasPerm(pkg, "android.permission.ACCESS_FINE_LOCATION") || hasPerm(pkg, "android.permission.ACCESS_COARSE_LOCATION")
            val grantedMic = hasPerm(pkg, "android.permission.RECORD_AUDIO")
            val grantedCam = hasPerm(pkg, "android.permission.CAMERA")
            val grantedSms = perms.any { Perms.SMS.contains(it) && hasPerm(pkg, it) }

            if (grantedLoc && (grantedMic || grantedCam) && !hasLauncher) {
                flag(pkg, label, 3, "HIDDEN (no home-screen icon) + live location + microphone/camera access")
            } else if (grantedLoc && (grantedMic || grantedCam) && !installedFromPlay) {
                flag(pkg, label, 2, "Live location + microphone/camera, installed outside Play Store")
            }

            if (grantedSms && grantedLoc) {
                flag(pkg, label, 2, "Can see your location AND your text messages")
            }

            if (perms.contains("android.permission.REQUEST_INSTALL_PACKAGES") && !hasLauncher && !installedFromPlay) {
                flag(pkg, label, 2, "Can silently install other apps, has no icon, and came from outside Play Store")
            }

            val sigCount = (if (grantedLoc) 1 else 0) + (if (grantedMic || grantedCam) 1 else 0) + (if (grantedSms || perms.contains("android.permission.READ_CONTACTS") || perms.contains("android.permission.READ_CALL_LOG")) 1 else 0)
            if (sigCount >= 3 && !installedFromPlay) {
                flag(pkg, label, 2, "Rare combination of tracking permissions, installed outside Play Store")
            }
        }

        if (ownerHit) {
            flag("android.device.owner.state", "", 3, "A device-owner app was found — this is 'company device' level control. If nobody set this up for work, remove it.")
        }

        return findings.sortedByDescending { it.severity }
    }

    private fun renderResults(findings: List<Finding>) {
        scanBtn.isEnabled = true
        scanBtn.alpha = 1f
        val crit = findings.count { it.severity >= 3 }
        val high = findings.count { it.severity == 2 }
        val low = findings.count { it.severity == 1 }
        scanStatus.text = "Scan complete · " + findings.size + " finding" + (if (findings.size == 1) "" else "s") + " · " + crit + " critical, " + high + " suspicious, " + low + " to review"

        resultsBox.removeAllViews()

        val banner: LinearLayout
        if (crit > 0) {
            banner = card("🔴 CRITICAL RISK", "Signs of surveillance software were found. Follow the 🛟 Safety tab: secure your accounts from a DIFFERENT device before touching anything here.", red, red)
        } else if (high > 0) {
            banner = card("🟠 HIGH RISK", "Suspicious apps found. Tap each one below — if you did not install it yourself, treat it as hostile.", amber, amber)
        } else if (low > 0) {
            banner = card("🟡 REVIEW", "Some apps need a manual look (tap them below). Nothing screams stalkerware, but trust your gut.", gold)
        } else {
            banner = card("🟢 CLEAR", "No stalkerware signals found. Remember: a clean scan is not a guarantee — the 🧹 Clean and 🛟 Safety tabs still matter, especially if someone ever had unlocked physical access to this phone.", green, green)
        }
        banner.setPadding(dp(14), dp(12), dp(14), dp(12))
        val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        lp.setMargins(0, dp(12), 0, 0)
        resultsBox.addView(banner, lp)

        for (f in findings) {
            val sevIcon = if (f.severity >= 3) "🔴 " else if (f.severity == 2) "🟠 " else "🟡 "
            val head = sevIcon + f.label
            val body = f.reasons.joinToString("\n") { "• " + it } +
                "\n→ tap for details"
            val item = card(head, body, if (f.severity >= 3) red else if (f.severity == 2) amber else edgeC, ink)
            item.setPadding(dp(14), dp(12), dp(14), dp(12))
            item.setOnClickListener { showFindingDialog(f) }
            val clp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            clp.setMargins(0, dp(8), 0, 0)
            resultsBox.addView(item, clp)
        }

        if (findings.isNotEmpty()) {
            val share = Button(this, null, 0)
            share.text = "📤 Share this report (with someone you trust)"
            share.textSize = 13f
            share.setTextColor(gold)
            share.background = panelBg(gold)
            share.setPadding(dp(8), dp(12), dp(8), dp(12))
            share.setOnClickListener { shareReport(findings) }
            val slp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            slp.setMargins(0, dp(12), 0, 0)
            resultsBox.addView(share, slp)
        }
    }

    private fun showFindingDialog(f: Finding) {
        val sb = StringBuilder()
        sb.append("App: ").append(f.label).append("\n")
        sb.append("Package: ").append(f.pkg).append("\n")
        val inst = when (f.installer) {
            "com.android.vending" -> "Google Play Store ✅"
            null, "" -> "UNKNOWN / sideloaded (installed without any store)"
            else -> "Installed by: " + f.installer
        }
        sb.append(inst).append("\n")
        if (f.installedOn != null) sb.append("Installed: ").append(DateFormat.getDateTimeInstance().format(f.installedOn)).append("\n")
        sb.append("\nWhy flagged:\n")
        for (r in f.reasons) sb.append("• ").append(r).append("\n")
        sb.append("\nWhat to do:\n")
        sb.append("• Screenshot this first (evidence).\n")
        sb.append("• Uninstall: press-and-hold the app icon → Uninstall, or Settings → Apps.\n")
        sb.append("• If Uninstall is greyed out: Settings → Security → Device admin apps → turn it off for this app, then uninstall.\n")
        sb.append("• If you did not install this app yourself, do NOT just confront — read the 🛟 Safety tab.")
        AlertDialog.Builder(this)
            .setTitle(f.label)
            .setMessage(sb.toString())
            .setPositiveButton("Close", null)
            .setNeutralButton("Open app info") { _, _ ->
                try {
                    startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + f.pkg)))
                } catch (e: Exception) {
                }
            }
            .show()
    }

    private fun shareReport(findings: List<Finding>): Boolean {
        val sb = StringBuilder()
        sb.append("🛡️ Phone Warrior scan report — ").append(DateFormat.getDateTimeInstance().format(Date())).append("\n\n")
        for (f in findings) {
            sb.append(if (f.severity >= 3) "🔴" else if (f.severity == 2) "🟠" else "🟡").append(" ").append(f.label)
                .append(" (").append(f.pkg).append(")\n")
            for (r in f.reasons) sb.append("   - ").append(r).append("\n")
            sb.append("\n")
        }
        sb.append("Scanned locally by Phone Warrior (no data left the phone).")
        val send = Intent(Intent.ACTION_SEND)
        send.type = "text/plain"
        send.putExtra(Intent.EXTRA_TEXT, sb.toString())
        try {
            startActivity(Intent.createChooser(send, "Send report to"))
            return true
        } catch (e: Exception) {
            return false
        }
    }

    /* ============================== CLEAN TAB ============================== */

    private fun buildCleanTab(): LinearLayout {
        val box = LinearLayout(this)
        box.orientation = LinearLayout.VERTICAL
        box.setPadding(0, dp(14), 0, 0)

        box.addView(card(
            "📝 STOP — EVIDENCE FIRST",
            "Before you delete ANYTHING: screenshot the abusive messages and save them somewhere safe (email them to a NEW secret account from a trusted person's device, or write them in a personal journal). Deleted messages can be permanent — and police and protective orders need them.",
            amber, amber
        ).also { it.setPadding(dp(14), dp(12), dp(14), dp(12)) }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).also { it.setMargins(0, dp(10), 0, 0) })

        box.addView(card(
            "⚖️ What deleting CAN and CAN'T do",
            "CAN erase: your trails on this device — search history, Maps timeline, browser history, notification history, keyboard history, chat history, previews.\n\nCAN'T undo: if stalkerware was installed, copies of texts/locations may already be on THEIR server. Deleting here does not pull back what they already have. That is why the scan + Safety steps matter too.",
            edgeC
        ).also { it.setPadding(dp(14), dp(12), dp(14), dp(12)) }, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).also { it.setMargins(0, dp(10), 0, 0) })

        val disTitle = tv("🥸 DISGUISE MODE", 15, gold, true)
        disTitle.setPadding(dp(16), dp(16), dp(4), dp(4))
        box.addView(disTitle)
        disguiseStatus = tv("", 13, muted)
        disguiseStatus.setPadding(dp(16), 0, dp(16), dp(8))
        box.addView(disguiseStatus)

        disguiseBtn = Button(this, null, 0)
        disguiseBtn.textSize = 14f
        disguiseBtn.setTypeface(null, Typeface.BOLD)
        disguiseBtn.setPadding(dp(8), dp(12), dp(8), dp(12))
        val dlp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dlp.setMargins(dp(14), 0, dp(14), dp(6))
        box.addView(disguiseBtn, dlp)
        disguiseBtn.setOnClickListener { toggleDisguise() }
        refreshDisguise()

        val clTitle = tv("🧹 ERASE YOUR TRACES — tap each when done", 15, gold, true)
        clTitle.setPadding(dp(16), dp(16), dp(4), dp(4))
        box.addView(clTitle)

        cleanList = LinearLayout(this)
        cleanList.orientation = LinearLayout.VERTICAL
        box.addView(cleanList)
        buildCleanChecklist()

        return box
    }

    private val cleanSteps = listOf(
        "Lock screen previews OFF" to "Nothing should pop up on your lock screen. Settings → Notifications → 'On lock screen' → 'Don't show notifications at all' (or hide sensitive content).",
        "Notification history OFF" to "Stalkerware and nosy people love this. Settings → Notifications → Notification history → OFF.",
        "Google search activity deleted" to "Google app → your picture (top right) → 'Search history' → delete all → settings → auto-delete → 3 months.",
        "Maps Timeline off + deleted" to "Google Maps → your picture → 'Your Timeline' → delete all history, then pause Timeline. Nobody needs a diary of everywhere you go.",
        "Browser history & data cleared" to "Chrome → ⋮ → History → Clear browsing data. Check 'Passwords' only if you're signed out of important things first — better: change passwords (Safety tab) and let them save fresh.",
        "Chat histories handled" to "After saving evidence: delete the abusive chats. Turn on 'Disappearing messages' for him. Archive the chat so it's out of sight. Settings → chats → hide notification text.",
        "Keyboard history cleared" to "Your keyboard remembers everything you typed. Gboard → Settings → 'Clear dictionary' / 'Delete learned words' and clipboard history.",
        "Recents screen cleared" to "Close every app in the Recents/multitasking view — no previews of what you've been looking at.",
        "Photo trash emptied" to "Photos → Library → Trash → Empty. Also Files app → Downloads → clear screenshots you don't need any more."
    )

    private fun buildCleanChecklist() {
        cleanList.removeAllViews()
        for (i in cleanSteps.indices) {
            val step = cleanSteps[i]
            val done = prefs.getBoolean("clean_" + i, false)
            val row = LinearLayout(this)
            row.orientation = LinearLayout.VERTICAL
            row.background = panelBg(if (done) green else edgeC)
            row.setPadding(dp(14), dp(10), dp(14), dp(10))
            val head = tv((if (done) "☑️ " else "⬜️ ") + step.first, 14, if (done) green else ink, true)
            row.addView(head)
            val body = tv(step.second, 12, muted)
            body.setPadding(0, dp(4), 0, 0)
            row.addView(body)
            row.setOnClickListener {
                val now = !prefs.getBoolean("clean_" + i, false)
                prefs.edit().putBoolean("clean_" + i, now).apply()
                buildCleanChecklist()
            }
            val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            lp.setMargins(0, dp(8), 0, 0)
            cleanList.addView(row, lp)
        }
    }

    private fun disguised(): Boolean {
        val alias = ComponentName(this, "com.phonewarrior.app.LauncherNotes")
        return packageManager.getComponentEnabledSetting(alias) == PackageManager.COMPONENT_ENABLED_STATE_ENABLED
    }

    private fun refreshDisguise() {
        if (disguised()) {
            disguiseBtn.text = "👁️ Stop disguising (show 🛡️ Phone Warrior icon again)"
            disguiseBtn.setTextColor(gold)
            disguiseBtn.background = panelBg(gold)
            disguiseStatus.text = "Disguise is ON — the app is called 'Notes' on your home screen with a boring notes icon. Open it from there anytime."
        } else {
            disguiseBtn.text = "🥸 Turn on disguise (home screen shows it as 'Notes')"
            disguiseBtn.setTextColor(ink)
            disguiseBtn.background = panelBg(edgeC)
            disguiseStatus.text = "Off — the app shows as 🛡️ Phone Warrior. Disguise mode changes the icon and name on your home screen to a boring 'Notes' app. It may take a few seconds to update."
        }
    }

    private fun toggleDisguise() {
        val war = ComponentName(this, "com.phonewarrior.app.LauncherWarrior")
        val notes = ComponentName(this, "com.phonewarrior.app.LauncherNotes")
        val turnOn = !disguised()
        try {
            packageManager.setComponentEnabledSetting(
                war,
                if (turnOn) PackageManager.COMPONENT_ENABLED_STATE_DISABLED else PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
            )
            packageManager.setComponentEnabledSetting(
                notes,
                if (turnOn) PackageManager.COMPONENT_ENABLED_STATE_ENABLED else PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP
            )
            refreshDisguise()
        } catch (e: Exception) {
            disguiseStatus.text = "Could not switch disguise: " + e.message
        }
    }

    /* ============================== VAULT TAB ============================== */

    private fun sha256Hex(s: String): String =
        MessageDigest.getInstance("SHA-256").digest(s.toByteArray(Charsets.UTF_8))
            .joinToString("") { String.format("%02x", it) }

    private fun vaultSaltBytes(): ByteArray {
        val s = prefs.getString("vault_salt", null) ?: return ByteArray(0)
        return Base64.decode(s, Base64.NO_WRAP)
    }

    private fun deriveKey(pin: String, salt: ByteArray): SecretKeySpec {
        val spec = PBEKeySpec(pin.toCharArray(), salt, 120000, 256)
        val f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
        return SecretKeySpec(f.generateSecret(spec).encoded, "AES")
    }

    private fun vaultEnc(key: SecretKeySpec, plain: String): String {
        val c = Cipher.getInstance("AES/GCM/NoPadding")
        c.init(Cipher.ENCRYPT_MODE, key)
        val iv = c.iv
        val ct = c.doFinal(plain.toByteArray(Charsets.UTF_8))
        val out = ByteArray(iv.size + ct.size)
        System.arraycopy(iv, 0, out, 0, iv.size)
        System.arraycopy(ct, 0, out, iv.size, ct.size)
        return Base64.encodeToString(out, Base64.NO_WRAP)
    }

    private fun vaultDec(key: SecretKeySpec, blob: String): String? {
        return try {
            val all = Base64.decode(blob, Base64.NO_WRAP)
            val iv = all.copyOfRange(0, 12)
            val ct = all.copyOfRange(12, all.size)
            val c = Cipher.getInstance("AES/GCM/NoPadding")
            c.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(128, iv))
            String(c.doFinal(ct), Charsets.UTF_8)
        } catch (e: Exception) {
            null
        }
    }

    private fun vaultInput(hint: String): EditText {
        val e = EditText(this)
        e.hint = hint
        e.textSize = 15f
        e.setTextColor(ink)
        e.setHintTextColor(muted)
        e.background = panelBg()
        e.setPadding(dp(12), dp(12), dp(12), dp(12))
        return e
    }

    private fun vaultBtn(label: String, bg: Int, edge: Int, fg: Int = 0xFF241A05.toInt()): Button {
        val b = Button(this, null, 0)
        b.text = label
        b.textSize = 13f
        b.setTypeface(null, Typeface.BOLD)
        b.setTextColor(fg)
        val d = GradientDrawable()
        d.setColor(bg)
        d.cornerRadius = dp(12).toFloat()
        d.setStroke(dp(1), edge)
        b.background = d
        b.setPadding(dp(10), dp(12), dp(10), dp(12))
        return b
    }

    private fun buildVaultTab(): LinearLayout {
        val box = LinearLayout(this)
        box.orientation = LinearLayout.VERTICAL
        box.setPadding(0, dp(14), 0, 0)
        vaultBox = LinearLayout(this)
        vaultBox.orientation = LinearLayout.VERTICAL
        box.addView(vaultBox)
        return box
    }

    private fun renderVault() {
        vaultBox.removeAllViews()
        val pad = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        vaultBox.addView(card(
            "\ud83d\udd10 SECRET VAULT",
            "A hidden, encrypted space on this phone. Everything inside is scrambled with your PIN (AES). This app has NO internet permission, so nothing in here can ever be copied out by anyone but you.",
            edgeC
        ).also { it.setPadding(dp(14), dp(12), dp(14), dp(12)) }, pad.also { it.setMargins(0, dp(10), 0, 0) })

        if (vaultMsg.isNotEmpty()) {
            vaultBox.addView(tv(vaultMsg, 13, red).also { it.setPadding(dp(6), dp(8), dp(6), 0) },
                pad.also { it.setMargins(0, dp(6), 0, 0) })
            vaultMsg = ""
        }

        if (prefs.getString("vault_salt", null) == null) {
            vaultSetup(pad)
        } else if (prefs.getString("vault_locked", null) == "1") {
            vaultLockdown(pad)
        } else if (vaultPinStage.isEmpty()) {
            vaultEnter(pad)
        } else {
            vaultContents(pad)
        }
    }

    private fun vaultSetup(pad: LinearLayout.LayoutParams) {
        val p1 = vaultInput("New PIN (4+ digits)")
        p1.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
        val p2 = vaultInput("Repeat the PIN")
        p2.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
        val go = vaultBtn("CREATE VAULT", gold, gold)

        go.setOnClickListener {
            val a = p1.text.toString()
            val b = p2.text.toString()
            if (a.length < 4) {
                vaultMsg = "The PIN needs at least 4 digits. Pick something he has NEVER seen you type — not your birthday, not your phone-unlock code."
                renderVault()
            } else if (a != b) {
                vaultMsg = "The two PINs don't match."
                renderVault()
            } else {
                val alphabet = "ABCDEFGHJKMNPQRSTUVWXYZ23456789"
                val rnd = SecureRandom()
                val chars = CharArray(12) { alphabet[rnd.nextInt(alphabet.length)] }
                val rec = chars.take(4).joinToString("") + "-" + chars.drop(4).take(4).joinToString("") + "-" + chars.drop(8).joinToString("")
                Thread {
                    val salt = ByteArray(16)
                    SecureRandom().nextBytes(salt)
                    val key = deriveKey(a, salt)
                    val data = vaultEnc(key, "[]")
                    val recHash = sha256Hex(rec.replace("-", ""))
                    runOnUiThread {
                        prefs.edit()
                            .putString("vault_salt", Base64.encodeToString(salt, Base64.NO_WRAP))
                            .putString("vault_data", data)
                            .putString("vault_key_hash", recHash)
                            .putString("vault_misses", "0")
                            .putString("vault_locked", "0")
                            .apply()
                        vaultOpenKey = key
                        vaultPinStage = "open"
                        vaultPinBuffer = ""
                        AlertDialog.Builder(this)
                            .setTitle("COPY THIS RECOVERY KEY")
                            .setMessage("Write this down ON PAPER right now and hide it somewhere he would never look:\n\n$rec\n\nThis is the ONLY way to open the vault if the PIN is entered wrong 3 times. It is shown this ONE time and never again. If you lose it and forget the PIN, the vault stays locked forever — nobody can open it, not even us.")
                            .setPositiveButton("I wrote it down") { _, _ -> renderVault() }
                            .setCancelable(false)
                            .show()
                    }
                }.start()
            }
        }

        vaultBox.addView(card(
            "Step 1 — set it up",
            "Pick a PIN nobody could guess, then WRITE DOWN the Recovery Key it gives you. The vault locks itself after 3 wrong PIN tries, and only the Recovery Key opens it again.",
            amber, amber
        ).also { it.setPadding(dp(14), dp(12), dp(14), dp(12)) }, pad.also { it.setMargins(0, dp(8), 0, 0) })
        vaultBox.addView(p1, pad.also { it.setMargins(0, dp(8), 0, 0) })
        vaultBox.addView(p2, pad.also { it.setMargins(0, dp(8), 0, 0) })
        vaultBox.addView(go, pad.also { it.setMargins(0, dp(10), 0, 0) })
    }

    private fun vaultEnter(pad: LinearLayout.LayoutParams) {
        val shown = if (vaultPinBuffer.isEmpty()) "\u2022 \u2022 \u2022 \u2022" else "\u2022 ".repeat(vaultPinBuffer.length).trim()
        val dots = tv(shown, 26, gold, true)
        dots.gravity = Gravity.CENTER
        dots.setPadding(0, dp(4), 0, dp(4))
        vaultBox.addView(dots, pad.also { it.setMargins(0, dp(6), 0, 0) })

        val misses = (prefs.getString("vault_misses", "0") ?: "0").toInt()
        if (misses > 0) {
            val warn = tv("$misses wrong ${if (misses == 1) "try" else "tries"} so far — a 3rd wrong try locks the vault.", 13, amber)
            warn.gravity = Gravity.CENTER
            vaultBox.addView(warn, pad.also { it.setMargins(0, dp(2), 0, 0) })
        }

        val grid = LinearLayout(this)
        grid.orientation = LinearLayout.VERTICAL
        val rows = listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
            listOf("C", "0", "\u232B")
        )
        for (rowKeys in rows) {
            val row = LinearLayout(this)
            row.orientation = LinearLayout.HORIZONTAL
            for (k in rowKeys) {
                val b = Button(this, null, 0)
                b.text = k
                b.textSize = 17f
                b.setTextColor(ink)
                b.background = panelBg()
                b.setPadding(0, dp(10), 0, dp(10))
                val lp = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
                lp.setMargins(dp(4), dp(4), dp(4), dp(4))
                row.addView(b, lp)
                b.setOnClickListener {
                    when (k) {
                        "C" -> { vaultPinBuffer = ""; renderVault() }
                        "\u232B" -> { if (vaultPinBuffer.isNotEmpty()) { vaultPinBuffer = vaultPinBuffer.dropLast(1); renderVault() } }
                        else -> { if (vaultPinBuffer.length < 12) { vaultPinBuffer += k; renderVault() } }
                    }
                }
            }
            grid.addView(row, pad.also { it.setMargins(0, 0, 0, 0) })
        }
        vaultBox.addView(grid)

        val go = vaultBtn("UNLOCK \u2192", gold, gold)
        go.setOnClickListener { tryUnlock() }
        vaultBox.addView(go, pad.also { it.setMargins(0, dp(8), 0, 0) })
    }

    private fun tryUnlock() {
        val pin = vaultPinBuffer
        if (pin.isEmpty()) {
            vaultMsg = "Type your PIN first."
            renderVault()
            return
        }
        Thread {
            val key = deriveKey(pin, vaultSaltBytes())
            val plain = vaultDec(key, prefs.getString("vault_data", "") ?: "")
            runOnUiThread {
                if (plain != null) {
                    prefs.edit().putString("vault_misses", "0").apply()
                    vaultOpenKey = key
                    vaultPinStage = "open"
                    vaultPinBuffer = ""
                    renderVault()
                } else {
                    val misses = (prefs.getString("vault_misses", "0") ?: "0").toInt() + 1
                    prefs.edit().putString("vault_misses", misses.toString()).apply()
                    vaultPinBuffer = ""
                    if (misses >= 3) {
                        prefs.edit().putString("vault_locked", "1").apply()
                    } else {
                        vaultMsg = "Wrong PIN — $misses of 3. On the 3rd wrong try the vault goes on lockdown and only the Recovery Key can open it."
                    }
                    renderVault()
                }
            }
        }.start()
    }

    private fun vaultLockdown(pad: LinearLayout.LayoutParams) {
        vaultPinBuffer = ""
        val input = vaultInput("RECOVERY KEY \u2014 XXXX-XXXX-XXXX")
        val go = vaultBtn("UNLOCK WITH RECOVERY KEY", gold, gold)
        go.setOnClickListener {
            val typed = input.text.toString().replace("-", "").replace(" ", "").uppercase()
            if (typed.isEmpty()) {
                vaultMsg = "Type the Recovery Key you wrote down (the dashes don't matter)."
                renderVault()
                return@setOnClickListener
            }
            Thread {
                val hash = prefs.getString("vault_key_hash", "") ?: ""
                val ok = sha256Hex(typed) == hash
                runOnUiThread {
                    if (ok) {
                        prefs.edit().putString("vault_locked", "0").putString("vault_misses", "0").apply()
                        vaultMsg = ""
                        vaultPinStage = ""
                        renderVault()
                    } else {
                        vaultMsg = "Wrong recovery key. Check the paper — dashes don't matter, letters and numbers do."
                        renderVault()
                    }
                }
            }.start()
        }

        vaultBox.addView(card(
            "\ud83d\udea8 VAULT ON LOCKDOWN",
            "3 wrong tries. The PIN no longer works — on purpose. The only way back in is the Recovery Key that was shown ONCE when the vault was created.\n\nIf you wrote it down and hid it, you're fine — type it below.\n\nIf nobody knows it: the vault stays closed. Not to you on a bad day, not to anyone who takes this phone, not even to us. There is no override. That is the point.",
            red, red
        ).also { it.setPadding(dp(14), dp(12), dp(14), dp(12)) }, pad.also { it.setMargins(0, dp(8), 0, 0) })
        vaultBox.addView(input, pad.also { it.setMargins(0, dp(8), 0, 0) })
        vaultBox.addView(go, pad.also { it.setMargins(0, dp(8), 0, 0) })
    }

    private fun vaultContents(pad: LinearLayout.LayoutParams) {
        val key = vaultOpenKey
        if (key == null) {
            vaultPinStage = ""
            renderVault()
            return
        }
        val plain = vaultDec(key, prefs.getString("vault_data", "") ?: "[]") ?: "[]"
        val arr = try { JSONArray(plain) } catch (e: Exception) { JSONArray() }

        val add = vaultBtn("ADD A SECRET NOTE", gold, gold)
        add.setOnClickListener {
            val boxIn = EditText(this)
            boxIn.hint = "What do you need to keep safe?"
            AlertDialog.Builder(this)
                .setTitle("New secret note")
                .setView(boxIn)
                .setPositiveButton("Save") { _, _ ->
                    val text = boxIn.text.toString().trim()
                    if (text.isNotEmpty()) {
                        val obj = JSONObject()
                        obj.put("t", System.currentTimeMillis())
                        obj.put("s", text)
                        arr.put(obj)
                        prefs.edit().putString("vault_data", vaultEnc(key, arr.toString())).apply()
                        renderVault()
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        val call = vaultBtn("\ud83d\udcde CALL DV HOTLINE \u2014 1-800-799-7233", red, red, ink)
        call.setOnClickListener {
            try { startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:18007997233"))) } catch (e: Exception) {}
        }
        val text = vaultBtn("\ud83d\udcac TEXT START to 88788", red, red, ink)
        text.setOnClickListener {
            try {
                val i = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:88788"))
                i.putExtra("sms_body", "START")
                startActivity(i)
            } catch (e: Exception) {}
        }
        val lock = vaultBtn("\ud83d\udd12 LOCK VAULT NOW", panelC, edgeC, gold)
        lock.setOnClickListener {
            vaultPinStage = ""
            vaultPinBuffer = ""
            vaultOpenKey = null
            renderVault()
        }

        vaultBox.addView(card(
            "\ud83d\udd13 VAULT OPEN",
            "Newest first. Tap a note to delete it. There is NO erase-everything button on purpose — the worst that can happen is lockdown, never losing your information.",
            edgeC
        ).also { it.setPadding(dp(14), dp(12), dp(14), dp(12)) }, pad.also { it.setMargins(0, dp(8), 0, 0) })
        vaultBox.addView(add, pad.also { it.setMargins(0, dp(8), 0, 0) })

        val items = (0 until arr.length()).map { arr.getJSONObject(it) }.sortedByDescending { it.optLong("t") }
        if (items.isEmpty()) {
            vaultBox.addView(tv("Nothing in here yet. Ideas: the new secret email addresses from the Safety tab, hotline case numbers, copies of protective-order paperwork lists, the address of a safe place.", 13, muted)
                .also { it.setPadding(dp(6), dp(4), dp(6), 0) }, pad.also { it.setMargins(0, dp(4), 0, 0) })
        }
        for (obj in items) {
            val whenStr = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(obj.optLong("t")))
            val row = card(whenStr, obj.optString("s"), edgeC)
            row.setOnClickListener {
                AlertDialog.Builder(this)
                    .setTitle("Delete this note?")
                    .setMessage("It cannot be recovered.")
                    .setPositiveButton("Delete") { _, _ ->
                        val t = obj.optLong("t")
                        val s = obj.optString("s")
                        val fresh = try { JSONArray(vaultDec(key, prefs.getString("vault_data", "") ?: "[]") ?: "[]") } catch (e: Exception) { JSONArray() }
                        val keep = JSONArray()
                        for (j in 0 until fresh.length()) {
                            val o = fresh.getJSONObject(j)
                            if (!(o.optLong("t") == t && o.optString("s") == s)) keep.put(o)
                        }
                        prefs.edit().putString("vault_data", vaultEnc(key, keep.toString())).apply()
                        renderVault()
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
            vaultBox.addView(row, pad.also { it.setMargins(0, dp(8), 0, 0) })
        }

        vaultBox.addView(call, pad.also { it.setMargins(0, dp(12), 0, 0) })
        vaultBox.addView(text, pad.also { it.setMargins(0, dp(8), 0, 0) })
        vaultBox.addView(lock, pad.also { it.setMargins(0, dp(8), 0, 0) })
    }

    /* ============================== SAFETY TAB ============================== */

    private fun step(n: Int, title: String, body: String): LinearLayout =
        card("STEP $n — $title", body)

    private fun buildSafetyTab(): LinearLayout {
        val box = LinearLayout(this)
        box.orientation = LinearLayout.VERTICAL
        box.setPadding(0, dp(14), 0, 0)

        box.addView(card(
            "⚠️ READ THIS FIRST",
            "Abuse often gets worse the moment an abuser notices surveillance is about to be cut off. Do these steps QUIETLY, in order, and tell at least one trusted person what's happening.",
            red, red
        ).also { it.setPadding(dp(14), dp(12), dp(14), dp(12)) })

        val pad = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        box.addView(step(1, "Change passwords from a DIFFERENT device", "Not on this phone — a library computer, a work computer, a trusted friend's phone. Email password FIRST (it resets everything else), then banking, then socials." ).also { it.setPadding(dp(14), dp(12), dp(14), dp(12)) }, pad.also { it.setMargins(0, dp(10), 0, 0) })
        box.addView(step(2, "Two-factor — app, not SMS", "Turn on 2FA everywhere using an authenticator app (Google Authenticator / Microsoft Authenticator). SMS codes arrive on THIS phone — which he may be reading.").also { it.setPadding(dp(14), dp(12), dp(14), dp(12)) }, pad.also { it.setMargins(0, dp(8), 0, 0) })
        box.addView(step(3, "Kick out unknown devices", "myaccount.google.com → Security → 'Your devices' → sign out anything unfamiliar. Do the same on Facebook, Instagram, Apple ID. Check 'Third-party access' — remove anything you didn't connect.").also { it.setPadding(dp(14), dp(12), dp(14), dp(12)) }, pad.also { it.setMargins(0, dp(8), 0, 0) })
        box.addView(step(4, "Check email forwarding traps", "Abusers often add auto-forwarding or a recovery email to your account so they get your reset emails. Gmail → Settings → 'Forwarding and POP/IMAP' AND check the recovery phone/email on the account.").also { it.setPadding(dp(14), dp(12), dp(14), dp(12)) }, pad.also { it.setMargins(0, dp(8), 0, 0) })
        box.addView(step(5, "Scan this phone", "Use the 🔍 Scan tab. Screenshot anything suspicious BEFORE uninstalling (evidence). Uninstall with the instructions in each finding.").also { it.setPadding(dp(14), dp(12), dp(14), dp(12)) }, pad.also { it.setMargins(0, dp(8), 0, 0) })
        box.addView(step(6, "Location sharing audit", "Google Maps → profile → 'Location sharing' → stop everything you didn't set up. Also check Find My Device and any family/location apps.").also { it.setPadding(dp(14), dp(12), dp(14), dp(12)) }, pad.also { it.setMargins(0, dp(8), 0, 0) })
        box.addView(step(7, "Change your screen lock", "Right after he last had the phone: Settings → Security → Screen lock → change PIN/password. If he knows your old PIN, he may have added things while it was unlocked.").also { it.setPadding(dp(14), dp(12), dp(14), dp(12)) }, pad.also { it.setMargins(0, dp(8), 0, 0) })
        box.addView(step(8, "If stalkerware was found: factory reset", "Back up photos first (from trusted Wi-Fi, to a NEW account he doesn't know). Then Settings → System → Reset → Erase all data. IMPORTANT: set up as a NEW device and do NOT restore old app backups — stalkerware can hide inside them. Reinstall everything fresh from Play Store.").also { it.setPadding(dp(14), dp(12), dp(14), dp(12)) }, pad.also { it.setMargins(0, dp(8), 0, 0) })
        box.addView(step(9, "Make an email he can NEVER take over", "This is how you stop losing every account to him: he is breaking in through the recovery email and recovery phone you've had since before him. Build a brand-new identity like this, from a CLEAN device (library computer, work computer, a trusted friend's phone — NEVER any device he has touched):\n\n1. Make the new email address on that clean device. Leave your real name and birthday out of the address.\n2. When it asks for a recovery email, give it a SECOND brand-new secret account — never an old one he's seen.\n3. Recovery phone: NEVER your old number. Get a free new number (Google Voice / TextNow) from that same clean device, or skip the phone entirely.\n4. Security questions/answers: make them FICTION. He knows your real ones.\n5. Turn on 2FA with an authenticator app only — never SMS to your old number.\n6. Password: brand new, never used anywhere before.\n\nThen in the new account: sign out all devices, and double-check no forwarding rules and no third-party apps. Write the new addresses down ONLY in the 🔒 Vault tab (and on hidden paper). Never log into the new accounts on a device he can reach.").also { it.setPadding(dp(14), dp(12), dp(14), dp(12)) }, pad.also { it.setMargins(0, dp(8), 0, 0) })

        val hotline = Button(this, null, 0)
        hotline.text = "📞 National DV Hotline — 1-800-799-7233\n(text START to 88788 · free · confidential · 24/7)"
        hotline.textSize = 13f
        hotline.setTextColor(ink)
        hotline.background = panelBg(red)
        hotline.setPadding(dp(8), dp(12), dp(8), dp(12))
        hotline.setOnClickListener {
            try { startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:18007997233"))) } catch (e: Exception) {}
        }
        box.addView(hotline, pad.also { it.setMargins(0, dp(12), 0, 0) })

        box.addView(card(
            "Also helpful",
            "techsafety.org has step-by-step tech safety guides for every app and situation.\nA local domestic-violence advocate can help you make a plan — most work free.",
            edgeC
        ), pad.also { it.setMargins(0, dp(8), 0, 0) })
        return box
    }

    /* ============================== IPHONE TAB ============================== */

    private fun buildIphoneTab(): LinearLayout {
        val box = LinearLayout(this)
        box.orientation = LinearLayout.VERTICAL
        box.setPadding(0, dp(14), 0, 0)

        box.addView(card(
            "📱 iPhone rules of thumb",
            "iPhones are much harder to bug WITHOUT your Apple ID password — so changing that password from another device is your single most powerful move. If the phone was jailbroken all bets are off (see below).",
            edgeC
        ).also { it.setPadding(dp(14), dp(12), dp(14), dp(12)) })
        val pad = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        box.addView(step(1, "Check for mystery profiles", "Settings → General → VPN & Device Management → delete ANY configuration profile you don't recognize. A profile can silently route everything through someone else's server.").also { it.setPadding(dp(14), dp(12), dp(14), dp(12)) }, pad.also { it.setMargins(0, dp(10), 0, 0) })
        box.addView(step(2, "Audit your Apple ID devices", "Settings → your name → scroll to the device list → tap anything unfamiliar → 'Remove from account'. Then Settings → name → Sign-In & Security → Change Password.").also { it.setPadding(dp(14), dp(12), dp(14), dp(12)) }, pad.also { it.setMargins(0, dp(8), 0, 0) })
        box.addView(step(3, "Location: who can see you", "Settings → Privacy & Security → Location Services → check each app. Then 'Share My Location' → remove people you don't recognize. Check Find My → 'People' tab too.").also { it.setPadding(dp(14), dp(12), dp(14), dp(12)) }, pad.also { it.setMargins(0, dp(8), 0, 0) })
        box.addView(step(4, "Family Sharing check", "Settings → your name → Family Sharing → make sure he isn't added — Family Sharing can expose purchases, location and more.").also { it.setPadding(dp(14), dp(12), dp(14), dp(12)) }, pad.also { it.setMargins(0, dp(8), 0, 0) })
        box.addView(step(5, "Message previews & Mail rules", "Settings → Notifications → Show Previews → 'Never' (on lock screen). Mail → check for forwarding rules. Delete abusive threads AFTER saving evidence.").also { it.setPadding(dp(14), dp(12), dp(14), dp(12)) }, pad.also { it.setMargins(0, dp(8), 0, 0) })
        box.addView(step(6, "Jailbreak signs = real danger", "Apps like 'Cydia', 'Sileo' or 'Zebra', or the phone restarting on its own, mean it may be JAILBROKEN — anyone who physically held the phone could have installed full surveillance. Back up to a trusted computer, then erase and re-set-up the phone from scratch.").also { it.setPadding(dp(14), dp(12), dp(14), dp(12)) }, pad.also { it.setMargins(0, dp(8), 0, 0) })
        box.addView(step(7, "Clean sweep if unsure", "Settings → General → Transfer or Reset iPhone → Erase All Content and Settings → set up as NEW iPhone. Don't restore an old iCloud app backup if you suspect stalkerware — restore photos/messages only.").also { it.setPadding(dp(14), dp(12), dp(14), dp(12)) }, pad.also { it.setMargins(0, dp(8), 0, 0) })

        val hotline = Button(this, null, 0)
        hotline.text = "📞 National DV Hotline — 1-800-799-7233\n(free · confidential · 24/7)"
        hotline.textSize = 13f
        hotline.setTextColor(ink)
        hotline.background = panelBg(red)
        hotline.setPadding(dp(8), dp(12), dp(8), dp(12))
        hotline.setOnClickListener {
            try { startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:18007997233"))) } catch (e: Exception) {}
        }
        box.addView(hotline, pad.also { it.setMargins(0, dp(12), 0, 0) })
        return box
    }
}
