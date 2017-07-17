import com.gu.management.{CountMetric, Metric, TimingMetric}
import conf.AppMetrics
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.libs.ws.WSClient
import play.api.test.Helpers._

import scala.concurrent.Await
import scala.concurrent.duration.Duration


//TODO add test for management server
class RequestMetricsSpec extends PlaySpec with GuiceOneServerPerSuite {

  val wsClient = app.injector.instanceOf[WSClient]

  val metric = getMetric[TimingMetric]("request_duration")

  val serverURL = s"localhost:$port"


  "request metrics" should {

    "correctly count and time requests for performance" in {
      val originalTimingCount = metric.count
      val startTimeTotal = metric.totalTimeInMillis
      val longRequest = s"http://$serverURL/scala-app/long"

      val response = Await.result(wsClient.url(longRequest).get , Duration.Inf)
      response.status mustBe OK

      val counted = metric.count - originalTimingCount
      val measuredDuration = metric.totalTimeInMillis - startTimeTotal

      counted must be (1)
      measuredDuration must be >= 2000L
    }

    "correctly count OK requests" in {
      val metric = getMetric[CountMetric]("200_ok")
      val originalOkCount = metric.count

      val request = s"http://$serverURL/scala-app"
      val response = Await.result(wsClient.url(request).get , Duration.Inf)

      response.status mustBe OK

      val counted = metric.count - originalOkCount

      counted must be (1)
    }
  }

  def getMetric[A <: Metric](metricName: String) = AppMetrics.PlayExampleRequestMetrics.asMetrics.find(
    _.name == metricName
  ).get.asInstanceOf[A]


}