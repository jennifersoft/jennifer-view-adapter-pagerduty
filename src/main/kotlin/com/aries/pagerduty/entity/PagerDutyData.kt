package com.aries.pagerduty.entity

import com.aries.extension.data.EventData
import org.json.JSONObject
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.*

/**
 * PagerDuty Data payload (Events API v2)
 */
class PagerDutyData(
    val prop: PagerDutyProp,
    private val event: EventData
) : JSONObject() {

    private val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX") // ISO 8601

    init {
        // --- 1. Root Fields ---
        this.put("routing_key", prop.integrationKey)
        this.put("event_action", mapAction(event))
        this.put("dedup_key", buildDedupKey(event))
        
        // Optional client info
        this.put("client", "JENNIFER5")
        this.put("client_url", prop.jenniferUrl ?: "https://www.jennifersoft.com")

        // --- 2. Payload Object ---
        val payload = JSONObject()
        payload.put("summary", buildSummary(event))
        payload.put("source", buildSource(event))
        payload.put("severity", mapSeverity(event))
        payload.put("timestamp", sdf.format(Date(event.time)))
        payload.put("component", event.instanceName)
        payload.put("group", event.domainName)
        payload.put("class", event.errorType)

        // --- 3. Custom Details (inside Payload) ---
        // Grouped for better readability (Option A style)
        val customDetails = JSONObject()
        
        val location = JSONObject()
        location.put("Domain Name", event.domainName)
        location.put("Instance Name", event.instanceName)
        location.put("Instance ID", event.instanceId)
        
        val context = JSONObject()
        context.put("Service Name", event.serviceName)
        context.put("Error Type", event.errorType)
        context.put("Event Level", event.eventLevel)
        context.put("Transaction ID", event.txid)
        
        if (event.metricsName != null && event.metricsName.isNotEmpty()) {
            context.put("Metrics", event.metricsName.toString())
        }

        customDetails.put("Location", location)
        customDetails.put("Context", context)

        payload.put("custom_details", customDetails)
        this.put("payload", payload)

        // --- 4. Links (X-View Analysis) ---
        if (!prop.jenniferUrl.isNullOrEmpty() && event.txid != -1L && event.txid != 0L) {
            val popupUrl = "/popup/xviewAnalysisV2?domainId=" + event.domainId +
                    "&transactionId=" + event.txid + "&searchTime=" + event.time
            
            // Encode the redirect URL
            val linkUrl = prop.jenniferUrl + popupUrl + "&redirect=" + encodeURIComponent(popupUrl)

            val links = org.json.JSONArray()
            val linkObj = JSONObject()
            linkObj.put("href", linkUrl)
            linkObj.put("text", "View in JENNIFER5")
            links.put(linkObj)
            
            this.put("links", links)
        }
    }

    private fun mapAction(event: EventData): String {
        return when (event.eventLevel.uppercase()) {
            "RECOVERY", "CLEAR", "NORMAL" -> "resolve"
            else -> "trigger"
        }
    }

    private fun mapSeverity(event: EventData): String {
        // PagerDuty allowed: critical, error, warning, info
        return when (event.eventLevel.uppercase()) {
            "FATAL" -> "critical"
            "CRITICAL" -> "critical"
            "WARNING" -> "warning"
            else -> "info" // Default for NORMAL or others
        }
    }

    private fun buildDedupKey(event: EventData): String {
        // Unique key to group similar events.
        // Format: domainId:instanceId:errorType:serviceName
        return "${event.domainId}:${event.instanceId}:${event.errorType}:${event.serviceName}"
    }

    private fun buildSummary(event: EventData): String {
        // Option A: Clean Standard
        // Format: ErrorType: Message (InstanceName)
        val errorType = event.errorType
        val instance = event.instanceName
        val message = if (!event.message.isNullOrBlank()) event.message else "No error message"
        
        return "$errorType: $message ($instance)"
    }

    private fun buildSource(event: EventData): String {
        return "${event.instanceName}:${event.domainName}"
    }

    companion object {
        fun encodeURIComponent(s: String): String {
            return try {
                URLEncoder.encode(s, "UTF-8")
                    .replace("+", "%20")
                    .replace("%21", "!")
                    .replace("%27", "'")
                    .replace("%28", "(")
                    .replace("%29", ")")
                    .replace("%7E", "~")
            } catch (e: UnsupportedEncodingException) {
                s
            }
        }
    }
}
