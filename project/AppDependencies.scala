import play.sbt.PlayImport.caffeine
import sbt.*

object AppDependencies {

  private val bootstrapVersion = "9.13.0"
  private val pekkoVersion = "1.0.3"

  val compile: Seq[ModuleID] = Seq(
    play.sbt.PlayImport.ws,
    "uk.gov.hmrc"          %% "play-conditional-form-mapping-play-30"   % "3.3.0",
    "uk.gov.hmrc"          %% "bootstrap-frontend-play-30"              % bootstrapVersion,
    "uk.gov.hmrc"          %% "play-frontend-hmrc-play-30"              % "12.4.0",
    "org.webjars.npm"       % "ministryofjustice__frontend"             % "5.1.3",
    "javax.xml.bind"        % "jaxb-api"                                % "2.3.1",
    "org.typelevel"        %% "cats-core"                               % "2.13.0",
    caffeine
  )

  val test: Seq[ModuleID] = Seq(
    "org.scalatest"              %% "scalatest"                % "3.2.19",
    "uk.gov.hmrc"                %% "bootstrap-test-play-30"   % bootstrapVersion,
    "org.mockito"                 % "mockito-core"             % "5.16.1",
    "org.scalatestplus"          %% "mockito-5-12"             % "3.2.19.0",
    "org.scalacheck"             %% "scalacheck"               % "1.18.1",
    "org.scalatestplus"          %% "scalacheck-1-18"          % "3.2.19.0",
    "org.jsoup"                   % "jsoup"                    % "1.19.1",
    "io.github.wolfendale"       %% "scalacheck-gen-regexp"    % "1.1.0",
    "org.apache.pekko"           %% "pekko-testkit"            % pekkoVersion,
    "org.apache.pekko"           %% "pekko-stream-testkit"     % pekkoVersion
  ).map(_ % "test")

  def apply(): Seq[ModuleID] = compile ++ test
}
