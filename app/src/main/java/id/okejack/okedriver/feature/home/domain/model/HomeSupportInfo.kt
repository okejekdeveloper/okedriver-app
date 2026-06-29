package id.okejack.okedriver.feature.home.domain.model

data class HomeSupportInfo(
    val motd: String = "",
    val queueEnabled: Boolean = false,
    val hasAttended: Boolean = false,
    val queueNumber: Int? = null,
    val queueTerminal: String? = null,
    val activeOrderId: Long? = null,
    val latestVersionCode: Int = 0,
    val latestVersionName: String = "",
    val paymentBiddingRange: List<Int> = emptyList(),
    val distanceBiddingRange: List<Int> = emptyList()
) {
    val hasQueueInfo: Boolean
        get() = queueEnabled

    val hasActiveOrder: Boolean
        get() = activeOrderId != null && activeOrderId > 0

    fun hasAppUpdate(currentVersionCode: Long): Boolean =
        latestVersionCode > currentVersionCode
}
