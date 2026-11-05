// =====================================================================
//  Inventario de tienda con alertas de stock
//  Proyecto final de Programación Funcional (Scala + ScalaFX)
// =====================================================================

ThisBuild / scalaVersion := "2.13.18"
ThisBuild / version      := "1.1.0"

// JavaFX necesita el binario nativo de cada sistema operativo
lazy val osClasificador: String = {
    val os   = System.getProperty("os.name")
    val arch = System.getProperty("os.arch")
    if (os.startsWith("Linux")) "linux"
    else if (os.startsWith("Mac")) { if (arch == "aarch64") "mac-aarch64" else "mac" }
    else if (os.startsWith("Windows")) "win"
    else throw new Exception(s"Sistema operativo no soportado: $os")
}

lazy val root = (project in file("."))
    .settings(
        name := "inventario-tienda",
        libraryDependencies ++= {
            // base, controls (incluye los gráficos de barras, líneas y pastel) y graphics
            val modulosFx = Seq("base", "controls", "graphics")
            Seq(
                "org.scalafx" %% "scalafx" % "21.0.0-R32",
                "org.scalameta" %% "munit" % "1.0.0" % Test
            ) ++ modulosFx.map(m => "org.openjfx" % s"javafx-$m" % "21.0.4" classifier osClasificador)
        },
        scalacOptions ++= Seq("-encoding", "UTF-8", "-deprecation", "-feature", "-unchecked", "-Xlint"),
        // `sbt run` abre la interfaz gráfica
        Compile / mainClass := Some("inventario.ui.Main"),
        // JavaFX debe ejecutarse en un proceso aparte
        fork := true
    )
