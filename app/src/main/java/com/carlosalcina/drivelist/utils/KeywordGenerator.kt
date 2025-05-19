package com.carlosalcina.drivelist.utils

import java.text.Normalizer

object KeywordGenerator {

    // Lista de palabras comunes a ignorar
    private val stopWords = setOf(
        "un", "una", "unos", "unas", "el", "la", "los", "las", "de", "del", "en", "y", "o", "u",
        "a", "ante", "bajo", "cabe", "con", "contra", "desde", "durante", "entre", "hacia",
        "hasta", "mediante", "para", "por", "según", "sin", "so", "sobre", "tras", "versus", "vía",
        "es", "esta", "este", "muy", "coche", "auto", "vehiculo",
        "the", "a", "is", "in", "and", "of", "to", "for", "with", "on", "car", "vehicle"
    )

    private fun normalizeAndClean(text: String?): List<String> {
        if (text.isNullOrBlank()) return emptyList()

        // 1. Convertir a minúsculas
        var normalized = text.lowercase()
        // 2. Quitar tildes y diacríticos (NFD: Normalization Form D - Canonical Decomposition)
        normalized = Normalizer.normalize(normalized, Normalizer.Form.NFD)
        normalized = Regex("\\p{InCombiningDiacriticalMarks}+").replace(normalized, "")
        // 3. Reemplazar caracteres no alfanuméricos (excepto espacios) con un espacio
        //    Esto ayuda a separar palabras unidas por guiones, etc.
        normalized = Regex("[^a-z0-9ñ ]").replace(normalized, " ")
        // 4. Dividir en palabras y filtrar stop words y palabras cortas
        return normalized.split("\\s+".toRegex())
            .map { it.trim() }
            .filter { it.length > 2 && it !in stopWords } // Palabras con más de 2 caracteres y no stop words
            .distinct() // Evitar duplicados de la misma fuente
    }

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
        val keywords = mutableSetOf<String>()
        keywords.addAll(normalizeAndClean(brand))
        keywords.addAll(normalizeAndClean(model))
        keywords.addAll(normalizeAndClean(version))
        keywords.addAll(normalizeAndClean(fuelType))
        keywords.addAll(normalizeAndClean(year))
        keywords.addAll(normalizeAndClean(ciudad))
        keywords.addAll(normalizeAndClean(comunidadAutonoma))

        if (!carColorName.isNullOrBlank()) {
            val colorEnumNameProcessed = carColorName.lowercase().replace("_", " ")
            keywords.addAll(normalizeAndClean(colorEnumNameProcessed))

        }

        return keywords.toList()
    }
}