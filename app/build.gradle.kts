import java.io.FileInputStream
import java.util.Properties

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.ktfmt)
}

ktfmt {
  kotlinLangStyle()
  maxWidth.set(100)
  blockIndent.set(2)
  continuationIndent.set(2)
}

// 读取 keystore.properties；该文件不应提交到 git（已被 .gitignore 忽略）。
// 如果文件不存在，release 构建会自动 fallback 到 debug 签名，方便没有 keystore 的开发机也能跑通构建。
val keystorePropsFile = rootProject.file("keystore.properties")
val keystoreProps =
  Properties().apply { if (keystorePropsFile.exists()) load(FileInputStream(keystorePropsFile)) }

android {
  namespace = "com.hope.echo"
  compileSdk = 36

  defaultConfig {
    applicationId = "com.hope.echo"
    minSdk = 24
    targetSdk = 36
    versionCode = 1
    versionName = "1.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  signingConfigs {
    if (keystorePropsFile.exists()) {
      create("release") {
        storeFile = file(keystoreProps["storeFile"] as String)
        storePassword = keystoreProps["storePassword"] as String
        keyAlias = keystoreProps["keyAlias"] as String
        keyPassword = keystoreProps["keyPassword"] as String
      }
    }
  }

  buildTypes {
    debug {
      // 开发环境：连内网服务；模拟器访问本机可用 http://10.0.2.2:9999/
      buildConfigField(
        "String",
        "BASE_URL",
        //        "\"http://192.168.2.172:9999/\"",
        "\"http://192.168.10.203:8080/\"",
      )
      // 替换为 Google Cloud Console 中创建的 Web 类型 OAuth Client ID
      buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", "\"206725443191-3qoc0hil5prd1n6539ih1rqj8k01hhcs.apps.googleusercontent.com\"")
    }
    release {
      // 没配 keystore.properties 时回落到 debug 签名，仅用于本地构建验证；正式发版必须配 release 签名。
      signingConfig = signingConfigs.findByName("release") ?: signingConfigs.getByName("debug")
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      buildConfigField("String", "BASE_URL", "\"https://ipush.chat/\"")
      buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", "\"206725443191-ouif7n3p6i0fhuufvu0mhr2av7omteri.apps.googleusercontent.com\"")
    }
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }

  kotlinOptions { jvmTarget = "11" }

  buildFeatures {
    compose = true
    buildConfig = true
  }
}

dependencies {
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.activity.compose)
  implementation(platform(libs.androidx.compose.bom))
  implementation(libs.androidx.ui)
  implementation(libs.androidx.ui.graphics)
  implementation(libs.androidx.ui.tooling.preview)
  implementation(libs.androidx.material3)
  implementation(libs.androidx.material.icons.extended)
  implementation(libs.androidx.navigation.compose)
  implementation(libs.androidx.splashscreen)
  implementation(libs.androidx.datastore.preferences)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.retrofit)
  implementation(libs.retrofit.converter.gson)
  implementation(libs.okhttp.logging)
  implementation(libs.gson)
  implementation(libs.coil.compose)
  implementation(libs.camera.core)
  implementation(libs.camera.camera2)
  implementation(libs.camera.lifecycle)
  implementation(libs.camera.view)
  implementation(libs.androidx.exifinterface)
  implementation(libs.androidx.credentials)
  implementation(libs.androidx.credentials.play.services)
  implementation(libs.googleid)
  testImplementation(libs.junit)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.ui.test.junit4)
  debugImplementation(libs.androidx.ui.tooling)
  debugImplementation(libs.androidx.ui.test.manifest)
}
