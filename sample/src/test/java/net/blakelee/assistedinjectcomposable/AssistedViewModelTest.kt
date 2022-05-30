package net.blakelee.assistedinjectcomposable

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AssistedViewModelTest {

    @get:Rule
    val rule = createAndroidComposeRule<TestActivity>()

    @Test
    fun `Kotlin primitive assisted injection`() {
        rule.setContent {
            val viewModel = assistedMainViewModel(test = 4)
            assertEquals(4, viewModel.test)
        }
    }

    @Test
    fun `Complex object assisted injection`() {
        rule.setContent {
            val map = mapOf<String, List<Int>>()
            val complexObj = TestObjectB(1)

            val viewModel = assistedComplexViewModel(
                valueA = map,
                valueB = complexObj
            )

            assertEquals(map, viewModel.valueA)
            assertEquals(complexObj, viewModel.valueB)
        }
    }
}