package inventario.datos

import inventario.modelo._
import java.time.LocalDate

// Datos de ejemplo definidos dentro del programa (no hay base de datos ni persistencia).
object DatosIniciales {

    private val hoy: LocalDate = LocalDate.now()

    // Fechas relativas a hoy (dia 1 = hace 29 días, dia 30 = ayer),
    // así el dashboard siempre muestra actividad reciente.
    private def fecha(dia: Int): LocalDate = {
        hoy.minusDays((30 - dia).toLong)
    }

    val proveedores: List[Proveedor] = List(
        Proveedor(1, "Distribuidora Andina", "70123456"),
        Proveedor(2, "Lácteos del Valle", "71234567"),
        Proveedor(3, "Limpieza Total", "72345678")
    )

    val productos: List[Producto] = List(
        Producto(1, "Arroz 1 kg", Alimentos, 8.50, 20, 1),
        Producto(2, "Fideo 500 g", Alimentos, 6.00, 15, 1),
        Producto(3, "Aceite 1 L", Alimentos, 16.00, 10, 1),
        Producto(4, "Leche 1 L", Alimentos, 7.50, 25, 2),
        Producto(5, "Gaseosa 2 L", Bebidas, 12.00, 20, 1),
        Producto(6, "Agua mineral 600 ml", Bebidas, 4.00, 30, 1),
        Producto(7, "Jugo de naranja 1 L", Bebidas, 11.00, 10, 2),
        Producto(8, "Detergente 1 kg", Limpieza, 18.00, 8, 3),
        Producto(9, "Lavandina 1 L", Limpieza, 7.00, 10, 3),
        Producto(10, "Papel higiénico x4", Otros, 14.00, 12, 3)
    )

    val movimientos: List[Movimiento] = List(
        // Entradas iniciales
        Entrada(1, 50, fecha(1)),
        Entrada(2, 30, fecha(1)),
        Entrada(3, 20, fecha(1)),
        Entrada(4, 60, fecha(2)),
        Entrada(5, 40, fecha(2)),
        Entrada(6, 80, fecha(2)),
        Entrada(7, 20, fecha(3)),
        Entrada(8, 15, fecha(3)),
        Entrada(9, 12, fecha(3)),
        Entrada(10, 40, fecha(3)),
        // Ventas
        Salida(1, 20, fecha(8)),
        Salida(1, 18, fecha(15)),
        Salida(2, 14, fecha(10)),
        Salida(3, 12, fecha(9)),
        Salida(3, 8, fecha(16)),
        Salida(4, 18, fecha(11)),
        Salida(4, 12, fecha(18)),
        Salida(5, 15, fecha(12)),
        Salida(5, 12, fecha(19)),
        Salida(6, 20, fecha(13)),
        Salida(7, 9, fecha(14)),
        Salida(8, 9, fecha(17)),
        Salida(9, 3, fecha(17)),
        Salida(10, 10, fecha(20)),
        Salida(6, 5, hoy),
        Salida(5, 2, hoy),
        // Ajustes
        Ajuste(9, -1, fecha(21), "Envase dañado")
    )

    val inventario: Inventario = Inventario(productos, proveedores, movimientos)
}
