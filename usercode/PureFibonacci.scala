import zio.prelude.{ SubtypeSmart, isGreaterThanEqualTo }
object PureFibonacci extends App {
  object NonNegative extends SubtypeSmart[Int](isGreaterThanEqualTo(0)) {
    val one: NonNegative = NonNegative(1)
    val two: NonNegative = NonNegative(2)
    implicit class NonNegativeOps(private val nn1: NonNegative) extends AnyVal {
      def plus(nn2: NonNegative): NonNegative = NonNegative(nn1 + nn2)
    }
  }
  type NonNegative = NonNegative.Type
  import NonNegative._

  def fib(n: NonNegative): NonNegative = n match {
    case NonNegative(0) => one
    case NonNegative(1) => one
    case _              =>
      // here n must be at least 2
      val nMinus2 = (n - 2).asInstanceOf[NonNegative]
      val nMinus1 = (n - 1).asInstanceOf[NonNegative]
      fib(nMinus2).plus(fib(nMinus1))
  }

  val value = 5
  println("Showing Fibonacci number " + value)
  NonNegative
    .make(value)
    .map(nonNegative => println(fib(nonNegative)))
    .mapError(println)
}
