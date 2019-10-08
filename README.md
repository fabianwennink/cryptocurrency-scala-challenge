# Learning Scala - HAN

Scala implementation of a very over-simplified crypto-currency.

Blog post: https://www.fabianwennink.nl/projects/Learning-Scala

**Note: This is not a real crypto-currency! The code is most likely not secure 
and should thus not be used in ANY production environment. The project was merely 
created as a programming challenge to become familiar with both the Scala 
language and blockchain technology.**

---------

**Mining**  
HTTP GET - Mine a new block: http://127.0.0.1:8080/mine?address=WALLET

**BlockChain**  
HTTP GET - See the full chain: http://127.0.0.1:8080/blockchain  
HTTP GET -  Check the integrity of the chain: http://127.0.0.1:8080/blockchain/verify

**Wallet**  
HTTP GET - Generate a new wallet: http://127.0.0.1:8080/wallet/generate  
HTTP GET - View the details of a wallet: http://127.0.0.1:8080/wallet?address=WALLET  
HTTP GET - See all generated wallets: http://127.0.0.1:8080/wallet/all

**Transaction**  
HTTP POST - Make a new transaction http://127.0.0.1:8080/transaction  

Transaction body (`application/json`)
```json
{
    "amount": 10,
	"receiver":{
		"address":"crypto-wallet-ADDRESS"
	},
	"sender":{
		"address":"crypto-wallet-ADDRESS"
	}
}
```
