package com.greencopper.core.content.manager

import kotlinx.coroutines.flow.*

internal class ValidContentHistory(
    override val currentProject: String? = null,
    override val currentProjectFlow: SharedFlow<String?> = MutableSharedFlow(replay = 1),
    currentContent: Content? = null,
    override var forcedContent: Content? = null,
    override val contents: Set<Content> = emptySet(),
    val saveContentValue: (Content) -> Unit = {},
    val saveHistoryValue: () -> Unit = {},
) : ContentHistory {

    private val _currentContent = MutableStateFlow<Content?>(null)

    init {
        _currentContent.value = currentContent
    }

    override var currentContent: Content?
        get() = _currentContent.value
        set(value) {
            _currentContent.value = value
        }

    override val currentContentFlow: Flow<Content?>
        get() = _currentContent

    override suspend fun saveContent(content: Content) = saveContentValue(content)

    override suspend fun saveHistory() = saveHistoryValue()
}
