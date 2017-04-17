
lazy val akkajs =
  project
    .in(file("."))
    .enablePlugins(ScalaJSPlugin)
    .settings(
      name := "akkajs",
      organization := "org.akka-js",
      scalaVersion := "2.12.1",
      scalacOptions := Seq("-feature", "-language:_", "-deprecation"),
      libraryDependencies ++= Seq(
        "org.akka-js" %%% "akkajsactor" % "1.2.5.0"
      ),
      scalaJSModuleKind := ModuleKind.CommonJSModule,
      skip in packageJSDependencies := false
    )
