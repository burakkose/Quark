package io.github.quark.stage

sealed trait StageResult[+T] {
  def isSuccess: Boolean
}

object StageResult {

  final case class Complete[+T](result: T) extends StageResult[T] {
    override def isSuccess: Boolean = true
  }

  final case class Failed[+T](cause: StageFailCause) extends StageResult[T] {
    override def isSuccess: Boolean = false
  }

}

final case class StageFailCause(cause: String)
