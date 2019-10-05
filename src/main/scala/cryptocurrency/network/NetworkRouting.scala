package cryptocurrency.network

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import akka.http.scaladsl.server.Directives.{complete, get, path}

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._
import akka.util.Timeout
import akka.pattern.ask
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import cryptocurrency.blockchain.BlockChain
import cryptocurrency.network.NetworkActor.{MineEvent, RequestBlockChainEvent, VerifyIntegrityEvent}
import JsonProtocol._

trait NetworkRouting extends SprayJsonSupport {

  implicit lazy val timeout: Timeout = 5.seconds
  implicit val executionContext: ExecutionContext

  val route: Route =
    get {
      path("mine") {
        val chainFuture: Future[MineEvent] = (WebServer.actor ? MineEvent).mapTo[MineEvent]
        onSuccess(chainFuture) { result =>
          complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, s"${ result.message }"))
        }
      } ~
      path("blockchain") {
        val chainFuture: Future[BlockChain] = (WebServer.actor ? RequestBlockChainEvent).mapTo[BlockChain]
        onSuccess(chainFuture) { result =>
          complete(StatusCodes.OK, result)
        }
      } ~
      path("verify") {
        val chainFuture: Future[VerifyIntegrityEvent] = (WebServer.actor ? VerifyIntegrityEvent).mapTo[VerifyIntegrityEvent]
        onSuccess(chainFuture) { result =>
          complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, s"Chain integrity status: ${ (if( result.status ) "Valid" else "Invalid") }"))
        }
      }
    }
}
