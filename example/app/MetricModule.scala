
import com.google.inject.{AbstractModule, _}
import com.gu.management.play.MetricsFilter
import conf.AppMetrics

//TODO check it we still need it since we are enabling filters in Filters
class MetricModule extends AbstractModule {

  override def configure(): Unit = {
    bind(new TypeLiteral[Seq[MetricsFilter]] {})
      .toInstance(AppMetrics.PlayExampleRequestMetrics.asFilters)
  }

}