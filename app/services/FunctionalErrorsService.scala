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

import connectors.DepartureCacheConnector
import generated.*
import models.FunctionalError.*
import models.FunctionalErrors.*
import models.{FunctionalErrorType, GuaranteeReference, InvalidGuaranteeReason}
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class FunctionalErrorsService @Inject() (
  departureCacheConnector: DepartureCacheConnector,
  referenceDataService: ReferenceDataService
) {

  def convertErrorsWithSection(
    functionalErrors: Seq[FunctionalErrorType]
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[FunctionalErrorsWithSection] =
    departureCacheConnector
      .convertErrors(functionalErrors)
      .flatMap {
        errors =>
          Future
            .sequence {
              errors.value.map {
                error =>
                  referenceDataService.getFunctionalError(error.error).map {
                    value => error.copy(error = value.toString)
                  }
              }
            }
            .map(FunctionalErrorsWithSection.apply)
      }

  def convertErrorsWithSectionAndSender(
    functionalErrors: Seq[FunctionalErrorType],
    messageSender: String
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[FunctionalErrorsWithSection] =
    departureCacheConnector
      .convertErrors(functionalErrors)
      .flatMap {
        errors =>
          Future
            .sequence {
              errors.value.map {
                error =>
                  referenceDataService.getFunctionalErrorForSender(error.error, messageSender).map {
                    value => error.copy(error = value.toString)
                  }
              }
            }
            .map(FunctionalErrorsWithSection.apply)
      }

  def convertErrorsWithoutSection(
    functionalErrors: Seq[FunctionalErrorType]
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[FunctionalErrorsWithoutSection] =
    Future
      .sequence {
        functionalErrors.map(FunctionalErrorWithoutSection.apply).map {
          error =>
            referenceDataService.getFunctionalError(error.error).map {
              value => error.copy(error = value.toString)
            }
        }
      }
      .map(FunctionalErrorsWithoutSection.apply)

  def convertErrorsWithoutSectionAndWithSender(
    functionalErrors: Seq[FunctionalErrorType],
    messageSender: String
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[FunctionalErrorsWithoutSection] =
    Future
      .sequence {
        functionalErrors.map(FunctionalErrorWithoutSection.apply).map {
          error =>
            referenceDataService.getFunctionalErrorForSender(error.error, messageSender).map {
              value => error.copy(error = value.toString)
            }
        }
      }
      .map(FunctionalErrorsWithoutSection.apply)

  def convertGuaranteeReferences(
    guaranteeReferences: Seq[GuaranteeReferenceType07]
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Seq[GuaranteeReference]] =
    Future
      .sequence {
        guaranteeReferences.map {
          guaranteeReference =>
            Future
              .sequence {
                guaranteeReference.InvalidGuaranteeReason.map {
                  invalidGuaranteeReason =>
                    referenceDataService.getInvalidGuaranteeReason(invalidGuaranteeReason.code).map {
                      value => InvalidGuaranteeReason(value.toString, invalidGuaranteeReason.text)
                    }
                }
              }
              .map(GuaranteeReference.apply(guaranteeReference.GRN, _))
        }
      }
}
