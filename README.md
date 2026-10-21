# iPhone Dialer · Android

**Estado: desarrollo alfa, no versión 1.0.0 estable.** Aplicación Kotlin/Compose nativa: no WebView, datos ficticios ni simulación de llamadas. Las acciones de llamada utilizan Android Telecom y los contactos/historial provienen de los proveedores del dispositivo. Que una ruta esté implementada no significa que se haya validado en hardware real.

## Identidad
- Identificador propuesto para producción: `com.pablormlopez.iphonedialer` (confirmar antes de publicar).
- APK de evaluación: `com.pablormlopez.iphonedialer.debug`.
- Versión actual: `1.0.0-alpha01` (el APK añade `-debug`).
- Android 10/API 29 o posterior, compile/target SDK 36, Java 17.
- Recursos originales; no afiliada a Apple. Revisar el nombre comercial y las marcas antes de distribuir.

## Qué contiene
- Cinco pestañas: Favoritos, Recientes, Contactos, Teclado y Buzón.
- Solicitud oficial del rol de marcador predeterminado; permisos contextuales.
- `TelecomManager.placeCall`, `InCallService`, interfaz entrante/en curso y notificaciones con acciones.
- Contestar/rechazar/finalizar, silencio, DTMF, espera y conferencia condicionadas a las capacidades comunicadas por Telecom.
- Selección de salida de audio: API de endpoints en Android 14+; compatibilidad anterior mediante rutas de audio.
- Lectura/búsqueda T9 de contactos, fotografías, favoritos, edición/creación con el editor del sistema, eliminación confirmada.
- Lectura, búsqueda, filtros, agrupación y eliminación confirmada del historial.
- Exportación VCF con selector de destino e importación delegada al gestor de contactos instalado; compartir vCard.
- Marcación rápida local 2–9, accesos directos fijados si el launcher los admite.
- Lista de bloqueo oficial de Android cuando el rol/perfil lo autoriza; detección local de candidatos duplicados por número (sin fusión automática).
- Tema del sistema/claro/oscuro, tres acentos, transparencia, tamaño adicional de texto, vibración, animaciones y pestaña inicial persistentes.
- Solicitud opcional de `READ_PHONE_STATE` para preguntar qué cuenta/SIM usar en cada llamada cuando hay varias.

**Consulta `docs/LIMITATIONS.md` y `docs/TEST_REPORT.md` antes de instalar o usar como teléfono predeterminado.** No es todavía el cumplimiento íntegro del prompt maestro. No sustituir el marcador de un teléfono crítico sin pruebas adicionales.

## Compilar
1. Instalar Android Studio o Java 17 + SDK de Android.
2. Instalar `platforms;android-36` y `build-tools;36.0.0`; aceptar los términos del SDK.
3. Configurar `ANDROID_HOME`/`ANDROID_SDK_ROOT` o `local.properties` con `sdk.dir=...`.
4. Ejecutar:

```sh
./gradlew testDebugUnitTest lintDebug assembleDebug
# Con dispositivo/emulador autorizado conectado:
./gradlew connectedDebugAndroidTest
```

APK: `app/build/outputs/apk/debug/app-debug.apk`.

## Instalar una compilación de evaluación
- Transferir el APK al teléfono e instalarlo permitiendo esa fuente; o ejecutar `adb install -r app-debug.apk` en tu ordenador.
- Abrir la app; elegir «Usar como teléfono» y confirmar en el diálogo de Android.
- Autorizar contactos e historial solo al abrir esas funciones; habilitar notificaciones de llamadas en Configuración. Android 14+ puede requerir autorización de pantalla completa.
- Para volver al marcador anterior: Ajustes de Android → Aplicaciones → Aplicaciones predeterminadas → Teléfono.
- No realizar llamadas de emergencia como prueba. Verificar el mecanismo mediante revisión y procedimientos autorizados, nunca llamando a un servicio real sin necesidad.

## Firma y futuras actualizaciones
El APK debug usa la identidad de desarrollo del entorno y **no** es una entrega firmada para producción. No se incluye esa clave privada ni se garantiza actualizar este APK desde otro entorno. Para la siguiente evaluación, puede ser necesario desinstalar la alfa (se perderán sus preferencias/teclas rápidas, no la agenda del sistema).

Para producción se debe aprovisionar **una sola clave permanente**, mantenerla fuera del repositorio y custodiar dos copias cifradas con recuperación probada. No generar otra para cada versión. Variables protegidas requeridas: `ANDROID_KEYSTORE_PATH`, `ANDROID_KEYSTORE_PASSWORD`, `ANDROID_KEY_ALIAS`, `ANDROID_KEY_PASSWORD`. Usar un gestor de secretos/variables de archivo; no escribir contraseñas en comandos registrados, README ni Git.

```sh
bash scripts/require-signing.sh
./gradlew testReleaseUnitTest lintRelease assembleRelease bundleRelease
```

`require-signing.sh` falla si falta la identidad. El build local sin estas variables puede producir artefactos release sin firma; **no** son instalables/publicables como entrega final. Verificar con `apksigner verify --verbose` y registrar huella pública del certificado, SHA-256 del APK, `versionCode`, tag y notas de cada versión. Mantener el mismo identificador y certificado; incrementar `versionCode`.

## GitLab / Codemagic / respaldos
Los archivos `.gitlab-ci.yml` y `codemagic.yaml` están preparados, **no ejecutados en esas plataformas**. Configurar runners, repositorio remoto y credenciales con el propietario. En Codemagic registrar la referencia de firma `iphonedialer_release`. No hay conexión a GitLab, Codemagic ni Google Drive en esta entrega. No se ha subido nada a esos servicios.

Antes de lanzar 1.0.0: completar pendientes, probar dispositivos/OEM, pasar lint/test y revisar política de permisos de Google Play, aviso de privacidad, accesibilidad, marca y seguridad. Conservar artefactos en CI y copiar código/documentación a Drive solo mediante una conexión autorizada. Las claves requieren un respaldo cifrado separado, no el ZIP de fuentes.

## Restaurar el repositorio local incluido
El paquete de entrega contiene un `repository.git.bundle` con la rama `main` y su historial local. Para restaurarlo en otra carpeta:

```sh
git clone repository.git.bundle iphone-dialer-restored
cd iphone-dialer-restored
# Después de crear un proyecto privado en GitLab y autenticar de forma segura:
git remote add gitlab <URL_DEL_REPOSITORIO_AUTORIZADO>
git push gitlab main
```

No se ha creado ni publicado un repositorio remoto. El nombre `main` no implica que esta alfa sea apta para producción.
