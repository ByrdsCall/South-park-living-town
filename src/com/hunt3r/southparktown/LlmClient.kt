package com.hunt3r.southparktown

import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

// ============================================================
// LlmClient — unified LLM transport: Gemini or OpenRouter.
// java.net only, no android.* imports. callback(null) on ANY
// failure (callers fall back to the mock brain). The API key
// is never logged.
// ============================================================
enum class LlmProvider { GEMINI, OPENROUTER }

data class LlmConfig(
    val provider: LlmProvider,
    val model: String,
    val apiKey: String
)

object LlmClient {
    const val DEFAULT_GEMINI_MODEL = "gemini-2.5-flash"
    const val DEFAULT_OPENROUTER_MODEL = "deepseek/deepseek-v4-flash-0731:free"
    private const val OPENROUTER_ENDPOINT = "https://openrouter.ai/api/v1/chat/completions"

    fun generate(
        prompt: String,
        config: LlmConfig,
        timeoutMs: Int = 20000,
        callback: (String?) -> Unit
    ) {
        Thread {
            try {
                val (url, headers, body) = when (config.provider) {
                    LlmProvider.GEMINI -> Triple(
                        "https://generativelanguage.googleapis.com/v1beta/models/" +
                            config.model + ":generateContent",
                        mapOf("x-goog-api-key" to config.apiKey),
                        "{\"contents\":[{\"parts\":[{\"text\":" +
                            GeminiClient.jsonEscape(prompt) +
                            "}]}],\"generationConfig\":{\"temperature\":0.7,\"maxOutputTokens\":500}}"
                    )
                    LlmProvider.OPENROUTER -> Triple(
                        OPENROUTER_ENDPOINT,
                        mapOf("Authorization" to "Bearer ${config.apiKey}"),
                        buildOpenRouterBody(config.model, prompt)
                    )
                }
                val conn = (URL(url).openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    setRequestProperty("Content-Type", "application/json")
                    for ((k, v) in headers) setRequestProperty(k, v)
                    connectTimeout = timeoutMs
                    readTimeout = timeoutMs
                    doOutput = true
                }
                conn.outputStream.use {
                    it.write(body.toByteArray(StandardCharsets.UTF_8))
                }
                val code = conn.responseCode
                if (code < 200 || code > 299) {
                    callback(null)
                    return@Thread
                }
                val resp = BufferedReader(
                    InputStreamReader(conn.inputStream, StandardCharsets.UTF_8)
                ).use { it.readText() }
                callback(
                    when (config.provider) {
                        LlmProvider.GEMINI -> GeminiClient.extractModelText(resp)
                        LlmProvider.OPENROUTER -> extractOpenRouterText(resp)
                    }
                )
            } catch (e: Exception) {
                callback(null)
            }
        }.start()
    }

    /**
     * Pulls choices[0].message.content out of an OpenRouter chat-completions
     * response. Finds "choices" first, then the first "message" after it,
     * then the first "content" string after that, parsed escape-aware.
     * Null on malformed JSON or missing fields (e.g. content:null).
     */
    fun extractOpenRouterText(json: String): String? {
        val choicesIdx = json.indexOf("\"choices\"")
        if (choicesIdx < 0) return null
        val msgIdx = json.indexOf("\"message\"", choicesIdx)
        if (msgIdx < 0) return null
        val contentIdx = json.indexOf("\"content\"", msgIdx)
        if (contentIdx < 0) return null
        val colon = json.indexOf(':', contentIdx + 9)
        if (colon < 0) return null
        return GeminiClient.readJsonString(json, colon + 1)
    }

    /** Builds the OpenRouter chat-completions request body. No network. */
    fun buildOpenRouterBody(model: String, prompt: String): String =
        "{\"model\":" + GeminiClient.jsonEscape(model) +
            ",\"messages\":[{\"role\":\"user\",\"content\":" +
            GeminiClient.jsonEscape(prompt) + "}]," +
            "\"temperature\":0.7,\"max_tokens\":500}"
}
