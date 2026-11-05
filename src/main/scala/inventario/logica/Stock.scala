package inventario.logica

import inventario.modelo._

// Producto con su stock y su nivel ya evaluados
case class EstadoProducto(
    producto: Producto,
    stock: Int,
    nivel: NivelStock
)

object Stock {

    // Stock actual = suma de los efectos de todos los movimientos del producto
    def actual(inv: Inventario, productoId: Int): Int = {
        inv.movimientos
            .filter(mov => mov.productoId == productoId)
            .foldLeft(0)((acumulado, mov) => acumulado + mov.efecto())
    }

    // Clasifica un stock respecto al mínimo deseado (pattern matching con condiciones)
    def nivel(stock: Int, minimo: Int): NivelStock = {
        stock match {
            case s if s <= 0 => Agotado
            case s if s < minimo => Bajo
            case _ => Suficiente
        }
    }

    def nivelDe(inv: Inventario, producto: Producto): NivelStock = {
        nivel(actual(inv, producto.id), producto.stockMinimo)
    }

    // Evalúa todos los productos del inventario
    def evaluar(inv: Inventario): List[EstadoProducto] = {
        inv.productos.map(producto => {
            val stock = actual(inv, producto.id)
            EstadoProducto(producto, stock, nivel(stock, producto.stockMinimo))
        })
    }

    // Productos agotados o por debajo de su mínimo
    def alertas(inv: Inventario): List[EstadoProducto] = {
        evaluar(inv).filter(estado => estado.nivel != Suficiente)
    }
}
