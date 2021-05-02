package de.psdev.devdrawer.ui.autocomplete

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import de.psdev.devdrawer.R
import de.psdev.devdrawer.ui.theme.DevDrawerTheme
import kotlin.math.roundToInt

@Composable
fun AutoCompleteTextView(
    modifier: Modifier = Modifier,
    options: List<String>,
    label: @Composable (() -> Unit)? = null,
    onTextChanged: (String) -> Unit = {}
) {
    val interactionSource = remember { MutableInteractionSource() }
    val textFieldFocused by interactionSource.collectIsFocusedAsState()
    var text by remember { mutableStateOf("") }
    var textPosition by remember {
        mutableStateOf(TextViewPositionData())
    }
    SelectionContainer {
        OutlinedTextField(
            modifier = modifier
                .fillMaxWidth()
                .onGloballyPositioned {
                    textPosition = TextViewPositionData(
                        positionInWindow = it.positionInWindow(),
                        positionInParent = it.positionInParent(),
                        size = it.size
                    )
                },
            value = text,
            trailingIcon = {
                if (text.isNotBlank()) {
                    Icon(
                        // TODO Fix size and clipping
                        modifier = Modifier
                            .clip(MaterialTheme.shapes.small.copy(CornerSize(percent = 50)))
                            .clickable {
                                text = ""
                                onTextChanged(text)
                            },
                        imageVector = Icons.Filled.Clear,
                        contentDescription = stringResource(id = R.string.clear)
                    )
                }
            },
            interactionSource = interactionSource,
            onValueChange = {
                text = it
                onTextChanged(text)
            },
            label = label,
            keyboardOptions = KeyboardOptions(autoCorrect = false, keyboardType = KeyboardType.Ascii)
        )
    }
    if (textFieldFocused) {
        val textFieldStart = textPosition.positionInParent.x.roundToInt()
        val textFieldBottom = (textPosition.positionInParent.y + textPosition.size.height).roundToInt()
        val popupOffset = IntOffset(textFieldStart, textFieldBottom)
        val popupWidth = with(LocalDensity.current) { textPosition.size.width.toDp() }
        val bottom = LocalView.current.height
        val popupHeightMax = with(LocalDensity.current) { (bottom - textFieldBottom).toDp() }
        Popup(
            offset = popupOffset,
        ) {
            Surface(
                modifier = Modifier
                    .requiredWidth(popupWidth)
                    .heightIn(max = popupHeightMax),
                elevation = 2.dp
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                ) {
                    items(options.filter { option -> option.contains(text) && option != text }) { packageName ->
                        Text(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    text = packageName
                                    onTextChanged(text)
                                }
                                .padding(16.dp),
                            text = packageName
                        )
                    }
                }
            }
        }
    }
}

data class TextViewPositionData(
    val positionInWindow: Offset = Offset.Unspecified,
    val positionInParent: Offset = Offset.Unspecified,
    val size: IntSize = IntSize.Zero
)

@Preview(showSystemUi = true)
@Preview(showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun Preview_AutoCompleteTextView() {
    DevDrawerTheme {
        Surface {
            Column {
                AutoCompleteTextView(
                    options = listOf(
                        "com.example.app1",
                        "com.example.app2",
                        "com.example.app3",
                        "com.example.app4",
                    ),
                    label = { Text(text = stringResource(id = R.string.packagefilter)) },
                )
            }
        }
    }
}