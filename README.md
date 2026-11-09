# Aguinaldo México 🇲🇽

Aplicación Android para calcular **aguinaldo bruto, parte exenta, parte gravada, ISR estimado y aguinaldo neto estimado**, con información fiscal por ejercicio, guía de derechos laborales y enlaces a fuentes oficiales.

> **Versión del proyecto:** `3.2.2` (`versionCode 18`). **Android:** `compileSdk 36`, `targetSdk 36`, `minSdk 24`. **Aplicación publicada:** `com.diversitisystems.aguinaldomexico`. El `namespace` de código Java es `com.mexico.aguinaldo.aguinaldomexico`; ambos valores son deliberadamente distintos. **No cambiar el `applicationId`** al actualizar la aplicación existente de Google Play.
>
> Este repositorio contiene **código fuente**, no la clave privada ni un Android App Bundle firmado. El cálculo del impuesto es una **estimación**: la retención real puede depender del resto de la nómina y de las condiciones de la persona.

## 1. ¿Qué contiene la app?

### Calculadora

- **Fecha de ingreso** y **una sola fecha de fin y pago del aguinaldo**: se usan para calcular días trabajados y elegir ejercicio fiscal/UMA.
- **Salario mensual bruto** editable, con sugerencia táctil del salario mínimo **general** mensual del ejercicio seleccionado, leída de los datos fiscales.
- **Días de aguinaldo** editables y sugerencia táctil **«Mínimo de ley: 15 días»**, que coloca `15` sin impedir cambiarlo.
- Selector de fecha Material Design y selección rápida de año.
- Validación de fechas, salario y días; cálculo proporcional considerando **365 o 366 días** del año de corte.
- Resultado con **días trabajados, aguinaldo bruto, parte exenta, parte gravada, ISR estimado, neto estimado, UMA diaria usada y ejercicio fiscal**.
- Tras calcular correctamente, el formulario se **contrae** y se conserva un resumen; **Editar datos** restaura los campos sin borrar los valores anteriores.
- Interfaz con soporte para barras de sistema Android 15/16, icono circular/adaptativo aprobado y publicidad de AdMob.

### Navegación: cada opción tiene su propia pantalla

El menú lateral (☰) está disponible desde la calculadora y desde las páginas informativas. Se abre con el botón de menú; **Atrás** cierra el menú abierto antes de salir de la pantalla. Desde una pantalla secundaria se puede volver a la calculadora o cambiar de tema directamente.

| Opción | Pantalla / clase | Contenido |
|---|---|---|
| Calculadora | `MainActivity` | Formulario y resultados |
| Guía del aguinaldo | `InformacionAguinaldoActivity` | Portada y accesos directos a las demás páginas |
| Cómo se calcula | `ComoCalcularActivity` | Fórmulas, proporcionalidad y ejemplo |
| Derechos laborales | `DerechosLaboralesActivity` | Derechos, fechas de pago, casos habituales y dudas frecuentes |
| ISR y UMA | `IsrUmaActivity` | Exención, parte gravada, cálculo del neto y vigencia de la UMA |
| Valores fiscales por año | `ValoresFiscalesActivity` | Selector de ejercicio, UMA de enero/febrero-diciembre y salarios mínimos general/frontera |
| Fuentes oficiales | `FuentesOficialesActivity` | PROFEDET, LFT, SAT, INEGI y CONASAMI |
| Calificar aplicación | `MenuNavegacion` | Abre ficha de la app en Google Play |

Las cinco pantallas temáticas son **Activities independientes** (declaradas en `AndroidManifest.xml`) que comparten el diseño y los componentes comunes de `InformacionAguinaldoActivity`. **Ya no navegan mediante desplazamiento dentro de una página única**.

### Calificaciones en Google Play

- Solicitud automática mediante **Google Play In-App Review** después de al menos **7 días desde el primer uso registrado** y **3 cálculos exitosos**.
- Solo se guarda **un intento automático** local (`SharedPreferences`); la aplicación no puede detectar si el usuario realmente calificó ni cuántas estrellas puso.
- Google Play puede decidir no mostrar el cuadro; se considera consumido el intento cuando se inicia la solicitud.
- La opción manual **Calificar aplicación** siempre abre Google Play y **no consume el intento automático**.
- Si se borran los datos de la app o se reinstala, la marca local puede perderse. Para probar la solicitud es preferible una instalación mediante un canal de prueba de Google Play.


### Teclado numérico en Android 15/16 (corrección 3.2.1)

- `MainActivity` utiliza `WindowInsetsCompat.Type.ime()` para reservar el espacio del teclado cuando la interfaz está en modo edge-to-edge.
- Los campos **Salario mensual** y **Días de aguinaldo** se desplazan automáticamente para permanecer visibles cuando el usuario les da foco.
- El anuncio AdMob inferior se oculta **solo mientras el teclado está abierto** y reaparece al cerrarlo. No se altera la configuración de AdMob.
- El teclado muestra **Siguiente** en el salario y **Listo** en los días de aguinaldo.
- `AndroidManifest.xml` mantiene `windowSoftInputMode="adjustResize"`; con edge-to-edge también se deben procesar los insets del IME explícitamente.
- **Para Google Play**, la versión usa `minSdk 24` porque se habilitó la protección automática de Play, y tiene `versionCode 18` por ser una actualización posterior a la versión 17 publicada en producción. Requiere un nuevo `.aab` firmado con la misma clave de carga.

Prueba manual antes de publicar: en un teléfono Android 15/16, abre la app, pulsa en Salario mensual y escribe; luego pulsa Días de aguinaldo (incluido el botón Siguiente del teclado), escribe un valor y verifica que el campo y sus números sean visibles. Oculta el teclado y verifica que el anuncio vuelva a aparecer. Repite con una pantalla pequeña, letras grandes y orientación horizontal si está habilitada.

## 2. Arquitectura y dependencias

- **Lenguaje:** Java 11 (código compatible con Android).
- **Gradle Wrapper:** `gradle/wrapper`, versiones fijadas en `gradle/libs.versions.toml` y `gradle/wrapper/gradle-wrapper.properties`.
- **Android Gradle Plugin:** 8.11.1; **compile/target SDK:** 36; **mínimo:** SDK 24.
- **UI:** AndroidX AppCompat, Material Components, DrawerLayout, ConstraintLayout.
- **Fechas:** ThreeTenABP y Material DatePicker.
- **Firebase:** Realtime Database para tarifas y parámetros, Crashlytics para registro de excepciones, `google-services.json` para configurar el proyecto Firebase.
- **Datos JSON:** Gson.
- **Publicidad:** Google Mobile Ads (AdMob).
- **Reseñas:** Play In-App Review 2.0.2.

Ubicaciones importantes:

```text
app/
├── build.gradle                      # ID publicado, SDK y versión
├── google-services.json             # Identificadores públicos de Firebase de esta app
├── src/main/
│   ├── AndroidManifest.xml           # Activities, permisos, icono y AdMob
│   ├── assets/tablasISR_base.json    # Respaldo oficial incluido (2025 y 2026)
│   ├── java/com/mexico/aguinaldo/aguinaldomexico/
│   │   ├── MainActivity.java         # Calculadora, picker, resultados, anuncios
│   │   ├── MenuNavegacion.java       # Drawer y navegación entre Activities
│   │   ├── InformacionAguinaldoActivity.java # Componentes compartidos y portada
│   │   ├── ComoCalcularActivity.java
│   │   ├── DerechosLaboralesActivity.java
│   │   ├── IsrUmaActivity.java
│   │   ├── ValoresFiscalesActivity.java
│   │   ├── FuentesOficialesActivity.java
│   │   ├── ISRManager.java           # Lectura Firebase, validación y caché local
│   │   ├── DatosFiscales.java        # UMA y salarios por ejercicio
│   │   ├── TablaISR.java             # Renglón de la tarifa mensual
│   │   ├── CalculoAguinaldo.java     # Fórmulas y retención estimada
│   │   ├── SolicitudCalificacion.java
│   │   └── ReviewPolicy.java
│   └── res/                           # Layouts, estilos, menús e iconos
└── src/test/                         # Pruebas unitarias reales
```

## 3. Firebase Realtime Database: estructura

El nodo **`/tablasISR`** contiene las **tarifas mensuales del ISR** y un objeto adicional **`parametrosFiscales`** con valores por año:

```json
{
  "tablasISR": {
    "2026": [
      { "min": 0.01, "max": 844.59, "cuotaFija": 0.0, "tasa": 0.0192 }
    ],
    "parametrosFiscales": {
      "2026": {
        "umaDiaria": 117.31,
        "umaEneroDiaria": 113.14,
        "vigenteDesde": "2026-02-01",
        "salarioMinimoGeneralDiario": 315.04,
        "salarioMinimoGeneralMensual": 9582.47,
        "salarioMinimoFronteraNorteDiario": 440.87,
        "salarioMinimoFronteraNorteMensual": 13409.80
      }
    }
  }
}
```

> **Ejemplo parcial para explicar la forma**: en Firebase cada tarifa anual debe tener **11 rangos completos**, no solo el primero. Si importas desde `app/src/main/assets/tablasISR_base.json`, el contenido de ese archivo se inserta **dentro** del nodo `/tablasISR` (ya contiene las claves `2025`, `2026` y `parametrosFiscales`). No dupliques el nombre `tablasISR` dentro del nodo.

El archivo `app/src/main/assets/tablasISR_base.json` contiene **2025 y 2026** como respaldo incluido, para uso sin conexión. Si editas Firebase, **no se modifica automáticamente este asset** ya empaquetado en instalaciones previas.

### Campos y unidades

| Campo | Significado |
|---|---|
| `min`, `max` | Límites inferior/superior del rango **mensual** de ISR, en MXN |
| `cuotaFija` | Cuota fija del rango mensual, MXN |
| `tasa` | Fracción, p. ej. `0.30` equivale a 30% (no escribir `30`) |
| `umaDiaria` | UMA diaria vigente desde `vigenteDesde` (usualmente febrero) |
| `umaEneroDiaria` | UMA diaria aplicable en enero del ejercicio |
| `vigenteDesde` | Fecha ISO `AAAA-MM-DD` del cambio de UMA |
| `salarioMinimoGeneralDiario/Mensual` | Salario mínimo general, referencia, MXN |
| `salarioMinimoFronteraNorteDiario/Mensual` | Salario mínimo ZLFN, referencia, MXN |

Valores incluidos en el proyecto:

| Ejercicio | UMA enero | UMA desde febrero | Salario mínimo general diario | Salario mínimo general mensual | Frontera norte diario | Frontera norte mensual |
|---|---:|---:|---:|---:|---:|---:|
| 2025 | $108.57 | $113.14 | $278.80 | $8,480.17 | $419.88 | $12,771.35 |
| 2026 | $113.14 | $117.31 | $315.04 | $9,582.47 | $440.87 | $13,409.80 |

La interfaz muestra **salario mínimo general mensual**, aunque también se conserva la referencia de frontera norte en la pantalla de valores fiscales. No se fuerza el valor al usuario; es una sugerencia táctil.

## 4. ¿Cómo y cuándo se actualizan los datos?

Implementado en `ISRManager.java`:

1. **Al abrir la calculadora**: `MainActivity` llama a `isrManager.precargar(añoActual)`. Este método **fuerza un intento de consulta a Firebase**, aunque haya datos locales recientes.
2. **Al calcular**: se llama a `isrManager.obtenerDatos(añoDePago)`. Si hay una **copia local validada** y su última actualización remota tiene menos de **24 horas**, la utiliza; si no, solicita el nodo `/tablasISR` a Firebase.
3. **Si Firebase responde**: valida 11 rangos ISR, continuidad, UMA, salarios y campos requeridos. Si pasa, guarda datos por año en `SharedPreferences` (`ISR_DATOS_FISCALES_V2`, claves `datos_AAAA` y `ultima_AAAA`).
4. **Si falla la red o los datos**: intenta utilizar la copia local válida. Si no existe, intenta el JSON de respaldo `assets/tablasISR_base.json` (solo 2025 y 2026).
5. **Si no hay datos fiscales válidos para ese año**: muestra error. **No toma tarifas de otro año ni inventa un ISR de cero**.
6. **Pantalla Valores fiscales**: usa `precargar(añoSeleccionado)` para consultar Firebase al entrar o cambiar de ejercicio.

**Importante:** la app **no escucha Firebase en tiempo real ni sincroniza mientras está cerrada**. Si acabas de cambiar las tarifas de un año, reiniciar la aplicación fuerza una consulta. Si el cálculo se ejecuta con caché reciente **antes de finalizar esa consulta**, aún puede usar la copia guardada; consulta los logs para confirmar el origen y verifica el resultado antes de publicar datos nuevos.

### ¿Por qué podría mostrar datos locales en el emulador?

No necesariamente indica un error: las consultas de cálculo respetan la copia de 24 horas. El método de apertura sí intenta Firebase, pero si falla (sin conexión, reglas RTDB, proyecto mal configurado, permisos, datos incompletos), usa respaldo. Para diagnosticar, filtra **Logcat** por `ISRManager`, revisa las reglas/URL de Realtime Database y confirma que `google-services.json` corresponda al `applicationId` publicado.

### Actualizar 2027 y años siguientes

1. Espera la **publicación oficial** de la tarifa mensual del ISR, la UMA y los salarios mínimos del ejercicio; **no copies 2026 como si fuera 2027**.
2. Respalda el nodo `/tablasISR` en Firebase Console.
3. Crea `/tablasISR/2027` con los 11 rangos ISR oficiales (objetos `min`, `max`, `cuotaFija`, `tasa`).
4. Crea `/tablasISR/parametrosFiscales/2027` con **los siete campos** indicados en la tabla anterior: UMA normal, UMA de enero, fecha de vigencia y salarios generales/ZLFN diarios y mensuales.
5. Comprueba que todos los rangos sean continuos (centavos), sin solapamientos, con último `max` suficientemente alto para cubrir los ingresos y tasas expresadas como fracción.
6. Prueba la app **con conexión** en 2027 (o cambiando la fecha de corte de la calculadora). Si no hay datos oficiales completos, debe indicar que no están disponibles.
7. Para garantizar cálculo sin conexión en ese año desde la primera instalación, incorpora también 2027 a `assets/tablasISR_base.json` y publica una actualización de la app. **Solo subir Firebase permite uso en línea, no añade nuevos años al respaldo incluido**.

> Regla operacional: nunca publicar información fiscal estimada como oficial. Revisa SAT/DOF, INEGI y CONASAMI antes de habilitar nuevos ejercicios.

## 5. ¿Cómo calcula el aguinaldo?

`CalculoAguinaldo.java` calcula:

1. `salarioDiario = salarioMensualBruto / 30`.
2. `diasTrabajados = fechas inclusivas entre máximo(fechaIngreso, 1 de enero del corte) y fechaFinPago`.
3. `aguinaldoBruto = salarioDiario × diasAguinaldo × diasTrabajados / diasDelAño` (`365` o `366`).
4. `exento = min(aguinaldoBruto, 30 × UMA diaria aplicable)`.
5. `gravado = aguinaldoBruto − exento`.
6. `ISR estimado`: se calcula una tasa efectiva aproximada de gratificación comparando el impuesto mensual sobre el sueldo ordinario y el sueldo más la fracción mensualizada de la gratificación gravada (método del art. 174 del Reglamento de la LISR).
7. `netoEstimado = aguinaldoBruto − ISR estimado`.

La calculadora **presupone disponible la exención anual** y que se cumplen los requisitos generales; no contabiliza otras gratificaciones exentas ya pagadas ni subsidio al empleo, ajustes, sueldos variables o regímenes especiales. El valor mostrado es **estimado**, aunque se ofrece un único resultado para no confundir al usuario. En el resultado se muestran la UMA aplicada y el ejercicio ISR, pero no párrafos legales extensos.

## 6. Compilar, probar y publicar

### Requisitos

- Android Studio actualizado y Android SDK **API 36** instalado.
- **JDK 17 o superior compatible** (revisar configuración de Gradle JDK; AGP 8.11.1 exige como mínimo JDK 17).
- Conexión a internet para la primera sincronización de dependencias.
- Para producción: **clave de carga original** (`.jks`/`.keystore`) de Google Play. **No incluirla en Git.**

### Abrir y validar

1. Abrir la carpeta **que contiene `settings.gradle`** en Android Studio.
2. Esperar Gradle Sync.
3. Usar **Build → Clean Project** y **Build → Assemble Project**, o desde Windows:

   ```powershell
   .\gradlew.bat clean :app:assembleDebug
   .\gradlew.bat :app:testDebugUnitTest
   ```

4. Probar en Android 15 y 16: reloj, señal y barra inferior, calendario, sugerencias táctiles, cálculo y botón Editar datos. Con el teclado numérico abierto, confirmar que sueldo y días se desplazan a una zona visible y que el anuncio no tapa los campos.
5. Recorrer **todas** las opciones del menú lateral: cada opción abre **su propia Activity**, sin desplazarse a otra sección. Comprobar botón Atrás, cambio entre pantallas y menú con el drawer abierto.
6. Probar Firebase en red y sin conexión; el ejercicio 2027 sin datos **no debe arrojar ISR cero**.
7. Revisar errores y ANR de Firebase Crashlytics, comportamiento del anuncio y reseñas en un canal de pruebas de Google Play.

### Firmar un AAB para Google Play

1. Confirmar en Play Console que el identificador sigue siendo `com.diversitisystems.aguinaldomexico`.
2. Revisar el **mayor `versionCode` ya utilizado** en Play Console: aumentar `versionCode` en `app/build.gradle` si es necesario (actualmente `18`).
3. En Android Studio, **Build → Generate Signed App Bundle or APK → Android App Bundle**, variante `release`.
4. Firmar con **la clave de carga registrada** para la aplicación; guardar keystore/contraseñas **fuera del repositorio**.
5. Subir el `.aab` a **Prueba interna**, verificar instalación y funcionamiento, y después promover a **Producción**.
6. Validar en Play Console la API de destino, firma, política de anuncios, ficha, declaración de datos y permisos antes de enviar a revisión.

> **Estado del repositorio:** fuentes, navegación y archivos XML revisados; la compilación Android completa, firma y subida a Play Console **deben realizarse en tu entorno con Android SDK**. No se incluye un `.aab` precompilado.

## 7. Git: mantener el repositorio limpio

Los únicos documentos de mantenimiento son **este `README.md`**. Se retiraron notas de cambios previas, exportaciones redundantes de JSON, imágenes sin referencias, archivos de build y tests de ejemplo. Se conservaron el Gradle Wrapper, los recursos del icono aprobado, las pruebas unitarias relevantes y el respaldo fiscal incluido.

El `.gitignore` impide subir `build/`, `.gradle/`, `.idea/`, `local.properties`, `.apk`, `.aab`, `.jks`, `.keystore` y secretos locales. **Revisa `git status` antes de hacer commit.** `google-services.json` contiene identificadores de configuración cliente, no una clave de cuenta de servicio; protege igualmente tu Firebase con reglas de lectura/escritura correctas. Nunca publiques contraseñas, `serviceAccountKey.json` ni claves de carga.

Para subirlo a un repositorio ya inicializado:

```bash
git status
git add .
git commit -m "Aguinaldo Mexico 3.2.2: optimizacion R8 para produccion"
git push origin main
```

Ajusta `main` a la rama real de tu repositorio. No ejecutes `git init` ni reemplaces el historial si el proyecto ya usa Git.

## 8. Fuentes oficiales

- [PROFEDET: aguinaldo](https://www.profedet.gob.mx/micrositio/index.php/aguinaldo)
- [Ley Federal del Trabajo (artículo 87)](https://www.diputados.gob.mx/LeyesBiblio/pdf/LFT.pdf)
- [SAT: régimen de salarios y beneficios](https://wwwmat.sat.gob.mx/consulta/02330/conoce-los-beneficios-del-regimen-de-salarios)
- [INEGI: UMA](https://www.inegi.org.mx/temas/uma/)
- [CONASAMI: salarios mínimos](https://www.gob.mx/conasami/documentos/consulta-de-salarios-minimos)

---

**Mantenimiento:** cuando cambien las cifras oficiales, actualiza Firebase, verifica el respaldo fiscal para nuevas instalaciones sin conexión y prueba cálculos de un período completo y uno proporcional antes de publicar otra versión.


## 10. Optimización DEX de Google Play (cambio 3.2.2)

Google Play advirtió **0 % de ofuscación** en la versión `17 (3.2.1)`.
La causa en `app/build.gradle` era `minifyEnabled false` en `release`.

Esta versión configura:

```gradle
buildTypes {
    release {
        minifyEnabled true
        shrinkResources true
        proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
    }
}
```

**Qué significa:** R8 reduce, optimiza y ofusca código en la variante `release`. `shrinkResources true` elimina recursos no utilizados. **La variante `debug` permanece sin ofuscación**, por lo que probar solamente con el botón Run de Android Studio **no comprueba** que la versión de Play funcione correctamente.

### Reglas de conservación y JSON

`proguard-rules.pro` conserva los dos modelos utilizados por Firebase/Gson (`TablaISR`, `DatosFiscales`), sus nombres de campos y las firmas genéricas de `TypeToken<List<TablaISR>>`. Evita reglas amplias como `-keep class com.google.** { *; }`, que impedirían parte de la optimización.

### Pruebas que deben pasar con la variante release

1. Ejecutar `gradlew.bat clean :app:assembleRelease` desde Windows, con el Android SDK disponible.
2. Firmar un **Android App Bundle release** con la clave de carga correcta. El AAB destinado a Play es el de esta variante `release`.
3. Instalar la versión release firmada en un dispositivo (o generar e instalar APKs desde el AAB con bundletool) antes de publicarla. Comprobar cálculo de 92 y 365 días, Firebase en línea y sin conexión, cambio entre años, pantallas, menús, calendario, campo de salario y teclado, anuncios y reseñas.
4. Conservar el archivo **`app/build/outputs/mapping/release/mapping.txt`** asociado a la versión 18. No se versiona en Git: guárdalo de forma privada para rastrear fallos. El plugin de Crashlytics puede subir el mapa automáticamente cuando se genera el release.
5. Subir el AAB **18 (3.2.2)** primero a **Prueba interna** en Play Console y revisar la sección **Explorador de app bundles → Optimización de código DEX**. La métrica se calcula sobre el paquete subido; activar R8 **no garantiza por sí solo** alcanzar el 25 % exigido para apps con más de 10 MB de DEX.
6. Si el porcentaje sigue bajo, revisar reglas `-keep` y las recomendaciones de Play para R8. No subir a producción una versión que presente errores por ofuscación.

Desde febrero de 2027 Google Play exige al menos el 25 % de ofuscación, optimización y reducción **a las aplicaciones cuyo DEX supere 10 MB**. Consulta [requisitos oficiales de Play Console](https://support.google.com/googleplay/android-developer/answer/17492799?hl=es-TM) y [documentación de R8](https://developer.android.com/topic/performance/app-optimization/enable-app-optimization).
