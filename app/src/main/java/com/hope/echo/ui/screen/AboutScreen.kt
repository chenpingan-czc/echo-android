package com.hope.echo.ui.screen

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ShieldMoon
import androidx.compose.material.icons.outlined.Castle
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hope.echo.BuildConfig
import com.hope.echo.R
import com.hope.echo.ui.theme.Blue500
import com.hope.echo.ui.theme.Gray300
import com.hope.echo.ui.theme.Green500
import com.hope.echo.ui.theme.Orange100
import com.hope.echo.ui.theme.Orange200
import com.hope.echo.ui.theme.Orange400
import com.hope.echo.ui.theme.Orange500
import com.hope.echo.ui.theme.Orange600
import com.hope.echo.ui.theme.Orange900

private val AboutBackground = Color(0xFFFDF8F5)
private val BlueIconBg = Color(0xFFEFF6FF)
private val GreenIconBg = Color(0xFFF0FDF4)

@Composable
fun AboutScreen(
  onBack: () -> Unit = {},
  onNavigateToWebView: (String, String) -> Unit = { _, _ -> },
) {
  Column(
    modifier =
      Modifier
        .fillMaxSize()
        .background(AboutBackground)
        .statusBarsPadding()
  ) {
    AboutHeader(onBack = onBack)

    AppInfoSection()

    Spacer(modifier = Modifier.height(16.dp))

    PolicyLinksSection(onNavigateToWebView = onNavigateToWebView)

    Spacer(modifier = Modifier.weight(1f))

    CopyrightFooter()
  }
}

@Composable
private fun AboutHeader(onBack: () -> Unit) {
  Box(
    modifier =
      Modifier
        .fillMaxWidth()
        .padding(horizontal = 24.dp)
        .padding(top = 12.dp),
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
        modifier = Modifier.size(20.dp),
      )
    }

    Text(
      text = stringResource(R.string.about_title),
      fontSize = 18.sp,
      fontWeight = FontWeight.ExtraBold,
      color = Orange900,
      modifier = Modifier.align(Alignment.Center),
    )
  }
}

@Composable
private fun AppInfoSection() {
  Column(
    modifier =
      Modifier
        .fillMaxWidth()
        .padding(top = 48.dp, bottom = 64.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Box(
      modifier =
        Modifier
          .size(112.dp)
          .shadow(
            elevation = 24.dp,
            shape = RoundedCornerShape(32.dp),
            ambientColor = Orange500.copy(alpha = 0.3f),
          )
          .border(4.dp, Color.White, RoundedCornerShape(32.dp))
          .background(
            brush = Brush.linearGradient(listOf(Orange500, Orange400)),
            shape = RoundedCornerShape(32.dp),
          ),
      contentAlignment = Alignment.Center,
    ) {
      Icon(
        imageVector = Icons.Outlined.Castle,
        contentDescription = stringResource(R.string.about_logo_cd),
        tint = Color.White,
        modifier = Modifier.size(56.dp),
      )
    }

    Spacer(modifier = Modifier.height(24.dp))

    Text(
      text = stringResource(R.string.about_app_name),
      fontSize = 30.sp,
      fontWeight = FontWeight.Black,
      color = Orange900,
      letterSpacing = 2.sp,
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
      text = stringResource(R.string.about_version, BuildConfig.VERSION_NAME),
      fontSize = 14.sp,
      fontWeight = FontWeight.Bold,
      color = Orange600,
      modifier =
        Modifier
          .background(Orange100, RoundedCornerShape(50))
          .border(2.dp, Orange200, RoundedCornerShape(50))
          .padding(horizontal = 16.dp, vertical = 6.dp),
    )
  }
}

@Composable
private fun PolicyLinksSection(
  onNavigateToWebView: (String, String) -> Unit,
) {
  val termsTitle = stringResource(R.string.about_terms_title)
  val privacyTitle = stringResource(R.string.about_privacy_title)
  Column(
    modifier = Modifier.padding(horizontal = 24.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    PolicyLinkItem(
      icon = Icons.Filled.Description,
      iconTint = Blue500,
      iconBgColor = BlueIconBg,
      title = termsTitle,
      onClick = {
        onNavigateToWebView(
          termsTitle,
          "${BuildConfig.BASE_URL}term_of_service.html",
        )
      },
    )

    PolicyLinkItem(
      icon = Icons.Filled.ShieldMoon,
      iconTint = Green500,
      iconBgColor = GreenIconBg,
      title = privacyTitle,
      onClick = {
        onNavigateToWebView(
          privacyTitle,
          "${BuildConfig.BASE_URL}child_privacy.html",
        )
      },
    )
  }
}

@Composable
private fun PolicyLinkItem(
  icon: ImageVector,
  iconTint: Color,
  iconBgColor: Color,
  title: String,
  onClick: () -> Unit,
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
        .padding(20.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Box(
        modifier =
          Modifier
            .size(48.dp)
            .background(iconBgColor, RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = icon,
          contentDescription = title,
          tint = iconTint,
          modifier = Modifier.size(24.dp),
        )
      }
      Spacer(modifier = Modifier.width(16.dp))
      Text(
        text = title,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = Orange900,
      )
    }

    Icon(
      imageVector = Icons.Filled.ChevronRight,
      contentDescription = null,
      tint = Gray300,
      modifier = Modifier.size(24.dp),
    )
  }
}

@Composable
private fun CopyrightFooter() {
  Column(
    modifier =
      Modifier
        .fillMaxWidth()
        .padding(bottom = 48.dp, top = 32.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Text(
      text = stringResource(R.string.about_copyright),
      fontSize = 12.sp,
      fontWeight = FontWeight.Bold,
      color = Orange900.copy(alpha = 0.3f),
    )
    Text(
      text = stringResource(R.string.about_made_with_love),
      fontSize = 12.sp,
      fontWeight = FontWeight.Bold,
      color = Orange900.copy(alpha = 0.3f),
    )
  }
}
