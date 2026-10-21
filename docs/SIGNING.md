# Identidad de firma permanente autorizada

Se creó una nueva identidad tras autorización explícita del usuario, porque la clave alpha01 no estaba disponible. Esta clave no sustituye la identidad antigua para actualizaciones sobre alpha01.debug.

- Paquete definitivo de esta línea: `com.pablormlopez.iphonedialer`.
- Primera entrega: `1.0.0-alpha02`, versionCode 2, variante release optimizada R8. Sigue siendo una alfa.
- Certificado público SHA-256: `2fdf5f434cdb0fd0882c24b11ae72e61011888a8d3cd9157faf94336c01ea926`.
- APK SHA-256: `0abd7ba2daf484b0c3535818ca9bf490882c3368aa7791fb4172282abf1e4fc9`.
- AAB SHA-256: `615113870db7f49f6ac35b9f1f5d5b86c49b7c1b767f6a0fdbb3ab571c16cecd`.
- APK y AAB: firmas verificadas y certificado coincidente. Un certificado autofirmado de aplicación no acredita auditoría ni aprobación de Google Play.

## Custodia
Se entrega por separado un respaldo 7z cifrado AES-256 con nombres de archivos cifrados, y un documento confidencial de recuperación con contraseñas. Se probó extraer el respaldo y abrir el almacén restaurado. Nada de esto forma parte de Git ni del ZIP de fuentes.

El documento de recuperación no está cifrado: guardarlo en un gestor de contraseñas, separado del archivo 7z. Mantener dos copias seguras. Los archivos descargables NO equivalen a un respaldo confirmado en poder del usuario. No hay Drive, gestor de secretos ni CI remoto conectado; no depender del almacenamiento temporal del agente. Antes de una actualización futura, recuperar esta MISMA clave.

## Actualizaciones
Mismo applicationId, mismo certificado, versionCode mayor que 2. Restaurar el almacén en ubicación segura, cargar las cuatro variables ANDROID_KEYSTORE_* indicadas en README y ejecutar require-signing.sh. Nunca regenerar la clave ni añadir contraseñas/archivos privados al repositorio. Importar la misma identidad en Codemagic/GitLab mediante sus mecanismos de secretos.

El ZIP de fuentes permite recompilar, pero sin el respaldo privado no permite firmar actualizaciones compatibles. Si se pierde la clave, no prometer recuperación desde la APK ni generar otra silenciosamente.
