package routing.utils

import scala.collection.mutable.ListBuffer

object Graph {
  private var res: ListBuffer[String] = new ListBuffer[String]()

  def get: ListBuffer[String] = {
    res += " aaa "
    res
  }
}
