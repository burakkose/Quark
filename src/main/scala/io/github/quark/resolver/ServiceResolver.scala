package io.github.quark.resolver

import java.util.concurrent.atomic.AtomicReference

import akka.http.scaladsl.server.PathMatcher.Matched
import io.github.quark.resolver.ServiceResolver._
import akka.http.scaladsl.server.PathMatchers.{Remaining, Segment, Slash}
import io.github.quark.stage.PipelineStage.Input

trait ServiceResolver {
  protected def routes: AtomicReference[Map[ServiceID, ServiceLocation]]

  def findServiceLocation(request: Input): Option[ResolverResponse] = {
    val inputPath = request.uri.path
    pathMatcher(inputPath) match {
      case Matched(_, (serviceID, rest)) if rest.nonEmpty =>
        routes.get().get(serviceID).map((_, rest))
      case _ => None
    }
  }

  private val pathMatcher = Slash ~ Segment ~ Remaining
}

object ServiceResolver {
  type ServiceID = String
  type ServiceLocation = String
  type RestPath = String
  type ResolverResponse = (ServiceLocation, RestPath)
}
