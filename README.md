#  Inventario de tienda con alertas de stock

Proyecto final de **Programación Funcional con Scala**. Es un sistema de inventario para una tienda pequeña:
registra los movimientos de mercadería (entradas, salidas y ajustes), calcula el stock a partir de ese historial
y avisa cuando un producto está **agotado** o por **debajo de su mínimo**, sugiriendo qué pedir y a qué proveedor.

La idea central del diseño: **el stock nunca se guarda, se calcula.** Cada venta o compra agrega un movimiento
nuevo al historial y produce un `Inventario` nuevo; el anterior queda intacto. Por eso todo el programa se apoya
en datos inmutables y funciones puras.

---

##  Qué puede hacer

| Pantalla | Funciones |
|---|---|
| **Dashboard** | Tarjetas de resumen, ventas de hoy y de los últimos 30 días, gráfico de ventas por día, valor por proveedor y tablas de productos agotados y con stock bajo |
| **Inventario** | Tabla con nivel de stock (etiquetas de color y barra de cobertura), filtros combinables (nombre, categoría, proveedor, nivel y rango de precio) y formulario para registrar entradas, salidas y ajustes |
| **Alertas y reposición** | Productos agotados o bajos y un reporte de reposición agrupado por proveedor, con la cantidad sugerida |
| **Estadísticas** | Valor total, producto más vendido, gráficos de valor por proveedor y por categoría, e historial de movimientos |
| **Registro** | Alta de proveedores y de productos, con validaciones y mensajes claros |

Reglas de negocio:

- Una **salida** (venta) que supere el stock disponible se rechaza.
- Un **ajuste** necesita un motivo y no puede dejar el stock en negativo (cantidad negativa = pérdida o vencimiento).
- Los nombres de productos y proveedores no se pueden repetir.
- Nivel de stock: `Agotado` (0 unidades), `Bajo` (menos que el mínimo) y `Suficiente` (en cualquier otro caso).
- Reposición sugerida: llegar al **doble del stock mínimo**.

---

##  Cómo ejecutarlo

**Requisitos:** JDK 17 o superior y [sbt](https://www.scala-sbt.org/download).

```bash
sbt run                          # abre la interfaz gráfica
sbt test                         # corre las pruebas de la lógica
sbt "runMain inventario.Demo"    # ejemplo de ejecución por consola, sin interfaz
```

La primera vez sbt descarga Scala 2.13.18, ScalaFX 21 y JavaFX 21 (el `build.sbt` elige los binarios según tu
sistema operativo), así que necesita conexión a internet.

### En Visual Studio Code

1. Instala la extensión **Scala (Metals)** (VS Code la recomienda al abrir la carpeta).
2. Abre la carpeta **`inventario-scala`** (la que contiene `build.sbt`), no una subcarpeta ni archivos sueltos.
3. Cuando Metals pregunte *New sbt workspace detected*, pulsa **Import build**.
4. Ejecuta `sbt run` en la terminal integrada.

<details>
<summary> Problemas frecuentes</summary>

| Síntoma | Solución |
|---|---|
| Muchos errores en rojo en los `import javafx...` | Abriste una carpeta sin `build.sbt`. Abre la carpeta raíz del proyecto. |
| Metals dice `scala-cli` abajo a la derecha | Paleta de comandos → **Metals: Switch build server** → **sbt**. |
| La ventana abre pero sin colores | No se encontró `src/main/resources/estilo.css`. Verifica que el archivo esté en esa ruta. |
| Aparece un aviso *Unsupported JavaFX configuration* | Es solo una advertencia de JavaFX al correr sin módulos; no afecta al programa. |
| Error de versión de Java | Verifica con `java -version` que sea 17 o superior. |

</details>

---

##  Estructura del proyecto

```
inventario-scala/
├── build.sbt                        Dependencias (ScalaFX, JavaFX, munit) y opciones del compilador
├── project/build.properties         Versión de sbt
├── .scalafmt.conf  .editorconfig    Reglas de formato del código
├── README.md
└── src/
    ├── main/
    │   ├── resources/estilo.css     Estilos de la interfaz (paleta tipo Tailwind)
    │   └── scala/inventario/
    │       ├── modelo/              Datos inmutables y jerarquías de tipos
    │       │   ├── Categoria.scala        sealed trait + case objects
    │       │   ├── NivelStock.scala       sealed trait + case objects
    │       │   ├── Movimiento.scala       sealed trait: Entrada, Salida, Ajuste
    │       │   ├── ErrorInventario.scala  sealed trait de errores de dominio
    │       │   ├── Resultado.scala        ADT genérico: Exito[A] / Fallo
    │       │   └── Proveedor, Producto, Inventario
    │       ├── logica/              Funciones puras
    │       │   ├── Agrupacion.scala       agruparPor[A, K] genérica (foldLeft)
    │       │   ├── Stock.scala            stock actual, nivel y alertas
    │       │   ├── Busqueda.scala         buscar(inv)(criterio): orden superior
    │       │   ├── Estadisticas.scala     valor total y por clave, más vendido, ventas por fecha
    │       │   ├── Reposicion.scala       reporte por proveedor
    │       │   └── Operaciones.scala      registrar movimientos, productos y proveedores
    │       ├── datos/DatosIniciales.scala Datos de ejemplo (sin base de datos)
    │       ├── ui/                  ScalaFX: la única capa con efectos
    │       │   ├── Main.scala             ventana, barra lateral y barra superior
    │       │   ├── Componentes.scala      tarjetas, etiquetas de color, barras de progreso
    │       │   ├── Graficos.scala         gráficos de barras, líneas y pastel
    │       │   ├── Formato.scala          texto, columnas de tabla y utilidades
    │       │   └── Panel*.scala           una clase por pantalla
    │       └── Demo.scala           Ejemplo de ejecución por consola
    └── test/scala/inventario/LogicaSuite.scala   Pruebas de la lógica (munit)
```

---

##  Diseño

### Tres capas separadas

| Capa | Contenido | Característica |
|---|---|---|
| **Modelo** | Entidades y jerarquías de tipos | Datos inmutables |
| **Lógica** | Funciones de procesamiento del inventario | Funciones puras, sin efectos |
| **Interfaz** | Ventana, tablas, formularios y gráficos | Única capa con efectos |

La interfaz solo llama a la lógica y muestra el resultado. Lo **único que cambia con el tiempo** es una referencia
(`ObjectProperty[Inventario]`) al inventario vigente, y vive en la capa gráfica: cuando el usuario registra una
venta, la lógica devuelve un inventario nuevo (o un error) y la interfaz reemplaza esa referencia.

### Jerarquías selladas

| Jerarquía | Variantes | Para qué |
|---|---|---|
| `Categoria` | `Alimentos`, `Bebidas`, `Limpieza`, `Otros` | Clasificar productos |
| `Movimiento` | `Entrada`, `Salida`, `Ajuste` | Hechos que alteran el stock |
| `NivelStock` | `Agotado`, `Bajo`, `Suficiente` | Resultado de evaluar un producto |
| `ErrorInventario` | `ProductoInexistente`, `CantidadInvalida`, `StockInsuficiente`, `DatoInvalido` | Errores de dominio |

Al ser `sealed`, el compilador avisa si algún `match` olvida una variante.

### Decisiones

1. **Stock derivado de movimientos.** No hay un campo de stock que modificar: se calcula con `foldLeft` sobre el
   historial, y de paso queda un registro completo de lo ocurrido.
2. **Errores como valores.** Las operaciones devuelven un `Resultado[Inventario]` (`Exito` o `Fallo` con un
   `ErrorInventario`) en lugar de lanzar excepciones; `Resultado` tiene `map` y `flatMap`, así que se encadena con
   `for ... yield`.
3. **Criterios como funciones.** Un único `Busqueda.buscar` sirve para todos los filtros porque el criterio llega
   como parámetro y se combina con `Busqueda.combinar`.
4. **Agrupación genérica.** `Agrupacion.agruparPor[A, K]` agrupa cualquier lista por cualquier clave y se reutiliza
   para el valor por proveedor, por categoría, las ventas por día y la reposición.
5. **Alcance acotado.** Sin base de datos ni persistencia: los datos iniciales viven en `DatosIniciales.scala`.

---

##  Cumplimiento de los requisitos

| Requisito | Dónde |
|---|---|
| Clases y objetos | `Producto`, `Proveedor`, `Inventario` y los movimientos, con sus métodos (`Producto.valorDe`, `Inventario.conMovimiento`, ...) |
| Datos inmutables | Solo `val`; cada operación devuelve un `Inventario` nuevo con `copy` |
| Listas | `List` de productos, proveedores y movimientos |
| `map`, `filter`, `flatMap`, `foldLeft`, `reduce` | `Stock`, `Estadisticas`, `Reposicion`, `Agrupacion` |
| Funciones de orden superior | `Busqueda.buscar(inv)(criterio)`, `Agrupacion.agruparPor(...)(clave)`, `Estadisticas.valorPor(...)(clave)` |
| Case classes y pattern matching | Entidades, `Movimiento.efecto`, `Stock.nivel`, `Operaciones`, `Formato.claseNivel` |
| Jerarquías de tipos | `Categoria`, `Movimiento`, `NivelStock`, `ErrorInventario` (todas `sealed trait`) |
| Genéricos | `Agrupacion.agruparPor[A, K]`, `Estadisticas.valorPor[K]`, `Resultado[+A]`, `Formato.columna[S]` |
| Interfaz gráfica | ScalaFX: tablas, formularios, filtros, tarjetas y gráficos |
| Restricciones | Sin `var`, sin `while` ni ciclos imperativos y sin `Any` |

---

##  Interfaz

La interfaz usa **ScalaFX** (JavaFX con sintaxis de Scala) y se estiliza con **JavaFX CSS** en
`src/main/resources/estilo.css`. Tailwind es una librería para páginas web y no funciona dentro de JavaFX, así que
el CSS usa su **paleta de colores** (slate, emerald, amber, red, indigo) y su estilo: tarjetas redondeadas, sombras
suaves, etiquetas de color y barra lateral oscura.

Para cambiar el aspecto no hace falta tocar Scala: los colores están definidos una sola vez como variables al
inicio de `estilo.css` (por ejemplo `-c-emerald-600`, el color principal).

---

##  Pruebas

```bash
sbt test
```

`LogicaSuite` verifica el stock, los niveles, las alertas, las validaciones de `Operaciones`, la búsqueda, el
valor del inventario, el producto más vendido, la agrupación genérica y el reporte de reposición, todo sin abrir
la interfaz (la lógica se puede probar sin ella).

### Ejemplo de ejecución (`inventario.Demo`)

Con los datos iniciales, el arroz queda en 12 unidades (mínimo 20, nivel `Bajo`), el aceite en 0 (`Agotado`) y el
producto más vendido es el arroz, con 38 unidades. Intentar vender 100 arroz devuelve
`Fallo(StockInsuficiente(12, 100))` y el inventario original no cambia.

---

##  Convenciones del código

El código sigue la sintaxis vista en clase:

- Llaves en `object`, `class`, `trait` y `def`, con sangría de **4 espacios**.
- Un parámetro por línea en los constructores y firmas largas.
- Tipos de retorno explícitos y `def nombre(): Tipo` para los métodos sin parámetros.
- `case class` / `case object` / `sealed trait` para los datos, y `match` en lugar de `if` encadenados.
- Lambdas con parámetro nombrado (`producto => producto.nombre`) para que se lean mejor.
- Comentarios con `//` que explican el porqué, no lo obvio.

El archivo `.scalafmt.conf` deja a Metals formatear el código con estas mismas reglas.
