package io.github.quark.action

import shapeless.{::, DepFn1, HList, HNil}

trait OperationSelector[L <: HList, U <: OperationAction] extends DepFn1[L] {
  type Out = U
}

object OperationSelector {
  implicit def hNilSelector[U <: OperationAction](implicit action: U) =
    new OperationSelector[HNil, U] {
      override def apply(l: HNil): U = action
    }

  implicit def hListSelectSelector[H <: OperationAction, T <: HList] =
    new OperationSelector[H :: T, H] {
      override def apply(l: ::[H, T]): H = l.head
    }

  implicit def hListSelector[H, T <: HList, U <: OperationAction](
      implicit st: OperationSelector[T, U]) =
    new OperationSelector[H :: T, U] {
      override def apply(t: ::[H, T]): U = st(t.tail)
    }
}
