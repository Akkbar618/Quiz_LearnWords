package com.example.quiz_engwords.presentation.quiz

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.quiz_engwords.presentation.quiz.components.*
import com.example.quiz_engwords.ui.theme.Success

/**
 * Главный экран викторины - Premium Edition.
 */
@Composable
fun QuizScreen(
    viewModel: QuizViewModel,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            QuizTopBar(
                onClose = onClose,
                onSkip = { viewModel.onEvent(QuizUiEvent.SkipQuestion) },
                showSkip = !uiState.isAnswerRevealed && !uiState.isQuizCompleted
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    LoadingState()
                }
                uiState.isQuizCompleted -> {
                    CompletionState(
                        correctAnswers = uiState.correctAnswersCount,
                        totalQuestions = uiState.questionsAnswered,
                        onRetry = { viewModel.onEvent(QuizUiEvent.RetryQuiz) }
                    )
                }
                uiState.error != null -> {
                    ErrorState(
                        errorMessage = uiState.error!!,
                        onRetry = { viewModel.onEvent(QuizUiEvent.NextQuestion) }
                    )
                }
                else -> {
                    QuizContent(
                        uiState = uiState,
                        onEvent = viewModel::onEvent
                    )
                }
            }
        }
    }
}

/**
 * Top App Bar для викторины.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuizTopBar(
    onClose: () -> Unit,
    onSkip: () -> Unit,
    showSkip: Boolean
) {
    TopAppBar(
        title = { 
            Text(
                text = "Тренировка",
                fontWeight = FontWeight.SemiBold
            )
        },
        navigationIcon = {
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Default.Close, 
                    contentDescription = "Закрыть",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        actions = {
            if (showSkip) {
                TextButton(
                    onClick = onSkip,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = "Пропустить",
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    )
}

/**
 * Основной контент викторины.
 */
@Composable
private fun QuizContent(
    uiState: QuizUiState,
    onEvent: (QuizUiEvent) -> Unit
) {
    val question = uiState.currentQuestion ?: return
    
    // Триггер для shake анимации
    var shouldShake by remember { mutableStateOf(false) }
    
    LaunchedEffect(uiState.isCorrectAnswer) {
        if (uiState.isCorrectAnswer == false) {
            shouldShake = true
            kotlinx.coroutines.delay(400)
            shouldShake = false
        }
    }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Прогресс-бар
        QuizProgressBar(
            progress = uiState.progressPercent,
            progressText = uiState.progressText
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Карточка с английским словом
        AnimatedWordCard(
            word = question.correctWord.original,
            visible = true,
            modifier = Modifier.shake(shouldShake)
        )
        
        Spacer(modifier = Modifier.height(40.dp))
        
        // Варианты ответов
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            question.variants.forEachIndexed { index, word ->
                val state = when {
                    !uiState.isAnswerRevealed -> AnswerButtonState.NEUTRAL
                    uiState.selectedAnswerIndex == index && uiState.isCorrectAnswer == true -> AnswerButtonState.CORRECT
                    uiState.selectedAnswerIndex == index && uiState.isCorrectAnswer == false -> AnswerButtonState.WRONG
                    index == question.getCorrectAnswerIndex() && uiState.isAnswerRevealed -> AnswerButtonState.CORRECT
                    else -> AnswerButtonState.NEUTRAL
                }
                
                AnswerButton(
                    numberText = "${index + 1}",
                    answerText = word.translate,
                    state = state,
                    onClick = {
                        if (!uiState.isAnswerRevealed) {
                            onEvent(QuizUiEvent.AnswerSelected(index))
                        }
                    }
                )
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Баннер результата
        AnimatedVisibility(
            visible = uiState.isAnswerRevealed,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(350, easing = FastOutSlowInEasing)
            ) + fadeIn(animationSpec = tween(300)),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(250)
            ) + fadeOut(animationSpec = tween(200))
        ) {
            ResultBanner(
                isCorrect = uiState.isCorrectAnswer ?: false,
                onContinue = { onEvent(QuizUiEvent.NextQuestion) }
            )
        }
    }
}

/**
 * Состояние загрузки с анимацией.
 */
@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(56.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 4.dp
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Text(
                text = "Подготовка вопросов...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Состояние завершения викторины.
 */
@Composable
private fun CompletionState(
    correctAnswers: Int,
    totalQuestions: Int,
    onRetry: () -> Unit
) {
    val percentage = if (totalQuestions > 0) (correctAnswers.toFloat() / totalQuestions * 100).toInt() else 0
    
    // Определяем цвета и эмодзи на основе результата
    val (emoji, message, gradientColors) = when {
        percentage >= 90 -> Triple("🏆", "Превосходно!", listOf(Color(0xFFFFD700), Color(0xFFFFA500)))
        percentage >= 70 -> Triple("🎉", "Отличный результат!", listOf(Success, Success.copy(alpha = 0.7f)))
        percentage >= 50 -> Triple("👍", "Хорошая работа!", listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary))
        else -> Triple("💪", "Продолжай практиковаться!", listOf(MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.primary))
    }
    
    // Анимация появления
    val infiniteTransition = rememberInfiniteTransition(label = "celebration")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "emoji_scale"
    )
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Эмодзи с анимацией
        Text(
            text = emoji,
            fontSize = 80.sp,
            modifier = Modifier.scale(scale)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Градиентный текст результата
        Text(
            text = message,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Карточка с результатом
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(24.dp))
                .background(
                    brush = Brush.linearGradient(colors = gradientColors)
                )
                .padding(32.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "$correctAnswers / $totalQuestions",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                
                Text(
                    text = "правильных ответов",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.9f)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "$percentage%",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // Кнопка повтора
        Button(
            onClick = onRetry,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(
                imageVector = Icons.Outlined.Refresh,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "НАЧАТЬ ЗАНОВО",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}

/**
 * Состояние ошибки.
 */
@Composable
private fun ErrorState(
    errorMessage: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "😕",
            fontSize = 72.sp
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Что-то пошло не так",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = errorMessage,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        OutlinedButton(
            onClick = onRetry,
            modifier = Modifier.height(52.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Refresh,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Попробовать снова",
                fontWeight = FontWeight.Medium
            )
        }
    }
}
