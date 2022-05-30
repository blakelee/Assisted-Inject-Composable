package net.blakelee.assistedinjectcomposable

import androidx.lifecycle.ViewModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import javax.inject.Inject

class ComplexViewModel @AssistedInject constructor(
    @Assisted val valueA: Map<String, List<Int>>,
    @Assisted val valueB: TestObjectB,
    val valueD: TestObjectA
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(valueA: Map<String, List<Int>>, valueB: TestObjectB): ComplexViewModel
    }
}

class TestObjectA @Inject constructor()

data class TestObjectB(val valueA: Int)

