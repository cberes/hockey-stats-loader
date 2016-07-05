lazy val root = (project in file(".")).
  settings(
    name := "hockey-stats-scraper",
    version := "1.0",
    scalaVersion := "2.11.8",
    scalacOptions ++= Seq("-feature"),
    libraryDependencies ++= Seq(
      "net.ruippeixotog" %% "scala-scraper" % "1.0.0",
      "org.postgresql" % "postgresql" % "9.4.1208",
      "com.typesafe.play" %% "anorm" % "2.5.0"
    )
  )
