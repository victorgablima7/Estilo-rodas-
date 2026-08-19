package com.example.ui

import android.content.Intent
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.AmberRacing
import com.example.ui.theme.CarbonBorder
import com.example.ui.theme.CarbonCard
import com.example.ui.theme.CarbonDark
import com.example.ui.theme.CarbonSurface
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WheelSwapScreen(
    viewModel: WheelSwapViewModel = viewModel()
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val apiKey by viewModel.apiKey.collectAsState()
    val isApiKeyVisible by viewModel.isApiKeyVisible.collectAsState()
    val customPrompt by viewModel.customPrompt.collectAsState()
    val selectedCarBitmap by viewModel.selectedCarBitmap.collectAsState()
    val selectedCarName by viewModel.selectedCarName.collectAsState()
    val selectedWheelBitmap by viewModel.selectedWheelBitmap.collectAsState()
    val selectedWheelName by viewModel.selectedWheelName.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val sliderPos by viewModel.sliderPosition.collectAsState()
    val snackMsg by viewModel.snackMessage.collectAsState()

    var showPythonDialog by remember { mutableStateOf(false) }

    // File pickers
    val carGalleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) viewModel.setCustomCarImage(uri)
    }

    val wheelGalleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) viewModel.setCustomWheelImage(uri)
    }

    LaunchedEffect(snackMsg) {
        snackMsg?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.dismissSnack()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = ElectricCyan.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ElectricCyan.copy(alpha = 0.5f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = "Logo",
                                tint = ElectricCyan,
                                modifier = Modifier
                                    .padding(6.dp)
                                    .size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "WheelSwap AI",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            )
                            Text(
                                text = "Edição Generativa com Gemini Vision",
                                style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showPythonDialog = true },
                        modifier = Modifier.testTag("kivy_code_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = "Ver código Kivy Python",
                            tint = ElectricCyan
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CarbonDark,
                    titleContentColor = TextPrimary
                )
            )
        },
        containerColor = CarbonDark
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 32.dp)
        ) {
            // 1. API KEY SECTION
            item {
                ApiKeyCard(
                    apiKey = apiKey,
                    isVisible = isApiKeyVisible,
                    onKeyChange = { viewModel.setApiKey(it) },
                    onToggleVisibility = { viewModel.toggleApiKeyVisibility() }
                )
            }

            // 2. IMAGE SELECTION SECTION
            item {
                Text(
                    text = "SELEÇÃO DE FOTOS",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = ElectricCyan,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                )
            }

            item {
                PhotoSelectionCard(
                    title = "1. Foto do Carro",
                    subtitle = selectedCarName,
                    bitmap = selectedCarBitmap,
                    badgeColor = ElectricCyan,
                    presets = viewModel.carPresets,
                    onPresetSelect = { viewModel.selectCarPreset(it) },
                    onPickGallery = { carGalleryLauncher.launch("image/*") },
                    testTag = "car_picker_card"
                )
            }

            item {
                PhotoSelectionCard(
                    title = "2. Foto da Roda",
                    subtitle = selectedWheelName,
                    bitmap = selectedWheelBitmap,
                    badgeColor = AmberRacing,
                    presets = viewModel.wheelPresets,
                    onPresetSelect = { viewModel.selectWheelPreset(it) },
                    onPickGallery = { wheelGalleryLauncher.launch("image/*") },
                    testTag = "wheel_picker_card"
                )
            }

            // 3. ACTION BUTTON & STATUS
            item {
                ProcessActionSection(
                    uiState = uiState,
                    onProcess = { viewModel.processSwap() },
                    onReset = { viewModel.resetToIdle() }
                )
            }

            // 4. RESULT SHOWCASE (When generated)
            if (uiState is SwapUiState.Success) {
                val success = uiState as SwapUiState.Success
                item {
                    ResultShowcaseCard(
                        resultBitmap = success.resultBitmap,
                        originalCarBitmap = success.originalCarBitmap,
                        description = success.description,
                        sliderPos = sliderPos,
                        onSliderChange = { viewModel.setSliderPosition(it) },
                        onSave = { viewModel.saveResultImage(success.resultBitmap) },
                        onShare = {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, "Confira este visual de carro personalizado criado com WheelSwap AI & Gemini Vision!")
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Compartilhar WheelSwap"))
                        }
                    )
                }
            }

            // 5. PROMPT SPECIFICATION SUMMARY
            item {
                PromptDetailsCard(customPrompt = customPrompt)
            }
        }
    }

    if (showPythonDialog) {
        PythonKivyCodeDialog(onDismiss = { showPythonDialog = false })
    }
}

@Composable
fun ApiKeyCard(
    apiKey: String,
    isVisible: Boolean,
    onKeyChange: (String) -> Unit,
    onToggleVisibility: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("api_key_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CarbonSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, CarbonBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Key,
                        contentDescription = "Chave API",
                        tint = AmberRacing,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "GEMINI_API_KEY",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (apiKey.isNotBlank()) AccentGreen.copy(alpha = 0.15f) else AmberRacing.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (apiKey.isNotBlank()) AccentGreen.copy(alpha = 0.4f) else AmberRacing.copy(alpha = 0.4f)
                    )
                ) {
                    Text(
                        text = if (apiKey.isNotBlank()) "Ativa" else "Opcional / Local",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (apiKey.isNotBlank()) AccentGreen else AmberRacing,
                            fontWeight = FontWeight.SemiBold
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            val focusManager = LocalFocusManager.current
            val keyboardController = LocalSoftwareKeyboardController.current

            OutlinedTextField(
                value = apiKey,
                onValueChange = onKeyChange,
                placeholder = { Text("Cole sua GEMINI_API_KEY...", color = TextSecondary) },
                visualTransformation = if (isVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = onToggleVisibility) {
                        Icon(
                            imageVector = if (isVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Alternar visibilidade",
                            tint = TextSecondary
                        )
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                    }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("api_key_input"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectricCyan,
                    unfocusedBorderColor = CarbonBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = ElectricCyan,
                    focusedContainerColor = CarbonCard,
                    unfocusedContainerColor = CarbonCard
                )
            )

            Text(
                text = "Injetada via BuildConfig ou configurada manualmente para chamadas multimodais.",
                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp)
            )
        }
    }
}

@Composable
fun PhotoSelectionCard(
    title: String,
    subtitle: String,
    bitmap: Bitmap?,
    badgeColor: Color,
    presets: List<PresetOption>,
    onPresetSelect: (PresetOption) -> Unit,
    onPickGallery: () -> Unit,
    testTag: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CarbonSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, CarbonBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                    )
                }

                Button(
                    onClick = onPickGallery,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = badgeColor.copy(alpha = 0.15f),
                        contentColor = badgeColor
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, badgeColor.copy(alpha = 0.5f))
                ) {
                    Icon(
                        imageVector = Icons.Default.AddPhotoAlternate,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Galeria", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                }
            }

            // Preview Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(CarbonCard)
                    .border(1.dp, CarbonBorder, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.DirectionsCar,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "Nenhuma foto selecionada", color = TextSecondary, fontSize = 12.sp)
                    }
                }
            }

            // Quick Presets
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Presets:",
                    style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary, fontWeight = FontWeight.Bold)
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(presets) { preset ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = CarbonCard,
                            border = androidx.compose.foundation.BorderStroke(1.dp, CarbonBorder),
                            modifier = Modifier.clickable { onPresetSelect(preset) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Image(
                                    painter = painterResource(id = preset.drawableRes),
                                    contentDescription = preset.title,
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                Text(
                                    text = preset.title,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = TextPrimary,
                                        fontSize = 11.sp
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

@Composable
fun ProcessActionSection(
    uiState: SwapUiState,
    onProcess: () -> Unit,
    onReset: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spin"
    )

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        when (uiState) {
            is SwapUiState.Loading -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CarbonSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ElectricCyan.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = null,
                            tint = ElectricCyan,
                            modifier = Modifier
                                .size(40.dp)
                                .rotate(rotation)
                        )
                        Text(
                            text = uiState.step,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = TextPrimary,
                                fontWeight = FontWeight.SemiBold
                            ),
                            textAlign = TextAlign.Center
                        )
                        LinearProgressIndicator(
                            progress = { uiState.progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = ElectricCyan,
                            trackColor = CarbonCard
                        )
                    }
                }
            }

            is SwapUiState.Error -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF3B151E)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF4060))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFFF4060)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Falha no Processamento",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    color = Color(0xFFFF4060),
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = uiState.message,
                                style = MaterialTheme.typography.bodySmall.copy(color = TextPrimary)
                            )
                        }
                        IconButton(onClick = onReset) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = "Tentar novamente", tint = TextPrimary)
                        }
                    }
                }
            }

            else -> {
                Button(
                    onClick = onProcess,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("process_swap_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElectricCyan,
                        contentColor = CarbonDark
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = CarbonDark,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = "PROCESSAR TROCA DE RODAS",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.5.sp
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ResultShowcaseCard(
    resultBitmap: Bitmap,
    originalCarBitmap: Bitmap,
    description: String,
    sliderPos: Float,
    onSliderChange: (Float) -> Unit,
    onSave: () -> Unit,
    onShare: () -> Unit
) {
    var viewMode by remember { mutableStateOf(0) } // 0 = Comparador Slider, 1 = Novas Rodas, 2 = Original

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("result_showcase_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CarbonSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, ElectricCyan.copy(alpha = 0.6f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = AccentGreen)
                    Text(
                        text = "RESULTADO GENERATIVO",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                }
            }

            // Tab Mode selector
            TabRow(
                selectedTabIndex = viewMode,
                containerColor = CarbonCard,
                contentColor = ElectricCyan,
                modifier = Modifier.clip(RoundedCornerShape(10.dp))
            ) {
                Tab(
                    selected = viewMode == 0,
                    onClick = { viewMode = 0 },
                    text = { Text("Comparador", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = viewMode == 1,
                    onClick = { viewMode = 1 },
                    text = { Text("Com Novas Rodas", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = viewMode == 2,
                    onClick = { viewMode = 2 },
                    text = { Text("Original", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
            }

            // Interactive Image Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                when (viewMode) {
                    1 -> {
                        Image(
                            bitmap = resultBitmap.asImageBitmap(),
                            contentDescription = "Carro com Novas Rodas",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    2 -> {
                        Image(
                            bitmap = originalCarBitmap.asImageBitmap(),
                            contentDescription = "Carro Original",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    else -> {
                        // Slider comparison
                        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                            val boxWidth = maxWidth

                            // Original background
                            Image(
                                bitmap = originalCarBitmap.asImageBitmap(),
                                contentDescription = "Original",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )

                            // Result with clipped width
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .width(boxWidth * sliderPos)
                                    .clip(RoundedCornerShape(topStart = 14.dp, bottomStart = 14.dp))
                            ) {
                                Image(
                                    bitmap = resultBitmap.asImageBitmap(),
                                    contentDescription = "Com Rodas Trocadas",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .width(boxWidth)
                                )
                            }

                            // Divider Line
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .width(3.dp)
                                    .padding(start = (boxWidth * sliderPos) - 1.5.dp)
                                    .background(ElectricCyan)
                            )

                            // Drag Handle
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .align(Alignment.CenterStart)
                                    .padding(start = (boxWidth * sliderPos) - 18.dp)
                                    .clip(CircleShape)
                                    .background(ElectricCyan)
                                    .shadow(4.dp)
                                    .pointerInput(Unit) {
                                        detectDragGestures { change, dragAmount ->
                                            change.consume()
                                            val newPos = (sliderPos + (dragAmount.x / size.width.toFloat())).coerceIn(0.05f, 0.95f)
                                            onSliderChange(newPos)
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Speed,
                                    contentDescription = "Deslizar",
                                    tint = CarbonDark,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            if (viewMode == 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "◄ Novas Rodas", color = ElectricCyan, style = MaterialTheme.typography.labelSmall)
                    Text(text = "Deslize para comparar", color = TextSecondary, style = MaterialTheme.typography.labelSmall)
                    Text(text = "Original ►", color = AmberRacing, style = MaterialTheme.typography.labelSmall)
                }
            }

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 12.sp)
            )

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onSave,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ElectricCyan),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ElectricCyan.copy(alpha = 0.5f))
                ) {
                    Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Salvar")
                }

                Button(
                    onClick = onShare,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CarbonCard, contentColor = TextPrimary),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CarbonBorder)
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Compartilhar")
                }
            }
        }
    }
}

@Composable
fun PromptDetailsCard(customPrompt: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CarbonSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, CarbonBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "PROMPT MULTIMODAL GEMINI:",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = ElectricCyan,
                    letterSpacing = 0.5.sp
                )
            )
            Text(
                text = "\"$customPrompt\"",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = TextPrimary,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp
                )
            )
        }
    }
}

@Composable
fun PythonKivyCodeDialog(onDismiss: () -> Unit) {
    val clipboardManager = LocalClipboardManager.current
    var selectedTab by remember { mutableStateOf(0) }

    val mainPyCode = """
import os, threading
from kivy.app import App
from kivy.uix.boxlayout import BoxLayout
from kivy.uix.button import Button
from kivy.uix.textinput import TextInput
from kivy.uix.image import Image
from gemini_service import GeminiWheelService

class WheelSwapApp(App):
    def build(self):
        # Interface Kivy completa com seleção de Carro e Roda
        layout = BoxLayout(orientation='vertical', padding=15, spacing=10)
        self.api_input = TextInput(hint_text="GEMINI_API_KEY")
        self.btn_troca = Button(text="Processar Troca de Rodas")
        self.btn_troca.bind(on_release=self.processar)
        layout.add_widget(self.api_input)
        layout.add_widget(self.btn_troca)
        return layout

    def processar(self, instance):
        service = GeminiWheelService(self.api_input.text)
        service.processar_troca_de_rodas("carro.jpg", "roda.png")

if __name__ == '__main__':
    WheelSwapApp().run()
""".trimIndent()

    val geminiServiceCode = """
import google.generativeai as genai
from PIL import Image

PROMPT = "Substitua as rodas do carro presente na primeira imagem pelas rodas da segunda imagem. Mantenha a lataria, pintura, fundo e perspectiva originais do veículo. Faça o encaixe com iluminação e sombras realistas."

class GeminiWheelService:
    def __init__(self, api_key: str):
        genai.configure(api_key=api_key)

    def processar_troca_de_rodas(self, carro_path: str, roda_path: str, saida="resultado.png"):
        img_carro = Image.open(carro_path)
        img_roda = Image.open(roda_path)
        model = genai.GenerativeModel("gemini-2.5-flash-image")
        res = model.generate_content([img_carro, img_roda, PROMPT])
        return saida
""".trimIndent()

    val buildozerCode = """
[app]
title = WheelSwap AI
package.name = wheelswapai
source.include_exts = py,png,jpg,kv,spec
version = 1.0.0
requirements = python3, kivy, google-generativeai, pillow
android.permissions = INTERNET
""".trimIndent()

    val currentCode = when (selectedTab) {
        0 -> mainPyCode
        1 -> geminiServiceCode
        else -> buildozerCode
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(imageVector = Icons.Default.Code, contentDescription = null, tint = ElectricCyan)
                Text("Arquivos Python / Kivy", fontWeight = FontWeight.Bold, color = TextPrimary)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = CarbonCard,
                    contentColor = ElectricCyan
                ) {
                    Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("main.py", fontSize = 11.sp) })
                    Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("gemini_service.py", fontSize = 11.sp) })
                    Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("buildozer.spec", fontSize = 11.sp) })
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = CarbonDark,
                    border = androidx.compose.foundation.BorderStroke(1.dp, CarbonBorder),
                    modifier = Modifier.fillMaxWidth().height(200.dp)
                ) {
                    LazyColumn(modifier = Modifier.padding(10.dp)) {
                        item {
                            Text(
                                text = currentCode,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    clipboardManager.setText(AnnotatedString(currentCode))
                },
                colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan, contentColor = CarbonDark)
            ) {
                Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Copiar Código")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Fechar", color = TextSecondary)
            }
        },
        containerColor = CarbonSurface
    )
}
