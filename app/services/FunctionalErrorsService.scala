/*
 * Copyright 2024 HM Revenue & Customs
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

import generated.FunctionalErrorType04
import models.FunctionalError

import javax.inject.Inject
import scala.concurrent.Future

class FunctionalErrorsService @Inject() (
  referenceDataService: ReferenceDataService
) {

  // send all errors to cache
  //  json writes for FunctionalErrorType04
  //  functional error reads and writes in backend
  //  convert errorPointer to section and invalidDataItem
  // get all errors back
  // call reference data for each one to update the FunctionalError.error
  def toDepartureFunctionalErrors(functionalErrors: Seq[FunctionalErrorType04]): Future[Seq[FunctionalError]] =
    ???
}
