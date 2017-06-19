package io.github.quark.route

import akka.http.scaladsl.model.HttpRequest

trait QuarkRoute extends QuarkRoute.PF

object QuarkRoute {
  type PF = PartialFunction[HttpRequest, QuarkRouteStatus]

  def instance(pf: PF): QuarkRoute =
    new QuarkRoute {
      override def isDefinedAt(x: HttpRequest): Boolean = pf.isDefinedAt(x)

      override def apply(v1: HttpRequest): QuarkRouteStatus = pf(v1)

    }
}
