package com.keal.ham
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.keal.ham.ui.HamScreen
import com.keal.ham.ui.HamViewModel
import com.keal.ham.ui.theme.HamTheme
class MainActivity : ComponentActivity() {
 override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); setContent { HamTheme { HamScreen(viewModel<HamViewModel>()) } } }
}
