package io.github.quark.route

import akka.http.scaladsl.model.{HttpRequest, Uri}
import io.github.quark.dsl.DSL.proxy
import io.github.quark.route.RouteStatus.{Matched, UnMatched}
import org.scalatest.FunSuite

class RouteTest extends FunSuite {
  val fakeService = proxy("test")
  val route = Route.instance(fakeService)

  test("Route should be built correctly") {
    val request = Seq(
      HttpRequest(null, Uri("/test")),
      HttpRequest(null, Uri("/b")),
      HttpRequest(null, Uri("/test1")),
      HttpRequest(null, Uri("/test2/")),
      HttpRequest(null, Uri("/test/"))
    )
    val results = request.map(route)
    assert(
      results === Seq(Matched(fakeService),
                      UnMatched,
                      UnMatched,
                      UnMatched,
                      Matched(fakeService)))
  }

  test("Route should detect path with sub routes and query params") {
    val request = Seq(
      HttpRequest(null, Uri("/test/bla")),
      HttpRequest(null, Uri("/test/bla/")),
      HttpRequest(null, Uri("/test/bla/blabla")),
      HttpRequest(null, Uri("/test2?q=bla")),
      HttpRequest(null, Uri("/test2?q=bla&p=blabla")),
      HttpRequest(null, Uri("/faketest")),
      HttpRequest(null, Uri("/faketesttest/bla")),
      HttpRequest(null, Uri("/random"))
    )
    val results = request.map(route)
    assert(
      results === Seq(Matched(fakeService),
                      Matched(fakeService),
                      Matched(fakeService),
                      UnMatched,
                      UnMatched,
                      UnMatched,
                      UnMatched,
                      UnMatched)
    )
  }
}
