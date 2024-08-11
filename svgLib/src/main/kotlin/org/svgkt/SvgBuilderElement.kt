package org.svgkt

import org.svgkt.Elements.g

interface SvgBuilderElement {
    var tag: String
    var children: MutableList<SvgBuilderElement>
    var attrs: MutableList<Pair<String, String>>

    var modifier: SvgModifier

    fun toElement(): SvgElement = modifier.applyModifier(
        SvgElementImpl(
            tag,
            attrs,
            children.map { it.toElement() }.toMutableList()
        )
    ).let {
        if (it.tag == "" && it.children.size == 1) {
            it.children[0]
        } else {
            it
        }
    }
}

interface SvgBuilderScope {
    fun attr(tag: String, value: String)
    fun child(vararg c: SvgBuilderElement)

    // TODO - is this still needed?
    // works around context receivers as they exist
    // hopefully named context receivers will come
    @Suppress("PropertyName")
    var _modifier: SvgModifier
}

interface SvgModifier {
    companion object : SvgModifier {
        override fun applyModifier(tree: SvgElement) = tree
        override fun then(other: SvgModifier): SvgModifier = other
    }

    fun applyModifier(tree: SvgElement): SvgElement

    infix fun then(other: SvgModifier): SvgModifier =
        if (other == SvgModifier) this else CombinedSvgModifier(this, other)
}

class CombinedSvgModifier(private val first: SvgModifier, private val second: SvgModifier) : SvgModifier {
    override fun applyModifier(tree: SvgElement): SvgElement = second.applyModifier(first.applyModifier(tree))
}

class SvgBuilderElementImpl(
    override var tag: String,
    override var attrs: MutableList<Pair<String, String>>,
    override var children: MutableList<SvgBuilderElement>,
    override var modifier: SvgModifier
) : SvgBuilderElement, SvgBuilderScope {
    override var _modifier: SvgModifier
        get() = modifier
        set(new) {
            modifier = new
        }

    override fun attr(tag: String, value: String) {
        attrs.add(tag to value)
    }

    override fun child(vararg c: SvgBuilderElement) {
        children.addAll(c)
    }
}

fun buildFragment(block: SvgBuilderScope.() -> Unit): SvgBuilderElement =
    SvgBuilderElementImpl(
        "",
        mutableListOf(),
        mutableListOf(),
        SvgModifier
    ).apply(block)

fun SvgBuilderElement.appendBuildFragment(block: SvgBuilderScope.() -> Unit): SvgBuilderElement =
    SvgBuilderElementImpl(
        tag,
        attrs.toMutableList(),
        children.toMutableList(),
        modifier
    ).apply(block)


fun simpleModifier(modify: (SvgElement) -> SvgElement): SvgModifier = object : SvgModifier {
    override fun applyModifier(tree: SvgElement): SvgElement = modify(tree)
}

fun SvgModifier.rotate(degrees: Float): SvgModifier = this then simpleModifier { tree ->
    fragment {
        g("transform" to "rotate($degrees)")
        {
            child(tree)
        }
    }
}

fun SvgModifier.translate(x: Float = 0.0f, y: Float = 0.0f): SvgModifier = this then simpleModifier { tree ->
    fragment {
        g("transform" to "translate($x,$y)")
        {
            child(tree)
        }
    }
}

fun SvgModifier.opacity(opacity: Float): SvgModifier = this then simpleModifier { tree ->
    fragment {
        g("opacity" to "$opacity")
        {
            child(tree)
        }
    }
}
