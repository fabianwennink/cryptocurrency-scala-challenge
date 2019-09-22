package cryptocurrency.network

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import cryptocurrency.network.NetworkConfig.{httpHost, httpPort}

import scala.concurrent.ExecutionContextExecutor
import scala.io.StdIn

object WebServer extends NetworkRouting {

  implicit val system: ActorSystem = ActorSystem("scala-crypto")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  val actor: ActorRef = system.actorOf(NetworkActor.props, "cryptocurrency-server")

  def run(): Unit = {
    val bindingFuture = Http().bindAndHandle(route, httpHost, httpPort)

    println(s"Server running at $httpHost:$httpPort")

    StdIn.readLine()

    bindingFuture
      .flatMap(_.unbind())
      .onComplete(_ => system.terminate())
  }
}
