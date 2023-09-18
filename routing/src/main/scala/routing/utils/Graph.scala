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
          ZIO.succeed(arr.foreach(node => {
            println(node)
            nodes += node
          }))
        }
        case Left(e) => {
          ZIO.fail(e)
        }
      }
      _ <- EdgeRepository.findAllEdges.runCollect.map(_.toArray).either.flatMap{
        case Right(arr) => {
          ZIO.succeed(arr.foreach(edge => {
            println(edge)
            edges += edge
          }))
        }
        case Left(e) => {
          ZIO.fail(e)
        }
      }
      _ <- initGraph
    } yield ()
  }

  def debug_graph: ArrayBuffer[String] = {
    // return all data from nodes, edges, graph
    var res = edges.map(x => x.label) ++ nodes.map(x => x.name)
    res += "_"
    graph.foreach(row => {
      row.foreach(edge => {
        res += edge.label
      })
      res += "_"
    })
    res
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
      graph(edge.toid - 1) += edge // unoriented graph
    })
    // return
    ZIO.succeed(())
  }

  def astar(fromId: Integer, toId: Integer): ZIO[Any, Throwable, ListBuffer[String]] = {
    // find path
    var res: ListBuffer[String] = new ListBuffer[String]()
    res ++= debug_graph
    res += fromId.toString += toId.toString
    // return
    ZIO.succeed(res)
  }
}
