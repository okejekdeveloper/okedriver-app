package id.okejack.okedriver.core.service

object LocationTrackingPolicy {
    const val DISCLOSURE_VERSION = 1

    fun canAccess(acceptedVersion: Int, permissionGranted: Boolean): Boolean =
        acceptedVersion == DISCLOSURE_VERSION && permissionGranted

    fun canTrack(acceptedVersion: Int, permissionGranted: Boolean, requested: Boolean, online: Boolean, token: String): Boolean =
        canAccess(acceptedVersion, permissionGranted) && requested && online && token.isNotBlank()
}
