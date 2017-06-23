package io.github.quark.flow

import akka.stream.scaladsl.Flow
import io.github.quark.flow.PipelineFlow.Output

trait OutgoingOpsFlow {
  lazy val outgoingFlow: Flow[Output, Output, _] = ???
}
