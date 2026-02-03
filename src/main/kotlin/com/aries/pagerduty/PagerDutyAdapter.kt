package com.aries.pagerduty

import com.aries.extension.data.EventData
import com.aries.extension.handler.EventHandler
import com.aries.extension.util.LogUtil
import com.aries.pagerduty.entity.PagerDutyData
import com.aries.pagerduty.util.ConfUtil
import com.aries.pagerduty.util.PagerDutyClient
import java.util.*

/**
 * PagerDuty Event Adapter for JENNIFER
 */
class PagerDutyAdapter : EventHandler {

    override fun on(eventData: Array<EventData>) {
        val properties = ConfUtil.getPagerDutyProperties()

        // Validation
        if (properties.integrationKey.isNullOrEmpty()) {
            LogUtil.error("PagerDuty integration_key is not configured.")
            return
        }

        for (event in eventData) {
            try {
                // Build payload
                val pdData = PagerDutyData(properties, event)
                
                // Send event
                PagerDutyClient(pdData).push()

            } catch (e: Exception) {
                LogUtil.error("Error processing event for PagerDuty: ${e.message}")
            }
        }
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            // Test locally
            val event = EventData(
                1004.toShort(),
                ArrayList(),
                "Jennifer",
                System.currentTimeMillis(),
                1000,
                "Groupware",
                "",
                "SERVICE_EXCEPTION",
                "",
                "FATAL",
                "High CPU Usage",
                95.0,
                "SYSTEM",
                "",
                "/service.jsp",
                123456789L,
                "Details about the error...",
                null
            )
            
            PagerDutyAdapter().on(arrayOf(event))
        }
    }
}
