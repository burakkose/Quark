package io.github.quark.flow

import akka.http.scaladsl.model.HttpResponse
import akka.stream.scaladsl.{Flow, GraphDSL, Merge, Partition}
import io.github.quark.flow.PipelineFlow.Input

trait RequestProcessingFlow
    extends IncomingOpsFlow
    with OutgoingOpsFlow
    with RoutingOpsFlow {

  lazy val foundFlow = GraphDSL.create() { implicit builder =>
    val input = builder.add(inputStage)

    val incomingOps = builder.add(routingFlow)
    val outgoingOps = builder.add(outgoingFlow)
    val routingOps = builder.add(routingOps)

    val partition = builder.add(partitionStage)
    val merge = builder.add(mergeStage)
    val abort = builder.add(abortStage)
  }

  private val inputStage = Flow[Input]
  private val partitionStage = Partition[Input](2, null)
  private val abortStage = Flow[Input].map(_ => HttpResponse())
  private val mergeStage = Merge[HttpResponse](2)
}
