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
| AndroidJUnitRunner | 3 pruebas de UI, todas aprobadas: entrada de dígitos/acción de llamada por callback con botón visible, controles deshabilitados con campo vacío y ausencia de superposición en Favoritos |
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

## Inspección e integración en el emulador
- Se abrió el diálogo oficial de ROLE_DIALER y se seleccionó la app; `dumpsys role` confirmó el paquete debug como titular.
- Se inyectó una llamada solo en el módem virtual del emulador. La pantalla propia recibió el estado entrante, el botón Contestar llevó a estado activo confirmado por `adb emu gsm list`, y Finalizar eliminó la sesión. No se realizó una llamada por red móvil.
- En la primera compilación se inspeccionaron pantallas principales y la interfaz de llamada. La inspección detectó superposición de búsqueda/estado vacío en Favoritos/Contactos y falta de acceso inmediato al botón Llamar en el viewport pequeño; se corrigieron y se añadió una prueba de regresión.
- Tras fijar el botón, una prueba intentaba desplazarlo aunque ya no pertenecía al contenedor desplazable; ese intento falló. Se corrigió la prueba para exigir que el botón estuviera visible y se repitieron las tres pruebas con éxito sobre el mismo APK de aplicación.
- Hubo alertas recurrentes de System UI sin respuesta durante el arranque/instalación e inspección; se recuperó con Esperar. El emulador también sufrió interrupciones por memoria al compilar simultáneamente y no pudo restaurar un snapshot; se separaron compilación y ejecución. No se extrapola estabilidad ni rendimiento de este entorno.
- No se validaron CRUD de contactos, importación VCF, historial poblado, bloqueo, doble SIM ni conferencia en hardware. Las pantallas vacías no equivalen a validar esas operaciones.
- La inspección visual final no se completó por bloqueos recurrentes de System UI del emulador. No se declara QA visual final aprobada. La prueba de llamada descrita fue anterior a los ajustes visuales; la integración Telecom no cambió, pero no equivale a repetir esa prueba en el APK final. Ver VISUAL_QA.md. No se verificaron TalkBack, contraste medido, todas las orientaciones ni los modos de tema.
