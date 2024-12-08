package com.ei.treasure.datamapper.model

data class OldPublisher(
    val name: String = "oldName",
    val address: OldAddress = OldAddress()
)