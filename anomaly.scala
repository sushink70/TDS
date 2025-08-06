// anomaly.scala
package com.threatdetector

import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.functions._
import org.apache.spark.sql.expressions.Window

object AnomalyDetector {
  
  def detectThreats(logsDF: DataFrame): DataFrame = {
    val windowSpec = Window
      .partitionBy("source_ip")
      .orderBy(col("timestamp").desc)
      .rangeBetween(-300, 0) // 5 minute window
    
    val enrichedDF = logsDF
      .withColumn("failed_attempts", 
        when(col("action") === "DENY", 1).otherwise(0))
      .withColumn("request_count", 
        count("*").over(windowSpec))
      .withColumn("failed_count", 
        sum("failed_attempts").over(windowSpec))
    
    enrichedDF
      .withColumn("threat_type", detectThreatType(
        col("failed_count"), 
        col("request_count"), 
        col("destination_port"),
        col("hour_of_day"),
        col("is_internal_ip")
      ))
      .withColumn("risk_score", calculateRiskScore(
        col("failed_count"),
        col("request_count"),
        col("is_internal_ip")
      ))
      .filter(col("threat_type").isNotNull)
  }
  
  private def detectThreatType = udf((
    failedCount: Long, 
    requestCount: Long, 
    destPort: Int,
    hourOfDay: Int,
    isInternal: Boolean
  ) => {
    
    if (failedCount > 50) "BRUTE_FORCE"
    else if (requestCount > 1000 && !isInternal) "DDOS_ATTEMPT" 
    else if (destPort != null && List(22, 23, 3389).contains(destPort) && failedCount > 10) "SSH_RDP_ATTACK"
    else if (hourOfDay < 6 || hourOfDay > 22) "OFF_HOURS_ACCESS"
    else if (requestCount > 100) "PORT_SCAN"
    else null
  })
  
  private def calculateRiskScore = udf((
    failedCount: Long,
    requestCount: Long, 
    isInternal: Boolean
  ) => {
    var score = 0
    
    if (failedCount > 100) score += 80
    else if (failedCount > 50) score += 60
    else if (failedCount > 10) score += 30
    
    if (requestCount > 1000) score += 50
    else if (requestCount > 100) score += 25
    
    if (!isInternal) score += 20
    
    math.min(score, 100)
  })
  
  def detectBruteForce(logsDF: DataFrame): DataFrame = {
    val window5Min = Window
      .partitionBy("source_ip", "destination_ip")
      .orderBy(col("timestamp").desc)
      .rangeBetween(-300, 0)
    
    logsDF
      .filter(col("action") === "DENY")
      .withColumn("failures_5min", count("*").over(window5Min))
      .filter(col("failures_5min") > 20)
      .withColumn("threat_type", lit("BRUTE_FORCE"))
  }
  
  def detectPortScan(logsDF: DataFrame): DataFrame = {
    val windowSpec = Window
      .partitionBy("source_ip")
      .orderBy(col("timestamp").desc)
      .rangeBetween(-60, 0) // 1 minute window
    
    logsDF
      .withColumn("unique_ports", 
        countDistinct("destination_port").over(windowSpec))
      .filter(col("unique_ports") > 10)
      .withColumn("threat_type", lit("PORT_SCAN"))
  }
  
  def detectDDoS(logsDF: DataFrame): DataFrame = {
    val windowSpec = Window
      .partitionBy("source_ip")
      .orderBy(col("timestamp").desc)
      .rangeBetween(-60, 0)
    
    logsDF
      .withColumn("requests_per_min", count("*").over(windowSpec))
      .filter(col("requests_per_min") > 500)
      .withColumn("threat_type", lit("DDOS_ATTEMPT"))
  }
}
