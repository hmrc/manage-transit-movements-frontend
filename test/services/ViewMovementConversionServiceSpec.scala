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
import models.referenceData.{CustomsOffice, Movement}
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import viewModels.ViewMovement

import scala.concurrent.Future

class ViewMovementConversionServiceSpec extends SpecBase with ModelGenerators with ScalaCheckPropertyChecks {

  private val mockReferenceDataConnector = mock[ReferenceDataConnector]

  private val appWithMockReferenceDataConnector = bindingOverride(mockReferenceDataConnector)

  "convertToViewMovements" - {
    "when the customs office data is returned from the reference data" in {

      val application = appWithMockReferenceDataConnector(applicationBuilder()).build()
      val service     = application.injector.instanceOf[ViewMovementConversionService]

      val mockReferenceDataResponse =
        CustomsOffice(
          "officeId",
          "office name",
          Seq("role1", "role2"),
          Some("testPhoneNumber")
        )
      when(mockReferenceDataConnector.getCustomsOffice(any())(any()))
        .thenReturn(Future.successful(Some(mockReferenceDataResponse)))

      forAll(arbitrary[Movement]) {
        movement =>
          val expectedResult = ViewMovement(
            movement.date,
            movement.time,
            movement.movementReferenceNumber
          )

          val result = service.convertToViewMovements(movement)

          result mustEqual expectedResult
      }

      app.stop()
    }

    "when the customs office data is not available from the reference data" in {
      val application = appWithMockReferenceDataConnector(applicationBuilder()).build()
      val service     = application.injector.instanceOf[ViewMovementConversionService]

      when(mockReferenceDataConnector.getCustomsOffice(any())(any()))
        .thenReturn(Future.successful(None))

      forAll(arbitrary[Movement]) {
        movement =>
          val expectedResult = ViewMovement(
            movement.date,
            movement.time,
            movement.movementReferenceNumber
          )

          val result = service.convertToViewMovements(movement)

          result mustEqual expectedResult
      }

      app.stop()
    }

  }
}
