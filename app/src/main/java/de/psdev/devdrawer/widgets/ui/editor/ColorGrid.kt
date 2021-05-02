package de.psdev.devdrawer.widgets.ui.editor

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.psdev.devdrawer.ui.theme.DevDrawerTheme

@Composable
fun ColorGrid(
    initialColor: Int,
    onColorClicked: (Int) -> Unit = {}
) {
    var selectedColor by remember { mutableStateOf(initialColor) }
    val colors = listOf(
        android.graphics.Color.BLACK,
        android.graphics.Color.DKGRAY,
        android.graphics.Color.GRAY,
        android.graphics.Color.LTGRAY,
        android.graphics.Color.WHITE,
        android.graphics.Color.RED,
        android.graphics.Color.GREEN,
        android.graphics.Color.BLUE,
        android.graphics.Color.YELLOW,
        android.graphics.Color.CYAN,
        android.graphics.Color.MAGENTA
    )
    LazyVerticalGrid(
        modifier = Modifier.wrapContentHeight(),
        cells = GridCells.Adaptive(minSize = 64.dp),
        contentPadding = PaddingValues(8.dp)
    ) {
        items(colors) { color ->
            val isSelectedColor = color == selectedColor
            ColorBox(
                isSelectedColor = isSelectedColor,
                color = color
            ) {
                selectedColor = it
                onColorClicked(it)
            }
        }
    }
}

@Composable
fun ColorBox(
    modifier: Modifier = Modifier,
    isSelectedColor: Boolean,
    color: Int,
    onColorClicked: (Int) -> Unit
) {
    val cornerSize by animateDpAsState(
        targetValue = if (isSelectedColor) 8.dp else 0.dp
    )
    val borderWidth by animateDpAsState(
        targetValue = if (isSelectedColor) 2.dp else 1.dp
    )
    val borderColor by animateColorAsState(
        targetValue = Color(if (isSelectedColor) android.graphics.Color.WHITE else android.graphics.Color.BLACK)
    )
    val shape = RoundedCornerShape(
        size = cornerSize
    )
    Box(modifier = modifier
        .padding(8.dp)
        .requiredSize(48.dp)
        .clip(shape)
        .border(
            width = borderWidth,
            color = borderColor,
            shape = shape
        )
        .background(Color(color), shape = shape)
        .clickable {
            onColorClicked(color)
        }
    )
}

@Preview
@Composable
fun Preview_ColorGrid() {
    DevDrawerTheme {
        ColorGrid(
            initialColor = android.graphics.Color.BLACK
        )
    }
}