package com.hope.echo.ui.screen

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hope.echo.BuildConfig
import com.hope.echo.R
import com.hope.echo.ui.theme.Orange100
import com.hope.echo.ui.theme.Orange200
import com.hope.echo.ui.theme.Orange300
import com.hope.echo.ui.theme.Orange400
import com.hope.echo.ui.theme.Orange600
import com.hope.echo.ui.theme.Orange900
import com.hope.echo.ui.theme.Pink200
import com.hope.echo.ui.theme.Pink400
import com.hope.echo.ui.theme.Yellow200

@Composable
fun LoginScreen(
  phone: String,
  code: String,
  isLoading: Boolean,
  countdown: Int,
  errorMessage: String? = null,
  isGoogleLoading: Boolean = false,
  onPhoneChange: (String) -> Unit,
  onCodeChange: (String) -> Unit,
  onSendCode: () -> Unit,
  onLogin: () -> Unit,
  onGoogleSignIn: () -> Unit = {},
  onNavigateToWebView: (title: String, url: String) -> Unit = { _, _ -> },
) {
  val infiniteTransition = rememberInfiniteTransition(label = "login_blob")
  val blobOffset1 by
    infiniteTransition.animateFloat(
      initialValue = 0f,
      targetValue = -20f,
      animationSpec =
        infiniteRepeatable(tween(4000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
      label = "loginBlob1Y",
    )
  val blobScale1 by
    infiniteTransition.animateFloat(
      initialValue = 1f,
      targetValue = 1.1f,
      animationSpec =
        infiniteRepeatable(tween(4000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
      label = "loginBlob1Scale",
    )
  val blobOffset2 by
    infiniteTransition.animateFloat(
      initialValue = 0f,
      targetValue = 20f,
      animationSpec =
        infiniteRepeatable(
          tween(5000, delayMillis = 1000, easing = FastOutSlowInEasing),
          RepeatMode.Reverse,
        ),
      label = "loginBlob2Y",
    )
  val blobScale2 by
    infiniteTransition.animateFloat(
      initialValue = 1f,
      targetValue = 1.2f,
      animationSpec =
        infiniteRepeatable(
          tween(5000, delayMillis = 1000, easing = FastOutSlowInEasing),
          RepeatMode.Reverse,
        ),
      label = "loginBlob2Scale",
    )

  val contentAlpha by
    animateFloatAsState(
      targetValue = 1f,
      animationSpec = spring(stiffness = 300f),
      label = "contentAlpha",
    )

  Box(
    modifier = Modifier.fillMaxSize().imePadding().background(Color(0xFFFDF8F5)),
    contentAlignment = Alignment.Center,
  ) {
    Box(
      modifier =
        Modifier.size(80.dp)
          .offset(x = (-60).dp, y = (-200).dp)
          .graphicsLayer {
            translationY = blobOffset1
            scaleX = blobScale1
            scaleY = blobScale1
          }
          .blur(24.dp)
          .clip(CircleShape)
          .background(Orange200.copy(alpha = 0.5f))
          .align(Alignment.Center)
    )

    Box(
      modifier =
        Modifier.size(128.dp)
          .offset(x = 80.dp, y = 200.dp)
          .graphicsLayer {
            translationY = blobOffset2
            scaleX = blobScale2
            scaleY = blobScale2
          }
          .blur(24.dp)
          .clip(CircleShape)
          .background(Pink200.copy(alpha = 0.5f))
          .align(Alignment.Center)
    )

    Box(
      modifier =
        Modifier.size(64.dp)
          .offset(x = 60.dp, y = (-120).dp)
          .blur(24.dp)
          .clip(CircleShape)
          .background(Yellow200.copy(alpha = 0.5f))
          .align(Alignment.Center)
    )

    val scrollState = rememberScrollState()
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier =
        Modifier.fillMaxWidth()
          .verticalScroll(scrollState)
          .padding(horizontal = 32.dp)
          .graphicsLayer { alpha = contentAlpha },
    ) {
      Box(
        modifier =
          Modifier.shadow(
              elevation = 24.dp,
              shape = RoundedCornerShape(32.dp),
              ambientColor = Orange200.copy(alpha = 0.3f),
              spotColor = Orange200.copy(alpha = 0.3f),
            )
            .background(Color.White, RoundedCornerShape(32.dp))
            .size(120.dp),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          painter = painterResource(R.drawable.ic_book_open),
          contentDescription = null,
          tint = Orange400,
          modifier = Modifier.size(72.dp),
        )
      }

      Spacer(modifier = Modifier.height(24.dp))

      Text(
        text = stringResource(R.string.login_title),
        fontSize = 36.sp,
        fontWeight = FontWeight.Black,
        color = Orange900,
        letterSpacing = 2.sp,
      )

      Spacer(modifier = Modifier.height(48.dp))

      TextField(
        value = phone,
        onValueChange = { onPhoneChange(it.filter { c -> c.isDigit() }.take(11)) },
        placeholder = {
          Text(stringResource(R.string.login_phone_placeholder), color = Orange300, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        },
        leadingIcon = {
          Icon(
            painter = painterResource(R.drawable.ic_phone),
            contentDescription = null,
            tint = Orange300,
            modifier = Modifier.size(24.dp),
          )
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
        singleLine = true,
        shape = RoundedCornerShape(50),
        colors =
          TextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            cursorColor = Orange400,
            focusedTextColor = Orange900,
            unfocusedTextColor = Orange900,
          ),
        modifier =
          Modifier.fillMaxWidth()
            .shadow(
              elevation = 4.dp,
              shape = RoundedCornerShape(50),
              ambientColor = Orange200.copy(alpha = 0.3f),
            ),
      )

      Spacer(modifier = Modifier.height(16.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        TextField(
          value = code,
          onValueChange = { onCodeChange(it.filter { c -> c.isDigit() }.take(6)) },
          placeholder = {
            Text(stringResource(R.string.login_code_label), color = Orange300, fontWeight = FontWeight.Bold, fontSize = 16.sp)
          },
          leadingIcon = {
            Icon(
              painter = painterResource(R.drawable.ic_shield),
              contentDescription = null,
              tint = Orange300,
              modifier = Modifier.size(24.dp),
            )
          },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          singleLine = true,
          shape = RoundedCornerShape(50),
          colors =
            TextFieldDefaults.colors(
              focusedContainerColor = Color.White,
              unfocusedContainerColor = Color.White,
              focusedIndicatorColor = Color.Transparent,
              unfocusedIndicatorColor = Color.Transparent,
              cursorColor = Orange400,
              focusedTextColor = Orange900,
              unfocusedTextColor = Orange900,
            ),
          modifier =
            Modifier.weight(1f)
              .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(50),
                ambientColor = Orange200.copy(alpha = 0.3f),
              ),
        )

        Button(
          onClick = onSendCode,
          enabled = phone.length == 11 && countdown == 0,
          shape = RoundedCornerShape(16.dp),
          colors =
            ButtonDefaults.buttonColors(
              containerColor = Orange100,
              contentColor = Orange600,
              disabledContainerColor = Color(0xFFF3F3F5),
              disabledContentColor = Color(0xFFBBBBBB),
            ),
          modifier = Modifier.width(130.dp).height(56.dp),
        ) {
          Text(
            text = if (countdown > 0) stringResource(R.string.login_countdown, countdown) else stringResource(R.string.login_send_code),
            fontWeight = FontWeight.ExtraBold,
            fontSize = 14.sp,
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))
      if (!errorMessage.isNullOrBlank()) {
        Text(
          text = errorMessage,
          color = Color(0xFFC62828),
          fontSize = 14.sp,
          fontWeight = FontWeight.Bold,
          textAlign = TextAlign.Center,
          modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        )
        Spacer(modifier = Modifier.height(12.dp))
      }
      Spacer(modifier = Modifier.height(16.dp))

      Button(
        onClick = onLogin,
        enabled = !isLoading && phone.length == 11 && code.length == 6,
        shape = RoundedCornerShape(50),
        colors =
          ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            disabledContainerColor = Color(0xFFD1D5DB),
          ),
        modifier =
          Modifier.fillMaxWidth()
            .height(56.dp)
            .background(
              brush =
                if (!isLoading && phone.length == 11 && code.length == 6)
                  Brush.horizontalGradient(listOf(Orange400, Pink400))
                else Brush.horizontalGradient(listOf(Color(0xFFD1D5DB), Color(0xFFD1D5DB))),
              shape = RoundedCornerShape(50),
            ),
      ) {
        if (isLoading) {
          CircularProgressIndicator(
            modifier = Modifier.size(24.dp),
            color = Color.White,
            strokeWidth = 3.dp,
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = stringResource(R.string.login_entering),
            color = Color.White,
            fontWeight = FontWeight.Black,
            fontSize = 18.sp,
          )
        } else {
          Text(
            text = stringResource(R.string.login_start),
            color = Color.White,
            fontWeight = FontWeight.Black,
            fontSize = 18.sp,
          )
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
      ) {
        HorizontalDivider(
          modifier = Modifier.weight(1f),
          color = Orange300.copy(alpha = 0.3f),
        )
        Text(
          text = stringResource(R.string.login_or),
          fontSize = 13.sp,
          fontWeight = FontWeight.Bold,
          color = Orange900.copy(alpha = 0.4f),
        )
        HorizontalDivider(
          modifier = Modifier.weight(1f),
          color = Orange300.copy(alpha = 0.3f),
        )
      }

      Spacer(modifier = Modifier.height(24.dp))

      OutlinedButton(
        onClick = onGoogleSignIn,
        enabled = !isGoogleLoading && !isLoading,
        shape = RoundedCornerShape(50),
        border = BorderStroke(1.dp, Orange300.copy(alpha = 0.5f)),
        colors =
          ButtonDefaults.outlinedButtonColors(
            containerColor = Color.White,
            contentColor = Orange900,
          ),
        modifier = Modifier.fillMaxWidth().height(56.dp),
      ) {
        if (isGoogleLoading) {
          CircularProgressIndicator(
            modifier = Modifier.size(20.dp),
            color = Orange400,
            strokeWidth = 2.dp,
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = stringResource(R.string.login_signing_in),
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = Orange900,
          )
        } else {
          Text(
            text = "G",
            fontWeight = FontWeight.Black,
            fontSize = 20.sp,
            color = Orange400,
          )
          Spacer(modifier = Modifier.width(12.dp))
          Text(
            text = stringResource(R.string.login_google),
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = Orange900,
          )
        }
      }

      Spacer(modifier = Modifier.height(32.dp))

      val agreementPrefix = stringResource(R.string.login_agreement_prefix)
      val agreementTerms = stringResource(R.string.login_agreement_terms)
      val agreementAnd = stringResource(R.string.login_agreement_and)
      val agreementPrivacy = stringResource(R.string.login_agreement_privacy)
      val agreementText = buildAnnotatedString {
        withStyle(
          SpanStyle(
            color = Orange900.copy(alpha = 0.4f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
          )
        ) {
          append(agreementPrefix)
        }
        pushStringAnnotation(tag = "URL", annotation = "terms")
        withStyle(
          SpanStyle(color = Orange400, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        ) {
          append(agreementTerms)
        }
        pop()
        withStyle(
          SpanStyle(
            color = Orange900.copy(alpha = 0.4f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
          )
        ) {
          append(agreementAnd)
        }
        pushStringAnnotation(tag = "URL", annotation = "privacy")
        withStyle(
          SpanStyle(color = Orange400, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        ) {
          append(agreementPrivacy)
        }
        pop()
      }

      @Suppress("DEPRECATION")
      ClickableText(
        text = agreementText,
        style = androidx.compose.ui.text.TextStyle(textAlign = TextAlign.Center),
        onClick = { offset ->
          agreementText.getStringAnnotations(tag = "URL", start = offset, end = offset).firstOrNull()
            ?.let { annotation ->
              when (annotation.item) {
                "terms" ->
                  onNavigateToWebView(
                    agreementTerms,
                    "${BuildConfig.BASE_URL}term_of_service.html",
                  )
                "privacy" ->
                  onNavigateToWebView(
                    agreementPrivacy,
                    "${BuildConfig.BASE_URL}child_privacy.html",
                  )
              }
            }
        },
      )
    }
  }
}
