package io.github.quark.dsl

import io.github.quark.action.{OperationAction, ServiceAction}
import shapeless.{::, HList, HNil}

trait Concatenation {

  implicit class Concatenation[P, L <: HList](l: L) {
    def ~[M <: P](other: M): M :: L = other +: l
  }

  implicit def enhanceOperationWithConcatenation[P <: OperationAction](
      action: P): Concatenation[OperationAction, P :: HNil] =
    new Concatenation[OperationAction, P :: HNil](action :: HNil)

  implicit def enhanceServiceWithConcatenation[P <: ServiceAction](
      action: P): Concatenation[ServiceAction, P :: HNil] =
    new Concatenation[ServiceAction, P :: HNil](action :: HNil)

}
