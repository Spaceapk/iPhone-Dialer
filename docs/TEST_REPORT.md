# Informe de pruebas · 1.0.0-alpha01

Fecha: 2026-10-21. **Alcance: build debug y emulador; no validación de producción.**

## Entorno
- Linux x86_64, Amazon Corretto JDK 17, Gradle 8.11.1, AGP 8.10.1.
- SDK compile/target 36; minSdk 29; Build Tools 36.0.0.
- Emulador AOSP Android 10/API 29, x86_64, pantalla 720 × 1440, densidad 320, sin aceleración KVM y sin audio de host. No hay teléfono físico conectado.

## Ejecutado y aprobado
| Verificación | Resultado real |
|---|---|
| `assembleDebug` | APK generado correctamente |
| `testDebugUnitTest` | 20 pruebas, 0 fallos, 0 errores, 0 omitidas |
| `lintDebug` | 0 errores, 18 advertencias; no se deshabilitó abortOnError ni se añadió baseline para ocultar errores |
| `assembleDebugAndroidTest` | APK instrumentado generado |
| Instalación con adb | App debug y APK de pruebas instalados con éxito |
| AndroidJUnitRunner | 2 pruebas de UI, ambas aprobadas: entrada de dígitos/acción de llamada por callback y controles deshabilitados con campo vacío |
| `apksigner verify` | APK válido, esquema v2, certificado Android Debug; **no** firma de producción |
| `zipalign -c -P 16 4` | Comprobación aprobada (no equivale a probar ejecución en dispositivo de páginas de 16 KB) |
| `check-project.py` | Comprobaciones básicas de manifest/configuración aprobadas; no material de firma privado en fuentes |

Las pruebas de UI del teclado verifican el callback solicitado por el usuario, **no hacen una llamada telefónica ni prueban Telecom**. Las pruebas unitarias cubren normalización de acentos, T9, búsqueda, filtrado de dígitos, validación de cadenas y formato de duración; no cubren todos los repositorios/servicios.

## Advertencias y correcciones
- El primer intento de build fue interrumpido por límites de memoria al coincidir con el emulador; se redujo el paralelismo y se compiló Kotlin dentro del proceso de Gradle.
- Se corrigieron errores del compilador y cuatro hallazgos de Lint relativos a permisos y API 34. El build final pasó; no se considera que un build fallido haya validado nada.
- Las 18 advertencias restantes incluyen versiones más recientes de dependencias/Gradle, preferencia por KSP frente a kapt, uso de extensiones KTX, orden opcional de parámetros Compose y decisión explícita de exigir hardware telefónico. Debe revisarse la actualización de dependencias antes del lanzamiento estable. El XML de Lint está en `evidence/`.

## No probado en hardware real
Llamadas sobre red del operador, timbre audible, recepción en todos los estados, emergencias, Bluetooth, auriculares físicos, doble SIM, espera/conferencia del operador, buzón, ahorro de batería, reinicio físico, TalkBack, contraste medido, rendimiento prolongado, seguridad de dependencias y compatibilidad multi-OEM/API moderna. No se afirma que estas funciones estén verificadas.

## Servicios externos
GitLab y Codemagic: solo configuración local, sin ejecución remota. Google Drive: sin conexión autorizada, sin respaldo subido. Firma de producción: no aprovisionada. El AAB firmado y la versión estable 1.0.0 están pendientes.

## Evidencias
Resultados XML unitarios, XML de Lint, verificación pública del certificado debug y capturas cuando se hayan podido inspeccionar. Ninguna evidencia contiene una clave privada. Los datos de prueba que se introduzcan en el emulador son exclusivamente sintéticos, nunca la agenda del usuario.
