package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.api.FacelessCanvasConfig
import com.example.api.GeminiService
import com.example.data.AppDatabase
import com.example.data.GenerationEntity
import com.example.data.GenerationRepository
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class StudioViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: GenerationRepository
    val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = GenerationRepository(database.generationDao())
    }

    // Modern Navigation Routing State (Home / Studio / Extensions / Gallery / Profile)
    private val _currentScreen = MutableStateFlow("landing")
    val currentScreen: StateFlow<String> = _currentScreen.asStateFlow()

    // Studio Input parameters
    private val _prompt = MutableStateFlow("")
    val prompt: StateFlow<String> = _prompt.asStateFlow()

    private val _selectedStyle = MutableStateFlow("Flat Vector Premium")
    val selectedStyle: StateFlow<String> = _selectedStyle.asStateFlow()

    private val _isIslamicKidsMode = MutableStateFlow(false)
    val isIslamicKidsMode: StateFlow<Boolean> = _isIslamicKidsMode.asStateFlow()

    // AI Generation States
    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    // Holds the currently rendered faceless canvas layout configuration
    private val _activeConfig = MutableStateFlow<FacelessCanvasConfig?>(null)
    val activeConfig: StateFlow<FacelessCanvasConfig?> = _activeConfig.asStateFlow()

    // Room Persistent History & Favorites States
    val historyGenerations: StateFlow<List<GenerationEntity>> = repository.allGenerations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteGenerations: StateFlow<List<GenerationEntity>> = repository.favoriteGenerations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Core Studio Credits Sandbox
    private val _userCredits = MutableStateFlow(150)
    val userCredits: StateFlow<Int> = _userCredits.asStateFlow()

    private val _isSubscribed = MutableStateFlow(false)
    val isSubscribed: StateFlow<Boolean> = _isSubscribed.asStateFlow()

    // Extension Controls Panel
    private val _isUpscaled = MutableStateFlow(false)
    val isUpscaled: StateFlow<Boolean> = _isUpscaled.asStateFlow()

    private val _isTransparentBg = MutableStateFlow(false)
    val isTransparentBg: StateFlow<Boolean> = _isTransparentBg.asStateFlow()

    private val _isColoringPageMode = MutableStateFlow(false)
    val isColoringPageMode: StateFlow<Boolean> = _isColoringPageMode.asStateFlow()

    private val _isStickerMode = MutableStateFlow(false)
    val isStickerMode: StateFlow<Boolean> = _isStickerMode.asStateFlow()

    private val _selectedMockup = MutableStateFlow("none") // "none", "tshirt", "mug", "poster"
    val selectedMockup: StateFlow<String> = _selectedMockup.asStateFlow()

    private val _beforeAfterSplit = MutableStateFlow(0.5f) // split ratio for slider
    val beforeAfterSplit: StateFlow<Float> = _beforeAfterSplit.asStateFlow()

    // Styles supported in Dropdown
    val availableStyles = listOf(
        "Flat Vector Premium",
        "Cute Character",
        "Islamic Illustration",
        "3D Cute",
        "Storybook",
        "Cartoon Mascot",
        "Minimalist",
        "Soft Aesthetic",
        "Sticker Style",
        "Kids Illustration"
    )

    fun navigateTo(screen: String) {
        _currentScreen.value = screen
    }

    fun updatePrompt(newStr: String) {
        _prompt.value = newStr
    }

    fun selectStyle(style: String) {
        _selectedStyle.value = style
        if (style == "Islamic Illustration") {
            _isIslamicKidsMode.value = true
        }
    }

    fun setIslamicMode(enabled: Boolean) {
        _isIslamicKidsMode.value = enabled
        if (enabled) {
            _selectedStyle.value = "Islamic Illustration"
        }
    }

    fun setBeforeAfterSplit(ratio: Float) {
        _beforeAfterSplit.value = ratio
    }

    // Toggle states
    fun toggleUpscaler() { _isUpscaled.value = !_isUpscaled.value }
    fun toggleBackgroundRemover() { _isTransparentBg.value = !_isTransparentBg.value }
    fun toggleColoringPage() { _isColoringPageMode.value = !_isColoringPageMode.value }
    fun toggleStickerMode() { _isStickerMode.value = !_isStickerMode.value }
    fun selectMockup(type: String) { _selectedMockup.value = type }

    // Enhancer API call
    fun enhanceWithAI() {
        if (_prompt.value.trim().isEmpty()) return
        viewModelScope.launch {
            _isGenerating.value = true
            val enhanced = GeminiService.enhancePrompt(_prompt.value, _selectedStyle.value, _isIslamicKidsMode.value)
            _prompt.value = enhanced
            _isGenerating.value = false
        }
    }

    // Generation engine
    fun generateImage() {
        val promptText = if (_prompt.value.trim().isEmpty()) "A cute hijab girl developer working on a laptop" else _prompt.value
        viewModelScope.launch {
            _isGenerating.value = true
            
            // Deduct credits if not unlimited
            if (!_isSubscribed.value && _userCredits.value > 0) {
                _userCredits.value = _userCredits.value - 10
            }

            // Real Gemini integration query
            val canvasConfig = GeminiService.generateFacelessConfig(
                prompt = promptText,
                style = _selectedStyle.value,
                isIslamicKidsMode = _isIslamicKidsMode.value
            )

            // Overwrite styling properties based on generator response
            _selectedStyle.value = canvasConfig.styleName
            _isIslamicKidsMode.value = canvasConfig.hijab

            _activeConfig.value = canvasConfig

            // Map and persist to local Room DB History
            val jsonAdapter = moshi.adapter(FacelessCanvasConfig::class.java)
            val jsonStr = jsonAdapter.toJson(canvasConfig)

            repository.insert(
                GenerationEntity(
                    prompt = promptText,
                    enhancedPrompt = canvasConfig.explanationShort,
                    styleName = canvasConfig.styleName,
                    colorPalette = "${canvasConfig.hijabColor},${canvasConfig.outfitColor},${canvasConfig.accessoryColor}",
                    renderJson = jsonStr,
                    isFavorite = false,
                    mode = if (canvasConfig.hijab) "Islamic Kids" else "Default",
                    isSticker = canvasConfig.styleName.contains("Sticker", ignoreCase = true)
                )
            )

            // Reset sandbox extension views to default unswitched layout states
            _isUpscaled.value = false
            _isTransparentBg.value = false
            _isColoringPageMode.value = false
            _isStickerMode.value = canvasConfig.styleName.contains("Sticker", ignoreCase = true)
            _selectedMockup.value = "none"

            _isGenerating.value = false
            // Auto navigate to active extensions preview screen to let user design modifications!
            _currentScreen.value = "studio"
        }
    }

    // Suggestion picker click
    fun applySuggestion(suggestion: String) {
        _prompt.value = suggestion
    }

    // Toggle Favorite
    fun toggleFavorite(id: Long, isFav: Boolean) {
        viewModelScope.launch {
            repository.toggleFavorite(id, isFav)
        }
    }

    // Load custom config from history
    fun loadConfig(entity: GenerationEntity) {
        try {
            val jsonAdapter = moshi.adapter(FacelessCanvasConfig::class.java)
            val config = jsonAdapter.fromJson(entity.renderJson)
            if (config != null) {
                _activeConfig.value = config
                _prompt.value = entity.prompt
                _selectedStyle.value = entity.styleName
                _isIslamicKidsMode.value = entity.mode == "Islamic Kids"
                _currentScreen.value = "studio"
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun submitSubscription() {
        _isSubscribed.value = true
        _userCredits.value = 9999
    }

    fun deleteHistoryId(id: Long) {
        viewModelScope.launch {
            repository.deleteById(id)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }
}

// Factory Helper
class StudioViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StudioViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StudioViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
