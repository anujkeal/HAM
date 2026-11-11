package com.keal.ham.ui.theme
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
private val Light=lightColorScheme(primary=Color(0xFF5B4BFF),secondary=Color(0xFF5B4BFF),tertiary=Color(0xFFCDF45E),background=Color(0xFFF7F6FC),surface=Color.White,surfaceVariant=Color(0xFFF0EEF8))
private val Dark=darkColorScheme(primary=Color(0xFF8A7DFF),secondary=Color(0xFF8A7DFF),tertiary=Color(0xFFCDF45E),background=Color(0xFF111018),surface=Color(0xFF1B1924),surfaceVariant=Color(0xFF292634))
@Composable fun HamTheme(content:@Composable()->Unit){MaterialTheme(colorScheme=if(isSystemInDarkTheme())Dark else Light,content=content)}
