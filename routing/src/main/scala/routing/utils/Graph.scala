package routing.utils

import scala.collection.mutable.ListBuffer
import scala.collection.mutable.ArrayBuffer
import routing.model.{Node, Edge}
import routing.repository.{NodeRepository, EdgeRepository}

import zio.ZIO

object Graph {
  private var nodes: ArrayBuffer[Node] = new ArrayBuffer[Node]()
  private var edges: ArrayBuffer[Edge] = new ArrayBuffer[Edge]()
  private var graph: ArrayBuffer[ListBuffer[Edge]] = new ArrayBuffer[ListBuffer[Edge]]()

  def loadGraph: ZIO[NodeRepository with EdgeRepository, Throwable, Unit] = {
    nodes.clear
    edges.clear
    for {
      _ <- NodeRepository.findAllNodes.runCollect.map(_.toArray).either.flatMap{
        case Right(arr) => {
          ZIO.succeed(nodes ++= arr)
        }
        case Left(e) => {
          ZIO.fail(e)
        }
      }
      _ <- EdgeRepository.findAllEdges.runCollect.map(_.toArray).either.flatMap{
        case Right(arr) => {
          ZIO.succeed(edges ++= arr)
        }
        case Left(e) => {
          ZIO.fail(e)
        }
      }
      _ <- initGraph
    } yield ()
  }

  override def toString: String = {
    // return all data from nodes, edges, graph
    "Nodes: " +
    nodes.foldLeft(""){(old,value) => old + value.toString + " "} +
    "\nEdges: " +
    edges.foldLeft(""){(old,value) => old + value.toString + " "} +
    "\nGraph connections:\n" +
    graph.foldLeft(""){(oldA,valueA) =>
      oldA + valueA.foldLeft(""){(old,value) =>
        old + s"(${value.toid.toString},${value.fromid.toString}) "
      } + "\n"
    }
  }

  def initGraph: ZIO[Any, Throwable, Unit] = {
    // clear data
    graph.clear
    // construct graph
    nodes.foreach(_ => {
      graph += new ListBuffer[Edge]()
    })
    edges.foreach(edge => {
      graph(edge.fromid - 1) += edge
      graph(edge.toid - 1) += new Edge(edge.label, edge.toid, edge.fromid, edge.distance) // unoriented graph
    })
    // return
    ZIO.succeed(())
  }

  def astar(fromid: Integer, toid: Integer): ZIO[Any, Throwable, String] = {
    // find node with specified id
    val fromIndex = nodes.foldLeft((-1,0)){(old,value) =>
      old match {
        case (oldRes, oldCur) =>
          if (value.id == fromid) {
            (oldCur, oldCur + 1)
          } else {
            (oldRes, oldCur + 1)
          }
      }
    }._1
    val toIndex = nodes.foldLeft((-1,0)){(old,value) =>
      old match {
        case (oldRes, oldCur) =>
          if (value.id == toid) {
            (oldCur, oldCur + 1)
          } else {
            (oldRes, oldCur + 1)
          }
      }
    }._1
    if (fromIndex == -1 || toIndex == -1) return ZIO.fail("Wrong index")
    // find path
    val path: ListBuffer[Node] = new ListBuffer[Node]()
    path += nodes(fromIndex) += nodes(toIndex)
    // format as string
    ZIO.succeed(
      path.foldLeft(("Route: ",true)){(old,value) =>
        old match {
          case (oldStr, oldIsFirst) => {
            var res = ""
            if (!oldIsFirst) res += " - "
            if (value.category == "0") {
              res += "House "
            } else {
               res += "Crossroad " 
            }
            (oldStr + res + value.name, false)
          }
        }
      }._1
    )
  }
}
