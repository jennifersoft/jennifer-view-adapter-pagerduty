package com.aries.pagerduty.util

import com.aries.extension.util.LogUtil
import com.aries.pagerduty.entity.PagerDutyData
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

/**
 * PagerDuty Client for pushing events (Events API v2)
 */
class PagerDutyClient(
    private val pagerDutyData: PagerDutyData
) {
    companion object {
        private const val API_URL = "https://events.pagerduty.com/v2/enqueue"
        private const val CONNECT_TIMEOUT = 10000 // 10 seconds
        private const val READ_TIMEOUT = 10000    // 10 seconds
    }

    /**
     * Push event to PagerDuty
     * @return "1" if successful, empty string if failed
     */
    fun push(): String {
        var connection: HttpURLConnection? = null
        try {
            val url = URL(API_URL)
            connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.connectTimeout = CONNECT_TIMEOUT
            connection.readTimeout = READ_TIMEOUT
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
            connection.setRequestProperty("Accept", "application/json")
            connection.useCaches = false
            connection.doOutput = true

            val jsonPayload = pagerDutyData.toString()

            // Send request
            DataOutputStream(connection.outputStream).use { out ->
                out.write(jsonPayload.toByteArray(StandardCharsets.UTF_8))
                out.flush()
            }

            val responseCode = connection.responseCode

            // Handle response
            when (responseCode) {
                // PagerDuty V2 returns 202 Accepted for success
                HttpURLConnection.HTTP_ACCEPTED,
                HttpURLConnection.HTTP_OK -> {
                    val response = readResponse(connection.inputStream)
                    LogUtil.info("PagerDuty event sent successfully. Response: $response")
                    return "1"
                }
                HttpURLConnection.HTTP_BAD_REQUEST -> {
                    val errorResponse = readResponse(connection.errorStream)
                    LogUtil.error("Bad Request (400): Invalid Payload or Routing Key. Response: $errorResponse")
                    return ""
                }
                429 -> { // Too Many Requests
                    LogUtil.error("Rate limit exceeded (429): Backing off recommended.")
                    return ""
                }
                HttpURLConnection.HTTP_UNAUTHORIZED,
                HttpURLConnection.HTTP_FORBIDDEN -> {
                    LogUtil.error("Unauthorized/Forbidden ($responseCode): Check integration key.")
                    return ""
                }
                else -> {
                    val errorResponse = readResponse(connection.errorStream)
                    LogUtil.error("Unexpected response: $responseCode. Response: $errorResponse")
                    return ""
                }
            }

        } catch (ex: Exception) {
            val stackTrace = ex.stackTraceToString()
            LogUtil.error("Error pushing to PagerDuty: ${ex.message}\nStack trace: $stackTrace")
            return ""
        } finally {
            connection?.disconnect()
        }
    }

    private fun readResponse(inputStream: java.io.InputStream?): String {
        if (inputStream == null) return ""
        return try {
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                reader.readText()
            }
        } catch (e: Exception) {
            ""
        }
    }
}
