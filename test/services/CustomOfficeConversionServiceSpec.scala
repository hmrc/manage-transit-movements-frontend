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

import java.time.{LocalDate, LocalTime}

import base.SpecBase
import connectors.ReferenceDataConnector
import generators.ModelGenerators
import models.referenceData.{CustomsOffice, Movement}
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import viewModels.{ViewArrivalMovements, ViewMovement}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.inject.{bind, Binding}
import play.api.libs.json.JsSuccess

import scala.concurrent.Future

class CustomOfficeConversionServiceSpec extends SpecBase with ModelGenerators with ScalaCheckPropertyChecks {

  private val mockReferenceDataConnector = mock[ReferenceDataConnector]

  private val appWithMockReferenceDataConnector = applicationBindingOverride(bind[ReferenceDataConnector].toInstance(mockReferenceDataConnector))

  "convertToViewMovements" - {
    "when the customs office data is returned from the reference data" in {
      val movementGen = arbitrary[Movement].map(_.copy(presentationOfficeId = "officeId"))

      val application = appWithMockReferenceDataConnector(applicationBuilder()).build()
      val service     = application.injector.instanceOf[CustomOfficeConversionService]

      val mockReferenceDataResponse =
        CustomsOffice(
          "officeId",
          "office name",
          Seq("role1", "role2"),
          Some("testPhoneNumber")
        )
      when(mockReferenceDataConnector.getCustomsOffice(any())(any()))
        .thenReturn(Future.successful(Some(JsSuccess(mockReferenceDataResponse))))

      forAll(movementGen) {
        case movement @ Movement(date, time, movementReferenceNumber, traderName, presentationOfficeId, procedure) =>
          val expectedResult = ViewMovement(
            date,
            time,
            movementReferenceNumber,
            traderName,
            presentationOfficeId,
            Some(mockReferenceDataResponse.name),
            procedure
          )

          val result = service.convertToViewMovements(movement).futureValue

          result mustEqual expectedResult
      }

      app.stop()
    }

    "when the customs office data is not available from the reference data" in {
      val application = appWithMockReferenceDataConnector(applicationBuilder()).build()
      val service     = application.injector.instanceOf[CustomOfficeConversionService]

      when(mockReferenceDataConnector.getCustomsOffice(any())(any()))
        .thenReturn(Future.successful(None))

      forAll(arbitrary[Movement]) {
        case movement @ Movement(date, time, movementReferenceNumber, traderName, presentationOfficeId, procedure) =>
          val expectedResult = ViewMovement(
            date,
            time,
            movementReferenceNumber,
            traderName,
            presentationOfficeId,
            None,
            procedure
          )

          val result = service.convertToViewMovements(movement).futureValue

          result mustEqual expectedResult
      }

      app.stop()
    }

  }
}
