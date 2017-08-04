package io.github.quark.config

import pureconfig._

object Config {
  lazy val staticRoutes: Map[String, String] =
    loadConfigOrThrow[QuarkRouteConfig]("quark").routes
}

case class QuarkRouteConfig(routes: Map[String, String])
