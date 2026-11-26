package com.streamwarrior.app

import android.app.Activity
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.streamwarrior.app.ai.configureAppCheck
import com.streamwarrior.app.data.CardDeckKind
import com.streamwarrior.app.ui.AppRoute
import com.streamwarrior.app.ui.StreamWarriorViewModel
import com.streamwarrior.app.ui.VoiceAiViewModel
import com.streamwarrior.app.ui.components.StreamHelpDialog
import com.streamwarrior.app.ui.components.StreamSettingsDialog
import com.streamwarrior.app.ui.components.StreamShell
import com.streamwarrior.app.ui.screens.CardDeckScreen
import com.streamwarrior.app.ui.screens.CoachDetailScreen
import com.streamwarrior.app.ui.screens.CoachesHubScreen
import com.streamwarrior.app.ui.screens.DenToolkitScreen
import com.streamwarrior.app.ui.screens.HomeScreen
import com.streamwarrior.app.ui.screens.MadLibsScreen
import com.streamwarrior.app.ui.screens.ProphetScreen
import com.streamwarrior.app.ui.screens.SoundboardScreen
import com.streamwarrior.app.ui.screens.StreamConnectScreen
import com.streamwarrior.app.ui.screens.TarotScreen
import com.streamwarrior.app.ui.screens.TriviaScreen
import com.streamwarrior.app.ui.screens.TwentyQuestionsScreen
import com.streamwarrior.app.ui.screens.VoiceAiScreen
import com.streamwarrior.app.ui.screens.WeirdLawsScreen
import com.streamwarrior.app.ui.screens.WheelOfChaosScreen
import com.streamwarrior.app.ui.theme.StreamWarriorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Configure Firebase App Check with launch intent before any UI or ViewModels
        configureAppCheck(this, intent)

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        val app = application as StreamWarriorApplication
        setContent {
            StreamWarriorTheme {
                val vm: StreamWarriorViewModel = viewModel(
                    factory = StreamWarriorViewModel.factory(app.settingsRepository)
                )
                val voiceVm: VoiceAiViewModel = viewModel(
                    factory = VoiceAiViewModel.factory(app)
                )
                StreamWarriorAppContent(vm = vm, voiceVm = voiceVm)
            }
        }
    }
}

@Composable
private fun StreamWarriorAppContent(
    vm: StreamWarriorViewModel,
    voiceVm: VoiceAiViewModel
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val dest = backStackEntry?.destination

    val streamerName by vm.streamerName.collectAsStateWithLifecycle()
    val aiConfig by vm.aiConfig.collectAsStateWithLifecycle()
    val customSounds by vm.customSounds.collectAsStateWithLifecycle()
    val forgeTools by vm.forgeTools.collectAsStateWithLifecycle()
    val denConfig by vm.denConfig.collectAsStateWithLifecycle()
    val weirdLawScore by vm.weirdLawScore.collectAsStateWithLifecycle()
    val barsHidden by vm.barsHidden.collectAsStateWithLifecycle()
    val showHelp by vm.showHelpDialog.collectAsStateWithLifecycle()
    val showSettings by vm.showSettingsDialog.collectAsStateWithLifecycle()
    val aiTestStatus by vm.aiTestStatus.collectAsStateWithLifecycle()
    val soundUploadStatus by vm.soundUploadStatus.collectAsStateWithLifecycle()
    val activeDenAlert by vm.activeDenAlert.collectAsStateWithLifecycle()
    val streamConnectionConfig by vm.streamConnectionConfig.collectAsStateWithLifecycle()
    val overlayRunning by vm.overlayRunning.collectAsStateWithLifecycle()
    val overlayPort by vm.overlayPort.collectAsStateWithLifecycle()
    val localIpAddress by vm.localIpAddress.collectAsStateWithLifecycle()
    val overlayError by vm.overlayError.collectAsStateWithLifecycle()

    val hostActivity = LocalContext.current as? Activity
    DisposableEffect(hostActivity, streamConnectionConfig.keepScreenAwake, barsHidden, overlayRunning) {
        val window = hostActivity?.window
        val shouldKeepAwake = streamConnectionConfig.keepScreenAwake || barsHidden || overlayRunning
        if (shouldKeepAwake) {
            window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    val currentModId = when {
        dest == null || dest.hasRoute<AppRoute.Home>() -> "home"
        dest.hasRoute<AppRoute.StreamConnect>() -> "connect"
        dest.hasRoute<AppRoute.VoiceAi>() -> "voice_ai"
        dest.hasRoute<AppRoute.Wheel>() -> "wheel"
        dest.hasRoute<AppRoute.Tarot>() -> "tarot"
        dest.hasRoute<AppRoute.TwentyQ>() -> "twentyq"
        dest.hasRoute<AppRoute.Trivia>() -> "trivia"
        dest.hasRoute<AppRoute.Soundboard>() -> "soundboard"
        dest.hasRoute<AppRoute.MadLibs>() -> "madlibs"
        dest.hasRoute<AppRoute.WeirdLaws>() -> "laws"
        dest.hasRoute<AppRoute.Prophet>() -> "prophet"
        dest.hasRoute<AppRoute.Den>() -> "den"
        dest.hasRoute<AppRoute.CoachesHub>() || dest.hasRoute<AppRoute.CoachDetail>() -> "coaches"
        dest.hasRoute<AppRoute.CardDeck>() -> {
            val args = runCatching { backStackEntry?.toRoute<AppRoute.CardDeck>() }.getOrNull()
            when (args?.kind) {
                CardDeckKind.JOKES -> "jokes"
                CardDeckKind.LINES -> "lines"
                CardDeckKind.FACTS -> "facts"
                CardDeckKind.WYR -> "wyr"
                null -> "jokes"
            }
        }
        else -> "home"
    }

    fun navigateTopLevel(route: AppRoute) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWide = maxWidth >= 680.dp

        StreamShell(
            currentModId = currentModId,
            isAiConnected = aiConfig.isEnabled,
            isOverlayLive = overlayRunning,
            barsHidden = barsHidden,
            isWideLayout = isWide,
            onSelectMod = { mod -> navigateTopLevel(mod.route) },
            onOpenStreamConnect = { navigateTopLevel(AppRoute.StreamConnect) },
            onToggleBarsHidden = vm::setBarsHidden,
            onOpenHelp = { vm.openHelpDialog(true) },
            onOpenSettings = { vm.openSettingsDialog(true) }
        ) {
            NavHost(
                navController = navController,
                startDestination = AppRoute.Home
            ) {
                composable<AppRoute.Home> {
                    HomeScreen(
                        streamerName = streamerName,
                        isWideLayout = isWide,
                        onNavigate = { route -> navigateTopLevel(route) }
                    )
                }
                composable<AppRoute.StreamConnect> {
                    StreamConnectScreen(
                        streamerName = streamerName,
                        connectionConfig = streamConnectionConfig,
                        overlayRunning = overlayRunning,
                        overlayPort = overlayPort,
                        localIpAddress = localIpAddress,
                        overlayError = overlayError,
                        onSaveConnectionConfig = vm::saveStreamConnectionConfig,
                        onToggleOverlayServer = vm::setOverlayServerEnabled,
                        onRefreshNetwork = vm::refreshNetworkInfo,
                        onSendTestAlert = {
                            vm.fireDenAlert(
                                kind = "sub",
                                viewerName = "StreamWarriorVIP",
                                message = "Connected live on stream!",
                                amount = ""
                            )
                        },
                        onAudioCheck = voiceVm::speakAudioCheck,
                        onStartCleanBroadcast = { route ->
                            vm.setBarsHidden(true)
                            navigateTopLevel(route)
                        }
                    )
                }
                composable<AppRoute.VoiceAi> {
                    VoiceAiScreen(
                        streamerName = streamerName,
                        vm = voiceVm
                    )
                }
                composable<AppRoute.Wheel> {
                    WheelOfChaosScreen(
                        onLaunchModule = { route -> navigateTopLevel(route) }
                    )
                }
                composable<AppRoute.Tarot> {
                    TarotScreen(
                        aiConfig = aiConfig,
                        onNavigateTool = { key ->
                            when {
                                key == "prophet" -> navigateTopLevel(AppRoute.Prophet)
                                key == "laws" -> navigateTopLevel(AppRoute.WeirdLaws)
                                key.startsWith("coach:") -> {
                                    navController.navigate(AppRoute.CoachDetail(key.removePrefix("coach:")))
                                }
                            }
                        }
                    )
                }
                composable<AppRoute.TwentyQ> {
                    TwentyQuestionsScreen(aiConfig = aiConfig)
                }
                composable<AppRoute.Trivia> {
                    TriviaScreen(aiConfig = aiConfig)
                }
                composable<AppRoute.Soundboard> {
                    SoundboardScreen(
                        customSounds = customSounds,
                        isWideLayout = isWide,
                        onOpenSettingsToAddSound = { vm.openSettingsDialog(true) },
                        onDeleteCustomSound = vm::deleteCustomSound
                    )
                }
                composable<AppRoute.MadLibs> {
                    MadLibsScreen()
                }
                composable<AppRoute.CardDeck> { entry ->
                    val args = entry.toRoute<AppRoute.CardDeck>()
                    CardDeckScreen(kind = args.kind, aiConfig = aiConfig)
                }
                composable<AppRoute.WeirdLaws> {
                    WeirdLawsScreen(
                        quizScore = weirdLawScore,
                        onRecordQuizAnswer = vm::recordWeirdLawAnswer
                    )
                }
                composable<AppRoute.Prophet> {
                    ProphetScreen(aiConfig = aiConfig)
                }
                composable<AppRoute.Den> {
                    DenToolkitScreen(
                        denConfig = denConfig,
                        activeAlert = activeDenAlert,
                        onSaveDenConfig = vm::saveDenConfig,
                        onFireAlert = vm::fireDenAlert,
                        onDismissAlert = vm::dismissDenAlert,
                        onOpenStreamConnect = { navigateTopLevel(AppRoute.StreamConnect) }
                    )
                }
                composable<AppRoute.CoachesHub> {
                    CoachesHubScreen(
                        customTools = forgeTools,
                        onOpenCoach = { coachId ->
                            navController.navigate(AppRoute.CoachDetail(coachId))
                        },
                        onSaveCustomTool = vm::upsertForgeTool,
                        onDeleteCustomTool = vm::deleteForgeTool
                    )
                }
                composable<AppRoute.CoachDetail> { entry ->
                    val args = entry.toRoute<AppRoute.CoachDetail>()
                    CoachDetailScreen(
                        coachId = args.coachId,
                        customTools = forgeTools,
                        aiConfig = aiConfig,
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }

        if (showHelp) {
            StreamHelpDialog(onDismiss = { vm.openHelpDialog(false) })
        }

        if (showSettings) {
            StreamSettingsDialog(
                streamerName = streamerName,
                aiConfig = aiConfig,
                customSounds = customSounds,
                aiTestStatus = aiTestStatus,
                soundUploadStatus = soundUploadStatus,
                onSaveStreamerName = vm::saveStreamerName,
                onSaveAiConfig = vm::saveAiConfig,
                onTestAiConnection = vm::testAiConnection,
                onImportSound = vm::importCustomSound,
                onDeleteCustomSound = vm::deleteCustomSound,
                onDismiss = { vm.openSettingsDialog(false) }
            )
        }
    }
}
