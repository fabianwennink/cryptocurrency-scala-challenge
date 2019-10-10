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
import cryptocurrency.blockchain.Transaction
import cryptocurrency.network.NetworkActorEvents.{GenerateNewWalletEvent, MineBlockEvent, MineBlockSuccessEvent, RegisterTransactionEvent, RegisterTransactionSuccessEvent, RequestBlockChainEvent, RequestWalletBalanceEvent, RequestWalletBalanceSuccessEvent, RequestsAllWalletsEvent, VerifyChainIntegrityEvent}
import JsonProtocol._
import akka.http.scaladsl.model.StatusCodes.Success
import cryptocurrency.network.JsonProtocol
import spray.json._

trait NetworkRouting extends SprayJsonSupport {

  implicit lazy val timeout: Timeout = 5.seconds
  implicit val executionContext: ExecutionContext

  val route: Route =
    get {
      path("mine") {
        parameter("address") { address =>
          val chainFuture = (WebServer.actor ? MineBlockEvent(address)).mapTo[MineBlockSuccessEvent]
          onSuccess(chainFuture) { result =>
            complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, s"${ result.message }"))
          }
        }
      } ~
      path("blockchain") {
        val chainFuture = (WebServer.actor ? RequestBlockChainEvent).mapTo[RequestBlockChainEvent]
        onSuccess(chainFuture) { result =>
          complete(StatusCodes.OK, result.chain)
        }
      } ~
      path("blockchain" / "verify") {
        val chainFuture = (WebServer.actor ? VerifyChainIntegrityEvent).mapTo[VerifyChainIntegrityEvent]
        onSuccess(chainFuture) { result =>
          complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, s"Chain integrity status: ${ (if( result.status ) "Valid" else "Invalid") }"))
        }
      } ~
      path("wallet") {
        parameter("address") { address =>
          val walletFuture = (WebServer.actor ? RequestWalletBalanceEvent(address)).mapTo[RequestWalletBalanceSuccessEvent]
          onSuccess(walletFuture) { result =>
            complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, s"Wallet balance: ${ result.balance }"))
          }
        }
      } ~
      path("wallet" / "generate") {
        val walletFuture = (WebServer.actor ? GenerateNewWalletEvent).mapTo[GenerateNewWalletEvent]
        onSuccess(walletFuture) { result =>
          complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, s"Wallet name: ${ result.wallet.address }"))
        }
      } ~
      path("wallet" / "all") {
        val walletFuture = (WebServer.actor ? RequestsAllWalletsEvent).mapTo[RequestsAllWalletsEvent]
        onSuccess(walletFuture) { result =>
          complete(StatusCodes.OK, result.wallets)
        }
      }
    } ~
    post {
      path("transaction") {
        entity(as[Transaction]) { transaction =>
          val transactionFuture = (WebServer.actor ? RegisterTransactionEvent(transaction)).mapTo[RegisterTransactionSuccessEvent]
          onSuccess(transactionFuture) { result =>
            complete(StatusCodes.Created, result.message)
          }
        }
      }
    }
}
