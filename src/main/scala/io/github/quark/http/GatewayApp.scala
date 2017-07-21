package io.github.quark.http

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.settings.ServerSettings
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Flow
import com.typesafe.config.ConfigFactory
import io.github.quark.action.{GatewayAction, ServiceSelector}
import io.github.quark.stage.PipelineStage
import shapeless.HList

import scala.concurrent.ExecutionContext.Implicits.global

class GatewayApp[L <: HList](gate: GatewayAction[L])(
    implicit selector: ServiceSelector[L]) {

  def start(host: String,
            port: Int,
            settings: ServerSettings,
            system: Option[ActorSystem]): Unit = {

    implicit val theSystem = system.getOrElse(ActorSystem())
    implicit val materializer = ActorMaterializer()

    val flow = PipelineStage(gate)
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
  def apply[L <: HList](gate: GatewayAction[L])(
      implicit selector: ServiceSelector[L]): GatewayApp[L] =
    new GatewayApp(gate)
}
