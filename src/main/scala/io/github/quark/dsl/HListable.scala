package io.github.quark.dsl

import io.github.quark.action.{OperationAction, ServiceAction}
import shapeless._

trait HListable[T] {
  type Out <: HList
  def apply(value: T): Out
}

object HListable {
  type Aux[T, Out0 <: HList] = HListable[T] { type Out = Out0 }

  implicit def fromHList[T <: HList] = new HListable[T] {
    type Out = T
    def apply(value: T) = value
  }

  implicit def fromOperationAction[T <: OperationAction] = new HListable[T] {
    type Out = T :: HNil
    def apply(value: T) = value :: HNil
  }

  implicit def fromServiceAction[T <: ServiceAction] = new HListable[T] {
    type Out = T :: HNil
    def apply(value: T) = value :: HNil
  }
}
