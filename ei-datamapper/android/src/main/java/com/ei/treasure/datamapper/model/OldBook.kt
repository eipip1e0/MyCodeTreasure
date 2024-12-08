package com.ei.treasure.datamapper.model

data class OldBook(
    val name: String = "oldName",
    val id: Int = -1,
    val publisher: OldPublisher = OldPublisher()
)