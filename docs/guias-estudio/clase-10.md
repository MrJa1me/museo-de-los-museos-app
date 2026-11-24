# Clase 10: Firma de APK, GitHub, Trello y diagnóstico

**Objetivo:** mostrar el APK firmado y su llave (paso 7) y tu colaboración en GitHub y Trello (paso 8).

## APK firmado (diapositiva «Generación de APK firmado en Android Studio»)

1. **Build → Generate Signed App Bundle / APK → APK**.
2. **Create new...**: ruta del `.jks`, contraseña, alias `museo`, validez en años y datos del certificado.
3. Variante **release** → **Create**.
4. En el proyecto es automático: `signingConfigs` en `app/build.gradle.kts` lee `app/keystore/keystore.properties`, y `./gradlew assembleRelease` genera el APK firmado.
5. Verificación: `apksigner verify --print-certs entrega/museo-de-los-museos-v1.0-release.apk`.

- **Por qué firmar**: Android solo instala APK firmados; la firma identifica al autor y cada actualización debe firmarse con la **misma** llave.
- **Si se pierde el `.jks`**: no se pueden publicar actualizaciones de la misma app.
- **Debug vs release**: debug usa una llave de depuración automática; release usa nuestra llave y activa la optimización (R8).
- El `.jks` y sus contraseñas se subieron porque la pauta lo exige; en una app real **nunca** se suben.

## GitHub

- Cada integrante trabaja en su rama y entra a `main` por **Pull Request**.
- Tus commits: `git log --author="Alanis" --oneline`.
- Comandos: `git add` (preparar), `git commit -m "..."` (registrar), `git push` (subir), `git pull` (traer).
- **Conflicto de merge**: dos ramas cambiaron las mismas líneas; se resuelve editando el archivo, `git add` y `git commit`.
- Mensajes de commit con prefijo: `feat(backend): ...`, `feat(app): ...`, `test(app): ...`, `docs: ...`.

## Trello

Tarjetas sugeridas para tu parte (columnas Pendiente → En desarrollo → Terminado):

1. Microservicio ms-productos (entidad, endpoints, validación)
2. Integrar catálogo con ms-productos (Retrofit + Room)
3. Validar stock con el backend al confirmar el carrito
4. Pruebas unitarias del catálogo (MockK + Mockito)
5. Documentación y guías de estudio

## Práctica (15 min)

1. Ejecuta `./gradlew assembleRelease` y encuentra el APK en `app/build/outputs/apk/release/`.
2. Abre GitHub en tu rama y muestra tus commits y tu Pull Request.

## Preguntas del quiz de esta clase (sección 10)

El quiz del Control 3 saca 7 preguntas al azar de estas 100, textuales. Lee la pregunta, tapa la respuesta y contesta.

**091. ¿Por qué se firma digitalmente una APK Android?**  
✅ B) Para que el sistema pueda verificar identidad de firma y continuidad de actualizaciones — La firma establece una identidad criptográfica necesaria para instalación y actualizaciones.

**092. ¿Qué contiene habitualmente un archivo `.jks` usado para firmar una APK?**  
✅ A) Un almacén de claves con una clave privada y certificado asociado — El keystore resguarda material criptográfico usado por el proceso de firma.

**093. ¿Qué ocurre si una actualización de una app no está firmada con una identidad de firma compatible con la instalada?**  
✅ C) Android rechaza normalmente la actualización como incompatible — Las actualizaciones deben mantener continuidad de identidad de firma, salvo mecanismos autorizados de rotación.

**094. ¿Cuál es una diferencia técnica habitual entre build `debug` y `release`?**  
✅ D) Debug suele usar clave de depuración y ajustes de desarrollo; release se prepara con configuración de distribución — Las variantes tienen configuraciones y credenciales distintas para desarrollo y distribución.

**095. ¿Qué precaución corresponde a una clave privada usada para firmar aplicaciones?**  
✅ D) Restringir su acceso y mantener copias de respaldo seguras — La pérdida o exposición de la clave compromete la continuidad y seguridad de publicación.

**096. ¿Qué comando crea un commit con los cambios ya preparados en el área de staging?**  
✅ B) `git commit -m "Implementa listado"` — `git commit` registra en el historial los cambios previamente añadidos con `git add`.

**097. ¿Qué ventaja tiene crear una rama Git para desarrollar una funcionalidad?**  
✅ D) Aislar cambios y facilitar su integración y revisión — Las ramas permiten trabajar en líneas de desarrollo independientes.

**098. ¿Qué significa un conflicto de merge en Git?**  
✅ C) Git no puede combinar automáticamente ciertos cambios y requiere resolverlos — El conflicto se resuelve editando el contenido y confirmando la combinación.

**099. ¿Qué evidencia técnica aporta un historial de commits bien escrito en GitHub?**  
✅ B) Permite rastrear cambios concretos, autores y evolución del código — Los commits documentan cambios versionados y facilitan revisión colaborativa.

**100. ¿Para qué sirven listas como “Pendiente”, “En desarrollo” y “Terminado” en Trello?**  
✅ A) Visualizar el estado de las tareas y coordinar su avance — Un tablero permite hacer seguimiento de trabajo, responsables y progreso.

---

[← Clase 9](clase-09.md) · [Índice](README.md)
