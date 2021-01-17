import zio.console.putStrLn
import zio.stream.ZStream
import zio.{ ExitCode, UIO, URIO }

object WordCounter extends zio.App {

  val poem: String =
    """Never trust atoms.
      |They make up everything.""".stripMargin

  case class Accumulator(count: Long, prevChar: Char)

  val stream: UIO[Long] = ZStream
    .fromIterable(poem)
    .mapAccum(Accumulator(0, ' ')) { (acc, char) =>
      val newAcc =
        if (char.isLetterOrDigit && !acc.prevChar.isLetterOrDigit)
          Accumulator(acc.count + 1, char)
        else Accumulator(acc.count, char)
      (newAcc, newAcc.count)
    }
    .runLast
    .map(_.getOrElse(0))

  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] =
    stream.flatMap(s => putStrLn(s.toString)).exitCode
}
