package net.blakelee.assistedinjectcomposable

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import dagger.hilt.android.AndroidEntryPoint
import net.blakelee.assistedinjectcomposable.viewmodel.MainViewModel

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MainScreen()
        }
    }
}

@Composable
fun MainScreen(assistedTest: Int = 1) {
    val mainViewModel: MainViewModel = assistedMainViewModel(assistedTest)
    Text("${mainViewModel.test}")
}