# iPhone Dialer · Android

**Estado: alpha02, corrección candidata y rediseño clásico; no validada en el HOT 40i y con APK release firmada tras autorización. Sigue pendiente validar llamadas en el teléfono.** Aplicación Kotlin/Compose nativa: no WebView, datos ficticios ni simulación de llamadas. Las acciones de llamada utilizan Android Telecom y los contactos/historial provienen de los proveedores del dispositivo. Que una ruta esté implementada no significa que se haya validado en hardware real.

## Identidad
- Identificador de esta nueva instalación: `com.pablormlopez.iphonedialer`.
- Alfa anterior/debug: `com.pablormlopez.iphonedialer.debug`; es otra instalación.
- Versión entregada: `1.0.0-alpha02`, versionCode 2, variante release (no equivale a versión estable).
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
- Tema del sistema/claro/oscuro, tres acentos, superficies sólidas, tamaño adicional de texto, vibración, animaciones y pestaña inicial persistentes.
- Solicitud opcional de `READ_PHONE_STATE` para preguntar qué cuenta/SIM usar en cada llamada cuando hay varias.

**Consulta `docs/LIMITATIONS.md` y `docs/TEST_REPORT.md` antes de instalar o usar como teléfono predeterminado.** No es todavía el cumplimiento íntegro del prompt maestro. No sustituir el marcador de un teléfono crítico sin pruebas adicionales.

## Compilar
1. Instalar Android Studio o Java 17 + SDK de Android.
2. Instalar `platforms;android-36` y `build-tools;36.0.0`; aceptar los términos del SDK.
3. Configurar `ANDROID_HOME`/`ANDROID_SDK_ROOT` o `local.properties` con `sdk.dir=...`.
4. Ejecutar:

```sh
./gradlew testDebugUnitTest lintDebug
# Con dispositivo/emulador autorizado conectado:
./gradlew connectedDebugAndroidTest
```

La compilación JVM y Lint no requieren una firma. Empaquetar una APK debug queda bloqueado si no se proporciona una identidad de evaluación **existente**; no se crea otra clave en silencio. Variables: `ANDROID_EVALUATION_KEYSTORE_PATH`, `ANDROID_EVALUATION_KEYSTORE_PASSWORD`, `ANDROID_EVALUATION_KEY_ALIAS`, `ANDROID_EVALUATION_KEY_PASSWORD`. La clave de la alpha01 no está en la entrega ni en este entorno y no se puede reconstruir desde su APK. Una clave nueva no permite actualizar la alpha01 instalada.

Producción sigue usando las variables `ANDROID_KEYSTORE_*` del apartado de firma. La identidad permanente ya se creó con autorización y se entrega respaldo cifrado separado. Ver `docs/SIGNING.md`; custodiar los archivos antes de futuras actualizaciones.

## Estado de instalación y seguridad
Se entrega una APK alpha02 release firmada, verificada y optimizada. Es una instalación nueva, no actualización sobre alpha01.debug. Ver `docs/INSTALLATION.md`. No reutilizar ni renombrar la APK alpha01 como si contuviera esta corrección.

Si la alpha01 se cierra al llamar, vuelve temporalmente al marcador original desde Ajustes de Android → Aplicaciones predeterminadas → Teléfono (los nombres pueden variar según el fabricante). No dependas de la alfa para llamadas importantes ni pruebes números de emergencia.

## Firma y futuras actualizaciones
La APK alpha01 usó una identidad de desarrollo que no está disponible en esta revisión; **no** era una entrega firmada para producción. No se incluye esa clave privada ni se garantiza actualizar este APK desde otro entorno. Para instalar con una identidad distinta, será necesario instalar otro identificador o desinstalar la alfa (se perderán sus preferencias/teclas rápidas, no la agenda del sistema).

Ya se aprovisionó **una sola clave permanente** con autorización. Se probó recuperar el respaldo; el usuario debe descargarlo y custodiar dos copias seguras fuera del repositorio. No generar otra para cada versión. Variables protegidas requeridas: `ANDROID_KEYSTORE_PATH`, `ANDROID_KEYSTORE_PASSWORD`, `ANDROID_KEY_ALIAS`, `ANDROID_KEY_PASSWORD`. Usar un gestor de secretos/variables de archivo; no escribir contraseñas en comandos registrados, README ni Git.

```sh
bash scripts/require-signing.sh
./gradlew testReleaseUnitTest lintRelease assembleRelease bundleRelease
```

`require-signing.sh` falla si falta la identidad. El build local sin estas variables puede producir artefactos release sin firma; **no** son instalables/publicables como entrega final. Verificar con `apksigner verify --verbose` y registrar huella pública del certificado, SHA-256 del APK, `versionCode`, tag y notas de cada versión. Mantener el mismo identificador y certificado; incrementar `versionCode`.

## GitLab / Codemagic / respaldos
Los archivos `.gitlab-ci.yml` y `codemagic.yaml` están preparados, **no ejecutados en esas plataformas**. Configurar runners, repositorio remoto y credenciales con el propietario. En Codemagic registrar la referencia de firma `iphonedialer_release`. No hay conexión a GitLab, Codemagic ni Google Drive en esta entrega. No se ha subido nada a esos servicios.

Antes de lanzar 1.0.0: completar pendientes, probar dispositivos/OEM, pasar lint/test y revisar política de permisos de Google Play, aviso de privacidad, accesibilidad, marca y seguridad. Conservar artefactos en CI y copiar código/documentación a Drive solo mediante una conexión autorizada. Las claves requieren el respaldo cifrado separado entregado, no el ZIP de fuentes. Los jobs debug requieren las variables ANDROID_EVALUATION_* de una identidad existente; sin ellas fallan deliberadamente, no generan otra clave.

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

## Cambios solicitados en alpha02
- Notificaciones salientes/en curso ordinarias con acción real Finalizar: se retira CallStyle de esa ruta porque el servicio está enlazado, no en primer plano.
- CallStyle entrante solo si se adjunta un full-screen intent autorizado; alternativa normal si está restringido. Construcción/publicación controlan rechazos de Android sin convertirlos en un cierre no controlado.
- Interfaz clásica: blanco/gris, azul de acción, teclado gris, botón de llamada verde y llamada en negro sólido. Sin Liquid Glass, gradientes ni paneles translúcidos.
- Informe técnico local compartido solo por acción explícita del usuario; no incluye mensajes de excepción, nombres ni números. No captura ANR, cierres nativos ni terminaciones forzadas del sistema.
- Pruebas nuevas de contrato de notificación Android 13 con Robolectric y renderizado Compose. No equivalen a probar una llamada real en el HOT 40i.

Ver `docs/ALPHA02_FIX.md` y el informe actual. Las evidencias de alpha01 quedaron archivadas en `docs/history/alpha01/`; no son resultados de alpha02.
