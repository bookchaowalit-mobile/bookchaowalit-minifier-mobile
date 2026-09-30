package com.bookchaowalit.minifier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class MinifierPass3Test {

    @Test
    fun jsonRejectsNonJsonWhitespaceInsteadOfSilentlyRepairingIt() {
        // RFC 8259 whitespace is only space, tab, LF and CR. Kotlin's isWhitespace()
        // also accepts NBSP-like and control separators, which hid invalid input.
        assertFailsWith<MinifyException> { Minifier.minifyJson("{\"a\": 1}") }
        assertFailsWith<MinifyException> { Minifier.minifyJson("[1,\u001F2]") }
        assertFailsWith<MinifyException> { Minifier.minifyJson("[1,　2]") }
        assertFailsWith<MinifyException> { Minifier.minifyJson("[1, 2]") }
        assertFailsWith<MinifyException> { Minifier.minifyJson("[1,​2]") }
    }

    @Test
    fun jsonToleratesLeadingBomAndKeepsUnicodeInsideStrings() {
        assertEquals("{\"a\":1}", Minifier.minifyJson("﻿{ \"a\" :\r\n 1 }"))
        assertEquals("[\"   \"]", Minifier.minifyJson("[ \"   \" ]"))
    }
}
