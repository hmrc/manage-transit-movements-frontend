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

package connectors

import config.FrontendAppConfig
import models.DraftDepartures
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpReads}
import uk.gov.hmrc.http.HttpReads.Implicits._

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DraftDeparturesConnector @Inject() (config: FrontendAppConfig, http: HttpClient)(implicit ec: ExecutionContext) {

  def getDraftDepartures(implicit hc: HeaderCarrier): Future[DraftDepartures] = {
    val url = s"${config.draftDeparturesUrl}/user-answers"

    http.GET[DraftDepartures](url)(HttpReads[DraftDepartures], hc, ec)
  }

}
