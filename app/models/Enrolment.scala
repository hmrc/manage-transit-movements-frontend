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
import play.api.ConfigLoader
import play.twirl.api.TwirlHelperImports.twirlJavaCollectionToScala
import uk.gov.hmrc.auth
import uk.gov.hmrc.auth.core.authorise.Predicate

case class Enrolment(key: String, identifierKey: String, legacy: Boolean) {

  def toPredicate: Predicate =
    auth.core.Enrolment.apply(key)
}

object Enrolment {

  implicit val configLoader: ConfigLoader[Seq[Enrolment]] = (config: Config, path: String) => {
    config.getConfigList(path).toList.map {
      enrolment =>
        val key           = enrolment.getString("key")
        val identifierKey = enrolment.getString("identifierKey")
        val legacy        = enrolment.getBoolean("legacy")
        Enrolment(key, identifierKey, legacy)
    }
  }
}
