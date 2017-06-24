package io.github.quark.stage

import akka.http.scaladsl.model.HttpResponse
import akka.stream.scaladsl.{Flow, GraphDSL, Merge}
import akka.stream.{FlowShape, Graph}
import io.github.quark.stage.OperationStage.OperationGraph
import io.github.quark.stage.PipelineStage.{Input, Output}
import io.github.quark.stage.StageResult.Failed

trait RequestProcessingStage {

  protected val incomingGraph: OperationGraph[Input, Input] =
    OperationStage[Input, Input]

  protected val outgoingGraph: OperationGraph[Output, Output] =
    OperationStage[Output, Output]

  protected val endpointGraph: OperationGraph[Input, Output] =
    OperationStage[Input, Output]

  lazy val requestProcessingGraph: Graph[FlowShape[Input, Output], _] =
    GraphDSL
      .create() { implicit builder =>
        import GraphDSL.Implicits._

        val input = builder.add(inputStage)

        val incoming = builder.add(incomingGraph)
        val outgoing = builder.add(outgoingGraph)
        val endpoint = builder.add(endpointGraph)

        val responseMerge = builder.add(responseMergeStage)

        val incomingOut = OperationStage.fromOutletResult(incoming.out(1))
        val endpointOut = OperationStage.fromOutletResult(endpoint.out(1))
        val outgoingOut = OperationStage.fromOutletResult(outgoing.out(1))

        // format: off
        input ~> incoming.in
                 incoming.out(0) ~> abortResponseFlow ~> responseMerge.in(0)
                 incomingOut ~> endpoint.in
                                endpoint.out(0) ~> abortResponseFlow ~> responseMerge.in(1)
                                endpointOut ~> outgoing.in
                                               outgoing.out(0) ~> abortResponseFlow ~> responseMerge.in(2)
                                               outgoingOut ~> responseMerge.in(3)
        // format: onf

    FlowShape(input.in, responseMerge.out)
    }
      .named("processing-flow")

  private lazy val inputStage = Flow[Input]
  private lazy val responseMergeStage = Merge[Output](4)
  private lazy val abortResponseFlow: Flow[StageResult[_], Output, _] =
    Flow[StageResult[_]]
      .map {
        case Failed(cause) => HttpResponse(entity = cause.cause)
        case _ => throw new MatchError("Abort flow excepts failed result.")
      }
}