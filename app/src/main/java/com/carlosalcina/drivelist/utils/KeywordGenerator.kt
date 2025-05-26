package com.carlosalcina.drivelist.utils

import java.text.Normalizer

object KeywordGenerator {

    // Palabras comunes a ignorar
    private val stopWords = setOf(
        "un", "una", "unos", "unas", "el", "la", "los", "las", "de", "del", "en", "y", "o", "u",
        "a", "ante", "bajo", "cabe", "con", "contra", "desde", "durante", "entre", "hacia",
        "hasta", "mediante", "para", "por", "según", "sin", "so", "sobre", "tras", "versus", "vía",
        "es", "esta", "este", "muy", "coche", "auto", "vehiculo",
        "the", "a", "is", "in", "and", "of", "to", "for", "with", "on", "car", "vehicle"
    )

    // Normaliza y limpia un texto en palabras clave
    private fun normalizeAndClean(text: String?): List<String> {
        if (text.isNullOrBlank()) return emptyList()

        var normalized = text.lowercase()
        normalized = Normalizer.normalize(normalized, Normalizer.Form.NFD)
        normalized = Regex("\\p{InCombiningDiacriticalMarks}+").replace(normalized, "")
        normalized = Regex("[^a-z0-9ñ ]").replace(normalized, " ")

        return normalized.split("\\s+".toRegex())
            .map { it.trim() }
            .filter { it.length > 2 && it !in stopWords }
            .distinct()
    }

    // Genera todos los prefijos útiles de una palabra (mínimo 2 letras)
    private fun generatePrefixes(word: String): List<String> {
        return (2..word.length).map { i -> word.substring(0, i) }
    }

    // Método principal: genera todas las keywords y sus prefijos
    fun generateKeywords(
        brand: String,
        model: String,
        version: String,
        carColorName: String?,
        fuelType: String?,
        year: String?,
        ciudad: String?,
        comunidadAutonoma: String?
    ): List<String> {
        val rawWords = mutableSetOf<String>()
        rawWords.addAll(normalizeAndClean(brand))
        rawWords.addAll(normalizeAndClean(model))
        rawWords.addAll(normalizeAndClean(version))
        rawWords.addAll(normalizeAndClean(fuelType))
        rawWords.addAll(normalizeAndClean(year))
        rawWords.addAll(normalizeAndClean(ciudad))
        rawWords.addAll(normalizeAndClean(comunidadAutonoma))

        if (!carColorName.isNullOrBlank()) {
            val colorEnumNameProcessed = carColorName.lowercase().replace("_", " ")
            rawWords.addAll(normalizeAndClean(colorEnumNameProcessed))
        }

        val keywords = mutableSetOf<String>()
        for (word in rawWords) {
            keywords.add(word)
            keywords.addAll(generatePrefixes(word))
        }

        return keywords.toList()
    }
}
