# alpha04 — primera revisión, no entrega certificada

## Auditoría inicial
GitHub inspeccionado: main en fc6383c, única rama; sin PR ni workflows Actions. Última release v1.0.0-alpha03. Se leyeron README, arquitectura, limitaciones, firma, configuración GitLab/Codemagic y código/pruebas. No se encontró AGENTS.md. Árbol inicial limpio.

Identidad conservada: com.pablormlopez.iphonedialer; versionCode 3 → 4, versionName 1.0.0-alpha04; minSdk 29, target/compileSdk 36. Debug conserva sufijo .debug.

## Cambios
- Teclado: Guardar número y Copiar. Recientes comparte el selector crear/añadir/copiar.
- ACTION_INSERT y ACTION_INSERT_OR_EDIT delegan cuenta, almacenamiento y escritura al editor oficial; no hay agenda paralela. Retorno del editor refresca la agenda. Cancelar no modifica el número del teclado, mantenido mediante rememberSaveable.
- Aviso no bloqueante de posible duplicado por dígitos exactos si la agenda está autorizada/cargada. No normaliza E.164 ni fusiona contactos.
- Pulsación prolongada de borrar limpia el número completo. Se conservan 0 → + y marcación rápida.
- Háptica usa KEYBOARD_TAP del sistema sin forzar vibración contra sus ajustes. Se desactiva la háptica automática de combinedClickable de las teclas para evitar duplicación y respetar la preferencia de la app.
- Exportación obtiene una instantánea actual en IO; conceder permiso abre el selector pendiente.
- Al reanudar sin permiso se cierran fichas/selección de contacto. Pérdida del rol limpia bloqueados cargados.
- Dos regresiones Compose añadidas: guardar/copiar sin alterar número y borrar todo manteniendo pulsado.
- Actions verifica JVM, lint y release sin firma. No publica releases ni genera claves.

## Verificación local y bloqueos
Java inicialmente ausente; instalado OpenJDK 17 con autorización. java -version funciona. Gradle 8.11.1 descargado. Se corrigió el directorio Gradle mediante GRADLE_USER_HOME local y el temporal con -Djava.io.tmpdir. Un intento posterior agotó 120 s antes de producir resultados de tareas; otro intento en segundo plano tampoco produjo resultados de tareas y se detuvo. Se añade CI Linux para continuar la verificación sin asumir compatibilidad del SDK nativo con Android ARM. No hay resultados nuevos aprobados de JVM, Compose, lint ni APK. Las pruebas se escribieron antes del cambio, pero el entorno impidió obtener un rojo funcional; no se afirma ciclo TDD completado.

No se ha aportado/verificado la clave original; no se genera ni sustituye. Sin APK firmado no hay entrega instalable ni actualización verificada. El artefacto unsigned de CI, si compila, NO se instala directamente. Custodia/fingerprint histórico en SIGNING.md sigue referido exclusivamente a alpha03.

## Pendientes obligatorios
- Compilación y compatibilidad de API Compose; pruebas de intents/editor real, cuenta sincronizada, cancelación y recreación.
- Háptica y tacto en hardware, accesibilidad, pantallas pequeñas, animaciones del sistema y pulsaciones rápidas. No se han medido latencia, memoria, ANR ni FPS; no se promete mejora medida de rendimiento.
- Denegación permanente y recuperación desde ajustes; acciones de permisos resistentes a recreación.
- Validar llamadas entrantes/salientes/perdidas, emergencia sin cambios, bloqueo de pantalla, audio/Bluetooth, espera, DTMF y doble SIM con operador real. No se ejecutaron llamadas físicas.
- Mantener marcador original disponible. Esta revisión no completa la matriz de aceptación ni autoriza una release estable.

## Instalación futura
Reutilizar exactamente la clave permanente de alpha03 y verificar su certificado antes de instalar encima, sin desinstalar. No usar claves nuevas. El paquete debug es otra aplicación. No hay instrucciones de instalación de alpha04 hasta disponer de APK firmado y comprobado.

## Corrección alpha05: estabilidad del teclado (versionCode 5)
Causa raíz del movimiento durante la escritura, en `KeypadScreen`:
- Las coincidencias de contactos aparecían/desaparecían sobre el número dentro de la misma columna desplazable, desplazando toda la rejilla en cada pulsación. Ahora ocupan un espacio reservado de altura fija (96.dp) mientras hay número marcado.
- La fila del número cambiaba de altura al saltar a dos líneas o cambiar de fuente. Ahora es de altura constante (52.dp), una sola línea con fuente adaptativa (30/24/20.sp según longitud).
- El tamaño de tecla se derivaba de la altura de ventana en vivo; cambios transitorios (p. ej. el IME al volver de otra pestaña) redimensionaban las teclas. Ahora se usa la mayor altura observada como referencia estable.
- El manifiesto no declaraba `windowSoftInputMode`; el sistema podía aplicar `adjustPan` y desplazar toda la ventana. Ahora `MainActivity` usa `adjustResize`: redimensionamiento determinista y animación natural del teclado virtual.
Sin cambios de diseño visual, sin funciones eliminadas, sin retrasos artificiales, misma identidad de firma permanente. Verificado: pruebas JVM + lintRelease SUCCESS; falta validación física.
