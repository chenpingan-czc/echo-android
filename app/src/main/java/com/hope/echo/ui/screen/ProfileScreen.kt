package com.hope.echo.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.SentimentDissatisfied
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.hope.echo.R
import com.hope.echo.ui.theme.Blue500
import com.hope.echo.ui.theme.Gray100
import com.hope.echo.ui.theme.Gray300
import com.hope.echo.ui.theme.Gray50
import com.hope.echo.ui.theme.Gray600
import com.hope.echo.ui.theme.Orange100
import com.hope.echo.ui.theme.Orange500
import com.hope.echo.ui.theme.Orange50
import com.hope.echo.ui.theme.Orange900
import com.hope.echo.ui.theme.Purple500
import com.hope.echo.ui.theme.Red100
import com.hope.echo.ui.theme.Red50
import com.hope.echo.ui.theme.Red500
import com.hope.echo.ui.theme.Pink400
import com.hope.echo.ui.viewmodel.ProfileViewModel

private val ProfileBackground = Color(0xFFFDF8F5)

@Composable
fun ProfileScreen(
  onNavigateToMyBooks: () -> Unit = {},
  onNavigateToMyVoice: () -> Unit = {},
  onNavigateToEditProfile: () -> Unit = {},
  onNavigateToAbout: () -> Unit = {},
  onLogout: () -> Unit = {},
  refreshTrigger: Int = 0,
  profileViewModel: ProfileViewModel = viewModel(),
) {
  val profileState by profileViewModel.uiState.collectAsState()
  var showLogoutConfirm by remember { mutableStateOf(false) }

  LaunchedEffect(refreshTrigger) {
    if (refreshTrigger > 0) profileViewModel.loadProfile()
  }

  Box(modifier = Modifier.fillMaxSize()) {
    Column(
      modifier =
        Modifier
          .fillMaxSize()
          .background(ProfileBackground)
          .statusBarsPadding()
          .verticalScroll(rememberScrollState())
          .padding(horizontal = 24.dp)
          .padding(top = 48.dp, bottom = 24.dp),
    ) {
      ProfileHeader(
        userName = profileState.userName,
        avatarUrl = profileState.avatarUrl,
        bookCount = profileState.listenedCount,
        onEditClick = onNavigateToEditProfile,
      )

      Spacer(modifier = Modifier.height(40.dp))

      Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        ProfileMenuItem(
          icon = Icons.Filled.Book,
          iconTint = Blue500,
          title = stringResource(R.string.profile_my_books),
          onClick = onNavigateToMyBooks,
        )
        ProfileMenuItem(
          icon = Icons.Filled.Mic,
          iconTint = Pink400,
          title = stringResource(R.string.profile_my_voice),
          onClick = onNavigateToMyVoice,
        )
        ProfileMenuItem(
          icon = Icons.Filled.Info,
          iconTint = Purple500,
          title = stringResource(R.string.profile_about),
          onClick = onNavigateToAbout,
        )
      }

      Spacer(modifier = Modifier.height(32.dp))
      Spacer(modifier = Modifier.weight(1f))

      LogoutButton(onClick = { showLogoutConfirm = true })
    }

    LogoutConfirmDialog(
      visible = showLogoutConfirm,
      onDismiss = { showLogoutConfirm = false },
      onConfirm = onLogout,
    )
  }
}

@Composable
private fun ProfileHeader(userName: String, avatarUrl: String, bookCount: Int, onEditClick: () -> Unit = {}) {
  Column(
    modifier = Modifier.fillMaxWidth(),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Box {
      Box(
        modifier =
          Modifier
            .size(96.dp)
            .shadow(elevation = 12.dp, shape = CircleShape, ambientColor = Orange900.copy(alpha = 0.1f))
            .clip(CircleShape)
            .border(4.dp, Color.White, CircleShape)
            .background(Orange100),
        contentAlignment = Alignment.Center,
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
            text = userName.take(1).ifBlank { stringResource(R.string.profile_user_default_initial) },
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            color = Orange500,
          )
        }
      }

      Box(
        modifier =
          Modifier
            .align(Alignment.BottomEnd)
            .offset(x = 2.dp, y = (-4).dp)
            .size(28.dp)
            .shadow(2.dp, CircleShape)
            .background(Color.White, CircleShape)
            .border(1.dp, Orange100, CircleShape)
            .clickable(
              interactionSource = remember { MutableInteractionSource() },
              indication = null,
              onClick = onEditClick,
            ),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = Icons.Filled.Edit,
          contentDescription = stringResource(R.string.profile_edit),
          tint = Orange500,
          modifier = Modifier.size(14.dp),
        )
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    Text(
      text = userName,
      fontSize = 24.sp,
      fontWeight = FontWeight.ExtraBold,
      color = Orange900,
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
      text = stringResource(R.string.profile_books_listened, bookCount),
      fontSize = 14.sp,
      fontWeight = FontWeight.Medium,
      color = Orange500,
      modifier =
        Modifier
          .background(Orange100, RoundedCornerShape(50))
          .padding(horizontal = 12.dp, vertical = 4.dp),
    )
  }
}

@Composable
private fun ProfileMenuItem(
  icon: ImageVector,
  iconTint: Color,
  title: String,
  badge: String? = null,
  onClick: () -> Unit = {},
) {
  Row(
    modifier =
      Modifier
        .fillMaxWidth()
        .shadow(2.dp, RoundedCornerShape(24.dp), ambientColor = Orange900.copy(alpha = 0.05f))
        .background(Color.White, RoundedCornerShape(24.dp))
        .clickable(
          interactionSource = remember { MutableInteractionSource() },
          indication = null,
          onClick = onClick,
        )
        .padding(16.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Box(
        modifier =
          Modifier
            .size(40.dp)
            .background(Gray50, CircleShape),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = icon,
          contentDescription = title,
          tint = iconTint,
          modifier = Modifier.size(22.dp),
        )
      }
      Spacer(modifier = Modifier.width(16.dp))
      Text(
        text = title,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = Orange900,
      )
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
      if (badge != null) {
        Text(
          text = badge,
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
          color = Orange500,
          modifier =
            Modifier
              .background(Orange50, RoundedCornerShape(50))
              .padding(horizontal = 8.dp, vertical = 4.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
      }
      Icon(
        imageVector = Icons.Filled.ChevronRight,
        contentDescription = null,
        tint = Gray300,
        modifier = Modifier.size(20.dp),
      )
    }
  }
}

@Composable
private fun LogoutButton(onClick: () -> Unit) {
  Row(
    modifier =
      Modifier
        .fillMaxWidth()
        .padding(bottom = 16.dp)
        .shadow(2.dp, RoundedCornerShape(24.dp), ambientColor = Red500.copy(alpha = 0.05f))
        .background(Red50, RoundedCornerShape(24.dp))
        .border(2.dp, Red100, RoundedCornerShape(24.dp))
        .clickable(
          interactionSource = remember { MutableInteractionSource() },
          indication = null,
          onClick = onClick,
        )
        .padding(horizontal = 16.dp, vertical = 16.dp),
    horizontalArrangement = Arrangement.Center,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Box(
      modifier = Modifier.size(40.dp),
      contentAlignment = Alignment.Center,
    ) {
      Icon(
        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
        contentDescription = stringResource(R.string.profile_logout),
        tint = Red500,
        modifier = Modifier.size(22.dp),
      )
    }
    Spacer(modifier = Modifier.width(12.dp))
    Text(
      text = stringResource(R.string.profile_logout),
      fontSize = 16.sp,
      fontWeight = FontWeight.Bold,
      color = Red500,
    )
  }
}

@Composable
private fun LogoutConfirmDialog(
  visible: Boolean,
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
            onClick = onDismiss,
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
              imageVector = Icons.Filled.SentimentDissatisfied,
              contentDescription = null,
              tint = Red500,
              modifier = Modifier.size(48.dp),
            )
          }

          Spacer(modifier = Modifier.height(24.dp))

          Text(
            text = stringResource(R.string.profile_logout_confirm),
            fontSize = 22.sp,
            fontWeight = FontWeight.Black,
            color = Orange900,
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
                    onClick = onDismiss,
                  )
                  .padding(vertical = 16.dp),
              contentAlignment = Alignment.Center,
            ) {
              Text(
                text = stringResource(R.string.profile_logout_cancel),
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
                    onClick = onConfirm,
                  )
                  .padding(vertical = 16.dp),
              contentAlignment = Alignment.Center,
            ) {
              Text(
                text = stringResource(R.string.profile_logout_ok),
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
