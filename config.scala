// config.scala
package com.threatdetector

import com.typesafe.config.ConfigFactory

object Config {
  private val config = ConfigFactory.load()
  
  // Kafka Configuration
  val kafkaBootstrapServers: String = config.getString("kafka.bootstrap.servers")
  val kafkaTopic: String = config.getString("kafka.topic")
  val kafkaGroupId: String = config.getString("kafka.group.id")
  
  // Spark Configuration
  val sparkAppName: String = config.getString("spark.app.name")
  val sparkMaster: String = config.getString("spark.master")
  val checkpointLocation: String = config.getString("spark.checkpoint.location")
  
  // Detection Thresholds
  val bruteForceThreshold: Int = config.getInt("detection.brute_force.threshold")
  val ddosThreshold: Int = config.getInt("detection.ddos.threshold")
  val portScanThreshold: Int = config.getInt("detection.port_scan.threshold")
  
  // Output Configuration
  val outputFormat: String = config.getString("output.format")
  val outputPath: String = config.getString("output.path")
  val elasticsearchHost: String = config.getString("elasticsearch.host")
  val elasticsearchPort: Int = config.getInt("elasticsearch.port")
  val elasticsearchIndex: String = config.getString("elasticsearch.index")
  
  // Alert Configuration
  val alertEnabled: Boolean = config.getBoolean("alerts.enabled")
  val slackWebhook: String = config.getString("alerts.slack.webhook")
  val emailHost: String = config.getString("alerts.email.host")
  val emailFrom: String = config.getString("alerts.email.from")
  val emailTo: String = config.getString("alerts.email.to")
}

// application.conf file content (put in src/main/resources/)
/*
kafka {
  bootstrap.servers = "localhost:9092"
  topic = "security-logs"
  group.id = "threat-detector"
}

spark {
  app.name = "ThreatDetector"
  master = "local[*]"
  checkpoint.location = "/tmp/spark-checkpoint"
}

detection {
  brute_force {
    threshold = 50
  }
  ddos {
    threshold = 1000
  }
  port_scan {
    threshold = 10
  }
}

output {
  format = "elasticsearch"
  path = "/tmp/threat-output"
}

elasticsearch {
  host = "localhost"
  port = 9200
  index = "security-threats"
}

alerts {
  enabled = true
  slack {
    webhook = "https://hooks.slack.com/your-webhook"
  }
  email {
    host = "smtp.gmail.com"
    from = "alerts@company.com"
    to = "security@company.com"
  }
}
*/
