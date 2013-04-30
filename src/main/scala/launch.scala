package conscript

import dispatch._
import Defaults._
import java.io.File
import util.control.Exception._
import language.implicitConversions

trait Launch extends Credentials {
  val sbtversion = "0.12.2"
  val sbtlaunchalias = "sbt-launch.jar"

  def launchJar(display: Display): Either[String, String] =
      configdir("sbt-launch-%s.jar" format sbtversion) match {
    case jar if jar.exists => Right("Launcher already up to date, fetching next...")
    case jar =>
      try {
        display.info("Fetching launcher...")
        val launchalias = configdir(sbtlaunchalias)
        if (!launchalias.getParentFile.exists) mkdir(launchalias)
        else ()

        val req = url("http://typesafe.artifactoryonline.com/typesafe/ivy-releases/org.scala-sbt/sbt-launch/%s/sbt-launch.jar" format sbtversion)

        enrichFuture(Http(req > as.File(jar)))()
        windows map { _ =>
          if (launchalias.exists) launchalias.delete
          else ()
          // should copy the one we already downloaded, but I don't
          // have a windows box to test any changes
          Http(req > as.File(launchalias))
        } getOrElse {
          val rt = Runtime.getRuntime
          rt.exec("ln -sf %s %s" format (jar, launchalias)).waitFor
        }
        Right("Fetching Conscript...")
      } catch {
        case e: Exception => 
          Left("Error downloading sbt-launch-%s: %s".format(
            sbtversion, e.toString
          ))
      }
  }

  implicit class StrPathBuilder(a: String) {
    def / (b: String) = a + File.separatorChar + b
  }
  def forceslash(a: String) =
    windows map { _ =>
      "/" + (a replaceAll ("""\\""", """/"""))
    } getOrElse {a}
  def configdir(path: String) = homedir(".conscript" / path)
  def homedir(path: String) = new File(System.getProperty("user.home"), path)
  def mkdir(file: File) =
    catching(classOf[SecurityException]).either {
      new File(file.getParent).mkdirs()
    }.left.toOption.map { e => "Unable to create path " + file }
}
