package com.hope.echo.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SentimentDissatisfied
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.hope.echo.R
import com.hope.echo.data.api.BookCardDto
import com.hope.echo.data.api.HomeFeedDto
import com.hope.echo.ui.theme.Orange100
import com.hope.echo.ui.theme.Orange300
import com.hope.echo.ui.theme.Orange500
import com.hope.echo.ui.theme.Orange900
import com.hope.echo.ui.theme.Pink400
import com.hope.echo.ui.theme.Red400
import com.hope.echo.ui.theme.Yellow400
import com.hope.echo.ui.viewmodel.HomeViewModel

/** 与首页接口约定一致：播放历史（继续听）最多展示本数 */
private const val MAX_CONTINUE_LISTENING_BOOKS = 10

/** 与首页接口约定一致：「我的绘本」横滑区最多展示本数 */
private const val MAX_HOME_MY_BOOKS = 10

private val HomeBackground = Color(0xFFFDF8F5)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
  onBookClick: (Long) -> Unit = {},
  onNavigateToMyBooks: () -> Unit = {},
  refreshTrigger: Int = 0,
) {
  val viewModel: HomeViewModel = viewModel()
  val state by viewModel.uiState.collectAsState()
  val pullState = rememberPullToRefreshState()

  LaunchedEffect(refreshTrigger) {
    if (refreshTrigger > 0) {
      viewModel.silentRefresh()
    }
  }

  PullToRefreshBox(
    isRefreshing = state.isRefreshing,
    onRefresh = viewModel::pullRefresh,
    state = pullState,
    modifier = Modifier.fillMaxSize().background(HomeBackground),
  ) {
    when {
      state.isLoading ->
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
          CircularProgressIndicator(color = Orange500)
        }
      state.feed != null ->
        HomeContent(
          feed = state.feed!!,
          onBookClick = onBookClick,
          onNavigateToMyBooks = onNavigateToMyBooks,
        )
      state.errorMessage != null ->
        HomeFeedFallback(message = state.errorMessage ?: "", onRetry = viewModel::load)
      else ->
        HomeFeedFallback(
          message = stringResource(R.string.home_error_fetch_failed),
          onRetry = viewModel::load,
        )
    }
  }
}

@Composable
private fun HomeFeedFallback(message: String, onRetry: () -> Unit) {
  Column(
    modifier =
      Modifier.fillMaxSize()
        .statusBarsPadding()
        .padding(horizontal = 40.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
  ) {
    Box(
      modifier = Modifier.size(112.dp).background(Orange100, CircleShape),
      contentAlignment = Alignment.Center,
    ) {
      Icon(
        imageVector = Icons.Filled.SentimentDissatisfied,
        contentDescription = null,
        tint = Orange300,
        modifier = Modifier.size(56.dp),
      )
    }
    Spacer(modifier = Modifier.height(28.dp))
    Text(
      text = stringResource(R.string.home_load_failed),
      fontSize = 22.sp,
      fontWeight = FontWeight.Black,
      color = Orange900,
      textAlign = TextAlign.Center,
    )
    Spacer(modifier = Modifier.height(36.dp))
    Button(
      onClick = onRetry,
      modifier = Modifier.fillMaxWidth().height(52.dp).padding(horizontal = 24.dp),
      shape = RoundedCornerShape(50.dp),
      colors = ButtonDefaults.buttonColors(containerColor = Orange500),
    ) {
      Text(text = stringResource(R.string.common_retry), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
    }
  }
}

@Composable
private fun HomeContent(
  feed: HomeFeedDto,
  onBookClick: (Long) -> Unit,
  onNavigateToMyBooks: () -> Unit,
) {
  Column(
    modifier =
      Modifier.fillMaxSize()
        .background(HomeBackground)
        .statusBarsPadding()
        .padding(top = 12.dp)
  ) {
    HomeHeader(userName = feed.userName, avatarUrl = feed.userAvatarUrl)
    Spacer(modifier = Modifier.height(24.dp))

    Column(
      modifier =
        Modifier.weight(1f)
          .fillMaxWidth()
          .verticalScroll(rememberScrollState())
          .padding(bottom = 8.dp)
    ) {
      HorizontalScrollSection(
        title = stringResource(R.string.home_section_continue),
        icon = {
          Icon(Icons.Filled.Schedule, contentDescription = null, tint = Pink400, modifier = Modifier.size(24.dp))
        },
        books = feed.continueListening.take(MAX_CONTINUE_LISTENING_BOOKS),
        onBookClick = onBookClick,
      )

      HorizontalScrollSection(
        title = stringResource(R.string.home_section_my_books),
        icon = {
          Icon(Icons.Filled.Favorite, contentDescription = null, tint = Red400, modifier = Modifier.size(24.dp))
        },
        books = feed.myBooks.take(MAX_HOME_MY_BOOKS),
        onBookClick = onBookClick,
        onMoreClick = onNavigateToMyBooks,
      )

      GridSection(
        title = stringResource(R.string.home_section_featured),
        icon = {
          Icon(Icons.Filled.Star, contentDescription = null, tint = Yellow400, modifier = Modifier.size(24.dp))
        },
        books = feed.featured,
        onBookClick = onBookClick,
      )
    }
  }
}

@Composable
private fun HomeHeader(userName: String, avatarUrl: String) {
  Row(
    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = stringResource(R.string.home_welcome),
        fontSize = 26.sp,
        fontWeight = FontWeight.Black,
        color = Orange500,
        letterSpacing = (-0.5).sp,
        lineHeight = 30.sp,
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = stringResource(R.string.home_subtitle),
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        color = Orange900.copy(alpha = 0.5f),
      )
    }
    Box(
      modifier =
        Modifier.size(48.dp)
          .clip(RoundedCornerShape(50))
          .border(4.dp, Color.White, RoundedCornerShape(50))
          .background(Orange500.copy(alpha = 0.2f))
    ) {
      if (avatarUrl.isNotBlank()) {
        AsyncImage(
          model = ImageRequest.Builder(LocalContext.current).data(avatarUrl).crossfade(true).build(),
          contentDescription = stringResource(R.string.common_avatar),
          contentScale = ContentScale.Crop,
          modifier = Modifier.fillMaxSize(),
        )
      } else {
        Text(
          text = userName.take(1).ifBlank { stringResource(R.string.home_user_default_initial) },
          modifier = Modifier.align(Alignment.Center),
          style = MaterialTheme.typography.titleMedium,
          color = Orange500,
          fontWeight = FontWeight.Bold,
        )
      }
    }
  }
}

/** 横向滚动书架，用于"继续听"和"我的绘本" */
@Composable
private fun HorizontalScrollSection(
  title: String,
  icon: @Composable () -> Unit,
  books: List<BookCardDto>,
  onBookClick: (Long) -> Unit,
  onMoreClick: (() -> Unit)? = null,
) {
  if (books.isEmpty()) return

  val listState = rememberLazyListState()

  LaunchedEffect(books.firstOrNull()?.id) {
    listState.animateScrollToItem(0)
  }

  Column(modifier = Modifier.padding(bottom = 28.dp)) {
    SectionHeader(title = title, icon = icon, onMoreClick = onMoreClick)
    LazyRow(
      state = listState,
      horizontalArrangement = Arrangement.spacedBy(16.dp),
      contentPadding = PaddingValues(horizontal = 24.dp),
    ) {
      items(books, key = { it.id }) { book ->
        BookCard(book = book, width = 120.dp, onClick = { onBookClick(book.id) })
      }
    }
  }
}

/** 3 列网格，用于"精选故事" */
@Composable
private fun GridSection(
  title: String,
  icon: @Composable () -> Unit,
  books: List<BookCardDto>,
  onBookClick: (Long) -> Unit,
) {
  if (books.isEmpty()) return

  Column(modifier = Modifier.padding(bottom = 28.dp)) {
    SectionHeader(title = title, icon = icon)
    Column(
      modifier = Modifier.padding(horizontal = 24.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      books.chunked(3).forEach { rowBooks ->
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
          rowBooks.forEach { book ->
            Box(modifier = Modifier.weight(1f)) {
              BookCard(book = book, width = null, onClick = { onBookClick(book.id) })
            }
          }
          // 末行不足3本时，用空 Box 补位保持对齐
          repeat(3 - rowBooks.size) {
            Box(modifier = Modifier.weight(1f))
          }
        }
      }
    }
  }
}

@Composable
private fun SectionHeader(
  title: String,
  icon: @Composable () -> Unit,
  onMoreClick: (() -> Unit)? = null,
) {
  Row(
    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 12.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      icon()
      Text(text = title, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Orange900)
    }
    Spacer(modifier = Modifier.weight(1f))
    if (onMoreClick != null) {
      Text(
        text = stringResource(R.string.home_more),
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold,
        color = Orange500,
        modifier =
          Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onMoreClick,
          ),
      )
    }
  }
}

/**
 * [width] 为 null 时填满父容器宽度（网格模式），不为 null 时固定宽度（横向滚动模式）。
 */
@Composable
private fun BookCard(
  book: BookCardDto,
  width: androidx.compose.ui.unit.Dp?,
  onClick: () -> Unit,
) {
  val isProcessing = book.status == 0
  val baseModifier = if (width != null) Modifier.width(width) else Modifier.fillMaxWidth()
  val modifier =
    if (isProcessing) baseModifier
    else baseModifier.clickable(onClick = onClick)

  Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
    Box(
      modifier =
        Modifier.fillMaxWidth()
          .aspectRatio(4f / 5f)
          .clip(RoundedCornerShape(20.dp))
          .border(3.dp, Color.White, RoundedCornerShape(20.dp))
          .background(Color.White)
    ) {
      if (book.coverUrl.isNotBlank()) {
        AsyncImage(
          model = ImageRequest.Builder(LocalContext.current).data(book.coverUrl).crossfade(true).build(),
          contentDescription = book.title,
          contentScale = ContentScale.Crop,
          modifier = Modifier.fillMaxSize(),
        )
      } else {
        Box(
          modifier = Modifier.fillMaxSize().background(Orange500.copy(alpha = 0.15f)),
          contentAlignment = Alignment.Center,
        ) {
          Text(text = "📖", fontSize = 28.sp)
        }
      }

      if (isProcessing) {
        Box(
          modifier =
            Modifier.fillMaxSize()
              .background(Color.Black.copy(alpha = 0.55f)),
          contentAlignment = Alignment.Center,
        ) {
          Text(
            text = stringResource(R.string.home_processing),
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
          )
        }
      }
    }
    Spacer(modifier = Modifier.height(8.dp))
    Text(
      text = if (isProcessing) stringResource(R.string.home_processing_progress, book.progress) else book.title,
      fontSize = 13.sp,
      fontWeight = FontWeight.Bold,
      color = if (isProcessing) Orange500 else Orange900,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
      textAlign = TextAlign.Center,
      modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
    )
  }
}
