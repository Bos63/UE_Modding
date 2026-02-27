package com.urzuasset.gui.model

import kotlinx.serialization.Serializable

@Serializable
data class KeyValidationRequest(
    val key: String
)
