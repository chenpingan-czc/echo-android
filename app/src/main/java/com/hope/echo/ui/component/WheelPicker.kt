package com.hope.echo.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hope.echo.ui.theme.Orange100
import com.hope.echo.ui.theme.Orange900
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter

private val DefaultItemHeight = 48.dp
private const val DefaultVisibleItems = 5

@Composable
fun <T> WheelPicker(
  items: List<T>,
  selectedItem: T,
  onItemSelected: (T) -> Unit,
  label: @Composable (T) -> String,
  modifier: Modifier = Modifier,
  itemHeight: Dp = DefaultItemHeight,
  visibleItems: Int = DefaultVisibleItems,
) {
  if (items.isEmpty()) return

  val halfVisible = visibleItems / 2
  val itemHeightPx = with(LocalDensity.current) { itemHeight.toPx() }

  val initialIndex = remember {
    items.indexOf(selectedItem).coerceAtLeast(0)
  }
  val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)
  val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)

  val latestItems by rememberUpdatedState(items)
  val latestSelected by rememberUpdatedState(selectedItem)
  val latestCallback by rememberUpdatedState(onItemSelected)

  val centeredIndex by remember {
    derivedStateOf {
      val first = listState.firstVisibleItemIndex
      val offset = listState.firstVisibleItemScrollOffset
      val raw = if (offset > itemHeightPx / 2f) first + 1 else first
      raw.coerceIn(0, (latestItems.size - 1).coerceAtLeast(0))
    }
  }

  LaunchedEffect(items, selectedItem) {
    val target = items.indexOf(selectedItem).coerceAtLeast(0)
    if (!listState.isScrollInProgress &&
      (listState.firstVisibleItemIndex != target ||
        listState.firstVisibleItemScrollOffset != 0)
    ) {
      listState.scrollToItem(target)
    }
  }

  LaunchedEffect(listState) {
    snapshotFlow { listState.isScrollInProgress }
      .drop(1)
      .filter { !it }
      .collect {
        val list = latestItems
        if (list.isEmpty()) return@collect

        val idx = centeredIndex.coerceIn(0, list.size - 1)

        if (listState.firstVisibleItemIndex != idx ||
          listState.firstVisibleItemScrollOffset != 0
        ) {
          listState.animateScrollToItem(idx)
        }

        val item = list[idx]
        if (item != latestSelected) latestCallback(item)
      }
  }

  val edgeFade = Brush.verticalGradient(
    0f to Color.White,
    0.3f to Color.White.copy(alpha = 0.6f),
    0.45f to Color.Transparent,
    0.55f to Color.Transparent,
    0.7f to Color.White.copy(alpha = 0.6f),
    1f to Color.White,
  )

  Box(
    modifier = modifier.height(itemHeight * visibleItems),
    contentAlignment = Alignment.Center,
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(itemHeight)
        .padding(horizontal = 4.dp)
        .background(
          color = Orange100.copy(alpha = 0.5f),
          shape = RoundedCornerShape(12.dp),
        )
    )

    LazyColumn(
      state = listState,
      flingBehavior = flingBehavior,
      modifier = Modifier
        .fillMaxWidth()
        .height(itemHeight * visibleItems)
        .drawWithContent {
          drawContent()
          drawRect(brush = edgeFade)
        },
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      items(halfVisible) { Spacer(modifier = Modifier.height(itemHeight)) }

      itemsIndexed(items) { index, item ->
        val isSelected = index == centeredIndex
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(itemHeight),
          contentAlignment = Alignment.Center,
        ) {
          Text(
            text = label(item),
            fontSize = if (isSelected) 20.sp else 16.sp,
            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
            color = if (isSelected) Orange900 else Orange900.copy(alpha = 0.35f),
          )
        }
      }

      items(halfVisible) { Spacer(modifier = Modifier.height(itemHeight)) }
    }
  }
}
