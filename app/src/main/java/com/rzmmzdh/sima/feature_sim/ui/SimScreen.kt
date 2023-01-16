package com.rzmmzdh.sima.feature_sim.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.telephony.CellSignalStrength
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import androidx.compose.animation.AnimatedVisibility
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
        topBar = {
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
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(shape = CutCornerShape(topStart = 16.dp),
                onClick = {
                    context.startActivity(Intent(Settings.ACTION_DATA_ROAMING_SETTINGS))
                },
                icon = { },
                text = {
                    Text(
                        text = stringResource(R.string.select_sim),
                        fontFamily = vazir,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Normal
                    )
                })
        }) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {

            AnimatedVisibility(visible = readPhoneStatePermission.status.isGranted) {
                Sims(
                    permissionState = readPhoneStatePermission,
                    paddingValues = paddingValues,
                )
            }
            AnimatedVisibility(visible = readPhoneStatePermission.status.shouldShowRationale) {
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
                        onClick = { readPhoneStatePermission.launchPermissionRequest() },
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Text(stringResource(R.string.grant_permission))
                    }
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalPermissionsApi::class)
private fun Sims(
    permissionState: PermissionState,
    paddingValues: PaddingValues,
) {
    val context = LocalContext.current
    var sims by remember {
        mutableStateOf(
            SimInfo(
                data = getSimInfo(
                    permissionState,
                    context = context,
                ).data
            )
        )
    }
    LaunchedEffect(key1 = sims) {
        val getSimInfoPeriodicService = Executors.newSingleThreadScheduledExecutor()
        getSimInfoPeriodicService.scheduleAtFixedRate(
            {
                sims = sims.copy(data = getSimInfo(permissionState, context).data)
            },
            0,
            2,
            TimeUnit.SECONDS
        )
    }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = paddingValues.calculateTopPadding()),
        horizontalAlignment = Alignment.CenterHorizontally,
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
                shape = CutCornerShape(topStart = 24.dp),
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

                            Text(
                                modifier = Modifier,
                                text = "${sim.signalStrength}\n dBm",
                                style = TextStyle(
                                    textDirection = TextDirection.Ltr,
                                    fontFamily = vazir,
                                    fontSize = 16.sp,
                                ), textAlign = TextAlign.Center

                            )
                        }
                        Column(
                            modifier = Modifier.fillMaxHeight(),
                            verticalArrangement = Center,
                            horizontalAlignment = CenterHorizontally
                        ) {
                            Text(
                                sim.carrierName,
                                style = TextStyle(
                                    textDirection = TextDirection.Rtl,
                                    fontFamily = vazir,
                                    fontSize = 16.sp
                                )
                            )
                        }
                        Column(
                            verticalArrangement = Center,
                            horizontalAlignment = CenterHorizontally
                        ) {
                            Text(
                                sim.slotNumber.toString(),
                                style = TextStyle(
                                    fontFamily = vazir,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 22.sp
                                )
                            )
                            AnimatedVisibility(visible = (it == topSim)) {
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
                        }
                    }
                }
            }

        }
    }
}

@SuppressLint("MissingPermission")
@OptIn(ExperimentalPermissionsApi::class)
private fun getSimInfo(
    permissionState: PermissionState,
    context: Context,
): SimInfo {
    val activeSims = mutableListOf<Sim>()
    var simInfo = SimInfo()
    val subscriptionManager =
        context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
    val telephonyManager =
        context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    if (permissionState.status.isGranted) {
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
                        asuSignalToDbm(it)
                    }
                )
            )
        }
        simInfo = SimInfo(activeSims)
    }
    return simInfo
}

private fun asuSignalToDbm(signalStrength: CellSignalStrength) = (signalStrength.asuLevel * 2) - 113

data class SimInfo(val data: List<Sim> = emptyList(), val isLoading: Boolean = false)
data class Sim(
    val subId: Int = 0,
    var slotNumber: Int = 0,
    val carrierName: String = "Carrier",
    var signalStrength: Int? = 0
)