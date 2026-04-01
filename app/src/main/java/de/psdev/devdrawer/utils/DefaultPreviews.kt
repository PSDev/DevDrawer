package de.psdev.devdrawer.utils

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Light (German)", locale = "de")
@Preview(name = "Dark (German)", uiMode = Configuration.UI_MODE_NIGHT_YES, locale = "de")
annotation class DefaultPreviews()
