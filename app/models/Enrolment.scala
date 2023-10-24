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

case class Enrolment(key: String, identifierKey: String)

object Enrolment {

  def apply(key: String, identifierKey: String): Enrolment = new Enrolment(key, identifierKey)

  implicit val configLoader: ConfigLoader[Enrolment] = (config: Config, path: String) => {
    val enrolment     = Configuration(config).get[Configuration](path)
    val key           = enrolment.get[String]("key")
    val identifierKey = enrolment.get[String]("identifierKey")
    Enrolment(key, identifierKey)
  }
}
