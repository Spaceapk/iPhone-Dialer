# Pruebas · 1.0.0-alpha03

Estado: alfa con fondo fotográfico; binario no instalado en el HOT 40i ni en un emulador en esta revisión.

## Ejecución
`testDebugUnitTest lintRelease assembleRelease bundleRelease`: BUILD SUCCESSFUL. APK y AAB release R8 firmados. Certificados comparados entre sí y con alpha02: idénticos. Paquete y versión leídos del APK: com.pablormlopez.iphonedialer, code 3, 1.0.0-alpha03.

37 pruebas JVM sin fallos: SearchTest 20, CallNotificationRegressionTest 5, DiagnosticsPrivacyTest 2, ClassicUiTest 3, CallPhotoTest 4, CallPhotoUiTest 3. Release Lint: {'Warning': 18}, sin errores fatales. No se ocultaron advertencias.

La primera ejecución tuvo un fallo: el modo gráfico heredado de Robolectric simulaba una imagen al abrir un proveedor inexistente. Se configuraron los tests del procesador con gráficos nativos, como los de UI, y se repitió toda la suite satisfactoriamente. No se declara que todos los intentos fueran exitosos.

## Nuevas comprobaciones
- API 29/Robolectric: muestreo acotado incluso con dimensiones impares, preservación de color uniforme, suavizado sin modificar el original y fallback de URI no soportada/proveedor inexistente. No se requiere RenderEffect ni API de desenfoque Android 12.
- API 33/Robolectric/Compose nativo: InCallScreen real conectado a proveedor y estado de llamadas DE PRUEBA; foto cargada antes de captura, controles visibles/accionables, entrante con Contestar/Rechazar y alternativa sin foto. No fueron llamadas reales, ni un operador ni Telecom activos.
- Todas las pruebas existentes de notificación y privacidad se conservan. Las acciones reales de CallStore/Telecom no se modificaron.

## Revisión visual
Se abrieron individualmente tres renderizados de 393×852dp: activa con foto, entrante con foto y sin foto. Textos/controles nítidos, sin solapamientos, botones sólidos, fondo continuo con sombra gradual y alternativa negra. La referencia fotográfica fue un recorte privado de la captura facilitada por el usuario, con nombre/número ficticios de prueba. Esa referencia y esas capturas personales NO se incorporan al APK ni al repositorio; las pruebas portables utilizan una imagen neutra si no se proporciona fixture privado.

Contraste calculado: blanco/fondo máximo 666666 5,74:1; texto secundario F0F0F0 sobre ese fondo 5,04:1; blanco/rojo C5221F 5,80:1; blanco/verde16803C 5,02:1; blanco/superficie29292B 14,52:1; icono deshabilitado9D9DA3/superficie29292B 5,38:1. No equivale a auditoría de accesibilidad completa.

## Pendiente de hardware
No hay llamadas nuevas probadas en teléfono, ni validación del binario R8, memoria/consumo en HOT 40i, rotación, texto grande, pantalla bloqueada, Bluetooth, múltiples llamadas o permisos revocados en medio de una llamada. La lectura con permisos denegados tiene alternativa negra pero se debe validar en el dispositivo. No se confirma que otros cierres del fabricante estén resueltos.

La foto se decodifica hasta 960px y se filtra en Dispatchers.Default; el temporizador no vuelve a ejecutar el filtro cada segundo. No se persiste ni se envía la foto. El recorte depende de la relación de aspecto.

Evidencias XML y firmas: evidence/. Mapa exacto de esta compilación: alpha03-r8-mapping.txt.gz. Historial alpha02 archivado en history/alpha02; no confundir con la validación actual.
