/*
 * Copyright 2022 HM Revenue & Customs
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

import play.api.Configuration

import javax.inject.Inject

sealed trait Phase5Switch {
  val name: String

  val config: Configuration

  lazy val enabled: Boolean =
    config.get[Boolean](s"microservice.services.features.phase5Enabled.$name")

  def getFrontendUrl: String = {
    val url = if (enabled) {
      s"manageTransitMovements${name.capitalize}Frontend"
    } else {
      s"declareTransitMovement${name.capitalize}Frontend"
    }
    config.get[String](s"urls.$url")
  }
}

class Phase5 @Inject() (configuration: Configuration) {

  case object Departures extends Phase5Switch {
    override val name: String          = "departure"
    override val config: Configuration = configuration
  }

  case object Arrivals extends Phase5Switch {
    override val name: String          = "arrival"
    override val config: Configuration = configuration
  }

  case object Unloading extends Phase5Switch {
    override val name: String          = "unloading"
    override val config: Configuration = configuration
  }

  case object Cancellations extends Phase5Switch {
    override val name: String          = "cancellation"
    override val config: Configuration = configuration
  }

}
