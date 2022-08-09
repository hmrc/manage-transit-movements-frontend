/*
 * Copyright 2022 HM Revenue & Customs
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

package connectors.testOnly

import config.FrontendAppConfig
import play.api.mvc.Headers
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.xml.NodeSeq

class TestOnlyRouterConnector @Inject() (val http: HttpClient, config: FrontendAppConfig)(implicit ec: ExecutionContext) {

  def submitInboundMessage(
    requestData: NodeSeq,
    headers: Headers
  )(implicit headerCarrier: HeaderCarrier): Future[HttpResponse] = {
    val routerUrl = s"${config.routerUrl}/messages"

    val header = headers.headers.filter(
      x =>
        x._1.equalsIgnoreCase("X-Message-Recipient") ||
          x._1.equalsIgnoreCase("X-Message-Type") ||
          x._1.equalsIgnoreCase("Content-Type")
    )

    http.POSTString[HttpResponse](routerUrl, requestData.toString, header)
  }
}
