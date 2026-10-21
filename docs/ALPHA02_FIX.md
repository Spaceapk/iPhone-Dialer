# Cierre al llamar · HOT 40i / Android 13

## Reporte del usuario
La interfaz se cierra al iniciar una llamada, aunque la llamada telefónica continúa. No hay todavía un log de ese teléfono, por lo que no se atribuye categóricamente el síntoma a una sola causa.

## Defecto concreto encontrado
En alpha01, `DialerInCallService.notifyCall` construye `CallStyle.forOngoingCall` para llamadas salientes/en curso. El servicio está enlazado por Telecom, pero no es un foreground service. Esa notificación tampoco incluye `fullScreenIntent`.

El código oficial Android 13 (`NotificationManagerService`, rama `android13-release`, líneas 6905–6912 consultadas) rechaza exactamente esa combinación con `IllegalArgumentException`. El código anterior solo atrapaba `SecurityException`, por lo que este rechazo podía cerrar el proceso. La llamada del operador, gestionada por Telecom fuera del proceso, puede continuar.

Fuente oficial:
https://android.googlesource.com/platform/frameworks/base/+/refs/heads/android13-release/services/core/java/com/android/server/notification/NotificationManagerService.java

## Cambio mínimo aplicado
- Constructor separado `CallNotificationFactory` para probar la notificación sin una llamada real.
- Saliente/en curso: notificación ordinaria persistente con PendingIntent privado real de Finalizar; sin CallStyle ni full-screen intent.
- Entrante: CallStyle solo con full-screen intent autorizado. Si no está disponible, aviso normal con Contestar/Rechazar.
- Se controla `IllegalArgumentException` alrededor de la construcción/publicación y se guarda un informe local; no se añaden permisos de foreground service ni flags privilegiados falsos.
- No se altera `TelecomManager.placeCall`, los números, la SIM elegida, la agenda ni el historial.

## Diagnóstico adicional
Un último informe técnico local registra clases y ubicaciones de excepciones, no sus mensajes, contactos, números ni datos de la llamada. Solo se comparte al pulsar el botón correspondiente. Es información voluntaria para confirmar un cierre si se repite después de instalar una revisión firmada. No existía en la alpha01 y no permite recuperar retrospectivamente su error.

## Diseño solicitado
Se retiró Liquid Glass, el gradiente y la preferencia de paneles translúcidos. Se usan grupos sólidos blancos/grises, acción azul, teclas grises y botón de llamada verde. La pantalla en llamada usa negro sólido. No se incorporan fuentes ni iconos propietarios de Apple.

## Qué falta para afirmar que está resuelto
- Validar la revisión firmada en el HOT 40i Android 13.
- Probar llamada saliente, entrante, respuesta, finalización y apertura desde notificación; después contactos con y sin foto, bloqueo de pantalla y doble SIM si existe.
- Si persiste: revisar el informe técnico voluntario o Logcat autorizado; no suponer que todos los cierres comparten causa.

## Bloqueo de firma
No está disponible la clave privada de la APK alpha01. Su certificado público no permite reconstruirla. Por tanto, no se entrega una APK alpha02 firmada con otra clave como si fuera una actualización compatible.

Antes de crear una nueva identidad permanente se requiere acordar su custodia y la transición. Instalar una aplicación con otro identificador o desinstalar la alfa son decisiones del usuario; no se realizan automáticamente. Desinstalar pierde preferencias/teclas rápidas locales, no elimina por sí mismo la agenda ni el historial del sistema. Las claves nunca deben entrar en Git.
