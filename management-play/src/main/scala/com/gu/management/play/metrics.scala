package com.gu.management.play

import javax.inject.Inject
import _root_.play.api.mvc._
import akka.stream.Materializer
import com.gu.management._

import scala.concurrent.Future
import scala.util.Try

abstract class MetricsFilter()(implicit val mat: Materializer) extends Filter {
  val metrics: Seq[Metric]
}

case class RequestMetrics @Inject()(implicit val mat:Materializer) {

  object Standard {
    val knownResultTypeCounters = List(OkCounter(), RedirectCounter(), NotFoundCounter(), ErrorCounter())

    val otherCounter = OtherCounter(knownResultTypeCounters)

    val asFilters: List[MetricsFilter] = List(TimingFilter, CountersFilter(otherCounter :: knownResultTypeCounters))

    val asMetrics: List[Metric] = asFilters.flatMap(_.metrics).distinct
  }

  object TimingFilter extends MetricsFilter {

    val timingMetric = new TimingMetric("performance", "request_duration", "Client requests", "incoming requests to the application")

    val metrics = Seq(timingMetric)

    override def apply(next: RequestHeader => Future[Result])(request: RequestHeader): Future[Result] = {
      implicit val ec = mat.executionContext
      val s = new StopWatch
      next(request).map { result =>
        timingMetric.recordTimeSpent(s.elapsed)
        result
      }
    }
  }

  object CountersFilter {
    def apply(counters: List[Counter]): MetricsFilter = new MetricsFilter {

      implicit val ec = mat.executionContext
      val metrics = counters.map(_.countMetric)

      override def apply(next: RequestHeader => Future[Result])(request: RequestHeader): Future[Result] = {
        val result = next(request)
        result.onComplete(resultTry => counters.foreach(_.submit(resultTry)))
        result
      }
    }
  }

  case class Counter(condition: Try[Result] => Boolean, countMetric: CountMetric) {
    def submit(resultTry: Try[Result]) {
      if (condition(resultTry)) countMetric increment ()
    }
  }

  object OkCounter {
    def apply() = Counter(StatusCode(200), new CountMetric("request-status", "200_ok", "200 Ok", "number of pages that responded 200"))
  }

  object RedirectCounter {
    def apply() = Counter(StatusCode(301, 302), new CountMetric("request-status", "30x_redirect", "30x Redirect", "number of pages that responded with a redirect"))
  }

  object NotFoundCounter {
    def apply() = Counter(StatusCode(404), new CountMetric("request-status", "404_not_found", "404 Not found", "number of pages that responded 404"))
  }

  object ErrorCounter {
    def apply() = Counter(t => { t.isFailure || StatusCode(500 to 509)(t) }, new CountMetric("request-status", "50x_error", "50x Error", "number of pages that responded 50x"))
  }

  object OtherCounter {
    def apply(knownResultTypeCounters: Seq[Counter]) = {
      def unknown(result: Try[Result]) = !knownResultTypeCounters.exists(_.condition(result))

      Counter(unknown, new CountMetric("request-status", "other", "Other", "number of pages that responded with an unexpected status code"))
    }
  }

  object StatusCode {

    def apply(codes: Traversable[Int]): Try[Result] => Boolean = apply(codes.toSet: Int => Boolean)

    def apply(codes: Int*): Try[Result] => Boolean = apply(Set(codes: _*): Int => Boolean)

    def apply(condition: Int => Boolean)(resultTry: Try[Result]) =
      resultTry.map(plainResult => condition(plainResult.header.status)).getOrElse(false)
  }

}
