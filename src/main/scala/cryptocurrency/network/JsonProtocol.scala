package cryptocurrency.network

import cryptocurrency.blockchain.{Block, BlockChain, BlockHeader, GenesisBlock, Transaction, Wallet}
import spray.json._

object JsonProtocol extends DefaultJsonProtocol {

  // Wallet JSON object
  implicit object WalletJsonFormat extends RootJsonFormat[Wallet] {
    override def read(json: JsValue): Wallet = {
      json.asJsObject.getFields("address") match {
        case Seq(JsString(address)) => Wallet(address)
        case Seq() => throw DeserializationException("Invalid Wallet")
      }
    }

    override def write(wallet: Wallet): JsValue = JsObject(
      "address" -> JsString(wallet.address)
    )
  }

  // Transaction JSON object
  implicit object TransactionJsonFormat extends RootJsonFormat[Transaction] {
    override def read(json: JsValue): Transaction = {
      json.asJsObject.getFields("sender", "receiver", "amount") match {
        case Seq(sender, receiver, JsNumber(amount), JsNumber(timestamp)) =>
          Transaction(sender.convertTo(WalletJsonFormat), receiver.convertTo(WalletJsonFormat), amount.toInt, timestamp.toLong)
        case Seq(sender, receiver, JsNumber(amount)) =>
          Transaction(sender.convertTo(WalletJsonFormat), receiver.convertTo(WalletJsonFormat), amount.toInt)
        case Seq() => throw DeserializationException("Invalid BlockHeader")
      }
    }

    override def write(transaction: Transaction): JsValue = JsObject(
      "sender" -> transaction.sender.toJson,
      "receiver" -> transaction.receiver.toJson,
      "amount" -> JsNumber(transaction.amount),
      "timestamp" -> JsNumber(transaction.timestamp)
    )
  }

  // Block JSON object
  implicit object BlockJsonFormat extends RootJsonFormat[Block] {
    override def read(json: JsValue): Block = {
      json.asJsObject.getFields("index", "header", "transactions", "previous") match {
        case Seq(JsNumber(index), header, transactions, tail) =>
          Block(index.toInt, header.convertTo(BlockHeaderJsonFormat), transactions.convertTo[List[Transaction]], tail.convertTo(BlockChainJsonFormat))
        case _ => throw DeserializationException("Invalid Block")
      }
    }

    override def write(block: Block): JsValue = JsObject(
      "index" -> JsNumber(block.index),
      "header" -> BlockHeaderJsonFormat.write(block.header),
      "transactions" -> JsArray(block.transactions.map(_.toJson).toVector),
      "previous" -> BlockChainJsonFormat.write(block.previous)
    )
  }

  // BlockChain JSON object
  implicit object BlockChainJsonFormat extends RootJsonFormat[BlockChain] {
    override def read(json: JsValue): BlockChain = {
      json.asJsObject.getFields("index", "header", "transactions", "previous") match {
        case Seq(_) => json.convertTo[Block]
        case Seq() => GenesisBlock
      }
    }

    override def write(chain: BlockChain): JsValue = chain match {
      case block: Block => block.toJson
      case GenesisBlock => JsObject(
        "index" -> JsNumber(GenesisBlock.index),
        "header" -> GenesisBlock.header.toJson,
        "transactions" -> JsArray()
      )
    }
  }

  // BlockHeader JSON object
  implicit object BlockHeaderJsonFormat extends RootJsonFormat[BlockHeader] {
    override def read(json: JsValue): BlockHeader = {
      json.asJsObject.getFields("hash", "nonce", "merkle", "difficulty", "reward", "timestamp") match {
        case Seq(JsString(previousHash), JsNumber(nonce), JsString(merkle), JsNumber(difficulty), JsNumber(reward),  JsNumber(timestamp)) =>
          BlockHeader(previousHash, nonce.toInt, merkle, difficulty.toInt, reward.toInt, timestamp.toLong)
        case Seq() => throw DeserializationException("Invalid BlockHeader")
      }
    }

    override def write(header: BlockHeader): JsValue = JsObject(
      "hash" -> JsString(header.hash),
      "nonce" -> JsNumber(header.nonce),
      "merkle" -> JsString(header.merkle),
      "difficulty" -> JsNumber(header.difficulty),
      "reward" -> JsNumber(header.reward),
      "timestamp" -> JsNumber(header.timestamp)
    )
  }
}
