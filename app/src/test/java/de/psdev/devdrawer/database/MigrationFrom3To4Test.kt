package de.psdev.devdrawer.database

import android.graphics.Color
import androidx.room.testing.MigrationTestHelper
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
// SDK 36 requires Java 21; use SDK 33 which is compatible with the project's Java 17 toolchain.
@Config(sdk = [33])
class MigrationFrom3To4Test {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        DevDrawerDatabase::class.java
    )

    @Test
    fun `given widgets with legacy colours, when migrating to 4, then each legacy colour maps to its closest header colour`() {
        // Given
        helper.createDatabase(TEST_DB, 3).use { db ->
            db.execSQL("INSERT INTO `widget_profiles` (`id`, `name`, `updatedAt`) VALUES ('p1', 'Default', 0)")
            db.execSQL("INSERT INTO `widgets` (`id`, `name`, `color`, `profile_id`) VALUES (1, 'BLACK', ${Color.BLACK}, 'p1')")
            db.execSQL("INSERT INTO `widgets` (`id`, `name`, `color`, `profile_id`) VALUES (2, 'DKGRAY', ${Color.DKGRAY}, 'p1')")
            db.execSQL("INSERT INTO `widgets` (`id`, `name`, `color`, `profile_id`) VALUES (3, 'GRAY', ${Color.GRAY}, 'p1')")
            db.execSQL("INSERT INTO `widgets` (`id`, `name`, `color`, `profile_id`) VALUES (4, 'LTGRAY', ${Color.LTGRAY}, 'p1')")
            db.execSQL("INSERT INTO `widgets` (`id`, `name`, `color`, `profile_id`) VALUES (5, 'WHITE', ${Color.WHITE}, 'p1')")
            db.execSQL("INSERT INTO `widgets` (`id`, `name`, `color`, `profile_id`) VALUES (6, 'RED', ${Color.RED}, 'p1')")
            db.execSQL("INSERT INTO `widgets` (`id`, `name`, `color`, `profile_id`) VALUES (7, 'GREEN', ${Color.GREEN}, 'p1')")
            db.execSQL("INSERT INTO `widgets` (`id`, `name`, `color`, `profile_id`) VALUES (8, 'BLUE', ${Color.BLUE}, 'p1')")
            db.execSQL("INSERT INTO `widgets` (`id`, `name`, `color`, `profile_id`) VALUES (9, 'YELLOW', ${Color.YELLOW}, 'p1')")
            db.execSQL("INSERT INTO `widgets` (`id`, `name`, `color`, `profile_id`) VALUES (10, 'CYAN', ${Color.CYAN}, 'p1')")
            db.execSQL("INSERT INTO `widgets` (`id`, `name`, `color`, `profile_id`) VALUES (11, 'MAGENTA', ${Color.MAGENTA}, 'p1')")
            db.execSQL("INSERT INTO `widgets` (`id`, `name`, `color`, `profile_id`) VALUES (12, 'Custom', ${0xFF123456.toInt()}, 'p1')")
        }

        // When
        val db = helper.runMigrationsAndValidate(TEST_DB, 4, true, MigrationFrom3To4)

        // Then
        val headerColors = mutableMapOf<Int, String>()
        db.query("SELECT `id`, `header_color`, `sort_order` FROM `widgets`").use { cursor ->
            while (cursor.moveToNext()) {
                headerColors[cursor.getInt(0)] = cursor.getString(1)
                assertNull(cursor.getString(2))
            }
        }
        assertEquals(mapOf(1 to "DARK", 2 to "DARK", 3 to "NEUTRAL", 4 to "NEUTRAL", 5 to "NEUTRAL", 6 to "RED", 7 to "GREEN", 8 to "BLUE", 9 to "AMBER", 10 to "TEAL", 11 to "PURPLE", 12 to "AMBER"), headerColors)
    }

    private companion object {
        const val TEST_DB = "migration-test"
    }
}
