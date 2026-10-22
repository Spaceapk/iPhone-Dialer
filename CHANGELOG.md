# Changelog

## 1.0.0-alpha03 (alfa, fondo fotográfico)
- Foto local del contacto a pantalla completa en llamada entrante y en curso.
- Desenfoque suave de la imagen fuera del hilo principal, límite de 960 px y capa oscura de contraste.
- Controles nítidos, superficies sólidas y botón de finalizar rojo. Sin Liquid Glass.
- Fondo negro/avatares existentes si falta foto, permiso o decodificación.
- Misma identidad de firma y paquete de alpha02; versionCode 3.
- Sin cambios en las acciones Telecom, historial, agenda ni solicitudes de permisos.


## 1.0.0-alpha02 (alfa firmada, pendiente validación física)
- Corrige la construcción inválida de notificaciones CallStyle salientes en Android 12/13.
- Controla rechazos de notificación y añade informe técnico local sin datos de llamadas.
- Retira Liquid Glass y adopta superficies sólidas y estilo iOS clásico.
- Acota la carga de fotografías y adapta las barras del sistema al tema elegido.
- Añade regresión del contrato de Android 13, privacidad del informe y renderizado Compose.
- Bloquea empaquetado debug sin una firma existente, para no sustituir silenciosamente la identidad de alpha01.

- Tras autorización explícita, nueva instalación release firmada con identidad permanente, APK/AAB verificados y respaldo privado cifrado separado.

## 1.0.0-alpha01
- Primera base nativa Kotlin/Compose con Hilt, Telecom, ContactsContract, Room y DataStore.
- Pantallas de navegación, llamadas y administración local descritas en README.
- Pruebas iniciales, configuración de CI y documentación de limitaciones.
- No es la versión 1.0.0 estable solicitada; completar los criterios de aceptación antes de etiquetarla así.

- Correcciones de inspección: disposición de Favoritos/Contactos, botón de llamada fijo, teclado adaptable y acentos coherentes.
- Regresión instrumentada de disposición y repetición de las tres pruebas de UI.
