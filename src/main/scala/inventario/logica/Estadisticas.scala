package inventario.logica

import inventario.modelo._
import java.time.LocalDate

// Unidades vendidas y su valor en un período
case class ResumenVentas(
    unidades: Int,
    valor: Double
)

object Estadisticas {

    def valorProducto(inv: Inventario, producto: Producto): Double = {
        producto.valorDe(Stock.actual(inv, producto.id))
    }

    def sumar(valores: List[Double]): Double = {
        valores.reduceOption((a, b) => a + b).getOrElse(0.0)
    }

    // Valor total del inventario: suma de stock x precio de todos los productos
    def valorTotal(inv: Inventario): Double = {
        sumar(inv.productos.map(producto => valorProducto(inv, producto)))
    }

    // Desglose del valor según una clave de agrupación (proveedor, categoría, ...)
    def valorPor[K](inv: Inventario)(clave: Producto => K): Map[K, Double] = {
        Agrupacion.agruparPor(inv.productos)(clave).map { case (k, productos) =>
            (k, sumar(productos.map(producto => valorProducto(inv, producto))))
        }
    }

    // Pares (producto, cantidad) de todos los movimientos de tipo Salida
    private def salidas(inv: Inventario): List[(Int, Int)] = {
        inv.movimientos.flatMap {
            case Salida(id, cantidad, _) => List((id, cantidad))
            case _: Entrada | _: Ajuste => Nil
        }
    }

    // Unidades vendidas (movimientos de tipo Salida) por id de producto
    def unidadesVendidas(inv: Inventario): Map[Int, Int] = {
        Agrupacion.agruparPor(salidas(inv))(par => par._1).map { case (id, pares) =>
            (id, pares.map(par => par._2).reduce((a, b) => a + b))
        }
    }

    // Ventas (salidas) de un período: unidades y valor según el precio actual
    def ventasEn(inv: Inventario)(enPeriodo: LocalDate => Boolean): ResumenVentas = {
        val ventas: List[(Int, Double)] = inv.movimientos.flatMap {
            case Salida(id, cantidad, fecha) if enPeriodo(fecha) =>
                inv.producto(id).map(p => (cantidad, p.valorDe(cantidad))).toList
            case _ => Nil
        }
        ResumenVentas(
            ventas.map(venta => venta._1).foldLeft(0)((a, b) => a + b),
            ventas.map(venta => venta._2).foldLeft(0.0)((a, b) => a + b)
        )
    }

    // Unidades que ingresaron (entradas) en un período
    def unidadesCompradasEn(inv: Inventario)(enPeriodo: LocalDate => Boolean): Int = {
        inv.movimientos
            .flatMap {
                case Entrada(_, cantidad, fecha) if enPeriodo(fecha) => List(cantidad)
                case _ => Nil
            }
            .foldLeft(0)((a, b) => a + b)
    }

    // Unidades vendidas por día entre dos fechas (los días sin ventas cuentan 0)
    def ventasPorFecha(inv: Inventario, desde: LocalDate, hasta: LocalDate): List[(LocalDate, Int)] = {
        val salidasConFecha: List[(LocalDate, Int)] = inv.movimientos.flatMap {
            case Salida(_, cantidad, fecha) => List((fecha, cantidad))
            case _: Entrada | _: Ajuste => Nil
        }
        val porDia: Map[LocalDate, Int] = Agrupacion.agruparPor(salidasConFecha)(par => par._1).map {
            case (fecha, pares) => (fecha, pares.map(par => par._2).sum)
        }
        val dias: List[LocalDate] = (desde.toEpochDay to hasta.toEpochDay).toList.map(d => LocalDate.ofEpochDay(d))
        dias.map(dia => (dia, porDia.getOrElse(dia, 0)))
    }

    // Producto con más unidades en salidas, junto con esa cantidad
    def masVendido(inv: Inventario): Option[(Producto, Int)] = {
        unidadesVendidas(inv).toList
            .sortBy(par => par._1)
            .maxByOption(par => par._2)
            .flatMap { case (id, unidades) => inv.producto(id).map(p => (p, unidades)) }
    }
}
