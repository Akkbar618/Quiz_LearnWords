package com.example.quiz_engwords.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val Shapes = Shapes(
    // ExtraSmall - для мелких элементов (чипы, бейджи)
    extraSmall = RoundedCornerShape(4.dp),
    
    // Small - для мелких кнопок, индикаторов
    small = RoundedCornerShape(8.dp),
    
    // Medium - для карточек, полей ввода
    medium = RoundedCornerShape(16.dp),
    
    // Large - для больших карточек, модальных окон
    large = RoundedCornerShape(24.dp),
    
    // ExtraLarge - для bottom sheets, dialogs
    extraLarge = RoundedCornerShape(32.dp)
)
