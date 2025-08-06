// LogProducer.scala - For generating test data
package com.threatdetector.producer

import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.apache.kafka.common.serialization.StringSerializer
import spray.json._
import DefaultJsonProtocol._

import java.time.Instant
import java.util.{Properties, Random}
import scala.concurrent.duration._

case class SecurityLog(
  timestamp: String,
  source_ip: String,
  destination_ip: String,
  source_port: Int,
  destination_port: Int,
  protocol: String,
  action: String,
  bytes: Long,
  log_level: String,
  message: String
)

object LogProducer {
  
  implicit val securityLogFormat = jsonFormat10(SecurityLog)
  
  def main(args: Array[String]): Unit = {
    val props = new Properties()
    props.put("bootstrap.servers", "localhost:9092")
    props.put("key.serializer", classOf[StringSerializer].getName)
    props.put("value.serializer", classOf[StringSerializer].getName)
    
    val producer = new KafkaProducer[String, String](props)
    val random = new Random()
    val topic = "security-logs"
    
    println("🚀 Starting log producer...")
    
    try {
      while (true) {
        val log = generateSampleLog(random)
        val record = new ProducerRecord[String, String](
          topic, 
          log.source_ip, 
          log.toJson.toString
        )
        
        producer.send(record)
        println(s"📝 Sent: ${log.source_ip} -> ${log.destination_ip} (${log.action})")
        
        Thread.sleep(100 + random.nextInt(900)) // Random delay 100-1000ms
      }
    } finally {
      producer.close()
    }
  }
  
  def generateSampleLog(random: Random): SecurityLog = {
    val sourceIPs = Array(
      "192.168.1.100", "192.168.1.101", "10.0.0.50", 
      "172.16.0.25", "203.0.113.5", "198.51.100.10"
    )
    
    val destIPs = Array(
      "192.168.1.1", "10.0.0.1", "172.16.0.1",
      "8.8.8.8", "1.1.1.1", "208.67.222.222"
    )
    
    val protocols = Array("TCP", "UDP", "ICMP")
    val actions = Array("ALLOW", "DENY", "DROP")
    val ports = Array(22, 23, 80, 443, 3389, 21, 25, 53, 8080, 9090)
    
    // Generate some suspicious patterns
    val isSuspicious = random.nextDouble() < 0.3 // 30% suspicious activity
    val sourceIp = sourceIPs(random.nextInt(sourceIPs.length))
    val action = if (isSuspicious && random.nextBoolean()) "DENY" else actions(random.nextInt(actions.length))
    
    SecurityLog(
      timestamp = Instant.now().toString,
      source_ip = sourceIp,
      destination_ip = destIPs(random.nextInt(destIPs.length)),
      source_port = 1024 + random.nextInt(64512),
      destination_port = ports(random.nextInt(ports.length)),
      protocol = protocols(random.nextInt(protocols.length)),
      action = action,
      bytes = random.nextLong() % 10000,
      log_level = if (action == "DENY") "WARN" else "INFO",
      message = generateMessage(action, sourceIp)
    )
  }
  
