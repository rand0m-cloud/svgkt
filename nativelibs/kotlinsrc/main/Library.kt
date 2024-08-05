package org.svgkt.nativelibs

import org.svgkt.Elements.svg
import org.svgkt.SvgElement
import org.svgkt.fragment
import org.svgkt.invoke
import org.svgkt.nativelibs.sys.bindings_h
import java.lang.foreign.Arena
import java.lang.foreign.MemorySegment
import java.lang.foreign.SegmentAllocator
import java.nio.charset.Charset
import java.nio.file.Files
import kotlin.io.path.createTempDirectory
import kotlin.io.path.div
import org.svgkt.nativelibs.sys.BoundingBox as SysBoundingBox

private object NativeLibraryInitializer {
    init {
        val lib = createTempDirectory("nativeLibs") / "libnativelibs.so"
        val stream = this.javaClass.getResourceAsStream("/resources/libnativelibs.so")
            ?: throw Throwable("Unable to grab native library from jar")
        Files.copy(stream, lib)
        System.load("$lib")
    }
}

data class BoundingBox(val width: Float, val height: Float) {
    fun scale(scale: Float): BoundingBox = BoundingBox(width * scale, height * scale)
}

data class UsvgTree(private val memory: MemorySegment) : AutoCloseable {
    override fun close() {
        bindings_h.free_tree(memory)
    }

    fun getBoundingBox(): BoundingBox = Arena.ofConfined().use { arena ->
        val bb = bindings_h.get_bounding_box(arena, memory)
        BoundingBox(SysBoundingBox.width(bb), SysBoundingBox.height(bb))
    }
}

private fun SvgElement.toUtf8MemorySegment(allocator: SegmentAllocator): MemorySegment =
    allocator.allocateFrom(toString(), Charset.forName("UTF-8"))

fun SvgElement.toUsvgTree(allocator: SegmentAllocator = Arena.ofAuto()): UsvgTree? {
    NativeLibraryInitializer
    val tree = bindings_h.read_svg_to_tree(toUtf8MemorySegment(allocator))

    if (tree.address() == 0L) return null
    return UsvgTree(tree)
}

private fun SvgElement.wrapToDocument(): SvgElement = fragment {
    svg("xmlns" to "http://www.w3.org/2000/svg") {
        child(this@wrapToDocument)
    }
}

fun SvgElement.calculateBoundingBox(): BoundingBox = Arena.ofConfined().use { arena ->
    wrapToDocument().toUsvgTree(arena).use { tree ->
        if (tree == null) {
            throw Throwable("toUsvgTree failed to create tree")
        }
        tree.getBoundingBox()
    }
}
