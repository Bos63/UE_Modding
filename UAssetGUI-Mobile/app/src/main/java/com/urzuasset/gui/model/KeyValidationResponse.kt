package com.urzuasset.gui.model

import kotlinx.serialization.Serializable

@Serializable
data class KeyValidationResponse(
    val valid: Boolean,
    val userName: String? = null,
    val expiresAt: String? = null,
    val tier: String? = null
)
