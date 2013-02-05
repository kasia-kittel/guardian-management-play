import com.typesafe.sbtscalariform.ScalariformPlugin
import sbt._
import sbt.Keys._
import sbt.PlayProject._

object ManagementBuild extends Build {

  implicit def project2noPublish(project: Project) = new {
    lazy val noPublish : sbt.Project = project.settings(publish := false)
  }

  lazy val root = Project("management-root", file(".")).aggregate(
    managementPlay,
    examplePlay
  ).noPublish

  lazy val management = managementProject("management")

  lazy val managementPlay = managementProject("management-play")

  lazy val examplePlay = play.Project(
    name = "example",
    applicationVersion = "1.0",
    dependencies = Nil,
    path = file("example")).
    dependsOn(managementPlay).
    noPublish

  def managementProject(name: String) = Project(name, file(name)).settings(ScalariformPlugin.scalariformSettings :_*)
}
