package net.blakelee.assistedinjectcomposable

import androidx.lifecycle.ViewModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject

class MainViewModel @AssistedInject constructor(
    @Assisted val test: Int
) : ViewModel() {

    @AssistedFactory
    @AssistedFactoryComposable
    interface Factory {
        fun create(test: Int): MainViewModel
    }
}