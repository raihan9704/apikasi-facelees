package com.example.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GenerateContentRequest(
    @Json(name = "contents") val contents: List<Content>,
    @Json(name = "generationConfig") val generationConfig: GenerationConfig? = null,
    @Json(name = "systemInstruction") val systemInstruction: Content? = null
)

@JsonClass(generateAdapter = true)
data class Content(
    @Json(name = "parts") val parts: List<Part>
)

@JsonClass(generateAdapter = true)
data class Part(
    @Json(name = "text") val text: String? = null
)

@JsonClass(generateAdapter = true)
data class GenerationConfig(
    @Json(name = "responseMimeType") val responseMimeType: String? = null,
    @Json(name = "responseSchema") val responseSchema: ResponseSchema? = null,
    @Json(name = "temperature") val temperature: Float? = null,
    @Json(name = "topP") val topP: Float? = null,
    @Json(name = "topK") val topK: Int? = null
)

@JsonClass(generateAdapter = true)
data class ResponseSchema(
    @Json(name = "type") val type: String,
    @Json(name = "description") val description: String? = null,
    @Json(name = "properties") val properties: Map<String, SchemaProperty>? = null,
    @Json(name = "required") val required: List<String>? = null,
    @Json(name = "items") val items: ResponseSchema? = null
)

@JsonClass(generateAdapter = true)
data class SchemaProperty(
    @Json(name = "type") val type: String,
    @Json(name = "description") val description: String? = null,
    @Json(name = "enum") val enum: List<String>? = null
)

@JsonClass(generateAdapter = true)
data class GenerateContentResponse(
    @Json(name = "candidates") val candidates: List<Candidate>?
)

@JsonClass(generateAdapter = true)
data class Candidate(
    @Json(name = "content") val content: Content?
)

// --- Faceless AI Art Custom Vector Canvas JSON representation ---
@JsonClass(generateAdapter = true)
data class FacelessCanvasConfig(
    @Json(name = "entity") val entity: String, // "human", "animal", "inanimate"
    @Json(name = "gender") val gender: String, // "female", "male", "other" (if human)
    @Json(name = "hijab") val hijab: Boolean, // if female, wears hijab
    @Json(name = "apparelType") val apparelType: String, // "casual", "suit", "traditional", "robe", "none" (if animal)
    @Json(name = "hijabColor") val hijabColor: String, // hex value, e.g. "#FF5722"
    @Json(name = "skinColor") val skinColor: String, // hex value
    @Json(name = "outfitColor") val outfitColor: String, // hex value
    @Json(name = "accessory") val accessory: String, // "book", "laptop", "coffee", "brush", "cat", "balloon", "none"
    @Json(name = "accessoryColor") val accessoryColor: String, // hex value
    @Json(name = "backgroundColor") val backgroundColor: String, // hex value for background
    @Json(name = "ambientElements") val ambientElements: List<String>, // list of "stars", "clouds", "leaves", "dots"
    @Json(name = "styleName") val styleName: String,
    @Json(name = "creatureType") val creatureType: String, // e.g. "cat", "bird", "bear" (if animal)
    @Json(name = "isLivingCreature") val isLivingCreature: Boolean,
    @Json(name = "explanationShort") val explanationShort: String // highly specific context
)
