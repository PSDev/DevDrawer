package de.psdev.devdrawer

import org.junit.Assert.assertEquals
import org.junit.Test
import org.w3c.dom.Element
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

/**
 * Every translatable English string and label array must have a German translation,
 * so German users never see a mixed-language UI.
 */
class TranslationCompletenessTest {

    @Test
    fun `given the English strings, when comparing with German, then no translatable string is missing`() {
        // Given
        val english = translatableNames(File(RES_DIR, "values/strings.xml"))

        // When
        val missing = english - translatableNames(File(RES_DIR, "values-de/strings.xml"))

        // Then
        assertEquals("Strings without a German translation", emptySet<String>(), missing)
    }

    @Test
    fun `given the English label arrays, when comparing with German, then no array is missing`() {
        // Given
        val english = labelArrays(File(RES_DIR, "values/strings.xml"))

        // When
        val missing = english - labelArrays(File(RES_DIR, "values-de/strings.xml"))

        // Then
        assertEquals("Label arrays without a German translation", emptySet<String>(), missing)
    }

    /** Names of `<string>` resources not marked `translatable="false"`. */
    private fun translatableNames(file: File): Set<String> = elements(file, "string")
        .filter { it.getAttribute("translatable") != "false" }
        .map { it.getAttribute("name") }
        .toSet()

    /** Names of `<array>` resources holding text labels (arrays of `@string` references need no translation). */
    private fun labelArrays(file: File): Set<String> = elements(file, "array")
        .filter { array ->
            val items = array.getElementsByTagName("item")
            (0 until items.length).any { !items.item(it).textContent.trim().startsWith("@") }
        }
        .map { it.getAttribute("name") }
        .toSet()

    private fun elements(file: File, tag: String): List<Element> {
        val nodes = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file).getElementsByTagName(tag)
        return (0 until nodes.length).map { nodes.item(it) as Element }
    }

    private companion object {
        // Unit tests run with the module directory as the working directory.
        val RES_DIR = File("src/main/res")
    }
}
