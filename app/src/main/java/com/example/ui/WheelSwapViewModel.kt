package com.example.ui

import android.app.Application
import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.R
import com.example.data.GeminiWheelService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

data class PresetOption(
    val id: String,
    val title: String,
    val subtitle: String,
    val drawableRes: Int
)

data class SwapHistoryItem(
    val id: Long = System.currentTimeMillis(),
    val timestamp: String,
    val carTitle: String,
    val wheelTitle: String,
    val resultBitmap: Bitmap
)

sealed interface SwapUiState {
    object Idle : SwapUiState
    data class Loading(val step: String, val progress: Float) : SwapUiState
    data class Success(
        val resultBitmap: Bitmap,
        val originalCarBitmap: Bitmap,
        val wheelBitmap: Bitmap,
        val description: String,
        val carTitle: String,
        val wheelTitle: String
    ) : SwapUiState
    data class Error(val message: String) : SwapUiState
}

class WheelSwapViewModel(application: Application) : AndroidViewModel(application) {

    private val service = GeminiWheelService(application)

    // Presets
    val carPresets = listOf(
        PresetOption("car_sports", "Coupé Esportivo", "Vermelho Metálico", R.drawable.sample_car_sports_1787169616868),
        PresetOption("car_suv", "SUV Moderno", "Branco Pérola", R.drawable.sample_car_suv_1787169709208)
    )

    val wheelPresets = listOf(
        PresetOption("wheel_bronze", "Roda Forjada Bronze", "Design Multi-Raio Sport", R.drawable.sample_wheel_alloy_1787169629295),
        PresetOption("wheel_silver", "Roda Diamantada Machined", "Acabamento Black & Silver", R.drawable.sample_wheel_silver_1787169725746)
    )

    // API Key state
    private val initialKey = BuildConfig.GEMINI_API_KEY.takeIf { it.isNotBlank() && it != "MY_GEMINI_API_KEY" } ?: ""
    private val _apiKey = MutableStateFlow(initialKey)
    val apiKey: StateFlow<String> = _apiKey.asStateFlow()

    private val _isApiKeyVisible = MutableStateFlow(false)
    val isApiKeyVisible: StateFlow<Boolean> = _isApiKeyVisible.asStateFlow()

    // Prompt state
    private val _customPrompt = MutableStateFlow(GeminiWheelService.PROMPT_EDICAO)
    val customPrompt: StateFlow<String> = _customPrompt.asStateFlow()

    // Selected Images
    private val _selectedCarBitmap = MutableStateFlow<Bitmap?>(null)
    val selectedCarBitmap: StateFlow<Bitmap?> = _selectedCarBitmap.asStateFlow()

    private val _selectedCarName = MutableStateFlow("Coupé Esportivo (Padrão)")
    val selectedCarName: StateFlow<String> = _selectedCarName.asStateFlow()

    private val _selectedWheelBitmap = MutableStateFlow<Bitmap?>(null)
    val selectedWheelBitmap: StateFlow<Bitmap?> = _selectedWheelBitmap.asStateFlow()

    private val _selectedWheelName = MutableStateFlow("Roda Forjada Bronze (Padrão)")
    val selectedWheelName: StateFlow<String> = _selectedWheelName.asStateFlow()

    // UI & Execution State
    private val _uiState = MutableStateFlow<SwapUiState>(SwapUiState.Idle)
    val uiState: StateFlow<SwapUiState> = _uiState.asStateFlow()

    // Interactive slider position
    private val _sliderPosition = MutableStateFlow(0.5f)
    val sliderPosition: StateFlow<Float> = _sliderPosition.asStateFlow()

    // History
    private val _history = MutableStateFlow<List<SwapHistoryItem>>(emptyList())
    val history: StateFlow<List<SwapHistoryItem>> = _history.asStateFlow()

    // Toast / Feedback message
    private val _snackMessage = MutableStateFlow<String?>(null)
    val snackMessage: StateFlow<String?> = _snackMessage.asStateFlow()

    init {
        // Load default preset images
        loadDefaultPresets()
    }

    private fun loadDefaultPresets() {
        viewModelScope.launch(Dispatchers.IO) {
            val app = getApplication<Application>()
            try {
                val carBmp = BitmapFactory.decodeResource(app.resources, carPresets[0].drawableRes)
                val wheelBmp = BitmapFactory.decodeResource(app.resources, wheelPresets[0].drawableRes)
                _selectedCarBitmap.value = carBmp
                _selectedWheelBitmap.value = wheelBmp
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    fun setApiKey(key: String) {
        _apiKey.value = key
    }

    fun toggleApiKeyVisibility() {
        _isApiKeyVisible.value = !_isApiKeyVisible.value
    }

    fun setCustomPrompt(prompt: String) {
        _customPrompt.value = prompt
    }

    fun setSliderPosition(pos: Float) {
        _sliderPosition.value = pos.coerceIn(0f, 1f)
    }

    fun selectCarPreset(preset: PresetOption) {
        viewModelScope.launch(Dispatchers.IO) {
            val app = getApplication<Application>()
            val bmp = BitmapFactory.decodeResource(app.resources, preset.drawableRes)
            _selectedCarBitmap.value = bmp
            _selectedCarName.value = preset.title
        }
    }

    fun selectWheelPreset(preset: PresetOption) {
        viewModelScope.launch(Dispatchers.IO) {
            val app = getApplication<Application>()
            val bmp = BitmapFactory.decodeResource(app.resources, preset.drawableRes)
            _selectedWheelBitmap.value = bmp
            _selectedWheelName.value = preset.title
        }
    }

    fun setCustomCarImage(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            val bmp = decodeUriToBitmap(uri)
            if (bmp != null) {
                _selectedCarBitmap.value = bmp
                _selectedCarName.value = "Foto do Carro (Galeria)"
            } else {
                _snackMessage.value = "Não foi possível carregar a foto do carro."
            }
        }
    }

    fun setCustomWheelImage(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            val bmp = decodeUriToBitmap(uri)
            if (bmp != null) {
                _selectedWheelBitmap.value = bmp
                _selectedWheelName.value = "Foto da Roda (Galeria)"
            } else {
                _snackMessage.value = "Não foi possível carregar a foto da roda."
            }
        }
    }

    fun processSwap() {
        val key = _apiKey.value.trim()
        val car = _selectedCarBitmap.value
        val wheel = _selectedWheelBitmap.value

        if (car == null || wheel == null) {
            _uiState.value = SwapUiState.Error("Por favor, selecione a foto do carro e da roda.")
            return
        }

        viewModelScope.launch {
            _uiState.value = SwapUiState.Loading("Preparando imagens em alta resolução...", 0.2f)
            delay(400)

            _uiState.value = SwapUiState.Loading("Enviando requisição multimodal para Gemini API...", 0.5f)
            delay(500)

            _uiState.value = SwapUiState.Loading("Gemini Vision processando encaixe e perspectiva...", 0.75f)

            val result = if (key.isNotBlank()) {
                service.processWheelSwap(
                    apiKey = key,
                    carBitmap = car,
                    wheelBitmap = wheel,
                    customPrompt = _customPrompt.value
                )
            } else {
                // If API key is not entered yet, perform local high-fidelity geometric wheel fitment
                delay(800)
                val simulatedBmp = service.renderRealisticWheelSwap(car, wheel)
                GeminiWheelService.Result.Success(
                    resultBitmap = simulatedBmp,
                    description = "Preview gerado com encaixe fotorrealista. Para refinamento generativo completo via Gemini 2.5 Flash Image, insira sua GEMINI_API_KEY no topo."
                )
            }

            when (result) {
                is GeminiWheelService.Result.Success -> {
                    _uiState.value = SwapUiState.Success(
                        resultBitmap = result.resultBitmap,
                        originalCarBitmap = car,
                        wheelBitmap = wheel,
                        description = result.description,
                        carTitle = _selectedCarName.value,
                        wheelTitle = _selectedWheelName.value
                    )
                    // Add to history
                    val item = SwapHistoryItem(
                        timestamp = "Agora",
                        carTitle = _selectedCarName.value,
                        wheelTitle = _selectedWheelName.value,
                        resultBitmap = result.resultBitmap
                    )
                    _history.value = listOf(item) + _history.value.take(4)
                }
                is GeminiWheelService.Result.Error -> {
                    _uiState.value = SwapUiState.Error(result.message)
                }
            }
        }
    }

    fun saveResultImage(bitmap: Bitmap) {
        viewModelScope.launch(Dispatchers.IO) {
            val app = getApplication<Application>()
            try {
                val filename = "WheelSwap_${System.currentTimeMillis()}.jpg"
                var saved = false

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val contentValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/WheelSwap")
                    }
                    val uri = app.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                    if (uri != null) {
                        app.contentResolver.openOutputStream(uri)?.use { stream ->
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, stream)
                            saved = true
                        }
                    }
                } else {
                    val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                    val dir = File(imagesDir, "WheelSwap")
                    if (!dir.exists()) dir.mkdirs()
                    val imageFile = File(dir, filename)
                    FileOutputStream(imageFile).use { stream ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 95, stream)
                        saved = true
                    }
                }

                if (saved) {
                    _snackMessage.value = "Imagem salva na Galeria com sucesso!"
                } else {
                    _snackMessage.value = "Não foi possível salvar na Galeria."
                }
            } catch (e: Exception) {
                _snackMessage.value = "Erro ao salvar imagem: ${e.localizedMessage}"
            }
        }
    }

    fun dismissSnack() {
        _snackMessage.value = null
    }

    fun resetToIdle() {
        _uiState.value = SwapUiState.Idle
    }

    private suspend fun decodeUriToBitmap(uri: Uri): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val app = getApplication<Application>()
            app.contentResolver.openInputStream(uri)?.use { stream ->
                val options = BitmapFactory.Options().apply { inPreferredConfig = Bitmap.Config.ARGB_8888 }
                BitmapFactory.decodeStream(stream, null, options)
            }
        } catch (e: Exception) {
            null
        }
    }
}
