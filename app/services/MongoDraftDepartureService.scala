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

package services

import models.{DraftDeparture, EoriNumber, LocalReferenceNumber}

import java.time.LocalDate
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class MongoDraftDepartureService @Inject() ()(implicit ec: ExecutionContext) extends DraftDepartureService {

  val data: List[DraftDeparture] =
    List(DraftDeparture(LocalDate.now(), new LocalReferenceNumber("12345"), "Open"), DraftDeparture(LocalDate.now(), new LocalReferenceNumber("23456"), "Open"))

  override def getAll(eori: EoriNumber): Future[List[DraftDeparture]] = Future(data)
}
