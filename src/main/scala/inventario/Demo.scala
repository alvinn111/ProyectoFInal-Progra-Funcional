package inventario

import inventario.datos.DatosIniciales
import inventario.logica._
import inventario.modelo._
import java.time.LocalDate

// Ejemplo de ejecución por consola (sin interfaz gráfica).
// Demuestra que la lógica se puede probar por separado:  sbt "runMain inventario.Demo"
object Demo {

    def main(args: Array[String]): Unit = {
        val inv = DatosIniciales.inventario

        println("== Stock actual y nivel ==")
        inv.productos.foreach(producto => {
            val stock = Stock.actual(inv, producto.id)
            val nivel = Stock.nivelDe(inv, producto).etiqueta()
            println(f"${producto.nombre}%-22s stock=$stock%3d  nivel=$nivel")
        })

        println("\n== Alertas ==")
        Stock.alertas(inv).foreach(estado => {
            println(s"${estado.producto.nombre}: ${estado.nivel.etiqueta()} (${estado.stock})")
        })

        println("\n== Búsqueda: bebidas con nivel Bajo ==")
        val criterio = Busqueda.ambos(Busqueda.porCategoria(Bebidas), Busqueda.porNivel(inv, Bajo))
        Busqueda.buscar(inv)(criterio).foreach(producto => println(producto.nombre))

        println(s"\n== Valor total == ${Estadisticas.valorTotal(inv)}")
        println("Por categoría: " + Estadisticas.valorPor(inv)(producto => producto.categoria.nombre()))
        println("Más vendido: " + Estadisticas.masVendido(inv).map { case (p, u) => s"${p.nombre} ($u)" })

        println("\n== Reporte de reposición ==")
        Reposicion.reporte(inv).foreach { case (proveedor, lineas) =>
            println(proveedor.nombre)
            lineas.foreach(linea => println(s"  ${linea.producto.nombre}: pedir ${linea.sugerido}"))
        }

        println("\n== Registrar movimientos ==")
        val hoy = LocalDate.now()

        Operaciones.registrarMovimiento(inv, Salida(1, 100, hoy)) match {
            case Exito(_) => println("Vender 100 arroz: aceptado")
            case Fallo(error) => println("Vender 100 arroz: rechazado -> " + error.mensaje())
        }

        Operaciones.registrarMovimiento(inv, Entrada(3, 30, hoy)) match {
            case Exito(nuevo) =>
                println("Entrada de 30 aceite -> stock nuevo: " + Stock.actual(nuevo, 3))
                println("Inventario original intacto -> stock aceite: " + Stock.actual(inv, 3))
            case Fallo(error) =>
                println("Entrada rechazada -> " + error.mensaje())
        }
    }
}
