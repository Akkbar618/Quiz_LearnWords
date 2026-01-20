package com.example.quiz_engwords.presentation.quiz

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.quiz_engwords.di.RepositoryModule
import com.example.quiz_engwords.presentation.quiz.components.*
import androidx.compose.ui.unit.sp

/**
 * Главный экран викторины на Jetpack Compose.
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
        title = { Text("Quiz") },
        navigationIcon = {
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
        },
        actions = {
            if (showSkip) {
                TextButton(onClick = onSkip) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("SKIP")
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}

/**
 * Основной контент викторины с вопросом и вариантами ответов.
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
            kotlinx.coroutines.delay(300)
            shouldShake = false
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 16.dp)
    ) {
        // Прогресс-бар
        QuizProgressBar(
            progress = uiState.progressPercent,
            progressText = uiState.progressText
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Карточка с английским словом
        AnimatedWordCard(
            word = question.correctWord.original,
            visible = true,
            modifier = Modifier.shake(shouldShake)
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // Варианты ответов
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
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
                animationSpec = tween(300)
            ) + fadeIn(),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(300)
            ) + fadeOut()
        ) {
            ResultBanner(
                isCorrect = uiState.isCorrectAnswer ?: false,
                onContinue = { onEvent(QuizUiEvent.NextQuestion) }
            )
        }
    }
}

/**
 * Состояние загрузки.
 */
@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🎉",
            style = MaterialTheme.typography.displayLarge,
            fontSize = 80.sp
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Quiz Completed!",
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "You got $correctAnswers out of $totalQuestions correct!",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onRetry,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("START AGAIN")
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
        horizontalAlignment = Alignment.CenterHorizontally, // Changed from CenterHorizontal
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "😕",
            style = MaterialTheme.typography.displayLarge,
            fontSize = 64.sp
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = errorMessage,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.error
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(onClick = onRetry) {
            Text("RETRY")
        }
    }
}
