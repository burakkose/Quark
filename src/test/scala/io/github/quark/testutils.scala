package io.github.quark

object testutils {
  def assertTypedEquals[A](expected: A, actual: A): Unit =
    assert(expected == actual)

  def assertTypedEquals[A](expected: A): Unit = assert(true)
}
