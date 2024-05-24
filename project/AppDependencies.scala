import sbt._

object AppDependencies {

  private val bootstrapVersion = "8.6.0"
  private val catsVersion = "2.10.0"
  private val pekkoVersion = "1.0.2"

  val compile: Seq[ModuleID] = Seq(
    play.sbt.PlayImport.ws,
    "uk.gov.hmrc"          %% "play-conditional-form-mapping-play-30"   % "2.0.0",
    "uk.gov.hmrc"          %% "bootstrap-frontend-play-30"              % bootstrapVersion,
    "uk.gov.hmrc"          %% "play-frontend-hmrc-play-30"              % "9.11.0",
    "com.lucidchart"       %% "xtract"                                  % "2.3.0",
    "javax.xml.bind"        % "jaxb-api"                                % "2.3.1"
  )

  val test: Seq[ModuleID] = Seq(
    "org.scalatest"              %% "scalatest"                % "3.2.18",
    "uk.gov.hmrc"                %% "bootstrap-test-play-30"   % bootstrapVersion,
    "org.mockito"                 % "mockito-core"             % "5.11.0",
    "org.scalatestplus"          %% "mockito-4-11"             % "3.2.18.0",
    "org.scalacheck"             %% "scalacheck"               % "1.18.0",
    "org.scalatestplus"          %% "scalacheck-1-17"          % "3.2.18.0",
    "org.jsoup"                   % "jsoup"                    % "1.17.2",
    "io.github.wolfendale"       %% "scalacheck-gen-regexp"    % "1.1.0",
    "org.apache.pekko"           %% "pekko-testkit"            % pekkoVersion,
    "org.apache.pekko"           %% "pekko-stream-testkit"     % pekkoVersion
  ).map(_ % Test)

  def apply(): Seq[ModuleID] = compile ++ test

  val overrides: Seq[ModuleID] = Seq(
    "org.typelevel" %% "cats-core" % catsVersion,
    "org.typelevel" %% "cats-kernel" % catsVersion
  )
}
