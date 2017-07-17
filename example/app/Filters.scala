import javax.inject.Inject

import com.gu.management.play.MetricsFilter
import play.api.http.{DefaultHttpFilters, EnabledFilters}


//need to enable metric filters
class Filters @Inject() (defaultFilters: EnabledFilters, metrics: Seq[MetricsFilter])
  extends DefaultHttpFilters(defaultFilters.filters ++ metrics: _*)

