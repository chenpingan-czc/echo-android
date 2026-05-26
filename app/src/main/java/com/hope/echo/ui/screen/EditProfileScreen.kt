package com.hope.echo.ui.screen

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.hope.echo.R
import com.hope.echo.ui.component.WheelPicker
import com.hope.echo.ui.theme.Gray300
import com.hope.echo.ui.theme.Gray600
import com.hope.echo.ui.theme.Green500
import com.hope.echo.ui.theme.Orange100
import com.hope.echo.ui.theme.Orange200
import com.hope.echo.ui.theme.Orange400
import com.hope.echo.ui.theme.Orange500
import com.hope.echo.ui.theme.Orange900
import com.hope.echo.ui.viewmodel.EditProfileViewModel
import java.util.Calendar

private val ScreenBackground = Color(0xFFFDF8F5)

@Composable
fun EditProfileScreen(
  onBack: () -> Unit,
  viewModel: EditProfileViewModel = viewModel(),
) {
  val uiState by viewModel.uiState.collectAsState()
  var showDatePicker by remember { mutableStateOf(false) }
  val context = LocalContext.current

  val albumLauncher =
    rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
      if (uri != null) viewModel.uploadAvatar(context, uri)
    }

  LaunchedEffect(uiState.saveSuccess) {
    if (uiState.saveSuccess) {
      kotlinx.coroutines.delay(1500)
      onBack()
    }
  }

  Box(modifier = Modifier.fillMaxSize()) {
    if (uiState.isLoading) {
      Box(
        modifier = Modifier.fillMaxSize().background(ScreenBackground),
        contentAlignment = Alignment.Center,
      ) {
        CircularProgressIndicator(color = Orange500)
      }
    } else {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .background(ScreenBackground)
          .statusBarsPadding()
          .verticalScroll(rememberScrollState()),
      ) {
        EditProfileHeader(onBack = onBack)

        AvatarEditSection(
          avatarUrl = uiState.avatar,
          onPickAvatar = { albumLauncher.launch("image/*") },
        )

        FormFields(
          name = uiState.name,
          birthday = uiState.birthday,
          onNameChange = viewModel::onNameChange,
          onShowDatePicker = { showDatePicker = true },
        )

        Spacer(modifier = Modifier.weight(1f))

        SaveButton(
          isSaving = uiState.isSaving,
          onClick = viewModel::save,
        )
      }
    }

    SaveSuccessOverlay(visible = uiState.saveSuccess)

    DateWheelPickerDialog(
      visible = showDatePicker,
      currentDate = uiState.birthday,
      onDismiss = { showDatePicker = false },
      onConfirm = { date ->
        viewModel.onBirthdayChange(date)
        showDatePicker = false
      },
    )
  }
}

@Composable
private fun EditProfileHeader(onBack: () -> Unit) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 24.dp)
      .padding(top = 12.dp),
  ) {
    Box(
      modifier = Modifier
        .align(Alignment.CenterStart)
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
        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
        contentDescription = stringResource(R.string.common_back),
        tint = Orange900,
        modifier = Modifier.size(24.dp),
      )
    }

    Text(
      text = stringResource(R.string.edit_profile_title),
      fontSize = 18.sp,
      fontWeight = FontWeight.ExtraBold,
      color = Orange900,
      modifier = Modifier.align(Alignment.Center),
    )

    Spacer(modifier = Modifier.size(40.dp).align(Alignment.CenterEnd))
  }
}

@Composable
private fun AvatarEditSection(avatarUrl: String, onPickAvatar: () -> Unit) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .padding(top = 24.dp, bottom = 40.dp),
    contentAlignment = Alignment.Center,
  ) {
    Box {
      Box(
        modifier = Modifier
          .size(112.dp)
          .shadow(elevation = 16.dp, shape = CircleShape, ambientColor = Orange900.copy(alpha = 0.1f))
          .clip(CircleShape)
          .border(4.dp, Color.White, CircleShape)
          .background(Orange200)
          .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onPickAvatar,
          ),
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
          Icon(
            imageVector = Icons.Filled.Person,
            contentDescription = stringResource(R.string.common_avatar),
            tint = Orange500,
            modifier = Modifier.size(48.dp),
          )
        }
      }

      Box(
        modifier = Modifier
          .align(Alignment.BottomEnd)
          .size(36.dp)
          .shadow(4.dp, CircleShape)
          .background(Orange500, CircleShape)
          .border(2.dp, Color.White, CircleShape)
          .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onPickAvatar,
          ),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = Icons.Filled.CameraAlt,
          contentDescription = stringResource(R.string.edit_profile_change_avatar),
          tint = Color.White,
          modifier = Modifier.size(18.dp),
        )
      }
    }
  }
}

@Composable
private fun FormFields(
  name: String,
  birthday: String,
  onNameChange: (String) -> Unit,
  onShowDatePicker: () -> Unit,
) {
  Column(
    modifier = Modifier.padding(horizontal = 24.dp),
    verticalArrangement = Arrangement.spacedBy(24.dp),
  ) {
    FormField(
      label = stringResource(R.string.edit_profile_nickname_label),
      icon = { Icon(Icons.Filled.Person, null, tint = Orange400, modifier = Modifier.size(20.dp)) },
    ) {
      BasicTextField(
        value = name,
        onValueChange = onNameChange,
        textStyle = TextStyle(
          fontSize = 16.sp,
          fontWeight = FontWeight.Bold,
          color = Orange900,
        ),
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        decorationBox = { innerTextField ->
          if (name.isEmpty()) {
            Text(
              text = stringResource(R.string.edit_profile_nickname_placeholder),
              fontSize = 16.sp,
              fontWeight = FontWeight.Bold,
              color = Orange900.copy(alpha = 0.3f),
            )
          }
          innerTextField()
        },
      )
    }

    FormField(
      label = stringResource(R.string.edit_profile_birthday_label),
      icon = { Icon(Icons.Filled.CalendarToday, null, tint = Orange400, modifier = Modifier.size(20.dp)) },
      onClick = onShowDatePicker,
    ) {
      Text(
        text = formatDisplayDate(birthday),
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = if (birthday.isNotBlank()) Orange900 else Orange900.copy(alpha = 0.3f),
      )
    }
  }
}

@Composable
private fun formatDisplayDate(date: String): String {
  if (date.isBlank()) return stringResource(R.string.edit_profile_birthday_placeholder)
  val parts = date.split("-")
  return if (parts.size == 3) {
    val year = parts[0]
    val month = parts[1].toIntOrNull()
    val day = parts[2].toIntOrNull()
    if (month != null && day != null) {
      stringResource(R.string.edit_profile_birthday_format, year, month, day)
    } else date
  } else date
}

@Composable
private fun FormField(
  label: String,
  icon: @Composable () -> Unit,
  onClick: (() -> Unit)? = null,
  content: @Composable () -> Unit,
) {
  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    Text(
      text = label,
      fontSize = 14.sp,
      fontWeight = FontWeight.Bold,
      color = Orange900,
      modifier = Modifier.padding(start = 8.dp),
    )

    Box(
      modifier = Modifier
        .fillMaxWidth()
        .shadow(2.dp, RoundedCornerShape(24.dp), ambientColor = Orange900.copy(alpha = 0.05f))
        .background(Color.White, RoundedCornerShape(24.dp))
        .border(2.dp, Orange100, RoundedCornerShape(24.dp))
        .then(
          if (onClick != null) {
            Modifier.clickable(
              interactionSource = remember { MutableInteractionSource() },
              indication = null,
              onClick = onClick,
            )
          } else Modifier
        )
        .padding(horizontal = 16.dp, vertical = 16.dp),
    ) {
      Box(modifier = Modifier.padding(start = 0.dp).align(Alignment.CenterStart)) {
        androidx.compose.foundation.layout.Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          icon()
          content()
        }
      }
    }
  }
}

@Composable
private fun SaveButton(isSaving: Boolean, onClick: () -> Unit) {
  val gradient = Brush.horizontalGradient(listOf(Orange400, Orange500))

  Box(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 24.dp)
      .padding(bottom = 80.dp, top = 24.dp)
      .shadow(12.dp, RoundedCornerShape(24.dp), ambientColor = Orange500.copy(alpha = 0.3f))
      .background(brush = gradient, shape = RoundedCornerShape(24.dp))
      .clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        enabled = !isSaving,
        onClick = onClick,
      )
      .padding(vertical = 16.dp),
    contentAlignment = Alignment.Center,
  ) {
    if (isSaving) {
      CircularProgressIndicator(
        color = Color.White,
        strokeWidth = 2.dp,
        modifier = Modifier.size(24.dp),
      )
    } else {
      Text(
        text = stringResource(R.string.edit_profile_save),
        fontSize = 18.sp,
        fontWeight = FontWeight.Black,
        color = Color.White,
      )
    }
  }
}

@Composable
private fun DateWheelPickerDialog(
  visible: Boolean,
  currentDate: String,
  onDismiss: () -> Unit,
  onConfirm: (String) -> Unit,
) {
  val now = Calendar.getInstance()
  val currentYear = now.get(Calendar.YEAR)
  val years = remember { (2010..currentYear).toList() }
  val months = remember { (1..12).toList() }

  val parsed = remember(currentDate) {
    runCatching {
      val parts = currentDate.split("-")
      Triple(parts[0].toInt(), parts[1].toInt(), parts[2].toInt())
    }.getOrDefault(Triple(currentYear - 3, 1, 1))
  }

  var selectedYear by remember(visible) { mutableIntStateOf(parsed.first) }
  var selectedMonth by remember(visible) { mutableIntStateOf(parsed.second) }
  var selectedDay by remember(visible) { mutableIntStateOf(parsed.third) }

  val daysInMonth by remember {
    derivedStateOf {
      val cal = Calendar.getInstance()
      cal.set(selectedYear, selectedMonth - 1, 1)
      cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    }
  }
  val days by remember { derivedStateOf { (1..daysInMonth).toList() } }

  LaunchedEffect(daysInMonth) {
    if (selectedDay > daysInMonth) selectedDay = daysInMonth
  }

  AnimatedVisibility(
    visible = visible,
    enter = fadeIn(),
    exit = fadeOut(),
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(Color.Black.copy(alpha = 0.4f))
        .clickable(
          interactionSource = remember { MutableInteractionSource() },
          indication = null,
          onClick = onDismiss,
        ),
      contentAlignment = Alignment.BottomCenter,
    ) {
      AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it }),
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .background(
              Color.White,
              RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            )
            .clickable(
              interactionSource = remember { MutableInteractionSource() },
              indication = null,
              onClick = {},
            )
            .padding(bottom = 40.dp),
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(
              text = stringResource(R.string.edit_profile_dialog_cancel),
              fontSize = 16.sp,
              fontWeight = FontWeight.Bold,
              color = Gray600,
              modifier = Modifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss,
              ),
            )
            Text(
              text = stringResource(R.string.edit_profile_dialog_title),
              fontSize = 17.sp,
              fontWeight = FontWeight.ExtraBold,
              color = Orange900,
            )
            Text(
              text = stringResource(R.string.edit_profile_dialog_confirm),
              fontSize = 16.sp,
              fontWeight = FontWeight.Bold,
              color = Orange500,
              modifier = Modifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                  onConfirm(
                    String.format("%04d-%02d-%02d", selectedYear, selectedMonth, selectedDay)
                  )
                },
              ),
            )
          }

          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(1.dp)
              .background(Gray300.copy(alpha = 0.5f))
          )

          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp)
              .padding(top = 8.dp),
            horizontalArrangement = Arrangement.Center,
          ) {
            WheelPicker(
              items = years,
              selectedItem = selectedYear,
              onItemSelected = { selectedYear = it },
              label = { stringResource(R.string.edit_profile_wheel_year, it) },
              modifier = Modifier.weight(1.2f),
            )
            WheelPicker(
              items = months,
              selectedItem = selectedMonth,
              onItemSelected = { selectedMonth = it },
              label = { stringResource(R.string.edit_profile_wheel_month, it) },
              modifier = Modifier.weight(1f),
            )
            WheelPicker(
              items = days,
              selectedItem = selectedDay,
              onItemSelected = { selectedDay = it },
              label = { stringResource(R.string.edit_profile_wheel_day, it) },
              modifier = Modifier.weight(1f),
            )
          }
        }
      }
    }
  }
}

@Composable
private fun SaveSuccessOverlay(visible: Boolean) {
  AnimatedVisibility(
    visible = visible,
    enter = fadeIn(),
    exit = fadeOut(),
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(Color.White.copy(alpha = 0.6f)),
      contentAlignment = Alignment.Center,
    ) {
      AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + scaleIn(initialScale = 0.8f),
        exit = fadeOut() + scaleOut(targetScale = 0.8f),
      ) {
        Column(
          modifier = Modifier
            .shadow(24.dp, RoundedCornerShape(24.dp))
            .background(Color.White, RoundedCornerShape(24.dp))
            .border(2.dp, Orange100, RoundedCornerShape(24.dp))
            .padding(horizontal = 32.dp, vertical = 24.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          Box(
            modifier = Modifier
              .size(64.dp)
              .background(Color(0xFFDCFCE7), CircleShape),
            contentAlignment = Alignment.Center,
          ) {
            Icon(
              imageVector = Icons.Filled.CheckCircle,
              contentDescription = null,
              tint = Green500,
              modifier = Modifier.size(36.dp),
            )
          }

          Text(
            text = stringResource(R.string.edit_profile_save_success),
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Orange900,
          )
        }
      }
    }
  }
}
