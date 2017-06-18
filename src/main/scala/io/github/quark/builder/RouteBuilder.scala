package io.github.quark.builder

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route

trait RouteBuilder {

  protected def filters: Seq[String] = Seq.empty[String]

  def via(f: Seq[String]): RouteBuilder =
    new RouteBuilder {
      override val filters: Seq[String] = f
    }

  def build(routes: Seq[String]): Route = {
    routes.map(routeBlueprint).reduceLeft(_ ~ _)
  }

  private def routeBlueprint(pm: String) = {
    path(pm) { ctx =>
      ctx.complete("")
    }
  }

}

object RouteBuilder extends RouteBuilder
