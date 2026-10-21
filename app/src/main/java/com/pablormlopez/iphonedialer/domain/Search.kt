package com.pablormlopez.iphonedialer.domain
import java.text.Normalizer
object Search {
    fun digits(value: String) = value.filter { it in '0'..'9' }
    fun normalized(value: String): String = Normalizer.normalize(value, Normalizer.Form.NFD)
        .replace("\\p{M}".toRegex(), "").lowercase(java.util.Locale.ROOT)
    fun t9(value: String): String = normalized(value).mapNotNull { c ->
        when(c) { in 'a'..'c' -> '2'; in 'd'..'f' -> '3'; in 'g'..'i' -> '4'; in 'j'..'l' -> '5'; in 'm'..'o' -> '6'; in 'p'..'s' -> '7'; in 't'..'v' -> '8'; in 'w'..'z' -> '9'; in '0'..'9' -> c; else -> null }
    }.joinToString("")
    fun matches(name: String, numbers: List<String>, query: String): Boolean {
        if (query.isBlank()) return true
        if (normalized(name).contains(normalized(query))) return true
        val n = digits(query)
        return n.isNotEmpty() && (numbers.any { digits(it).contains(n) } || t9(name).contains(n))
    }
    fun validDialString(value: String): Boolean = value.isNotBlank() && value.length <= 128 && value.all { it.isDigit() || it in "+*#,; ()-." }
    fun duration(seconds: Long): String = "%02d:%02d".format(java.util.Locale.ROOT, seconds.coerceAtLeast(0) / 60, seconds.coerceAtLeast(0) % 60)
}
