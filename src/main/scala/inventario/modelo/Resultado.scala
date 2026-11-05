package inventario.modelo

// Resultado de una operación: éxito con un valor o fallo con un error de dominio.
// Reemplaza a las excepciones, así las funciones siguen siendo puras y predecibles.
sealed trait Resultado[+A] {

    // Transforma el valor si hubo éxito; si hubo fallo, lo deja igual
    def map[B](f: A => B): Resultado[B] = {
        this match {
            case Exito(valor) => Exito(f(valor))
            case Fallo(error) => Fallo(error)
        }
    }

    // Encadena otra operación que también puede fallar (permite usar for-yield)
    def flatMap[B](f: A => Resultado[B]): Resultado[B] = {
        this match {
            case Exito(valor) => f(valor)
            case Fallo(error) => Fallo(error)
        }
    }

    def esExito(): Boolean = {
        this match {
            case Exito(_) => true
            case Fallo(_) => false
        }
    }
}

case class Exito[A](
    valor: A
) extends Resultado[A]

case class Fallo(
    error: ErrorInventario
) extends Resultado[Nothing]

object Resultado {

    // Convierte un Option en Resultado: Some pasa a Exito y None pasa a Fallo con el error dado
    def desdeOpcion[A](opcion: Option[A], error: ErrorInventario): Resultado[A] = {
        opcion match {
            case Some(valor) => Exito(valor)
            case None => Fallo(error)
        }
    }
}
