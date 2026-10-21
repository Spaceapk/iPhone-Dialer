# Entrega autorizada · 1.0.0-alpha02

## Artefactos
- APK Android instalable, variante release optimizada R8, firmada con la identidad permanente nueva autorizada.
- AAB firmado, para distribución posterior: no se instala directamente y no implica aprobación de ninguna tienda.
- Código fuente editable Kotlin/Compose, documentación, XML de pruebas, Lint, renderizados y Git bundle recuperable.
- Respaldo privado 7z/AES-256 y recuperación confidencial entregados por separado, fuera del código. Extracción y apertura del almacén restaurado comprobadas. Custodia final pendiente de descarga por el usuario; no hay respaldo externo automático.
- Identificador de esta línea: com.pablormlopez.iphonedialer; versionName 1.0.0-alpha02; versionCode 2. No es 1.0.0 estable ni cumplimiento completo del prompt maestro.

## Transición
La primera alfa tenía otro paquete .debug y otra firma no disponible. La nueva instalación tiene identidad permanente autorizada y puede coexistir, pero no actualiza automáticamente sus preferencias. Ver INSTALLATION.md para evitar confundir las dos.

## Validación
Compilación release y 30 pruebas JVM satisfactorias, firmas APK/AAB verificadas y certificados coincidentes. Release Lint sin errores fatales; advertencias en TEST_REPORT. No hay dispositivo conectado y no se probó el binario optimizado en el HOT 40i, por lo que no se garantiza resuelto el cierre. Ver TEST_REPORT, ALPHA02_FIX y LIMITATIONS.

## Actualizaciones
Reutilizar la MISMA clave y paquete; incrementar versionCode. Ver SIGNING.md. No se publicaron repositorios remotos ni se configuraron cuentas Codemagic/GitLab/Drive. Los archivos CI son configuración pendiente, no pipelines remotos verificados.
