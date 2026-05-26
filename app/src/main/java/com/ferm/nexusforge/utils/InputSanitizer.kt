package com.ferm.nexusforge.utils


object InputSanitizer {
    fun sanitizeModpackName(name: String): String {
        if (name.isBlank()) return "modpack"

        return name
            .replace(Regex("""\.\.[\\/]"""), "")
            .replace("..", "")
            .replace(Regex("""[\s<>:"|?*\\/]"""), "_")
            .replace(Regex("""[\x00-\x1f\x7f]"""), "")
            .replace(Regex("""[^a-zA-Z0-9_\-]"""), "")
            .replace(Regex("""_+"""), "_")
            //края
            .trim('_', '-')
            .takeIf { it.isNotEmpty() } ?: "modpack"
    }

    fun sanitizeSearchQuery(query: String): String {
        if (query.isBlank()) return ""
        
        return query
            .take(100)
            .replace(Regex("""[\x00-\x1f\x7f]"""), "")
            // space
            .trim()
    }
    fun isValidSearchQuery(query: String): Boolean {
        return query.length in 1..100
    }
}
