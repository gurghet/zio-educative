import java.io.IOException

import CrystalGazer.CrystalGazer
import Match.Match
import zio._
import zio.console.{ Console, getStrLn, putStr, putStrLn }
import zio.macros.accessible

object LoveApp extends zio.App {
  val program: ZIO[Console with Match with CrystalGazer, IOException, Unit] =
    for {
      name1   <- putStr("Enter your name: ") *> getStrLn
      name2   <- putStr("Enter your crush name: ") *> getStrLn
      result  <- Match.computeMatch(name1, name2)
      fortune <- CrystalGazer.gaze(name1, name2)
      _       <- putStrLn(s"You are a $result% match!")
      _       <- putStrLn(fortune)
    } yield ()

  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] =
    program.provideCustomLayer(Match.live ++ CrystalGazer.live).exitCode
}

@accessible
object Match {
  type Match = Has[Match.Service]

  trait Service {
    def computeMatch(name1: String, name2: String): UIO[Int]
  }

  val live: ULayer[Match] =
    ZLayer.succeed((name1: String, name2: String) => UIO(100))
}

@accessible
object CrystalGazer {
  type CrystalGazer = Has[CrystalGazer.Service]

  trait Service {
    def gaze(name1: String, name2: String): UIO[String]
  }

  val live: ULayer[CrystalGazer] =
    ZLayer.succeed((name1: String, name2: String) =>
      UIO {
        val childrenCount = (name1.length + name2.length) / 4
        s"You will have $childrenCount children."
      }
    )
}
