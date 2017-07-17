package controllers

import javax.inject.Inject

import play.api.mvc._
import com.gu.management.Switch.On

import scala.concurrent._
import ExecutionContext.Implicits.global

class ScalaApp @Inject()(cc: ControllerComponents) extends AbstractController(cc) {


  def apply() = Action {
    conf.Switches.takeItDown match {
      case On() => InternalServerError("Temporarily switched off!")
      case _ => Ok("Thank you for invoking this app!")
    }
  }

  def exception() = Action {
    throw new Exception("Expected exception.")
    InternalServerError("Unreachable")
  }

  def long() = Action {
    Thread.sleep(2000)
    Ok("Slept OK")
  }

  def async() = Action.async {
    Future {
      Thread.sleep(2000)
      Ok("Slept OK")
    }
  }
}