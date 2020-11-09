import java.io.IOException

import zio.ZIO
import zio.ZIO.validatePar
import zio.console.{ Console, getStrLn, putStr, putStrLn }

object CollectFailures extends App {
  trait Validable {
    def isValid: Either[String, Unit]
  }

  final case class Name(name: String) extends Validable {
    override def isValid: Either[String, Unit] =
      if (name == "Null")
        Left("Name cannot be Null")
      else
        Right(())
  }
  final case class Surname(surname: String) extends Validable {
    override def isValid: Either[String, Unit] =
      if (surname.length > 1)
        Right(())
      else
        Left("Surname should have at least 2 letters")
  }

  val askName: ZIO[Console, IOException, Name] =
    putStr("Name: ") *> getStrLn.map(Name)
  val askSurname: ZIO[Console, IOException, Surname] =
    putStr("Surname: ") *> getStrLn.map(Surname)
  val questionnaire: ZIO[Console, IOException, List[Validable]] =
    for {
      name    <- askName
      surname <- askSurname
    } yield List(name, surname)

  val composite: ZIO[Console, List[String], List[Validable]] =
    for {
      results <- questionnaire.orDie
      _       <- validatePar(results)(field => ZIO.fromEither(field.isValid))
      _ <- putStrLn("User correctly registered:") *>
             putStrLn(results.mkString(", "))
    } yield results

  val program: ZIO[Console, Nothing, List[Validable]] = composite.catchAll {
    errors =>
      putStrLn("Please correct these errors and submit again: ") *>
        putStrLn(errors.mkString(", ")) *> program
  }

  val runtime: zio.Runtime[zio.ZEnv] = zio.Runtime.default
  runtime.unsafeRun(program)
}
