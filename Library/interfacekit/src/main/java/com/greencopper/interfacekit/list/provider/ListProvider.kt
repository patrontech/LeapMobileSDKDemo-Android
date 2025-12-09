package com.greencopper.interfacekit.list.provider

public interface ListProvider {

    public suspend fun getElements(): List<Element>

    public data class Element(
        val id: Any,
        val order: Int? = null,
        val title: String,
        val subtitle: String? = null,
        val tags: List<String> = emptyList(),
        val image: String?,
    )
}
