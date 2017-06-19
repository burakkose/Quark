package io.github.quark.route

sealed trait QuarkRouteStatus

object QuarkRouteStatus {
  case object SUCCESS extends QuarkRouteStatus
  case object FAILED extends QuarkRouteStatus
}
