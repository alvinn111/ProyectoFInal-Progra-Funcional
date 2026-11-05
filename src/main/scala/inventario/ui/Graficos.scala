package inventario.ui

import javafx.geometry.Side
import javafx.scene.chart.{BarChart, CategoryAxis, LineChart, NumberAxis, PieChart, XYChart}
import scala.jdk.CollectionConverters._
import scalafx.scene.layout.StackPane

// Gráficos reutilizables. Envuelven a los gráficos de JavaFX para que el resto de la
// interfaz solo tenga que llamar a `mostrar(datos)` con una lista de pares (etiqueta, valor).

// Una barra por cada (etiqueta, valor)
class GraficoBarras {

    private val grafico = new BarChart[String, Number](new CategoryAxis(), new NumberAxis())
    grafico.setLegendVisible(false)
    grafico.setAnimated(false)
    grafico.setCategoryGap(24)
    grafico.setPrefHeight(270)

    val vista: StackPane = new StackPane {
        delegate.getChildren.add(grafico)
    }

    def mostrar(datos: List[(String, Double)]): Unit = {
        val puntos = datos.map(par => new XYChart.Data[String, Number](par._1, java.lang.Double.valueOf(par._2)))
        val serie = new XYChart.Series[String, Number]()
        serie.getData.setAll(puntos.asJava)
        grafico.getData.setAll(List(serie).asJava)
    }
}

// Una línea que une los puntos (etiqueta, valor), por ejemplo ventas por día
class GraficoLineas {

    private val grafico = new LineChart[String, Number](new CategoryAxis(), new NumberAxis())
    grafico.setLegendVisible(false)
    grafico.setAnimated(false)
    grafico.setCreateSymbols(true)
    grafico.setPrefHeight(270)

    val vista: StackPane = new StackPane {
        delegate.getChildren.add(grafico)
    }

    def mostrar(datos: List[(String, Double)]): Unit = {
        val puntos = datos.map(par => new XYChart.Data[String, Number](par._1, java.lang.Double.valueOf(par._2)))
        val serie = new XYChart.Series[String, Number]()
        serie.getData.setAll(puntos.asJava)
        grafico.getData.setAll(List(serie).asJava)
    }
}

// Una porción por cada (etiqueta, valor); la leyenda indica a qué corresponde cada color
class GraficoPastel {

    private val grafico = new PieChart()
    grafico.setLabelsVisible(false)
    grafico.setAnimated(false)
    grafico.setLegendSide(Side.BOTTOM)
    grafico.setPrefHeight(270)

    val vista: StackPane = new StackPane {
        delegate.getChildren.add(grafico)
    }

    def mostrar(datos: List[(String, Double)]): Unit = {
        val porciones = datos.map(par => new PieChart.Data(par._1, par._2))
        grafico.getData.setAll(porciones.asJava)
    }
}
