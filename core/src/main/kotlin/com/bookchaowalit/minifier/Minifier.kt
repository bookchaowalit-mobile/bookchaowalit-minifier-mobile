package com.bookchaowalit.minifier

enum class Language { JSON, CSS, JAVASCRIPT_WHITESPACE }

data class MinifyResult(val output: String, val originalBytes: Int, val minifiedBytes: Int) {
    val savedBytes: Int get() = originalBytes - minifiedBytes

    /** Percentage saved, 0.0 for empty input. */
    val savedPercent: Double
        get() = if (originalBytes == 0) 0.0 else savedBytes * 100.0 / originalBytes
}

class MinifyException(message: String) : IllegalArgumentException(message)

object Minifier {

    fun minify(source: String, language: Language): MinifyResult {
        val out = when (language) {
            Language.JSON -> minifyJson(source)
            Language.CSS -> minifyCss(source)
            Language.JAVASCRIPT_WHITESPACE -> stripLineWhitespace(source)
        }
        return MinifyResult(out, source.encodeToByteArray().size, out.encodeToByteArray().size)
    }

    /**
     * Removes insignificant whitespace from JSON while leaving string contents
     * (including escaped quotes) untouched. Throws [MinifyException] on an
     * unterminated string or unbalanced brackets.
     */
    fun minifyJson(source: String): String {
        val sb = StringBuilder(source.length)
        val stack = ArrayDeque<Char>()
        var i = 0
        while (i < source.length) {
            val ch = source[i]
            when {
                ch == '"' -> {
                    val end = endOfString(source, i, '"')
                    sb.append(source, i, end + 1)
                    i = end
                }
                ch == '{' || ch == '[' -> { stack.addLast(ch); sb.append(ch) }
                ch == '}' || ch == ']' -> {
                    val open = stack.removeLastOrNull()
                    val expected = if (ch == '}') '{' else '['
                    if (open != expected) throw MinifyException("unbalanced '$ch' at index $i")
                    sb.append(ch)
                }
                ch.isWhitespace() -> Unit
                else -> sb.append(ch)
            }
            i++
        }
        if (stack.isNotEmpty()) throw MinifyException("unclosed '${stack.last()}'")
        return sb.toString()
    }

    /**
     * CSS minification: drops comments, collapses whitespace, removes spaces
     * around `{ } : ; , >` and the last `;` before `}`. Quoted strings are kept verbatim.
     */
    fun minifyCss(source: String): String {
        val sb = StringBuilder(source.length)
        var i = 0
        var pendingSpace = false
        fun flushSpace(next: Char) {
            if (pendingSpace && sb.isNotEmpty() && sb.last() !in CSS_TIGHT && next !in CSS_TIGHT) sb.append(' ')
            pendingSpace = false
        }
        while (i < source.length) {
            val ch = source[i]
            when {
                ch == '/' && i + 1 < source.length && source[i + 1] == '*' -> {
                    val end = source.indexOf("*/", i + 2)
                    if (end < 0) throw MinifyException("unterminated comment at index $i")
                    i = end + 1
                    pendingSpace = true
                }
                ch == '"' || ch == '\'' -> {
                    flushSpace(ch)
                    val end = endOfString(source, i, ch)
                    sb.append(source, i, end + 1)
                    i = end
                }
                ch.isWhitespace() -> pendingSpace = true
                ch == '}' -> {
                    pendingSpace = false
                    if (sb.isNotEmpty() && sb.last() == ';') sb.setLength(sb.length - 1)
                    sb.append(ch)
                }
                else -> { flushSpace(ch); sb.append(ch) }
            }
            i++
        }
        return sb.toString().trim()
    }

    /**
     * Conservative JavaScript shrink: trims each line and drops blank lines.
     * It never rewrites tokens, so it cannot change program meaning.
     */
    fun stripLineWhitespace(source: String): String =
        source.lineSequence().map { it.trim() }.filter { it.isNotEmpty() }.joinToString("\n")

    private val CSS_TIGHT = setOf('{', '}', ':', ';', ',', '>')

    private fun endOfString(source: String, start: Int, quote: Char): Int {
        var j = start + 1
        while (j < source.length) {
            when (source[j]) {
                '\\' -> j++
                quote -> return j
            }
            j++
        }
        throw MinifyException("unterminated string starting at index $start")
    }
}
