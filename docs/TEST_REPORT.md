# Informe de validación · 1.0.0-alpha02

Estado: candidato de código fuente, no APK entregada ni corrección confirmada en dispositivo.

## Ejecución actual
- `:app:testDebugUnitTest :app:lintDebug`: BUILD SUCCESSFUL. El código Kotlin de la variante debug y sus pruebas compilan.
- 30 pruebas JVM satisfactorias: SearchTest 20, CallNotificationRegressionTest 5, DiagnosticsPrivacyTest 2, ClassicUiTest 3. XML en `evidence/`.
- Lint: 0 errores, 17 advertencias pendientes de revisión. No equivale a auditoría de seguridad ni certificación.
- La primera ejecución tuvo dos fallos de captura PixelCopy por timeout. Se cambió la captura a dibujo nativo de la View en Robolectric y se volvió a ejecutar toda la suite, que finalizó satisfactoriamente.
- Revisión estructural: 10 comprobaciones de manifiesto/configuración, ausencia de archivos de firma privada y `git diff --check` satisfactorios al preparar la entrega.
- La barrera `validateSigningDebug` rechaza intencionadamente empaquetar sin una clave existente configurada. Se comprobó que no creó una clave debug de reemplazo.

## Alcance de las pruebas
Las cinco pruebas de notificaciones comprueban el contrato relevante de Android 13 frente a la condición de AOSP documentada en ALPHA02_FIX.md: construcción anterior inválida; saliente nueva; llamada activa; entrante con pantalla completa; entrante sin pantalla completa. Son pruebas JVM/Robolectric API 33, no una ejecución de system_server ni una llamada de operador real.

Las dos pruebas de diagnóstico comprueban que no se incluyen mensajes de excepción con información personal y que el contenido queda limitado. No prueban ANR ni cierre forzado por el fabricante.

Las tres pruebas Compose ejercitan el teclado, la entrada de dígitos y los temas. Las imágenes son renderizados Android nativos mediante Robolectric con un contenedor de pruebas, no capturas del HOT 40i ni del flujo telefónico real. No se generó una interfaz web sustitutiva.

## Revisión visual
Se abrieron individualmente las capturas clara y oscura. Teclas 0–9, asterisco y almohadilla, acción de llamada y cinco pestañas visibles, sin solapamiento a 393 × 852 dp. Fondos sólidos, sin desenfoque ni paneles de cristal. Botón gris deshabilitado cuando el número está vacío. Queda espacio vertical amplio: es una elección conservadora para mantener visibles todos los controles, no una validación de todas las resoluciones.

Contraste calculado de cuatro pares: azul0062CC/blanco 5,80:1; secundario636366/fondoF2F2F7 5,37:1; blanco/verde16803C 5,02:1; secundarioB2B2B8/superficie1C1C1E 8,06:1. No se ha realizado una auditoría completa TalkBack/escala de fuentes.

## Pendiente obligatorio en hardware
No se ha instalado alpha02 en el HOT 40i Android 13. No se han probado esta revisión con llamadas reales entrantes/salientes, pantalla bloqueada, Bluetooth, doble SIM, reinicio ni variaciones del fabricante. No hay Logcat del cierre original. El defecto concreto corregido es compatible con el síntoma, pero no se afirma que sea su única causa.

Los resultados anteriores de alpha01 están archivados en `history/alpha01/` y no se atribuyen a esta revisión.
