package com.phonewarrior.app

object ThreatDb {

    val KNOWN_STALKERWARE_PREFIXES = listOf(
        "mspy", "flexispy", "flexicore", "hoverwatch", "cocospy", "spyic", "spyzie",
        "umobix", "eyezy", "eyezie", "xnspy", "guestspy", "thetruthspy", "mobistealth",
        "mobipast", "ikeymonitor", "highster", "spapp", "kidsguard", "celltracker",
        "trackview", "pumpic", "teensafe", "webwatcher", "spyera", "spyzee", "spy24",
        "spyfer", "onemonitar", "mobileryx", "spytector", "phoneticker", "safeguarde",
        "olpair", "mmguardian", "shadowspy", "stealthgenie", "mobilespy"
    )

    val KNOWN_STALKERWARE_PACKAGES = listOf(
        "com.mspy.lite",
        "com.the.truth.spy",
        "com.spyapp.mobile",
        "com.spappmonitor",
        "com.hpublisher.app",
        "com.hoverwatch.lite",
        "ru.modspy.mobile",
        "com.wonderground.time",
        "com.googlecode.android.wifitether",
        "com.teslacoilsw.launcher.hidensearch"
    )

    val SEVERE_LABEL_WORDS = listOf("spy", "keylog", "stalker", "spyware", "intercept")

    val SOFT_LABEL_WORDS = listOf("tracker", "monitor", "hidden", "trace", "surveil")

    val SYSTEM_PREFIXES = listOf(
        "com.android", "com.google.android", "com.samsung", "com.sec.android",
        "com.miui", "com.xiaomi", "com.huawei", "com.hihonor", "com.oppo", "com.vivo",
        "com.oneplus", "com.coloros", "com.realme", "com.transsion", "com.zte",
        "com.lenovo", "com.motorola", "com.sony", "com.htc", "com.lge", "com.asus",
        "com.nokia", "com.tcl", "com.alcatel", "android", "org.chromium",
        "org.lineageos", "com.wssyncmldm", "com.sec.factory", "com.ws.dm",
        "com.google.ar", "com.facebook.system", "com.linkedin.android.jobdispatcher"
    )

    fun isSystemPkg(pkg: String): Boolean {
        val p = pkg.lowercase()
        return SYSTEM_PREFIXES.any { p == it || p.startsWith("$it.") }
    }

    fun isKnownStalkerware(pkg: String, label: String): Boolean {
        val p = pkg.lowercase().replace(" ", "")
        val l = label.lowercase().replace(" ", "")
        val pkgHit = KNOWN_STALKERWARE_PACKAGES.any { p == it || p.startsWith(it) || p.contains(it) }
        val prefixHit = KNOWN_STALKERWARE_PREFIXES.any { p.contains(it) || l.contains(it) }
        return pkgHit || prefixHit
    }

    fun hasSevereWord(s: String): Boolean = SEVERE_LABEL_WORDS.any { s.contains(it) }

    fun hasSoftWord(s: String): Boolean = SOFT_LABEL_WORDS.any { s.contains(it) }

    fun prettyPerm(perm: String): String = when {
        perm.endsWith("ACCESS_FINE_LOCATION") || perm.endsWith("ACCESS_COARSE_LOCATION") -> "Sees your location"
        perm.endsWith("RECORD_AUDIO") -> "Uses the microphone"
        perm.endsWith("CAMERA") -> "Uses the camera"
        perm.endsWith("READ_SMS") -> "Reads text messages"
        perm.endsWith("RECEIVE_SMS") -> "Intercepts incoming texts"
        perm.endsWith("SEND_SMS") -> "Sends texts from your number"
        perm.endsWith("READ_CONTACTS") -> "Reads your contacts"
        perm.endsWith("READ_CALL_LOG") -> "Reads your call history"
        perm.endsWith("READ_PHONE_STATE") -> "Sees your phone number & calls"
        perm.endsWith("SYSTEM_ALERT_WINDOW") -> "Draws over other apps (screen capture overlay)"
        perm.endsWith("QUERY_ALL_PACKAGES") -> "Sees every app installed"
        perm.endsWith("READ_EXTERNAL_STORAGE") || perm.endsWith("WRITE_EXTERNAL_STORAGE") -> "Reads your files & photos"
        perm.endsWith("REQUEST_INSTALL_PACKAGES") -> "Can install other apps silently"
        perm.endsWith("BIND_ACCESSIBILITY_SERVICE") -> "Accessibility control"
        perm.endsWith("GET_ACCOUNTS") -> "Sees your accounts"
        perm.endsWith("BODY_SENSORS") -> "Reads body sensors"
        else -> null
    }
}

class Finding(
    val pkg: String,
    val label: String,
    var severity: Int,
    val reasons: MutableList<String>,
    val installer: String?,
    val installedOn: java.util.Date?
)

object Perms {
    val LOCATION = setOf("android.permission.ACCESS_FINE_LOCATION", "android.permission.ACCESS_COARSE_LOCATION")
    val MIC = setOf("android.permission.RECORD_AUDIO")
    val CAM = setOf("android.permission.CAMERA")
    val SMS = setOf("android.permission.READ_SMS", "android.permission.RECEIVE_SMS", "android.permission.SEND_SMS", "android.permission.RECEIVE_MMS")
    val CONTACTS = setOf("android.permission.READ_CONTACTS")
    val CALLLOG = setOf("android.permission.READ_CALL_LOG")
}
