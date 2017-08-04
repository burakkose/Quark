package io.github.quark.resolver

import java.util.concurrent.atomic.AtomicReference

import io.github.quark.config.Config
import io.github.quark.resolver.ServiceResolver.{ServiceID, ServiceLocation}

trait SimpleServiceResolver extends ServiceResolver

object SimpleServiceResolver {
  def apply(): SimpleServiceResolver = new SimpleServiceResolver {
    override protected def routes
      : AtomicReference[Map[ServiceID, ServiceLocation]] =
      new AtomicReference[Map[ServiceID, ServiceLocation]](Config.staticRoutes)
  }
}
