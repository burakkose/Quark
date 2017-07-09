package io.github.quark.http

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.settings.ServerSettings
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Flow
import com.typesafe.config.ConfigFactory
import io.github.quark.route.Route
import io.github.quark.stage.PipelineStage

import scala.concurrent.ExecutionContext.Implicits.global

trait WebServer {

  protected val routes: Route

  def start(host: String,
            port: Int,
            settings: ServerSettings,
            system: Option[ActorSystem]): Unit = {

    implicit val theSystem = system.getOrElse(ActorSystem())
    implicit val materializer = ActorMaterializer()

    val flow = PipelineStage(routes)
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
