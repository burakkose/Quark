package io.github.quark.action

import io.github.quark.route.RouteStatus.{Matched, UnMatched}
import io.github.quark.route.{Route, RouteStatus}
import io.github.quark.stage.PipelineStage.Input

import scala.annotation.tailrec

sealed trait GatewayAction {
  def service(request: Input): RouteStatus
}

object GatewayAction {

  final case class Gateway(routes: Seq[Route]) extends GatewayAction {
    def service(request: Input): RouteStatus = {
      @tailrec
      def inner(s: List[Route]): RouteStatus = {
        s match {
          case Nil => UnMatched
          case h :: t =>
            h.apply(request) match {
              case m @ Matched(_) => m
              case UnMatched => inner(t)
            }
        }
      }
      inner(routes.toList)
    }
  }

}
