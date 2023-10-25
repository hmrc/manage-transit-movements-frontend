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

package models

import com.typesafe.config.Config
import play.api.{ConfigLoader, Configuration}

sealed trait Enrolment {
  val key: String
  val identifierKey: String
}

object Enrolment {

  case class NewEnrolment(key: String, identifierKey: String) extends Enrolment

  object NewEnrolment {
    implicit val configLoader: ConfigLoader[NewEnrolment] = loadConfig(NewEnrolment.apply)
  }

  case class LegacyEnrolment(key: String, identifierKey: String) extends Enrolment

  object LegacyEnrolment {
    implicit val configLoader: ConfigLoader[LegacyEnrolment] = loadConfig(LegacyEnrolment.apply)
  }

  private def loadConfig[T <: Enrolment](apply: (String, String) => T): ConfigLoader[T] = (config: Config, path: String) => {
    val enrolment     = Configuration(config).get[Configuration](path)
    val key           = enrolment.get[String]("key")
    val identifierKey = enrolment.get[String]("identifierKey")
    apply(key, identifierKey)
  }
}
