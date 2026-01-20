package com.example.quiz_engwords.presentation.quiz.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Премиум карточка с английским словом для викторины.
 * 
 * @param word английское слово
 * @param modifier модификатор
 */
@Composable
fun WordCard(
    word: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(28.dp),
                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            )
            .clip(RoundedCornerShape(28.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f)
                    )
                )
            )
    ) {
        // Декоративные круги на фоне
        Box(
            modifier = Modifier
                .size(100.dp)
                .offset(x = (-30).dp, y = (-30).dp)
                .clip(RoundedCornerShape(50))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
        )
        
        Box(
            modifier = Modifier
                .size(60.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 20.dp, y = 20.dp)
                .clip(RoundedCornerShape(50))
                .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f))
        )
        
        // Основной контент
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 48.dp, horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Метка
            Surface(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Translate this word",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Слово
            Text(
                text = word,
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign = TextAlign.Center,
                lineHeight = 56.sp
            )
        }
    }
}

/**
 * WordCard с анимацией появления.
 */
@Composable
fun AnimatedWordCard(
    word: String,
    visible: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(400)) +
                scaleIn(
                    initialScale = 0.92f,
                    animationSpec = tween(400, easing = FastOutSlowInEasing)
                ) +
                slideInVertically(
                    initialOffsetY = { -30 },
                    animationSpec = tween(400, easing = FastOutSlowInEasing)
                ),
        exit = fadeOut(animationSpec = tween(200)) +
               scaleOut(targetScale = 0.95f)
    ) {
        WordCard(word = word, modifier = modifier)
    }
}

/**
 * Shake animation modifier для эффекта тряски при ошибке.
 */
@Composable
fun Modifier.shake(trigger: Boolean): Modifier {
    val offsetX by animateFloatAsState(
        targetValue = if (trigger) 1f else 0f,
        animationSpec = if (trigger) {
            keyframes {
                durationMillis = 400
                0f at 0
                -15f at 50
                15f at 100
                -12f at 150
                12f at 200
                -8f at 250
                8f at 300
                -4f at 350
                0f at 400
            }
        } else {
            snap()
        },
        label = "shake"
    )
    
    return this.offset(x = offsetX.dp)
}
