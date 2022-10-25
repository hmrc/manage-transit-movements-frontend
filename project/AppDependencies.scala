import play.core.PlayVersion
import sbt._

object AppDependencies {

  private val bootstrapVersion = "7.8.0"
  private val catsVersion = "2.8.0"

  val compile: Seq[ModuleID] = Seq(
    play.sbt.PlayImport.ws,
    "uk.gov.hmrc"          %% "logback-json-logger"           % "5.2.0",
    "uk.gov.hmrc"          %% "play-conditional-form-mapping" % "1.12.0-play-28",
    "uk.gov.hmrc"          %% "bootstrap-frontend-play-28"    % bootstrapVersion,
    "uk.gov.hmrc"          %% "play-frontend-hmrc"            % "3.32.0-play-28",
    "com.lucidchart"       %% "xtract"                        % "2.2.1"
  )

  val test: Seq[ModuleID] = Seq(
    "org.scalatest"              %% "scalatest"                % "3.2.14",
    "uk.gov.hmrc"                %% "bootstrap-test-play-28"   % bootstrapVersion,
    "com.typesafe.play"          %% "play-test"                % PlayVersion.current,
    "org.mockito"                 % "mockito-core"             % "4.8.0",
    "org.scalatestplus"          %% "mockito-4-6"              % "3.2.14.0",
    "org.scalacheck"             %% "scalacheck"               % "1.17.0",
    "org.scalatestplus"          %% "scalacheck-1-17"          % "3.2.14.0",
    "org.pegdown"                 % "pegdown"                  % "1.6.0",
    "org.jsoup"                   % "jsoup"                    % "1.15.3",
    "com.github.tomakehurst"      % "wiremock-standalone"      % "2.27.2",
    "com.vladsch.flexmark"        % "flexmark-all"             % "0.62.2"
  ).map(_ % Test)

  def apply(): Seq[ModuleID] = compile ++ test

  val overrides: Seq[ModuleID] = Seq(
    "org.typelevel" %% "cats-core" % catsVersion,
    "org.typelevel" %% "cats-kernel" % catsVersion
  )
}
