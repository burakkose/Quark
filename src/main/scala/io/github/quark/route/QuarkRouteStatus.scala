package io.github.quark.route

sealed trait QuarkRouteStatus

object QuarkRouteStatus {
  case object Success extends QuarkRouteStatus
  case object Failure extends QuarkRouteStatus
}
