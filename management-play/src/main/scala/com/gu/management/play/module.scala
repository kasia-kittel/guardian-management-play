package com.gu.management.play

import com.gu.management.ManagementPage
import com.gu.management.internal._
import play.api.inject.{ApplicationLifecycle, Binding, Module}
import play.api._
import javax.inject._

import com.google.inject.AbstractModule

import scala.concurrent.Future

trait Management {
  val applicationName: String
  val pages: List[ManagementPage]
}

class InternalManagementModule extends AbstractModule {
  def configure(): Unit =
    bind(classOf[InternalManagementServer]).to(classOf[InternalManagementServerImpl]).asEagerSingleton()
}

trait InternalManagementServer {
  def startServer(applicationName: String, pages: List[ManagementPage])
}

@Singleton
class InternalManagementServerImpl @Inject() (lifecycle: ApplicationLifecycle, management: Management)
    extends InternalManagementServer {

  implicit val log = Logger(getClass)

  def startServer(applicationNameParam: String, pagesParam: List[ManagementPage]): Unit = {
    log.info(s"Starting internal management server for $applicationNameParam")
    ManagementServer.start(new ManagementHandler {
      val applicationName = applicationNameParam
      val pages = pagesParam
    })
  }

  lifecycle.addStopHook { () =>
    log.info(s"Shutting down management server")
    ManagementServer.shutdown()
    Future.successful(())
  }

  startServer(management.applicationName, management.pages)
}

object InternalManagementServer {
  def start(app: Application, management: Management): Unit = {
    val server = app.injector.instanceOf[InternalManagementServer]
    server.startServer(management.applicationName, management.pages)
  }
}