package io.github.quark.builder

import akka.http.scaladsl.model._
import io.github.quark.route.{QuarkRoute, QuarkRouteStatus}

trait RouteBuilder {

  def build(routes: Seq[String]): QuarkRoute = {
    val definedRoutes = routes.map(createRoute)
    val failedRoutes = QuarkRoute.instance {
      case _: HttpRequest =>
        QuarkRouteStatus.FAILED
    }
    val routeList = definedRoutes.toList :+ failedRoutes
    routeList.reduceLeft((r1, r2) => QuarkRoute.instance(r1 orElse r2))
  }

  private def createRoute(pm: String) = {
    QuarkRoute.instance {
      case HttpRequest(_, uri, _, _, _) if uri.path == Uri.Path(s"/$pm") =>
        QuarkRouteStatus.SUCCESS
    }
  }

}

object RouteBuilder extends RouteBuilder
