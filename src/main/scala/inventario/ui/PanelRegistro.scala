package inventario.ui

import inventario.logica._
import inventario.modelo._
import scalafx.Includes._
import scalafx.beans.property.ObjectProperty
import scalafx.collections.ObservableBuffer
import scalafx.event.ActionEvent
import scalafx.geometry.Insets
import scalafx.scene.control._
import scalafx.scene.layout.{HBox, VBox}

// Fila de la tabla de proveedores: el proveedor y cuántos productos le compramos
case class FilaProveedor(
    proveedor: Proveedor,
    productos: Int
)

// Pantalla para registrar proveedores y productos nuevos.
class PanelRegistro(
    estado: ObjectProperty[Inventario]
) {

    private val opcionesCategoria = ObservableBuffer(Categoria.todas.map(categoria => categoria.nombre()): _*)
    private val opcionesProveedor = ObservableBuffer[String]()
    private val filasProveedores = ObservableBuffer[FilaProveedor]()

    // ---- Proveedor ----
    private val txtNombreProveedor = new TextField { promptText = "Ej: Distribuidora Andina" }
    private val txtTelefonoProveedor = new TextField { promptText = "Ej: 70123456" }
    private val lblMensajeProveedor = new Label { wrapText = true }
    private val btnProveedor = new Button("Registrar proveedor") {
        styleClass += "boton-primario"
        onAction = (_: ActionEvent) => registrarProveedor()
    }

    // ---- Producto ----
    private val txtNombreProducto = new TextField { promptText = "Ej: Arroz 1 kg" }
    private val cmbCategoria = new ComboBox[String](opcionesCategoria) { value = "Alimentos"; maxWidth = Double.MaxValue }
    private val txtPrecio = new TextField { promptText = "Ej: 8.50" }
    private val txtStockMinimo = new TextField { promptText = "Ej: 20" }
    private val cmbProveedor = new ComboBox[String](opcionesProveedor) { maxWidth = Double.MaxValue }
    private val lblMensajeProducto = new Label { wrapText = true }
    private val btnProducto = new Button("Registrar producto") {
        styleClass += "boton-primario"
        onAction = (_: ActionEvent) => registrarProducto()
    }

    // ---- Tabla de proveedores ----
    private val tablaProveedores = new TableView[FilaProveedor](filasProveedores) {
        columns += Formato.columna[FilaProveedor]("ID", 50)(fila => fila.proveedor.id.toString)
        columns += Formato.columna[FilaProveedor]("Proveedor", 240)(fila => fila.proveedor.nombre)
        columns += Formato.columna[FilaProveedor]("Teléfono", 130)(fila => fila.proveedor.telefono)
        columns += Formato.columna[FilaProveedor]("Productos", 100)(fila => fila.productos.toString)
        prefHeight = 230
        placeholder = Componentes.etiqueta("Todavía no hay proveedores", "vacio")
    }

    private val tarjetaProveedor = new VBox(12) {
        styleClass += "card"
        prefWidth = 420
        children = Seq(
            Componentes.etiqueta("Nuevo proveedor", "card-titulo"),
            Componentes.campoEtiquetado("NOMBRE", txtNombreProveedor),
            Componentes.campoEtiquetado("TELÉFONO", txtTelefonoProveedor),
            btnProveedor,
            lblMensajeProveedor
        )
    }

    private val tarjetaProducto = new VBox(12) {
        styleClass += "card"
        prefWidth = 420
        children = Seq(
            Componentes.etiqueta("Nuevo producto", "card-titulo"),
            Componentes.campoEtiquetado("NOMBRE", txtNombreProducto),
            Componentes.campoEtiquetado("CATEGORÍA", cmbCategoria),
            new HBox(12) {
                children = Seq(
                    Componentes.campoEtiquetado("PRECIO DE VENTA (Bs.)", txtPrecio),
                    Componentes.campoEtiquetado("STOCK MÍNIMO", txtStockMinimo)
                )
            },
            Componentes.campoEtiquetado("PROVEEDOR", cmbProveedor),
            btnProducto,
            lblMensajeProducto
        )
    }

    private val contenido: VBox = new VBox(16) {
        padding = Insets(4)
        children = Seq(
            new HBox(16) { children = Seq(tarjetaProveedor, tarjetaProducto) },
            Componentes.tarjeta("Proveedores registrados", tablaProveedores, 900)
        )
    }

    val vista: ScrollPane = new ScrollPane {
        fitToWidth = true
        styleClass += "scroll-transparente"
        content = contenido
    }

    // Si el resultado fue exitoso reemplaza el inventario vigente; si fue un fallo, muestra el error
    private def mostrar(mensaje: Label, resultado: Resultado[Inventario], textoExito: String): Unit = {
        resultado match {
            case Exito(nuevo) =>
                estado.value = nuevo
                Componentes.mostrarMensaje(mensaje, textoExito, true)
            case Fallo(error) =>
                Componentes.mostrarMensaje(mensaje, error.mensaje(), false)
        }
    }

    private def registrarProveedor(): Unit = {
        val resultado = Operaciones.registrarProveedor(
            estado.value,
            txtNombreProveedor.text.value,
            txtTelefonoProveedor.text.value
        )
        mostrar(lblMensajeProveedor, resultado, "Proveedor registrado correctamente")
        if (resultado.esExito()) {
            txtNombreProveedor.text = ""
            txtTelefonoProveedor.text = ""
        }
    }

    private def registrarProducto(): Unit = {
        val inv = estado.value
        val resultado: Resultado[Inventario] = for {
            precio <- Resultado.desdeOpcion(
                txtPrecio.text.value.trim.replace(',', '.').toDoubleOption,
                DatoInvalido("El precio debe ser un número")
            )
            minimo <- Resultado.desdeOpcion(
                txtStockMinimo.text.value.trim.toIntOption,
                DatoInvalido("El stock mínimo debe ser un número entero")
            )
            categoria <- Resultado.desdeOpcion(
                Categoria.desdeNombre(cmbCategoria.value.value),
                DatoInvalido("Seleccione una categoría")
            )
            proveedor <- Resultado.desdeOpcion(
                inv.proveedores.find(p => p.nombre == cmbProveedor.value.value),
                DatoInvalido("Seleccione un proveedor")
            )
            nuevo <- Operaciones.registrarProducto(inv, txtNombreProducto.text.value, categoria, precio, minimo, proveedor.id)
        } yield nuevo

        mostrar(lblMensajeProducto, resultado, "Producto registrado correctamente")
        if (resultado.esExito()) {
            txtNombreProducto.text = ""
            txtPrecio.text = ""
            txtStockMinimo.text = ""
        }
    }

    private def refrescar(inv: Inventario): Unit = {
        val nombres = inv.proveedores.map(p => p.nombre)
        Formato.actualizarOpciones(cmbProveedor, opcionesProveedor, nombres, nombres.headOption.getOrElse(""))
        Formato.reemplazar(filasProveedores, inv.proveedores.map(proveedor => {
            FilaProveedor(proveedor, inv.productos.count(producto => producto.proveedorId == proveedor.id))
        }))
    }

    estado.onChange((_, _, nuevo) => refrescar(nuevo))
    refrescar(estado.value)
}
