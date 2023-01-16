package com.rzmmzdh.sima.feature_sim.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement.Center
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.shouldShowRationale
import com.rzmmzdh.sima.R
import com.rzmmzdh.sima.feature_sim.core.theme.vazir
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun SimScreen(
    readPhoneStatePermission: PermissionState,
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val snackbarScope = rememberCoroutineScope()
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = { SimaAppBar() },
        floatingActionButton = {
            SelectSimFab {
                context.startActivity(Intent(Settings.ACTION_DATA_ROAMING_SETTINGS))
            }
        }) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            AnimatedVisibility(visible = readPhoneStatePermission.status.isGranted) {
                Sims(
                    isPermissionGranted = readPhoneStatePermission.status.isGranted,
                    paddingValues = paddingValues
                )
            }
            AnimatedVisibility(visible = readPhoneStatePermission.status.shouldShowRationale) {
                GrantPermissionRationale { readPhoneStatePermission.launchPermissionRequest() }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun SimaAppBar() {
    CenterAlignedTopAppBar(
        modifier = Modifier,
        title = {
            Text(
                stringResource(R.string.sima),
                fontFamily = vazir,
                fontWeight = FontWeight.Black,
                fontSize = 24.sp
            )
        })
}

@Composable
private fun Sims(
    isPermissionGranted: Boolean,
    paddingValues: PaddingValues,
) {
    val context = LocalContext.current
    var sims by remember {
        mutableStateOf(
            SimStatusUiState(
                data = simStatus(
                    isPermissionGranted = isPermissionGranted,
                    context = context,
                ).data
            )
        )
    }
    LaunchedEffect(key1 = sims) {
        val initialDelay: Long = 0
        val refreshRate: Long = 2
        val simStatusPeriodicService = Executors.newSingleThreadScheduledExecutor()
        simStatusPeriodicService.scheduleAtFixedRate(
            {
                sims = sims.copy(
                    data = simStatus(
                        isPermissionGranted = isPermissionGranted,
                        context = context
                    ).data
                )
            },
            initialDelay,
            refreshRate,
            TimeUnit.SECONDS
        )
    }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = paddingValues.calculateTopPadding()),
        horizontalAlignment = CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        items(sims.data) { sim ->
            val topSim = sims.data.sortedByDescending { it.signalStrength }.first()
            val topSimBackgroundColor =
                if (sim == topSim) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.surfaceVariant

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .height(136.dp),
                shape = CutCornerShape(bottomStart = 24.dp),
                colors = CardDefaults.cardColors(containerColor = topSimBackgroundColor)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    sim.let {
                        Column(
                            modifier = Modifier.fillMaxHeight(),
                            verticalArrangement = Center,
                            horizontalAlignment = CenterHorizontally
                        ) {
                            SignalStrength(it.signalStrength)
                        }
                        Column(
                            modifier = Modifier.fillMaxHeight(),
                            verticalArrangement = Center,
                            horizontalAlignment = CenterHorizontally
                        ) {
                            Carrier(it.carrierName)
                        }
                        Column(
                            verticalArrangement = Center,
                            horizontalAlignment = CenterHorizontally
                        ) {
                            SlotNumber(it.slotNumber)
                            AnimatedVisibility(visible = (it == topSim)) {
                                TopSimStar()
                            }
                        }
                    }
                }
            }

        }
    }
}

@Composable
private fun SlotNumber(number: Int) {
    Text(
        number.toString(),
        style = TextStyle(
            fontFamily = vazir,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp
        )
    )
}

@Composable
private fun Carrier(name: String) {
    Text(
        name,
        style = TextStyle(
            textDirection = TextDirection.Rtl,
            fontFamily = vazir,
            fontSize = 16.sp
        )
    )
}

@Composable
private fun SignalStrength(value: Int?) {
    Text(
        modifier = Modifier
            .border(
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ), shape = CutCornerShape(topStart = 16.dp)
            )
            .padding(8.dp),
        text = "${value}\n dBm",
        style = TextStyle(
            textDirection = TextDirection.Ltr,
            fontFamily = vazir,
            fontSize = 16.sp,
        ), textAlign = TextAlign.Center

    )
}

@Composable
private fun TopSimStar() {
    Text(
        modifier = Modifier,
        text = "⭐",
        style = TextStyle(
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
            fontFamily = vazir
        )
    )
}

@Composable
private fun GrantPermissionRationale(onGrantPermission: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Center,
        horizontalAlignment = CenterHorizontally,
    ) {
        Text(
            stringResource(R.string.permission_rationale),
            style = TextStyle(
                textDirection = TextDirection.Rtl,
                fontFamily = vazir,
                fontSize = 16.sp
            )
        )
        Button(
            onClick = { onGrantPermission() },
            modifier = Modifier.padding(8.dp)
        ) {
            Text(stringResource(R.string.grant_permission))
        }
    }
}

@Composable
private fun SelectSimFab(onClick: () -> Unit) {
    ExtendedFloatingActionButton(shape = CutCornerShape(bottomStart = 20.dp),
        onClick = { onClick() },
        icon = { },
        text = {
            Text(
                text = stringResource(R.string.select_sim),
                fontFamily = vazir,
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal
            )
        })
}

@SuppressLint("MissingPermission")
private fun simStatus(
    isPermissionGranted: Boolean,
    context: Context,
): SimStatusUiState {
    val activeSims = mutableListOf<Sim>()
    var simStatus = SimStatusUiState()
    val subscriptionManager =
        context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
    val telephonyManager =
        context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    if (isPermissionGranted) {
        val subList =
            subscriptionManager.activeSubscriptionInfoList
        for (sub in subList) {
            activeSims.add(
                Sim(
                    subId = sub.subscriptionId,
                    slotNumber = sub.simSlotIndex + 1,
                    carrierName = sub.carrierName.toString(),
                    signalStrength = telephonyManager.createForSubscriptionId
                        (sub.subscriptionId).signalStrength?.cellSignalStrengths?.firstNotNullOf {
                        asuLevelToDbm(it.asuLevel)
                    }
                )
            )
        }
        simStatus = simStatus.copy(data = activeSims)
    }
    return simStatus
}

private fun asuLevelToDbm(asuLevel: Int) = (asuLevel * 2) - 113