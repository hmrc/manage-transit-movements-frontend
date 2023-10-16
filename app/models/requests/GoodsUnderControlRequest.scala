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

package models.requests

import models.LocalReferenceNumber
import models.departureP5.IE060MessageData
import models.referenceData.CustomsOffice
import play.api.mvc.{Request, WrappedRequest}

case class GoodsUnderControlRequest[A](
  request: Request[A],
  eoriNumber: String,
  ie060MessageData: IE060MessageData,
  lrn: LocalReferenceNumber,
  customsOffice: Option[CustomsOffice]
) extends WrappedRequest[A](request)
