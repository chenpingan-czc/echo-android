package com.hope.echo.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.SentimentDissatisfied
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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
import com.hope.echo.ui.theme.Gray100
import com.hope.echo.ui.theme.Gray300
import com.hope.echo.ui.theme.Gray600
import com.hope.echo.ui.theme.Green500
import com.hope.echo.ui.theme.Orange100
import com.hope.echo.ui.theme.Orange300
import com.hope.echo.ui.theme.Orange50
import com.hope.echo.ui.theme.Orange500
import com.hope.echo.ui.theme.Orange600
import com.hope.echo.ui.theme.Orange900
import com.hope.echo.ui.theme.Pink500
import com.hope.echo.ui.theme.Red100
import com.hope.echo.ui.theme.Red500
import com.hope.echo.ui.viewmodel.MyBooksViewModel

private val MyBooksBackground = Color(0xFFFDF8F5)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyBooksScreen(
  onBack: () -> Unit,
  onBookClick: (Long) -> Unit = {},
) {
  val viewModel: MyBooksViewModel = viewModel()
  val state by viewModel.uiState.collectAsState()
  val pullState = rememberPullToRefreshState()

  Box(modifier = Modifier.fillMaxSize()) {
    Column(
      modifier =
        Modifier
          .fillMaxSize()
          .background(MyBooksBackground)
          .statusBarsPadding(),
    ) {
      MyBooksHeader(
        onBack = onBack,
        isEditing = state.isEditing,
        onToggleEdit = viewModel::toggleEditMode,
        showEditButton = state.books.isNotEmpty(),
      )

      PullToRefreshBox(
        isRefreshing = state.isRefreshing,
        onRefresh = viewModel::pullRefresh,
        state = pullState,
        modifier = Modifier.fillMaxSize(),
      ) {
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
                Button(onClick = viewModel::load) { Text(stringResource(R.string.common_retry)) }
              }
            }
          state.books.isEmpty() -> EmptyState()
          else -> BookGrid(
            books = state.books,
            isEditing = state.isEditing,
            selectedBookIds = state.selectedBookIds,
            onBookClick = onBookClick,
            onToggleSelection = viewModel::toggleBookSelection,
          )
        }
      }
    }

    FloatingDeleteButton(
      visible = state.isEditing,
      selectedCount = state.selectedBookIds.size,
      onClick = viewModel::showDeleteConfirm,
    )

    BatchDeleteConfirmDialog(
      visible = state.showDeleteConfirm,
      selectedCount = state.selectedBookIds.size,
      isDeleting = state.isDeleting,
      onDismiss = viewModel::dismissDeleteConfirm,
      onConfirm = viewModel::confirmBatchDelete,
    )
  }
}

@Composable
private fun MyBooksHeader(
  onBack: () -> Unit,
  isEditing: Boolean,
  onToggleEdit: () -> Unit,
  showEditButton: Boolean,
) {
  Box(
    modifier =
      Modifier
        .fillMaxWidth()
        .background(MyBooksBackground.copy(alpha = 0.8f))
        .padding(horizontal = 16.dp, vertical = 8.dp),
  ) {
    Box(
      modifier =
        Modifier
          .align(Alignment.CenterStart)
          .size(40.dp)
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
        modifier = Modifier.size(18.dp),
      )
    }

    Text(
      text = stringResource(R.string.my_books_title),
      fontSize = 18.sp,
      fontWeight = FontWeight.ExtraBold,
      color = Orange900,
      modifier = Modifier.align(Alignment.Center),
    )

    if (showEditButton) {
      Box(
        modifier =
          Modifier
            .align(Alignment.CenterEnd)
            .size(40.dp)
            .shadow(2.dp, CircleShape)
            .background(Color.White, CircleShape)
            .border(2.dp, Orange100, CircleShape)
            .clip(CircleShape)
            .clickable(onClick = onToggleEdit),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = if (isEditing) Icons.Filled.Check else Icons.Filled.Edit,
          contentDescription = if (isEditing) stringResource(R.string.common_done) else stringResource(R.string.common_edit),
          tint = if (isEditing) Green500 else Orange600,
          modifier = Modifier.size(18.dp),
        )
      }
    }
  }
}

@Composable
private fun EmptyState() {
  Column(
    modifier = Modifier.fillMaxSize().padding(top = 96.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Box(
      modifier =
        Modifier
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
      text = stringResource(R.string.my_books_empty_title),
      fontSize = 18.sp,
      fontWeight = FontWeight.Bold,
      color = Orange900.copy(alpha = 0.4f),
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
      text = stringResource(R.string.my_books_empty_subtitle),
      fontSize = 14.sp,
      color = Orange900.copy(alpha = 0.4f),
    )
  }
}

@Composable
private fun BookGrid(
  books: List<BookCardDto>,
  isEditing: Boolean,
  selectedBookIds: Set<Long>,
  onBookClick: (Long) -> Unit,
  onToggleSelection: (Long) -> Unit,
) {
  LazyVerticalGrid(
    columns = GridCells.Fixed(3),
    horizontalArrangement = Arrangement.spacedBy(12.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
    contentPadding = PaddingValues(
      start = 16.dp, end = 16.dp, top = 8.dp, bottom = 120.dp
    ),
    modifier = Modifier.fillMaxSize(),
  ) {
    items(books, key = { it.id }) { book ->
      MyBookCard(
        book = book,
        isEditing = isEditing,
        isSelected = selectedBookIds.contains(book.id),
        onClick = {
          if (isEditing) {
            onToggleSelection(book.id)
          } else if (book.status != 0) {
            onBookClick(book.id)
          }
        },
      )
    }
  }
}

@Composable
private fun MyBookCard(
  book: BookCardDto,
  isEditing: Boolean,
  isSelected: Boolean,
  onClick: () -> Unit,
) {
  val isProcessing = book.status == 0

  Column(
    modifier =
      Modifier
        .fillMaxWidth()
        .shadow(2.dp, RoundedCornerShape(24.dp), ambientColor = Orange900.copy(alpha = 0.05f))
        .background(Color.White, RoundedCornerShape(24.dp))
        .border(2.dp, Orange50, RoundedCornerShape(24.dp))
        .clip(RoundedCornerShape(24.dp))
        .clickable(onClick = onClick),
  ) {
    Box(
      modifier =
        Modifier
          .fillMaxWidth()
          .aspectRatio(4f / 5f)
          .background(Orange100),
    ) {
      if (book.coverUrl.isNotBlank()) {
        AsyncImage(
          model = ImageRequest.Builder(LocalContext.current).data(book.coverUrl).crossfade(true).build(),
          contentDescription = book.title,
          contentScale = ContentScale.Crop,
          modifier = Modifier.fillMaxSize(),
        )
      }

      if (isProcessing) {
        Box(
          modifier =
            Modifier
              .fillMaxSize()
              .background(Color.Black.copy(alpha = 0.4f)),
          contentAlignment = Alignment.Center,
        ) {
          Text(
            text = stringResource(R.string.my_books_processing_progress, book.progress),
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
          )
        }
      }

      if (isEditing) {
        Box(
          modifier =
            Modifier
              .fillMaxSize()
              .background(Color.Black.copy(alpha = 0.1f)),
        )
        Box(
          modifier =
            Modifier
              .align(Alignment.TopEnd)
              .padding(8.dp)
              .size(24.dp)
              .then(
                if (isSelected)
                  Modifier.background(Pink500, CircleShape)
                else
                  Modifier
                    .background(Color.White.copy(alpha = 0.5f), CircleShape)
                    .border(2.dp, Color.White.copy(alpha = 0.8f), CircleShape)
              ),
          contentAlignment = Alignment.Center,
        ) {
          if (isSelected) {
            Icon(
              imageVector = Icons.Filled.Check,
              contentDescription = stringResource(R.string.my_books_selected),
              tint = Color.White,
              modifier = Modifier.size(14.dp),
            )
          }
        }
      }
    }

    Box(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 8.dp, vertical = 10.dp),
      contentAlignment = Alignment.Center,
    ) {
      Text(
        text = book.title,
        fontSize = 13.sp,
        fontWeight = FontWeight.ExtraBold,
        color = Orange900,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
    }
  }
}

@Composable
private fun FloatingDeleteButton(
  visible: Boolean,
  selectedCount: Int,
  onClick: () -> Unit,
) {
  Box(
    modifier = Modifier.fillMaxSize(),
    contentAlignment = Alignment.BottomCenter,
  ) {
    AnimatedVisibility(
      visible = visible,
      enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
      exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
    ) {
      Box(
        modifier = Modifier
          .padding(bottom = 64.dp)
          .shadow(12.dp, RoundedCornerShape(50), ambientColor = Red500.copy(alpha = 0.3f))
          .background(
            if (selectedCount > 0) Red500 else Gray300,
            RoundedCornerShape(50),
          )
          .clip(RoundedCornerShape(50))
          .then(
            if (selectedCount > 0) Modifier.clickable(onClick = onClick)
            else Modifier
          )
          .padding(horizontal = 32.dp, vertical = 16.dp),
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Icon(
            imageVector = Icons.Filled.Delete,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(24.dp),
          )
          Text(
            text = if (selectedCount > 0) stringResource(R.string.my_books_delete_with_count, selectedCount) else stringResource(R.string.my_books_delete),
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
          )
        }
      }
    }
  }
}

@Composable
private fun BatchDeleteConfirmDialog(
  visible: Boolean,
  selectedCount: Int,
  isDeleting: Boolean,
  onDismiss: () -> Unit,
  onConfirm: () -> Unit,
) {
  AnimatedVisibility(
    visible = visible,
    enter = fadeIn(),
    exit = fadeOut(),
  ) {
    Box(
      modifier =
        Modifier
          .fillMaxSize()
          .background(Color.Black.copy(alpha = 0.4f))
          .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = { if (!isDeleting) onDismiss() },
          ),
      contentAlignment = Alignment.Center,
    ) {
      AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + scaleIn(initialScale = 0.9f),
        exit = fadeOut() + scaleOut(targetScale = 0.9f),
      ) {
        Column(
          modifier =
            Modifier
              .padding(horizontal = 32.dp)
              .fillMaxWidth()
              .shadow(24.dp, RoundedCornerShape(32.dp))
              .background(Color.White, RoundedCornerShape(32.dp))
              .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {},
              )
              .padding(32.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          Box(
            modifier =
              Modifier
                .size(80.dp)
                .background(Red100, CircleShape),
            contentAlignment = Alignment.Center,
          ) {
            Icon(
              imageVector = Icons.Filled.Delete,
              contentDescription = null,
              tint = Red500,
              modifier = Modifier.size(36.dp),
            )
          }

          Spacer(modifier = Modifier.height(24.dp))

          Text(
            text = stringResource(R.string.my_books_delete_confirm, selectedCount),
            fontSize = 22.sp,
            fontWeight = FontWeight.Black,
            color = Orange900,
            textAlign = TextAlign.Center,
          )

          Spacer(modifier = Modifier.height(32.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
          ) {
            Box(
              modifier =
                Modifier
                  .weight(1f)
                  .background(Gray100, RoundedCornerShape(16.dp))
                  .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { if (!isDeleting) onDismiss() },
                  )
                  .padding(vertical = 16.dp),
              contentAlignment = Alignment.Center,
            ) {
              Text(
                text = stringResource(R.string.my_books_delete_cancel),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Gray600,
              )
            }

            Box(
              modifier =
                Modifier
                  .weight(1f)
                  .shadow(8.dp, RoundedCornerShape(16.dp), ambientColor = Red500.copy(alpha = 0.3f))
                  .background(Red500, RoundedCornerShape(16.dp))
                  .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { if (!isDeleting) onConfirm() },
                  )
                  .padding(vertical = 16.dp),
              contentAlignment = Alignment.Center,
            ) {
              if (isDeleting) {
                CircularProgressIndicator(
                  color = Color.White,
                  strokeWidth = 2.dp,
                  modifier = Modifier.size(20.dp),
                )
              } else {
                Text(
                  text = stringResource(R.string.my_books_delete_ok),
                  fontSize = 16.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color.White,
                )
              }
            }
          }
        }
      }
    }
  }
}
