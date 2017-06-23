package io.github.quark.flow

import akka.stream.scaladsl.Flow
import io.github.quark.flow.PipelineFlow.{Input, Output}

trait RoutingOpsFlow {
  lazy val routingFlow: Flow[Input, Output, _] = ???
}
