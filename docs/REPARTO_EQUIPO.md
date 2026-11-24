# Reparto de la Evaluación 4 en 4 Pull Requests (uno por integrante)

El trabajo nuevo (microservicio `ms-productos`, catálogo conectado, stock del carrito y documentación) está dividido en **4 partes**. Cada parte es un commit de la rama `ev4-alanis`. Cada integrante copia **su** parte con un comando, hace el commit **desde su computador y con su cuenta de GitHub** y abre su propio Pull Request. Así el historial muestra un aporte real de cada uno.

> No abran un Pull Request directo de `ev4-alanis`: esa rama es solo la fuente de las 4 partes.

## Quién hace qué (propuesta, se puede cambiar)

| Orden | Parte | Integrante sugerido | Commit fuente | Qué contiene | Qué explica en la defensa |
| --- | --- | --- | --- | --- | --- |
| 0 | PR #29 (ya existe) | Benjamín | — | ms-museos, ms-pedidos, API externa, APK | Lo que ya hizo |
| 1 | **Backend ms-productos** | Alan | `0a7f769c2d` | `microservicios/ms-productos/` completo + 29 pruebas | Clases 5, 6 y 7 |
| 2 | **Catálogo conectado** | Alanis | `b0313547d6` | Retrofit, repositorio remoto, catálogo/formulario/detalle + arreglo de `libs.versions.toml` | Clases 3, 4 y 8 |
| 3 | **Stock del carrito** | Nicolás | `f51091cdc6` | Validar disponibilidad y descontar stock al confirmar el carrito + pruebas | Clases 8 y 9 |
| 4 | **Documentación** | Benjamín | `4987622251` | README (ms-productos) y `docs/guias-estudio/` | Clase 10 |

Cada parte compila y pasa sus pruebas por sí sola (parte 2: 213 pruebas; parte 3: 224).

## Regla de oro: uno a la vez y en orden

1. Se une (merge) el **PR #29** a `main`.
2. **Parte 1** → PR → merge. Recién ahí empieza la parte 2.
3. **Parte 2** → PR → merge.
4. **Parte 3** → PR → merge.
5. **Parte 4** → PR → merge.

Cada uno parte desde `main` actualizado, así no hay conflictos. Avisen por el grupo cuando su PR quede unido.

## Pasos para cada integrante (terminal de Android Studio o Git Bash)

Reemplaza `<N>`, `<nombre-rama>`, `<commit>` y el mensaje según tu fila de la tabla.

**1. Tu identidad (una sola vez, con tu usuario y correo de GitHub):**

```bash
git config user.name "TuUsuarioGitHub"
git config user.email "tu_correo_de_github@ejemplo.com"
```

**2. Traer lo último:**

```bash
git checkout main
git pull origin main
git fetch origin ev4-alanis
```

**3. Crear tu rama y copiar tu parte sin hacer commit todavía (`-n`):**

```bash
git checkout -b <nombre-rama>
git cherry-pick -n <commit>
```

**4. Revisar, probar y hacer tu commit:**

```bash
git status
./gradlew testDebugUnitTest          # partes 2 y 3 (en Windows: gradlew.bat testDebugUnitTest)
git commit -m "<mensaje>"
git push -u origin <nombre-rama>
```

En la parte 1, en vez de `gradlew` prueba el microservicio: `cd microservicios/ms-productos` y `./mvnw test` (Windows: `mvnw.cmd test`).

**5. Abrir el Pull Request en GitHub** desde tu rama hacia `main`, con un título y una descripción con tus palabras. Pide a otro integrante que lo revise y lo una.

## Datos de cada parte

| Parte | `<nombre-rama>` | `<commit>` | `<mensaje>` sugerido |
| --- | --- | --- | --- |
| 1 | `feature/ev4-ms-productos` | `0a7f769c2d` | `feat(backend): agregar microservicio ms-productos para el catalogo` |
| 2 | `feature/ev4-catalogo-remoto` | `b0313547d6` | `feat(app): conectar el catalogo con ms-productos` |
| 3 | `feature/ev4-stock-carrito` | `f51091cdc6` | `feat(app): validar stock con ms-productos al confirmar el carrito` |
| 4 | `feature/ev4-docs-guias` | `4987622251` | `docs: documentar ms-productos y agregar guias de estudio` |

## Si algo sale mal

- **`cherry-pick` dice que hay conflicto**: probablemente no se unió la parte anterior. Ejecuta `git cherry-pick --abort`, espera el merge anterior y repite desde el paso 2.
- **Te equivocaste antes del commit**: `git reset --hard` vuelve todo atrás (solo en tu rama nueva).
- **Gradle no sincroniza en la parte 2**: abre *File → Sync Project with Gradle Files*; la parte 2 agrega las librerías que faltaban.

## Después de los 4 merges

- Volver a generar el APK firmado (`./gradlew assembleRelease`) y copiarlo a `entrega/` (puede ser otro PR pequeño).
- Capturas `docs/capturas/apk-firmado.png` y `docs/capturas/keystore-jks.png`, y la tabla de integrantes del README.
- Mover las tarjetas en Trello: cada uno la suya.
