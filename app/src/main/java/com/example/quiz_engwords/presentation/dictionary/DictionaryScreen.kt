package com.example.quiz_engwords.presentation.dictionary

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.quiz_engwords.data.repository.WordRepository
import com.example.quiz_engwords.domain.model.Word

/**
 * Экран словаря с поиском и фильтрацией.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DictionaryScreen(
    repository: WordRepository,
    modifier: Modifier = Modifier
) {
    val viewModel: DictionaryViewModel = viewModel {
        DictionaryViewModel(repository)
    }
    
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Dictionary") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.onEvent(DictionaryEvent.ShowAddDialog) }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Word")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search bar
            SearchBar(
                query = uiState.searchQuery,
                onQueryChange = { viewModel.onEvent(DictionaryEvent.SearchQueryChanged(it)) }
            )
            
            // Filters
            FiltersRow(
                selectedCategory = uiState.selectedCategory,
                selectedDifficulty = uiState.selectedDifficulty,
                categories = uiState.categories.map { it.name },
                onCategorySelected = { viewModel.onEvent(DictionaryEvent.CategoryFilterChanged(it)) },
                onDifficultySelected = { viewModel.onEvent(DictionaryEvent.DifficultyFilterChanged(it)) }
            )
            
            // Words list
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                uiState.filteredWords.isEmpty() -> {
                    EmptyState()
                }
                else -> {
                    WordsList(
                        words = uiState.filteredWords,
                        onDeleteWord = { viewModel.onEvent(DictionaryEvent.DeleteWord(it)) },
                        onResetProgress = { viewModel.onEvent(DictionaryEvent.ResetWordProgress(it)) }
                    )
                }
            }
        }
        
        // Add Word Dialog
        if (uiState.showAddDialog) {
            AddWordDialog(
                categories = uiState.categories.map { it.name },
                onDismiss = { viewModel.onEvent(DictionaryEvent.HideAddDialog) },
                onConfirm = { original, translate, category ->
                    viewModel.onEvent(DictionaryEvent.AddWord(original, translate, category))
                }
            )
        }
    }
}

/**
 * Search bar для поиска слов.
 */
@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        placeholder = { Text("Search words...") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(24.dp)
    )
}

/**
 * Фильтры по категории и сложности.
 */
@Composable
private fun FiltersRow(
    selectedCategory: String?,
    selectedDifficulty: Int?,
    categories: List<String>,
    onCategorySelected: (String?) -> Unit,
    onDifficultySelected: (Int?) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Category filter
        var showCategoryMenu by remember { mutableStateOf(false) }
        
        FilterChip(
            selected = selectedCategory != null,
            onClick = { showCategoryMenu = true },
            label = { Text(selectedCategory ?: "Category") },
            leadingIcon = if (selectedCategory != null) {
                { Icon(Icons.Default.Check, contentDescription = null, Modifier.size(16.dp)) }
            } else null
        )
        
        DropdownMenu(
            expanded = showCategoryMenu,
            onDismissRequest = { showCategoryMenu = false }
        ) {
            DropdownMenuItem(
                text = { Text("All") },
                onClick = {
                    onCategorySelected(null)
                    showCategoryMenu = false
                }
            )
            categories.forEach { category ->
                DropdownMenuItem(
                    text = { Text(category) },
                    onClick = {
                        onCategorySelected(category)
                        showCategoryMenu = false
                    }
                )
            }
        }
        
        // Difficulty filter
        var showDifficultyMenu by remember { mutableStateOf(false) }
        
        FilterChip(
            selected = selectedDifficulty != null,
            onClick = { showDifficultyMenu = true },
            label = { Text(selectedDifficulty?.let { "Level $it" } ?: "Level") },
            leadingIcon = if (selectedDifficulty != null) {
                { Icon(Icons.Default.Check, contentDescription = null, Modifier.size(16.dp)) }
            } else null
        )
        
        DropdownMenu(
            expanded = showDifficultyMenu,
            onDismissRequest = { showDifficultyMenu = false }
        ) {
            DropdownMenuItem(
                text = { Text("All Levels") },
                onClick = {
                    onDifficultySelected(null)
                    showDifficultyMenu = false
                }
            )
            (0..5).forEach { level ->
                DropdownMenuItem(
                    text = { Text("Level $level") },
                    onClick = {
                        onDifficultySelected(level)
                        showDifficultyMenu = false
                    }
                )
            }
        }
    }
}

/**
 * Список слов.
 */
@Composable
private fun WordsList(
    words: List<Word>,
    onDeleteWord: (Long) -> Unit,
    onResetProgress: (Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(words, key = { it.id }) { word ->
            WordItem(
                word = word,
                onDelete = { onDeleteWord(word.id) },
                onResetProgress = { onResetProgress(word.id) }
            )
        }
    }
}

/**
 * Элемент списка слов.
 */
@Composable
private fun WordItem(
    word: Word,
    onDelete: () -> Unit,
    onResetProgress: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = { showMenu = true }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = word.original,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = word.translate,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AssistChip(
                        onClick = {},
                        label = { Text(word.category, style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.height(24.dp)
                    )
                    AssistChip(
                        onClick = {},
                        label = { Text("Level ${word.difficultyLevel}", style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.height(24.dp)
                    )
                }
            }
            
            IconButton(onClick = { showMenu = true }) {
                Icon(Icons.Default.MoreVert, contentDescription = "Options")
            }
            
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Reset Progress") },
                    onClick = {
                        onResetProgress()
                        showMenu = false
                    },
                    leadingIcon = { Icon(Icons.Default.Refresh, contentDescription = null) }
                )
                if (word.isCustom) {
                    DropdownMenuItem(
                        text = { Text("Delete") },
                        onClick = {
                            onDelete()
                            showMenu = false
                        },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) }
                    )
                }
            }
        }
    }
}

/**
 * Empty state когда нет слов.
 */
@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "📚",
                style = MaterialTheme.typography.displayLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No words found",
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Try adjusting your filters",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Dialog для добавления нового слова.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddWordDialog(
    categories: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit
) {
    var original by remember { mutableStateOf("") }
    var translate by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(categories.firstOrNull() ?: "General") }
    var showCategoryMenu by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Word") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = original,
                    onValueChange = { original = it },
                    label = { Text("English Word") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = translate,
                    onValueChange = { translate = it },
                    label = { Text("Translation") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Category selector
                ExposedDropdownMenuBox(
                    expanded = showCategoryMenu,
                    onExpandedChange = { showCategoryMenu = it }
                ) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = showCategoryMenu,
                        onDismissRequest = { showCategoryMenu = false }
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category) },
                                onClick = {
                                    selectedCategory = category
                                    showCategoryMenu = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (original.isNotBlank() && translate.isNotBlank()) {
                        onConfirm(original.trim(), translate.trim(), selectedCategory)
                    }
                },
                enabled = original.isNotBlank() && translate.isNotBlank()
            ) {
                Text("ADD")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL")
            }
        }
    )
}
