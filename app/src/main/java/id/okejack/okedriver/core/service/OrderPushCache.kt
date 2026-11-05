package id.okejack.okedriver.core.service

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Remembers the order JSON carried by a "job_received" push, so the incoming-order screen can still show the
 * offer when the server has no pending order to return (the legacy app did the same: it rendered the order
 * straight from the push and only asked the server when the push carried none).
 *
 * In memory on purpose: an offer is only worth showing for a short time, and a restarted process falls back
 * to asking the server.
 */
@Singleton
class OrderPushCache @Inject constructor() {
    private data class Entry(val json: String, val receivedAtMs: Long)

    private val entries = LinkedHashMap<Long, Entry>()

    @Synchronized
    fun put(orderId: Long, json: String, nowMs: Long = System.currentTimeMillis()) {
        if (orderId <= 0 || json.isBlank()) return
        entries.remove(orderId)
        entries[orderId] = Entry(json, nowMs)
        while (entries.size > MAX_ENTRIES) entries.remove(entries.keys.first())
    }

    /** The pushed JSON for [orderId], or null if none arrived or it is older than [MAX_AGE_MS]. */
    @Synchronized
    fun get(orderId: Long, nowMs: Long = System.currentTimeMillis()): String? {
        val entry = entries[orderId] ?: return null
        if (nowMs - entry.receivedAtMs > MAX_AGE_MS) {
            entries.remove(orderId)
            return null
        }
        return entry.json
    }

    companion object {
        /** An order offer stays open only briefly; showing an older one would mislead the driver. */
        const val MAX_AGE_MS = 120_000L
        private const val MAX_ENTRIES = 5
    }
}
