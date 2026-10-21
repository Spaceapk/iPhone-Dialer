# Arquitectura y decisiones

## Capas actuales
Un módulo `app` con paquetes separados: `domain` (búsqueda/validación sin Android), `data` (proveedores Android, Room, DataStore), `telephony` (adaptadores Telecom) y `ui` (Compose/MVVM). Hilt inyecta dependencias; ViewModel/StateFlow exponen estados y Coroutines usan IO para proveedores. La separación por paquetes permite extraer módulos después. **No se afirma una Clean Architecture completa**: los contratos de repositorio aún no están separados de todas las implementaciones y parte de la coordinación de permisos reside en Compose.

- Android Telecom, no una ConnectionService propia, gestiona la telefonía del operador.
- RoleManager pide `ROLE_DIALER`; `InCallService` implementa UI entrante/en curso y `ACTION_DIAL` sirve entradas del sistema.
- `CallStore` conserva únicamente referencias a las llamadas entregadas por Telecom durante la sesión; elimina callbacks al retirar llamadas y al destruir el servicio. No fabrica estados telefónicos ni historial.
- La UI no marca una llamada activa hasta recibir ese estado del framework.
- Android 14+: endpoints de audio y manejo explícito del resultado de cambio; Android 10–13: API de rutas compatible. Bluetooth y auriculares dependen de los endpoints ofrecidos por el sistema, sin escanear dispositivos.
- No se declara `IN_CALL_SERVICE_RINGING`: se mantiene el timbre bajo Telecom; esto requiere validación por OEM. Canal entrante de alta importancia silencioso para evitar duplicar el timbre; CallStyle y PendingIntents explícitos/inmutables.
- Cada llamada tiene ID interno y notificación independiente. Se omiten hijos de conferencia del listado principal. Conferencia y espera se habilitan solo cuando Telecom anuncia capacidad; hay selección de las otras llamadas y reanudación de llamadas en espera.
- Emergencias: no filtrar números ni usar una lista regional propia; `placeCall` permite el traspaso oficial al marcador preinstalado. No probado con un servicio real.
- Contactos y llamadas no se copian a Room. Room guarda asignaciones locales de marcación rápida. DataStore guarda apariencia y preferencias; sin secretos.
- ContentObservers invalidan lecturas; se consulta nuevamente al reanudar. Si el usuario revoca permisos se vacía el estado correspondiente.
- El editor de contactos del sistema proporciona validación de cuentas, sincronización y edición de campos; no se implementa un motor de sincronización.
- No se necesita `READ_PHONE_NUMBERS`. Los permisos de historial solo se solicitan tras el rol.
- No se solicita Internet, micrófono, acceso a grabaciones, almacenamiento general ni permisos privilegiados.

## Decisiones visuales
Superficies flotantes translúcidas, acento verde predeterminado, tipografía nativa, fotografía/monograma real y controles circulares. Contraste de texto sobre superficies, estados por texto e icono además de color. Las transparencias se pueden desactivar. La implementación es **inspirada** en Liquid Glass: no reproduce el motor de refracción/desenfoque de Apple, ni incluye recursos de Apple. No hay imágenes decorativas descargadas.

## Referencias oficiales consultadas
- https://developer.android.com/reference/android/telecom/InCallService
- https://developer.android.com/guide/topics/permissions/default-handlers
- https://developer.android.com/develop/connectivity/telecom
- https://support.google.com/googleplay/android-developer/answer/16558241

El comportamiento de Android y políticas de tiendas cambian; revisar las referencias al publicar. No confundir los ejemplos de llamadas VoIP autogestionadas/ConnectionService con un reemplazo de la interfaz del marcador de SIM.
