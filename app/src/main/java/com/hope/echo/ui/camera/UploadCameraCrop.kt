package com.hope.echo.ui.camera

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/** 取景框在预览区域内的归一化坐标（与全屏叠加层一致，0..1）。 */
data class GuideRectNorm(
  val left: Float,
  val top: Float,
  val right: Float,
  val bottom: Float,
)

fun defaultGuideRectNorm() = GuideRectNorm(0.075f, 0.175f, 0.925f, 0.825f)

fun clampGuideRectNorm(g: GuideRectNorm, minSpan: Float = 0.22f): GuideRectNorm {
  val e = 0.02f
  var l = g.left.coerceIn(e, 1f - e - minSpan)
  var t = g.top.coerceIn(e, 1f - e - minSpan)
  var r = g.right.coerceIn(l + minSpan, 1f - e)
  var b = g.bottom.coerceIn(t + minSpan, 1f - e)
  l = l.coerceIn(e, r - minSpan)
  t = t.coerceIn(e, b - minSpan)
  r = r.coerceIn(l + minSpan, 1f - e)
  b = b.coerceIn(t + minSpan, 1f - e)
  return GuideRectNorm(l, t, r, b)
}

private const val MAX_DECODE_EDGE = 2560

/**
 * 按与 PreviewView 默认 FILL_CENTER 一致的缩放，将取景框映射到 upright 位图并裁剪。
 * 失败时返回 [sourceFile]。
 */
fun cropJpegToPreviewGuide(
  sourceFile: File,
  guide: GuideRectNorm,
  viewWidthPx: Int,
  viewHeightPx: Int,
): File {
  if (viewWidthPx <= 0 || viewHeightPx <= 0) return sourceFile
  val path = sourceFile.absolutePath
  val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
  BitmapFactory.decodeFile(path, bounds)
  if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return sourceFile

  var sample = 1
  while (max(bounds.outWidth, bounds.outHeight) / sample > MAX_DECODE_EDGE) {
    sample *= 2
  }

  val decodeOpts = BitmapFactory.Options().apply { inSampleSize = sample }
  var decoded = BitmapFactory.decodeFile(path, decodeOpts) ?: return sourceFile

  val rotation =
    try {
      ExifInterface(path).rotationDegrees
    } catch (_: Exception) {
      0
    }

  if (rotation != 0) {
    val matrix = Matrix().apply { postRotate(rotation.toFloat()) }
    val rotated =
      Bitmap.createBitmap(decoded, 0, 0, decoded.width, decoded.height, matrix, true)
    if (rotated != decoded) {
      decoded.recycle()
    }
    decoded = rotated
  }

  val bw = decoded.width
  val bh = decoded.height
  if (bw <= 0 || bh <= 0) {
    decoded.recycle()
    return sourceFile
  }

  val vw = viewWidthPx.toFloat()
  val vh = viewHeightPx.toFloat()
  val scale = max(vw / bw, vh / bh)
  val dispWp = bw * scale
  val dispHp = bh * scale
  val ox = (vw - dispWp) / 2f
  val oy = (vh - dispHp) / 2f

  val gx0 = guide.left * vw
  val gy0 = guide.top * vh
  val gx1 = guide.right * vw
  val gy1 = guide.bottom * vh

  val cl = ((gx0 - ox) / scale).roundToInt().coerceIn(0, bw - 1)
  val ct = ((gy0 - oy) / scale).roundToInt().coerceIn(0, bh - 1)
  val cr = ((gx1 - ox) / scale).roundToInt().coerceIn(min(cl + 1, bw), bw)
  val cb = ((gy1 - oy) / scale).roundToInt().coerceIn(min(ct + 1, bh), bh)

  val cw = cr - cl
  val ch = cb - ct
  if (cw <= 0 || ch <= 0) {
    decoded.recycle()
    return sourceFile
  }

  val cropped = Bitmap.createBitmap(decoded, cl, ct, cw, ch)
  decoded.recycle()

  val out = File(sourceFile.parentFile, "crop_${System.currentTimeMillis()}.jpg")
  try {
    FileOutputStream(out).use { fos -> cropped.compress(Bitmap.CompressFormat.JPEG, 92, fos) }
  } finally {
    cropped.recycle()
  }

  if (sourceFile.exists() && sourceFile != out) {
    sourceFile.delete()
  }
  return out
}
