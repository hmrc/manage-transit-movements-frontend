import sbt._

object AppDependencies {
  import play.core.PlayVersion

  val compile: Seq[ModuleID] = Seq(
    play.sbt.PlayImport.ws,
    "org.reactivemongo" %% "play2-reactivemongo"           % "0.20.11-play28",
    "uk.gov.hmrc"       %% "logback-json-logger"           % "5.1.0",
    "uk.gov.hmrc"       %% "play-conditional-form-mapping" % "1.9.0-play-28",
    "uk.gov.hmrc"       %% "bootstrap-frontend-play-28"    % "5.16.0",
    "uk.gov.hmrc"       %% "play-nunjucks"                 % "0.33.0-play-28",
    "uk.gov.hmrc"       %% "play-nunjucks-viewmodel"       % "0.15.0-play-28",
    "org.webjars.npm"   %  "govuk-frontend"                % "3.13.0",
    "org.webjars.npm"   %  "hmrc-frontend"                 % "1.35.2",
    "com.lucidchart"    %% "xtract"                        % "2.2.1"
  )

  val test: Seq[ModuleID] = Seq(
    "org.scalatest"               %% "scalatest"                % "3.2.9",
    "org.scalatestplus"           %% "mockito-3-2"              % "3.1.2.0",
    "org.scalatestplus.play"      %% "scalatestplus-play"       % "5.1.0",
    "org.scalatestplus"           %% "scalatestplus-scalacheck" % "3.1.0.0-RC2",
    "org.pegdown"                 %  "pegdown"                  % "1.6.0",
    "org.jsoup"                   %  "jsoup"                    % "1.14.2",
    "com.typesafe.play"           %% "play-test"                % PlayVersion.current,
    "org.mockito"                 %  "mockito-core"             % "3.12.4",
    "org.scalacheck"              %% "scalacheck"               % "1.15.4",
    "com.github.tomakehurst"      % "wiremock-standalone"       % "2.27.2",
    "com.vladsch.flexmark"        % "flexmark-all"              % "0.36.8",
    "uk.gov.hmrc"                 %% "bootstrap-test-play-28"   % "5.14.0"
  ).map(_ % Test)

  def apply(): Seq[ModuleID] = compile ++ test
}
