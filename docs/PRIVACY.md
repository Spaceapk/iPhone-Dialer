# Política de privacidad · borrador para evaluación local

La aplicación consulta contactos e historial en el dispositivo cuando el usuario lo autoriza. No solicita permiso de Internet, no incorpora publicidad ni SDK de analítica y no envía agenda ni registros a servidores de la aplicación. Android/operador gestionan la telefonía, sincronización de cuentas y datos del sistema según sus propias políticas.

Los nombres y números asignados a teclas rápidas se guardan en una base Room dentro del espacio privado de la aplicación, protegido por el aislamiento y cifrado del dispositivo cuando estén disponibles. Las preferencias se guardan en DataStore. No existe cifrado adicional de base de datos ni una promesa de protección frente a un dispositivo comprometido. Se deshabilita el backup de datos de la app.

Compartir/exportar contactos entrega los datos seleccionados al destino que elija el usuario. El archivo VCF exportado contiene datos personales y puede quedar fuera de la protección de la app; conservarlo o borrarlo es responsabilidad del usuario. Editar o eliminar contactos puede sincronizar esos cambios mediante cuentas ya configuradas en Android. Eliminar registros modifica el proveedor del sistema tras confirmación.

Se pueden revocar permisos o cambiar de app de teléfono desde Ajustes de Android. Desinstalar borra preferencias y teclas rápidas de la app, no borra automáticamente la agenda, el historial ni los VCF exportados.

No se graban ni transcriben llamadas. No hay identificación remota de desconocidos. El bloqueo se delega al proveedor autorizado de Android.

Antes de publicación: el titular debe añadir identidad legal, canal de contacto, jurisdicción, fecha de vigencia y revisión de distribución. Este borrador no es un aviso legal definitivo.

## Diagnóstico local en alpha02
Puede guardarse un último informe de error en el almacenamiento privado de la aplicación. Contiene versión, API, fabricante/modelo, categoría técnica constante y clases/métodos/líneas de la excepción; se omiten sus mensajes y los números/nombres de llamadas. Es acotado y sustituye al informe anterior. No se envía automáticamente. El usuario puede compartirlo mediante el selector de Android o borrarlo desde Configuración. No es una garantía de detectar cierres del sistema, ANR o fallos nativos.
