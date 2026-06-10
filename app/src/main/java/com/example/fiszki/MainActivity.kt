package com.example.fiszki

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.drawscope.Fill
import kotlinx.coroutines.delay
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import com.example.fiszki.ui.theme.*
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.google.zxing.EncodeHintType
import java.io.File
import java.io.FileOutputStream
import java.util.*


data class Flashcard(val pojecie: String, val definicja: String, val wrongCount: Int = 0)
data class FlashcardSet(val id: String = UUID.randomUUID().toString(), val name: String, val cards: List<Flashcard>)
data class FlashcardDraft(val pojecie: String = "", val definicja: String = "")

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = FlashcardDatabase.getInstance(this)
        val repository = FlashcardRepository(database.flashcardDao())
        val viewModel = ViewModelProvider(this, FlashcardViewModel.Factory(repository))[FlashcardViewModel::class.java]

        setContent {
            FiszkiTheme {
                var showSplash by remember { mutableStateOf(true) }

                LaunchedEffect(Unit) {
                    delay(2500)
                    showSplash = false
                }

                Crossfade(targetState = showSplash, animationSpec = tween(800), label = "splashTransition") { isSplash ->
                    if (isSplash) {
                        FishSplashScreen()
                    } else {
                        FlashcardApp(viewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun AppLogo(modifier: Modifier = Modifier, animate: Boolean = true) {
    val infiniteTransition = rememberInfiniteTransition(label = "logoAnim")
    val wag by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wag"
    )

    Box(
        modifier = modifier.size(120.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier.size(80.dp)
        ) {
            val w = size.width
            val h = size.height
            val wagOffset = if (animate) Math.sin(wag.toDouble() * Math.PI * 2.0).toFloat() * 12f else 0f

            val fishPath = Path().apply {
                moveTo(w * 0.85f, h * 0.5f)
                quadraticTo(w * 0.5f, h * 0.15f, w * 0.2f, h * 0.5f)
                lineTo(w * 0.05f, h * 0.35f + wagOffset)
                lineTo(w * 0.05f, h * 0.65f + wagOffset)
                lineTo(w * 0.2f, h * 0.5f)
                quadraticTo(w * 0.5f, h * 0.85f, w * 0.85f, h * 0.5f)
                close()
            }
            drawPath(fishPath, Color.White, style = Fill)
            drawCircle(GreenBlueGradient[0], radius = 3.dp.toPx(), center = center.copy(x = center.x + w * 0.25f, y = center.y - h * 0.02f))
        }
    }
}

@Composable
fun FishSplashScreen() {
    val infiniteTransition = rememberInfiniteTransition(label = "fishSwim")

    val swimX by infiniteTransition.animateFloat(
        initialValue = -0.5f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(3500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "swimX"
    )

    val wag by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wag"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(GreenBlueGradient)),
        contentAlignment = Alignment.Center
    ) {
        val bubbleIcons = listOf(Icons.Rounded.CheckCircle, Icons.Rounded.Error, Icons.Rounded.Rule, Icons.Rounded.CheckCircle, Icons.Rounded.Error)
        bubbleIcons.forEachIndexed { index, icon ->
            val bubbleProgress by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2000, delayMillis = index * 400, easing = LinearOutSlowInEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "bubble$index"
            )

            Box(
                modifier = Modifier
                    .offset(
                        x = (Math.sin(index.toDouble() + bubbleProgress * 5).toFloat() * 100).dp,
                        y = (200 - bubbleProgress * 400).dp
                    )
                    .alpha((1f - bubbleProgress) * 0.6f)
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .graphicsLayer { translationX = (swimX - 0.5f) * 1000f },
                contentAlignment = Alignment.Center
            ) {
                AppLogo(animate = true)
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                "FISZKI",
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 42.sp,
                letterSpacing = 12.sp,
                modifier = Modifier.graphicsLayer {
                    alpha = 0.9f
                    val s = 1f + (wagOffsetForText(wag) * 0.05f)
                    scaleX = s
                    scaleY = s
                }
            )

            Text(
                "Twoja nauka płynie gładko",
                color = Color.White.copy(alpha = 0.7f),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

fun wagOffsetForText(wag: Float): Float = Math.sin(wag.toDouble() * Math.PI * 2.0).toFloat()

@Composable
fun FlashcardApp(viewModel: FlashcardViewModel) {
    var currentScreen by remember { mutableStateOf("sets_list") }
    val flashcardSets by viewModel.flashcardSets.collectAsState()
    var activeSetId by remember { mutableStateOf<String?>(null) }
    val activeSet = flashcardSets.find { it.id == activeSetId }

    var qrSetToExport by remember { mutableStateOf<FlashcardSet?>(null) }
    var showImportDialog by remember { mutableStateOf(false) }
    var showStatsSetId by remember { mutableStateOf<String?>(null) }
    var showHistorySetId by remember { mutableStateOf<String?>(null) }
    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    drawCircle(
                        Brush.radialGradient(
                            listOf(AppPrimary.copy(alpha = 0.1f), Color.Transparent),
                            center = center.copy(x = size.width, y = 0f),
                            radius = size.maxDimension
                        )
                    )
                }
        )

        AnimatedContent(
            targetState = currentScreen,
            transitionSpec = {
                if (targetState == "sets_list") {
                    (slideInHorizontally { -it } + fadeIn()).togetherWith(slideOutHorizontally { it } + fadeOut())
                } else {
                    (slideInHorizontally { it } + fadeIn()).togetherWith(slideOutHorizontally { -it } + fadeOut())
                } using SizeTransform(clip = false)
            },
            label = "screenTransition"
        ) { screen ->
            Surface(color = Color.Transparent) {
                when (screen) {
                    "sets_list" -> SetsListScreen(
                        sets = flashcardSets,
                        onCreateNewSet = {
                            activeSetId = null
                            currentScreen = "creator"
                        },
                        onEditSet = { id ->
                            activeSetId = id
                            currentScreen = "creator"
                        },
                        onStudyFlip = { id ->
                            activeSetId = id
                            currentScreen = "flip"
                        },
                        onStudyTest = { id ->
                            activeSetId = id
                            currentScreen = "study"
                        },
                        onDeleteSet = { id -> viewModel.deleteSet(id) },
                        onExportQr = { set -> qrSetToExport = set },
                        onOpenImport = { showImportDialog = true },
                        onShowStats = { id -> showStatsSetId = id },
                        onShowHistory = { id -> showHistorySetId = id }
                    )
                    "creator" -> CreatorScreen(
                        initialName = activeSet?.name ?: "",
                        initialCards = activeSet?.cards ?: emptyList(),
                        onSave = { updatedName, updatedCards ->
                            val id = activeSetId ?: UUID.randomUUID().toString()
                            viewModel.saveSet(id, updatedName, updatedCards)
                            currentScreen = "sets_list"
                        },
                        onBack = { currentScreen = "sets_list" }
                    )
                    "flip" -> FlipScreen(
                        setName = activeSet?.name ?: "Nauka",
                        flashcards = activeSet?.cards ?: emptyList(),
                        onFinish = { currentScreen = "sets_list" }
                    )
                    "study" -> StudyScreen(
                        setName = activeSet?.name ?: "Test",
                        flashcards = activeSet?.cards ?: emptyList(),
                        onLogMistake = { pojecie -> activeSetId?.let { viewModel.logMistake(it, pojecie) } },
                        onLogSession = { correct, total -> activeSetId?.let { viewModel.logSession(it, correct, total) } },
                        onFinish = { currentScreen = "sets_list" }
                    )
                }
            }
        }

        qrSetToExport?.let { setToExport ->
            QrExportDialog(flashcardSet = setToExport, onDismiss = { qrSetToExport = null })
        }

        if (showImportDialog) {
            ImportDialog(
                onDismiss = { showImportDialog = false },
                onImportSuccess = { importedSet ->
                    viewModel.saveSet(importedSet.id, importedSet.name, importedSet.cards)
                    showImportDialog = false
                }
            )
        }

        showStatsSetId?.let { id ->
            val name = flashcardSets.find { it.id == id }?.name ?: ""
            StatsDialog(
                setId = id,
                setName = name,
                viewModel = viewModel,
                onDismiss = { showStatsSetId = null }
            )
        }
        showHistorySetId?.let { id ->
            val name = flashcardSets.find { it.id == id }?.name ?: ""
            HistoryDialog(
                setId = id,
                setName = name,
                viewModel = viewModel,
                onDismiss = { showHistorySetId = null }
            )
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetsListScreen(
    sets: List<FlashcardSet>,
    onCreateNewSet: () -> Unit,
    onEditSet: (String) -> Unit,
    onStudyFlip: (String) -> Unit,
    onStudyTest: (String) -> Unit,
    onDeleteSet: (String) -> Unit,
    onExportQr: (FlashcardSet) -> Unit,
    onOpenImport: () -> Unit,
    onShowStats: (String) -> Unit,
    onShowHistory: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredSets = remember(searchQuery, sets) {
        sets.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "FISZKI",
                        fontWeight = FontWeight.Black,
                        letterSpacing = 6.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                actions = {
                    IconButton(onClick = onOpenImport) {
                        Icon(Icons.Rounded.QrCodeScanner, contentDescription = "Import", tint = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = {
            AppFab(onClick = onCreateNewSet, icon = Icons.Rounded.Add)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (sets.isEmpty()) {
                EmptyState(onCreateNewSet)
            } else {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    placeholder = { Text("Szukaj zestawu...") },
                    leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = "Szukaj", tint = MaterialTheme.colorScheme.primary) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Rounded.Clear, contentDescription = "Wyczyść")
                            }
                        }
                    },
                    shape = RoundedCornerShape(24.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )

                if (filteredSets.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                        Text("Brak wyników wyszukiwania", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(24.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredSets) { set ->
                            AppSetCard(
                                set = set,
                                onEdit = { onEditSet(set.id) },
                                onDelete = { onDeleteSet(set.id) },
                                onFlip = { onStudyFlip(set.id) },
                                onTest = { onStudyTest(set.id) },
                                onQr = { onExportQr(set) },
                                onStats = { onShowStats(set.id) },
                                onHistory = { onShowHistory(set.id) }
                            )
                        }
                        item { Spacer(modifier = Modifier.height(80.dp)) }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatorScreen(
    initialName: String,
    initialCards: List<Flashcard>,
    onSave: (String, List<Flashcard>) -> Unit,
    onBack: () -> Unit
) {
    var setName by remember { mutableStateOf(initialName) }
    var drafts by remember { mutableStateOf(if (initialCards.isEmpty()) listOf(FlashcardDraft(), FlashcardDraft()) else initialCards.map { FlashcardDraft(it.pojecie, it.definicja) }) }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Kreator Zestawu", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = {
            Box(modifier = Modifier.padding(24.dp)) {
                AppMainButton(
                    text = "ZAPISZ ZESTAW",
                    onClick = {
                        val valid = drafts.filter { it.pojecie.isNotBlank() && it.definicja.isNotBlank() }
                        onSave(setName.ifBlank { "Mój Zestaw" }, valid.map { Flashcard(it.pojecie, it.definicja) })
                    },
                    gradient = RoseOrangeGradient
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                AppTextField(
                    value = setName,
                    onValueChange = { setName = it },
                    label = "Nazwa zestawu",
                    placeholder = "np. Angielski - Owoce",
                    isHeader = true
                )
            }
            itemsIndexed(drafts) { index, draft ->
                AppDraftCard(
                    number = index + 1,
                    draft = draft,
                    onPojecieChange = { v -> drafts = drafts.toMutableList().also { it[index] = draft.copy(pojecie = v) } },
                    onDefinicjaChange = { v -> drafts = drafts.toMutableList().also { it[index] = draft.copy(definicja = v) } },
                    onDelete = { if (drafts.size > 1) drafts = drafts.toMutableList().also { it.removeAt(index) } }
                )
            }
            item {
                TextButton(
                    onClick = { drafts = drafts + FlashcardDraft() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Rounded.Add, null)
                    Spacer(Modifier.width(8.dp))
                    Text("DODAJ KOLEJNĄ FISZKĘ", fontWeight = FontWeight.Bold)
                }
            }
            item { Spacer(Modifier.height(100.dp)) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlipScreen(setName: String, flashcards: List<Flashcard>, onFinish: () -> Unit) {
    var currentIndex by remember { mutableStateOf(0) }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text(setName, fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = onFinish) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "${currentIndex + 1} z ${flashcards.size}",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            if (flashcards.isNotEmpty()) {
                AppFlippableCard(flashcards[currentIndex])
            } else {
                Text("Brak fiszek w zestawie")
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                AppCircleButton(
                    icon = Icons.AutoMirrored.Rounded.ArrowBack,
                    onClick = { if (currentIndex > 0) currentIndex-- },
                    enabled = currentIndex > 0,
                    modifier = Modifier.weight(1f)
                )
                AppCircleButton(
                    icon = Icons.AutoMirrored.Rounded.ArrowForward,
                    onClick = { if (currentIndex < flashcards.lastIndex) currentIndex++ },
                    enabled = currentIndex < flashcards.lastIndex,
                    isPrimary = true,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyScreen(
    setName: String,
    flashcards: List<Flashcard>,
    onLogMistake: (String) -> Unit,
    onLogSession: (Int, Int) -> Unit,
    onFinish: () -> Unit
) {

    var answers by remember { mutableStateOf(flashcards.map { "" }) }
    var isVerified by remember { mutableStateOf(false) }
    val correctCount = answers.zip(flashcards).count { it.first.trim().lowercase() == it.second.definicja.trim().lowercase() }
    val scorePercentage = if (flashcards.isNotEmpty()) (correctCount * 100 / flashcards.size) else 0

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(setName, fontWeight = FontWeight.Black, fontSize = 18.sp)
                        if (isVerified) {
                            Text("Twój wynik: $scorePercentage%", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onFinish) {
                        Icon(Icons.Rounded.Close, contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = {
            Box(modifier = Modifier.padding(24.dp)) {
                AppMainButton(
                    text = if (isVerified) "POWRÓT DO MENU" else "ZOBACZ WYNIKI",
                    onClick = {
                        if (isVerified) {
                            onFinish()
                        } else {

                            answers.zip(flashcards).forEach { (answer, card) ->
                                val isCorrect = answer.trim().lowercase() == card.definicja.trim().lowercase()
                                if (!isCorrect) {
                                    onLogMistake(card.pojecie)
                                }
                            }
                            onLogSession(correctCount, flashcards.size)
                            isVerified = true
                        }
                    },
                    gradient = if (isVerified) GoldGradient else OrangeRedGradient
                )
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            AnimatedVisibility(
                visible = isVerified,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                ScoreHeader(correctCount, flashcards.size, scorePercentage)
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                itemsIndexed(flashcards) { index, card ->
                    AppStudyRow(
                        card = card,
                        answer = answers[index],
                        isVerified = isVerified,
                        onAnswerChange = { v -> if (!isVerified) answers = answers.toMutableList().also { it[index] = v } }
                    )
                }
                item { Spacer(Modifier.height(100.dp)) }
            }
        }
    }
}

@Composable
fun ScoreHeader(correct: Int, total: Int, percentage: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp)
            .shadow(16.dp, RoundedCornerShape(32.dp)),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp),
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Row(
            modifier = Modifier.padding(28.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = when {
                        percentage == 100 -> "Idealnie! 🎉"
                        percentage >= 75 -> "Świetnie! 👏"
                        percentage >= 50 -> "Dobra robota! 👍"
                        else -> "Próbuj dalej! 💪"
                    },
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 16.sp,
                    letterSpacing = 0.5.sp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "$correct / $total poprawnych",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-1).sp
                )
            }

            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(80.dp).padding(4.dp)) {
                CircularProgressIndicator(
                    progress = { percentage / 100f },
                    modifier = Modifier.fillMaxSize(),
                    strokeWidth = 8.dp,
                    color = if (percentage >= 50) AppSuccess else AppError,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                )
                Text(
                    text = "$percentage%",
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}



@Composable
fun AppSetCard(
    set: FlashcardSet,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onFlip: () -> Unit,
    onTest: () -> Unit,
    onQr: () -> Unit,
    onStats: () -> Unit,
    onHistory: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {


            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = set.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${set.cards.size} fiszek",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }


                IconButton(onClick = onDelete) {
                    Icon(Icons.Rounded.Delete, null, tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f))
                }
            }

            Spacer(modifier = Modifier.height(20.dp))


            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AppActionCard(
                    text = "NAUKA",
                    icon = Icons.Rounded.PlayArrow,
                    gradient = GreenBlueGradient,
                    onClick = onFlip,
                    modifier = Modifier.weight(1f)
                )
                AppActionCard(
                    text = "QUIZ",
                    icon = Icons.Rounded.Rule,
                    gradient = OrangeRedGradient,
                    onClick = onTest,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))


            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onStats) { Icon(Icons.Rounded.BarChart, null, tint = MaterialTheme.colorScheme.primary) }
                IconButton(onClick = onHistory) { Icon(Icons.Rounded.History, null, tint = MaterialTheme.colorScheme.outline) }
                IconButton(onClick = onQr) { Icon(Icons.Rounded.QrCode, null, tint = MaterialTheme.colorScheme.outline) }
                IconButton(onClick = onEdit) { Icon(Icons.Rounded.Edit, null, tint = MaterialTheme.colorScheme.outline) }
            }
        }
    }
}

@Composable
fun AppActionCard(text: String, icon: ImageVector, gradient: List<Color>, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(64.dp),
        shape = RoundedCornerShape(24.dp),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .background(Brush.linearGradient(gradient))
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = Color.White, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(10.dp))
                Text(text, color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp, letterSpacing = 1.sp)
            }
        }
    }
}

@Composable
fun AppFlippableCard(flashcard: Flashcard) {
    var rotated by remember(flashcard) { mutableStateOf(false) }
    val rotation by animateFloatAsState(
        targetValue = if (rotated) 180f else 0f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessLow),
        label = "cardFlip"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.85f)
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 16f * density
            }
            .clickable { rotated = !rotated }
            .shadow(40.dp, RoundedCornerShape(48.dp))
            .background(
                Brush.verticalGradient(
                    if (rotation <= 90f) GreenBlueGradient else OrangeRedGradient
                ),
                shape = RoundedCornerShape(48.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        if (rotation <= 90f) {
            Text(
                text = flashcard.pojecie,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Black,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(32.dp)
            )
        } else {
            Text(
                text = flashcard.definicja,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .graphicsLayer { rotationY = 180f }
                    .padding(32.dp)
            )
        }
    }
}

@Composable
fun AppDraftCard(
    number: Int,
    draft: FlashcardDraft,
    onPojecieChange: (String) -> Unit,
    onDefinicjaChange: (String) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = CircleShape,
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("$number", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
                    }
                }
                Spacer(Modifier.width(12.dp))
                Text("FISZKA", fontWeight = FontWeight.Black, letterSpacing = 2.sp, fontSize = 12.sp)
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onDelete) { Icon(Icons.Rounded.Delete, null, tint = MaterialTheme.colorScheme.error.copy(alpha = 0.5f), modifier = Modifier.size(20.dp)) }
            }
            Spacer(Modifier.height(16.dp))
            AppTextField(value = draft.pojecie, onValueChange = onPojecieChange, label = "Pojęcie")
            Spacer(Modifier.height(12.dp))
            AppTextField(value = draft.definicja, onValueChange = onDefinicjaChange, label = "Definicja")
        }
    }
}

@Composable
fun AppStudyRow(card: Flashcard, answer: String, isVerified: Boolean, onAnswerChange: (String) -> Unit) {
    val isCorrect = answer.trim().lowercase() == card.definicja.trim().lowercase()

    val backgroundColor by animateColorAsState(
        targetValue = when {
            !isVerified -> MaterialTheme.colorScheme.surface
            isCorrect -> if (isSystemInDarkTheme()) Color(0xFF064E3B) else Color(0xFFF0FDF4)
            else -> if (isSystemInDarkTheme()) Color(0xFF7F1D1D) else Color(0xFFFEF2F2)
        },
        label = "bgColor"
    )

    val contentColor = MaterialTheme.colorScheme.onSurface

    val borderColor by animateColorAsState(
        targetValue = when {
            !isVerified -> MaterialTheme.colorScheme.outlineVariant
            isCorrect -> AppSuccess.copy(alpha = 0.6f)
            else -> AppError.copy(alpha = 0.6f)
        },
        label = "borderColor"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(2.dp, borderColor),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isVerified) 0.dp else 4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = card.pojecie,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    modifier = Modifier.weight(1f),
                    color = contentColor
                )
                if (isVerified) {
                    Icon(
                        imageVector = if (isCorrect) Icons.Rounded.CheckCircle else Icons.Rounded.Error,
                        contentDescription = null,
                        tint = if (isCorrect) AppSuccess else AppError,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = answer,
                onValueChange = onAnswerChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Wpisz definicję...") },
                shape = RoundedCornerShape(16.dp),
                enabled = !isVerified,
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(color = contentColor, fontWeight = FontWeight.Medium),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Black.copy(alpha = 0.05f),
                    disabledBorderColor = if (isCorrect) AppSuccess.copy(alpha = 0.5f) else AppError.copy(alpha = 0.5f),
                    disabledTextColor = contentColor,
                    disabledPlaceholderColor = contentColor.copy(alpha = 0.5f)
                )
            )

            AnimatedVisibility(
                visible = isVerified && !isCorrect,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    Text(
                        "Poprawna odpowiedź:",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSystemInDarkTheme()) Color(0xFFFCA5A5) else AppError,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = card.definicja,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = contentColor
                    )
                }
            }
        }
    }
}

@Composable
fun AppTextField(value: String, onValueChange: (String) -> Unit, label: String, placeholder: String = "", isHeader: Boolean = false) {
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(start = 8.dp, bottom = 4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder) },
            textStyle = if (isHeader) MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black) else MaterialTheme.typography.bodyLarge,
            shape = RoundedCornerShape(20.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
            )
        )
    }
}

@Composable
fun AppMainButton(text: String, onClick: () -> Unit, gradient: List<Color>) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(64.dp),
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 12.dp
    ) {
        Box(
            modifier = Modifier.background(Brush.linearGradient(gradient)),
            contentAlignment = Alignment.Center
        ) {
            Text(text, color = Color.White, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
        }
    }
}

@Composable
fun AppFab(onClick: () -> Unit, icon: ImageVector) {
    LargeFloatingActionButton(
        onClick = onClick,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = Color.White,
        shape = CircleShape,
        modifier = Modifier.padding(8.dp).shadow(12.dp, CircleShape)
    ) {
        Icon(icon, null, modifier = Modifier.size(36.dp))
    }
}

@Composable
fun AppCircleButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isPrimary: Boolean = false
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(64.dp),
        shape = RoundedCornerShape(24.dp),
        color = if (isPrimary) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer,
        contentColor = if (isPrimary) Color.White else MaterialTheme.colorScheme.primary,
        shadowElevation = if (enabled) 4.dp else 0.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, null, modifier = Modifier.size(28.dp).alpha(if (enabled) 1f else 0.3f))
        }
    }
}

@Composable
fun EmptyState(onCreate: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Rounded.Style, null, modifier = Modifier.size(120.dp).alpha(0.1f))
        Spacer(Modifier.height(24.dp))
        Text("Brak zestawów", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
        Text("Zacznij od stworzenia swoich pierwszych fiszek!", textAlign = TextAlign.Center, modifier = Modifier.alpha(0.6f))
        Spacer(Modifier.height(32.dp))
        Button(onClick = onCreate, shape = RoundedCornerShape(20.dp)) {
            Text("STWÓRZ TERAZ", fontWeight = FontWeight.Black)
        }
    }
}



@Composable
fun QrExportDialog(flashcardSet: FlashcardSet, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val jsonString = remember(flashcardSet) {
        val cardsJson = flashcardSet.cards.joinToString(",") { "{\"p\":\"${it.pojecie.replace("\"", "\\\"")}\",\"d\":\"${it.definicja.replace("\"", "\\\"")}\"}" }
        "{\"n\":\"${flashcardSet.name.replace("\"", "\\\"")}\",\"c\":[$cardsJson]}"
    }
    val qrBitmap = remember(jsonString) { generateQrCode(jsonString, 600) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = onDismiss, shape = RoundedCornerShape(16.dp)) { Text("GOTOWE") }
        },
        dismissButton = {
            if (qrBitmap != null) {
                TextButton(onClick = { shareQrCode(context, qrBitmap) }) { Text("UDOSTĘPNIJ") }
            }
        },
        title = { Text("Udostępnij Zestaw", fontWeight = FontWeight.Black) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(flashcardSet.name, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(16.dp))
                if (qrBitmap != null) {
                    Image(
                        bitmap = qrBitmap.asImageBitmap(),
                        contentDescription = "QR",
                        modifier = Modifier.size(240.dp).clip(RoundedCornerShape(24.dp))
                    )
                }
            }
        },
        shape = RoundedCornerShape(32.dp)
    )
}

fun shareQrCode(context: Context, bitmap: Bitmap) {
    try {
        val file = File(context.cacheDir, "qr.png").apply { parentFile?.mkdirs() }
        FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, "Hej! Zaimportuj moje fiszki z tego kodu QR!")
            clipData = android.content.ClipData.newUri(context.contentResolver, "QR Code", uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Udostępnij kod QR"))
    } catch (e: Exception) {
        e.printStackTrace()
        android.widget.Toast.makeText(context, "Błąd: ${e.localizedMessage}", android.widget.Toast.LENGTH_LONG).show()
    }
}

@Composable
fun ImportDialog(onDismiss: () -> Unit, onImportSuccess: (FlashcardSet) -> Unit) {
    val context = LocalContext.current
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            val image = InputImage.fromFilePath(context, it)
            BarcodeScanning.getClient().process(image)
                .addOnSuccessListener { barcodes ->
                    val text = barcodes.firstOrNull()?.rawValue
                    val set = text?.let { parseJson(it) }
                    if (set != null) onImportSuccess(set) else errorMessage = "Nieprawidłowy kod QR"
                }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { },
        title = { Text("Importuj Fiszki", fontWeight = FontWeight.Black) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                AppActionCard(
                    text = "SKANUJ APARATEM",
                    icon = Icons.Rounded.QrCodeScanner,
                    gradient = RoseOrangeGradient,
                    onClick = {
                        GmsBarcodeScanning.getClient(context).startScan()
                            .addOnSuccessListener { barcode ->
                                barcode.rawValue?.let { parseJson(it) }?.let { onImportSuccess(it) }
                            }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                AppActionCard(
                    text = "WYBIERZ Z GALERII",
                    icon = Icons.Rounded.Image,
                    gradient = LearningGradient,
                    onClick = { galleryLauncher.launch("image/*") },
                    modifier = Modifier.fillMaxWidth()
                )
                if (errorMessage != null) {
                    Text(errorMessage!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                }
            }
        },
        shape = RoundedCornerShape(32.dp)
    )
}

fun generateQrCode(text: String, size: Int): Bitmap? {
    return try {
        val hints = mapOf(EncodeHintType.CHARACTER_SET to "UTF-8")
        val bitMatrix = com.google.zxing.MultiFormatWriter().encode(text, com.google.zxing.BarcodeFormat.QR_CODE, size, size, hints)
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
        for (x in 0 until size) {
            for (y in 0 until size) {
                bitmap.setPixel(x, y, if (bitMatrix.get(x, y)) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
            }
        }
        bitmap
    } catch (e: Exception) { null }
}

fun parseJson(json: String): FlashcardSet? {
    return try {
        val name = "\"n\":\"(.*?)\"".toRegex().find(json)?.groupValues?.get(1)?.replace("\\\"", "\"") ?: return null
        val cards = "\\{\"p\":\"(.*?)\",\"d\":\"(.*?)\"\\}".toRegex().findAll(json).map {
            Flashcard(it.groupValues[1].replace("\\\"", "\""), it.groupValues[2].replace("\\\"", "\""))
        }.toList()
        if (cards.isEmpty()) null else FlashcardSet(name = name, cards = cards)
    } catch (e: Exception) { null }
}

@Composable
fun StatsDialog(
    setId: String,
    setName: String,
    viewModel: FlashcardViewModel,
    onDismiss: () -> Unit
) {
    val sessions by viewModel.getSessionsForSet(setId).collectAsState(initial = emptyList())
    val hardestCards by viewModel.getHardestCardsForSet(setId).collectAsState(initial = emptyList())

    val lastScore = sessions.firstOrNull()?.let { (it.correctCount.toFloat() / it.totalCount * 100).toInt() }
    val maxScore = sessions.maxOfOrNull { (it.correctCount.toFloat() / it.totalCount * 100).toInt() }
    val minScore = sessions.minOfOrNull { (it.correctCount.toFloat() / it.totalCount * 100).toInt() }
    val avgScore = if (sessions.isNotEmpty()) sessions.map { (it.correctCount.toFloat() / it.totalCount * 100).toInt() }.average().toInt() else null

    val streak = remember(sessions) {
        if (sessions.isEmpty()) return@remember 0
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        val loggedDays = sessions.map { sdf.format(java.util.Date(it.timestamp)) }.toSet()

        val cal = java.util.Calendar.getInstance()
        var currentStreak = 0

        val todayStr = sdf.format(cal.time)
        cal.add(java.util.Calendar.DAY_OF_YEAR, -1)
        val yesterdayStr = sdf.format(cal.time)

        if (loggedDays.contains(todayStr) || loggedDays.contains(yesterdayStr)) {
            cal.time = java.util.Date()
            while (true) {
                val checkStr = sdf.format(cal.time)
                if (loggedDays.contains(checkStr)) {
                    currentStreak++
                    cal.add(java.util.Calendar.DAY_OF_YEAR, -1)
                } else {
                    break
                }
            }
        }
        currentStreak
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val chartData = remember(sessions) {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        val cal = java.util.Calendar.getInstance()

        val last7DaysStrings = (0..6).map {
            val dateStr = sdf.format(cal.time)
            cal.add(java.util.Calendar.DAY_OF_YEAR, -1)
            dateStr
        }.reversed()

        val grouped = sessions.groupBy { sdf.format(java.util.Date(it.timestamp)) }
        last7DaysStrings.map { day ->
            val daySessions = grouped[day] ?: emptyList()
            if (daySessions.isEmpty()) 0f
            else daySessions.map { it.correctCount.toFloat() / it.totalCount * 100 }.average().toFloat()
        }
    }

    val dayLabels = remember {
        val cal = java.util.Calendar.getInstance()
        val names = arrayOf("Nd", "Pon", "Wt", "Śr", "Czw", "Pt", "Sob")
        (0..6).map {
            val dow = cal.get(java.util.Calendar.DAY_OF_WEEK)
            cal.add(java.util.Calendar.DAY_OF_YEAR, -1)
            names[dow - 1]
        }.reversed()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = onDismiss, shape = RoundedCornerShape(16.dp)) { Text("ZAMKNIJ") }
        },
        title = { Text("Statystyki: $setName", fontWeight = FontWeight.Black) },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Rounded.LocalFireDepartment, "Streak", tint = Color(0xFFFF5722), modifier = Modifier.size(36.dp))
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text("Seria nauki (Streak)", style = MaterialTheme.typography.labelMedium)
                                Text("$streak dni pod rząd!", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            }
                        }
                    }
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            StatMiniCard("Ostatni", lastScore?.let { "$it%" } ?: "-", Modifier.weight(1f))
                            StatMiniCard("Średni", avgScore?.let { "$it%" } ?: "-", Modifier.weight(1f))
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            StatMiniCard("Najlepszy", maxScore?.let { "$it%" } ?: "-", Modifier.weight(1f))
                            StatMiniCard("Najgorszy", minScore?.let { "$it%" } ?: "-", Modifier.weight(1f))
                        }
                    }
                }

                item {
                    Text("Średnie wyniki z ostatnich 7 dni", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                    Spacer(Modifier.height(8.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp),
                            verticalAlignment = Alignment.Bottom
                        ) {

                            Column(
                                modifier = Modifier
                                    .width(40.dp)
                                    .height(100.dp)
                                    .padding(end = 8.dp),
                                verticalArrangement = Arrangement.SpaceBetween,
                                horizontalAlignment = Alignment.End
                            ) {
                                Text("100%", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, softWrap = false)
                                Text("50%", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, softWrap = false)
                                Text("0%", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, softWrap = false)
                            }


                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                chartData.forEachIndexed { i, score ->
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Bottom
                                    ) {

                                        if (score > 0f) {
                                            Text(
                                                text = "${score.toInt()}%",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.padding(bottom = 4.dp),
                                                softWrap = false
                                            )
                                        }


                                        Box(
                                            modifier = Modifier
                                                .width(16.dp)
                                                .height(if (score > 0f) (score * 1.0f).dp else 6.dp)
                                                .background(
                                                    color = if (score > 0f) primaryColor else primaryColor.copy(alpha = 0.15f),
                                                    shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                                                )
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))


                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Spacer(modifier = Modifier.width(40.dp))


                            Row(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                dayLabels.forEach { day ->
                                    Text(
                                        text = day,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Text("Najtrudniejsze fiszki (najwięcej błędów)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                    Spacer(Modifier.height(8.dp))
                    if (hardestCards.none { it.wrongCount > 0 }) {
                        Text("Brak błędów! Świetna robota.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            hardestCards.filter { it.wrongCount > 0 }.forEach { card ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(card.pojecie, fontWeight = FontWeight.Bold)
                                        Text(card.definicja, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                    }
                                    Text("${card.wrongCount} ❌", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        },
        shape = RoundedCornerShape(32.dp)
    )
}

@Composable
fun StatMiniCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
        }
    }
}
@Composable
fun HistoryDialog(
    setId: String,
    setName: String,
    viewModel: FlashcardViewModel,
    onDismiss: () -> Unit
) {

    val sessions by viewModel.getSessionsForSet(setId).collectAsState(initial = emptyList())
    val sdf = remember { java.text.SimpleDateFormat("dd.MM.yyyy, HH:mm", java.util.Locale.getDefault()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = onDismiss, shape = RoundedCornerShape(16.dp)) { Text("ZAMKNIJ") }
        },
        title = { Text("Historia prób: $setName", fontWeight = FontWeight.Black) },
        text = {
            Box(modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp)) {
                if (sessions.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Rounded.History, null, modifier = Modifier.size(64.dp).alpha(0.2f))
                        Spacer(Modifier.height(12.dp))
                        Text("Brak rozwiązanych testów dla tego zestawu", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.alpha(0.6f), textAlign = TextAlign.Center)
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(sessions) { session ->
                            val percentage = if (session.totalCount > 0) (session.correctCount * 100 / session.totalCount) else 0
                            val dateStr = sdf.format(java.util.Date(session.timestamp))

                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Rounded.DateRange, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.outline)
                                            Spacer(Modifier.width(4.dp))
                                            Text(dateStr, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.outline)
                                        }
                                        Spacer(Modifier.height(4.dp))
                                        Text("Wynik: ${session.correctCount} / ${session.totalCount}", fontWeight = FontWeight.Bold)
                                    }


                                    val badgeColor = when {
                                        percentage >= 80 -> AppSuccess
                                        percentage >= 50 -> MaterialTheme.colorScheme.tertiary
                                        else -> AppError
                                    }

                                    Surface(
                                        color = badgeColor.copy(alpha = 0.15f),
                                        contentColor = badgeColor,
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text(
                                            text = "$percentage%",
                                            fontWeight = FontWeight.Black,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        shape = RoundedCornerShape(32.dp)
    )
}