package io.github.quark.route

import io.github.quark.operation.ServiceAction

sealed trait RouteStatus

object RouteStatus {
  case class Matched(service: ServiceAction) extends RouteStatus
  case object UnMatched extends RouteStatus
}
