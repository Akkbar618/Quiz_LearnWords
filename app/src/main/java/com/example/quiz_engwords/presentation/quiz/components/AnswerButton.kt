package com.example.quiz_engwords.presentation.quiz.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.quiz_engwords.presentation.quiz.AnswerButtonState
import com.example.quiz_engwords.ui.theme.Success
import com.example.quiz_engwords.ui.theme.Error as ErrorColor

/**
 * Кнопка варианта ответа с тремя состояниями: NEUTRAL, CORRECT, WRONG.
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
    // Анимация цветов в зависимости от состояния
    val containerColor by animateColorAsState(
        targetValue = when (state) {
            AnswerButtonState.NEUTRAL -> MaterialTheme.colorScheme.surfaceVariant
            AnswerButtonState.CORRECT -> Success.copy(alpha = 0.2f)
            AnswerButtonState.WRONG -> ErrorColor.copy(alpha = 0.2f)
        },
        animationSpec = tween(300),
        label = "containerColor"
    )
    
    val borderColor by animateColorAsState(
        targetValue = when (state) {
            AnswerButtonState.NEUTRAL -> MaterialTheme.colorScheme.outline
            AnswerButtonState.CORRECT -> Success
            AnswerButtonState.WRONG -> ErrorColor
        },
        animationSpec = tween(300),
        label = "borderColor"
    )
    
    val badgeColor by animateColorAsState(
        targetValue = when (state) {
            AnswerButtonState.NEUTRAL -> MaterialTheme.colorScheme.onSurfaceVariant
            AnswerButtonState.CORRECT -> Success
            AnswerButtonState.WRONG -> ErrorColor
        },
        animationSpec = tween(300),
        label = "badgeColor"
    )
    
    val textColor by animateColorAsState(
        targetValue = when (state) {
            AnswerButtonState.NEUTRAL -> MaterialTheme.colorScheme.onSurfaceVariant
            AnswerButtonState.CORRECT -> Success
            AnswerButtonState.WRONG -> ErrorColor
        },
        animationSpec = tween(300),
        label = "textColor"
    )
    
    // Анимация масштаба при нажатии
    val scale by animateFloatAsState(
        targetValue = if (state != AnswerButtonState.NEUTRAL) 0.98f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )
    
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(70.dp)
            .scale(scale),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 2.dp,
            color = borderColor
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Номер варианта (бейдж)
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(12.dp),
                color = when (state) {
                    AnswerButtonState.NEUTRAL -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    AnswerButtonState.CORRECT -> Success
                    AnswerButtonState.WRONG -> ErrorColor
                }
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = numberText,
                        style = MaterialTheme.typography.labelLarge,
                        color = when (state) {
                            AnswerButtonState.NEUTRAL -> MaterialTheme.colorScheme.onSurfaceVariant
                            else -> Color.White
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Текст ответа
            Text(
                text = answerText,
                style = MaterialTheme.typography.bodyLarge,
                color = textColor,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
