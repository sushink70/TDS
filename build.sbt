name := "threat-detector"

version := "1.0.0"

scalaVersion := "2.12.17"

val sparkVersion = "3.4.1"
val kafkaVersion = "3.5.1"

libraryDependencies ++= Seq(
  // Spark Core
  "org.apache.spark" %% "spark-core" % sparkVersion,
  "org.apache.spark" %% "spark-sql" % sparkVersion,
  "org.apache.spark" %% "spark-streaming" % sparkVersion,
  "org.apache.spark" %% "spark-mllib" % sparkVersion,
  
  // Kafka
  "org.apache.spark" %% "spark-sql-kafka-0-10" % sparkVersion,
  "org.apache.kafka" %% "kafka" % kafkaVersion,
  
  // Configuration
  "com.typesafe" % "config" % "1.4.2",
  
  // JSON Processing
  "io.spray" %% "spray-json" % "1.3.6",
  "com.fasterxml.jackson.core" % "jackson-databind" % "2.15.2",
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.15.2",
  
  // HTTP Server (Akka HTTP)
  "com.typesafe.akka" %% "akka-http" % "10.2.10",
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.2.10",
  "com.typesafe.akka" %% "akka-actor-typed" % "2.6.20",
  "com.typesafe.akka" %% "akka-stream" % "2.6.20",
  
  // Database Connectors
  "org.elasticsearch" %% "elasticsearch-spark-30" % "8.8.2",
  "org.postgresql" % "postgresql" % "42.6.0",
  "com.zaxxer" % "HikariCP" % "5.0.1",
  
  // Logging
  "org.slf4j" % "slf4j-api" % "1.7.36",
  "ch.qos.logback" % "logback-classic" % "1.2.12",
  
  // Testing
  "org.scalatest" %% "scalatest" % "3.2.16" % Test,
  "org.apache.spark" %% "spark-core" % sparkVersion % Test classifier "tests",
  "org.apache.spark" %% "spark-sql" % sparkVersion % Test classifier "tests"
)

// Assembly settings for creating fat JAR
assembly / assemblyMergeStrategy := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case "reference.conf" => MergeStrategy.concat
  case _ => MergeStrategy.first
}

// Exclude Scala library from assembly (if running on cluster with Scala)
assembly / assemblyOption := (assembly / assemblyOption).value.copy(includeScala = false)

// JVM settings
ThisBuild / javacOptions ++= Seq("-source", "11", "-target", "11")
ThisBuild / scalacOptions ++= Seq("-target:jvm-11")

// Fork JVM for tests
Test / fork := true
Test / javaOptions ++= Seq("-Xmx2G")

// Spark submit settings
sparkVersion := sparkVersion
sparkSubmitSparkArgs := Map(
  "--class" -> "com.threatdetector.ThreatDetectorMain",
  "--master" -> "local[*]",
  "--driver-memory" -> "2g",
  "--executor-memory" -> "4g"
)
