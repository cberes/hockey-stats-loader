lazy val root = (project in file(".")).
  settings(
    name := "hockey-stats-scraper",
    version := "1.0",
    scalaVersion := "2.11.8",
    libraryDependencies += "net.ruippeixotog" %% "scala-scraper" % "1.0.0"
  )
