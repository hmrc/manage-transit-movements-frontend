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

import config.FrontendAppConfig
import javax.inject.Inject
import models.{ArrivalId, Arrivals}
import play.api.libs.ws.WSClient
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import cats.implicits._

import scala.concurrent.{ExecutionContext, Future}

class ArrivalMovementConnector @Inject()(config: FrontendAppConfig, http: HttpClient, ws: WSClient)(implicit ec: ExecutionContext) {

  def getArrivals()(implicit hc: HeaderCarrier): Future[Arrivals] = {
    val serviceUrl: String = s"${config.destinationUrl}/movements/arrivals"
    http.GET[Arrivals](serviceUrl)
  }

  def getPDF(arrivalId: ArrivalId)(implicit hc: HeaderCarrier): Future[Option[Array[Byte]]] = {
    val serviceUrl: String = s"${config.destinationUrl}/movements/arrivals/${arrivalId.index}/unloading-permission"

    hc.authorization.traverse(
      result =>
        ws.url(serviceUrl)
          .withHttpHeaders(("Authorization", result.value))
          .get
          .filter(_.status == 200)
          .map(_.bodyAsBytes.toArray)
    )
  }

}
