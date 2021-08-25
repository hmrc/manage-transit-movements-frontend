import sbt._

object AppDependencies {
  import play.core.PlayVersion

  val compile = Seq(
    play.sbt.PlayImport.ws,
    "org.reactivemongo" %% "play2-reactivemongo"           % "0.18.6-play26",
    "uk.gov.hmrc"       %% "logback-json-logger"           % "5.1.0",
    "uk.gov.hmrc"       %% "play-health"                   % "3.16.0-play-27",
    "uk.gov.hmrc"       %% "play-conditional-form-mapping" % "1.9.0-play-26",
    "uk.gov.hmrc"       %% "bootstrap-frontend-play-27"    % "5.0.0",
    "uk.gov.hmrc"       %% "play-nunjucks"                 % "0.27.0-play-26",
    "uk.gov.hmrc"       %% "play-nunjucks-viewmodel"       % "0.13.0-play-26",
    "org.webjars.npm"    % "govuk-frontend"                % "3.11.0",
    "org.webjars.npm"    % "hmrc-frontend"                 % "1.31.0",
    "com.lucidchart"    %% "xtract"                        % "2.2.1"
  )

  val test = Seq(
    "org.scalatest"         %% "scalatest"                % "3.2.0",
    "org.scalatestplus"     %% "mockito-3-2"              % "3.1.2.0",
    "org.scalatestplus"     %% "scalatestplus-scalacheck" % "3.1.0.0-RC2",
    "org.pegdown"            % "pegdown"                  % "1.6.0",
    "org.jsoup"              % "jsoup"                    % "1.10.3",
    "com.typesafe.play"     %% "play-test"                % PlayVersion.current,
    "org.mockito"            % "mockito-core"             % "3.3.3",
    "org.scalacheck"        %% "scalacheck"               % "1.14.3",
    "com.github.tomakehurst" % "wiremock-standalone"      % "2.25.0",
    "com.vladsch.flexmark"   % "flexmark-all"             % "0.35.10",
    "uk.gov.hmrc"           %% "bootstrap-test-play-27"   % "5.0.0"
  ).map(_ % Test)

  def apply(): Seq[ModuleID] = compile ++ test

  val akkaVersion     = "2.6.7"
  val akkaHttpVersion = "10.1.12"

  val overrides = Seq(
    "com.typesafe.akka" %% "akka-stream"    % akkaVersion,
    "com.typesafe.akka" %% "akka-protobuf"  % akkaVersion,
    "com.typesafe.akka" %% "akka-slf4j"     % akkaVersion,
    "com.typesafe.akka" %% "akka-actor"     % akkaVersion,
    "com.typesafe.akka" %% "akka-http-core" % akkaHttpVersion
  )
}
