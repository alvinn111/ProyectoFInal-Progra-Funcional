package inventario

import inventario.datos.DatosIniciales
import inventario.logica._
import inventario.modelo._
import java.time.LocalDate

// Pruebas de la lógica, modelo incluido, SIN interfaz gráfica:  sbt test
class LogicaSuite extends munit.FunSuite {

    private val inv = DatosIniciales.inventario
    private val hoy = LocalDate.now()

    private def nombres(productos: List[Producto]): List[String] = {
        productos.map(producto => producto.nombre)
    }

    // ---------- Stock y niveles ----------

    test("el stock se calcula sumando entradas, salidas y ajustes") {
        assertEquals(Stock.actual(inv, 1), 12) // arroz: +50 -20 -18
        assertEquals(Stock.actual(inv, 3), 0) // aceite: +20 -12 -8
        assertEquals(Stock.actual(inv, 9), 8) // lavandina: +12 -3 -1 (ajuste)
    }

    test("el nivel de stock se clasifica según el mínimo") {
        assertEquals(Stock.nivel(0, 10), Agotado)
        assertEquals(Stock.nivel(5, 10), Bajo)
        assertEquals(Stock.nivel(10, 10), Suficiente)
    }

    test("las alertas incluyen los productos agotados y los de stock bajo") {
        val alertas = Stock.alertas(inv)
        assertEquals(alertas.size, 5)
        assertEquals(alertas.filter(estado => estado.nivel == Agotado).map(estado => estado.producto.nombre), List("Aceite 1 L"))
    }

    // ---------- Registro de movimientos ----------

    test("una entrada válida devuelve un inventario nuevo y deja intacto el original") {
        Operaciones.registrarMovimiento(inv, Entrada(3, 30, hoy)) match {
            case Exito(nuevo) =>
                assertEquals(Stock.actual(nuevo, 3), 30)
                assertEquals(Stock.actual(inv, 3), 0)
            case Fallo(error) => fail(error.mensaje())
        }
    }

    test("no se puede vender más de lo disponible") {
        assertEquals(
            Operaciones.registrarMovimiento(inv, Salida(1, 100, hoy)),
            Fallo(StockInsuficiente(12, 100))
        )
    }

    test("se rechazan cantidades inválidas, ajustes sin motivo y productos inexistentes") {
        assertEquals(Operaciones.registrarMovimiento(inv, Entrada(1, 0, hoy)), Fallo(CantidadInvalida(0)))
        assertEquals(
            Operaciones.registrarMovimiento(inv, Ajuste(1, -1, hoy, "  ")),
            Fallo(DatoInvalido("El ajuste necesita un motivo"))
        )
        assertEquals(Operaciones.registrarMovimiento(inv, Salida(99, 1, hoy)), Fallo(ProductoInexistente(99)))
    }

    test("se registran proveedores y productos nuevos y se rechazan los nombres repetidos") {
        Operaciones.registrarProveedor(inv, "Nuevo Proveedor", "70000000") match {
            case Exito(nuevo) => assertEquals(nuevo.proveedores.size, 4)
            case Fallo(error) => fail(error.mensaje())
        }
        assertEquals(
            Operaciones.registrarProveedor(inv, "limpieza total", "1"),
            Fallo(DatoInvalido("Ya existe un proveedor con ese nombre"))
        )
        Operaciones.registrarProducto(inv, "Sal 1 kg", Alimentos, 3.0, 10, 1) match {
            case Exito(nuevo) => assertEquals(nuevo.productos.last.id, 11)
            case Fallo(error) => fail(error.mensaje())
        }
    }

    // ---------- Búsqueda (criterios como funciones) ----------

    test("la búsqueda recibe el criterio como parámetro y se pueden combinar criterios") {
        assertEquals(
            nombres(Busqueda.buscar(inv)(Busqueda.porCategoria(Bebidas))),
            List("Gaseosa 2 L", "Agua mineral 600 ml", "Jugo de naranja 1 L")
        )
        assertEquals(nombres(Busqueda.buscar(inv)(Busqueda.porNombre("ARROZ"))), List("Arroz 1 kg"))
        assertEquals(
            nombres(Busqueda.buscar(inv)(Busqueda.porRangoPrecio(10, 12))),
            List("Gaseosa 2 L", "Jugo de naranja 1 L")
        )
        val bebidasBajas = Busqueda.ambos(Busqueda.porCategoria(Bebidas), Busqueda.porNivel(inv, Bajo))
        assertEquals(nombres(Busqueda.buscar(inv)(bebidasBajas)), List("Gaseosa 2 L"))
    }

    // ---------- Estadísticas y reportes ----------

    test("el valor del inventario es la suma de stock por precio") {
        assertEqualsDouble(Estadisticas.valorTotal(inv), 1480.0, 0.001)
    }

    test("el producto más vendido es el que tiene más unidades en salidas") {
        assertEquals(Estadisticas.masVendido(inv).map(par => (par._1.nombre, par._2)), Some(("Arroz 1 kg", 38)))
    }

    test("las ventas por fecha incluyen los días sin ventas") {
        assertEquals(
            Estadisticas.ventasPorFecha(inv, hoy.minusDays(1), hoy),
            List((hoy.minusDays(1), 0), (hoy, 7))
        )
    }

    test("la agrupación genérica sirve para cualquier tipo de elemento y clave") {
        assertEquals(
            Agrupacion.agruparPor(List(1, 2, 3, 4, 5, 6))(n => n % 2),
            Map(1 -> List(1, 3, 5), 0 -> List(2, 4, 6))
        )
    }

    test("el reporte de reposición agrupa por proveedor y sugiere llegar al doble del mínimo") {
        val reporte = Reposicion.reporte(inv).map(par => (par._1.nombre, par._2.map(linea => linea.producto.nombre)))
        assertEquals(
            reporte,
            List(
                ("Distribuidora Andina", List("Arroz 1 kg", "Aceite 1 L", "Gaseosa 2 L")),
                ("Limpieza Total", List("Detergente 1 kg", "Lavandina 1 L"))
            )
        )
        assertEquals(Reposicion.cantidadSugerida(inv.productos.head, 12), 28)
    }

    // ---------- Resultado ----------

    test("Resultado transforma y encadena éxitos y propaga los fallos") {
        val exito: Resultado[Int] = Exito(2)
        val fallo: Resultado[Int] = Fallo(DatoInvalido("x"))
        assertEquals(exito.map(n => n + 1), Exito(3))
        assertEquals(exito.flatMap(n => Exito(n * 10)), Exito(20))
        assertEquals(fallo.map(n => n + 1), Fallo(DatoInvalido("x")))
        assertEquals(Resultado.desdeOpcion(Option.empty[Int], DatoInvalido("falta")), Fallo(DatoInvalido("falta")))
    }
}
