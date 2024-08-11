import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.singleWindowApplication
import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.Image
import org.jetbrains.skia.ImageInfo
import org.svgkt.SvgElement
import org.svgkt.nativelibs.calculateBoundingBox
import org.svgkt.nativelibs.makeImageBuffer
import org.svgkt.nativelibs.toUsvgTree
import org.svgkt.compose.ApplicationScope as SvgApplicationScope
import org.svgkt.compose.application as svgApplication

fun createImage(frame: SvgElement): ImageBitmap = frame.toUsvgTree()!!.use {
    val imageBuffer = it.makeImageBuffer()
    Image.makeRaster(
        ImageInfo.makeN32(imageBuffer.width, imageBuffer.height, ColorAlphaType.PREMUL),
        imageBuffer.data,
        4 * imageBuffer.width
    ).toComposeImageBitmap()
}

fun composeViewer(fps: Int, duration: Double, animation: @Composable SvgApplicationScope.() -> Unit) {
    val frames = mutableListOf<SvgElement>()
    svgApplication(fps, duration, { frame, _ ->
        frames.add(frame.toSvgBuilderElement().toElement())
    }, animation)

    val startFrame = frames[0].calculateBoundingBox()
    val width = startFrame.width.toInt()
    val height = startFrame.height.toInt()


    singleWindowApplication {
        val clipboardManager = LocalClipboardManager.current
        Column {
            Text("Width: $width")
            Text("Height: $height")
            Text("frame[0]: ${frames[0]}", modifier = Modifier.clickable {
                clipboardManager.setText(AnnotatedString(frames[0].toString()))
            })
            LazyColumn {
                items(frames) {
                    Box(Modifier.fillMaxWidth().height(240.dp))
                    FrameView(createImage(it), width, height)
                }
            }
        }
    }
}

@Composable
fun FrameView(frame: ImageBitmap, pixelWidth: Int, pixelHeight: Int) {
    with(LocalDensity.current) {
        BoxWithConstraints {
            val scale = ContentScale.Fit.computeScaleFactor(
                Size(pixelWidth.toFloat(), pixelHeight.toFloat()),
                Size(maxWidth.toPx(), maxHeight.toPx())
            )
            val size = Size(pixelWidth.toFloat() * scale.scaleX, pixelHeight.toFloat() * scale.scaleY)
            Canvas(
                Modifier
                    .requiredSize(size.width.toDp(), size.height.toDp())
                    .background(Color.Gray)
                    .border(4.dp, Color.Black)
            ) {
                drawImage(frame, dstSize = IntSize(size.width.toInt(), size.height.toInt()))
            }
        }
    }

}