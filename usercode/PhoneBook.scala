import Database.Database
import zio._
import zio.console.{ Console, getStrLn, putStr }
import zio.logging.{ Logger, Logging }
import zio.macros.accessible

import java.io.IOException

object PhoneBook extends zio.App {
  val programWrite: ZIO[Database with Console, IOException, Unit] = for {
    name  <- putStr("Enter name: ") *> getStrLn
    phone <- putStr("Enter phone: ") *> getStrLn
    _     <- Database.store(name, phone)
  } yield ()

  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] = programWrite
    .provideCustomLayer(Database.logless)
    .exitCode
}

@accessible
object Database {
  type Database = Has[Database.Service]

  trait Service {
    def store(key: String, value: String): UIO[Unit]
  }

  val live: URLayer[Logging, Database] =
    ZLayer.fromService[Logger[String], Database.Service](logger => Live(logger))

  val logless: ULayer[Database] =
    ZLayer.succeed {
      new Service {
        override def store(key: String, value: String): UIO[Unit] = UIO(())
      }
    }

  case class Live(logger: Logger[String]) extends Database.Service {
    def store(key: String, value: String): UIO[Unit] =
      logger.info(s"Writing ($key, $value)") *> UIO(())
  }
}
