package com.hope.echo.ui.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.SentimentDissatisfied
import androidx.compose.material.icons.filled.MusicNote
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hope.echo.R
import com.hope.echo.data.api.VoiceDto
import com.hope.echo.ui.theme.Orange100
import com.hope.echo.ui.theme.Orange300
import com.hope.echo.ui.theme.Orange400
import com.hope.echo.ui.theme.Orange500
import com.hope.echo.ui.theme.Orange50
import com.hope.echo.ui.theme.Orange900
import com.hope.echo.ui.theme.Pink400
import com.hope.echo.ui.theme.Red500
import com.hope.echo.ui.viewmodel.MyVoiceViewModel

private val VoiceBackground = Color(0xFFFDF8F5)
/** Figma tailwind `pink-50`，封面渐变终点 */
private val VoiceCoverGradientEnd = Color(0xFFFDF2F8)

@Composable
fun MyVoiceScreen(
  onBack: () -> Unit = {},
  onNavigateToRecord: () -> Unit = {},
  refreshTrigger: Int = 0,
  viewModel: MyVoiceViewModel = viewModel(),
) {
  val state by viewModel.uiState.collectAsState()
  val context = LocalContext.current

  LaunchedEffect(refreshTrigger) {
    if (refreshTrigger > 0) viewModel.loadVoices()
  }

  Box(modifier = Modifier.fillMaxSize().background(VoiceBackground)) {
    Column(modifier = Modifier.fillMaxSize()) {
      VoiceHeader(
        onBack = onBack,
      )

      if (state.isLoading) {
        Box(
          modifier = Modifier.fillMaxSize(),
          contentAlignment = Alignment.Center,
        ) {
          CircularProgressIndicator(color = Orange500, modifier = Modifier.size(36.dp))
        }
      } else if (state.voices.isEmpty()) {
        VoiceEmptyState()
      } else {
        LazyVerticalGrid(
          columns = GridCells.Fixed(3),
          modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .padding(bottom = 120.dp),
          horizontalArrangement = Arrangement.spacedBy(16.dp),
          verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
          items(state.voices, key = { it.id }) { voice ->
            val showDeleteAction = state.pendingDeleteVoiceId == voice.id
            VoiceToneCard(
              voice = voice,
              showDeleteAction = showDeleteAction,
              isDeleting = state.deletingVoiceId == voice.id,
              isPlaying = state.playingVoiceId == voice.id,
              onClick = {
                if (showDeleteAction) {
                  viewModel.dismissDeleteAction()
                } else {
                  viewModel.playVoice(voice, context)
                }
              },
              onLongClick = { viewModel.showDeleteAction(voice.id) },
              onDeleteClick = { viewModel.deleteVoice(voice.id) },
            )
          }
        }
      }
    }

    Box(
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .fillMaxWidth()
        .background(
          Brush.verticalGradient(
            colors = listOf(Color.Transparent, VoiceBackground, VoiceBackground),
          )
        )
        .navigationBarsPadding()
        .padding(horizontal = 24.dp)
        .padding(top = 24.dp, bottom = 16.dp),
    ) {
      RecordNewVoiceButton(onClick = onNavigateToRecord)
    }
  }
}

@Composable
private fun VoiceHeader(
  onBack: () -> Unit,
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .statusBarsPadding()
      .padding(horizontal = 24.dp, vertical = 16.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Box(
      modifier = Modifier
        .size(40.dp)
        .shadow(2.dp, CircleShape)
        .background(Color.White, CircleShape)
        .border(2.dp, Orange100, CircleShape)
        .clickable(
          interactionSource = remember { MutableInteractionSource() },
          indication = null,
          onClick = onBack,
        ),
      contentAlignment = Alignment.Center,
    ) {
      Icon(
        imageVector = Icons.Filled.ChevronLeft,
        contentDescription = stringResource(R.string.common_back),
        tint = Orange900,
        modifier = Modifier.size(24.dp),
      )
    }
    Box(
      modifier = Modifier.weight(1f),
      contentAlignment = Alignment.Center,
    ) {
      Text(
        text = stringResource(R.string.my_voice_title),
        fontSize = 18.sp,
        fontWeight = FontWeight.ExtraBold,
        color = Orange900,
      )
    }
    Spacer(modifier = Modifier.size(40.dp))
  }
}

@Composable
private fun VoiceEmptyState() {
  Column(
    modifier = Modifier.fillMaxSize().padding(bottom = 100.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
  ) {
    Box(
      modifier = Modifier
        .size(96.dp)
        .background(Orange100, CircleShape),
      contentAlignment = Alignment.Center,
    ) {
      Icon(
        imageVector = Icons.Filled.SentimentDissatisfied,
        contentDescription = null,
        tint = Orange300,
        modifier = Modifier.size(48.dp),
      )
    }
    Spacer(modifier = Modifier.height(16.dp))
    Text(
      text = stringResource(R.string.my_voice_empty_title),
      fontSize = 18.sp,
      fontWeight = FontWeight.Bold,
      color = Orange900.copy(alpha = 0.4f),
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
      text = stringResource(R.string.my_voice_empty_subtitle),
      fontSize = 14.sp,
      fontWeight = FontWeight.Medium,
      color = Orange900.copy(alpha = 0.4f),
    )
  }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun VoiceToneCard(
  voice: VoiceDto,
  showDeleteAction: Boolean,
  isDeleting: Boolean,
  isPlaying: Boolean,
  onClick: () -> Unit,
  onLongClick: () -> Unit,
  onDeleteClick: () -> Unit,
) {
  val cardShape = RoundedCornerShape(24.dp)
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .shadow(4.dp, cardShape, ambientColor = Orange900.copy(alpha = 0.05f))
      .background(Color.White, cardShape)
      .border(2.dp, Orange50, cardShape)
      .clip(cardShape)
      .combinedClickable(
        onClick = onClick,
        onLongClick = onLongClick,
      ),
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .aspectRatio(1f)
        .drawBehind {
          drawRect(
            brush =
              Brush.linearGradient(
                colors = listOf(Orange100, VoiceCoverGradientEnd),
                start = Offset.Zero,
                end = Offset(size.width, size.height),
              ),
          )
        },
    ) {
      // 设计稿：w-16 h-16、rounded-full、bg-white/60、shadow-sm（先 shadow 再 background，形状一致）
      Box(
        modifier = Modifier
          .align(Alignment.Center)
          .size(64.dp)
          .shadow(
            elevation = 1.dp,
            shape = CircleShape,
            ambientColor = Color.Black.copy(alpha = 0.06f),
            spotColor = Color.Black.copy(alpha = 0.06f),
          )
          .background(Color.White.copy(alpha = 0.6f), CircleShape),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = Icons.Filled.MusicNote,
          contentDescription = stringResource(R.string.my_voice_play),
          tint = if (isPlaying) Orange500 else Orange400,
          modifier = Modifier.size(32.dp),
        )
      }
      if (showDeleteAction) {
        Box(
          modifier = Modifier
            .matchParentSize()
            .background(Color.Black.copy(alpha = 0.12f)),
        )
      }

      if (showDeleteAction) {
        Box(
          modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(8.dp)
            .size(28.dp)
            .shadow(6.dp, CircleShape, ambientColor = Red500.copy(alpha = 0.3f))
            .background(Red500, CircleShape)
            .clip(CircleShape)
            .clickable(enabled = !isDeleting, onClick = onDeleteClick),
          contentAlignment = Alignment.Center,
        ) {
          if (isDeleting) {
            CircularProgressIndicator(
              color = Color.White,
              strokeWidth = 2.dp,
              modifier = Modifier.size(16.dp),
            )
          } else {
            Icon(
              imageVector = Icons.Filled.Delete,
              contentDescription = stringResource(R.string.my_voice_delete),
              tint = Color.White,
              modifier = Modifier.size(16.dp),
            )
          }
        }
      }
    }
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      contentAlignment = Alignment.Center,
    ) {
      Text(
        text = voice.name,
        fontSize = 14.sp,
        fontWeight = FontWeight.ExtraBold,
        color = Orange900,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        textAlign = TextAlign.Center,
      )
    }
  }
}

@Composable
private fun RecordNewVoiceButton(onClick: () -> Unit) {
  val gradient = Brush.horizontalGradient(colors = listOf(Orange400, Pink400))
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .shadow(8.dp, RoundedCornerShape(50), ambientColor = Pink400.copy(alpha = 0.3f))
      .background(gradient, RoundedCornerShape(50))
      .clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClick = onClick,
      )
      .padding(vertical = 16.dp),
    contentAlignment = Alignment.Center,
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Icon(
        imageVector = Icons.Filled.Add,
        contentDescription = null,
        tint = Color.White,
        modifier = Modifier.size(24.dp),
      )
      Spacer(modifier = Modifier.width(8.dp))
      Text(
        text = stringResource(R.string.my_voice_record_new),
        fontSize = 18.sp,
        fontWeight = FontWeight.Black,
        color = Color.White,
      )
    }
  }
}
