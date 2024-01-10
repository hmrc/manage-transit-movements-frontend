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

import cats.data.NonEmptyList
import config.Constants.NotificationType.IntentionToControl
import config.PaginationAppConfig
import generated._
import play.api.libs.json._

import java.time.{Clock, LocalDateTime, ZoneId}
import java.util.Currency
import scala.annotation.nowarn
import scala.util.{Success, Try}

package object models {

  lazy val booleanReads: Reads[Boolean] = Reads {
    case JsString("0") => JsSuccess(false)
    case JsString("1") => JsSuccess(true)
    case x             => JsError(s"$x could not be read as a Boolean")
  }

  implicit class RichJsObject(jsObject: JsObject) {

    def setObject(path: JsPath, value: JsValue): JsResult[JsObject] =
      jsObject.set(path, value).flatMap(_.validate[JsObject])

    def removeObject(path: JsPath): JsResult[JsObject] =
      jsObject.remove(path).flatMap(_.validate[JsObject])
  }

  implicit class RichJsValue(jsValue: JsValue) {

    def set(path: JsPath, value: JsValue): JsResult[JsValue] =
      (path.path, jsValue) match {

        case (Nil, _) =>
          JsError("path cannot be empty")

        case ((_: RecursiveSearch) :: _, _) =>
          JsError("recursive search not supported")

        case ((n: IdxPathNode) :: Nil, _) =>
          setIndexNode(n, jsValue, value)

        case ((n: KeyPathNode) :: Nil, _) =>
          setKeyNode(n, jsValue, value)

        case (first :: second :: rest, oldValue) =>
          Reads
            .optionNoError(Reads.at[JsValue](JsPath(first :: Nil)))
            .reads(oldValue)
            .flatMap {
              opt =>
                opt
                  .map(JsSuccess(_))
                  .getOrElse {
                    second match {
                      case _: KeyPathNode =>
                        JsSuccess(Json.obj())
                      case _: IdxPathNode =>
                        JsSuccess(Json.arr())
                      case _: RecursiveSearch =>
                        JsError("recursive search is not supported")
                    }
                  }
                  .flatMap {
                    _.set(JsPath(second :: rest), value).flatMap {
                      newValue =>
                        oldValue.set(JsPath(first :: Nil), newValue)
                    }
                  }
            }
      }

    private def setIndexNode(node: IdxPathNode, oldValue: JsValue, newValue: JsValue): JsResult[JsValue] = {

      val index: Int = node.idx

      oldValue match {
        case oldValue: JsArray if index >= 0 && index <= oldValue.value.length =>
          if (index == oldValue.value.length) {
            JsSuccess(oldValue.append(newValue))
          } else {
            JsSuccess(JsArray(oldValue.value.updated(index, newValue)))
          }
        case oldValue: JsArray =>
          JsError(s"array index out of bounds: $index, $oldValue")
        case _ =>
          JsError(s"cannot set an index on $oldValue")
      }
    }

    private def removeIndexNode(node: IdxPathNode, valueToRemoveFrom: JsArray): JsResult[JsValue] = {
      val index: Int = node.idx

      valueToRemoveFrom match {
        case valueToRemoveFrom: JsArray if index >= 0 && index < valueToRemoveFrom.value.length =>
          val updatedJsArray = valueToRemoveFrom.value.slice(0, index) ++ valueToRemoveFrom.value.slice(index + 1, valueToRemoveFrom.value.size)
          JsSuccess(JsArray(updatedJsArray))
        case valueToRemoveFrom: JsArray => JsError(s"array index out of bounds: $index, $valueToRemoveFrom")
        case _                          => JsError(s"cannot set an index on $valueToRemoveFrom")
      }
    }

    private def setKeyNode(node: KeyPathNode, oldValue: JsValue, newValue: JsValue): JsResult[JsValue] = {

      val key = node.key

      oldValue match {
        case oldValue: JsObject =>
          JsSuccess(oldValue + (key -> newValue))
        case _ =>
          JsError(s"cannot set a key on $oldValue")
      }
    }

    @nowarn("msg=Exhaustivity analysis reached max recursion depth, not all missing cases are reported.")
    @nowarn("msg=match may not be exhaustive")
    // scalastyle:off cyclomatic.complexity
    def remove(path: JsPath): JsResult[JsValue] =
      (path.path, jsValue) match {
        case (Nil, _)                                                                  => JsError("path cannot be empty")
        case ((n: KeyPathNode) :: Nil, value: JsObject) if value.keys.contains(n.key)  => JsSuccess(value - n.key)
        case ((n: KeyPathNode) :: Nil, value: JsObject) if !value.keys.contains(n.key) => JsSuccess(value)
        case ((n: IdxPathNode) :: Nil, value: JsArray)                                 => removeIndexNode(n, value)
        case ((_: KeyPathNode) :: Nil, _)                                              => JsError(s"cannot remove a key on $jsValue")
        case (first :: second :: rest, oldValue) =>
          Reads
            .optionNoError(Reads.at[JsValue](JsPath(first :: Nil)))
            .reads(oldValue)
            .flatMap {
              opt: Option[JsValue] =>
                opt
                  .map(JsSuccess(_))
                  .getOrElse {
                    second match {
                      case _: KeyPathNode =>
                        JsSuccess(Json.obj())
                      case _: IdxPathNode =>
                        JsSuccess(Json.arr())
                      case _: RecursiveSearch =>
                        JsError("recursive search is not supported")
                    }
                  }
                  .flatMap {
                    _.remove(JsPath(second :: rest)).flatMap {
                      newValue =>
                        oldValue.set(JsPath(first :: Nil), newValue)
                    }
                  }
            }
      }
    // scalastyle:on cyclomatic.complexity
  }

  implicit class RichLocalDateTime(localDateTime: LocalDateTime) {

    /**
      * Converts a UTC time to the time at the system default time zone
      * @param clock implicitly bound as `systemDefaultZone()` in Module
      * @return the time at the time zone as set by `clock`
      */
    def toSystemDefaultTime(implicit clock: Clock): LocalDateTime = {
      val utcTime = localDateTime.atZone(ZoneId.of("UTC"))
      utcTime.withZoneSameInstant(clock.getZone).toLocalDateTime
    }
  }

  implicit def nonEmptyListReads[A: Reads]: Reads[NonEmptyList[A]] =
    Reads
      .of[List[A]]
      .collect(
        JsonValidationError("expected a NonEmptyList but the list was empty")
      ) {
        case head :: tail => NonEmptyList(head, tail)
      }

  implicit class RichCC060Type(value: CC060CType) {

    def informationRequested: Boolean =
      value.RequestedDocument.nonEmpty ||
        value.TransitOperation.notificationType == IntentionToControl
  }

  // TODO - refactor to use Format
  implicit class RichRecoveryNotificationType(value: RecoveryNotificationType) {

    def formattedCurrency: String = value match {
      case RecoveryNotificationType(_, _, amountClaimed, currency) =>
        Try(Currency.getInstance(currency).getSymbol) match {
          case Success(currency) => s"$currency$amountClaimed"
          case _                 => s"$amountClaimed $currency"
        }
    }
  }

  implicit class RichCC056CType(value: CC056CType) {

    def pagedFunctionalErrors(page: Int)(implicit paginationAppConfig: PaginationAppConfig): Seq[FunctionalErrorType04] = {
      val start = (page - 1) * paginationAppConfig.departuresNumberOfErrorsPerPage
      value.FunctionalError
        .sortBy(_.errorCode.toString)
        .slice(start, start + paginationAppConfig.departuresNumberOfErrorsPerPage)
    }
  }

  implicit class RichCC057CType(value: CC057CType) {

    def pagedFunctionalErrors(page: Int)(implicit paginationAppConfig: PaginationAppConfig): Seq[FunctionalErrorType04] = {
      val start = (page - 1) * paginationAppConfig.arrivalsNumberOfErrorsPerPage
      value.FunctionalError
        .sortBy(_.errorCode.toString)
        .slice(start, start + paginationAppConfig.arrivalsNumberOfErrorsPerPage)
    }
  }
}
