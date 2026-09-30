package com.bookchaowalit.minifier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class MinifierTest {
    @Test
    fun jsonRemovesWhitespaceButKeepsStrings() {
        val src = """
            {
              "name" : "a  b",
              "quote": "say \"hi\" { }",
              "list": [ 1, 2 ,3 ]
            }
        """.trimIndent()
        assertEquals("""{"name":"a  b","quote":"say \"hi\" { }","list":[1,2,3]}""", Minifier.minifyJson(src))
    }

    @Test
    fun jsonRejectsBrokenInput() {
        assertFailsWith<MinifyException> { Minifier.minifyJson("""{"a": "open}""") }
        assertFailsWith<MinifyException> { Minifier.minifyJson("""{"a": [1, 2}""") }
        assertFailsWith<MinifyException> { Minifier.minifyJson("""[1, 2""") }
    }

    @Test
    fun cssDropsCommentsAndSpaces() {
        val src = """
            /* header */
            .a  >  .b ,  .c {
              color : red ;
              content: "a  /* not a comment */";
            }
            @media (max-width: 600px) { .d { margin: 0 auto; } }
        """.trimIndent()
        assertEquals(
            """.a>.b,.c{color:red;content:"a  /* not a comment */"}@media (max-width:600px){.d{margin:0 auto}}""",
            Minifier.minifyCss(src),
        )
    }

    @Test
    fun cssRejectsUnterminatedComment() {
        assertFailsWith<MinifyException> { Minifier.minifyCss(".a{} /* oops") }
    }

    @Test
    fun jsWhitespaceOnlyTrimsLines() {
        assertEquals("let a = 1;\nif (a) {\nreturn a;\n}", Minifier.stripLineWhitespace("  let a = 1;\n\n  if (a) {\n    return a;\n  }\n"))
    }

    @Test
    fun resultReportsSavings() {
        val r = Minifier.minify("{ \"a\" : 1 }", Language.JSON)
        assertEquals("{\"a\":1}", r.output)
        assertEquals(11, r.originalBytes)
        assertEquals(7, r.minifiedBytes)
        assertEquals(4, r.savedBytes)
        assertEquals(0.0, Minifier.minify("", Language.CSS).savedPercent)
        // multi-byte characters are counted as UTF-8 bytes
        assertEquals(4, Minifier.minify("\"ก\"", Language.JSON).minifiedBytes - 1)
    }
}
