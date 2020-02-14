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

package generators

import models._
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}
import uk.gov.hmrc.viewmodels.SummaryList.Action

trait ModelGenerators {

/*  implicit val arbitraryMovement: Arbitrary[Movement] = {
    Arbitrary {
      for {
        update <- arbitrary[String]
        mrn <- arbitrary[String]
        traderName <- arbitrary[String]
        office <- arbitrary[String]
        procedure <- arbitrary[String]
        status <- arbitrary[String]
        action <- arbitrary[Seq[Action]]
      } yield Movement(
        update, mrn, traderName, office, procedure, status, action
      )
    }
  }*/

}
