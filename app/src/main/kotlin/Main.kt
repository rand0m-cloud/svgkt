package org.svgkt.app

import androidx.compose.runtime.*
import composeViewer
import org.svgkt.*
import org.svgkt.Elements.g
import org.svgkt.compose.ApplicationScope
import org.svgkt.compose.SvgElement
import org.svgkt.compose.currentFrameIndex
import org.svgkt.compose.currentTime
import org.svgkt.nativelibs.calculateBoundingBox

@Composable
fun ApplicationScope.MainComponent() {
    var count by remember { mutableStateOf(0) }
    val time = currentTime()
    val frame = currentFrameIndex()

    LaunchedEffect(Unit) {
        while (true) {
            delay(150L)
            count += 1
        }
    }

    SvgElement(
        "svg",
        "frame" to frame.toString(),
        "time" to time.toString(),
        "xmlns" to "http://www.w3.org/2000/svg",
        "width" to "1280",
        "height" to "720",
    ) {
       SvgElement(
           "g",
           modifier = SvgModifier.rotateAroundCenter(time*720f).translate(1280f * time,0f)
       ) {
           SvgElement(
               "rect",
               "fill" to "purple",
               "width" to "100",
               "height" to "100",
           )
       }
    }
}

fun SvgModifier.center() = this then simpleModifier { tree ->
    val bb = tree.calculateBoundingBox().scale(-0.5f)
    fragment {
        g("transform" to "translate(${bb.width}, ${bb.height})") {
            child(tree)
        }
    }
}

fun SvgModifier.rotateAroundCenter(degrees: Float) = this then simpleModifier { tree ->
    val bb = tree.calculateBoundingBox().scale(0.5f)
    fragment {
        g("transform" to "rotate($degrees, ${bb.width}, ${bb.height})") {
            child(tree)
        }
    }
}

fun main() = composeViewer(
    24,
    1.0,
) {
    MainComponent()
}

