package de.psdev.devdrawer.utils

import android.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
// SDK 36 requires Java 21; use SDK 33 which is compatible with the project's Java 17 toolchain.
@Config(manifest = Config.NONE, sdk = [33])
class ColorsKtTest {

    @Test
    fun `given a bright yellow background, when getting text color, then BLACK is returned`() {
        // Given
        val yellow = Color.rgb(255, 255, 0)

        // When
        val result = yellow.textColorForBackground()

        // Then
        assertEquals(Color.BLACK, result)
    }

    @Test
    fun `given a dark navy background, when getting text color, then WHITE is returned`() {
        // Given
        val navy = Color.rgb(0, 0, 128)

        // When
        val result = navy.textColorForBackground()

        // Then
        assertEquals(Color.WHITE, result)
    }

    @Test
    fun `given white background, when getting text color, then BLACK is returned`() {
        // When
        val result = Color.WHITE.textColorForBackground()

        // Then
        assertEquals(Color.BLACK, result)
    }

    @Test
    fun `given black background, when getting text color, then WHITE is returned`() {
        // When
        val result = Color.BLACK.textColorForBackground()

        // Then
        assertEquals(Color.WHITE, result)
    }

    @Test
    fun `given a saturated color, when desaturating, then saturation is reduced to 60 percent`() {
        // Given
        val red = Color.rgb(255, 0, 0)
        val originalHSV = FloatArray(3)
        Color.colorToHSV(red, originalHSV)

        // When
        val desaturated = red.getDesaturatedColor()

        // Then
        assertNotEquals(red, desaturated)
        val desaturatedHSV = FloatArray(3)
        Color.colorToHSV(desaturated, desaturatedHSV)
        assertEquals(originalHSV[1] * 0.6f, desaturatedHSV[1], 0.01f)
    }

    @Test
    fun `given a color, when desaturating, then hue is preserved`() {
        // Given
        val blue = Color.rgb(0, 0, 255)
        val originalHSV = FloatArray(3)
        Color.colorToHSV(blue, originalHSV)

        // When
        val desaturated = blue.getDesaturatedColor()

        // Then
        val desaturatedHSV = FloatArray(3)
        Color.colorToHSV(desaturated, desaturatedHSV)
        assertEquals(originalHSV[0], desaturatedHSV[0], 0.01f)
    }

    @Test
    fun `given colors with different hues, when sorting, then they are ordered by ascending hue`() {
        // Given — red hue ~0°, green hue ~120°, blue hue ~240°
        val red = Color.rgb(255, 0, 0)
        val green = Color.rgb(0, 200, 0)
        val blue = Color.rgb(0, 0, 255)

        // When
        val sorted = intArrayOf(blue, green, red).sortColorList()

        // Then
        assertEquals(listOf(red, green, blue), sorted)
    }

    @Test
    fun `given colors with equal hue but different saturation, when sorting, then lower saturation comes first`() {
        // Given — both have H≈0°; desaturatedRed S≈33%, pureRed S=100%
        val pureRed = Color.rgb(255, 0, 0)
        val desaturatedRed = Color.rgb(128, 64, 64)

        // When
        val sorted = intArrayOf(pureRed, desaturatedRed).sortColorList()

        // Then
        assertEquals(desaturatedRed, sorted[0])
        assertEquals(pureRed, sorted[1])
    }
}
