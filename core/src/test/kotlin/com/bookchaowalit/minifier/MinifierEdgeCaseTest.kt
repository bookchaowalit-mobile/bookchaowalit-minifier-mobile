package com.bookchaowalit.minifier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class MinifierEdgeCaseTest {

    @Test
    fun jsonRejectsValuesThatWouldBeGluedTogether() {
        assertFailsWith<MinifyException> { Minifier.minifyJson("[1 2]") }
        assertFailsWith<MinifyException> { Minifier.minifyJson("""{"a" "b"}""") }
        assertFailsWith<MinifyException> { Minifier.minifyJson("[tr ue]") }
        assertEquals("[1,-2.5e+3,true,null]", Minifier.minifyJson("[ 1 ,\n -2.5e+3 , true,\tnull ]"))
    }

    @Test
    fun jsonKeepsEscapesAndUnicode() {
        assertEquals("""{"k":"a\\\"b","t":"ไทย 😀"}""", Minifier.minifyJson("""{ "k" : "a\\\"b" , "t" : "ไทย 😀" }"""))
        assertEquals("", Minifier.minifyJson("  \n "))
        assertFailsWith<MinifyException> { Minifier.minifyJson("]") }
        assertFailsWith<MinifyException> { Minifier.minifyJson("""["unterminated\"]""") }
    }

    @Test
    fun cssKeepsDescendantSpaceBeforePseudoClass() {
        assertEquals("div :first-child{color:red}", Minifier.minifyCss("div :first-child { color : red; }"))
        assertEquals("a:hover{color:red}", Minifier.minifyCss("a:hover {\n  color: red;\n}"))
        assertEquals(
            "@media (max-width:600px){.a :focus{outline:0}}",
            Minifier.minifyCss("@media (max-width: 600px) {\n  .a :focus { outline: 0; }\n}"),
        )
    }

    @Test
    fun cssKeepsStringsCombinatorsAndValueSpaces() {
        assertEquals(
            // "+" and "~" keep their spaces: calc(1px + 2px) needs them.
            """.a>.b + .c ~ .d{content:"x  ;  }";margin:0 auto}""",
            Minifier.minifyCss(""".a > .b + .c ~ .d { content: "x  ;  }"; margin: 0   auto; }"""),
        )
        assertEquals(".a{}", Minifier.minifyCss("/* c */ .a { } /* d */"))
    }

    @Test
    fun jsKeepsMultiLineTemplateLiteralsVerbatim() {
        val src = "  const html = `\n    <p>\n\n      hi  \n    </p>`;\n\n  run(html);  \n"
        assertEquals("const html = `\n    <p>\n\n      hi  \n    </p>`;\nrun(html);", Minifier.stripLineWhitespace(src))
    }

    @Test
    fun jsEscapedBacktickDoesNotOpenATemplate() {
        assertEquals("const s = `a \\` b`;\nx();", Minifier.stripLineWhitespace("  const s = `a \\` b`;  \n   x();"))
    }

    @Test
    fun savingsForEveryLanguage() {
        for (lang in Language.entries) {
            val r = Minifier.minify("", lang)
            assertEquals("", r.output)
            assertEquals(0, r.savedBytes)
        }
        val r = Minifier.minify("[ 1 ]", Language.JSON)
        assertEquals("[1]", r.output)
        assertEquals(40.0, r.savedPercent)
    }
}
