scalaVersion := "2.13.3"

// Monix Minitest:
libraryDependencies += "io.monix" %% "minitest" % "2.8.2" % "test"
testFrameworks += new TestFramework("minitest.runner.Framework")

// Hedgehog:
val hedgehogVersion = "0.4.2"
libraryDependencies ++= Seq(
  "qa.hedgehog" %% "hedgehog-core" % hedgehogVersion,
  "qa.hedgehog" %% "hedgehog-runner" % hedgehogVersion,
  "qa.hedgehog" %% "hedgehog-minitest" % hedgehogVersion,
  "org.scala-lang.modules" %% "scala-xml" % "1.3.0",
  "net.sf.saxon" % "Saxon-HE" % "10.1"
)

scalacOptions ++= Seq(
  //"-encoding", "utf8", // Option and arguments on same line
  //"-Xfatal-warnings",  // New lines for each options
  "-deprecation")