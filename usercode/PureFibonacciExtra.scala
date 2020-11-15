import zio.prelude.{ SubtypeSmart, isGreaterThanEqualTo }

object PureFibonacciExtra extends App {
  object NonNegative extends SubtypeSmart[Int](isGreaterThanEqualTo(0)) {
    val one: NonNegative = NonNegative(1)
    val two: NonNegative = NonNegative(2)
    implicit class NonNegativeOps(private val nn1: NonNegative) extends AnyVal {
      def plus(nn2: NonNegative): NonNegative = NonNegative(nn1 + nn2)
      def unsafeMinus(nn2: Int): NonNegative =
        if (nn1 - nn2 >= 0) {
          NonNegative(nn1 - nn2)
        } else
          throw new ArithmeticException(
            s"$nn1 - $nn2 does not yield NonNegative"
          )
    }
  }
  type NonNegative = NonNegative.Type
  import NonNegative._

  def fib(n: NonNegative): NonNegative = n match {
    case NonNegative(0) => one
    case NonNegative(1) => one
    case _              =>
      // here n must be at least 2
      val nMinus2 = n.unsafeMinus(2)
      val nMinus1 = n.unsafeMinus(1)
      fib(nMinus2).plus(fib(nMinus1))
  }

  val value = 5
  println("Showing Fibonacci number " + value)
  NonNegative
    .make(value)
    .map(nonNegative => println(fib(nonNegative)))
    .mapError(println)
}
