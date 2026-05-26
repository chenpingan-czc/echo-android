package com.hope.echo.ui.screen

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.outlined.DriveFolderUpload
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hope.echo.R
import com.hope.echo.ui.theme.Gray100
import com.hope.echo.ui.theme.Gray600
import com.hope.echo.ui.theme.Orange100
import com.hope.echo.ui.theme.Orange400
import com.hope.echo.ui.theme.Orange50
import com.hope.echo.ui.theme.Orange900
import com.hope.echo.ui.theme.Pink400
import com.hope.echo.ui.theme.Pink500
import com.hope.echo.ui.viewmodel.MyVoiceViewModel
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.delay
import java.io.File

private val RecordBackground = Color(0xFFFDF8F5)

@Composable
fun RecordVoiceScreen(
  onBack: () -> Unit = {},
  voiceViewModel: MyVoiceViewModel = viewModel(),
) {
  val context = LocalContext.current
  val voiceUiState by voiceViewModel.uiState.collectAsState()
  var isRecording by remember { mutableStateOf(false) }
  var recordingComplete by remember { mutableStateOf(false) }
  var seconds by remember { mutableIntStateOf(0) }
  var mediaRecorder by remember { mutableStateOf<MediaRecorder?>(null) }
  var outputFile by remember { mutableStateOf<File?>(null) }
  var isUploaded by remember { mutableStateOf(false) }
  var showNameDialog by remember { mutableStateOf(false) }
  var voiceName by remember { mutableStateOf("") }
  var hasPermission by remember {
    mutableStateOf(
      ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
        PackageManager.PERMISSION_GRANTED
    )
  }
  var pendingRecordAfterPermission by remember { mutableStateOf(false) }

  val permissionLauncher = rememberLauncherForActivityResult(
    ActivityResultContracts.RequestPermission()
  ) { granted ->
    hasPermission = granted
    if (granted) pendingRecordAfterPermission = true
  }

  val audioPickerLauncher = rememberLauncherForActivityResult(
    ActivityResultContracts.GetContent()
  ) { uri: Uri? ->
    if (uri != null) {
      val file = File(context.cacheDir, "voice_picked_${System.currentTimeMillis()}.m4a")
      context.contentResolver.openInputStream(uri)?.use { input ->
        file.outputStream().use { output -> input.copyTo(output) }
      }
      outputFile = file
      isUploaded = true
      recordingComplete = true
    }
  }

  LaunchedEffect(isRecording) {
    if (isRecording) {
      while (true) {
        delay(1000)
        seconds++
      }
    }
  }

  DisposableEffect(Unit) {
    onDispose {
      try {
        mediaRecorder?.apply {
          stop()
          release()
        }
      } catch (_: Exception) {
      }
    }
  }

  fun startRecording() {
    if (!hasPermission) {
      permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
      return
    }
    val file = File(context.cacheDir, "voice_${System.currentTimeMillis()}.m4a")
    outputFile = file
    seconds = 0
    recordingComplete = false
    val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      MediaRecorder(context)
    } else {
      @Suppress("DEPRECATION")
      MediaRecorder()
    }
    recorder.apply {
      setAudioSource(MediaRecorder.AudioSource.MIC)
      setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
      setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
      setAudioSamplingRate(44100)
      setAudioEncodingBitRate(128000)
      setOutputFile(file.absolutePath)
      prepare()
      start()
    }
    mediaRecorder = recorder
    isRecording = true
  }

  LaunchedEffect(pendingRecordAfterPermission) {
    if (pendingRecordAfterPermission) {
      pendingRecordAfterPermission = false
      startRecording()
    }
  }

  fun stopRecording() {
    try {
      mediaRecorder?.apply {
        stop()
        release()
      }
    } catch (_: Exception) {
    }
    mediaRecorder = null
    isRecording = false
    recordingComplete = true
  }

  fun resetRecording() {
    outputFile?.delete()
    outputFile = null
    isRecording = false
    recordingComplete = false
    isUploaded = false
    seconds = 0
  }

  fun showSaveDialog() {
    voiceName = ""
    showNameDialog = true
  }

  fun confirmSave(name: String) {
    val file = outputFile ?: return
    showNameDialog = false
    voiceViewModel.uploadVoice(file, name) {
      onBack()
    }
  }

  Box(modifier = Modifier.fillMaxSize()) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(RecordBackground)
      .statusBarsPadding()
      .navigationBarsPadding(),
  ) {
    RecordHeader(onBack = onBack)

    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 24.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Spacer(modifier = Modifier.height(16.dp))

      Text(
        text = stringResource(R.string.record_voice_prompt),
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold,
        color = Orange900.copy(alpha = 0.6f),
      )

      Spacer(modifier = Modifier.height(24.dp))

      TextPromptCard(text = voiceUiState.promptText)

      Spacer(modifier = Modifier.weight(1f))

      AnimatedContent(
        targetState = recordingComplete,
        transitionSpec = {
          (fadeIn() + scaleIn(initialScale = 0.9f)) togetherWith
            (fadeOut() + scaleOut(targetScale = 0.9f))
        },
        label = "recordingState",
      ) { complete ->
        if (!complete) {
          RecordingControls(
            isRecording = isRecording,
            seconds = seconds,
            onToggleRecording = {
              if (isRecording) stopRecording() else startRecording()
            },
            onPickFile = { audioPickerLauncher.launch("audio/*") },
          )
        } else {
          CompleteControls(
            seconds = seconds,
            isUploaded = isUploaded,
            onReset = { resetRecording() },
            onSave = { showSaveDialog() },
          )
        }
      }

      Spacer(modifier = Modifier.height(48.dp))
    }
  }

  if (showNameDialog) {
    VoiceNameDialog(
      voiceName = voiceName,
      onNameChange = { voiceName = it },
      onDismiss = { showNameDialog = false },
      onConfirm = { confirmSave(voiceName) },
    )
  }

  if (voiceUiState.isUploading) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(Orange900.copy(alpha = 0.4f))
        .clickable(
          interactionSource = remember { MutableInteractionSource() },
          indication = null,
          onClick = {},
        ),
      contentAlignment = Alignment.Center,
    ) {
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        CircularProgressIndicator(
          color = Color.White,
          strokeWidth = 4.dp,
          modifier = Modifier.size(48.dp),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
          text = stringResource(R.string.record_voice_creating),
          fontSize = 16.sp,
          fontWeight = FontWeight.Bold,
          color = Color.White,
        )
      }
    }
  }
  }
}

@Composable
private fun RecordHeader(onBack: () -> Unit) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 24.dp, vertical = 16.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Box(
      modifier = Modifier
        .size(40.dp)
        .shadow(2.dp, CircleShape)
        .background(Color.White, CircleShape)
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
    Spacer(modifier = Modifier.width(16.dp))
    Text(
      text = stringResource(R.string.record_voice_title),
      fontSize = 20.sp,
      fontWeight = FontWeight.ExtraBold,
      color = Orange900,
    )
  }
}

@Composable
private fun TextPromptCard(text: String?) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .shadow(4.dp, RoundedCornerShape(24.dp))
      .background(Color.White, RoundedCornerShape(24.dp))
      .border(2.dp, Orange100.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
      .padding(10.dp),
    contentAlignment = Alignment.Center,
  ) {
    Text(
      text = "\u201C",
      fontSize = 96.sp,
      fontWeight = FontWeight.Black,
      color = Orange50,
      modifier = Modifier.align(Alignment.TopStart),
    )
    Text(
      text = "\u201D",
      fontSize = 96.sp,
      fontWeight = FontWeight.Black,
      color = Orange50,
      modifier = Modifier.align(Alignment.BottomEnd),
    )
    if (text != null) {
      Text(
        text = text,
        fontSize = 22.sp,
        lineHeight = 48.sp,
        fontWeight = FontWeight.Bold,
        color = Orange900,
        textAlign = TextAlign.Center,
        letterSpacing = 1.sp,
      )
    }
  }
}

@Composable
private fun RecordingControls(
  isRecording: Boolean,
  seconds: Int,
  onToggleRecording: () -> Unit,
  onPickFile: () -> Unit,
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier.fillMaxWidth(),
  ) {
    Text(
      text = formatRecordTime(seconds),
      fontSize = 30.sp,
      fontWeight = FontWeight.Black,
      color = if (isRecording) Pink500 else Orange900,
    )

    Spacer(modifier = Modifier.height(24.dp))

    Box(contentAlignment = Alignment.Center) {
      if (isRecording) {
        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
        val scale by infiniteTransition.animateFloat(
          initialValue = 1f,
          targetValue = 1.5f,
          animationSpec = infiniteRepeatable(tween(1500), RepeatMode.Restart),
          label = "pulseScale",
        )
        val alpha by infiniteTransition.animateFloat(
          initialValue = 0.6f,
          targetValue = 0f,
          animationSpec = infiniteRepeatable(tween(1500), RepeatMode.Restart),
          label = "pulseAlpha",
        )
        Box(
          modifier = Modifier
            .size(96.dp)
            .scale(scale)
            .background(Pink400.copy(alpha = alpha), CircleShape),
        )
      }

      val gradient = Brush.linearGradient(colors = listOf(Orange400, Pink500))
      Box(
        modifier = Modifier
          .size(96.dp)
          .shadow(12.dp, CircleShape, ambientColor = Pink500.copy(alpha = 0.3f))
          .then(
            if (isRecording)
              Modifier
                .background(Color.White, CircleShape)
                .border(4.dp, Orange100.copy(alpha = 0.5f), CircleShape)
            else
              Modifier.background(gradient, CircleShape)
          )
          .clip(CircleShape)
          .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onToggleRecording,
          ),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = if (isRecording) Icons.Filled.Stop else Icons.Filled.Mic,
          contentDescription = if (isRecording) stringResource(R.string.record_voice_stop) else stringResource(R.string.record_voice_start),
          tint = if (isRecording) Pink500 else Color.White,
          modifier = Modifier.size(if (isRecording) 32.dp else 40.dp),
        )
      }
    }

    Spacer(modifier = Modifier.height(24.dp))

    if (!isRecording) {
      Row(
        modifier = Modifier
          .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onPickFile,
          ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
      ) {
        Icon(
          imageVector = Icons.Outlined.DriveFolderUpload,
          contentDescription = null,
          tint = Orange400,
          modifier = Modifier.size(22.dp),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = stringResource(R.string.record_voice_pick_audio),
          fontSize = 16.sp,
          fontWeight = FontWeight.Bold,
          color = Orange400,
        )
      }
      Spacer(modifier = Modifier.height(16.dp))
    }
  }
}

@Composable
private fun CompleteControls(
  seconds: Int,
  isUploaded: Boolean,
  onReset: () -> Unit,
  onSave: () -> Unit,
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier.fillMaxWidth(),
  ) {
    Text(
      text = if (isUploaded) stringResource(R.string.record_voice_audio_selected) else stringResource(R.string.record_voice_complete, formatRecordTime(seconds)),
      fontSize = 20.sp,
      fontWeight = FontWeight.Bold,
      color = Orange900,
    )

    Spacer(modifier = Modifier.height(32.dp))

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      Box(
        modifier = Modifier
          .weight(1f)
          .shadow(2.dp, RoundedCornerShape(16.dp))
          .background(Color.White, RoundedCornerShape(16.dp))
          .border(2.dp, Orange100, RoundedCornerShape(16.dp))
          .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onReset,
          )
          .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center,
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Filled.Refresh,
            contentDescription = null,
            tint = Orange900,
            modifier = Modifier.size(20.dp),
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = if (isUploaded) stringResource(R.string.record_voice_reselect) else stringResource(R.string.record_voice_rerecord),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Orange900,
          )
        }
      }

      val gradient = Brush.horizontalGradient(colors = listOf(Orange400, Pink400))
      Box(
        modifier = Modifier
          .weight(1f)
          .shadow(8.dp, RoundedCornerShape(16.dp), ambientColor = Pink500.copy(alpha = 0.3f))
          .background(gradient, RoundedCornerShape(16.dp))
          .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onSave,
          )
          .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center,
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Filled.Check,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(20.dp),
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = stringResource(R.string.record_voice_save),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
          )
        }
      }
    }
  }
}

@Composable
private fun VoiceNameDialog(
  voiceName: String,
  onNameChange: (String) -> Unit,
  onDismiss: () -> Unit,
  onConfirm: () -> Unit,
) {
  val focusRequester = remember { FocusRequester() }
  LaunchedEffect(Unit) { focusRequester.requestFocus() }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false),
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 24.dp),
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .shadow(16.dp, RoundedCornerShape(24.dp))
          .background(Color.White, RoundedCornerShape(24.dp))
          .border(2.dp, Orange100, RoundedCornerShape(24.dp))
          .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        Text(
          text = stringResource(R.string.record_voice_name_prompt),
          fontSize = 20.sp,
          fontWeight = FontWeight.ExtraBold,
          color = Orange900,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Box(
          modifier = Modifier
            .fillMaxWidth()
            .background(RecordBackground, RoundedCornerShape(16.dp))
            .border(2.dp, Orange100, RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 16.dp),
        ) {
          if (voiceName.isEmpty()) {
            Text(
              text = stringResource(R.string.record_voice_name_placeholder),
              fontSize = 16.sp,
              fontWeight = FontWeight.Bold,
              color = Orange900.copy(alpha = 0.3f),
            )
          }
          BasicTextField(
            value = voiceName,
            onValueChange = onNameChange,
            singleLine = true,
            textStyle = TextStyle(
              fontSize = 16.sp,
              fontWeight = FontWeight.Bold,
              color = Orange900,
            ),
            cursorBrush = SolidColor(Orange400),
            modifier = Modifier
              .fillMaxWidth()
              .focusRequester(focusRequester),
          )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          Box(
            modifier = Modifier
              .weight(1f)
              .background(Color.White, RoundedCornerShape(12.dp))
              .border(2.dp, Orange100, RoundedCornerShape(12.dp))
              .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss,
              )
              .padding(vertical = 14.dp),
            contentAlignment = Alignment.Center,
          ) {
            Text(
              text = stringResource(R.string.record_voice_dialog_cancel),
              fontSize = 16.sp,
              fontWeight = FontWeight.Bold,
              color = Orange900,
            )
          }

          val canConfirm = voiceName.isNotBlank()
          val gradient = Brush.horizontalGradient(colors = listOf(Orange400, Pink400))
          Box(
            modifier = Modifier
              .weight(1f)
              .shadow(
                if (canConfirm) 6.dp else 0.dp,
                RoundedCornerShape(12.dp),
                ambientColor = Pink500.copy(alpha = 0.2f),
              )
              .background(gradient, RoundedCornerShape(12.dp))
              .then(
                if (canConfirm)
                  Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onConfirm,
                  )
                else Modifier
              )
              .clip(RoundedCornerShape(12.dp))
              .padding(vertical = 14.dp),
            contentAlignment = Alignment.Center,
          ) {
            Text(
              text = stringResource(R.string.record_voice_dialog_confirm),
              fontSize = 16.sp,
              fontWeight = FontWeight.Bold,
              color = Color.White.copy(alpha = if (canConfirm) 1f else 0.5f),
            )
          }
        }
      }
    }
  }
}

private fun formatRecordTime(secs: Int): String {
  val m = secs / 60
  val s = secs % 60
  return "${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}"
}
