package inventario.modelo

// Jerarquía sellada: resultado de evaluar el stock de un producto.
sealed trait NivelStock {
    def etiqueta(): String
}

case object Agotado extends NivelStock {
    def etiqueta(): String = "Agotado"
}

case object Bajo extends NivelStock {
    def etiqueta(): String = "Bajo"
}

case object Suficiente extends NivelStock {
    def etiqueta(): String = "Suficiente"
}

object NivelStock {

    val todos: List[NivelStock] = List(Agotado, Bajo, Suficiente)

    def desdeEtiqueta(texto: String): Option[NivelStock] = {
        todos.find(nivel => nivel.etiqueta() == texto)
    }
}
