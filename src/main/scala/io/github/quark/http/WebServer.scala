package io.github.quark.http

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.settings.ServerSettings
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory

trait WebServer {

  protected val routes: Route

  def start(host: String,
            port: Int,
            settings: ServerSettings,
            system: Option[ActorSystem]): Unit = {

    implicit val theSystem = system.getOrElse(ActorSystem())
    implicit val materializer = ActorMaterializer()

    import theSystem.dispatcher

    Http()
      .bindAndHandle(handler = routes,
                     interface = host,
                     port = port,
                     settings = settings)
      .foreach(_ => println(s"Server online at http://$host:$port/"))
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
