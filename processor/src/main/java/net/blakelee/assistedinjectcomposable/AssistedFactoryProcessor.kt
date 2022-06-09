package net.blakelee.assistedinjectcomposable

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import java.io.OutputStream

private const val fileName = "AssistedComposableModule"
private const val packageName = "net.blakelee.assistedinjectcomposable"
private const val assistedAnnotation = "dagger.assisted.AssistedFactory"

class AssistedFactoryProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
    private val options: Map<String, String>
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver
            .getSymbolsWithAnnotation(assistedAnnotation)
            .filterIsInstance<KSClassDeclaration>()

        if (!symbols.iterator().hasNext()) return emptyList()

        val file: OutputStream = codeGenerator.createNewFile(
            // Make sure to associate the generated file with sources to keep/maintain it across incremental builds.
            // Learn more about incremental processing in KSP from the official docs:
            // https://kotlinlang.org/docs/ksp-incremental.html
            dependencies = Dependencies(false, *resolver.getAllFiles().toList().toTypedArray()),
            packageName = packageName,
            fileName = fileName
        )

        generateImports(file)

        symbols.forEach { it.accept(Visitor(file), Unit) }

        generatedGetAssistedFactory(file)
        generateCreateAssistedViewModel(file)
        generateGetActivity(file)

        file.close()
        val unableToProcess = symbols.filterNot { it.validate() }.toList()
        return unableToProcess

    }

    operator fun OutputStream.plusAssign(str: String) {
        this.write(str.toByteArray())
    }

    inner class Visitor(private val file: OutputStream) : KSVisitorVoid() {
        override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
            val factoryName = classDeclaration.qualifiedName?.asString() ?: return

            val functions = classDeclaration.getAllFunctions().filter {
                it.simpleName.asString() !in listOf("equals", "hashCode", "toString")
            }

            val factoryFunctionName = functions.first()
                .returnType
                ?.resolve()
                ?.declaration
                ?.simpleName
                ?.asString()
                .orEmpty()

            generateProvider(factoryFunctionName, factoryName)

            functions.forEach { function ->

                // Return the fully qualified return type
                val returnType = function.returnType
                    ?.resolve()
                    ?.declaration ?: return@forEach

                val qualifiedReturnType = returnType
                    .qualifiedName
                    ?.asString() ?: return@forEach

                val simpleReturnType = returnType
                    .simpleName
                    .asString()

                val parameters = function.parameters

                val functionName = function.simpleName.asString()

                generateComposable(simpleReturnType, qualifiedReturnType, parameters, functionName)
            }
        }

        fun visitTypeArgument(typeArgument: KSTypeArgument): String {
            return buildString {
                val declaration = typeArgument
                    .type
                    ?.resolve()
                    ?.declaration

                this += if (declaration?.qualifiedName?.asString()?.startsWith("kotlin") == true) {
                    declaration
                        .simpleName
                        .asString()
                } else {
                    declaration
                        ?.qualifiedName
                        ?.asString()
                        .orEmpty()
                }
            }
        }

        private fun visitTypeArguments(typeArguments: List<KSTypeArgument>): String {
            return buildString {

                if (typeArguments.isNotEmpty()) {
                    this += "<"
                    typeArguments.forEachIndexed { i, arg ->
                        this += visitTypeArgument(arg)
                        this += visitTypeArguments(arg.type?.resolve()?.arguments ?: emptyList())
                        if (i < typeArguments.lastIndex) this += ", "
                    }
                    this += ">"
                }
            }
        }

        private fun generateParameters(
            parameters: List<KSValueParameter>
        ): String {

            return parameters.joinToString(prefix = "    ", separator = ",\n    ", postfix = "\n") {
                val type = it.type.resolve()
                val declaration = type.declaration
                val qualifiedName: String = declaration.qualifiedName?.asString().orEmpty()
                val parameterType = qualifiedName.takeIf { p -> !p.startsWith("kotlin") }
                    ?: declaration.simpleName.asString()
                val parameterProjections = visitTypeArguments(type.arguments)

                val parameterName = "${it.name?.asString()}"
                "$parameterName: $parameterType$parameterProjections"
            }
        }

        private fun generateProvider(returnType: String, factoryType: String) {
            file += """
            
            @EntryPoint
            @InstallIn(ActivityComponent::class)
            private interface ${returnType}_FactoryProvider {
                fun ${returnType}Factory(): $factoryType
            }
            
            """.trimIndent()
        }

        private fun generateComposable(
            simpleReturnType: String,
            qualifiedReturnType: String,
            parameters: List<KSValueParameter>,
            functionName: String
        ) {
            val parameterNames = parameters.joinToString(", ") { it.name?.asString().orEmpty() }
            file += "\n@Composable\n"
            file += "fun assisted$simpleReturnType(\n"
            file += generateParameters(parameters)
            file += """
                ): $qualifiedReturnType { 
                    val factory = getAssistedFactory<${simpleReturnType}_FactoryProvider>()
                        .${simpleReturnType}Factory()
                    
                    return createAssistedViewModel(LocalSavedStateRegistryOwner.current) {
                        factory.$functionName($parameterNames)
                    }
                }
                
            """.trimIndent()
        }
    }

    private fun generateImports(file: OutputStream) {
        file += """
            package $packageName
            
            import android.app.Activity
            import android.content.Context
            import android.content.ContextWrapper
            import androidx.compose.runtime.Composable
            import androidx.compose.ui.platform.LocalContext
            import androidx.compose.ui.platform.LocalSavedStateRegistryOwner
            import androidx.lifecycle.AbstractSavedStateViewModelFactory
            import androidx.lifecycle.SavedStateHandle
            import androidx.lifecycle.ViewModel
            import androidx.lifecycle.viewmodel.compose.viewModel
            import androidx.savedstate.SavedStateRegistryOwner
            import dagger.hilt.EntryPoint
            import dagger.hilt.InstallIn
            import dagger.hilt.android.EntryPointAccessors
            import dagger.hilt.android.components.ActivityComponent
                
            """.trimIndent()
    }

    private fun generateCreateAssistedViewModel(file: OutputStream) {
        file += """
            
            @Suppress("UNCHECKED_CAST")
            @Composable
            private inline fun <reified T : ViewModel> createAssistedViewModel(
                owner: SavedStateRegistryOwner,
                crossinline factory: () -> T
            ): T {
                return viewModel(
                    factory = object : AbstractSavedStateViewModelFactory(owner, null) {
                        override fun <T : ViewModel> create(
                            key: String, modelClass: Class<T>, handle: SavedStateHandle
                        ): T = factory() as T
                    }
                )
            }
            
        """.trimIndent()
    }

    private fun generatedGetAssistedFactory(file: OutputStream) {
        file += """
                        
            @Composable
            private inline fun <reified T> getAssistedFactory() = EntryPointAccessors.fromActivity(
                LocalContext.current.getActivity(),
                T::class.java
            )

        """.trimIndent()
    }

    private fun generateGetActivity(file: OutputStream) {
        file += """
            
            @Composable
            private fun Context.getActivity(): Activity {
                return when(this) {
                    is Activity -> this
                    is ContextWrapper -> baseContext.getActivity()
                    else -> throw Exception("Can't find Activity")
                }
            }
            
        """.trimIndent()
    }
}

private operator fun StringBuilder.plusAssign(value: String) {
    append(value)
}