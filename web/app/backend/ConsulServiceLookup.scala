package backend

import java.net.URLEncoder

import models.consul.HealthInfo
import play.api.Logger
import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json.{JsSuccess, Json}
import play.api.libs.ws.WS

import scala.concurrent.Future
import scala.concurrent.duration._

class ConsulServiceLookup(baseUrl: String, serviceName: String) {
  private val lookupRequest = WS.url(baseUrl + URLEncoder.encode(serviceName, "UTF-8") + "?passing")
  private val timeout = 5.seconds
  @volatile
  private var lastLookup = (Deadline.now, Seq.empty[HealthInfo])

  def lookup: Future[Seq[HealthInfo]] = {
    val last = lastLookup
    if (last._1.isOverdue()) {
      Logger.info(s"Lookup service $serviceName from consul")
      lookupRequest.get().map {
        response =>
          if (response.status == 200) {
            Json.fromJson[Seq[HealthInfo]](Json.parse(response.body)) match {
              case JsSuccess(healthInfo, _) =>
                lastLookup = (timeout.fromNow, healthInfo)
                healthInfo
              case error =>
                Logger.error(s"Invalid json response from consul: $error")
                last._2
            }
          } else {
            Logger.error(s"Request to consul failed to status: ${response.status}")
            last._2
          }
      }.recover {
        case t: Throwable =>
          Logger.error("Request to consul failed", t)
          last._2
      }
    } else {
      Future.successful(last._2)
    }
  }
}
