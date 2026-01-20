package com.example.quiz_engwords.presentation.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.quiz_engwords.data.repository.WordRepository
import com.example.quiz_engwords.ui.components.*
import com.example.quiz_engwords.ui.theme.*

/**
 * Главный экран приложения (Home Screen) - Premium Edition.
 * 
 * Показывает статистику с градиентными карточками, streak и кнопку начала тренировки.
 */
@Composable
fun HomeScreen(
    onStartQuiz: () -> Unit,
    repository: WordRepository,
    modifier: Modifier = Modifier
) {
    val viewModel: HomeViewModel = viewModel {
        HomeViewModel(repository)
    }
    
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isDarkTheme = isSystemInDarkTheme()
    
    val scrollState = rememberScrollState()
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        
        // Приветствие
        WelcomeSection()
        
        Spacer(modifier = Modifier.height(32.dp))
        
        if (uiState.isLoading) {
            // Skeleton loading
            CardSkeleton()
        } else {
            // Статистика с градиентами
            uiState.statistics?.let { stats ->
                StatisticsSection(
                    totalWords = stats.totalWords,
                    learnedWords = stats.learnedWords,
                    progress = stats.progressPercentage / 100f,
                    streak = uiState.streak,
                    wordsToday = uiState.wordsLearnedToday,
                    isDarkTheme = isDarkTheme
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Мотивационная подсказка
        MotivationalTip()
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Кнопка начала тренировки
        StartTrainingButton(onClick = onStartQuiz)
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

/**
 * Секция приветствия.
 */
@Composable
private fun WelcomeSection() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Логотип/эмодзи
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.tertiary
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "📚",
                fontSize = 40.sp
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Quiz English Words",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = "Учи слова играя",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Секция статистики.
 */
@Composable
private fun StatisticsSection(
    totalWords: Int,
    learnedWords: Int,
    progress: Float,
    streak: Int,
    wordsToday: Int,
    isDarkTheme: Boolean
) {
    val progressGradient = if (isDarkTheme) {
        listOf(DarkCardProgressGradientStart, DarkCardProgressGradientEnd)
    } else {
        listOf(CardProgressGradientStart, CardProgressGradientEnd)
    }
    
    val streakGradient = if (isDarkTheme) {
        listOf(DarkCardStreakGradientStart, DarkCardStreakGradientEnd)
    } else {
        listOf(CardStreakGradientStart, CardStreakGradientEnd)
    }
    
    val todayGradient = if (isDarkTheme) {
        listOf(DarkCardTodayGradientStart, DarkCardTodayGradientEnd)
    } else {
        listOf(CardTodayGradientStart, CardTodayGradientEnd)
    }
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Карточка прогресса (большая)
        ProgressCard(
            learnedWords = learnedWords,
            totalWords = totalWords,
            progress = progress,
            gradientColors = progressGradient
        )
        
        // Streak и Today (маленькие)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatCard(
                modifier = Modifier.weight(1f),
                icon = {
                    PulsingIcon(
                        icon = Icons.Default.LocalFireDepartment,
                        contentDescription = "Streak",
                        tint = Color.White
                    )
                },
                value = "$streak",
                label = "Day Streak",
                gradientColors = streakGradient
            )
            
            StatCard(
                modifier = Modifier.weight(1f),
                icon = {
                    Icon(
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = "Today",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                },
                value = "$wordsToday",
                label = "Сегодня",
                gradientColors = todayGradient
            )
        }
    }
}

/**
 * Карточка прогресса обучения.
 */
@Composable
private fun ProgressCard(
    learnedWords: Int,
    totalWords: Int,
    progress: Float,
    gradientColors: List<Color>
) {
    GradientCard(
        modifier = Modifier.fillMaxWidth(),
        gradientColors = gradientColors
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Прогресс обучения",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                
                // Процент
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Прогресс-бар
            GradientProgressBar(
                progress = progress,
                gradientColors = listOf(Color.White, Color.White.copy(alpha = 0.8f)),
                trackColor = Color.White.copy(alpha = 0.25f),
                height = 14.dp
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Статистика
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "$learnedWords",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "изучено",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "$totalWords",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "всего слов",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

/**
 * Мотивационная подсказка.
 */
@Composable
private fun MotivationalTip() {
    val tips = listOf(
        "💡 Занимайся каждый день по 5 минут",
        "🎯 Маленькие шаги ведут к большим результатам",
        "🔥 Не прерывай свой streak!",
        "📈 Регулярность важнее длительности"
    )
    
    val tipIndex = remember { (0 until tips.size).random() }
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Lightbulb,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Text(
                text = tips[tipIndex],
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Кнопка начала тренировки.
 */
@Composable
private fun StartTrainingButton(onClick: () -> Unit) {
    // Анимация пульсации
    val infiniteTransition = rememberInfiniteTransition(label = "button_pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    GradientButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .scale(scale)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
            ),
        gradientColors = listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.tertiary
        )
    ) {
        Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(28.dp)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Text(
            text = "НАЧАТЬ ТРЕНИРОВКУ",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            fontSize = 18.sp
        )
    }
}
