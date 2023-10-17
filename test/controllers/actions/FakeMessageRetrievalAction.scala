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

package controllers.actions

import models.LocalReferenceNumber
import models.departureP5._
import models.requests.{IdentifierRequest, MessageRetrievalRequestProvider}
import org.scalacheck.Arbitrary
import org.scalacheck.Arbitrary.arbitrary
import services.DepartureP5MessageService
import uk.gov.hmrc.http.HttpReads

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class FakeMessageRetrievalAction[B](
  departureId: String,
  messageId: String,
  departureP5MessageService: DepartureP5MessageService,
  referenceNumbers: DepartureReferenceNumbers = DepartureReferenceNumbers(LocalReferenceNumber("AB123"), None),
  data: Option[B] = None
)(implicit reads: HttpReads[B], arb: Arbitrary[B])
    extends MessageRetrievalAction[B](departureId, messageId, departureP5MessageService) {

  val useGenData: B = data.getOrElse(
    arbitrary[B].sample.get
  )

  override protected def transform[A](request: IdentifierRequest[A]): Future[MessageRetrievalRequestProvider[B]#MessageRetrievalRequest[A]] =
    Future.successful(
      new MessageRetrievalRequestProvider[B].MessageRetrievalRequest(
        request,
        request.eoriNumber,
        useGenData,
        referenceNumbers
      )
    )

}
