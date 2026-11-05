package inventario.ui

import inventario.logica._
import inventario.modelo._
import java.time.LocalDate
import scalafx.Includes._
import scalafx.beans.property.ObjectProperty
import scalafx.collections.ObservableBuffer
import scalafx.geometry.Insets
import scalafx.scene.control.{Label, ScrollPane, TableView}
import scalafx.scene.layout.{HBox, VBox}

// Fila de las tablas de agotados y stock bajo del dashboard
case class FilaStock(
    numero: Int,
    producto: String,
    stock: Int,
    minimo: Int,
    nivel: NivelStock
)

// Pantalla de inicio: resumen en tarjetas, gráficos de actividad y alertas de stock.
class PanelDashboard(
    estado: ObjectProperty[Inventario]
) {

    private val filasAgotados = ObservableBuffer[FilaStock]()
    private val filasBajos = ObservableBuffer[FilaStock]()

    private val lblProductos = new Label
    private val lblProveedores = new Label
    private val lblValor = new Label
    private val lblAlertas = new Label

    private val lblHoyVentas = new Label
    private val lblHoyCompras = new Label
    private val lblMesVentas = new Label
    private val lblMesCompras = new Label

    private val graficoVentas = new GraficoLineas
    private val graficoProveedores = new GraficoPastel

    private def tabla(filas: ObservableBuffer[FilaStock]): TableView[FilaStock] = {
        new TableView[FilaStock](filas) {
            columns += Formato.columna[FilaStock]("#", 45)(fila => fila.numero.toString)
            columns += Formato.columna[FilaStock]("Producto", 200)(fila => fila.producto)
            columns += Formato.columna[FilaStock]("Stock", 65)(fila => fila.stock.toString)
            columns += Formato.columna[FilaStock]("Mínimo", 70)(fila => fila.minimo.toString)
            columns += Formato.columnaGrafica[FilaStock]("Cobertura", 120)(fila => {
                Componentes.barraCobertura(fila.stock, fila.minimo, fila.nivel)
            })
            prefHeight = 250
            placeholder = Componentes.etiqueta("Sin productos en esta situación", "vacio")
        }
    }

    private val contenido: VBox = new VBox(16) {
        padding = Insets(4)
        children = Seq(
            new HBox(16) {
                children = Seq(
                    Componentes.tarjetaEstadistica("Total productos", lblProductos, "📦", "kpi-violeta"),
                    Componentes.tarjetaEstadistica("Proveedores", lblProveedores, "🚚", "kpi-cielo"),
                    Componentes.tarjetaEstadistica("Valor del inventario", lblValor, "💰", "kpi-esmeralda"),
                    Componentes.tarjetaEstadistica("Alertas activas", lblAlertas, "⚠", "kpi-rosa")
                )
            },
            new HBox(16) {
                children = Seq(
                    Componentes.banner("Hoy", lblHoyVentas, lblHoyCompras, "banner-esmeralda"),
                    Componentes.banner("Últimos 30 días", lblMesVentas, lblMesCompras, "banner-indigo")
                )
            },
            new HBox(16) {
                children = Seq(
                    Componentes.tarjeta("Ventas de los últimos 14 días (unidades)", graficoVentas.vista, 600),
                    Componentes.tarjeta("Valor del inventario por proveedor", graficoProveedores.vista, 360)
                )
            },
            new HBox(16) {
                children = Seq(
                    Componentes.tarjeta("Productos agotados", tabla(filasAgotados), 400),
                    Componentes.tarjeta("Stock bajo", tabla(filasBajos), 400)
                )
            }
        )
    }

    val vista: ScrollPane = new ScrollPane {
        fitToWidth = true
        styleClass += "scroll-transparente"
        content = contenido
    }

    private def filasDe(alertas: List[EstadoProducto], nivel: NivelStock): List[FilaStock] = {
        alertas
            .filter(estadoProducto => estadoProducto.nivel == nivel)
            .zipWithIndex
            .map { case (e, i) => FilaStock(i + 1, e.producto.nombre, e.stock, e.producto.stockMinimo, e.nivel) }
    }

    private def textoVentas(inv: Inventario, enPeriodo: LocalDate => Boolean): String = {
        val resumen = Estadisticas.ventasEn(inv)(enPeriodo)
        s"Ventas: ${resumen.unidades} u. · ${Formato.moneda(resumen.valor)}"
    }

    private def textoCompras(inv: Inventario, enPeriodo: LocalDate => Boolean): String = {
        s"Compras: ${Estadisticas.unidadesCompradasEn(inv)(enPeriodo)} u."
    }

    private def refrescar(inv: Inventario): Unit = {
        val alertas = Stock.alertas(inv)
        Formato.reemplazar(filasAgotados, filasDe(alertas, Agotado))
        Formato.reemplazar(filasBajos, filasDe(alertas, Bajo))

        lblProductos.text = inv.productos.size.toString
        lblProveedores.text = inv.proveedores.size.toString
        lblValor.text = Formato.moneda(Estadisticas.valorTotal(inv))
        lblAlertas.text = alertas.size.toString

        val hoy = LocalDate.now()
        val desde = hoy.minusDays(30)
        val esHoy: LocalDate => Boolean = fecha => fecha == hoy
        val ultimos30: LocalDate => Boolean = fecha => !fecha.isBefore(desde) && !fecha.isAfter(hoy)

        lblHoyVentas.text = textoVentas(inv, esHoy)
        lblHoyCompras.text = textoCompras(inv, esHoy)
        lblMesVentas.text = textoVentas(inv, ultimos30)
        lblMesCompras.text = textoCompras(inv, ultimos30)

        val ventasDiarias = Estadisticas.ventasPorFecha(inv, hoy.minusDays(13), hoy)
        graficoVentas.mostrar(ventasDiarias.map(par => (Formato.diaCorto(par._1), par._2.toDouble)))

        val valorProveedores = Estadisticas.valorPor(inv)(producto => producto.proveedorId).toList
            .map { case (id, valor) => (inv.proveedor(id).map(p => p.nombre).getOrElse(s"Proveedor $id"), valor) }
            .sortBy(par => -par._2)
        graficoProveedores.mostrar(valorProveedores)
    }

    estado.onChange((_, _, nuevo) => refrescar(nuevo))
    refrescar(estado.value)
}
