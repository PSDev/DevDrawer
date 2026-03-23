package de.psdev.devdrawer.profiles.ui.editor

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.psdev.devdrawer.R
import de.psdev.devdrawer.database.FilterType
import de.psdev.devdrawer.database.PackageFilter
import de.psdev.devdrawer.ui.dialog.DefaultDialog
import de.psdev.devdrawer.ui.theme.DevDrawerTheme

@Composable
fun PackageFilterInfoDialog(
    packageFilter: PackageFilter,
    onDismiss: () -> Unit
) {
    DefaultDialog(
        onDismissRequest = onDismiss,
        titleContent = {
            Row(
                modifier = Modifier.padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val iconRes = when (packageFilter.type) {
                    FilterType.PACKAGE_NAME -> R.drawable.ic_regex
                    FilterType.SIGNATURE -> R.drawable.ic_certificate
                }
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = null
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = stringResource(
                        id = when (packageFilter.type) {
                            FilterType.PACKAGE_NAME -> R.string.add_package_name
                            FilterType.SIGNATURE -> R.string.app_signature
                        }
                    )
                )
            }
        },
        bottomContent = {
            TextButton(
                modifier = Modifier.align(Alignment.End),
                onClick = onDismiss
            ) {
                Text(text = stringResource(id = R.string.close).toUpperCase(Locale.current))
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            if (packageFilter.type == FilterType.SIGNATURE && !packageFilter.description.isNullOrBlank()) {
                Text(
                    text = stringResource(id = R.string.name),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = packageFilter.description,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Text(
                text = stringResource(id = R.string.packagefilter),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            SelectionContainer {
                Text(
                    text = packageFilter.filter,
                    style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace)
                )
            }
        }
    }
}

@Preview(showSystemUi = true)
@Preview(showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun Preview_PackageFilterInfoDialog_Signature() {
    DevDrawerTheme {
        Surface {
            PackageFilterInfoDialog(
                packageFilter = PackageFilter(
                    profileId = "1",
                    filter = "0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF",
                    type = FilterType.SIGNATURE,
                    description = "Example App"
                ),
                onDismiss = {}
            )
        }
    }
}

@Preview(showSystemUi = true)
@Preview(showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun Preview_PackageFilterInfoDialog_PackageName() {
    DevDrawerTheme {
        Surface {
            PackageFilterInfoDialog(
                packageFilter = PackageFilter(
                    profileId = "1",
                    filter = "com.example.*",
                    type = FilterType.PACKAGE_NAME
                ),
                onDismiss = {}
            )
        }
    }
}
