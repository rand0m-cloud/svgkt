package org.svgkt.nativelibs.test

import org.svgkt.Elements.g
import org.svgkt.Elements.rect
import org.svgkt.SvgModifier
import org.svgkt.buildFragment
import org.svgkt.invoke
import org.svgkt.nativelibs.BoundingBox
import org.svgkt.nativelibs.calculateBoundingBox
import org.svgkt.translate
import kotlin.test.Test
import kotlin.test.assertEquals

class LibraryTest {
    @Test
    fun calculateBoundingBoxTest() {
        val element = buildFragment {
            g {
                rect("width" to "100", "height" to "10", "fill" to "red")
                rect(
                    "width" to "100",
                    "height" to "100",
                    "fill" to "red",
                    modifier = SvgModifier.translate(10.0f, 10.0f)
                )

            }
        }.toElement()
        assertEquals(BoundingBox(110.0f, 110.0f), element.calculateBoundingBox())
    }
}
