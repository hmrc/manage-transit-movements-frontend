package utils

import java.time.{LocalDate, LocalDateTime, LocalTime, OffsetDateTime}
import java.time.format.DateTimeFormatter

object Format {

  val dateFormatter: DateTimeFormatter               = DateTimeFormatter.ofPattern("yyyyMMdd")
  def dateFormatted(date: LocalDate): String         = date.format(dateFormatter)
  def dateFormatted(dateTime: LocalDateTime): String = dateTime.format(dateFormatter)

  val timeFormatter: DateTimeFormatter               = DateTimeFormatter.ofPattern("HHmm")
  def timeFormatted(dateTime: LocalDateTime): String = dateTime.format(timeFormatter)
  def timeFormatted(time: LocalTime): String         = time.format(timeFormatter)

  def dateFormattedForHeader(dateTime: OffsetDateTime): String =
    dateTime.format(DateTimeFormatter.RFC_1123_DATE_TIME)
}

