package inventario.logica

import inventario.modelo._

// Una línea del reporte: qué producto pedir y cuántas unidades se sugieren
case class LineaReposicion(
    producto: Producto,
    stock: Int,
    sugerido: Int
)

object Reposicion {

    // Se sugiere reponer hasta el doble del stock mínimo
    def cantidadSugerida(producto: Producto, stock: Int): Int = {
        math.max(producto.stockMinimo * 2 - stock, 0)
    }

    // Productos a reponer, agrupados por proveedor (ordenado por id de proveedor)
    def reporte(inv: Inventario): List[(Proveedor, List[LineaReposicion])] = {
        val lineas = Stock.alertas(inv).map(estado => {
            LineaReposicion(estado.producto, estado.stock, cantidadSugerida(estado.producto, estado.stock))
        })
        Agrupacion
            .agruparPor(lineas)(linea => linea.producto.proveedorId)
            .toList
            .sortBy(par => par._1)
            .flatMap { case (proveedorId, grupo) => inv.proveedor(proveedorId).map(prov => (prov, grupo)).toList }
    }
}
