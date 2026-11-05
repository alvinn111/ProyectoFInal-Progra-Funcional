package inventario.logica

import inventario.modelo._

// Operaciones que "modifican" el inventario: en realidad devuelven uno NUEVO o un Fallo.
object Operaciones {

    def registrarMovimiento(inv: Inventario, mov: Movimiento): Resultado[Inventario] = {
        for {
            producto <- Resultado.desdeOpcion(inv.producto(mov.productoId), ProductoInexistente(mov.productoId))
            _ <- validarCantidad(mov)
            _ <- validarStock(inv, producto, mov)
        } yield inv.conMovimiento(mov)
    }

    private def validarCantidad(mov: Movimiento): Resultado[Unit] = {
        mov match {
            case Entrada(_, c, _) if c <= 0 => Fallo(CantidadInvalida(c))
            case Salida(_, c, _) if c <= 0 => Fallo(CantidadInvalida(c))
            case Ajuste(_, c, _, _) if c == 0 => Fallo(CantidadInvalida(c))
            case Ajuste(_, _, _, razon) if razon.trim.isEmpty => Fallo(DatoInvalido("El ajuste necesita un motivo"))
            case _ => Exito(())
        }
    }

    // Rechaza cualquier movimiento que deje el stock en negativo (por ejemplo, vender de más)
    private def validarStock(inv: Inventario, producto: Producto, mov: Movimiento): Resultado[Unit] = {
        val disponible = Stock.actual(inv, producto.id)
        if (disponible + mov.efecto() < 0) {
            Fallo(StockInsuficiente(disponible, -mov.efecto()))
        } else {
            Exito(())
        }
    }

    def registrarProveedor(inv: Inventario, nombre: String, telefono: String): Resultado[Inventario] = {
        if (nombre.trim.isEmpty) {
            Fallo(DatoInvalido("El nombre del proveedor es obligatorio"))
        } else if (inv.proveedores.exists(p => p.nombre.equalsIgnoreCase(nombre.trim))) {
            Fallo(DatoInvalido("Ya existe un proveedor con ese nombre"))
        } else {
            Exito(inv.conProveedor(Proveedor(inv.siguienteIdProveedor(), nombre.trim, telefono.trim)))
        }
    }

    def registrarProducto(
        inv: Inventario,
        nombre: String,
        categoria: Categoria,
        precio: Double,
        stockMinimo: Int,
        proveedorId: Int
    ): Resultado[Inventario] = {
        if (nombre.trim.isEmpty) {
            Fallo(DatoInvalido("El nombre del producto es obligatorio"))
        } else if (precio <= 0) {
            Fallo(DatoInvalido("El precio debe ser mayor que cero"))
        } else if (stockMinimo < 0) {
            Fallo(DatoInvalido("El stock mínimo no puede ser negativo"))
        } else if (inv.proveedor(proveedorId).isEmpty) {
            Fallo(DatoInvalido("El proveedor indicado no existe"))
        } else if (inv.productos.exists(p => p.nombre.equalsIgnoreCase(nombre.trim))) {
            Fallo(DatoInvalido("Ya existe un producto con ese nombre"))
        } else {
            val nuevo = Producto(inv.siguienteIdProducto(), nombre.trim, categoria, precio, stockMinimo, proveedorId)
            Exito(inv.conProducto(nuevo))
        }
    }
}
