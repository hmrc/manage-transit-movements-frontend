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

package connectors

import java.time.LocalDateTime

import config.FrontendAppConfig
import javax.inject.Inject
import models.{Departure, DepartureId, Departures, LocalReferenceNumber}
import play.api.libs.ws.WSClient
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.http.HttpClient

import scala.concurrent.{ExecutionContext, Future}

class DeparturesMovementConnector @Inject()(config: FrontendAppConfig, http: HttpClient, ws: WSClient)(implicit ec: ExecutionContext) {

  def get()(implicit hc: HeaderCarrier): Future[Departures] = {
    val serviceUrl: String = s"${config.departureUrl}/movements/departures"
    http.GET[Departures](serviceUrl)
  }

}

//TODO: Remove this once we're calling stub/backend
class DeparturesMovementConnectorTemp {

  def get()(implicit hc: HeaderCarrier): Future[Departures] = {
    val depSub     = Departure(DepartureId(22), LocalDateTime.now(), LocalReferenceNumber("LRN123456"), "office1", "DepartureSubmitted")
    val depMrn     = Departure(DepartureId(23), LocalDateTime.now(), LocalReferenceNumber("LRN123457"), "office2", "MrnAllocated")
    val depRel     = Departure(DepartureId(24), LocalDateTime.now().minusDays(1), LocalReferenceNumber("LRN123458"), "office3", "ReleasedForTransit")
    val depTra     = Departure(DepartureId(25), LocalDateTime.now().minusDays(1), LocalReferenceNumber("LRN123459"), "office4", "TransitDeclarationRejected")
    val depDep     = Departure(DepartureId(26), LocalDateTime.now().minusDays(1), LocalReferenceNumber("LRN123460"), "office5", "DepartureDeclarationReceived")
    val depQua     = Departure(DepartureId(27), LocalDateTime.now().minusDays(2), LocalReferenceNumber("LRN123461"), "office6", "QuaranteeValidationFail")
    val depSent    = Departure(DepartureId(28), LocalDateTime.now().minusDays(2), LocalReferenceNumber("LRN123462"), "office7", "TransitDeclarationSent")
    val departures = Departures(Seq(depSub, depMrn, depRel, depTra, depDep, depQua, depSent))
    Future.successful(departures)
  }
}
