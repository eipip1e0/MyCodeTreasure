package com.ei.treasure.datamapper.model

data class Book(
    val name: String = "new",
    val id: Int = -1,
    val publisher: Publisher = Publisher()
)