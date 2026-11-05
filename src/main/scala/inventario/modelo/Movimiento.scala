package inventario.modelo

import java.time.LocalDate

// Jerarquía sellada: hechos que alteran el stock.
// El stock de un producto nunca se guarda: se deriva de su historial de movimientos.
sealed trait Movimiento {
    val productoId: Int
    val cantidad: Int
    val fecha: LocalDate

    // Unidades que suma (+) o resta (-) este movimiento al stock
    def efecto(): Int = {
        this match {
            case Entrada(_, c, _) => c
            case Salida(_, c, _) => -c
            case Ajuste(_, c, _, _) => c // puede ser negativo (pérdida) o positivo (corrección)
        }
    }

    def tipo(): String = {
        this match {
            case _: Entrada => "Entrada"
            case _: Salida => "Salida"
            case _: Ajuste => "Ajuste"
        }
    }

    def motivo(): Option[String] = {
        this match {
            case Ajuste(_, _, _, razon) => Some(razon)
            case _: Entrada | _: Salida => None
        }
    }
}

// Ingreso de mercadería (cantidad positiva)
case class Entrada(
    productoId: Int,
    cantidad: Int,
    fecha: LocalDate
) extends Movimiento

// Salida por venta (cantidad positiva; resta al stock)
case class Salida(
    productoId: Int,
    cantidad: Int,
    fecha: LocalDate
) extends Movimiento

// Ajuste: cantidad con signo (+ corrección, - pérdida o vencimiento) y su motivo
case class Ajuste(
    productoId: Int,
    cantidad: Int,
    fecha: LocalDate,
    razon: String
) extends Movimiento
