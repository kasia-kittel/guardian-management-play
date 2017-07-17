package com.gu.management.play

import _root_.play.api.inject.bind
import _root_.play.api.inject.guice.GuiceApplicationBuilder
import _root_.play.api.{Configuration, Environment, Mode}
import com.gu.management._
import com.gu.management.internal.ManagementServer
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite

import scala.io.Source

object TestManagement extends Management {
  val applicationName = "test-app"
  val pages = List(
    new ManagementPage {
      def get(req: HttpRequest): Response = new PlainTextResponse("response")
      val path: String = "/management/test"
    }
  )
}

class ModuleSpec extends PlaySpec with GuiceOneAppPerSuite {

  "module" should {
    "be created" in {
      configuredAppBuilder.injector.instanceOf[InternalManagementServer] mustBe an [InternalManagementServer]
    }

    "start management server" in {
      ManagementServer.isRunning mustBe true
    }

    "serve management page" in {
      val port = ManagementServer.port()
      val response = Source.fromURL(s"http://localhost:$port/management/test") mkString ""
      response mustBe "response"
    }

  }

  def configuredAppBuilder = {
    val env = Environment.simple(mode = Mode.Test)
    val config = Configuration.load(env)
    val modules = config.get[Seq[String]]("play.modules.enabled")

    new GuiceApplicationBuilder()
      .configure("play.modules.enabled" -> (modules :+
            "com.gu.management.play.InternalManagementModule"))
      .bindings(bind(classOf[Management]).toInstance(TestManagement))
      .build()
  }

}
