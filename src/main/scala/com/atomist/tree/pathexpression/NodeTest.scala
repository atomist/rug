package com.atomist.tree.pathexpression

import com.atomist.rug.spi.TypeRegistry
import com.atomist.tree.content.text._
import com.atomist.tree.pathexpression.ExecutionResult.ExecutionResult
import com.atomist.tree.{ContainerTreeNode, TreeNode}
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeInfo.{As, Id}

/**
  * One of the three core elements of a LocationStep. Inspired by XPath NodeTest.
  */
@JsonTypeInfo(include = As.WRAPPER_OBJECT, use = Id.NAME)
trait NodeTest {

  /**
    * Find nodes from the given node, observing the given AxisSpecifier
    *
    * @param tn   node to drill down from
    * @param axis AxisSpecifier indicating the kind of navigation
    * @return Resulting nodes
    */
  def follow(tn: TreeNode, axis: AxisSpecifier, ee: ExpressionEngine, typeRegistry: TypeRegistry): ExecutionResult

}

/**
  * Convenience superclass that uses a predicate to handle sourced nodes
  *
  * @param predicate predicate used to filter nodes
  */
abstract class PredicatedNodeTest(name: String, predicate: Predicate) extends NodeTest {

  final override def follow(tn: TreeNode, axis: AxisSpecifier, ee: ExpressionEngine, typeRegistry: TypeRegistry): ExecutionResult =
    sourceNodes(tn, axis, typeRegistry) match {
      case Right(nodes) =>
        ExecutionResult(nodes.filter(tn => predicate.evaluate(tn, nodes, ee, typeRegistry, None)))
      case failure => failure
    }

  /**
    * Subclasses can override this to provide a more efficient implementation.
    * This one works but can be expensive.
    */
  protected def sourceNodes(tn: TreeNode, axis: AxisSpecifier, typeRegistry: TypeRegistry): ExecutionResult = axis match {
    case Self => ExecutionResult(List(tn))
    case Child =>
      val kids = tn.childNodes.filter(_.significance != TreeNode.Noise).toList
      ExecutionResult(kids)
    case Descendant =>
      val kids = Descendant.allDescendants(tn).filter(_.significance != TreeNode.Noise).toList
      ExecutionResult(kids)
    case NavigationAxis(propertyName) =>
      val nodes = tn.childrenNamed(propertyName)
      ExecutionResult(nodes)
  }
}

/**
  * Return all nodes on the given axis
  */
object All extends PredicatedNodeTest("All", TruePredicate) {
  override def toString = "*"
}
