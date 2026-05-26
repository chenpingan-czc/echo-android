package com.hope.echo.ui.screen

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.ui.text.style.TextOverflow
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import android.os.Handler
import android.os.Looper
import com.hope.echo.R
import com.hope.echo.data.api.BookVoiceDto
import com.hope.echo.ui.camera.GuideRectNorm
import com.hope.echo.ui.camera.clampGuideRectNorm
import com.hope.echo.ui.camera.cropJpegToPreviewGuide
import com.hope.echo.ui.camera.defaultGuideRectNorm
import com.hope.echo.ui.theme.Orange400
import com.hope.echo.ui.theme.Orange500
import com.hope.echo.ui.theme.Pink400
import com.hope.echo.ui.theme.Pink500
import com.hope.echo.ui.viewmodel.UploadViewModel
import java.io.File
import java.util.concurrent.Executors

@Composable
fun UploadScreen(
  onBack: () -> Unit,
  onUploadSuccess: () -> Unit = {},
  onNavigateToRecordVoice: () -> Unit = {},
) {
  val viewModel: UploadViewModel = viewModel()
  val state by viewModel.uiState.collectAsState()
  val context = LocalContext.current

  LaunchedEffect(state.uploadSuccess) {
    if (state.uploadSuccess) {
      onUploadSuccess()
    }
  }

  val step =
    when {
      state.isUploading || state.uploadSuccess -> "processing"
      else -> "camera"
    }

  AnimatedContent(
    targetState = step,
    transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(300)) },
    modifier = Modifier.fillMaxSize(),
    label = "uploadStep",
  ) { currentStep ->
    when (currentStep) {
      "camera" ->
        CameraStep(
          capturedImages = state.selectedImages,
          onCapture = { uri -> viewModel.addImages(listOf(uri)) },
          onAlbumSelect = { uris -> viewModel.addImages(uris) },
          onRemove = viewModel::removeImage,
          onBack = onBack,
          onFinish = { viewModel.upload(context) },
          voices = state.voices,
          selectedVoiceId = state.selectedVoiceId,
          onSelectVoice = viewModel::selectVoice,
          onNavigateToRecordVoice = onNavigateToRecordVoice,
        )
      "processing" -> ProcessingStep()
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CameraStep(
  capturedImages: List<Uri>,
  onCapture: (Uri) -> Unit,
  onAlbumSelect: (List<Uri>) -> Unit,
  onRemove: (Uri) -> Unit,
  onBack: () -> Unit,
  onFinish: () -> Unit,
  voices: List<BookVoiceDto>,
  selectedVoiceId: String?,
  onSelectVoice: (String?) -> Unit,
  onNavigateToRecordVoice: () -> Unit,
) {
  var showVoiceSheet by remember { mutableStateOf(false) }
  val context = LocalContext.current
  val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
  var hasCameraPermission by remember {
    mutableStateOf(
      ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
        PackageManager.PERMISSION_GRANTED
    )
  }

  val permissionLauncher =
    rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
      hasCameraPermission = granted
    }

  val albumLauncher =
    rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
      if (uris.isNotEmpty()) onAlbumSelect(uris)
    }

  val imageCapture = remember { ImageCapture.Builder().build() }
  val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
  val listState = rememberLazyListState()
  var boundCamera by remember { mutableStateOf<Camera?>(null) }
  var guideRect by remember { mutableStateOf(defaultGuideRectNorm()) }
  var overlayPx by remember { mutableStateOf(IntSize.Zero) }

  LaunchedEffect(Unit) {
    if (!hasCameraPermission) permissionLauncher.launch(Manifest.permission.CAMERA)
  }

  LaunchedEffect(capturedImages.size) {
    if (capturedImages.isNotEmpty()) listState.animateScrollToItem(capturedImages.size - 1)
  }

  Box(
    modifier =
      Modifier.fillMaxSize()
        .background(Color.Black)
        .onGloballyPositioned { overlayPx = it.size }
  ) {
    if (hasCameraPermission) {
      AndroidView(
        factory = { ctx ->
          val previewView = PreviewView(ctx)
          val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
          cameraProviderFuture.addListener(
            {
              val cameraProvider = cameraProviderFuture.get()
              val preview = Preview.Builder().build()
              preview.setSurfaceProvider(previewView.surfaceProvider)
              val cameraSelector =
                CameraSelector.Builder()
                  .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                  .build()
              try {
                cameraProvider.unbindAll()
                val cam =
                  cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture,
                  )
                previewView.post { boundCamera = cam }
              } catch (_: Exception) {}
            },
            ContextCompat.getMainExecutor(ctx),
          )
          previewView
        },
        modifier = Modifier.fillMaxSize(),
      )
      InteractiveCameraGuideOverlay(
        guide = guideRect,
        onGuideChange = { guideRect = it },
        camera = boundCamera,
      )
    } else {
      NoCameraView()
    }

    Column(modifier = Modifier.fillMaxSize()) {
      CameraHeader(
        capturedImages = capturedImages,
        listState = listState,
        onBack = onBack,
        onRemove = onRemove,
        onVoiceClick = { showVoiceSheet = true },
      )
      Spacer(modifier = Modifier.weight(1f))
      CameraBottomControls(
        hasImages = capturedImages.isNotEmpty(),
        onAlbumClick = { albumLauncher.launch("image/*") },
        onCaptureClick = {
          if (hasCameraPermission) {
            capturePhoto(
              context,
              imageCapture,
              cameraExecutor,
              guideRect,
              overlayPx.width,
              overlayPx.height,
              onCapture,
            )
          } else {
            albumLauncher.launch("image/*")
          }
        },
        onFinish = onFinish,
      )
    }

    if (showVoiceSheet) {
      VoiceSelectionSheet(
        voices = voices,
        selectedVoiceId = selectedVoiceId,
        onConfirm = { voiceId ->
          onSelectVoice(voiceId)
          showVoiceSheet = false
        },
        onDismiss = { showVoiceSheet = false },
        onGoToRecord = { voiceId ->
          onSelectVoice(voiceId)
          showVoiceSheet = false
          onNavigateToRecordVoice()
        },
      )
    }
  }
}

@Composable
private fun NoCameraView() {
  Box(
    modifier = Modifier.fillMaxSize().background(Color(0xFF111827)),
    contentAlignment = Alignment.Center,
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(12.dp),
      modifier = Modifier.padding(horizontal = 40.dp),
    ) {
      Icon(
        imageVector = Icons.Filled.PhotoLibrary,
        contentDescription = null,
        tint = Color.White.copy(alpha = 0.3f),
        modifier = Modifier.size(48.dp),
      )
      Text(text = stringResource(R.string.upload_camera_unavailable), color = Color.White.copy(alpha = 0.5f), fontSize = 16.sp)
      Text(
        text = stringResource(R.string.upload_camera_hint),
        color = Color.White.copy(alpha = 0.3f),
        fontSize = 12.sp,
        textAlign = TextAlign.Center,
        lineHeight = 18.sp,
      )
    }
  }
}

private enum class GuideCorner {
  TOP_LEFT,
  TOP_RIGHT,
  BOTTOM_LEFT,
  BOTTOM_RIGHT,
}

@Composable
private fun InteractiveCameraGuideOverlay(
  guide: GuideRectNorm,
  onGuideChange: (GuideRectNorm) -> Unit,
  camera: Camera?,
) {
  val cameraRef = rememberUpdatedState(camera)
  BoxWithConstraints(Modifier.fillMaxSize()) {
    val w = maxWidth
    val h = maxHeight
    val handle = 44.dp

    CameraGuideDimCanvas(guide = guide)

    Box(
      Modifier.fillMaxSize().pointerInput(camera) {
        detectTransformGestures { _, _, zoomChange, _ ->
          val cam = cameraRef.value ?: return@detectTransformGestures
          val state = cam.cameraInfo.zoomState.value ?: return@detectTransformGestures
          val next =
            (state.zoomRatio * zoomChange).coerceIn(state.minZoomRatio, state.maxZoomRatio)
          cam.cameraControl.setZoomRatio(next)
        }
      }
    )

    for (corner in GuideCorner.entries) {
      GuideCornerHandle(
        corner = corner,
        guide = guide,
        containerW = w,
        containerH = h,
        handleSize = handle,
        onGuideChange = onGuideChange,
      )
    }
  }
}

@Composable
private fun CameraGuideDimCanvas(guide: GuideRectNorm) {
  Canvas(
    modifier =
      Modifier.fillMaxSize().graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
  ) {
    val guideW = (guide.right - guide.left) * size.width
    val guideH = (guide.bottom - guide.top) * size.height
    val guideX = guide.left * size.width
    val guideY = guide.top * size.height
    val cornerR = minOf(40.dp.toPx(), guideW * 0.12f, guideH * 0.12f).coerceAtLeast(8.dp.toPx())
    val cornerLen = 44.dp.toPx()
    val strokeW = 4.dp.toPx()
    val orange = Color(0xFFFB923C)

    drawRect(color = Color.Black.copy(alpha = 0.5f))

    drawRoundRect(
      color = Color.Transparent,
      topLeft = Offset(guideX, guideY),
      size = Size(guideW, guideH),
      cornerRadius = CornerRadius(cornerR),
      blendMode = BlendMode.Clear,
    )

    drawRoundRect(
      color = Color.White.copy(alpha = 0.3f),
      topLeft = Offset(guideX, guideY),
      size = Size(guideW, guideH),
      cornerRadius = CornerRadius(cornerR),
      style = Stroke(width = 2.dp.toPx()),
    )

    val rx = guideX + guideW
    val by = guideY + guideH
    val hh = strokeW / 2f

    drawLine(
      orange,
      Offset(guideX - hh, guideY + cornerLen),
      Offset(guideX - hh, guideY - hh),
      strokeW,
      StrokeCap.Round,
    )
    drawLine(
      orange,
      Offset(guideX - hh, guideY - hh),
      Offset(guideX + cornerLen, guideY - hh),
      strokeW,
      StrokeCap.Round,
    )
    drawLine(
      orange,
      Offset(rx + hh, guideY + cornerLen),
      Offset(rx + hh, guideY - hh),
      strokeW,
      StrokeCap.Round,
    )
    drawLine(
      orange,
      Offset(rx + hh, guideY - hh),
      Offset(rx - cornerLen, guideY - hh),
      strokeW,
      StrokeCap.Round,
    )
    drawLine(
      orange,
      Offset(guideX - hh, by - cornerLen),
      Offset(guideX - hh, by + hh),
      strokeW,
      StrokeCap.Round,
    )
    drawLine(
      orange,
      Offset(guideX - hh, by + hh),
      Offset(guideX + cornerLen, by + hh),
      strokeW,
      StrokeCap.Round,
    )
    drawLine(
      orange,
      Offset(rx + hh, by - cornerLen),
      Offset(rx + hh, by + hh),
      strokeW,
      StrokeCap.Round,
    )
    drawLine(
      orange,
      Offset(rx + hh, by + hh),
      Offset(rx - cornerLen, by + hh),
      strokeW,
      StrokeCap.Round,
    )
  }
}

@Composable
private fun GuideCornerHandle(
  corner: GuideCorner,
  guide: GuideRectNorm,
  containerW: Dp,
  containerH: Dp,
  handleSize: Dp,
  onGuideChange: (GuideRectNorm) -> Unit,
) {
  val density = LocalDensity.current
  val containerWPx = with(density) { containerW.toPx() }
  val containerHPx = with(density) { containerH.toPx() }
  val onGuide = rememberUpdatedState(onGuideChange)
  val guideRef = rememberUpdatedState(guide)
  val half = handleSize / 2
  val offsetX =
    when (corner) {
      GuideCorner.TOP_LEFT, GuideCorner.BOTTOM_LEFT -> containerW * guide.left - half
      GuideCorner.TOP_RIGHT, GuideCorner.BOTTOM_RIGHT -> containerW * guide.right - half
    }
  val offsetY =
    when (corner) {
      GuideCorner.TOP_LEFT, GuideCorner.TOP_RIGHT -> containerH * guide.top - half
      GuideCorner.BOTTOM_LEFT, GuideCorner.BOTTOM_RIGHT -> containerH * guide.bottom - half
    }
  Box(
    Modifier.offset(offsetX, offsetY)
      .size(handleSize)
      .pointerInput(corner, containerWPx, containerHPx) {
        var acc = guideRef.value
        detectDragGestures(
          onDragStart = { acc = guideRef.value },
          onDrag = { _, amount ->
            val dw = amount.x / containerWPx
            val dh = amount.y / containerHPx
            acc =
              clampGuideRectNorm(
                when (corner) {
                  GuideCorner.TOP_LEFT ->
                    acc.copy(left = acc.left + dw, top = acc.top + dh)
                  GuideCorner.TOP_RIGHT ->
                    acc.copy(right = acc.right + dw, top = acc.top + dh)
                  GuideCorner.BOTTOM_LEFT ->
                    acc.copy(left = acc.left + dw, bottom = acc.bottom + dh)
                  GuideCorner.BOTTOM_RIGHT ->
                    acc.copy(right = acc.right + dw, bottom = acc.bottom + dh)
                }
              )
            onGuide.value(acc)
          },
        )
      },
    contentAlignment = Alignment.Center,
  ) {
    Box(
      Modifier.size(handleSize * 0.45f)
        .clip(CircleShape)
        .background(Color.White.copy(alpha = 0.95f))
        .border(2.dp, Color(0xFFFB923C), CircleShape)
    )
  }
}

@Composable
private fun CameraHeader(
  capturedImages: List<Uri>,
  listState: androidx.compose.foundation.lazy.LazyListState,
  onBack: () -> Unit,
  onRemove: (Uri) -> Unit,
  onVoiceClick: () -> Unit,
) {
  Box(
    modifier =
      Modifier.fillMaxWidth()
        .background(
          Brush.verticalGradient(
            listOf(Color.Black.copy(alpha = 0.8f), Color.Black.copy(alpha = 0.4f), Color.Transparent)
          )
        )
        .statusBarsPadding()
        .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 24.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Box(
        modifier =
          Modifier.size(40.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.4f))
            .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape)
            .clickable(
              interactionSource = remember { MutableInteractionSource() },
              indication = null,
              onClick = onBack,
            ),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = Icons.Filled.ArrowBackIosNew,
          contentDescription = stringResource(R.string.common_back),
          tint = Color.White,
          modifier = Modifier.size(20.dp),
        )
      }

      if (capturedImages.isEmpty()) {
        Column(
          modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center,
        ) {
          Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            DemoCoverHint(painterResource(R.drawable.demo1), rotate180 = true)
            DemoCoverHint(painterResource(R.drawable.demo2))
            DemoCoverHint(painterResource(R.drawable.demo3))
          }
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = stringResource(R.string.upload_demo_hint),
            color = Color.White.copy(alpha = 0.9f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.5.sp,
          )
        }
        VoiceButton(onClick = onVoiceClick)
      } else {
        LazyRow(
          state = listState,
          horizontalArrangement = Arrangement.spacedBy(12.dp),
          contentPadding = PaddingValues(horizontal = 8.dp),
          modifier = Modifier.weight(1f).height(76.dp),
        ) {
          items(capturedImages, key = { it.toString() }) { uri ->
            PageThumbnail(uri = uri, onRemove = { onRemove(uri) })
          }
        }
        VoiceButton(onClick = onVoiceClick)
      }
    }
  }
}

@Composable
private fun DemoCoverHint(
  painter: androidx.compose.ui.graphics.painter.Painter,
  rotate180: Boolean = false,
) {
  Image(
    painter = painter,
    contentDescription = null,
    contentScale = ContentScale.Crop,
    modifier = Modifier
      .size(68.dp, 52.dp)
      .clip(RoundedCornerShape(10.dp))
      .border(1.5.dp, Color.White.copy(alpha = 0.7f), RoundedCornerShape(10.dp))
      .then(if (rotate180) Modifier.rotate(180f) else Modifier),
  )
}

@Composable
private fun PageThumbnail(uri: Uri, onRemove: () -> Unit) {
  Box(modifier = Modifier.size(52.dp, 68.dp)) {
    AsyncImage(
      model = ImageRequest.Builder(LocalContext.current).data(uri).crossfade(true).build(),
      contentDescription = null,
      contentScale = ContentScale.Crop,
      modifier =
        Modifier.fillMaxSize()
          .clip(RoundedCornerShape(10.dp))
          .border(1.5.dp, Color.White, RoundedCornerShape(10.dp)),
    )
    Box(
      modifier =
        Modifier.align(Alignment.TopEnd)
          .offset(x = 6.dp, y = (-6).dp)
          .size(20.dp)
          .clip(CircleShape)
          .background(Color(0xFFEF4444))
          .border(1.dp, Color.White, CircleShape)
          .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onRemove,
          ),
      contentAlignment = Alignment.Center,
    ) {
      Icon(
        imageVector = Icons.Filled.Close,
        contentDescription = stringResource(R.string.common_delete),
        tint = Color.White,
        modifier = Modifier.size(10.dp),
      )
    }
  }
}

@Composable
private fun VoiceButton(onClick: () -> Unit) {
  val interactionSource = remember { MutableInteractionSource() }
  val pressed by interactionSource.collectIsPressedAsState()
  val pressScale by animateFloatAsState(
    targetValue = if (pressed) 0.85f else 1f,
    animationSpec = tween(100),
    label = "voiceBtnPress",
  )
  val infiniteTransition = rememberInfiniteTransition(label = "voiceBtnIdle")
  val rotation by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 0f,
    animationSpec =
      infiniteRepeatable(
        animation =
          keyframes {
            durationMillis = 2500
            0f at 0
            15f at 417
            -15f at 834
            8f at 1250
            -8f at 1667
            0f at 2500
          },
        repeatMode = RepeatMode.Restart,
      ),
    label = "voiceBtnRot",
  )
  val idleScale by infiniteTransition.animateFloat(
    initialValue = 1f,
    targetValue = 1f,
    animationSpec =
      infiniteRepeatable(
        animation =
          keyframes {
            durationMillis = 2500
            1f at 0
            1.3f at 1250
            1f at 2500
          },
        repeatMode = RepeatMode.Restart,
      ),
    label = "voiceBtnScale",
  )
  val combinedScale = idleScale * pressScale
  Box(
    modifier =
      Modifier.size(40.dp)
        .shadow(
          elevation = 4.dp,
          shape = CircleShape,
          ambientColor = Pink400.copy(alpha = 0.35f),
          spotColor = Pink400.copy(alpha = 0.45f),
        )
        .clip(CircleShape)
        .background(Color.White)
        .border(2.dp, Color(0xFFFCE7F3), CircleShape)
        .graphicsLayer {
          rotationZ = rotation
          scaleX = combinedScale
          scaleY = combinedScale
        }
        .clickable(
          interactionSource = interactionSource,
          indication = null,
          onClick = onClick,
        ),
    contentAlignment = Alignment.Center,
  ) {
    Icon(
      imageVector = Icons.Filled.AutoAwesome,
      contentDescription = stringResource(R.string.upload_choose_voice_cd),
      tint = Pink500,
      modifier = Modifier.size(20.dp),
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VoiceSelectionSheet(
  voices: List<BookVoiceDto>,
  selectedVoiceId: String?,
  onConfirm: (String?) -> Unit,
  onDismiss: () -> Unit,
  onGoToRecord: (String?) -> Unit,
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  var pendingVoiceId by remember(voices, selectedVoiceId) {
    mutableStateOf(selectedVoiceId ?: voices.firstOrNull()?.voiceId)
  }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = Color.White,
    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
    dragHandle = {
      Box(
        modifier = Modifier.padding(top = 12.dp, bottom = 8.dp),
        contentAlignment = Alignment.Center,
      ) {
        Box(
          modifier =
            Modifier.width(48.dp)
              .height(5.dp)
              .clip(RoundedCornerShape(2.5.dp))
              .background(Color(0xFFE5E7EB))
        )
      }
    },
  ) {
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val sheetBodyHeight = screenHeight * 0.86f

    Column(
      modifier =
        Modifier.fillMaxWidth()
          .height(sheetBodyHeight)
          .padding(horizontal = 24.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Spacer(modifier = Modifier.width(40.dp))
        Row(
          modifier = Modifier.weight(1f),
          horizontalArrangement = Arrangement.Center,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Icon(
            imageVector = Icons.Filled.AutoAwesome,
            contentDescription = null,
            tint = Pink400,
            modifier = Modifier.size(20.dp),
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = stringResource(R.string.upload_choose_voice),
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF7C2D12),
          )
        }
        Box(
          modifier =
            Modifier.size(40.dp)
              .shadow(2.dp, CircleShape)
              .clip(CircleShape)
              .background(Color.White)
              .border(2.dp, Color(0xFFFFEDD5), CircleShape)
              .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { onConfirm(pendingVoiceId) },
              ),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = Icons.Filled.Check,
            contentDescription = stringResource(R.string.common_done),
            tint = Color(0xFF22C55E),
            modifier = Modifier.size(20.dp),
          )
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.weight(1f).fillMaxWidth(),
      ) {
        items(voices, key = { it.voiceId ?: it.name }) { voice ->
          VoiceItem(
            voice = voice,
            isSelected = voice.voiceId == pendingVoiceId,
            onClick = { pendingVoiceId = voice.voiceId },
            modifier = Modifier.fillMaxWidth(),
          )
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      Box(
        modifier =
          Modifier.fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(24.dp), ambientColor = Pink400.copy(alpha = 0.35f))
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.horizontalGradient(listOf(Orange500, Pink400)))
            .border(3.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(24.dp))
            .clickable(
              interactionSource = remember { MutableInteractionSource() },
              indication = null,
              onClick = { onGoToRecord(pendingVoiceId) },
            )
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center,
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center,
        ) {
          Icon(
            imageVector = Icons.Filled.Mic,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(24.dp),
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = stringResource(R.string.upload_record_my_voice),
            color = Color.White,
            fontWeight = FontWeight.Black,
            fontSize = 18.sp,
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))
      Spacer(modifier = Modifier.fillMaxWidth().windowInsetsBottomHeight(WindowInsets.navigationBars))
    }
  }
}

@Composable
private fun VoiceItem(
  voice: BookVoiceDto,
  isSelected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val bgColor =
    if (isSelected) Color(0xFFFDF2F8) else Color(0xFFFFF7ED)
  val borderColor =
    if (isSelected) Pink400 else Color.Transparent
  val textColor =
    if (isSelected) Pink500 else Color(0xFF7C2D12)
  val cardScale by animateFloatAsState(
    targetValue = if (isSelected) 1.02f else 1f,
    animationSpec = tween(200),
    label = "voiceCardScale",
  )

  Box(
    modifier =
      modifier
        .fillMaxWidth()
        .graphicsLayer {
          scaleX = cardScale
          scaleY = cardScale
        }
        .then(
          if (isSelected) {
            Modifier.shadow(6.dp, RoundedCornerShape(24.dp), ambientColor = Pink400.copy(alpha = 0.4f))
          } else {
            Modifier
          }
        )
        .clip(RoundedCornerShape(24.dp))
        .background(bgColor)
        .then(
          if (isSelected) Modifier.border(4.dp, borderColor, RoundedCornerShape(24.dp))
          else Modifier.border(4.dp, Color.Transparent, RoundedCornerShape(24.dp))
        )
        .clickable(
          interactionSource = remember { MutableInteractionSource() },
          indication = null,
          onClick = onClick,
        )
        .padding(vertical = 20.dp),
    contentAlignment = Alignment.Center,
  ) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      Text(text = voice.emoji, fontSize = 36.sp)
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = voice.name,
        fontWeight = FontWeight.Bold,
        color = textColor,
        fontSize = 14.sp,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
    }
  }
}

@Composable
private fun CameraBottomControls(
  hasImages: Boolean,
  onAlbumClick: () -> Unit,
  onCaptureClick: () -> Unit,
  onFinish: () -> Unit,
) {
  Box(
    modifier =
      Modifier.fillMaxWidth()
        .background(
          Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f), Color.Black))
        )
        .padding(top = 32.dp, bottom = 64.dp),
    contentAlignment = Alignment.Center,
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 40.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      // Album button
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier =
          Modifier.size(56.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.15f))
            .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
            .clickable(
              interactionSource = remember { MutableInteractionSource() },
              indication = null,
              onClick = onAlbumClick,
            ),
      ) {
        Icon(
          imageVector = Icons.Filled.PhotoLibrary,
          contentDescription = stringResource(R.string.upload_album),
          tint = Color.White,
          modifier = Modifier.size(22.dp),
        )
        Text(text = stringResource(R.string.upload_album), color = Color.White.copy(alpha = 0.8f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
      }

      // Shutter button
      Box(
        modifier =
          Modifier.size(80.dp)
            .clip(CircleShape)
            .background(Color.White)
            .border(4.dp, Color(0xFFD1D5DB), CircleShape)
            .clickable(
              interactionSource = remember { MutableInteractionSource() },
              indication = null,
              onClick = onCaptureClick,
            ),
        contentAlignment = Alignment.Center,
      ) {
        Box(modifier = Modifier.size(64.dp).border(2.dp, Color.White.copy(alpha = 0.5f), CircleShape))
      }

      // Done button (fixed size, animate alpha+scale)
      val doneAlpha by animateFloatAsState(
        targetValue = if (hasImages) 1f else 0f,
        animationSpec = tween(200),
        label = "doneAlpha",
      )
      val doneScale by animateFloatAsState(
        targetValue = if (hasImages) 1f else 0.7f,
        animationSpec = tween(200),
        label = "doneScale",
      )
      Box(modifier = Modifier.size(56.dp), contentAlignment = Alignment.Center) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center,
          modifier =
            Modifier.size(56.dp)
              .graphicsLayer { alpha = doneAlpha; scaleX = doneScale; scaleY = doneScale }
              .clip(CircleShape)
              .background(Brush.linearGradient(listOf(Orange500, Pink400)))
              .clickable(
                enabled = hasImages,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onFinish,
              ),
        ) {
          Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = stringResource(R.string.common_done),
            tint = Color.White,
            modifier = Modifier.size(22.dp),
          )
          Text(text = stringResource(R.string.upload_done), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}

@Composable
private fun ProcessingStep() {
  val infiniteTransition = rememberInfiniteTransition(label = "wand")
  val rotation by
    infiniteTransition.animateFloat(
      initialValue = 0f,
      targetValue = 360f,
      animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing)),
      label = "wandRotation",
    )
  val progress by
    animateFloatAsState(
      targetValue = 0.9f,
      animationSpec = tween(durationMillis = 3000, easing = FastOutSlowInEasing),
      label = "uploadProgress",
    )

  Box(
    modifier =
      Modifier.fillMaxSize()
        .background(Brush.linearGradient(listOf(Orange400, Pink400))),
    contentAlignment = Alignment.Center,
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier.padding(horizontal = 32.dp),
    ) {
      Icon(
        imageVector = Icons.Filled.AutoAwesome,
        contentDescription = null,
        tint = Color(0xFFFEF08A),
        modifier = Modifier.size(64.dp).graphicsLayer { rotationZ = rotation },
      )
      Spacer(modifier = Modifier.height(32.dp))
      Text(
        text = stringResource(R.string.upload_processing_title),
        color = Color.White,
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 2.sp,
      )
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = stringResource(R.string.upload_processing_subtitle),
        color = Color(0xFFFCE7F3),
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
      )
      Spacer(modifier = Modifier.height(40.dp))
      Box(
        modifier =
          Modifier.width(256.dp)
            .height(12.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(Color.White.copy(alpha = 0.3f))
      ) {
        Box(
          modifier =
            Modifier.fillMaxHeight()
              .fillMaxWidth(progress)
              .clip(RoundedCornerShape(6.dp))
              .background(Color(0xFFFEF08A))
        )
      }
    }
  }
}

private fun capturePhoto(
  context: android.content.Context,
  imageCapture: ImageCapture,
  executor: java.util.concurrent.Executor,
  guide: GuideRectNorm,
  viewWidthPx: Int,
  viewHeightPx: Int,
  onCapture: (Uri) -> Unit,
) {
  val outputFile = File(context.cacheDir, "capture_${System.currentTimeMillis()}.jpg")
  val outputOptions = ImageCapture.OutputFileOptions.Builder(outputFile).build()
  val mainHandler = Handler(Looper.getMainLooper())
  imageCapture.takePicture(
    outputOptions,
    executor,
    object : ImageCapture.OnImageSavedCallback {
      override fun onImageSaved(output: ImageCapture.OutputFileResults) {
        executor.execute {
          val finalFile =
            try {
              cropJpegToPreviewGuide(outputFile, guide, viewWidthPx, viewHeightPx)
            } catch (_: Exception) {
              outputFile
            }
          mainHandler.post { onCapture(Uri.fromFile(finalFile)) }
        }
      }

      override fun onError(exception: ImageCaptureException) {
        // Silently ignore capture errors
      }
    },
  )
}
