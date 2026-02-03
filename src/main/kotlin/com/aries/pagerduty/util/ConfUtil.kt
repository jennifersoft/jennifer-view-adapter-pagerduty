package com.aries.pagerduty.util

import com.aries.extension.util.PropertyUtil
import com.aries.pagerduty.entity.PagerDutyProp

/**
 * Load adapter configuration
 */
object ConfUtil {
    private val pagerDutyProperties = PagerDutyProp()

    /**
     * The adapter ID
     */
    private const val ADAPTER_ID = "pagerduty"

    /**
     * Get a configuration value using the provided key
     * @param key configuration key
     * @param defaultValue Optional default configuration value
     * @return String? configuration value
     */
    fun getValue(key: String?, defaultValue: String?): String? {
        return try {
            PropertyUtil.getValue(ADAPTER_ID, key, defaultValue)
        } catch (e: Exception) {
            defaultValue
        }
    }

    /**
     * Get the PagerDuty properties
     * @return PagerDutyProp
     */
    fun getPagerDutyProperties(): PagerDutyProp {
        pagerDutyProperties.integrationKey = getValue("integration_key", null)
        pagerDutyProperties.jenniferUrl = getValue("jennifer_url", null)
        return pagerDutyProperties
    }
}
