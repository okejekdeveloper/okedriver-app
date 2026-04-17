package com.application.okedriver.ui.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

/** Default center: Central Jakarta, Indonesia */
private val DEFAULT_CENTER = GeoPoint(-6.2000, 106.8166)

/**
 * OpenStreetMap composable powered by osmdroid.
 *
 * ✅ No API key required — completely free
 * - Shows the driver's live position once location permission is granted
 * - Tiles served from OpenStreetMap (tile.openstreetmap.org)
 * - Multi-touch zoom + pan enabled
 */
@Composable
fun OkeDriverMap(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    // ── Location permission state ─────────────────────────────────────────────
    var locationGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        locationGranted =
            result[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    LaunchedEffect(Unit) {
        if (!locationGranted) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    // ── osmdroid MapView ──────────────────────────────────────────────────────
    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            // Configure osmdroid cache directory (no WRITE_EXTERNAL_STORAGE needed)
            Configuration.getInstance().apply {
                load(ctx, ctx.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
                userAgentValue = ctx.packageName
                osmdroidBasePath = ctx.cacheDir
            }

            MapView(ctx).apply {
                setTileSource(TileSourceFactory.MAPNIK) // OpenStreetMap tiles
                setMultiTouchControls(true)
                isHorizontalMapRepetitionEnabled = false
                isVerticalMapRepetitionEnabled   = false

                // Hide default zoom controls (we keep our own UI)
                zoomController.setVisibility(
                    org.osmdroid.views.CustomZoomButtonsController.Visibility.NEVER
                )

                // Set initial position: Jakarta
                controller.setZoom(15.0)
                controller.setCenter(DEFAULT_CENTER)

                // ── My Location overlay ───────────────────────────────────
                if (ContextCompat.checkSelfPermission(
                        ctx, Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    val locationOverlay = MyLocationNewOverlay(
                        GpsMyLocationProvider(ctx),
                        this
                    )
                    locationOverlay.enableMyLocation()
                    locationOverlay.enableFollowLocation() // camera follows driver
                    overlays.add(locationOverlay)
                }
            }
        },
        update = { mapView ->
            // Re-add location overlay when permission is freshly granted
            if (locationGranted && mapView.overlays.none { it is MyLocationNewOverlay }) {
                val locationOverlay = MyLocationNewOverlay(
                    GpsMyLocationProvider(mapView.context),
                    mapView
                )
                locationOverlay.enableMyLocation()
                locationOverlay.enableFollowLocation()
                mapView.overlays.add(locationOverlay)
                mapView.invalidate()
            }
        }
    )
}
