package org.svgkt.app

import androidx.compose.runtime.*
import org.svgkt.*
import org.svgkt.Elements.g
import org.svgkt.compose.*
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
        "width" to "300",
        "height" to "200",
    ) {
        SvgElement(
            "g",
            modifier = SvgModifier.rotateAroundCenter(360.0f * time).translate(50f, 50f)
        ) {
            SvgElement(
                "rect",
                "fill" to "red",
                "width" to "100",
                "height" to "100",
            ) {
                SvgElement("g", "data-count" to "$count")
            }

        }

    }
}

@Suppress("UnusedReceiverParameter")
fun SvgModifier.center() = simpleModifier { tree ->
    val bb = tree.calculateBoundingBox().scale(-0.5f)
    fragment {
        g("transform" to "translate(${bb.width}, ${bb.height})") {
            child(tree)
        }
    }
}

@Suppress("UnusedReceiverParameter")
fun SvgModifier.rotateAroundCenter(degrees: Float) = simpleModifier { tree ->
    val bb = tree.calculateBoundingBox().scale(0.5f)
    fragment {
        g("transform" to "rotate($degrees, ${bb.width}, ${bb.height})") {
            child(tree)
        }
    }
}

fun main() = application(
    duration = 1.0,
    fps = 60,
    onFrame = { frame, _ ->
        println("${frame.toSvgBuilderElement().toElement()}")
    }
) {
    MainComponent()
}


