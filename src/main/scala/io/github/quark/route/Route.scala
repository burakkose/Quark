package io.github.quark.route

import akka.http.scaladsl.model._
import akka.http.scaladsl.server.PathMatcher.Unmatched
import akka.http.scaladsl.server.PathMatcher1
import akka.http.scaladsl.server.PathMatchers.{PathEnd, Remaining, Slash}
import io.github.quark.action.ServiceAction
import io.github.quark.route.RouteStatus.{
  Matched => QMatched,
  UnMatched => QUnMatched
}
import io.github.quark.stage.PipelineStage.Input

trait Route extends Route.Fn

object Route {
  type Fn = (Input => RouteStatus)

  implicit def instance(service: ServiceAction): Route = {
    new Route {
      def apply(v1: Input): RouteStatus = {
        service match {
          case s: ServiceAction =>
            val baseMatcher = Slash ~ s.id
            val matcher = baseMatcher ~ (Slash | (Slash.? ~ PathEnd)) ~ Remaining
            val isSuccessFn = isSuccess(matcher) _
            v1 match {
              case HttpRequest(_, uri, _, _, _) if isSuccessFn(uri.path) =>
                QMatched(service)
              case _ => QUnMatched
            }
          case _ => QUnMatched
        }
      }
    }
  }

  private def isSuccess[T](pm: PathMatcher1[T])(path: Uri.Path): Boolean = {
    pm(path) match {
      case Unmatched => false
      case _ => true
    }
  }
}
