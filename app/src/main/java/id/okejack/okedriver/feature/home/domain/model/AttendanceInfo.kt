package id.okejack.okedriver.feature.home.domain.model

data class AttendanceInfo(
    val hasAttended: Boolean = false,
    val queueNumber: Int? = null,
    val queueTerminal: String? = null,
    val queueTerminalTotal: Int? = null,
    val queueMessage: String = ""
) {
    val hasQueueNumber: Boolean
        get() = queueNumber != null && queueNumber > 0
}
