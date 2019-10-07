package cryptocurrency.network

import com.typesafe.config.{Config, ConfigFactory}

object NetworkConfig {
  val config: Config = ConfigFactory.load()

  implicit val httpHost: String = config.getString("http.host")
  implicit val httpPort: Int = config.getInt("http.port")

  implicit val miningDifficultyIncreaseRate: Int = config.getInt("mining.difficulty-increase-rate")
  implicit val defaultMiningDifficulty: Int = {
    val option = config.getInt("mining.default-difficulty");
    if(option < 1) 1 else option
  }
  implicit val blockReward: Int = config.getInt("mining.block-reward")

  implicit val walletNamePrefix: String = config.getString("wallet.name-prefix")
}
