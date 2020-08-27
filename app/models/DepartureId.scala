/*
 * Copyright 2020 HM Revenue & Customs
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

import play.api.libs.json.{__, JsNumber, Reads, Writes}
import play.api.mvc.PathBindable

case class DepartureId(index: Int)

object DepartureId {
  implicit def reads: Reads[DepartureId] = __.read[Int] map DepartureId.apply

  implicit def writes: Writes[DepartureId] = Writes(departureId => JsNumber(departureId.index))

  implicit lazy val pathBindable: PathBindable[DepartureId] = new PathBindable[DepartureId] {
    override def bind(key: String, value: String): Either[String, DepartureId] =
      implicitly[PathBindable[Int]].bind(key, value).right.map(DepartureId(_))

    override def unbind(key: String, value: DepartureId): String =
      value.index.toString
  }
}
