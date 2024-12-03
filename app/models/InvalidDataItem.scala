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

package models

import play.api.libs.json.*

import scala.annotation.tailrec

case class InvalidDataItem(value: String)

object InvalidDataItem {

  // scalastyle:off cyclomatic.complexity
  // scalastyle:off method.length
  def apply(errorPointer: String): InvalidDataItem = {

    /** @param path
      *   the errorPointer from the functional error
      * @param acc
      *   the accumulated string that forms the eventual result
      * @return
      *   the errorPointer broken up into a value that is easier to read by:
      *   - Splitting the errorPointer at each forward slash
      *   - Ignoring the message identifier e.g. CC015C
      *   - Ignoring empty sub-paths
      *   - Ignoring the 'Consignment' sub-path as this is somewhat implied
      */
    @tailrec
    def rec(path: List[String], acc: String = ""): String = path match {
      case Nil                                        => acc
      case ("" | "Consignment") :: tail               => rec(tail, acc)
      case head :: tail if head.matches("""CC\d*C""") => rec(tail, acc)
      // case head :: tail if head.matches("""HouseConsignment\[\d*]""") => rec(tail, acc)
      case head :: tail =>
        val indexedPattern = "(.*)\\[(\\d*)]".r
        val path = head match {
          case indexedPattern(group, index) => s"$group $index:"
          case _                            => if (tail.isEmpty) head else s"$head:"
        }
        rec(tail, combine(acc, separate(path)))
    }

    def combine(str1: String, str2: String): String =
      if (str1.isEmpty) str2 else s"$str1 $str2"

    /** @param str
      *   the string to separate
      * @return
      *   the string broken up into a value that is easier to read by:
      *   - Capitalising the first letter
      *   - Retaining consecutive uppercase characters (i.e. acronyms like UCR)
      *   - Replacing individual uppercase characters with a space and the character in lowercase (i.e. a new word)
      */
    def separate(str: String): String = {
      @tailrec
      def rec(chars: List[Char], acc: String = ""): String = chars match {
        case Nil                         => acc
        case head :: tail if acc.isEmpty => rec(tail, head.toUpper.toString)
        case head :: next :: tail if head.isUpper && next.isUpper =>
          val f: Char => Boolean = _.isUpper
          rec(tail.dropWhile(f), acc + " " + head + next + tail.takeWhile(f).mkString)
        case head :: tail if head.isUpper => rec(tail, acc + " " + head.toLower)
        case head :: tail                 => rec(tail, acc + head)
      }

      rec(str.toList)
    }

    new InvalidDataItem(rec(errorPointer.split('/').toList))
  }

  implicit val reads: Reads[InvalidDataItem] =
    __.read[JsString].map(_.value).map(InvalidDataItem.apply)
}
