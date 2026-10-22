# Alojamiento del proyecto en GitHub

Repositorio público autorizado: https://github.com/Spaceapk/iPhone-Dialer

La rama main conserva código fuente, documentación e historial. El tag v1.0.0-alpha03 identifica el código de la versión instalada; los cambios posteriores de documentación no alteran el APK ya compilado. La versión se publica como prerelease/alfa, no estable.

## Descargas de la entrega
https://github.com/Spaceapk/iPhone-Dialer/releases/tag/v1.0.0-alpha03

- iPhoneDialer-FotoContacto-alpha03.apk: instalación Android.
- iPhoneDialer-alpha03.aab: distribución posterior, no instalación directa.
- iPhoneDialer-codigo-alpha03.zip: respaldo editable con Git bundle.
- SHA256SUMS.txt: comprobación de integridad.

No se suben claves privadas, contraseñas, recuperación confidencial ni fotografía personal de referencia. El certificado público dentro del APK y de SIGNING.md no es la clave privada.

## Mantenimiento
No reconstruir ni volver a firmar alpha03 solo por alojarla: se conserva exactamente la APK ya entregada. Para futuras versiones: misma clave permanente y paquete, versionCode creciente; pruebas, notas y nueva Release. No sobrescribir una versión publicada con un binario diferente bajo el mismo nombre. No hay pipeline de GitHub Actions de compilación/firma configurado en esta entrega, ni secretos de firma almacenados en GitHub. GitLab/Codemagic siguen sin publicar ni ejecutar.

Conservar copias propias del código y de la clave. Un repositorio público permite descargar, pero no reemplaza un respaldo privado de firma ni garantiza disponibilidad perpetua.
