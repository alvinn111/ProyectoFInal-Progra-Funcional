package inventario.logica

object Agrupacion {

    // Función genérica y de orden superior: agrupa los elementos de una lista
    // según la clave que calcula la función recibida. Sirve para cualquier tipo
    // de elemento A y de clave K (proveedor, categoría, fecha, producto, ...).
    def agruparPor[A, K](elementos: List[A])(clave: A => K): Map[K, List[A]] = {
        elementos.foldLeft(Map.empty[K, List[A]])((acumulado, elemento) => {
            val k = clave(elemento)
            acumulado.updated(k, acumulado.getOrElse(k, Nil) :+ elemento)
        })
    }
}
