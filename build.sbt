import com.typesafe.sbt.web.Import.WebKeys.webModules
import play.sbt.routes.RoutesKeys
import scoverage.ScoverageKeys
import uk.gov.hmrc.DefaultBuildSettings
import uk.gov.hmrc.versioning.SbtGitVersioning.autoImport.majorVersion

import scala.sys.process.*

lazy val appName: String = "manage-transit-movements-frontend"

ThisBuild / majorVersion      := 0
ThisBuild / scalaVersion      := "3.5.0"
ThisBuild / scalafmtOnCompile := true

lazy val microservice = (project in file("."))
  .enablePlugins(PlayScala, SbtDistributablesPlugin, ScalaxbPlugin)
  .disablePlugins(JUnitXmlReportPlugin)
  .configs(A11yTest)
  .settings(inConfig(A11yTest)(org.scalafmt.sbt.ScalafmtPlugin.scalafmtConfigSettings) *)
  .settings(headerSettings(A11yTest) *)
  .settings(automateHeaderSettings(A11yTest))
  .settings(
    Compile / scalaxb / scalaxbXsdSource       := new File("./conf/xsd"),
    Compile / scalaxb / scalaxbDispatchVersion := "1.1.3",
    Compile / scalaxb / scalaxbPackageName     := "generated"
  )
  .settings(
    name := appName,
    RoutesKeys.routesImport += "models._",
    TwirlKeys.templateImports ++= Seq(
      "play.twirl.api.HtmlFormat",
      "play.twirl.api.HtmlFormat._",
      "uk.gov.hmrc.govukfrontend.views.html.components._",
      "uk.gov.hmrc.hmrcfrontend.views.html.components._",
      "uk.gov.hmrc.hmrcfrontend.views.html.helpers._",
      "controllers.routes._",
      "views.html.helper.CSPNonce",
      "templates._"
    ),
    PlayKeys.playDefaultPort := 9485,
    ScoverageKeys.coverageExcludedFiles := "<empty>;Reverse.*;.*handlers.*;.*repositories.*;" +
      ".*BuildInfo.*;.*javascript.*;.*Routes.*;.*GuiceInjector;" +
      ".*ControllerConfiguration;.*TestOnly.*",
    ScoverageKeys.coverageExcludedPackages := Seq(
      ".*scalaxb.*",
      ".*generated.*",
      "views\\.html\\.components.*",
      "views\\.html\\.resources.*",
      "views\\.html\\.templates.*"
    ).mkString(";"),
    ScoverageKeys.coverageMinimumStmtTotal := 90,
    ScoverageKeys.coverageFailOnMinimum    := true,
    ScoverageKeys.coverageHighlighting     := true,
    scalacOptions ++= Seq(
      "-feature",
      "-Wconf:src=routes/.*:s",
      "-Wconf:src=src_managed/.*:s",
      "-Wconf:src=html/.*&msg=unused import:s",
      "-Wconf:msg=Flag .* set repeatedly:s"
    ),
    libraryDependencies ++= AppDependencies(),
    retrieveManaged           := true,
    Assets / pipelineStages   := Seq(digest),
    ThisBuild / useSuperShell := false
  )

lazy val createSassSymlink = taskKey[Unit]("Create symlink from node_modules to lib for Sass overrides")

createSassSymlink := {
  val log        = streams.value.log
  val targetLink = baseDirectory.value / "target" / "web" / "web-modules" / "main" / "webjars" / "node_modules"
  val sourceDir  = baseDirectory.value / "target" / "web" / "web-modules" / "main" / "webjars" / "lib" / "govuk-frontend"

  if (!targetLink.exists()) {
    IO.createDirectory(targetLink)
    log.info(s"Creating symlink: $targetLink → $sourceDir")
    val result = Process(Seq("ln", "-s", sourceDir.getAbsolutePath, targetLink.getAbsolutePath)).!
    if (result != 0) sys.error("Failed to create symlink.")
  }
}

createSassSymlink := createSassSymlink.dependsOn(Assets / webModules).value
Compile / compile := (Compile / compile).dependsOn(createSassSymlink).value

lazy val it = project
  .enablePlugins(PlayScala)
  .dependsOn(microservice % "test->test") // the "test->test" allows reusing test code and test dependencies
  .settings(
    libraryDependencies ++= AppDependencies.test,
    DefaultBuildSettings.itSettings()
  )
