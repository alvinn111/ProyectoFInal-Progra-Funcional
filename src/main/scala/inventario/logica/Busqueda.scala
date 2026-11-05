package inventario.logica

import inventario.modelo._

object Busqueda {

    // Un criterio es simplemente una función de Producto a Boolean
    type Criterio = Producto => Boolean

    // Función de orden superior: recibe el criterio de búsqueda como parámetro
    def buscar(inv: Inventario)(criterio: Criterio): List[Producto] = {
        inv.productos.filter(criterio)
    }

    // Criterio que acepta todos los productos
    val todos: Criterio = producto => true

    def porCategoria(categoria: Categoria): Criterio = {
        producto => producto.categoria == categoria
    }

    def porProveedor(proveedorId: Int): Criterio = {
        producto => producto.proveedorId == proveedorId
    }

    def porRangoPrecio(minimo: Double, maximo: Double): Criterio = {
        producto => producto.precio >= minimo && producto.precio <= maximo
    }

    def porNivel(inv: Inventario, nivel: NivelStock): Criterio = {
        producto => Stock.nivelDe(inv, producto) == nivel
    }

    def porNombre(texto: String): Criterio = {
        val buscado = texto.trim.toLowerCase
        producto => producto.nombre.toLowerCase.contains(buscado)
    }

    // Combina dos criterios: se deben cumplir ambos
    def ambos(a: Criterio, b: Criterio): Criterio = {
        producto => a(producto) && b(producto)
    }

    // Combina una lista de criterios: se deben cumplir todos
    def combinar(criterios: List[Criterio]): Criterio = {
        criterios.foldLeft(todos)((acumulado, siguiente) => ambos(acumulado, siguiente))
    }
}
