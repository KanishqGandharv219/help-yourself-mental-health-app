package com.helpyourself.com.ui.screens

import android.Manifest
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.accompanist.permissions.*
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch
import androidx.hilt.navigation.compose.hiltViewModel
import com.helpyourself.com.ui.viewmodels.NearbyViewModel

@OptIn(
  ExperimentalPermissionsApi::class,
  ExperimentalMaterial3Api::class
)
@Composable
fun TherapyNearMeScreen(
  navController: NavController,
  modifier: Modifier = Modifier,
  viewModel: NearbyViewModel = hiltViewModel()
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val fusedClient = remember {
    LocationServices.getFusedLocationProviderClient(context)
  }

  var isLoading by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf<String?>(null) }
  var showErrorSnackbar by remember { mutableStateOf(false) }
  val snackbarHostState = remember { SnackbarHostState() }
  val locationPerm = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

  // 1) Initialize SDKs & prompt for permission
  LaunchedEffect(Unit) {
    viewModel.initialize(context)
    locationPerm.launchPermissionRequest()
  }

  val cameraState = rememberCameraPositionState {
    position = CameraPosition.fromLatLngZoom(viewModel.userLocation ?: LatLng(0.0, 0.0), 13f)
  }

  // 2) When permission is granted → fetch location + places
  LaunchedEffect(locationPerm.status) {
    if (locationPerm.status is PermissionStatus.Granted) {
      isLoading = true
      errorMessage = null
      scope.launch {
        try {
          val loc = viewModel.fetchLocationAndPlaces(fusedClient)
          // Stop loading first so the map can compose before animation
          isLoading = false
          loc?.let { cameraState.animate(CameraUpdateFactory.newLatLngZoom(it, 13f)) }
        } catch (e: Exception) {
          errorMessage = e.message
          showErrorSnackbar = true
          isLoading = false
        }
      }
    }
  }

  // 3) Snackbar on errors
  LaunchedEffect(showErrorSnackbar, errorMessage) {
    if (showErrorSnackbar && errorMessage != null) {
      snackbarHostState.showSnackbar(errorMessage!!)
      showErrorSnackbar = false
    }
  }

  Scaffold(
    snackbarHost = { SnackbarHost(snackbarHostState) },
    topBar = {
      TopAppBar(
        title = { Text("Find Therapy Near Me") },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.primaryContainer,
          titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        actions = {
          IconButton(onClick = {
            if (locationPerm.status is PermissionStatus.Granted) {
              isLoading = true
              errorMessage = null
              scope.launch {
                try {
                  val loc = viewModel.fetchLocationAndPlaces(fusedClient)
                  isLoading = false
                  loc?.let { cameraState.animate(CameraUpdateFactory.newLatLngZoom(it, 13f)) }
                } catch (e: Exception) {
                  errorMessage = e.message
                  showErrorSnackbar = true
                  isLoading = false
                }
              }
            } else {
              locationPerm.launchPermissionRequest()
            }
          }) {
            Icon(
              Icons.Filled.Refresh,
              contentDescription = "Refresh",
              tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
          }
        }
      )
    }
  ) { padding ->
    Column(modifier = modifier.fillMaxSize().padding(padding)) {
      // Info Card
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.surfaceVariant,
        )
      ) {
        Column(
          modifier = Modifier.padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          // Location Row
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                Icons.Filled.MyLocation,
                contentDescription = "Location",
                tint = MaterialTheme.colorScheme.primary
              )
              Text(
                text = "Your Location",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
            Text(
              text = "${viewModel.userLocation?.latitude?.let { "%.6f".format(it) } ?: "--"}, " +
                     "${viewModel.userLocation?.longitude?.let { "%.6f".format(it) } ?: "--"}",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
          
          // Therapists Count Row
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                Icons.Filled.Person,
                contentDescription = "Therapists",
                tint = MaterialTheme.colorScheme.primary
              )
              Text(
                text = "Therapists Found",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
            Text(
              text = "${viewModel.places.size}",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }

      Box(
        modifier = Modifier.weight(1f),
        contentAlignment = Alignment.Center
      ) {
        when {
          isLoading -> CircularProgressIndicator(
            modifier = Modifier
              .size(48.dp),
            color = MaterialTheme.colorScheme.primary
          )

          viewModel.userLocation != null -> {
            GoogleMap(
              modifier = Modifier.fillMaxSize(),
              cameraPositionState = cameraState,
              properties = MapProperties(isMyLocationEnabled = true),
              uiSettings = MapUiSettings(zoomControlsEnabled = true),
              onMapLoaded = { Log.d("TherapyNearMe", "Map loaded") }
            ) {
              Marker(
                state = MarkerState(viewModel.userLocation!!),
                title = "You",
                snippet = "Your current location"
              )
              viewModel.places.forEach { p ->
                Marker(
                  state = MarkerState(p.latLng),
                  title = p.name,
                  snippet = p.address
                )
              }
            }

            if (!isLoading && viewModel.places.isEmpty()) {
              Box(
                modifier = Modifier
                  .align(Alignment.TopCenter)
                  .padding(16.dp)
              ) {
                Card(
                  colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                  )
                ) {
                  Text(
                    "No therapists found nearby",
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer
                  )
                }
              }
            }
          }

          errorMessage != null -> Column(
            Modifier
              .fillMaxSize()
              .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
          ) {
            Text(
              errorMessage!!,
              style = MaterialTheme.typography.bodyLarge,
              color = MaterialTheme.colorScheme.error
            )
            if (locationPerm.status is PermissionStatus.Denied) {
              Spacer(modifier = Modifier.height(16.dp))
              Button(
                onClick = { locationPerm.launchPermissionRequest() },
                colors = ButtonDefaults.buttonColors(
                  containerColor = MaterialTheme.colorScheme.primary
                )
              ) {
                Text("Grant Permission")
              }
            }
          }
        }
      }
    }
  }
}
