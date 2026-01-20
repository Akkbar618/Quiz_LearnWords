package com.example.quiz_engwords.presentation.settings

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.quiz_engwords.data.repository.WordRepository

/**
 * Экран настроек - Premium Edition.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    repository: WordRepository,
    onExit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val viewModel: SettingsViewModel = viewModel {
        SettingsViewModel(
            repository = repository,
            wordDao = com.example.quiz_engwords.di.AppModule.provideWordDao(context),
            contentResolver = context.contentResolver
        )
    }
    
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Export launcher - отправляет Uri в ViewModel
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let {
            viewModel.onEvent(SettingsEvent.ExportToUri(it))
        }
    }
    
    // Import launcher - отправляет Uri в ViewModel
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            viewModel.onEvent(SettingsEvent.ImportFromUri(it))
        }
    }
    
    // Show snackbar for messages
    LaunchedEffect(uiState.exportMessage, uiState.importMessage, uiState.error) {
        val message = uiState.exportMessage ?: uiState.importMessage ?: uiState.error
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onEvent(SettingsEvent.ClearMessages)
        }
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = { 
                        Text(
                            text = "Настройки",
                            fontWeight = FontWeight.Bold
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            }
        ) { paddingValues ->
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // App Info Header
                AppInfoCard()
                
                // Appearance section
                SettingsSection(title = "Внешний вид") {
                    SettingsRow(
                        icon = Icons.Outlined.DarkMode,
                        title = "Тёмная тема",
                        subtitle = "Согласно системным настройкам",
                        enabled = !uiState.isLoading,
                        trailing = {
                            Switch(
                                checked = uiState.isDarkTheme,
                                onCheckedChange = {
                                    viewModel.onEvent(SettingsEvent.ThemeChanged(it))
                                },
                                enabled = !uiState.isLoading
                            )
                        }
                    )
                }
                
                // Data Management section
                SettingsSection(title = "Управление данными") {
                    SettingsRow(
                        icon = Icons.Outlined.FileUpload,
                        title = "Экспорт словаря",
                        subtitle = if (uiState.isLoading) "Выполняется..." else "Сохранить в JSON файл",
                        enabled = !uiState.isLoading,
                        isLoading = uiState.isLoading,
                        onClick = {
                            exportLauncher.launch("quiz_words_${System.currentTimeMillis()}.json")
                        }
                    )
                    
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 56.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                    
                    SettingsRow(
                        icon = Icons.Outlined.FileDownload,
                        title = "Импорт словаря",
                        subtitle = if (uiState.isLoading) "Выполняется..." else "Загрузить из JSON файла",
                        enabled = !uiState.isLoading,
                        isLoading = uiState.isLoading,
                        onClick = {
                            importLauncher.launch(arrayOf("application/json"))
                        }
                    )
                    
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 56.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                    
                    SettingsRow(
                        icon = Icons.Outlined.Refresh,
                        title = "Сбросить прогресс",
                        subtitle = "Начать обучение заново",
                        textColor = MaterialTheme.colorScheme.error,
                        enabled = !uiState.isLoading,
                        onClick = { /* TODO: Add confirmation dialog */ }
                    )
                }
            
            // About section
            SettingsSection(title = "О приложении") {
                SettingsRow(
                    icon = Icons.Outlined.Info,
                    title = "Версия",
                    subtitle = "1.0.0"
                )
                
                HorizontalDivider(
                    modifier = Modifier.padding(start = 56.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                
                SettingsRow(
                    icon = Icons.Outlined.Code,
                    title = "Исходный код",
                    subtitle = "GitHub",
                    onClick = { /* TODO: Open GitHub */ }
                )
                
                HorizontalDivider(
                    modifier = Modifier.padding(start = 56.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                
                SettingsRow(
                    icon = Icons.Outlined.Star,
                    title = "Оценить приложение",
                    subtitle = "Поставьте оценку в магазине",
                    onClick = { /* TODO: Open store */ }
                )
            }
            
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
        
        // Loading overlay
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier.padding(24.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                        Text(
                            text = "Обработка...",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }
}

/**
 * Карточка с информацией о приложении.
 */
@Composable
private fun AppInfoCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.tertiary
                    )
                )
            )
            .padding(24.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App Icon
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "📚",
                    fontSize = 32.sp
                )
            }
            
            Spacer(modifier = Modifier.width(20.dp))
            
            Column {
                Text(
                    text = "Quiz English Words",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "Учи английские слова играя",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
        }
    }
}

/**
 * Секция настроек.
 */
@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
        )
        
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp
        ) {
            Column(
                modifier = Modifier.padding(vertical = 8.dp),
                content = content
            )
        }
    }
}

/**
 * Строка настройки с поддержкой loading state.
 */
@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    trailing: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    val alpha = if (enabled) 1f else 0.5f
    
    Surface(
        onClick = onClick ?: {},
        enabled = onClick != null && enabled,
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = alpha)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = (if (textColor == MaterialTheme.colorScheme.error) 
                        textColor else MaterialTheme.colorScheme.onSurfaceVariant).copy(alpha = alpha),
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Text
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = textColor.copy(alpha = alpha)
                )
                
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = alpha)
                )
            }
            
            // Trailing
            if (trailing != null) {
                trailing()
            } else if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
            } else if (onClick != null && enabled) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
