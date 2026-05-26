package com.hope.echo.ui.screen

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hope.echo.R
import com.hope.echo.ui.theme.Orange100
import com.hope.echo.ui.theme.Orange200
import com.hope.echo.ui.theme.Orange400
import com.hope.echo.ui.theme.Orange600
import com.hope.echo.ui.theme.Pink200
import com.hope.echo.ui.theme.Yellow200
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onTimeout: () -> Unit) {
  val scaleAnim = remember { Animatable(0.5f) }
  val alphaAnim = remember { Animatable(0f) }
  val titleOffsetAnim = remember { Animatable(20f) }
  val titleAlphaAnim = remember { Animatable(0f) }
  val subtitleOffsetAnim = remember { Animatable(20f) }
  val subtitleAlphaAnim = remember { Animatable(0f) }

  LaunchedEffect(Unit) {
    scaleAnim.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = 300f))
  }

  LaunchedEffect(Unit) {
    alphaAnim.animateTo(1f, spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = 300f))
  }

  LaunchedEffect(Unit) {
    delay(300)
    titleAlphaAnim.animateTo(1f, tween(500))
  }

  LaunchedEffect(Unit) {
    delay(300)
    titleOffsetAnim.animateTo(0f, tween(500, easing = FastOutSlowInEasing))
  }

  LaunchedEffect(Unit) {
    delay(500)
    subtitleAlphaAnim.animateTo(1f, tween(500))
  }

  LaunchedEffect(Unit) {
    delay(500)
    subtitleOffsetAnim.animateTo(0f, tween(500, easing = FastOutSlowInEasing))
  }

  LaunchedEffect(Unit) {
    delay(2500)
    onTimeout()
  }

  val infiniteTransition = rememberInfiniteTransition(label = "blob")
  val blobOffset1 by
    infiniteTransition.animateFloat(
      initialValue = 0f,
      targetValue = -20f,
      animationSpec =
        infiniteRepeatable(tween(4000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
      label = "blob1Y",
    )
  val blobScale1 by
    infiniteTransition.animateFloat(
      initialValue = 1f,
      targetValue = 1.1f,
      animationSpec =
        infiniteRepeatable(tween(4000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
      label = "blob1Scale",
    )
  val blobOffset2 by
    infiniteTransition.animateFloat(
      initialValue = 0f,
      targetValue = 20f,
      animationSpec =
        infiniteRepeatable(
          tween(5000, delayMillis = 1000, easing = FastOutSlowInEasing),
          RepeatMode.Reverse,
        ),
      label = "blob2Y",
    )
  val blobScale2 by
    infiniteTransition.animateFloat(
      initialValue = 1f,
      targetValue = 1.2f,
      animationSpec =
        infiniteRepeatable(
          tween(5000, delayMillis = 1000, easing = FastOutSlowInEasing),
          RepeatMode.Reverse,
        ),
      label = "blob2Scale",
    )

  Box(
    modifier = Modifier.fillMaxSize().background(Orange100.copy(alpha = 0.5f)),
    contentAlignment = Alignment.Center,
  ) {
    Box(
      modifier =
        Modifier.size(160.dp)
          .offset(x = (-40).dp, y = (-80).dp)
          .graphicsLayer {
            translationY = blobOffset1
            scaleX = blobScale1
            scaleY = blobScale1
          }
          .blur(24.dp)
          .clip(CircleShape)
          .background(Pink200.copy(alpha = 0.6f))
          .align(Alignment.TopStart)
    )

    Box(
      modifier =
        Modifier.size(240.dp)
          .offset(x = 40.dp, y = 80.dp)
          .graphicsLayer {
            translationY = blobOffset2
            scaleX = blobScale2
            scaleY = blobScale2
          }
          .blur(24.dp)
          .clip(CircleShape)
          .background(Yellow200.copy(alpha = 0.6f))
          .align(Alignment.BottomEnd)
    )

    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier =
        Modifier.graphicsLayer {
          scaleX = scaleAnim.value
          scaleY = scaleAnim.value
          alpha = alphaAnim.value
        },
    ) {
      Box(
        modifier =
          Modifier.shadow(
              elevation = 16.dp,
              shape = RoundedCornerShape(24.dp),
              ambientColor = Orange200.copy(alpha = 0.5f),
              spotColor = Orange200.copy(alpha = 0.5f),
            )
            .background(Color.White, RoundedCornerShape(24.dp))
            .size(96.dp),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          painter = painterResource(R.drawable.ic_book_open),
          contentDescription = null,
          tint = Orange400,
          modifier = Modifier.size(64.dp),
        )
      }

      Spacer(modifier = Modifier.height(24.dp))

      Text(
        text = stringResource(R.string.splash_title),
        fontSize = 30.sp,
        fontWeight = FontWeight.Bold,
        color = Orange600,
        letterSpacing = 2.sp,
        modifier =
          Modifier.graphicsLayer {
            translationY = titleOffsetAnim.value
            alpha = titleAlphaAnim.value
          },
      )

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = stringResource(R.string.splash_subtitle),
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium,
        color = Orange400.copy(alpha = 0.8f),
        modifier =
          Modifier.graphicsLayer {
            translationY = subtitleOffsetAnim.value
            alpha = subtitleAlphaAnim.value
          },
      )
    }
  }
}
