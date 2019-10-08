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
import cryptocurrency.blockchain.{BlockChain, Transaction}
import cryptocurrency.network.NetworkActorEvents.{AddTransactionEvent, AddTransactionSuccessEvent, GetWalletsEvent, MineEvent, MineSuccessEvent, RegisterWalletEvent, RequestBlockChainEvent, VerifyIntegrityEvent}
import JsonProtocol._
import cryptocurrency.network.JsonProtocol
import spray.json._

trait NetworkRouting extends SprayJsonSupport {

  implicit lazy val timeout: Timeout = 5.seconds
  implicit val executionContext: ExecutionContext

  val route: Route =
    get {
      path("mine") {
        parameter('address) { address =>
          val chainFuture: Future[MineSuccessEvent] = (WebServer.actor ? MineEvent(address)).mapTo[MineSuccessEvent]
          onSuccess(chainFuture) { result =>
            complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, s"${ result.message }"))
          }
        }
      } ~
      path("blockchain") {
        val chainFuture: Future[BlockChain] = (WebServer.actor ? RequestBlockChainEvent).mapTo[BlockChain]
        onSuccess(chainFuture) { result =>
          complete(StatusCodes.OK, result)
        }
      } ~
      path("blockchain" / "verify") {
        val chainFuture: Future[VerifyIntegrityEvent] = (WebServer.actor ? VerifyIntegrityEvent).mapTo[VerifyIntegrityEvent]
        onSuccess(chainFuture) { result =>
          complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, s"Chain integrity status: ${ (if( result.status ) "Valid" else "Invalid") }"))
        }
      } ~
      path("wallet") {
        parameter('address) { address =>
          complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, s"Not yet implemented"))
        }
      } ~
      path("wallet" / "generate") {
        val walletFuture: Future[RegisterWalletEvent] = (WebServer.actor ? RegisterWalletEvent).mapTo[RegisterWalletEvent]
        onSuccess(walletFuture) { result =>
          complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, s"Wallet name: ${ result.wallet.address }"))
        }
      } ~
      path("wallet" / "all") {
        val walletFuture: Future[GetWalletsEvent] = (WebServer.actor ? GetWalletsEvent).mapTo[GetWalletsEvent]
        onSuccess(walletFuture) { result =>
          complete(StatusCodes.OK, result.wallets)
        }
      }
    } ~
    post {
      path("transaction") {
        entity(as[Transaction]) { transaction =>
          val transactionFuture: Future[AddTransactionSuccessEvent] = (WebServer.actor ? AddTransactionEvent(transaction)).mapTo[AddTransactionSuccessEvent]
          onSuccess(transactionFuture) { result =>
            complete(StatusCodes.Created, result.message)
          }
        }
      }
    }
}
