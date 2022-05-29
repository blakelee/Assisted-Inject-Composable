package net.blakelee.assistedinjectcomposable

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

class AssistedFactoryProcessorProvider : SymbolProcessorProvider {

    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return AssistedFactoryProcessor(
            environment.codeGenerator,
            environment.logger,
            environment.options
        )
    }
}