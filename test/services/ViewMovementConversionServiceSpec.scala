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

package services

import base.SpecBase
import connectors.ReferenceDataConnector
import generators.ModelGenerators
import models.Arrival
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import viewModels.ViewMovement

class ViewMovementConversionServiceSpec extends SpecBase with ModelGenerators with ScalaCheckPropertyChecks {

  "convertToViewMovements" - {
    "when the customs office data is returned from the reference data" in {

      val application = applicationBuilder().build()
      val service     = application.injector.instanceOf[ViewMovementConversionService]

      forAll(arbitrary[Arrival]) {
        arrival =>
          val expectedResult = ViewMovement(
            arrival.updated.date,
            arrival.updated.time,
            arrival.movementReferenceNumber,
            arrival.state
          )

          val result = service.convertToViewArrival(arrival)

          result mustEqual expectedResult
      }

      app.stop()
    }

    "when the customs office data is not available from the reference data" in {
      val application = applicationBuilder().build()
      val service     = application.injector.instanceOf[ViewMovementConversionService]

      forAll(arbitrary[Arrival]) {
        arrival =>
          val expectedResult = ViewMovement(
            arrival.updated.date,
            arrival.updated.time,
            arrival.movementReferenceNumber,
            arrival.state
          )

          val result = service.convertToViewArrival(arrival)

          result mustEqual expectedResult
      }

      app.stop()
    }

  }
}
