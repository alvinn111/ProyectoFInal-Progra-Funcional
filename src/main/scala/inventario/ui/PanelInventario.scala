package inventario.ui

import inventario.logica._
import inventario.modelo._
import java.time.LocalDate
import scalafx.Includes._
import scalafx.beans.property.ObjectProperty
import scalafx.collections.ObservableBuffer
import scalafx.event.ActionEvent
import scalafx.geometry.Insets
import scalafx.scene.control._
import scalafx.scene.layout.{HBox, Priority, VBox}

// Fila de la tabla de productos: el producto con su proveedor, stock y nivel ya calculados
case class FilaProducto(
    producto: Producto,
    proveedor: String,
    stock: Int,
    nivel: NivelStock
)

// Pantalla principal de trabajo: tabla de productos con filtros y formulario de movimientos.
class PanelInventario(
    estado: ObjectProperty[Inventario]
) {

    private val filas = ObservableBuffer[FilaProducto]()

    private val opcionesCategoria = ObservableBuffer("Todas")
    private val opcionesProveedor = ObservableBuffer("Todos")
    private val opcionesNivel = ObservableBuffer("Todos" :: NivelStock.todos.map(nivel => nivel.etiqueta()): _*)
    private val opcionesProducto = ObservableBuffer[String]()

    // ---- Resumen (píldoras) ----
    private val lblProductos = Componentes.etiqueta("", "pildora")
    private val lblValor = Componentes.etiqueta("", "pildora")
    private val lblAlertas = Componentes.etiqueta("", "pildora-alerta")

    // ---- Filtros ----
    private val cmbCategoria = new ComboBox[String](opcionesCategoria) { value = "Todas"; prefWidth = 140 }
    private val cmbProveedor = new ComboBox[String](opcionesProveedor) { value = "Todos"; prefWidth = 190 }
    private val cmbNivel = new ComboBox[String](opcionesNivel) { value = "Todos"; prefWidth = 130 }
    private val txtBusqueda = new TextField { promptText = "Buscar por nombre..."; prefWidth = 200 }
    private val txtPrecioMin = new TextField { promptText = "Mín."; prefWidth = 80 }
    private val txtPrecioMax = new TextField { promptText = "Máx."; prefWidth = 80 }
    private val btnLimpiar = new Button("Limpiar filtros") {
        styleClass += "boton-secundario"
        onAction = (_: ActionEvent) => limpiarFiltros()
    }

    // ---- Formulario de movimientos ----
    private val cmbProducto = new ComboBox[String](opcionesProducto) { prefWidth = 260; maxWidth = Double.MaxValue }
    private val grupoTipo = new ToggleGroup
    private val tbEntrada = segmento("Entrada", "segmento-izq")
    private val tbSalida = segmento("Salida", "segmento-centro")
    private val tbAjuste = segmento("Ajuste", "segmento-der")
    private val lblCantidad = Componentes.etiqueta("CANTIDAD", "campo-etiqueta")
    private val txtCantidad = new TextField { promptText = "Ej: 10" }
    private val lblMotivo = Componentes.etiqueta("MOTIVO DEL AJUSTE", "campo-etiqueta")
    private val txtMotivo = new TextField { promptText = "Ej: Producto vencido" }
    private val lblMensaje = new Label {
        wrapText = true
    }
    private val btnRegistrar = new Button("Registrar movimiento") {
        styleClass += "boton-primario"
        maxWidth = Double.MaxValue
        onAction = (_: ActionEvent) => registrar()
    }

    // Botón del selector segmentado (Entrada | Salida | Ajuste)
    private def segmento(texto: String, clase: String): ToggleButton = {
        val boton = new ToggleButton(texto) {
            styleClass ++= Seq("segmento", clase)
            maxWidth = Double.MaxValue
            onAction = (_: ActionEvent) => alElegirTipo()
        }
        boton.delegate.setToggleGroup(grupoTipo.delegate)
        HBox.setHgrow(boton, Priority.Always)
        boton
    }

    // ---- Tabla ----
    private def col(titulo: String, ancho: Double)(extraer: FilaProducto => String): TableColumn[FilaProducto, String] = {
        Formato.columna[FilaProducto](titulo, ancho)(extraer)
    }

    private val tabla = new TableView[FilaProducto](filas) {
        columns += col("ID", 45)(fila => fila.producto.id.toString)
        columns += col("Producto", 190)(fila => Formato.icono(fila.producto.categoria) + "  " + fila.producto.nombre)
        columns += col("Categoría", 100)(fila => fila.producto.categoria.nombre())
        columns += col("Proveedor", 160)(fila => fila.proveedor)
        columns += col("Precio", 90)(fila => Formato.moneda(fila.producto.precio))
        columns += col("Stock", 60)(fila => fila.stock.toString)
        columns += col("Mínimo", 65)(fila => fila.producto.stockMinimo.toString)
        columns += Formato.columnaGrafica[FilaProducto]("Cobertura", 115)(fila => {
            Componentes.barraCobertura(fila.stock, fila.producto.stockMinimo, fila.nivel)
        })
        columns += Formato.columnaGrafica[FilaProducto]("Nivel", 105)(fila => Componentes.chipNivel(fila.nivel))
        placeholder = Componentes.etiqueta("No hay productos que coincidan con los filtros", "vacio")
        rowFactory = { _ =>
            new TableRow[FilaProducto] {
                item.onChange { (_, _, fila) =>
                    Formato.pintarFila(styleClass, Option(fila).map(f => f.nivel))
                }
            }
        }
    }

    // ---- Distribución de la pantalla ----
    private val barraResumen = new HBox(10) {
        children = Seq(lblProductos, lblValor, lblAlertas)
    }

    private val tarjetaFiltros = new HBox(12) {
        styleClass += "card"
        children = Seq(
            Componentes.campoEtiquetado("BUSCAR", txtBusqueda),
            Componentes.campoEtiquetado("CATEGORÍA", cmbCategoria),
            Componentes.campoEtiquetado("PROVEEDOR", cmbProveedor),
            Componentes.campoEtiquetado("NIVEL DE STOCK", cmbNivel),
            Componentes.campoEtiquetado("PRECIO MÍN.", txtPrecioMin),
            Componentes.campoEtiquetado("PRECIO MÁX.", txtPrecioMax),
            Componentes.espaciadorHorizontal(),
            Componentes.campoEtiquetado(" ", btnLimpiar)
        )
    }

    private val tarjetaTabla = Componentes.tarjeta("Productos", tabla, 700)

    private val tarjetaMovimiento = new VBox(10) {
        styleClass += "card"
        prefWidth = 300
        minWidth = 300
        maxWidth = 300
        children = Seq(
            Componentes.etiqueta("Registrar movimiento", "card-titulo"),
            Componentes.campoEtiquetado("PRODUCTO", cmbProducto),
            Componentes.campoEtiquetado("TIPO DE MOVIMIENTO", new HBox(0) { children = Seq(tbEntrada, tbSalida, tbAjuste) }),
            new VBox(4) { children = Seq(lblCantidad, txtCantidad) },
            new VBox(4) { children = Seq(lblMotivo, txtMotivo) },
            btnRegistrar,
            lblMensaje
        )
    }

    private val cuerpo = new HBox(16) {
        children = Seq(tarjetaTabla, tarjetaMovimiento)
    }
    VBox.setVgrow(cuerpo, Priority.Always)

    val vista: VBox = new VBox(14) {
        padding = Insets(4)
        children = Seq(barraResumen, tarjetaFiltros, cuerpo)
    }

    // ---- Lógica de la vista (solo llama a funciones puras de la capa lógica) ----

    private def alElegirTipo(): Unit = {
        // Siempre debe quedar un tipo elegido
        if (!tbEntrada.selected.value && !tbSalida.selected.value && !tbAjuste.selected.value) {
            tbEntrada.selected = true
        }
        val esAjuste = tbAjuste.selected.value
        lblMotivo.visible = esAjuste
        lblMotivo.managed = esAjuste
        txtMotivo.visible = esAjuste
        txtMotivo.managed = esAjuste
        lblCantidad.text = if (esAjuste) "CANTIDAD (NEGATIVA PARA RESTAR)" else "CANTIDAD"
    }

    private def tipoElegido(): Option[String] = {
        List((tbEntrada, "Entrada"), (tbSalida, "Salida"), (tbAjuste, "Ajuste"))
            .find(par => par._1.selected.value)
            .map(par => par._2)
    }

    private def criterioActual(inv: Inventario): Busqueda.Criterio = {
        val porCategoria = Categoria
            .desdeNombre(cmbCategoria.value.value)
            .map(categoria => Busqueda.porCategoria(categoria))
            .getOrElse(Busqueda.todos)
        val porProveedor = inv.proveedores
            .find(p => p.nombre == cmbProveedor.value.value)
            .map(p => Busqueda.porProveedor(p.id))
            .getOrElse(Busqueda.todos)
        val porNivel = NivelStock
            .desdeEtiqueta(cmbNivel.value.value)
            .map(nivel => Busqueda.porNivel(inv, nivel))
            .getOrElse(Busqueda.todos)
        val porNombre = Busqueda.porNombre(txtBusqueda.text.value)
        val minimo = txtPrecioMin.text.value.trim.replace(',', '.').toDoubleOption.getOrElse(0.0)
        val maximo = txtPrecioMax.text.value.trim.replace(',', '.').toDoubleOption.getOrElse(Double.MaxValue)

        Busqueda.combinar(List(porCategoria, porProveedor, porNivel, porNombre, Busqueda.porRangoPrecio(minimo, maximo)))
    }

    private def aplicarFiltros(): Unit = {
        val inv = estado.value
        val encontrados = Busqueda.buscar(inv)(criterioActual(inv))
        Formato.reemplazar(filas, encontrados.map(producto => {
            FilaProducto(
                producto,
                inv.proveedor(producto.proveedorId).map(p => p.nombre).getOrElse("—"),
                Stock.actual(inv, producto.id),
                Stock.nivelDe(inv, producto)
            )
        }))
    }

    private def limpiarFiltros(): Unit = {
        txtBusqueda.text = ""
        txtPrecioMin.text = ""
        txtPrecioMax.text = ""
        cmbCategoria.value = "Todas"
        cmbProveedor.value = "Todos"
        cmbNivel.value = "Todos"
        aplicarFiltros()
    }

    private def etiquetaProducto(producto: Producto): String = {
        s"${producto.id} · ${producto.nombre}"
    }

    private def idProductoSeleccionado(): Option[Int] = {
        Option(cmbProducto.value.value).flatMap(texto => texto.takeWhile(c => c.isDigit).toIntOption)
    }

    private def construirMovimiento(id: Int, cantidad: Int): Resultado[Movimiento] = {
        tipoElegido() match {
            case Some("Entrada") => Exito(Entrada(id, cantidad, LocalDate.now()))
            case Some("Salida") => Exito(Salida(id, cantidad, LocalDate.now()))
            case Some("Ajuste") => Exito(Ajuste(id, cantidad, LocalDate.now(), txtMotivo.text.value))
            case _ => Fallo(DatoInvalido("Seleccione el tipo de movimiento"))
        }
    }

    private def registrar(): Unit = {
        val inv = estado.value
        val resultado: Resultado[Inventario] = for {
            id <- Resultado.desdeOpcion(idProductoSeleccionado(), DatoInvalido("Seleccione un producto"))
            cantidad <- Resultado.desdeOpcion(
                txtCantidad.text.value.trim.toIntOption,
                DatoInvalido("La cantidad debe ser un número entero")
            )
            mov <- construirMovimiento(id, cantidad)
            nuevo <- Operaciones.registrarMovimiento(inv, mov)
        } yield nuevo

        resultado match {
            case Exito(nuevo) =>
                estado.value = nuevo
                txtCantidad.text = ""
                txtMotivo.text = ""
                Componentes.mostrarMensaje(lblMensaje, "Movimiento registrado correctamente", true)
            case Fallo(error) =>
                Componentes.mostrarMensaje(lblMensaje, error.mensaje(), false)
        }
    }

    private def refrescar(inv: Inventario): Unit = {
        Formato.actualizarOpciones(
            cmbProveedor,
            opcionesProveedor,
            "Todos" :: inv.proveedores.map(p => p.nombre),
            "Todos"
        )
        val productos = inv.productos.map(producto => etiquetaProducto(producto))
        Formato.actualizarOpciones(cmbProducto, opcionesProducto, productos, productos.headOption.getOrElse(""))

        lblProductos.text = s"📦  ${inv.productos.size} productos"
        lblValor.text = s"💰  ${Formato.moneda(Estadisticas.valorTotal(inv))}"
        lblAlertas.text = s"⚠  ${Stock.alertas(inv).size} alertas activas"
        aplicarFiltros()
    }

    // ---- Conexión de eventos y primer dibujado ----
    Formato.actualizarOpciones(
        cmbCategoria,
        opcionesCategoria,
        "Todas" :: Categoria.todas.map(categoria => categoria.nombre()),
        "Todas"
    )
    cmbCategoria.onAction = (_: ActionEvent) => aplicarFiltros()
    cmbProveedor.onAction = (_: ActionEvent) => aplicarFiltros()
    cmbNivel.onAction = (_: ActionEvent) => aplicarFiltros()
    txtBusqueda.text.onChange((_, _, _) => aplicarFiltros())
    txtPrecioMin.text.onChange((_, _, _) => aplicarFiltros())
    txtPrecioMax.text.onChange((_, _, _) => aplicarFiltros())

    tbEntrada.selected = true
    alElegirTipo()

    estado.onChange((_, _, nuevo) => refrescar(nuevo))
    refrescar(estado.value)
}
