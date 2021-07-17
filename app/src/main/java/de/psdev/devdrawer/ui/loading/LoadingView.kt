package de.psdev.devdrawer.ui.loading

import androidx.compose.foundation.layout.*
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.psdev.devdrawer.R
import de.psdev.devdrawer.ui.theme.DevDrawerTheme

@Composable
fun LoadingView(
    modifier: Modifier = Modifier,
    showText: Boolean = true
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(modifier = Modifier.size(64.dp))
        if (showText) {
            Spacer(modifier = Modifier.size(16.dp))
            Text(text = stringResource(id = R.string.loading))
        }
    }
}

@Preview
@Composable
fun Preview_LoadingView() {
    DevDrawerTheme {
        Surface(color = MaterialTheme.colors.background) {
            LoadingView()
        }
    }
}