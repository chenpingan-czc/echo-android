package com.hope.echo.ui.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SentimentDissatisfied
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.hope.echo.R
import com.hope.echo.data.api.BookPageDto
import com.hope.echo.ui.theme.Gray100
import com.hope.echo.ui.theme.Orange100
import com.hope.echo.ui.theme.Orange400
import com.hope.echo.ui.theme.Orange500
import com.hope.echo.ui.theme.Orange600
import com.hope.echo.ui.theme.Orange900
import com.hope.echo.ui.theme.Pink400
import com.hope.echo.ui.viewmodel.PlayerUiState
import com.hope.echo.ui.viewmodel.PlayerViewModel

/** 设计稿 #FFF5EE，与童话屋主题一致 */
private val PlayerBackground = Color(0xFFFFF5EE)

private val PlayButtonGradient =
  Brush.linearGradient(colors = listOf(Orange400, Pink400))

private val ProgressFillBrush =
  Brush.horizontalGradient(colors = listOf(Orange400, Pink400))

@Composable
fun PlayerScreen(bookId: Long, onBack: () -> Unit) {
  val viewModel: PlayerViewModel = viewModel()
  val state by viewModel.uiState.collectAsState()

  LaunchedEffect(bookId) {
    viewModel.loadBook(bookId)
  }

  Box(modifier = Modifier.fillMaxSize().background(PlayerBackground)) {
    when {
      state.isLoading ->
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
          CircularProgressIndicator(color = Orange500)
        }
      state.errorMessage != null ->
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
              text = state.errorMessage ?: "",
              color = Orange900.copy(alpha = 0.6f),
              fontSize = 14.sp,
              modifier = Modifier.padding(horizontal = 32.dp),
              textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { viewModel.loadBook(bookId) }) { Text(stringResource(R.string.player_retry)) }
          }
        }
      state.book == null -> Unit
      state.book!!.pages.isEmpty() -> PlayerEmptyState(onBack = onBack)
      else ->
        PlayerContent(
          state = state,
          onBack = onBack,
          onTogglePlay = viewModel::togglePlayPause,
          onPrev = viewModel::goPrev,
          onNext = viewModel::goNext,
        )
    }
  }
}

@Composable
private fun PlayerEmptyState(onBack: () -> Unit) {
  Column(
    modifier =
      Modifier.fillMaxSize()
        .statusBarsPadding()
        .padding(horizontal = 24.dp),
  ) {
    PlayerTopBar(
      pageLabel = null,
      onBack = onBack,
    )
    Column(
      modifier = Modifier.fillMaxSize().padding(top = 48.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Icon(
        imageVector = Icons.Filled.SentimentDissatisfied,
        contentDescription = null,
        tint = Orange500.copy(alpha = 0.45f),
        modifier = Modifier.size(64.dp),
      )
      Spacer(modifier = Modifier.height(16.dp))
      Text(
        text = stringResource(R.string.player_empty),
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = Orange900.copy(alpha = 0.5f),
        textAlign = TextAlign.Center,
      )
    }
  }
}

@Composable
private fun PlayerContent(
  state: PlayerUiState,
  onBack: () -> Unit,
  onTogglePlay: () -> Unit,
  onPrev: () -> Unit,
  onNext: () -> Unit,
) {
  val book = state.book ?: return
  val pages = book.pages

  Column(
    modifier = Modifier.fillMaxSize().statusBarsPadding(),
  ) {
    PlayerTopBar(
      pageLabel = "${state.pageIndex + 1} / ${pages.size}",
      onBack = onBack,
    )

    Box(
      modifier =
        Modifier
          .weight(1f)
          .fillMaxWidth()
          .padding(horizontal = 24.dp)
          .padding(bottom = 24.dp),
    ) {
      AnimatedContent(
        targetState = state.pageIndex,
        transitionSpec = {
          (fadeIn(tween(220)) + scaleIn(initialScale = 0.95f, animationSpec = tween(220))) togetherWith
            (fadeOut(tween(180)) + scaleOut(targetScale = 1.05f, animationSpec = tween(180)))
        },
        modifier = Modifier.fillMaxSize(),
        label = "playerPage",
      ) { index ->
        val p = pages[index]
        StoryPageFrame(page = p)
      }
    }

    Column(
      modifier =
        Modifier
          .fillMaxWidth()
          .padding(start = 32.dp, end = 32.dp, top = 24.dp, bottom = 24.dp),
    ) {
      StoryProgressBar(progress = state.progress)
      Spacer(modifier = Modifier.height(24.dp))
      Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        PlayerIconButton(
          enabled = state.canGoPrev,
          onClick = onPrev,
          content = {
            Icon(
              imageVector = Icons.Filled.FastRewind,
              contentDescription = stringResource(R.string.player_prev),
              tint = Orange600,
              modifier = Modifier.size(32.dp),
            )
          },
        )
        Box(
          modifier =
            Modifier
              .size(80.dp)
              .shadow(12.dp, CircleShape, spotColor = Pink400.copy(alpha = 0.45f))
              .clip(CircleShape)
              .background(PlayButtonGradient)
              .border(4.dp, Color.White, CircleShape)
              .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onTogglePlay,
              ),
          contentAlignment = Alignment.Center,
        ) {
          if (state.isPlaying) {
            Icon(
              imageVector = Icons.Filled.Pause,
              contentDescription = stringResource(R.string.player_pause),
              tint = Color.White,
              modifier = Modifier.size(36.dp),
            )
          } else {
            Icon(
              imageVector = Icons.Filled.PlayArrow,
              contentDescription = stringResource(R.string.player_play),
              tint = Color.White,
              modifier = Modifier.size(40.dp),
            )
          }
        }
        PlayerIconButton(
          enabled = state.canGoNext,
          onClick = onNext,
          content = {
            Icon(
              imageVector = Icons.Filled.FastForward,
              contentDescription = stringResource(R.string.player_next),
              tint = Orange600,
              modifier = Modifier.size(32.dp),
            )
          },
        )
      }
      Spacer(modifier = Modifier.height(32.dp))
    }
  }
}

@Composable
private fun PlayerTopBar(pageLabel: String?, onBack: () -> Unit) {
  Row(
    modifier =
      Modifier
        .fillMaxWidth()
        .padding(horizontal = 24.dp, vertical = 12.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Box(
      modifier =
        Modifier
          .size(48.dp)
          .shadow(2.dp, CircleShape)
          .background(Color.White, CircleShape)
          .border(2.dp, Orange100, CircleShape)
          .clip(CircleShape)
          .clickable(onClick = onBack),
      contentAlignment = Alignment.Center,
    ) {
      Icon(
        imageVector = Icons.Filled.ArrowBackIosNew,
        contentDescription = stringResource(R.string.common_back),
        tint = Orange900,
        modifier = Modifier.size(20.dp),
      )
    }
    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
      if (pageLabel != null) {
        Text(
          text = pageLabel,
          fontSize = 14.sp,
          fontWeight = FontWeight.Bold,
          color = Orange500,
          modifier =
            Modifier
              .shadow(1.dp, RoundedCornerShape(999.dp))
              .background(Color.White.copy(alpha = 0.88f), RoundedCornerShape(999.dp))
              .border(1.dp, Orange100, RoundedCornerShape(999.dp))
              .padding(horizontal = 16.dp, vertical = 6.dp),
        )
      }
    }
    Spacer(modifier = Modifier.width(48.dp))
  }
}

@Composable
private fun StoryPageFrame(page: BookPageDto) {
  val context = LocalContext.current
  Column(
    modifier =
      Modifier
        .fillMaxSize()
        .shadow(16.dp, RoundedCornerShape(32.dp), spotColor = Orange900.copy(alpha = 0.08f))
        .clip(RoundedCornerShape(32.dp))
        .border(6.dp, Color.White, RoundedCornerShape(32.dp))
        .background(Color.White),
  ) {
    Box(
      modifier =
        Modifier
          .fillMaxSize()
          .padding(6.dp)
          .clip(RoundedCornerShape(20.dp))
          .background(Gray100),
    ) {
      if (page.imageUrl.isNotBlank()) {
        AsyncImage(
          model = ImageRequest.Builder(context).data(page.imageUrl).crossfade(true).build(),
          contentDescription = page.textContent,
          contentScale = ContentScale.Crop,
          modifier = Modifier.fillMaxSize(),
        )
      }
    }
  }
}

@Composable
private fun StoryProgressBar(progress: Float, modifier: Modifier = Modifier) {
  val target = progress.coerceIn(0f, 100f)
  val animatedProgress by animateFloatAsState(
    targetValue = target,
    animationSpec = tween(durationMillis = 48, easing = LinearEasing),
    label = "storyProgress",
  )
  val clamped = animatedProgress.coerceIn(0f, 100f)
  val density = LocalDensity.current
  BoxWithConstraints(modifier = modifier.height(32.dp).fillMaxWidth()) {
    Box(
      modifier =
        Modifier
          .align(Alignment.Center)
          .fillMaxWidth()
          .height(16.dp)
          .clip(RoundedCornerShape(999.dp))
          .border(2.dp, Color.White, RoundedCornerShape(999.dp))
          .background(Orange100),
    ) {
      Box(
        Modifier
          .fillMaxHeight()
          .fillMaxWidth(clamped / 100f)
          .background(ProgressFillBrush),
      )
    }
    val thumb = 28.dp
    val thumbPx = with(density) { thumb.toPx() }
    val trackWPx = with(density) { maxWidth.toPx() }
    val cxPx = trackWPx * clamped / 100f
    val thumbOffsetX = with(density) { (cxPx - thumbPx / 2f).toDp() }
    Box(
      modifier =
        Modifier
          .align(Alignment.CenterStart)
          .offset(x = thumbOffsetX, y = 0.dp)
          .size(thumb)
          .clip(CircleShape)
          .background(Color.White)
          .border(4.dp, Pink400, CircleShape),
    )
  }
}

@Composable
private fun PlayerIconButton(
  enabled: Boolean,
  onClick: () -> Unit,
  content: @Composable () -> Unit,
) {
  Box(
    modifier =
      Modifier
        .size(56.dp)
        .clip(CircleShape)
        .alpha(if (enabled) 1f else 0.3f)
        .clickable(enabled = enabled, onClick = onClick),
    contentAlignment = Alignment.Center,
  ) {
    content()
  }
}
