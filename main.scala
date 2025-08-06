// main.scala
package com.threatdetector

import org.apache.spark.sql.{SparkSession, DataFrame}
import org.apache.spark.sql.functions._
import org.apache.spark.sql.streaming.Trigger
import org.apache.spark.sql.types._

object ThreatDetectorMain {
  
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .appName("ThreatDetector")
      .master("local[*]") // Change to cluster mode for production
      .config("spark.sql.streaming.checkpointLocation", "/tmp/checkpoint")
      .getOrCreate()
    
    spark.sparkContext.setLogLevel("WARN")
    
    try {
      val kafkaDF = readFromKafka(spark)
      val parsedDF = LogParser.parseSecurityLogs(kafkaDF)
      val anomaliesDF = AnomalyDetector.detectThreats(parsedDF)
      
      val query = anomaliesDF
        .writeStream
        .outputMode("append")
        .format("console") // Change to your preferred sink
        .option("truncate", false)
        .trigger(Trigger.ProcessingTime("10 seconds"))
        .start()
      
      query.awaitTermination()
    } finally {
      spark.stop()
    }
  }
  
  private def readFromKafka(spark: SparkSession): DataFrame = {
    spark
      .readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", Config.kafkaBootstrapServers)
      .option("subscribe", Config.kafkaTopic)
      .option("startingOffsets", "latest")
      .load()
      .select(
        col("key").cast("string").as("log_key"),
        col("value").cast("string").as("log_data"),
        col("timestamp").as("kafka_timestamp")
      )
  }
}
