# Firma de alpha03 · misma identidad permanente

- applicationId: `com.pablormlopez.iphonedialer`.
- versionName: `1.0.0-alpha03`; versionCode: 3; variante release R8.
- Certificado público SHA-256: `2fdf5f434cdb0fd0882c24b11ae72e61011888a8d3cd9157faf94336c01ea926`.
- APK SHA-256: `45e59359350b17e351d576770ed1bc8c39c010e8f0275f536da7e8cbfc89c633`.
- AAB SHA-256: `a0b9ffc8cdd0b41bb80ecf498dfbdc49f94073286b7b607dcd6b5979ce1d6be4`.

`apksigner verify` satisfactorio; certificado comparado con el registro de alpha02: idéntico. Firma JAR del AAB verificada y mismo certificado. No se creó otra clave. Se puede instalar encima de alpha02 sin desinstalar. No actualiza alpha01.debug, que tenía otra identidad.

Conservar el respaldo cifrado y documento de recuperación entregados con alpha02; esta entrega no los reemplaza. No se incluyen claves ni contraseñas en Git ni en el ZIP de código. La custodia segura y el respaldo externo siguen a cargo del usuario. No depender del almacenamiento temporal del agente. Usar esa MISMA clave y paquete, y versionCode mayor que 3, para futuras actualizaciones. Configuración CI/variables: README.
