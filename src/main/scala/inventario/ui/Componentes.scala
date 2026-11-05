package inventario.ui

import inventario.logica.LineaReposicion
import inventario.modelo._
import scalafx.Includes._
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.Node
import scalafx.scene.control.{Label, ProgressBar, Separator}
import scalafx.scene.layout.{HBox, Priority, Region, VBox}

// Piezas visuales reutilizables (los colores y tamaños viven en estilo.css).
object Componentes {

    // ------------------------------------------------------------------
    //  Piezas básicas
    // ------------------------------------------------------------------

    def etiqueta(texto: String, clase: String): Label = {
        new Label(texto) {
            styleClass += clase
        }
    }

    def espaciadorHorizontal(): Region = {
        val relleno = new Region
        HBox.setHgrow(relleno, Priority.Always)
        relleno
    }

    def espaciadorVertical(): Region = {
        val relleno = new Region
        VBox.setVgrow(relleno, Priority.Always)
        relleno
    }

    // Un campo de formulario con su título pequeño arriba
    def campoEtiquetado(titulo: String, campo: Node): VBox = {
        new VBox(4) {
            children = Seq(etiqueta(titulo, "campo-etiqueta"), campo)
        }
    }

    // Muestra un mensaje de éxito (verde) o de error (rojo)
    def mostrarMensaje(mensaje: Label, texto: String, exito: Boolean): Unit = {
        mensaje.styleClass --= List("mensaje-ok", "mensaje-error")
        mensaje.styleClass += (if (exito) "mensaje-ok" else "mensaje-error")
        mensaje.text = (if (exito) "✔  " else "✖  ") + texto
    }

    // ------------------------------------------------------------------
    //  Etiquetas de color y barras
    // ------------------------------------------------------------------

    def chip(texto: String, clase: String): Label = {
        new Label(texto) {
            styleClass ++= Seq("chip", clase)
        }
    }

    def chipNivel(nivel: NivelStock): Label = {
        chip(nivel.etiqueta(), s"chip-${Formato.claseNivel(nivel)}")
    }

    def chipMovimiento(mov: Movimiento): Label = {
        chip(mov.tipo(), s"chip-${Formato.claseMovimiento(mov)}")
    }

    // La barra se llena por completo cuando el stock llega al doble del mínimo
    def barraCobertura(stock: Int, minimo: Int, nivel: NivelStock): ProgressBar = {
        val referencia = math.max(minimo * 2, 1)
        val avance = math.min(math.max(stock, 0).toDouble / referencia, 1.0)
        new ProgressBar {
            progress = avance
            prefWidth = 100
            styleClass += s"barra-${Formato.claseNivel(nivel)}"
        }
    }

    // ------------------------------------------------------------------
    //  Tarjetas
    // ------------------------------------------------------------------

    // Tarjeta de resumen: título pequeño, valor grande e icono dentro de un círculo de color
    def tarjetaEstadistica(titulo: String, valor: Label, icono: String, clase: String): HBox = {
        valor.styleClass += "kpi-valor"
        val textos = new VBox(4) {
            children = Seq(etiqueta(titulo, "kpi-titulo"), valor)
        }
        val circulo = new Label(icono) {
            styleClass ++= Seq("kpi-icono", clase)
        }
        val caja = new HBox(12) {
            styleClass += "kpi-card"
            alignment = Pos.CenterLeft
            children = Seq(textos, espaciadorHorizontal(), circulo)
        }
        HBox.setHgrow(caja, Priority.Always)
        caja.maxWidth = Double.MaxValue
        caja.prefWidth = 200
        caja
    }

    // Banner con degradado: un título y dos líneas de resumen
    def banner(titulo: String, linea1: Label, linea2: Label, clase: String): VBox = {
        linea1.styleClass += "banner-linea"
        linea2.styleClass += "banner-linea"
        val caja = new VBox(4) {
            styleClass ++= Seq("banner", clase)
            alignment = Pos.CenterLeft
            children = Seq(etiqueta(titulo, "banner-titulo"), linea1, linea2)
        }
        HBox.setHgrow(caja, Priority.Always)
        caja.maxWidth = Double.MaxValue
        caja.prefWidth = 300
        caja
    }

    // Tarjeta blanca con título y contenido; `ancho` es el ancho preferido (crece si hay espacio)
    def tarjeta(titulo: String, contenido: Node, ancho: Double): VBox = {
        val caja = new VBox(12) {
            styleClass += "card"
            children = Seq(etiqueta(titulo, "card-titulo"), contenido)
        }
        VBox.setVgrow(contenido, Priority.Always)
        HBox.setHgrow(caja, Priority.Always)
        caja.maxWidth = Double.MaxValue
        caja.prefWidth = ancho
        caja
    }

    // Tarjeta de reposición: un proveedor con los productos que hay que pedirle
    def tarjetaReposicion(proveedor: Proveedor, lineas: List[LineaReposicion]): VBox = {
        val totalUnidades = lineas.map(linea => linea.sugerido).sum

        val encabezado = new HBox(8) {
            alignment = Pos.CenterLeft
            children = Seq(
                etiqueta("🚚", "repo-icono"),
                etiqueta(proveedor.nombre, "repo-proveedor"),
                espaciadorHorizontal(),
                etiqueta(s"Tel. ${proveedor.telefono}", "repo-detalle")
            )
        }

        val filas: List[Node] = lineas.map(linea => {
            new HBox(8) {
                alignment = Pos.CenterLeft
                children = Seq(
                    etiqueta(linea.producto.nombre, "repo-producto"),
                    espaciadorHorizontal(),
                    etiqueta(s"stock ${linea.stock} / mín. ${linea.producto.stockMinimo}", "repo-detalle"),
                    chip(s"pedir ${linea.sugerido} u.", "chip-pedido")
                )
            }
        })

        val caja = new VBox(8) {
            styleClass += "repo-card"
            padding = Insets(14)
        }
        caja.children += encabezado
        caja.children += new Separator
        filas.foreach(fila => caja.children += fila)
        caja.children += etiqueta(s"Total sugerido: $totalUnidades unidades", "repo-total")
        caja
    }
}
