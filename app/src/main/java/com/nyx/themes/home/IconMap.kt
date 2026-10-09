package com.nyx.themes.home

/** Which painted glyph an installed app gets, matched on package name and label. */
object IconMap {
    /** every glyph the icon packs ship (file name = glyph + ".png") */
    val GLYPHS = listOf(
        "phone", "messages", "contacts", "camera", "browser", "music", "video", "maps", "clock", "settings", "gallery", "mail",
        "calendar", "notes", "calculator", "files", "weather", "store", "games", "wallet", "health", "cloud", "shield",
        "assistant", "recorder", "book", "download", "chat", "social",
    )

    private class Rule(val glyph: String, val pkg: List<String>, val label: List<String>)

    private val rules = listOf(
        Rule("phone", listOf("dialer", ".phone", "telecom"), listOf("phone", "dialer")),
        Rule("messages", listOf("messaging", ".mms", "messages", "sms"), listOf("messages", "sms", "messaging")),
        Rule("contacts", listOf("contacts"), listOf("contacts")),
        Rule("camera", listOf("camera"), listOf("camera")),
        Rule("assistant", listOf("gemini", "chatgpt", "openai", "claude", "assistant", "character", "copilot", "perplexity"), listOf("gemini", "chatgpt", "claude", "assistant", "character.ai", "copilot", "ai ")),
        Rule("browser", listOf("chrome", "firefox", "opera", "brave", "browser", "emmx", "duckduckgo", "vivaldi"), listOf("chrome", "browser", "firefox", "opera", "brave", "edge")),
        Rule("music", listOf("music", "spotify", "soundcloud", "deezer", "shazam", "anghami"), listOf("music", "spotify", "soundcloud", "deezer", "shazam", "anghami")),
        Rule("video", listOf("youtube", "netflix", "primevideo", "video", "twitch", "vlc", "hotstar", "disney", ".tv"), listOf("youtube", "netflix", "prime video", "video", "twitch", "vlc", "tv", "player")),
        Rule("maps", listOf("maps", "waze", "navigation"), listOf("maps", "waze", "navigation")),
        Rule("clock", listOf("clock", "alarm"), listOf("clock", "alarm", "timer")),
        Rule("gallery", listOf("photos", "gallery", "picture"), listOf("photos", "gallery")),
        Rule("mail", listOf("android.gm", "mail", "outlook", "email"), listOf("gmail", "mail", "outlook", "email")),
        Rule("calendar", listOf("calendar"), listOf("calendar")),
        Rule("notes", listOf("keep", "notes", "notepad", "memo", "onenote", "notion"), listOf("notes", "keep", "notepad", "memo", "notion")),
        Rule("calculator", listOf("calculator"), listOf("calculator")),
        Rule("files", listOf("documentsui", "files", "filemanager", "file.manager", "explorer", "archiver", "zarchiver"), listOf("files", "file manager", "archiver", "explorer")),
        Rule("weather", listOf("weather"), listOf("weather")),
        Rule("store", listOf("vending", "appstore", "appcenter", "galaxystore", "aptoide"), listOf("play store", "app store", "app center", "store")),
        Rule("games", listOf("game", "pubg", "minecraft", "roblox", "supercell", "tencent", "garena", "ea.gp", "fcmobile", "ufl", "xarena"), listOf("game", "pubg", "minecraft", "roblox", "fc mobile", "ufl", "arena")),
        Rule("wallet", listOf("wallet", "pay", "bank", "finance", "paypal"), listOf("wallet", "pay", "bank")),
        Rule("health", listOf("fitness", "health", ".fit", "gms.fit"), listOf("fit", "health")),
        Rule("cloud", listOf("drive", "cloud", "dropbox", "onedrive", "mega"), listOf("drive", "cloud", "dropbox", "onedrive")),
        Rule("shield", listOf("vpn", "security", "antivirus", "phonemaster", "safety", "guard", "protect", "authenticator"), listOf("vpn", "security", "phone master", "safety", "authenticator", "guard")),
        Rule("recorder", listOf("recorder", "voice", "dictaphone"), listOf("recorder", "voice")),
        Rule("book", listOf("books", "reader", "kindle", "wattpad", "pdf"), listOf("books", "reader", "kindle", "pdf")),
        Rule("download", listOf("download", "torrent", "idm"), listOf("download", "torrent")),
        Rule("chat", listOf("whatsapp", "telegram", "signal", "messenger", "discord", "viber", "wechat", "skype", "line", "slack", "teams", "meet", "duo", "zoom"), listOf("whatsapp", "telegram", "signal", "messenger", "discord", "viber", "skype", "slack", "teams", "meet", "zoom")),
        Rule("social", listOf("instagram", "facebook", "twitter", "tiktok", "snapchat", "reddit", "pinterest", "linkedin", "threads", "x.android"), listOf("instagram", "facebook", "twitter", "tiktok", "snapchat", "reddit", "pinterest", "linkedin", "threads")),
        Rule("settings", listOf("settings"), listOf("settings")),
    )

    /** glyph name for an app, or null when no painted icon fits (the app then gets a pack tile around its own icon) */
    fun glyphFor(pkg: String, label: String): String? {
        val p = pkg.lowercase()
        val l = label.lowercase()
        // exact label wins over loose package matching so e.g. "Settings" is not mistaken for another rule
        for (r in rules) if (r.label.any { l == it }) return r.glyph
        for (r in rules) if (r.pkg.any { p.contains(it) }) return r.glyph
        for (r in rules) if (r.label.any { l.contains(it) && it.length >= 4 }) return r.glyph
        return null
    }
}
