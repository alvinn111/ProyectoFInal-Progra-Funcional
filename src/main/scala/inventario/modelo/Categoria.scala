package inventario.modelo

// Jerarquía sellada: categorías de producto.
// Las variantes no necesitan datos adicionales, por eso son objetos simples (case object).
sealed trait Categoria {
    def nombre(): String
}

case object Alimentos extends Categoria {
    def nombre(): String = "Alimentos"
}

case object Bebidas extends Categoria {
    def nombre(): String = "Bebidas"
}

case object Limpieza extends Categoria {
    def nombre(): String = "Limpieza"
}

case object Otros extends Categoria {
    def nombre(): String = "Otros"
}

object Categoria {

    val todas: List[Categoria] = List(Alimentos, Bebidas, Limpieza, Otros)

    def desdeNombre(texto: String): Option[Categoria] = {
        todas.find(categoria => categoria.nombre() == texto)
    }
}
