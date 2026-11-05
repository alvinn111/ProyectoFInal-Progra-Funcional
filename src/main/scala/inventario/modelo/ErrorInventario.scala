package inventario.modelo

// Jerarquía sellada: errores de dominio.
// Las operaciones los devuelven dentro de un Resultado en lugar de lanzar excepciones.
sealed trait ErrorInventario {
    def mensaje(): String
}

case class ProductoInexistente(
    id: Int
) extends ErrorInventario {
    def mensaje(): String = s"No existe un producto con id $id"
}

case class CantidadInvalida(
    cantidad: Int
) extends ErrorInventario {
    def mensaje(): String = s"Cantidad inválida: $cantidad"
}

case class StockInsuficiente(
    disponible: Int,
    solicitado: Int
) extends ErrorInventario {
    def mensaje(): String = s"Stock insuficiente: hay $disponible unidades y se piden $solicitado"
}

case class DatoInvalido(
    detalle: String
) extends ErrorInventario {
    def mensaje(): String = detalle
}
