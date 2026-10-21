# Limitaciones y criterios pendientes para 1.0.0

Esta entrega es alfa. No debe anunciarse como producto completo, estable ni verificado para uso diario.

## No implementado o solo parcialmente implementado
- No hay widgets de favoritos; los accesos directos fijados sí tienen implementación.
- No hay buzón visual, escucha/gestión local de mensajes ni transcripción. Solo acceso al buzón telefónico del operador.
- No hay identificación remota de desconocidos, base de spam ni CallScreeningService propia. El bloqueo usa el proveedor oficial y su política; no puede prometerse bloqueo universal.
- Duplicados: candidatos por coincidencia exacta de dígitos, sin normalización regional E.164 ni fusión propia; no se combinan contactos automáticamente. No hay buscador por cuentas ni una pantalla propia de todas las direcciones/correos; se utiliza el editor de contactos de Android.
- No se implementan tonos por contacto desde esta UI. El proveedor puede conservar los configurados externamente; su reproducción depende del sistema/OEM.
- No hay reordenación personalizada de favoritos, preferencias persistentes de SIM propia, color dinámico Material You ni opciones adicionales de diseño de fichas. Se respetan las preferencias SIM del sistema; con permiso se muestra un selector por llamada.
- No hay un efecto de desenfoque/refracción Liquid Glass real: transparencia y superficies superpuestas nativas, degradadas a opacas si se pide.
- Interfaz de audio: no se proporciona video, RTT, respuestas de rechazo por SMS, continuidad con Android Auto/Wear ni interfaces especiales para llamadas gestionadas por otras apps. Debe ampliarse y probarse antes de afirmar soporte completo de ROLE_DIALER en todos los escenarios.
- No hay notificación propia de llamadas perdidas, proximitad/apagado automático de pantalla ni gestión de todas las interacciones con bloqueo de pantalla y llamadas simultáneas. La lista de llamadas y notificaciones se limpia al terminar; esto no sustituye un manejo completo de fallos del proceso.
- Recientes agrupa por número, tipo y día según formato local; la caché en memoria lee la agenda/historial completos. Grandes volúmenes requieren Paging, normalización y pruebas de memoria antes de producción.
- Las pruebas de UI incluidas son básicas. Aún no hay cobertura exhaustiva de proveedores, callbacks Telecom, rotación de llamadas ni degradación de permisos durante una llamada.

## Validaciones externas obligatorias
- Teléfono físico con SIM: llamada saliente/entrante, pantalla bloqueada, timbre, rechazo, perdida, espera, segunda llamada y conferencia.
- Android 10, versiones intermedias y versión actual; fabricantes distintos. No basta una compilación con SDK 36 para afirmar compatibilidad probada con todos.
- Bluetooth, auriculares, doble SIM, selección persistente del sistema, buzón y estados de operador.
- Revocación/auto-restablecimiento de permisos, pérdida del rol, restauración de proceso, reinicio, ahorro de batería, no molestar y restricciones de pantalla completa.
- TalkBack, contraste medido, escalado grande, pantallas pequeñas/grandes, orientación y reducción de animación a nivel del sistema.
- Perfil de rendimiento, consumo, estabilidad prolongada y auditoría de privacidad de dependencias.
- Política de Google Play (historial, rol, fullscreen, nombre/marcas), avisos de uso/coste, firma permanente y plan de recuperación.

## Entrega externa pendiente
No hay repositorio remoto publicado, pipeline remoto ejecutado, cuenta de Codemagic conectada, respaldo de Drive ni clave de producción. Los archivos locales de CI y Git no prueban que esos servicios estén funcionando. El APK debug sirve para evaluación; el identificador propuesto y la firma de producción deben confirmarse antes del primer lanzamiento estable.

## Revisión final de evaluación
La inspección visual manual completa de la compilación final no pudo completarse por bloqueos recurrentes de System UI del emulador. La regresión instrumentada de disposición sí aprobó; no equivale a una aprobación visual integral. El APK debug incluye dependencias de desarrollo y no está optimizado en tamaño/rendimiento como una release con R8. No usar esta alfa como marcador principal de un dispositivo crítico.
