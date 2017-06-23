package io.github.quark.flow

import akka.stream.scaladsl.Flow
import io.github.quark.flow.PipelineFlow.Input

trait IncomingOpsFlow {
  lazy val incomingFlow: Flow[Input, Input, _] = ???
}
