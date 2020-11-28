import java.io.IOException

import zio.{ ExitCode, IO, UIO, URIO, ZIO }
import zio.console.{ Console, getStrLn, putStr, putStrLn }

import scala.io.Source

object BetsAndBets extends zio.App {
  val program: ZIO[Console, IOException, Unit] = for {
    name      <- putStr("Enter your name: ") *> getStrLn
    betAmount <- putStr("How much you want to bet: ") *> getStrLn.map(_.toInt)
    betTarget <-
      putStr("Guess the least significant digit of the value of 1 Bitcoin: ") *>
        getStrLn.map(_.toInt)
    bitcoinPrice <-
      ZIO
        .fromOption(getBitcoinPrice)
        .mapError(_ => new IOException("Could not retrieve bitcoin price"))
    target = getLeastSignificantDigit(bitcoinPrice)
    _ <- if (target == betTarget)
           putStrLn(s"Congratulations $name! The price was $bitcoinPrice")
         else
           putStrLn(s"Sorry, you lost €$betAmount!")
  } yield ()

  def getBitcoinPrice: Option[Double] = {
    val url =
      "https://api.coingecko.com/api/v3/simple/price?ids=bitcoin&vs_currencies=eur"
    val rawJson             = Source.fromURL(url).mkString
    val regexToExtractPrice = """.+\"eur\"\:([0-9.]+).+""".r
    rawJson match {
      case regexToExtractPrice(price) => price.toDoubleOption
      case _                          => None
    }
  }

  def getLeastSignificantDigit(d: Double): Int = (d * 100 % 10).toInt

  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] =
    program.exitCode
}
