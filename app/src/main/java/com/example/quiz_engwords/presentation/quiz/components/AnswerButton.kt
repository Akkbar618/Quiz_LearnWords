package com.example.quiz_engwords.presentation.quiz.components

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.quiz_engwords.presentation.quiz.AnswerButtonState
import com.example.quiz_engwords.ui.theme.Success
import com.example.quiz_engwords.ui.theme.Error as ErrorColor

/**
 * Премиум кнопка варианта ответа с анимациями и градиентами.
 * 
 * @param numberText номер варианта ("1", "2", "3", "4")
 * @param answerText текст ответа (перевод слова)
 * @param state текущее состояние кнопки
 * @param onClick обработчик клика
 * @param modifier модификатор
 */
@Composable
fun AnswerButton(
    numberText: String,
    answerText: String,
    state: AnswerButtonState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Анимация масштаба
    val scale by animateFloatAsState(
        targetValue = when (state) {
            AnswerButtonState.NEUTRAL -> 1f
            AnswerButtonState.CORRECT -> 1.02f
            AnswerButtonState.WRONG -> 0.98f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "scale"
    )
    
    // Анимация цветов
    val containerColor by animateColorAsState(
        targetValue = when (state) {
            AnswerButtonState.NEUTRAL -> MaterialTheme.colorScheme.surface
            AnswerButtonState.CORRECT -> Success.copy(alpha = 0.12f)
            AnswerButtonState.WRONG -> ErrorColor.copy(alpha = 0.12f)
        },
        animationSpec = tween(300),
        label = "containerColor"
    )
    
    val borderColor by animateColorAsState(
        targetValue = when (state) {
            AnswerButtonState.NEUTRAL -> MaterialTheme.colorScheme.outlineVariant
            AnswerButtonState.CORRECT -> Success
            AnswerButtonState.WRONG -> ErrorColor
        },
        animationSpec = tween(300),
        label = "borderColor"
    )
    
    val textColor by animateColorAsState(
        targetValue = when (state) {
            AnswerButtonState.NEUTRAL -> MaterialTheme.colorScheme.onSurface
            AnswerButtonState.CORRECT -> Success
            AnswerButtonState.WRONG -> ErrorColor
        },
        animationSpec = tween(300),
        label = "textColor"
    )
    
    val badgeGradient = when (state) {
        AnswerButtonState.NEUTRAL -> listOf(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
            MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
        )
        AnswerButtonState.CORRECT -> listOf(Success, Success.copy(alpha = 0.8f))
        AnswerButtonState.WRONG -> listOf(ErrorColor, ErrorColor.copy(alpha = 0.8f))
    }
    
    val elevation = when (state) {
        AnswerButtonState.NEUTRAL -> 2.dp
        AnswerButtonState.CORRECT -> 8.dp
        AnswerButtonState.WRONG -> 0.dp
    }

    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp)
            .scale(scale)
            .shadow(
                elevation = elevation,
                shape = RoundedCornerShape(18.dp),
                ambientColor = borderColor.copy(alpha = 0.3f),
                spotColor = borderColor.copy(alpha = 0.3f)
            ),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = if (state == AnswerButtonState.NEUTRAL) 1.5.dp else 2.5.dp,
            color = borderColor
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Номер варианта с градиентом
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        brush = Brush.linearGradient(colors = badgeGradient)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = numberText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = when (state) {
                        AnswerButtonState.NEUTRAL -> MaterialTheme.colorScheme.primary
                        else -> Color.White
                    }
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Текст ответа
            Text(
                text = answerText,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (state != AnswerButtonState.NEUTRAL) FontWeight.SemiBold else FontWeight.Normal,
                color = textColor,
                modifier = Modifier.weight(1f)
            )
            
            // Индикатор результата
            if (state != AnswerButtonState.NEUTRAL) {
                Spacer(modifier = Modifier.width(12.dp))
                
                val icon = when (state) {
                    AnswerButtonState.CORRECT -> "✓"
                    AnswerButtonState.WRONG -> "✗"
                    else -> ""
                }
                
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            when (state) {
                                AnswerButtonState.CORRECT -> Success
                                else -> ErrorColor
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = icon,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
