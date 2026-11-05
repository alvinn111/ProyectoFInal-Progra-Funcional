package inventario.ui

import inventario.logica._
import inventario.modelo._
import scalafx.Includes._
import scalafx.beans.property.ObjectProperty
import scalafx.collections.ObservableBuffer
import scalafx.geometry.Insets
import scalafx.scene.control.{ScrollPane, TableColumn, TableRow, TableView}
import scalafx.scene.layout.{HBox, VBox}

// Fila de la tabla de alertas
case class FilaAlerta(
    producto: String,
    stock: Int,
    minimo: Int,
    sugerido: Int,
    nivel: NivelStock,
    proveedor: String
)

// Alertas de stock (agotados y bajos) y reporte de reposición agrupado por proveedor.
class PanelAlertas(
    estado: ObjectProperty[Inventario]
) {

    private val filas = ObservableBuffer[FilaAlerta]()

    private def col(titulo: String, ancho: Double)(extraer: FilaAlerta => String): TableColumn[FilaAlerta, String] = {
        Formato.columna[FilaAlerta](titulo, ancho)(extraer)
    }

    private val tabla = new TableView[FilaAlerta](filas) {
        columns += col("Producto", 190)(fila => fila.producto)
        columns += col("Stock", 60)(fila => fila.stock.toString)
        columns += col("Mínimo", 65)(fila => fila.minimo.toString)
        columns += col("Sugerido", 75)(fila => fila.sugerido.toString)
        columns += Formato.columnaGrafica[FilaAlerta]("Nivel", 100)(fila => Componentes.chipNivel(fila.nivel))
        columns += col("Proveedor", 170)(fila => fila.proveedor)
        placeholder = Componentes.etiqueta("No hay productos agotados ni con stock bajo", "vacio")
        rowFactory = { _ =>
            new TableRow[FilaAlerta] {
                item.onChange { (_, _, fila) =>
                    Formato.pintarFila(styleClass, Option(fila).map(f => f.nivel))
                }
            }
        }
    }

    // Aquí se dibujan las tarjetas de reposición (una por proveedor)
    private val contenedorReposicion = new VBox(12) {
        padding = Insets(2)
    }

    private val desplazamiento = new ScrollPane {
        fitToWidth = true
        styleClass += "scroll-transparente"
        content = contenedorReposicion
    }

    val vista: HBox = new HBox(16) {
        padding = Insets(4)
        children = Seq(
            Componentes.tarjeta("Productos agotados o con stock bajo", tabla, 640),
            Componentes.tarjeta("Reposición sugerida por proveedor", desplazamiento, 420)
        )
    }

    private def refrescar(inv: Inventario): Unit = {
        Formato.reemplazar(filas, Stock.alertas(inv).map(estadoProducto => {
            FilaAlerta(
                estadoProducto.producto.nombre,
                estadoProducto.stock,
                estadoProducto.producto.stockMinimo,
                Reposicion.cantidadSugerida(estadoProducto.producto, estadoProducto.stock),
                estadoProducto.nivel,
                inv.proveedor(estadoProducto.producto.proveedorId).map(p => p.nombre).getOrElse("—")
            )
        }))

        contenedorReposicion.children.clear()
        val reporte = Reposicion.reporte(inv)
        if (reporte.isEmpty) {
            contenedorReposicion.children += Componentes.etiqueta("🎉  No hay productos por reponer", "vacio-grande")
        } else {
            reporte.foreach(par => contenedorReposicion.children += Componentes.tarjetaReposicion(par._1, par._2))
        }
    }

    estado.onChange((_, _, nuevo) => refrescar(nuevo))
    refrescar(estado.value)
}
