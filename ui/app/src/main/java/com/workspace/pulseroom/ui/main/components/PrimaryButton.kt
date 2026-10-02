package com.workspace.pulseroom.ui.main.components

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.workspace.pulseroom.theme.PlusJakartaSans

// Temporary token definitions (Move these to Color.kt later)
val GradientStart = Color(0xFFFF7A45)
val GradientEnd = Color(0xFFFF5722)
val ShadowColor = Color(0x33FF5722)

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isTablet: Boolean = false
){
    // 1. Interaction Source: The core of Compose touch handling
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // 2. The Physics Engine: Animating the scale based on touch state
    val scale by animateFloatAsState(
        targetValue = if(isPressed) 0.96f else 1f,// Scale down to 96% on press[cite: 15]
        animationSpec = tween(
            durationMillis = 180,
            easing = CubicBezierEasing(0.34f, 1.56f, 0.64f, 1f) // Custom bounce curve[cite: 14]
        ),
        label = "button_bounce"
    )

    // Button height adapts based on device type[cite: 15]
    val buttonHeight = if(isTablet) 48.dp else 52.dp
    Row(
        modifier = modifier
            .scale(scale)
            .shadow(
                elevation = 8.dp,
                shape = CircleShape,
                ambientColor = ShadowColor,
                spotColor = ShadowColor
            )
            .background(
                brush = Brush.linearGradient(listOf(GradientStart, GradientEnd)), //[cite: 21]
                shape = CircleShape // Fully rounded pill shape[cite: 15]
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null, // We remove the default Android ripple to use our custom bounce
                onClick = onClick
            )
            .padding(horizontal = 24.dp)
            .height(buttonHeight),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ){
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = null,
            tint = Color.White
        )
        Spacer(modifier = Modifier.width(8.dp)) // 8px gap between icon and text[cite: 15]
        Text(
            text = text,
            color = Color.White,
            fontFamily = PlusJakartaSans,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold, // Represents Jakarta 700[cite: 15]
            letterSpacing = 0.sp
        )
    }
}

// 3. The Preview Section: Kept cleanly at the bottom
@Preview(showBackground = true, backgroundColor = 0xFFFAF7F2)
@Composable
private fun PrimaryButtonPreview() {
    Row(modifier = Modifier.padding(16.dp)) {
        PrimaryButton(
            text = "Start a room", //[cite: 15]
            onClick = {},
            isTablet = false
        )
    }
}