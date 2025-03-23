/*
 * Copyright (C) 2023-2025 The risingOS Android Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.settings.utils

import android.os.Bundle
import android.util.DisplayMetrics
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.android.settings.R
import kotlin.math.roundToInt
import kotlin.random.Random
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PlatLogoActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
    super.onCreate(savedInstanceState)

    val displayMetrics = DisplayMetrics()
    windowManager.defaultDisplay.getMetrics(displayMetrics)
    val screenWidth = displayMetrics.widthPixels.toFloat()
    val screenHeight = displayMetrics.heightPixels.toFloat()

    setContent { MaterialTheme { RocketScreen(screenHeight, screenWidth) } }
  }
}

@Composable
fun RocketScreen(screenHeight: Float, screenWidth: Float) {
  val context = LocalContext.current
  val rocketY = remember { Animatable(0f) }
  val isHolding = remember { mutableStateOf(false) }
  val coroutineScope = rememberCoroutineScope()
  val rocketScale = remember { Animatable(0.5f) }
  val starPositions = remember { mutableStateListOf<StarData>() }

  val showLogo = remember { mutableStateOf(false) }
  val logoScale = remember { Animatable(0f) }
  val logoAlpha = remember { Animatable(0f) }

  val rocketVisible = remember { mutableStateOf(true) }
  val guideNotified = remember { mutableStateOf(false) }

  LaunchedEffect(true) { generateNewStars(starPositions, 200, screenWidth, screenHeight) }

  LaunchedEffect(showLogo.value) {
    if (showLogo.value) {
      while (true) {
        generateNewStars(starPositions, 200, screenWidth, screenHeight)
        kotlinx.coroutines.delay(10)
      }
    }
  }

  Box(
    modifier =
      Modifier.fillMaxSize().background(Color.Black).pointerInput(Unit) {
        detectTapGestures(
          onPress = {
            isHolding.value = true
            coroutineScope.launch {
              rocketScale.animateTo(0.65f, animationSpec = tween(100))
              while (isHolding.value) {
                rocketY.animateTo(rocketY.value - 10f, animationSpec = tween(16))
                generateNewStars(starPositions, 200, screenWidth, screenHeight)
                if (rocketY.value <= ((screenHeight * 0.9f) * -1)) {
                  if (rocketVisible.value) {
                    rocketVisible.value = false
                    showLogo.value = true
                    coroutineScope.launch {
                      rocketScale.animateTo(0f, animationSpec = tween(500))
                      logoAlpha.animateTo(1f, animationSpec = tween(500))
                      logoScale.animateTo(1f, animationSpec = tween(500))
                    }
                  }
                }
              }
            }
          },
          onTap = {
            isHolding.value = false
            coroutineScope.launch { rocketScale.animateTo(0.5f, animationSpec = tween(100)) }
          },
        )
      }
  ) {
    SpaceStars(starPositions)
    if (!guideNotified.value) {
      Toast.makeText(context, "Press and hold to launch the rocket!", Toast.LENGTH_SHORT).show()
      guideNotified.value = true
    }

    if (rocketVisible.value) {
      Rocket(
        rocketY = rocketY.value,
        rocketScale = rocketScale.value,
        isHolding = isHolding,
        screenHeight = screenHeight,
        onFall = { currentY ->
          coroutineScope.launch {
            val targetY = screenHeight - 100f
            if (currentY < targetY + 50f && !isHolding.value) {
              rocketY.animateTo(targetY, animationSpec = tween(500))
            }
          }
        },
      )
    }

    if (showLogo.value) {
      Image(
        painter = painterResource(id = R.drawable.risingOS_logo),
        contentDescription = "Rising Logo",
        modifier =
          Modifier.graphicsLayer(
              scaleX = logoScale.value,
              scaleY = logoScale.value,
              alpha = logoAlpha.value,
            )
            .align(Alignment.Center),
      )
    }
  }
}

@Composable
fun SpaceStars(stars: MutableList<StarData>) {
  Box(modifier = Modifier.fillMaxSize()) { stars.forEach { star -> Star(dot = star) } }
}

@Composable
fun Rocket(
  rocketY: Float,
  rocketScale: Float,
  isHolding: MutableState<Boolean>,
  screenHeight: Float,
  onFall: (Float) -> Unit,
) {
  LaunchedEffect(key1 = rocketY, key2 = isHolding.value) {
    if (!isHolding.value && rocketY < screenHeight) {
      onFall(rocketY)
    }
  }

  Box(modifier = Modifier.fillMaxSize()) { RocketImage(rocketY, rocketScale) }
}

fun generateNewStars(
  stars: MutableList<StarData>,
  count: Int,
  screenWidth: Float,
  screenHeight: Float,
) {
  stars.clear()
  repeat(count) { stars.add(generateRandomStar(screenWidth, screenHeight)) }
}

fun generateRandomStar(screenWidth: Float, screenHeight: Float): StarData {
  val randomX = (0..screenWidth.toInt()).random()
  val randomY = (0..screenHeight.toInt()).random()
  val randomSize = (1..3).random()
  val randomOpacity = Random.nextFloat() * (1.0f - 0.1f) + 0.1f
  return StarData(randomX, randomY, randomSize, randomOpacity)
}

@Composable
fun Star(dot: StarData) {
  Box(
    modifier =
      Modifier.offset { IntOffset(dot.x, dot.y) }
        .size(dot.size.dp)
        .background(Color.White.copy(alpha = dot.opacity), shape = CircleShape)
  )
}

data class StarData(var x: Int, var y: Int, val size: Int, val opacity: Float)

@Composable
fun RocketImage(rocketY: Float, rocketScale: Float) {
  Box(
    modifier =
      Modifier.fillMaxSize().graphicsLayer(scaleX = rocketScale, scaleY = rocketScale).offset {
        IntOffset(0, rocketY.roundToInt())
      },
    contentAlignment = Alignment.Center,
  ) {
    Image(
      painter = painterResource(id = R.drawable.rocket),
      contentDescription = "Rocket",
      modifier = Modifier.fillMaxSize(),
    )
  }
}
