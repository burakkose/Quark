package io.github.quark.gateway

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.settings.ServerSettings
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Flow
import com.typesafe.config.ConfigFactory
import io.github.quark.action.GatewayAction
import io.github.quark.resolver.{ServiceResolver, SimpleServiceResolver}
import io.github.quark.stage.PipelineStage

class GatewayApp(gate: GatewayAction,
                 serviceResolver: Option[ServiceResolver] = None) {

  def start(host: String,
            port: Int,
            settings: ServerSettings,
            system: Option[ActorSystem]): Unit = {

    implicit val theSystem = system.getOrElse(ActorSystem())
    implicit val materializer = ActorMaterializer()
    implicit val executionContext = theSystem.dispatcher

    val flow =
      PipelineStage(gate, serviceResolver.getOrElse(SimpleServiceResolver()))
    val f = Flow.fromGraph(flow.pipelineFlow)

    Http()
      .bindAndHandle(f, interface = host, port = port)
      .foreach(_ => println("started"))
  }

  def start(host: String, port: Int): Unit = {
    start(host, port, ServerSettings(ConfigFactory.load))
  }

  def start(host: String, port: Int, settings: ServerSettings): Unit = {
    start(host, port, settings, None)
  }

  def start(host: String,
            port: Int,
            settings: ServerSettings,
            system: ActorSystem): Unit = {
    start(host, port, settings, Some(system))
  }

}

object GatewayApp {
  def apply(gate: GatewayAction,
            serviceResolver: Option[ServiceResolver] = None): GatewayApp =
    new GatewayApp(gate, serviceResolver)
}
