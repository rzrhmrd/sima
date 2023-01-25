package com.rzmmzdh.sima.feature_sim.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.telephony.CellSignalStrength.*
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement.Center
import androidx.compose.foundation.layout.Arrangement.SpaceEvenly
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
import com.rzmmzdh.sima.feature_sim.model.Sim
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
    val fabVisibility = remember {
        mutableStateOf(
            readPhoneStatePermission.status.isGranted
        )
    }
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = { SimaAppBar() },
        floatingActionButton = {
            SelectSimFab { context.startActivity(Intent(Settings.ACTION_DATA_ROAMING_SETTINGS)) }
        }) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            AnimatedVisibility(visible = readPhoneStatePermission.status.shouldShowRationale || !readPhoneStatePermission.status.isGranted) {
                GrantPermissionRationale {
                    readPhoneStatePermission.launchPermissionRequest()
                }
            }
            AnimatedVisibility(visible = readPhoneStatePermission.status.isGranted) {
                Sims(
                    isPermissionGranted = readPhoneStatePermission.status.isGranted,
                    paddingValues = paddingValues
                )
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

@OptIn(ExperimentalFoundationApi::class)
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
        val simsSortedByStrength = sims.data.sortedByDescending { it.signalStrength }
        items(simsSortedByStrength, key = { it.slotNumber }) { sim ->
            val topSim = simsSortedByStrength.first()
            val topSimBackgroundColor =
                if (sim == topSim) MaterialTheme.colorScheme.tertiaryContainer
                else MaterialTheme.colorScheme.surfaceVariant

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .height(136.dp)
                    .animateItemPlacement(),
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
                        SignalStrength(it.signalStrength)
                        Column(
                            verticalArrangement = SpaceEvenly,
                            horizontalAlignment = CenterHorizontally
                        ) {
                            Carrier(it.carrierName)
                            NetworkType(networkTypeName(it.networkType))
                        }
                        Column(
                            verticalArrangement = Center,
                            horizontalAlignment = CenterHorizontally
                        ) {
                            SlotNumber(number = it.slotNumber)
                            QualityLevel(qualityLevelName(it.qualityLevel), it == topSim)
                        }
                    }
                }
            }

        }
        item {
            Divider(
                modifier = Modifier.padding(horizontal = 34.dp, vertical = 16.dp),
                thickness = 1.dp
            )
            Description()
        }
    }
}

@Composable
fun NetworkType(networkType: String) {
    Text(networkType, fontSize = 16.sp)
}

private fun networkTypeName(type: Int) =
    when (type) {
        TelephonyManager.NETWORK_TYPE_GPRS -> "GPRS"
        TelephonyManager.NETWORK_TYPE_EDGE -> "EDGE"
        TelephonyManager.NETWORK_TYPE_UMTS -> "UMTS"
        TelephonyManager.NETWORK_TYPE_CDMA -> "CDMA"
        TelephonyManager.NETWORK_TYPE_EVDO_0 -> "EVDO revision 0"
        TelephonyManager.NETWORK_TYPE_EVDO_A -> "EVDO revision A"
        TelephonyManager.NETWORK_TYPE_1xRTT -> "1xRTT"
        TelephonyManager.NETWORK_TYPE_HSDPA -> "HSDPA"
        TelephonyManager.NETWORK_TYPE_HSUPA -> "HSUPA"
        TelephonyManager.NETWORK_TYPE_HSPA -> "HSPA"
        TelephonyManager.NETWORK_TYPE_IDEN -> "IDEN"
        TelephonyManager.NETWORK_TYPE_EVDO_B -> "EVDO revision B"
        TelephonyManager.NETWORK_TYPE_LTE -> "LTE"
        TelephonyManager.NETWORK_TYPE_EHRPD -> "eHRPD"
        TelephonyManager.NETWORK_TYPE_HSPAP -> "HSPA+"
        TelephonyManager.NETWORK_TYPE_GSM -> "GSM"
        TelephonyManager.NETWORK_TYPE_TD_SCDMA -> "TD_SDMA"
        TelephonyManager.NETWORK_TYPE_IWLAN -> "IWLAN"
        TelephonyManager.NETWORK_TYPE_NR -> "NR (New Radio) 5G"
        TelephonyManager.NETWORK_TYPE_UNKNOWN -> "unkown"
        else -> "unkown"
    }

@Composable
private fun SlotNumber(
    number: Int,
) {
    Text(
        modifier = Modifier,
        text = number.toString(),
        style = TextStyle(
            fontFamily = vazir,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp, textAlign = TextAlign.Center
        )
    )
}

@Composable
fun QualityLevel(level: String, isTopSim: Boolean) {
    val borderColor =
        if (isTopSim) MaterialTheme.colorScheme.onTertiaryContainer
        else MaterialTheme.colorScheme.onSurfaceVariant
    Text(
        level,
        modifier = Modifier
            .border(
                border = BorderStroke(
                    1.dp,
                    color = borderColor
                ), shape = CutCornerShape(bottomStart = 8.dp)
            )
            .padding(8.dp),
        fontSize = 16.sp,
    )
}

private fun qualityLevelName(level: Int?) =
    when (level) {
        SIGNAL_STRENGTH_GOOD -> "خوب"
        SIGNAL_STRENGTH_GREAT -> "عالی"
        SIGNAL_STRENGTH_MODERATE -> "متوسط"
        SIGNAL_STRENGTH_NONE_OR_UNKNOWN -> "قطع"
        SIGNAL_STRENGTH_POOR -> "ضعیف"
        else -> "نامشخص"
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
        text = "${value}\n dBm",
        style = TextStyle(
            textDirection = TextDirection.Ltr,
            fontFamily = vazir,
            fontSize = 16.sp,
        ), textAlign = TextAlign.Center

    )
}

@Composable
private fun Description() {
    Text(
        text = stringResource(R.string.description),
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .alpha(0.5f),
        textAlign = TextAlign.Center,
        style = TextStyle(
            textDirection = TextDirection.Rtl,
            fontFamily = vazir,
            fontWeight = FontWeight.Light,
            fontSize = 12.sp,
        )
    )
}

@Composable
private fun GrantPermissionRationale(onGrantPermission: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Center,
        horizontalAlignment = CenterHorizontally,
    ) {
        Text(
            stringResource(R.string.permission_rationale),
            style = TextStyle(
                textDirection = TextDirection.Rtl,
                fontFamily = vazir,
                fontSize = 16.sp, textAlign = TextAlign.Center
            )
        )
        OutlinedButton(
            onClick = { onGrantPermission() },
            modifier = Modifier
                .padding(8.dp).fillMaxWidth(),
            shape = CutCornerShape(bottomStart = 16.dp),
        ) {
            Text(
                stringResource(R.string.grant_permission),
                style = TextStyle(
                    fontFamily = vazir,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )
            )
        }
    }
}

@Composable
private fun SelectSimFab(onClick: () -> Unit) {
    ExtendedFloatingActionButton(shape = CutCornerShape(bottomStart = 16.dp),
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
                    networkType = telephonyManager.createForSubscriptionId(sub.subscriptionId).dataNetworkType,
                    qualityLevel = telephonyManager.createForSubscriptionId(sub.subscriptionId)
                        .signalStrength?.cellSignalStrengths?.firstNotNullOf { it.level },
                    signalStrength = telephonyManager.createForSubscriptionId(sub.subscriptionId)
                        .signalStrength?.cellSignalStrengths?.firstNotNullOf { it.dbm }
                )
            )
        }
        simStatus = simStatus.copy(data = activeSims)
    }
    return simStatus
}