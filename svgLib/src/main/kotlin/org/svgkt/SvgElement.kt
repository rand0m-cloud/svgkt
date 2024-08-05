package org.svgkt

interface SvgElement {
    var tag: String
    var attrs: MutableList<Pair<String, String>>
    var children: MutableList<SvgElement>
}

interface SvgElementScope {
    fun attr(tag: String, value: String)
    fun child(vararg c: SvgElement)
}

class SvgElementImpl(
    override var tag: String,
    override var attrs: MutableList<Pair<String, String>> = mutableListOf(),
    override var children: MutableList<SvgElement> = mutableListOf()
) : SvgElement, SvgElementScope {
    override fun attr(tag: String, value: String) {
        attrs.add(tag to value)
    }

    override fun child(vararg c: SvgElement) {
        children += c
    }

    override fun toString(): String {
        val stringChildren = StringBuilder().run {
            children.asIterable().forEach {
                append(it.toString())
            }
            toString()
        }

        if (tag == "") {
            return stringChildren
        }

        val stringAttrs = StringBuilder().run {
            attrs.asIterable().forEach { pair ->
                append(" ${pair.first}=\"${pair.second}\"")
            }
            toString()
        }

        return "<$tag$stringAttrs>$stringChildren</$tag>"
    }
}

inline fun SvgElementScope.element(
    tag: String,
    vararg attr: Pair<String, String>,
    block: SvgElementScope.() -> Unit
) {
    child(SvgElementImpl(
        tag,
        mutableListOf(),
        mutableListOf()
    )
        .apply {
            attr.forEach {
                this.attr(
                    it.first,
                    it.second
                )
            }
            block()
        })
}

inline fun SvgBuilderScope.element(
    tag: String,
    vararg attr: Pair<String, String>,
    modifier: SvgModifier = SvgModifier,
    block: SvgBuilderScope.() -> Unit
) {
    child(SvgBuilderElementImpl(
        tag,
        attr.toMutableList(),
        mutableListOf(),
        modifier
    )
        .apply {
            block()
        })
}

fun mkElement(tag: String): SvgFactory {
    return SvgFactory(tag)
}

class SvgFactory(val tag: String)

context(SvgElementScope)
inline operator fun SvgFactory.invoke(
    vararg attr: Pair<String, String>,
    block: SvgElementScope.() -> Unit = {}
) {
    element(tag, *attr) {
        block()
    }
}

context(SvgBuilderScope)
inline operator fun SvgFactory.invoke(
    vararg attr: Pair<String, String>,
    modifier: SvgModifier = SvgModifier,
    block: SvgBuilderScope.() -> Unit = {}
) {
    element(tag, *attr) {
        _modifier = _modifier then modifier
        block()
    }
}

object Elements {
    val a = mkElement("a")
    val altGlyph = mkElement("altGlyph")
    val altGlyphDef = mkElement("altGlyphDef")
    val altGlyphItem = mkElement("altGlyphItem")
    val animate = mkElement("animate")
    val animateColor = mkElement("animateColor")
    val animateMotion = mkElement("animateMotion")
    val animateTransform = mkElement("animateTransform")
    val animation = mkElement("animation")
    val audio = mkElement("audio")
    val canvas = mkElement("canvas")
    val circle = mkElement("circle")
    val clipPath = mkElement("clipPath")
    val color_profile = mkElement("color-profile")
    val cursor = mkElement("cursor")
    val defs = mkElement("defs")
    val desc = mkElement("desc")
    val discard = mkElement("discard")
    val ellipse = mkElement("ellipse")
    val feBlend = mkElement("feBlend")
    val feColorMatrix = mkElement("feColorMatrix")
    val feComponentTransfer = mkElement("feComponentTransfer")
    val feComposite = mkElement("feComposite")
    val feConvolveMatrix = mkElement("feConvolveMatrix")
    val feDiffuseLighting = mkElement("feDiffuseLighting")
    val feDisplacementMap = mkElement("feDisplacementMap")
    val feDistantLight = mkElement("feDistantLight")
    val feDropShadow = mkElement("feDropShadow")
    val feFlood = mkElement("feFlood")
    val feFuncA = mkElement("feFuncA")
    val feFuncB = mkElement("feFuncB")
    val feFuncG = mkElement("feFuncG")
    val feFuncR = mkElement("feFuncR")
    val feGaussianBlur = mkElement("feGaussianBlur")
    val feImage = mkElement("feImage")
    val feMerge = mkElement("feMerge")
    val feMergeNode = mkElement("feMergeNode")
    val feMorphology = mkElement("feMorphology")
    val feOffset = mkElement("feOffset")
    val fePointLight = mkElement("fePointLight")
    val feSpecularLighting = mkElement("feSpecularLighting")
    val feSpotLight = mkElement("feSpotLight")
    val feTile = mkElement("feTile")
    val feTurbulence = mkElement("feTurbulence")
    val filter = mkElement("filter")
    val font = mkElement("font")
    val font_face = mkElement("font-face")
    val font_face_format = mkElement("font-face-format")
    val font_face_name = mkElement("font-face-name")
    val font_face_src = mkElement("font-face-src")
    val font_face_uri = mkElement("font-face-uri")
    val foreignObject = mkElement("foreignObject")
    val g = mkElement("g")
    val glyph = mkElement("glyph")
    val glyphRef = mkElement("glyphRef")
    val handler = mkElement("handler")
    val hkern = mkElement("hkern")
    val iframe = mkElement("iframe")
    val image = mkElement("image")
    val line = mkElement("line")
    val linearGradient = mkElement("linearGradient")
    val listener = mkElement("listener")
    val marker = mkElement("marker")
    val mask = mkElement("mask")
    val metadata = mkElement("metadata")
    val missing_glyph = mkElement("missing-glyph")
    val mpath = mkElement("mpath")
    val path = mkElement("path")
    val pattern = mkElement("pattern")
    val polygon = mkElement("polygon")
    val polyline = mkElement("polyline")
    val prefetch = mkElement("prefetch")
    val radialGradient = mkElement("radialGradient")
    val rect = mkElement("rect")
    val script = mkElement("script")
    val set = mkElement("set")
    val solidColor = mkElement("solidColor")
    val stop = mkElement("stop")
    val style = mkElement("style")
    val svg = mkElement("svg")
    val switch = mkElement("switch")
    val symbol = mkElement("symbol")
    val tbreak = mkElement("tbreak")
    val text = mkElement("text")
    val textArea = mkElement("textArea")
    val textPath = mkElement("textPath")
    val title = mkElement("title")
    val tref = mkElement("tref")
    val tspan = mkElement("tspan")
    val unknown = mkElement("unknown")
    val use = mkElement("use")
    val video = mkElement("video")
    val view = mkElement("view")
    val vker = mkElement("vker")
}


fun fragment(block: SvgElementScope.() -> Unit): SvgElement = SvgElementImpl(
    "",
    mutableListOf(),
    mutableListOf()
).run {
    block()
    if (children.size == 1) {
        children[0]
    } else {
        SvgElementImpl("g", mutableListOf(), children)
    }
}

fun SvgElement.appendFragment(block: SvgElementScope.() -> Unit): SvgElement =
    SvgElementImpl(
        tag,
        attrs.toMutableList(),
        children.toMutableList()
    ).apply(block)
