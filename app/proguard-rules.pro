# Aguinaldo México - reglas R8 para compilaciones release
# Mantener símbolos y líneas para reportes de errores en Firebase Crashlytics.
-keepattributes SourceFile,LineNumberTable,Signature,InnerClasses,EnclosingMethod,*Annotation*

# ISRManager serializa estos modelos con Gson y los lee desde Firebase.
# Los nombres de sus campos son parte del formato JSON y NO deben renombrarse.
# Son solo dos modelos pequeños; el resto de la app queda disponible para R8.
-keep class com.mexico.aguinaldo.aguinaldomexico.TablaISR { *; }
-keep class com.mexico.aguinaldo.aguinaldomexico.DatosFiscales { *; }

# ISRManager usa TypeToken<List<TablaISR>> y R8 debe conservar esa firma genérica.
-keep,allowobfuscation,allowshrinking,allowoptimization class com.google.gson.reflect.TypeToken { *; }
-keep,allowobfuscation,allowshrinking,allowoptimization class * extends com.google.gson.reflect.TypeToken
