package com.hunt3r.southparktown

import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

// ============================================================
// GeminiClient — minimal Gemini REST client. java.net only,
// no android.* imports. callback(null) on ANY failure.
// ============================================================
object GeminiClient {
    private const val ENDPOINT =
        "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent"

    fun generate(
        prompt: String,
        apiKey: String,
        timeoutMs: Int = 20000,
        callback: (String?) -> Unit
    ) {
        Thread {
            try {
                val conn = (URL(ENDPOINT).openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    setRequestProperty("Content-Type", "application/json")
                    setRequestProperty("x-goog-api-key", apiKey)
                    connectTimeout = timeoutMs
                    readTimeout = timeoutMs
                    doOutput = true
                }
                val body = "{\"contents\":[{\"parts\":[{\"text\":" + jsonEscape(prompt) +
                    "}]}],\"generationConfig\":{\"temperature\":0.7,\"maxOutputTokens\":500}}"
                conn.outputStream.use {
                    it.write(body.toByteArray(StandardCharsets.UTF_8))
                }
                val code = conn.responseCode
                if (code < 200 || code > 299) {
                    callback(null)
                    return@Thread
                }
                val resp = BufferedReader(InputStreamReader(conn.inputStream, StandardCharsets.UTF_8))
                    .use { it.readText() }
                callback(extractModelText(resp))
            } catch (e: Exception) {
                callback(null)
            }
        }.start()
    }

    fun jsonEscape(s: String): String {
        val sb = StringBuilder(s.length + 16)
        sb.append('"')
        for (ch in s) {
            when (ch) {
                '"' -> sb.append("\\\"")
                '\\' -> sb.append("\\\\")
                '\n' -> sb.append("\\n")
                '\r' -> sb.append("\\r")
                '\t' -> sb.append("\\t")
                '\b' -> sb.append("\\b")
                else -> if (ch < ' ') sb.append("\\u%04x".format(ch.code)) else sb.append(ch)
            }
        }
        sb.append('"')
        return sb.toString()
    }

    /**
     * Pulls candidates[0].content.parts[0].text out of the raw JSON.
     * Finds "parts" first (skips thoughtSignature etc.), then the first
     * "text" string after it, parsed with a small escape-aware reader.
     */
    fun extractModelText(json: String): String? {
        val partsIdx = json.indexOf("\"parts\"")
        if (partsIdx < 0) return null
        val textIdx = json.indexOf("\"text\"", partsIdx)
        if (textIdx < 0) return null
        val colon = json.indexOf(':', textIdx + 6)
        if (colon < 0) return null
        return readJsonString(json, colon + 1)
    }

    /** Reads a JSON string literal starting at/after [from] (skips whitespace). Null on malformed. */
    fun readJsonString(json: String, from: Int): String? {
        var i = from
        while (i < json.length && json[i].isWhitespace()) i++
        if (i >= json.length || json[i] != '"') return null
        i++
        val sb = StringBuilder()
        while (i < json.length) {
            val ch = json[i]
            when (ch) {
                '"' -> return sb.toString()
                '\\' -> {
                    if (i + 1 >= json.length) return null
                    when (val e = json[i + 1]) {
                        '"', '\\', '/' -> sb.append(e)
                        'n' -> sb.append('\n')
                        'r' -> sb.append('\r')
                        't' -> sb.append('\t')
                        'b' -> sb.append('\b')
                        'u' -> {
                            if (i + 5 >= json.length) return null
                            val hex = json.substring(i + 2, i + 6)
                            val code = hex.toIntOrNull(16) ?: return null
                            sb.append(code.toChar())
                            i += 4
                        }
                        else -> sb.append(e)
                    }
                    i += 2
                }
                else -> {
                    sb.append(ch)
                    i++
                }
            }
        }
        return null
    }
}
