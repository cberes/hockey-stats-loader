package net.seabears.hockey

object Memoize {
  def apply[T, R](f: T => R) = new Memoize(f)
}

class Memoize[-T, +R](func: T => R) extends (T => R) {
  private[this] var values = Map.empty[T, R]

  def apply(arg: T): R = {
    if (values.contains(arg)) {
      values(arg)
    } else {
      val result = func(arg)
      values = values + (arg -> result)
      result
    }
  }
}
