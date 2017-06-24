package io.github.quark.route

import akka.http.scaladsl.model.{HttpRequest, Uri}
import io.github.quark.route.QuarkRouteStatus._
import org.scalatest.FunSuite

class RouteBuilderTest extends FunSuite {
  test("Route tree should be built correctly") {
    val candidates = Seq("a", "b", "c")
    val routes = RouteBuilder.build(candidates)
    val request = Seq(
      HttpRequest(null, Uri("/a")),
      HttpRequest(null, Uri("/d")),
      HttpRequest(null, Uri("/b")),
      HttpRequest(null, Uri("/c")),
      HttpRequest(null, Uri("/f")),
      HttpRequest(null, Uri("/a/"))
    )
    val results = request.map(routes)
    assert(
      results === Seq(Success, Failure, Success, Success, Failure, Success))
  }

  test("Route tree should detect path with sub routes, query params") {
    val candidates = Seq("a", "b", "c")
    val routes = RouteBuilder.build(candidates)
    val request = Seq(
      HttpRequest(null, Uri("/a/bla")),
      HttpRequest(null, Uri("/a/bla/")),
      HttpRequest(null, Uri("/a/bla/blabla")),
      HttpRequest(null, Uri("/c?q=bla")),
      HttpRequest(null, Uri("/c?q=bla&p=blabla")),
      HttpRequest(null, Uri("/b1")),
      HttpRequest(null, Uri("/b1/bla")),
      HttpRequest(null, Uri("/ab"))
    )
    val results = request.map(routes)
    assert(
      results === Seq(Success,
                      Success,
                      Success,
                      Success,
                      Success,
                      Failure,
                      Failure,
                      Failure)
    )
  }
}
