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
import org.scalacheck.Arbitrary
import org.scalacheck.Arbitrary.arbitrary
import viewModels.ViewArrivalMovement

trait ModelGenerators {

  implicit val arbitraryMovement: Arbitrary[Movement] = {
    Arbitrary {
      for {
        mrn         <- arbitrary[String]
        traderName  <- arbitrary[String]
        office      <- arbitrary[String]
        procedure   <- arbitrary[String]
      } yield Movement(mrn, traderName, office, procedure)
    }
  }

  implicit val arbitraryViewArrivalModel: Arbitrary[ViewArrivalMovement] = {
    Arbitrary {
      for {
        date            <- arbitrary[String]
        time            <- arbitrary[String]
        movement        <- arbitrary[Movement]
      } yield ViewArrivalMovement(date, time, movement)
    }
  }

}
