import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

    val appName         = "EvernoteScalaSample"
    val appVersion      = "1.0"

    val appDependencies = Seq(
      "com.evernote" % "evernote-api" % "1.25.1"
    )

    val main = play.Project(appName, appVersion, appDependencies).settings(
    )

}
