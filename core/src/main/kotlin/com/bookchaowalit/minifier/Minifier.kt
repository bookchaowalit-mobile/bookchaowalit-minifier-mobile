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
        // A leading byte-order mark is an encoding artefact, not content.
        var i = if (source.startsWith('\uFEFF')) 1 else 0
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
                ch in JSON_WHITESPACE -> {
                    // "[1 2]" must not silently become "[12]".
                    if (sb.isNotEmpty() && isJsonScalarChar(sb.last())) {
                        var k = i + 1
                        while (k < source.length && source[k] in JSON_WHITESPACE) k++
                        if (k < source.length && isJsonScalarChar(source[k])) {
                            throw MinifyException("missing ',' or ':' before index $k")
                        }
                    }
                }
                // NBSP, U+2028, U+001F, zero-width spaces ... are not JSON whitespace;
                // dropping them silently would "repair" invalid input.
                ch.isWhitespace() || ch.isISOControl() || ch.category == CharCategory.FORMAT ->
                    throw MinifyException("invalid character U+${ch.code.toString(16).uppercase().padStart(4, '0')} at index $i")
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
        fun flushSpace(next: Char, at: Int) {
            val keep = pendingSpace && sb.isNotEmpty() && sb.last() !in CSS_TIGHT &&
                (next !in CSS_TIGHT || (next == ':' && inSelector(source, at)))
            if (keep) sb.append(' ')
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
                    flushSpace(ch, i)
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
                else -> { flushSpace(ch, i); sb.append(ch) }
            }
            i++
        }
        return sb.toString().trim()
    }

    /**
     * Conservative JavaScript shrink: trims each line and drops blank lines.
     * It never rewrites tokens. Lines inside a multi-line template literal
     * (`...`) are kept verbatim because their whitespace is part of the string.
     * Backticks inside quotes or comments are not recognised (heuristic).
     */
    fun stripLineWhitespace(source: String): String {
        val out = mutableListOf<String>()
        var inTemplate = false
        for (line in source.lines()) {
            val startsInside = inTemplate
            var escaped = false
            for (c in line) {
                when {
                    escaped -> escaped = false
                    c == '\\' -> escaped = true
                    c == '`' -> inTemplate = !inTemplate
                }
            }
            var kept = if (startsInside) line else line.trimStart()
            if (!inTemplate) kept = kept.trimEnd()
            if (kept.isNotEmpty() || startsInside) out += kept
        }
        return out.joinToString("\n")
    }

    /** The only whitespace RFC 8259 allows between JSON tokens. */
    private val JSON_WHITESPACE = setOf(' ', '\t', '\n', '\r')

    private val CSS_TIGHT = setOf('{', '}', ':', ';', ',', '>')

    private fun isJsonScalarChar(c: Char) = c.isLetterOrDigit() || c == '.' || c == '-' || c == '+' || c == '"'

    /**
     * True when the ':' at [at] belongs to a selector (`div :hover {`), where the
     * space before it is a descendant combinator and must be kept. A selector
     * reaches `{` before any `;` or `}`; a declaration does not.
     */
    private fun inSelector(source: String, at: Int): Boolean {
        for (j in at until source.length) {
            when (source[j]) {
                '{' -> return true
                ';', '}' -> return false
            }
        }
        return false
    }

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
