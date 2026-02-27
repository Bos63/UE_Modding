package com.urzuasset.gui.network

import com.urzuasset.gui.BuildConfig
import com.urzuasset.gui.model.KeyValidationRequest
import com.urzuasset.gui.model.KeyValidationResponse
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class KeyPanelApi(
    private val client: OkHttpClient = OkHttpClient(),
    private val json: Json = Json { ignoreUnknownKeys = true }
) {
    fun validateKey(key: String): Result<KeyValidationResponse> {
        return runCatching {
            val body = json.encodeToString(
                KeyValidationRequest.serializer(),
                KeyValidationRequest(key = key)
            ).toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url("${BuildConfig.KEY_PANEL_BASE_URL}/api/mobile/validate-key")
                .post(body)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    error("Key validation failed with status: ${response.code}")
                }
                val payload = response.body?.string().orEmpty()
                json.decodeFromString(KeyValidationResponse.serializer(), payload)
            }
        }
    }
}
