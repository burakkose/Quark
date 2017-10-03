package io.github.quark.route

import io.github.quark.action.ServiceAction.Service
import io.github.quark.dsl.DSL._
import io.github.quark.testutils._
import org.scalatest.FunSuite
import shapeless._

class RouteBuilderTest extends FunSuite {
  test("Builder should take an HList of services and return HList of Routes") {
    val services = proxy("test1") :: proxy("test2") :: HNil
    val builder = implicitly[
      RouteBuilder.Aux[Service :: Service :: HNil, Route :: Route :: HNil]]
    assertTypedEquals[Route :: Route :: HNil](builder(services))
  }
}
