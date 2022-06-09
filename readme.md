# Assisted Inject Composable

### Summary

The purpose of this library is to generate assisted injected ViewModel's that can be called from any composable.

### How to use

1. Add KSP to your project
2. Add Dagger to your project if you haven't already
3. Add `implementation "net.blakelee:assisted-inject-composable:1.0.0"` to app build.gradle
4. Build the project
5. Utilize the new assisted methods by prepending `assisted` before the `ViewModel` class name e.g. `assistedMyViewModel()`
   ```kotlin
   // Sample class
   class MyViewModel @AssistedInject constructor(
      @Assisted val myValue: Int
   ) : ViewModel() {
      
      ...
      
      @AssistedFactory
      interface Factory {
         fun create(myValue: Int): MyViewModel
      }
   }
   
   ...
   
   @Composable
   fun MyComposable(
      myValue: Int, 
      myViewModel: MyViewModel = assistedMyViewModel(myValue)
   ) {
   
      ...
   }
    ```