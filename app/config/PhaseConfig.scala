/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package config

import com.typesafe.config.Config
import config.PhaseConfig.Values
import play.api.{ConfigLoader, Configuration}

import javax.inject.Inject

trait PhaseConfig {
  val values: Values
}

object PhaseConfig {

  class TransitionConfig @Inject() (configuration: Configuration) extends PhaseConfig {
    override val values: Values = configuration.get[Values]("phase.transitional")
  }

  class PostTransitionConfig @Inject() (configuration: Configuration) extends PhaseConfig {
    override val values: Values = configuration.get[Values]("phase.final")
  }

  case class Values(apiVersion: Double)

  object Values {

    implicit val configLoader: ConfigLoader[Values] = (config: Config, path: String) =>
      config.getConfig(path) match {
        case phase =>
          val apiVersion = phase.getDouble("apiVersion")
          Values(apiVersion)
      }
  }
}
