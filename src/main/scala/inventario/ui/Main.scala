package inventario.ui

import scalafx.stage.Screen
import inventario.datos.DatosIniciales
import inventario.modelo.Inventario
import java.time.LocalDate
import scalafx.Includes._
import scalafx.application.JFXApp3
import scalafx.beans.property.ObjectProperty
import scalafx.event.ActionEvent
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.Scene
import scalafx.scene.control.{Button, Label}
import scalafx.scene.layout.{BorderPane, HBox, Region, VBox}

// Capa de interfaz (la única con efectos).
// Lo único que cambia con el tiempo es la referencia al inventario vigente (`estado`);
// la lógica de negocio y el modelo son puros e inmutables.
object Main extends JFXApp3 {

    // Una página del menú lateral
    case class Pagina(
        titulo: String,
        subtitulo: String,
        icono: String,
        contenido: Region
    )

    override def start(): Unit = {
        val estado = ObjectProperty[Inventario](DatosIniciales.inventario)
        val hojaEstilo: Option[String] = Option(Main.getClass.getResource("/estilo.css")).map(url => url.toExternalForm)

        val paginas: List[Pagina] = List(
            Pagina("Dashboard", "Resumen general de tu tienda", "📊", new PanelDashboard(estado).vista),
            Pagina("Inventario", "Productos, stock y movimientos", "📦", new PanelInventario(estado).vista),
            Pagina("Alertas y reposición", "Qué hay que pedir y a quién", "⚠", new PanelAlertas(estado).vista),
            Pagina("Estadísticas", "Valor del inventario e historial", "📈", new PanelEstadisticas(estado).vista),
            Pagina("Registro", "Nuevos productos y proveedores", "➕", new PanelRegistro(estado).vista)
        )

        // ---- Zona central ----
        val centro = new BorderPane {
            padding = Insets(20)
        }
        val tituloPagina = new Label {
            styleClass += "pagina-titulo"
        }
        val subtituloPagina = new Label {
            styleClass += "pagina-subtitulo"
        }

        // ---- Barra lateral ----
        val botones: List[Button] = paginas.map(pagina => {
            new Button(s"${pagina.icono}   ${pagina.titulo}") {
                styleClass += "nav-boton"
                maxWidth = Double.MaxValue
            }
        })

        def seleccionar(pagina: Pagina): Unit = {
            botones.foreach(boton => boton.styleClass -= "nav-activo")
            botones(paginas.indexOf(pagina)).styleClass += "nav-activo"
            centro.center = pagina.contenido
            tituloPagina.text = pagina.titulo
            subtituloPagina.text = pagina.subtitulo
        }

        botones.zip(paginas).foreach { case (boton, pagina) =>
            boton.onAction = (_: ActionEvent) => seleccionar(pagina)
        }

        val logo = new HBox(12) {
            alignment = Pos.CenterLeft
            padding = Insets(24, 20, 20, 20)
            children = Seq(
                Componentes.etiqueta("📦", "logo-icono"),
                new VBox(1) {
                    children = Seq(
                        Componentes.etiqueta("Inventario", "logo-titulo"),
                        Componentes.etiqueta("Tienda · alertas de stock", "logo-subtitulo")
                    )
                }
            )
        }

        val barraLateral = new VBox(4) {
            styleClass += "sidebar"
            prefWidth = 250
            minWidth = 250
        }
        barraLateral.children += logo
        barraLateral.children += Componentes.etiqueta("MENÚ", "nav-seccion")
        botones.foreach(boton => barraLateral.children += boton)
        barraLateral.children += Componentes.espaciadorVertical()
        barraLateral.children += Componentes.etiqueta("Programación Funcional · Scala + ScalaFX", "sidebar-pie")

        // ---- Barra superior ----
        val usuario = new HBox(8) {
            alignment = Pos.CenterLeft
            children = Seq(
                Componentes.etiqueta("A", "avatar"),
                Componentes.etiqueta("Inventario Admin", "usuario-nombre")
            )
        }
        val barraSuperior = new HBox(14) {
            styleClass += "topbar"
            alignment = Pos.CenterLeft
            padding = Insets(14, 24, 14, 24)
            children = Seq(
                new VBox(2) { children = Seq(tituloPagina, subtituloPagina) },
                Componentes.espaciadorHorizontal(),
                Componentes.etiqueta("📅  " + Formato.fechaLarga(LocalDate.now()), "fecha"),
                usuario
            )
        }

        val raiz = new BorderPane {
            left = barraLateral
            top = barraSuperior
            center = centro
        }

        seleccionar(paginas.head)

                // Ajusta la ventana al área visible de la pantalla (descuenta la barra de tareas)
        val area = Screen.primary.visualBounds
        val anchoVentana = math.min(1320.0, area.width * 0.95)
        val altoVentana = math.min(840.0, area.height * 0.92)

        stage = new JFXApp3.PrimaryStage {
            title = "Inventario de tienda con alertas de stock"
            minWidth = 900
            minHeight = 560
            width = anchoVentana
            height = altoVentana
            x = area.minX + (area.width - anchoVentana) / 2
            y = area.minY + (area.height - altoVentana) / 2
            scene = new Scene {
                hojaEstilo.foreach(ruta => stylesheets += ruta)
                root = raiz
            }
        }
    }
}
