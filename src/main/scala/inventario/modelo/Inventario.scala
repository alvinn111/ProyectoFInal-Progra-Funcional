package inventario.modelo

// Estado completo del sistema en un momento dado.
// Es inmutable: cada operación devuelve un Inventario nuevo y el anterior queda intacto.
case class Inventario(
    productos: List[Producto],
    proveedores: List[Proveedor],
    movimientos: List[Movimiento]
) {

    def producto(id: Int): Option[Producto] = {
        productos.find(p => p.id == id)
    }

    def proveedor(id: Int): Option[Proveedor] = {
        proveedores.find(p => p.id == id)
    }

    // Cada método "con..." devuelve una copia con el elemento agregado
    def conProducto(nuevo: Producto): Inventario = {
        copy(productos = productos :+ nuevo)
    }

    def conProveedor(nuevo: Proveedor): Inventario = {
        copy(proveedores = proveedores :+ nuevo)
    }

    def conMovimiento(nuevo: Movimiento): Inventario = {
        copy(movimientos = movimientos :+ nuevo)
    }

    def siguienteIdProducto(): Int = {
        productos.map(p => p.id).maxOption.getOrElse(0) + 1
    }

    def siguienteIdProveedor(): Int = {
        proveedores.map(p => p.id).maxOption.getOrElse(0) + 1
    }
}

object Inventario {

    val vacio: Inventario = Inventario(Nil, Nil, Nil)
}
