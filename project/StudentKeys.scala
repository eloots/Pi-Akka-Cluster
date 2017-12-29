package sbtstudent

import sbt._

object StudentKeys {
  val bookmarkKeyName = "bookmark"
  val mapPrevKeyName = "map-prev"
  val mapNextKeyName = "map-next"
  val bookmark: AttributeKey[File] = AttributeKey[File](bookmarkKeyName)
  val mapPrev: AttributeKey[Map[String, String]] = AttributeKey[Map[String, String]](mapPrevKeyName)
  val mapNext: AttributeKey[Map[String, String]] = AttributeKey[Map[String, String]](mapNextKeyName)
}