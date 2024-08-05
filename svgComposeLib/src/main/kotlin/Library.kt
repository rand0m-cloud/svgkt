package org.svgkt.compose

import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.ObserverHandle
import androidx.compose.runtime.snapshots.Snapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import org.svgkt.*
import org.svgkt.Elements.g
import org.svgkt.nativelibs.calculateBoundingBox

abstract class Node {
    val children = mutableListOf<Node>()
    abstract fun toSvgBuilderElement(): SvgBuilderElement
}

class SvgNode(var svg: SvgBuilderElement) : Node() {
    override fun toString(): String = "SvgNode($svg) {${children.joinToString(",")}}"
    override fun toSvgBuilderElement(): SvgBuilderElement = svg.appendBuildFragment {
        children.forEach {
            child(it.toSvgBuilderElement())
        }
    }
}

class NodeApplier(root: Node) : AbstractApplier<Node>(root) {
    override fun move(from: Int, to: Int, count: Int) {
        current.children.move(from, to, count)
    }

    override fun onClear() {
        root.children.clear()
    }

    override fun remove(index: Int, count: Int) {
        current.children.remove(index, count)
    }

    override fun insertTopDown(index: Int, instance: Node) {
        current.children.add(index, instance)
    }

    override fun insertBottomUp(index: Int, instance: Node) {
    }
}

fun Node.setContent(parent: CompositionContext, content: @Composable () -> Unit): Composition {
    return Composition(NodeApplier(this), parent).apply {
        setContent(content)
    }
}

@Composable
fun SvgElement(
    tag: String,
    vararg attr: Pair<String, String>,
    modifier: SvgModifier = SvgModifier,
    content: @Composable () -> Unit = {}
) {
    ComposeNode<SvgNode, NodeApplier>(factory = {
        SvgNode(buildFragment {})
    }, update = {
        set(tag) {
            this.svg.tag = it
        }
        set(attr) {
            this.svg.attrs = attr.toMutableList()
        }
        set(modifier) {
            this.svg.modifier = modifier
        }
    }) {
        content()
    }
}

fun runApp(root: Node, parent: CompositionContext, content: @Composable () -> Unit) {
    root.setContent(parent) {
        content()
    }
}

object GlobalSnapshotManager {
    private var started = false
    private var removeWriteObserver: (ObserverHandle)? = null
    var commitPending = false

    fun ensureStarted() {
        if (!started) {
            started = true
            removeWriteObserver = Snapshot.registerGlobalWriteObserver(globalWriteObserver)
        }
    }

    private val globalWriteObserver: (Any) -> Unit = {
        commitPending = true
    }

    inline fun onPending(block: () -> Unit) {
        if (!commitPending) return
        block()
        commitPending = false
    }
}

interface ApplicationScope {
    fun exit()
    suspend fun delay(timeMillis: Long)
}

interface FrameClock {
    val lastNanoTime: State<Long>
    val frameIndex: State<Long>
    val delta: Long
    fun nextFrameTime(): Long
}

val LocalFrameClock = staticCompositionLocalOf<FrameClock> { error("missing frame clock") }

@OptIn(ExperimentalComposeRuntimeApi::class)
fun application(
    duration: Double = Double.POSITIVE_INFINITY,
    fps: Int = 24,
    onFrame: (Node, Long) -> Unit = { _, _ -> },
    content: @Composable ApplicationScope.() -> Unit
) {

    GlobalSnapshotManager.ensureStarted()
    runBlocking(Dispatchers.Main) {
        val node = SvgNode(buildFragment { })

        val frameClock = object : FrameClock {
            private val _lastNanoTime = mutableStateOf(0L)
            override val lastNanoTime: State<Long> = _lastNanoTime

            private val _frameIndex = mutableStateOf(0L)
            override val frameIndex: State<Long> = _frameIndex

            override val delta: Long = 1_000_000_000L / fps.toLong()
            override fun nextFrameTime(): Long {
                _lastNanoTime.value += delta
                _frameIndex.value += 1
                return _lastNanoTime.value
            }
        }
        var isRunning = true

        val recomposer = Recomposer(Dispatchers.Main)

        val composition = ControlledComposition(NodeApplier(node), parent = recomposer)
        composition.setContent {
            with(object : ApplicationScope {
                override fun exit() {
                    isRunning = false
                }

                override suspend fun delay(timeMillis: Long) {
                    val start = frameClock.lastNanoTime.value
                    val end = start + timeMillis * 1_000_000
                    while (true) {
                        yield()
                        if (frameClock.lastNanoTime.value >= end) {
                            return
                        }
                    }
                }
            }) {
                CompositionLocalProvider(LocalFrameClock provides frameClock) {
                    content()
                }
            }
        }

        onFrame(node, 0L)

        while (isRunning) {
            yield()

            val time = frameClock.nextFrameTime()
            if (time.toDouble() > duration * 1_000_000_000.0) {
                isRunning = false
                break
            }

            GlobalSnapshotManager.onPending {
                Snapshot.sendApplyNotifications()
                composition.invalidateAll()

                if (composition.recompose()) {
                    composition.applyChanges()
                }
            }

            onFrame(node, time)
        }
        recomposer.cancel()
        recomposer.join()
    }
}

@Composable
fun currentTimeNanos(): Long = LocalFrameClock.current.lastNanoTime.value

@Composable
fun currentFrameIndex(): Long = LocalFrameClock.current.frameIndex.value

@Composable
fun currentTime(): Float = currentTimeNanos().toFloat() / 1_000_000_000