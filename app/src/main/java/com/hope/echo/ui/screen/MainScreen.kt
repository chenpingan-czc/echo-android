package com.hope.echo.ui.screen

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hope.echo.R
import com.hope.echo.ui.theme.Gray400
import com.hope.echo.ui.theme.Orange100
import com.hope.echo.ui.theme.Orange500
import com.hope.echo.ui.theme.Pink400

@Composable
fun MainScreen(
  refreshTrigger: Int = 0,
  profileRefreshTrigger: Int = 0,
  onNavigateToUpload: () -> Unit,
  onNavigateToMyBooks: () -> Unit = {},
  onNavigateToMyVoice: () -> Unit = {},
  onNavigateToEditProfile: () -> Unit = {},
  onNavigateToAbout: () -> Unit = {},
  onNavigateToPlayer: (Long) -> Unit = {},
  onLogout: () -> Unit = {},
) {
  var selectedTab by rememberSaveable { mutableIntStateOf(0) }

  Scaffold(
    contentWindowInsets = WindowInsets(0),
    bottomBar = {
      MainBottomBar(
        selectedTab = selectedTab,
        onTabSelected = { selectedTab = it },
        onUploadClick = onNavigateToUpload,
      )
    },
  ) { paddingValues ->
    Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
      when (selectedTab) {
        0 ->
          HomeScreen(
            onBookClick = onNavigateToPlayer,
            onNavigateToMyBooks = onNavigateToMyBooks,
            refreshTrigger = refreshTrigger,
          )
        1 ->
          ProfileScreen(
            onNavigateToMyBooks = onNavigateToMyBooks,
            onNavigateToMyVoice = onNavigateToMyVoice,
            onNavigateToEditProfile = onNavigateToEditProfile,
            onNavigateToAbout = onNavigateToAbout,
            onLogout = onLogout,
            refreshTrigger = profileRefreshTrigger,
          )
      }
    }
  }
}

@Composable
private fun MainBottomBar(
  selectedTab: Int,
  onTabSelected: (Int) -> Unit,
  onUploadClick: () -> Unit,
) {
  Box(
    modifier = Modifier.fillMaxWidth(),
    contentAlignment = Alignment.TopCenter,
  ) {
    Surface(
      modifier = Modifier.fillMaxWidth(),
      color = Color.White,
      shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp),
      shadowElevation = 12.dp,
      tonalElevation = 0.dp,
    ) {
      Column(modifier = Modifier.fillMaxWidth()) {
        Row(
          modifier = Modifier.fillMaxWidth().height(80.dp).padding(horizontal = 32.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          NavItem(
            icon = Icons.Filled.Home,
            label = stringResource(R.string.main_tab_home),
            selected = selectedTab == 0,
            onClick = { onTabSelected(0) },
            modifier = Modifier.weight(1f),
          )
          Spacer(modifier = Modifier.width(64.dp))
          NavItem(
            icon = Icons.Filled.Person,
            label = stringResource(R.string.main_tab_profile),
            selected = selectedTab == 1,
            onClick = { onTabSelected(1) },
            modifier = Modifier.weight(1f),
          )
        }
        Spacer(modifier = Modifier.fillMaxWidth().windowInsetsBottomHeight(WindowInsets.navigationBars))
      }
    }

    Box(modifier = Modifier.offset(y = (-28).dp)) {
      UploadFab(onClick = onUploadClick)
    }
  }
}

@Composable
private fun NavItem(
  icon: ImageVector,
  label: String,
  selected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val offsetY by animateDpAsState(
    targetValue = if (selected) (-4).dp else 0.dp,
    animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
    label = "navItemOffset",
  )
  val scale by animateFloatAsState(
    targetValue = if (selected) 1.1f else 1f,
    animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
    label = "navItemScale",
  )

  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier =
      modifier
        .clickable(
          interactionSource = remember { MutableInteractionSource() },
          indication = null,
          onClick = onClick,
        )
        .offset(y = offsetY)
        .scale(scale),
  ) {
    Box(
      modifier =
        Modifier.size(40.dp)
          .background(
            color = if (selected) Orange100 else Color.Transparent,
            shape = RoundedCornerShape(12.dp),
          ),
      contentAlignment = Alignment.Center,
    ) {
      Icon(
        imageVector = icon,
        contentDescription = label,
        tint = if (selected) Orange500 else Gray400,
        modifier = Modifier.size(24.dp),
      )
    }
  }
}

@Composable
private fun UploadFab(onClick: () -> Unit) {
  val gradient = Brush.linearGradient(colors = listOf(Orange500, Pink400))

  Box(
    modifier =
      Modifier.size(72.dp)
        .shadow(elevation = 8.dp, shape = CircleShape, ambientColor = Pink400.copy(alpha = 0.5f))
        .background(color = Color.White, shape = CircleShape)
        .clickable(
          interactionSource = remember { MutableInteractionSource() },
          indication = null,
          onClick = onClick,
        ),
    contentAlignment = Alignment.Center,
  ) {
    Box(
      modifier = Modifier.size(60.dp).background(brush = gradient, shape = CircleShape),
      contentAlignment = Alignment.Center,
    ) {
      Icon(
        imageVector = Icons.Filled.Add,
        contentDescription = stringResource(R.string.main_upload),
        tint = Color.White,
        modifier = Modifier.size(32.dp),
      )
    }
  }
}
