# Práctica #2 — CRUD completo sobre TiendaApp

## Información general

| Dato | Valor |
|------|-------|
| Curso | SC-403 Desarrollo de Aplicaciones Web y Patrones |
| Universidad | Fidélitas |
| Modalidad | Individual, asincrónica |
| Valor | **5%** de la nota final |
| Tiempo estimado | 3 a 4 horas |
| Fecha de asignación | Día de tu clase de la Semana 7 |
| Fecha de entrega | **8 días** después de la asignación |
| Fecha límite — Grupo Martes | **Martes de la Semana 8 a las 23:59** |
| Fecha límite — Grupo Viernes | **Viernes de la Semana 8 a las 23:59** |

> Después de la fecha límite, la entrega aplica como tardía según el reglamento de la universidad.

---

## Propósito

Esta segunda práctica busca que **apliqués el patrón CRUD completo** sobre un proyecto distinto al que vimos en clase. Reutilizás el proyecto `tiendaapp` de la Práctica #1 y le agregás las operaciones **Create, Update y Delete** que le faltaban.

A diferencia de la Práctica #1 (que era más de "setup y un cambio cosmético"), esta práctica te exige aplicar conceptos de la Clase 6:

- Formularios para crear y editar.
- Validaciones con `@Valid` + `BindingResult`.
- Modal de Bootstrap para confirmación de borrado.
- Flash messages con `RedirectAttributes`.

Si terminaste el CRUD de Cursos en la Clase 6, esta práctica te debería tomar entre 3 y 4 horas. Es prácticamente lo mismo, solo que sobre la entidad `Producto`.

---

## Objetivos de aprendizaje

Al terminar esta práctica vas a haber demostrado que sos capaz de:

1. Implementar un CRUD completo sobre una entidad nueva.
2. Aplicar validaciones de Bean Validation con anotaciones y mostrarlas en la vista.
3. Usar componentes de Bootstrap (modal, alert) integrados con Spring + Thymeleaf.
4. Mantener un workflow Git ordenado con commits convencionales.

---

## Material entregado

| Archivo | Descripción |
|---------|-------------|
| `practica_2.md` | Este documento con instrucciones y rúbrica. |
| `tiendaapp.zip` | Proyecto Spring Boot — el mismo que usaste en la Práctica #1. |

> **Sugerencia:** podés reusar la copia del proyecto que tenías en tu repo personal (`Practica_1/tiendaapp`). En este caso, copiala a `Practica_2/tiendaapp` y trabajá ahí, manteniendo separadas ambas prácticas.

---

## Antes de empezar

Verificá que tenés todo configurado igual que en la Práctica #1:

```bash
java --version    # OpenJDK 21
git --version
mysql --version   # o Workbench abierto
echo $env:DB_PASSWORD   # debería imprimir tu password (PowerShell)
```

Si la BD `tiendadb` ya existe de la Práctica #1, podés reutilizarla. Si no, crearla con:

```sql
CREATE DATABASE tiendadb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

---

## Lo que tenés que hacer

### Paso 1 — Setup del proyecto

1. En tu repositorio personal, hacer `git pull`.
2. Crear la carpeta `Practica_2/`.
3. Descomprimir `tiendaapp.zip` adentro.
4. Verificar que arranca con `mvnw spring-boot:run` y que `/productos` muestra los productos del seed.

### Paso 2 — Agregar validaciones a Producto

En `src/main/java/com/ufide/tiendaapp/entity/Producto.java`:

1. Agregá los imports:

```java
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
```

2. Agregá las anotaciones de validación sobre los campos:

```java
@NotBlank(message = "El nombre es obligatorio")
@Size(max = 100)
private String nombre;

@Size(max = 500)
private String descripcion;

@Positive(message = "El precio debe ser mayor a 0")
private double precio;

@PositiveOrZero(message = "El stock no puede ser negativo")
private int stock;

@Size(max = 50)
private String categoria;
```

3. Verificá que el `pom.xml` ya tenga la dependencia `spring-boot-starter-validation`. Si no, agregala.

### Paso 3 — CREATE de productos

#### 3.1 Endpoints en el controller

En `ProductoController.java` agregá:

```java
@GetMapping("/nuevo")
public String mostrarFormNuevo(Model m) {
    m.addAttribute("producto", new Producto());
    return "productos/form";
}

@PostMapping
public String guardar(@Valid @ModelAttribute("producto") Producto producto,
                      BindingResult result,
                      RedirectAttributes ra) {
    if (result.hasErrors()) {
        return "productos/form";
    }
    productoService.guardar(producto);
    ra.addFlashAttribute("ok", "Producto guardado correctamente");
    return "redirect:/productos";
}
```

Asegurate que el `ProductoService` tenga un método `guardar(Producto p)` que llame a `repo.save(p)`.

#### 3.2 Vista del formulario

Crear `src/main/resources/templates/productos/form.html` con un formulario para todos los campos: nombre, descripción, precio, stock, categoría. Usá `th:object="${producto}"` y `th:field="*{campo}"`.

Acordate de:

- El `<input type="hidden" th:field="*{id}">` para que sirva igual para CREATE y UPDATE.
- Mostrar errores con `th:errors="*{campo}"` debajo de cada input.

#### 3.3 Botón "Nuevo producto"

En `productos.html`, agregá arriba del listado un botón:

```html
<a class="btn btn-primary mb-3" th:href="@{/productos/nuevo}">
    Nuevo producto
</a>
```

### Paso 4 — UPDATE de productos

#### 4.1 Endpoints

```java
@GetMapping("/{id}/editar")
public String mostrarFormEditar(@PathVariable Long id, Model m) {
    Producto p = productoService.buscarPorId(id)
        .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));
    m.addAttribute("producto", p);
    return "productos/form";
}

@PostMapping("/{id}")
public String actualizar(@PathVariable Long id,
                         @Valid @ModelAttribute("producto") Producto producto,
                         BindingResult result,
                         RedirectAttributes ra) {
    if (result.hasErrors()) {
        return "productos/form";
    }
    producto.setId(id);
    productoService.guardar(producto);
    ra.addFlashAttribute("ok", "Producto actualizado correctamente");
    return "redirect:/productos";
}
```

#### 4.2 Botón "Editar" en cada card

En `productos.html`, dentro de cada card, agregá:

```html
<a class="btn btn-outline-warning btn-sm"
   th:href="@{/productos/{id}/editar(id=${producto.id})}">
    Editar
</a>
```

### Paso 5 — DELETE con modal de confirmación

#### 5.1 Endpoint

```java
@PostMapping("/{id}/eliminar")
public String eliminar(@PathVariable Long id, RedirectAttributes ra) {
    productoService.eliminar(id);
    ra.addFlashAttribute("ok", "Producto eliminado correctamente");
    return "redirect:/productos";
}
```

(Asegurate que `ProductoService` tenga `eliminar(Long id)`.)

#### 5.2 Botón "Eliminar" y modal

En cada card agregá:

```html
<button type="button"
        class="btn btn-outline-danger btn-sm"
        data-bs-toggle="modal"
        data-bs-target="#modalEliminar"
        th:data-id="${producto.id}"
        th:data-nombre="${producto.nombre}">
    Eliminar
</button>
```

Al final del `<body>`, antes del bundle de Bootstrap, agregá el modal y el script (podés copiarlo del proyecto `Clase_4_Base_Final` de la Clase 6).

### Paso 6 — Mostrar flash de éxito

En `productos.html`, arriba del listado:

```html
<div class="alert alert-success alert-dismissible fade show" th:if="${ok}">
    <span th:text="${ok}">Operación realizada</span>
    <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
</div>
```

### Paso 7 — Probar el CRUD completo

1. Crear un producto desde `/productos/nuevo`. Llenar el form. Verificar el flash.
2. Intentar guardar dejando el nombre vacío. Verificar que aparezca el mensaje de error.
3. Editar el producto creado. Cambiar el precio. Verificar que se actualiza.
4. Click en Eliminar. Verificar que aparece el modal con el nombre.
5. Cancelar y confirmar. Verificar que el producto se borra.

### Paso 8 — Subir el resultado

```bash
cd ruta/a/tu/repo
git pull
git status   # verificar que NO aparece application-local.properties
git add .
git commit -m "feat(practica2): CRUD completo de productos"
git push
```

### Paso 9 — Capturar evidencia

Tomá **3 capturas** y guardalas en `Practica_2/evidencia/`:

- `01-create.png`: formulario "Nuevo producto" lleno con un producto.
- `02-update.png`: formulario de edición con un producto cargado.
- `03-modal-delete.png`: modal de confirmación abierto sobre un producto.

```bash
git add Practica_2/evidencia
git commit -m "docs(practica2): capturas de evidencia"
git push
```

---

## Cómo entregar

Subí al campus virtual (Moodle), en el espacio designado:

1. **URL de tu repositorio de GitHub** completa.
2. **Hash del último commit** (`git log --oneline -1`).

No subas el código ni las capturas al campus. La revisión se hace directamente en GitHub.

---

## Rúbrica de evaluación (5%)

La nota se asigna sobre 100 y luego se multiplica por 0.05 para obtener el porcentaje final.

| Criterio | Peso | Excelente (100) | Aceptable (70) | Insuficiente (40) | No entregado (0) |
|----------|:----:|:----------------|:---------------|:------------------|:-----------------|
| **1. Estructura del repo** | 10 | Carpeta `Practica_2/tiendaapp/` correctamente ubicada. | Existe pero el nombre o la ubicación no siguen convención. | Mezclada con la Práctica 1 o desorganizada. | No hay carpeta de la práctica. |
| **2. CREATE funcional** | 20 | Botón "Nuevo" funciona, form muestra todos los campos, guarda y muestra flash. | Funciona pero le falta el flash o algún campo. | Form aparece pero no guarda. | No implementado. |
| **3. UPDATE funcional** | 20 | Botón "Editar" en cada card, form precargado, guarda cambios. | Funciona pero crea duplicado en vez de actualizar. | Form aparece vacío. | No implementado. |
| **4. DELETE con modal** | 15 | Modal de Bootstrap con nombre del producto, cancela y elimina correctamente. | Solo `confirm()` nativo, sin modal Bootstrap. | DELETE sin confirmación. | No implementado. |
| **5. Validaciones visibles** | 15 | Anotaciones `@Valid` + `BindingResult` y errores visibles en el form. | Anotaciones puestas pero errores no se muestran. | Sin anotaciones de validación. | No implementado. |
| **6. Flash messages** | 5 | Flash "Producto guardado/actualizado/eliminado" aparece. | Aparece solo en una de las 3 operaciones. | No aparecen. | No implementado. |
| **7. Evidencia** | 5 | 3 capturas claras en `Practica_2/evidencia/`. | 1 o 2 capturas. | Capturas no relacionadas. | No hay capturas. |
| **8. Workflow Git** | 5 | Al menos 2 commits con mensajes convencionales (`feat:`, `docs:`). | Hay commits pero mensajes vagos. | Un solo commit con todo. | No hay commits. |
| **9. Seguridad** | 5 | `application-local.properties` NO está en el repo. | Hay archivo de config local sin credenciales reales. | Hay alguna credencial visible. | Credenciales reales subidas. |

**Penalizaciones:**

- Subir contraseña real al repositorio: nota máxima 60.
- Entrega después de la fecha límite: penalidad según reglamento universitario.

---

## Problemas comunes

| Síntoma | Solución |
|---------|----------|
| Validaciones no se ejecutan | Verificar que `spring-boot-starter-validation` esté en `pom.xml` y que el método tenga `@Valid @ModelAttribute`. |
| UPDATE crea duplicado | Falta `<input type="hidden" th:field="*{id}">` en el form. |
| Errores no se muestran en el form | Falta `th:errors="*{campo}"` debajo del input. |
| Modal no abre | El script de Bootstrap debe estar al final del HTML, antes del `</body>`. |
| El modal muestra el nombre equivocado | El JS del script no está leyendo `event.relatedTarget`. Copiarlo del Clase_4_Base_Final de la Clase 6. |
| `BindingResult` no funciona | Va INMEDIATAMENTE después de `@Valid @ModelAttribute`, sin nada en medio. |

---

## Recursos

- **Tu propio CRUD de Cursos** (Clase 6) — es el ejemplo más cercano. Compará tu código.
- `Clase_4_Base_Final` (de la Clase 6) — proyecto de referencia con el patrón aplicado.
- Bootstrap Modals: https://getbootstrap.com/docs/5.3/components/modal/
- Spring Validation: https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-controller/ann-methods/modelattrib-method-args.html
