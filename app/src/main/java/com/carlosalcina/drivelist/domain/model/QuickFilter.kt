package com.carlosalcina.drivelist.domain.model

data class QuickFilter(
    val id: String,
    val displayText: String,
    val type: QuickFilterType,
    val value: Any,
    val fieldToUpdate: String? = null
)