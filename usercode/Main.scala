import zio.console.putStrLn
import zio.prelude.fx.ZPure
import zio.prelude.{State, Validation, isGreaterThanEqualTo}
import zio.{ExitCode, URIO, ZIO}

object Main extends zio.App {
  import zio.prelude.NewtypeSmart
  object Natural extends NewtypeSmart[Int](isGreaterThanEqualTo(0))
  type Natural = Natural.Type

  def fib(n: Natural): Validation[String, Natural] =
    Natural.make(fibInt(Natural.unwrap(n)))

  def fibInt(n: Int): Int = n match {
    case 0|1 => 1
    case _ => fibInt(n - 2) + fibInt(n - 1)
  }
  def badInput(n: Int): Nothing =
    throw new IllegalArgumentException(
      s"fib only accepts non-negative numbers, got $n"
    )


  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] =
    fib(-3.asInstanceOf[Natural])
      .fold(err => ZIO.fail(err.mkString), n => putStrLn(Natural.unwrap(n).toString))
      .exitCode
}
