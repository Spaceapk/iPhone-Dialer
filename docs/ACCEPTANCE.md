# Matriz de aceptación de la futura 1.0.0

Leyenda: ninguna casilla representa una prueba ya aprobada. Los resultados ejecutados están exclusivamente en TEST_REPORT.md.

| Área | Procedimiento mínimo | Criterio de aceptación |
|---|---|---|
| Instalación/apertura | Instalar limpio y actualizar con misma clave | Abre sin cierres; versión/certificado correctos |
| Rol | Aceptar y rechazar ROLE_DIALER; cambiar a otra app | Respeta decisión; no pide historial sin rol |
| Permisos | Aceptar, denegar, denegar permanente y revocar | Explicación contextual; pantalla degradada, sin cierre |
| Contactos | Crear, editar foto/campos/cuenta y eliminar con confirmación | Cambios reales en ContactsContract; cancelación sin mutación |
| Búsqueda | Acentos, T9, números internacionales, vacío | Resultados correctos, respuesta adecuada con agenda grande |
| Marcación | Número normal y cuenta predeterminada | Sesión real del framework; sin simulación |
| Recepción | App abierta, fondo, cerrada, bloqueo y DND | Aviso y timbre correctos; contesta/rechaza de verdad |
| En llamada | Silencio, DTMF, espera, segunda llamada, conferencia | Cambios confirmados por Telecom y operador |
| Historial | Entrante/saliente/perdida/rechazada y selección múltiple | Refleja registro del sistema; borrado solo confirmado |
| Favoritos | Marcar/desmarcar, contactos sin/múltiples números | Persiste en proveedor; selección segura |
| Doble SIM | Dos cuentas, una sin servicio, preferencias del sistema | Selector y ruta correctos; no usa cuenta equivocada |
| Temas | Claro, oscuro, sistema, transparencias desactivadas | Contraste legible; persistencia tras reinicio |
| Rendimiento | Inicio frío, agenda/historial masivos, llamadas largas | Sin ANR/fugas; presupuesto medido antes de fijarlo |
| Rotación | Girar en teclado, contacto, diálogo y llamada | Conserva estado; una sola sesión telefónica |
| Denegaciones | Quitar rol/permisos mientras está abierta | No acceso indebido ni datos obsoletos expuestos |
| Reinicio | Reiniciar teléfono con configuración guardada | Preferencias retenidas; recepción correcta tras reinicio |
| Accesibilidad | TalkBack, fuente grande, teclado y contraste | Acciones con nombre; sin recortes críticos; AA verificado |
| Privacidad | Revisar dependencias, tráfico, backup y exportación | Sin envíos ocultos, claves en Git ni copia no autorizada |

## Instrucciones de prueba segura
- Usar un teléfono de evaluación, copia previa de contactos y números propios autorizados.
- No llamar a emergencias para probar; emplear procedimientos certificados del fabricante/operador cuando sea necesario.
- Emulador GSM: documentar explícitamente que es simulación del módem del emulador integrada en Telecom, no llamada a una red real.
- No marcar Bluetooth, doble SIM o conferencia como aprobados sin el hardware/servicio correspondientes.
- Anotar modelo, API, fabricante, operador, versión y certificado de la build en cada resultado.
