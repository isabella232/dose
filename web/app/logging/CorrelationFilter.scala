package logging

import play.api.mvc.{Cookie, Result, RequestHeader, Filter}
import scala.concurrent.Future
import tyrex.services.UUID
import play.api.libs.concurrent.Execution.Implicits._
import org.slf4j.MDC

object CorrelationFilter {
  def tagRequest(rh: RequestHeader) = {
    val optSessionCorrelationId = rh.cookies.get(CorrelatedLogging.SESSION_CORRELATION_ID).map(_.value)

    val sessionCorrelationId = optSessionCorrelationId.getOrElse(
      rh.headers.get(CorrelatedLogging.SESSION_CORRELATION_ID).getOrElse(UUID.create())
    )

    val requestCorrelationId =
      rh.headers.get(CorrelatedLogging.REQUEST_CORRELATION_ID).getOrElse(UUID.create())

    val correlationTags = Map(
      CorrelatedLogging.SESSION_CORRELATION_ID -> sessionCorrelationId,
      CorrelatedLogging.REQUEST_CORRELATION_ID -> requestCorrelationId
    )

    rh.copy(tags = rh.tags ++ correlationTags)
//    MDC.put(CorrelatedLogging.SESSION_CORRELATION_ID, sessionCorrelationId)
//    MDC.put(CorrelatedLogging.REQUEST_CORRELATION_ID, requestCorrelationId)
//    val result = f()
//    MDC.clear()
//
//    if (optSessionCorrelationId.isEmpty) {
//      val correlationCookie = Cookie(CorrelatedLogging.SESSION_CORRELATION_ID, sessionCorrelationId)
//      result.map(_.withCookies(correlationCookie))
//    } else {
//      result
//    }
  }
}
