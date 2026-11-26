package id.okejack.okedriver.feature.order.presentation.components

private const val DISTANCE_UNAVAILABLE = "Jarak belum tersedia"

/**
 * The order API's `distance` is whole kilometres: the legacy app rendered it as "${distance}KM", and
 * the driver bid range (`preferred_distance`) uses the same unit. Treating it as metres showed a 2 km
 * trip as "2 m". A value of 0 means the server sent none, since the DTO defaults a missing field to 0.
 */
fun formatOrderDistance(distanceKm: Int): String =
    if (distanceKm > 0) "$distanceKm km" else DISTANCE_UNAVAILABLE
