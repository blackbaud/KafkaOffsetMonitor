name := "KafkaOffsetMonitor"

version := "0.3.0-SNAPSHOT"

scalaVersion := "2.11.8"

organization := "com.quantifind"

scalacOptions ++= Seq("-deprecation", "-unchecked", "-optimize")

mainClass in Compile := Some("com.quantifind.kafka.offsetapp.OffsetGetterWeb")

libraryDependencies ++= Seq(
      "log4j" % "log4j" % "1.2.17",
      "net.databinder" %% "unfiltered-filter" % "0.8.4",
      "net.databinder" %% "unfiltered-jetty" % "0.8.4",
      "net.databinder" %% "unfiltered-json4s" % "0.8.4",
      "com.quantifind" %% "sumac" % "0.3.0",
      "com.typesafe.slick" %% "slick" % "2.1.0",
      "org.xerial" % "sqlite-jdbc" % "3.7.2",
      "com.twitter" %% "util-core" % "6.39.0",
      "org.reflections" % "reflections" % "0.9.10",
      "org.scalatest" %% "scalatest" % "2.2.4" % "test",
      "org.mockito" % "mockito-all" % "1.10.19" % "test",
      "org.apache.kafka" %% "kafka" % "0.10.0.0")

assemblyMergeStrategy in assembly := {
  case "about.html" => MergeStrategy.discard
  case x => {
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
  }
}
