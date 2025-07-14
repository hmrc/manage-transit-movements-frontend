/*
 * Copyright 2025 HM Revenue & Customs
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

import config.Constants.AdditionalDeclarationType.PreLodged
import models.IE015.*

import scala.language.implicitConversions
import scala.xml.Node

// Since we only need the additionalDeclarationType field from the IE015,
// we have created a custom case class to only retrieve the field we need
// rather than using scalaxb and storing the entire message in memory

case class IE015(
  transitOperation: TransitOperation
) {

  val isPreLodged: Boolean = transitOperation.additionalDeclarationType == PreLodged
}

object IE015 {

  implicit def reads(node: Node): IE015 = {
    val transitOperation = TransitOperation.reads((node \ "TransitOperation").head)
    new IE015(transitOperation)
  }

  case class TransitOperation(additionalDeclarationType: String)

  object TransitOperation {

    def reads(node: Node): TransitOperation = {
      val lrn = (node \ "additionalDeclarationType").text
      new TransitOperation(lrn)
    }
  }
}
