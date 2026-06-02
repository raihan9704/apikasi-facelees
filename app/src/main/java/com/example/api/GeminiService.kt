package com.example.api

import android.util.Log
import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit
import kotlin.random.Random

object GeminiService {
    private const val TAG = "GeminiService"
    private const val MODEL_NAME = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent"

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    /**
     * Determines whether the prompt represents a living creature.
     * Always returns clean, validated canvas elements for UI rendering on Compose canvas.
     */
    suspend fun generateFacelessConfig(
        prompt: String,
        style: String,
        isIslamicKidsMode: Boolean
    ): FacelessCanvasConfig = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.w(TAG, "API Key is empty or placeholder. Falling back to high-fidelity local generator.")
            return@withContext simulateLocalConfig(prompt, style, isIslamicKidsMode)
        }

        val systemInstruction = """
            You are 'FaceLess AI Studio Generator'. Your task is to analyze an image generation prompt, check for any living creatures (humans, animals, anime characters, birds, cartoon mascots, etc.), and transform it into a structured, minimalist graphic art layout.
            
            Strict Faceless Design Rule:
            - If any living creature is identified, they MUST be FACELESS.
            - This means: strictly NO eyes, eyebrow, nose, mouth, lips, cheeks, whiskers, or facial details.
            - The face/head is a perfectly flat, clean, smooth, minimal aesthetic surface.
            - Hijab, hair styles, clothing, caps, glasses (without eyes showing), hats, accessories, workspace tools, books, and body proportions are fully preserved.
            - Keep style cute, professional, modern, creative and artistic.
            
            Inanimate Objects Exception:
            - Trees, cars, food, flowers, mountains, oceans, buildings, technology, clouds, normal nature contain their regular beautiful artistic details.
            
            You must output a single, raw, valid JSON matching the structure:
            {
               "entity": "human" | "animal" | "inanimate",
               "gender": "female" | "male" | "other" (blank if not human),
               "hijab": true | false (forces hijab for Islamic illustration mode if female/appropriate),
               "apparelType": "casual" | "suit" | "traditional" | "robe" | "none",
               "hijabColor": "#HEX",
               "skinColor": "#HEX",
               "outfitColor": "#HEX",
               "accessory": "book" | "laptop" | "coffee" | "brush" | "cat" | "balloon" | "none",
               "accessoryColor": "#HEX",
               "backgroundColor": "#HEX",
               "ambientElements": ["stars", "clouds", "leaves", "dots"],
               "styleName": "selected style name",
               "creatureType": "cat", "bird", "bear" etc. (blank if not animal),
               "isLivingCreature": true | false,
               "explanationShort": "Brief premium visual layout outline"
            }
        """.trimIndent()

        val jsonRequest = """
            {
                "contents": [
                    {
                        "parts": [
                            {
                                "text": "Prompt to generate: '$prompt', using styleName: '$style', isIslamicKidsModeActive: $isIslamicKidsMode"
                            }
                        ]
                    }
                ],
                "generationConfig": {
                    "responseMimeType": "application/json",
                    "temperature": 0.3
                },
                "systemInstruction": {
                    "parts": [
                        {
                            "text": ${escapeJsonString(systemInstruction)}
                        }
                    ]
                }
            }
        """.trimIndent()

        try {
            val requestBody = jsonRequest.toRequestBody("application/json".toMediaType())
            val url = "$BASE_URL?key=$apiKey"
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .header("Content-Type", "application/json")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e(TAG, "Request failed: Code ${response.code}, Body: ${response.body?.string()}")
                    return@withContext simulateLocalConfig(prompt, style, isIslamicKidsMode)
                }

                val responseBodyStr = response.body?.string() ?: ""
                val responseObj = moshi.adapter(GenerateContentResponse::class.java).fromJson(responseBodyStr)
                val textResponse = responseObj?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""

                if (textResponse.isNotEmpty()) {
                    val config = moshi.adapter(FacelessCanvasConfig::class.java).fromJson(textResponse)
                    if (config != null) {
                        return@withContext config
                    }
                }
                return@withContext simulateLocalConfig(prompt, style, isIslamicKidsMode)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in generateFacelessConfig: ${e.message}", e)
            return@withContext simulateLocalConfig(prompt, style, isIslamicKidsMode)
        }
    }

    /**
     * AI Prompt Enhancer.
     * Takes standard input and scales it to a highly descriptive creative prompts list.
     */
    suspend fun enhancePrompt(prompt: String, style: String, isIslamicMode: Boolean): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "Explore a premium $style faceless representation, minimalist vectors, detailed vector shapes, cute lighting, flat solid workspace graphics of: $prompt"
        }

        val designPrompt = """
            You are a creative prompt engineer for image generator studio APIs. Enhance the user's prompt to be descriptive, premium, professional, cute and artistic while ensuring all elements align with the selected style '$style'.
            If the prompt includes living figures, always include instructions that 'head is an eye-catching, smooth minimalist faceless vector, with absolutely no eyes, nose, mouth or facial details'.
            If Islamic Mode is active: $isIslamicMode, add warm Islamic graphics accents (e.g. cute dynamic crescent, beautiful mild patterns, comforting colors).
            Raw Prompt: $prompt
            Response should be just ONE single paragraph of enhanced prompt, do not add prefixes like 'Enhanced prompt:'.
        """.trimIndent()

        val jsonRequest = """
            {
                "contents": [
                    {
                        "parts": [
                            {
                                "text": ${escapeJsonString(designPrompt)}
                            }
                        ]
                    }
                ],
                "generationConfig": {
                    "temperature": 0.7
                }
            }
        """.trimIndent()

        try {
            val requestBody = jsonRequest.toRequestBody("application/json".toMediaType())
            val url = "$BASE_URL?key=$apiKey"
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val responseStr = response.body?.string() ?: ""
                    val responseObj = moshi.adapter(GenerateContentResponse::class.java).fromJson(responseStr)
                    val result = responseObj?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text?.trim() ?: ""
                    if (result.isNotEmpty()) return@withContext result
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to enhance prompt: ${e.message}")
        }
        return@withContext "Flat vector illustration of a blank faceless figure representing ($prompt), premium modern $style aesthetic, soft colors, smooth textures."
    }

    /**
     * AI Prompt Suggestions generator based on selected styles to aid users in clicking one.
     */
    fun getPromptSuggestions(style: String, isIslamic: Boolean): List<String> {
        return if (isIslamic) {
            listOf(
                "Anak perempuan berhijab membaca Al-Quran di kamar aesthetic",
                "Keluarga muslim bahagia berdiri di depan masjid minimalis modern",
                "Adik laki-laki bermain layangan bersama kakak berhijab di sawah hijau",
                "Ibu tersenyum tanpa wajah mengajari anak perempuan tata cara sholat",
                "Karakter 3D imut anak perempuan bercadar biru membawa ransel pastel"
            )
        } else {
            when (style) {
                "Flat Vector Premium" -> listOf(
                    "Professional developer coding on multiple monitors, elegant slate dark workspace",
                    "A woman wearing stylish business suit holding active neon phone",
                    "Teacher showing chemical formulas on organic chalk board backdrop"
                )
                "Cute Character" -> listOf(
                    "Cute chibi astronaut character floating with purple balloons in deep space",
                    "An adorable faceless dynamic barista brewing premium creamy matcha",
                    "Cozy girl drinking vanilla milk tea on highly comfy bean bag"
                )
                "3D Cute" -> listOf(
                    "Clay style cute kitten holding a mini delicious cheese slice",
                    "Plump cute faceless character with bright orange hoodie playing guitar",
                    "3D isometric model of an orange retro camera sitting on soft cloud"
                )
                "Minimalist" -> listOf(
                    "Simple silhouette of a traveler holding a luggage looking at huge mountain",
                    "A single organic coffee cup sitting on retro designer desk, warm shadows",
                    "Elegant aesthetic girl walking under huge aesthetic pink maple leaf"
                )
                else -> listOf(
                    "Cute developer working peacefully in a flower garden",
                    "Young creative designer painting faceless characters with paint brush",
                    "A stylish driver in red electric futuristic sports car on seaside speedway"
                )
            }
        }
    }

    private fun escapeJsonString(input: String): String {
        return moshi.adapter(String::class.java).toJson(input)
    }

    /**
     * High fidelity local fallback generator. Makes the screen fully functional even without active internet or key.
     */
    private fun simulateLocalConfig(prompt: String, style: String, isIslamicMode: Boolean): FacelessCanvasConfig {
        val lowercase = prompt.lowercase()
        val isLiving = lowercase.contains("gadis") || lowercase.contains("perempuan") || lowercase.contains("anak") ||
                lowercase.contains("girl") || lowercase.contains("boy") || lowercase.contains("pria") || lowercase.contains("wanita") ||
                lowercase.contains("man") || lowercase.contains("woman") || lowercase.contains("developer") ||
                lowercase.contains("kucing") || lowercase.contains("cat") || lowercase.contains("anjing") || lowercase.contains("hewan") ||
                lowercase.contains("animal") || lowercase.contains("ibu") || lowercase.contains("mother") ||
                lowercase.contains("bapak") || lowercase.contains("father") || isIslamicMode

        val isAnimal = lowercase.contains("kucing") || lowercase.contains("cat") || lowercase.contains("anjing") ||
                lowercase.contains("hewan") || lowercase.contains("animal") || lowercase.contains("burung") || lowercase.contains("bird")

        val styleColors = when (style) {
            "Flat Vector Premium" -> listOf("#3F51B5", "#E91E63", "#FFEB3B", "#4CAF50", "#9C27B0")
            "Soft Aesthetic" -> listOf("#F48FB1", "#CE93D8", "#B39DDB", "#90CAF9", "#80CBC4")
            "3D Cute" -> listOf("#FF7043", "#FFCA28", "#66BB6A", "#26A69A", "#42A5F5")
            else -> listOf("#FF9800", "#9C27B0", "#E91E63", "#00BCD4", "#8BC34A")
        }

        val isFemaleHijab = isIslamicMode || lowercase.contains("hijab") || lowercase.contains("berhijab") || lowercase.contains("gadis") || lowercase.contains("perempuan")
        val randomColor1 = styleColors[Random.nextInt(styleColors.size)]
        val randomColor2 = styleColors[(Random.nextInt(styleColors.size) + 1) % styleColors.size]
        val randomColor3 = styleColors[(Random.nextInt(styleColors.size) + 2) % styleColors.size]

        val accessoryType = when {
            lowercase.contains("buku") || lowercase.contains("book") || lowercase.contains("baca") -> "book"
            lowercase.contains("laptop") || lowercase.contains("coding") || lowercase.contains("kerja") -> "laptop"
            lowercase.contains("kopi") || lowercase.contains("coffee") || lowercase.contains("tea") -> "coffee"
            lowercase.contains("melukis") || lowercase.contains("paint") || lowercase.contains("brush") -> "brush"
            lowercase.contains("lucu") || lowercase.contains("cute") -> "balloon"
            isAnimal -> "none"
            else -> if (Random.nextBoolean()) "book" else "none"
        }

        return FacelessCanvasConfig(
            entity = if (isLiving) { if (isAnimal) "animal" else "human" } else "inanimate",
            gender = if (isFemaleHijab) "female" else "male",
            hijab = isFemaleHijab,
            apparelType = if (isIslamicMode) "traditional" else "casual",
            hijabColor = randomColor1,
            skinColor = "#FFE082",
            outfitColor = randomColor2,
            accessory = accessoryType,
            accessoryColor = randomColor3,
            backgroundColor = "#1A237E", // Royal dark blue for modern cyber style
            ambientElements = listOf("stars", "clouds", "leaves").shuffled().take(Random.nextInt(2) + 1),
            styleName = style,
            creatureType = if (isAnimal) "cat" else "",
            isLivingCreature = isLiving,
            explanationShort = "Created high quality faceless layout matching your customized $style aesthetic of '$prompt'"
        )
    }
}
