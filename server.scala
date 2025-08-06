// server.scala
package com.threatdetector.dashboard

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import spray.json._
import DefaultJsonProtocol._

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}

case class ThreatEvent(
  timestamp: String,
  sourceIp: String,
  threatType: String,
  riskScore: Int,
  details: String
)

case class ThreatStats(
  totalThreats: Int,
  activeThreatsByType: Map[String, Int],
  topSourceIPs: List[String]
)

object DashboardServer {
  
  implicit val system: ActorSystem = ActorSystem("dashboard-system")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  
  // JSON formatters
  implicit val threatEventFormat = jsonFormat5(ThreatEvent)
  implicit val threatStatsFormat = jsonFormat3(ThreatStats)
  
  val threatService = new ThreatService()
  
  val route: Route = 
    pathPrefix("api") {
      concat(
        path("threats") {
          get {
            parameters("limit".as[Int] ? 100, "severity".?) { (limit, severity) =>
              val threats = threatService.getRecentThreats(limit, severity)
              complete(HttpEntity(ContentTypes.`application/json`, threats.toJson.toString))
            }
          }
        },
        path("threats" / "stats") {
          get {
            val stats = threatService.getThreatStats()
            complete(HttpEntity(ContentTypes.`application/json`, stats.toJson.toString))
          }
        },
        path("threats" / "realtime") {
          get {
            // WebSocket endpoint for real-time updates
            complete("WebSocket endpoint - implement with Akka Streams")
          }
        },
        pathPrefix("static") {
          getFromResourceDirectory("web")
        }
      )
    } ~
    pathSingleSlash {
      getFromResource("web/index.html")
    }
  
  def main(args: Array[String]): Unit = {
    val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)
    
    bindingFuture.onComplete {
      case Success(binding) =>
        println(s"Dashboard server online at http://localhost:8080/")
      case Failure(ex) =>
        println(s"Failed to bind HTTP server: ${ex.getMessage}")
        system.terminate()
    }
  }
}

class ThreatService {
  
  // Mock data - replace with actual database queries
  def getRecentThreats(limit: Int, severity: Option[String]): List[ThreatEvent] = {
    List(
      ThreatEvent("2023-12-01T10:30:00Z", "192.168.1.100", "BRUTE_FORCE", 85, "Multiple failed login attempts"),
      ThreatEvent("2023-12-01T10:25:00Z", "10.0.0.50", "PORT_SCAN", 70, "Scanning multiple ports"),
      ThreatEvent("2023-12-01T10:20:00Z", "172.16.0.25", "DDOS_ATTEMPT", 95, "High volume requests")
    ).take(limit)
  }
  
  def getThreatStats(): ThreatStats = {
    ThreatStats(
      totalThreats = 1247,
      activeThreatsByType = Map(
        "BRUTE_FORCE" -> 45,
        "DDOS_ATTEMPT" -> 12,
        "PORT_SCAN" -> 78,
        "OFF_HOURS_ACCESS" -> 23
      ),
      topSourceIPs = List("192.168.1.100", "10.0.0.50", "172.16.0.25")
    )
  }
  
  def getThreatsFromElasticsearch(): Future[List[ThreatEvent]] = {
    // Implement Elasticsearch query
    Future.successful(List.empty)
  }
  
  def getThreatsFromDatabase(): Future[List[ThreatEvent]] = {
    // Implement database query
    Future.successful(List.empty)
  }
}
