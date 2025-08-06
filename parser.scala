// parser.scala
package com.threatdetector

import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._

object LogParser {
  
  val logSchema = StructType(Array(
    StructField("timestamp", TimestampType, true),
    StructField("source_ip", StringType, true),
    StructField("destination_ip", StringType, true),
    StructField("source_port", IntegerType, true),
    StructField("destination_port", IntegerType, true),
    StructField("protocol", StringType, true),
    StructField("action", StringType, true),
    StructField("bytes", LongType, true),
    StructField("log_level", StringType, true),
    StructField("message", StringType, true)
  ))
  
  def parseSecurityLogs(kafkaDF: DataFrame): DataFrame = {
    kafkaDF
      .select(
        from_json(col("log_data"), logSchema).as("parsed_log"),
        col("kafka_timestamp")
      )
      .select("parsed_log.*", "kafka_timestamp")
      .filter(col("source_ip").isNotNull)
      .withColumn("hour_of_day", hour(col("timestamp")))
      .withColumn("day_of_week", dayofweek(col("timestamp")))
      .withColumn("is_internal_ip", isInternalIP(col("source_ip")))
  }
  
  private def isInternalIP = udf((ip: String) => {
    if (ip == null) false
    else {
      val parts = ip.split("\\.")
      if (parts.length != 4) false
      else {
        val first = parts(0).toInt
        val second = parts(1).toInt
        // Check for private IP ranges
        first == 10 || 
        (first == 172 && second >= 16 && second <= 31) ||
        (first == 192 && second == 168)
      }
    }
  })
  
  def parseFirewallLogs(logData: String): Map[String, Any] = {
    // Implement specific firewall log parsing logic
    Map(
      "timestamp" -> System.currentTimeMillis(),
      "source_ip" -> extractIP(logData),
      "action" -> extractAction(logData)
    )
  }
  
  def parseWebLogs(logData: String): Map[String, Any] = {
    // Implement web server log parsing logic
    Map()
  }
  
  private def extractIP(log: String): String = {
    val ipPattern = """(\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3})""".r
    ipPattern.findFirstIn(log).getOrElse("unknown")
  }
  
  private def extractAction(log: String): String = {
    if (log.toLowerCase.contains("deny") || log.toLowerCase.contains("drop")) "DENY"
    else if (log.toLowerCase.contains("allow") || log.toLowerCase.contains("accept")) "ALLOW"
    else "UNKNOWN"
  }
}
