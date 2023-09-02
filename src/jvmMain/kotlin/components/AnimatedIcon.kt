package components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.coroutines.launch

@Composable
fun AnimatedIcon(
    imageVector: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val scale = remember { Animatable(1f) }
    val interactionSource = MutableInteractionSource()

    Icon(
        imageVector = imageVector,
        contentDescription = contentDescription,
        modifier = Modifier.scale(scale.value)
            .clickable(interactionSource = interactionSource, indication = null) {
                coroutineScope.launch {
                    scale.animateTo(
                        0.85f,
                        animationSpec = tween(100),
                    )
                    scale.animateTo(
                        1f,
                        animationSpec = tween(100),
                    )
                }
                onClick()
            }
    )
}