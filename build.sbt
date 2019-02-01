import dependencies._

cancelable in Global := true

val commonSettings = Seq(
  organization := "com.clovellytech",
  version := Version.version,
  scalaVersion := Version.scalaVersion,
  resolvers ++= addResolvers,
  scalacOptions ++= options.scalac,
  scalacOptions in (Compile, console) := options.scalacConsole,
  updateOptions := updateOptions.value.withLatestSnapshots(false)
) ++ compilerPlugins

val withTests : String = "compile->compile;test->test"
val testOnly : String = "test->test"

lazy val docs = (project in file("./router-docs"))
  .settings(name := "outwatch-router-docs")
  .enablePlugins(MdocPlugin)
  .settings(commonSettings)
  .dependsOn(router)

lazy val copyFastOptJS = TaskKey[Unit]("copyFastOptJS", "Copy javascript files to target directory")

lazy val router  = (project in file("./outwatch-router"))
  .settings(name := "outwatch-router")
  .enablePlugins(ScalaJSPlugin)
  .enablePlugins(ScalaJSBundlerPlugin)
  .settings(commonSettings)
  .settings(
    scalaJSModuleKind := ModuleKind.CommonJSModule,
    scalacOptions += "-P:scalajs:sjsDefinedByDefault",
    useYarn := true, // makes scalajs-bundler use yarn instead of npm
    jsEnv in Test := new org.scalajs.jsenv.jsdomnodejs.JSDOMNodeJSEnv,
    scalaJSUseMainModuleInitializer := true,
    scalaJSModuleKind := ModuleKind.CommonJSModule, // configure Scala.js to emit a JavaScript module instead of a top-level script
    version in webpack := "4.16.1",
    version in startWebpackDevServer := "3.1.4",
    webpackDevServerExtraArgs := Seq("--progress", "--color"),
    webpackConfigFile in fastOptJS := Some(baseDirectory.value / "webpack.config.dev.js"),
    // https://scalacenter.github.io/scalajs-bundler/cookbook.html#performance
    webpackBundlingMode in fastOptJS := BundlingMode.LibraryOnly(),
    resolvers += "jitpack" at "https://jitpack.io",
    libraryDependencies ++= Seq(
      "io.github.outwatch" % "outwatch" % "ea240c6d04",
      "org.http4s" %% "parboiled" % "1.0.0",
      "org.scalatest" %%% "scalatest" % "3.0.5" % Test
    ),
    copyFastOptJS := {
      val inDir = (crossTarget in (Compile, fastOptJS)).value
      val outDir = (crossTarget in (Compile, fastOptJS)).value / "dev"
      val files = Seq("outwatch-router-fastopt-loader.js", "outwatch-router-frontend-fastopt.js", "outwatch-router-frontend-fastopt.js.map") map { p =>   (inDir / p, outDir / p) }
      IO.copy(files, overwrite = true, preserveLastModified = true, preserveExecutable = true)
    },
    // hot reloading configuration:
    // https://github.com/scalacenter/scalajs-bundler/issues/180
    addCommandAlias("dev", "; compile; fastOptJS::startWebpackDevServer; devwatch; fastOptJS::stopWebpackDevServer"),
    addCommandAlias("devwatch", "~; fastOptJS; copyFastOptJS")
  )

lazy val exampleApp = (project in file("router-example"))
  .settings(name := "outwatch-example")
  .settings(commonSettings)
  .dependsOn(router)

lazy val root = (project in file("."))
  .settings(name := "outwatch-router-root")
  .settings(commonSettings)
  .settings(
    skip in publish := true,
    aggregate in reStart := false,
  )
  .dependsOn(router)
  .aggregate(router)

