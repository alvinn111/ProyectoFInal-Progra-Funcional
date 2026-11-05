package inventario.modelo

// Artículo que se vende en la tienda.
// El stock NO es un atributo: se calcula a partir de los movimientos.
case class Producto(
    id: Int,
    nombre: String,
    categoria: Categoria,
    precio: Double,
    stockMinimo: Int,
    proveedorId: Int
) {

    // Valor monetario de `unidades` de este producto
    def valorDe(unidades: Int): Double = {
        unidades * precio
    }
}
