package conf

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, Materializer}
import com.google.inject.AbstractModule
import com.gu.management._
import com.gu.management.logback._
import com.gu.management.play.{Management, RequestMetrics}

// example of creating your own new page type
class DummyPage extends ManagementPage {
  val path = "/management/dummy"
  def get(request: HttpRequest) = PlainTextResponse("Hello dummy!")
}

// switches
object Switches {
  val omniture = new DefaultSwitch("omniture", "enables omniture java script")
  val takeItDown = new DefaultSwitch("take-it-down", "enable this switch to take the site down", initiallyOn = false)

  val all = List(omniture, takeItDown, Healthcheck.switch)
}


object AppMetrics {
  implicit val system: ActorSystem = ActorSystem()
  implicit val mat: Materializer =  ActorMaterializer()

  val PlayExampleRequestMetrics = RequestMetrics().Standard
}


// properties
object Properties {
  // If I were using com.gu.configuration I'd comment out the following line
  // val all = new ConfigurationFactory getConfiguration ("music", "conf/arts_music").toString
  val all = "key1=value1\nkey2=value2"
}

object ExampleManagement extends Management {
  val applicationName: String = "Example Play App"

  lazy val pages = List(
    new DummyPage(),
    new ManifestPage(),
    new Switchboard(applicationName, Switches.all),
    StatusPage(applicationName, ExceptionCountMetric :: ServerErrorCounter :: ClientErrorCounter :: AppMetrics.PlayExampleRequestMetrics.asMetrics),
    new HealthcheckManagementPage(),
    new PropertiesPage(Properties.all),
    new LogbackLevelPage(applicationName)
  )
}


class ManagementModule extends AbstractModule {
  override def configure(): Unit = {
    bind(classOf[Management])
      .toInstance(ExampleManagement)
  }
}
