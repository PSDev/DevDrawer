package de.psdev.devdrawer.ui

import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.core.graphics.drawable.toBitmap

/**
 * An app's icon at [size]. Rendered at that size rather than the drawable's own, which is missing for some drawables
 * (and would then fail to convert); an empty space of the same size when there is no icon.
 */
@Composable
fun AppIcon(icon: Drawable?, size: Dp, modifier: Modifier = Modifier) {
    val sizePx = with(LocalDensity.current) { size.roundToPx() }
    val bitmap = remember(icon, sizePx) { icon?.toBitmap(sizePx, sizePx)?.asImageBitmap() }
    if (bitmap != null) {
        Image(bitmap = bitmap, contentDescription = null, modifier = modifier.size(size))
    } else {
        Spacer(modifier.size(size))
    }
}
