package com.recipeapp.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup

/**
 * Downloads a recipe web page and extracts plain readable text (stripping nav,
 * ads, scripts, etc.) so we only send Claude the useful content. Many recipe
 * sites bury the actual recipe under paragraphs of life story -- Claude handles
 * sifting through that fine, but trimming obvious boilerplate first keeps
 * requests smaller and cheaper.
 */
object PageFetcher {

    suspend fun fetchReadableText(url: String): String = withContext(Dispatchers.IO) {
        val doc = Jsoup.connect(url)
            .userAgent("Mozilla/5.0 (Android) RecipeApp/0.1")
            .timeout(15_000)
            .get()

        // Drop obviously irrelevant elements.
        doc.select("script, style, nav, footer, header, noscript, iframe, svg").remove()

        // Prefer common recipe-container selectors if present, else fall back to body.
        val candidates = listOf(
            "[itemtype*=Recipe]",
            ".recipe",
            "#recipe",
            "article"
        )
        val container = candidates
            .asSequence()
            .mapNotNull { doc.select(it).firstOrNull() }
            .firstOrNull()
            ?: doc.body()

        container.text().take(20_000) // keep request size sane
    }
}
