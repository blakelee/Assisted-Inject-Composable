package net.blakelee.assistedinjectcomposable.viewmodel

import androidx.lifecycle.ViewModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject

class DuplicateSignatureViewModel @AssistedInject constructor(
    @Assisted val test: Int
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(test: Int): DuplicateSignatureViewModel
    }
}