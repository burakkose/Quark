package io.github.quark.route

import akka.http.scaladsl.model._
import akka.http.scaladsl.server.PathMatcher.Unmatched
import akka.http.scaladsl.server.PathMatchers.{PathEnd, Remaining, Slash}
import akka.http.scaladsl.server.{ImplicitPathMatcherConstruction, PathMatcher1}
import io.github.quark.route.QuarkRouteStatus.{Failure, Success}

trait RouteBuilder extends ImplicitPathMatcherConstruction {

  def build(routes: Seq[String]): QuarkRoute = {
    val serviceRoutes = routes.map(createRouteWithPath)
    val failedRoute = QuarkRoute.instance {
      case _: HttpRequest => Failure
    }
    val routeList = serviceRoutes.toList :+ failedRoute
    routeList.reduceLeft((r1, r2) => QuarkRoute.instance(r1 orElse r2))
  }

  private def createRouteWithPath(pm: String) = {
    val baseMatcher = Slash ~ pm
    val matcher = baseMatcher ~ (Slash | (Slash.? ~ PathEnd)) ~ Remaining
    val isSuccessFn = isSuccess(matcher) _
    QuarkRoute.instance {
      case HttpRequest(_, uri, _, _, _) if isSuccessFn(uri.path) => Success
    }
  }

  private def isSuccess[T](pm: PathMatcher1[T])(path: Uri.Path): Boolean = {
    pm(path) match {
      case Unmatched => false
      case _ => true
    }
  }

}

object RouteBuilder extends RouteBuilder
