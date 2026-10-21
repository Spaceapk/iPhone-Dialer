# Entrega · 1.0.0-alpha02 · candidato

## Incluido
- Proyecto editable Kotlin/Compose y documentación actualizada.
- Historial Git recuperable mediante `repository.git.bundle` dentro del ZIP.
- Corrección del contrato de notificación de Android 13 y diagnóstico local voluntario.
- Interfaz opaca de inspiración iOS clásica, sin Liquid Glass.
- XML de 30 pruebas JVM satisfactorias, Lint y dos renderizados de interfaz.
- Identificador actual: `com.pablormlopez.iphonedialer`; versionCode 2.

## No incluido
No se entrega una nueva APK ni AAB. La clave privada de firma de alpha01 no está disponible en este entorno. No se creó otra ni se reutilizó la antigua APK bajo un nombre nuevo. Esta entrega no es la versión 1.0.0 de producción completa.

## Firma y transición
Android exige la identidad de firma original para actualizar sobre la instalación existente. El certificado de una APK no permite reconstruir su clave privada.

Si no se recupera esa clave, se necesita autorización para una nueva instalación con identidad de firma permanente. Antes de producirla debe acordarse custodia segura, respaldo y futura reutilización; nunca guardar claves o contraseñas en Git ni enviarlas en texto plano. Una instalación con otro identificador o la desinstalación de la alfa requieren decisión explícita. La desinstalación elimina preferencias locales, aunque no elimina por sí misma contactos o historial del proveedor del sistema.

Para empaquetado de evaluación se requieren las variables de una clave EXISTENTE indicadas en README; la validación evita crear automáticamente la clave debug estándar. Para distribución se usa la configuración de firma release existente con secretos de CI. No se han publicado repositorios remotos, configurado cuentas Codemagic ni subido respaldos a Drive.

## Siguiente validación
Con una APK firmada y autorizada: comprobar llamada saliente, entrante, respuesta, finalización y notificación en HOT 40i Android 13. Si persiste el cierre, compartir voluntariamente el informe técnico desde Configuración; alpha01 no tenía esa función. Ver TEST_REPORT y LIMITATIONS para el resto del alcance.

Mientras no se confirme estabilidad, utilizar el marcador original del teléfono para llamadas importantes.
