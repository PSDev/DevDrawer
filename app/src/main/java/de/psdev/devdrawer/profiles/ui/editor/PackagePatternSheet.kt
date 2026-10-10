package de.psdev.devdrawer.profiles.ui.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import de.psdev.devdrawer.R
import de.psdev.devdrawer.appwidget.AppInfo

/** What a package name pattern being typed would match. */
data class PatternPreview(
    val pattern: String = "",
    val isValid: Boolean = false,
    val matchCount: Int = 0,
    /** The first matching apps, by name. */
    val apps: List<AppInfo> = emptyList()
)

/** Enter a package name pattern and see which installed apps it matches before adding it. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PackagePatternSheet(
    preview: PatternPreview,
    onPatternChange: (String) -> Unit,
    onAdd: (String) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        Column(
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = stringResource(R.string.setup_source_pattern), style = MaterialTheme.typography.titleLarge)
            Text(
                text = stringResource(R.string.add_filter_pattern_text),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            // The field keeps its own text: the preview is computed asynchronously, and feeding it back into the
            // field would drop or reorder characters typed quickly. It only describes the text once it caught up.
            var text by rememberSaveable { mutableStateOf(preview.pattern) }
            val upToDate = preview.pattern == text
            val invalid = upToDate && text.isNotBlank() && !preview.isValid
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = text,
                onValueChange = {
                    text = it
                    onPatternChange(it)
                },
                singleLine = true,
                isError = invalid,
                label = { Text(stringResource(R.string.packagefilter)) },
                placeholder = { Text(stringResource(R.string.setup_source_pattern_summary)) },
                supportingText = when {
                    invalid -> {
                        { Text(stringResource(R.string.pattern_invalid)) }
                    }
                    upToDate && text.isNotBlank() -> {
                        { Text(pluralStringResource(R.plurals.pattern_match_count, preview.matchCount, preview.matchCount)) }
                    }
                    else -> null
                },
                keyboardOptions = KeyboardOptions(autoCorrectEnabled = false, keyboardType = KeyboardType.Uri)
            )
            preview.apps.forEach { AppInfoItem(appInfo = it) }
            val hidden = preview.matchCount - preview.apps.size
            if (hidden > 0) {
                Text(
                    text = pluralStringResource(R.plurals.widget_more_apps, hidden, hidden),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Button(
                modifier = Modifier.align(Alignment.End),
                enabled = upToDate && preview.isValid,
                onClick = { onAdd(text.trim()) }
            ) {
                Text(stringResource(R.string.add))
            }
        }
    }
}
