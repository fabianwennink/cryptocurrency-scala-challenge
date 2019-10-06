package cryptocurrency.network

import cryptocurrency.blockchain.{Block, BlockChain, BlockHeader, GenesisBlock}
import spray.json._

object JsonProtocol extends DefaultJsonProtocol {

  implicit object BlockJsonFormat extends RootJsonFormat[Block] {
    override def read(json: JsValue): Block = json.asJsObject.getFields("index", "header", "previous") match {
      case Seq(JsNumber(index), header, tail) =>
        Block(index.toInt, header.convertTo(BlockHeaderJsonFormat), tail.convertTo(BlockChainJsonFormat))
      case _ => throw DeserializationException("Invalid Block")
    }

    override def write(block: Block): JsValue = JsObject(
      "index" -> JsNumber(block.index),
      "header" -> BlockHeaderJsonFormat.write(block.header),
      "previous" -> BlockChainJsonFormat.write(block.previous)
    )
  }

  implicit object BlockChainJsonFormat extends RootJsonFormat[BlockChain] {
    override def read(json: JsValue): BlockChain = {
      json.asJsObject.getFields("index") match {
        case Seq(_) => json.convertTo[Block]
        case Seq() => GenesisBlock
      }
    }

    override def write(chain: BlockChain): JsValue = chain match {
      case block: Block => block.toJson
      case GenesisBlock => JsObject(
        "index" -> JsNumber(GenesisBlock.index),
        "header" -> GenesisBlock.header.toJson
      )
    }
  }

  implicit object BlockHeaderJsonFormat extends RootJsonFormat[BlockHeader] {
    override def read(json: JsValue): BlockHeader = {
      json.asJsObject.getFields("hash") match {
        case Seq(JsString(previousHash), JsNumber(nonce), JsNumber(difficulty), JsNumber(reward),  JsNumber(timestamp)) =>
          BlockHeader(previousHash, nonce.toInt, difficulty.toInt, reward.toInt, timestamp.toLong)
        case Seq() => throw DeserializationException("Invalid BlockHeader")
      }
    }

    override def write(header: BlockHeader): JsValue = JsObject(
      "hash" -> JsString(header.hash),
      "nonce" -> JsNumber(header.nonce),
      "difficulty" -> JsNumber(header.difficulty),
      "reward" -> JsNumber(header.reward),
      "timestamp" -> JsNumber(header.timestamp)
    )
  }
}
