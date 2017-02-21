package com.atomist.rug.runtime.js

import com.atomist.param._
import com.atomist.rug.InvalidHandlerResultException
import com.atomist.rug.runtime.js.interop.JavaScriptHandlerContext
import com.atomist.rug.runtime.plans.MappedParameterSupport
import com.atomist.rug.runtime.{CommandContext, CommandHandler, ParameterizedRug}
import com.atomist.rug.spi.Handlers.Plan
import jdk.nashorn.api.scripting.ScriptObjectMirror

import scala.collection.JavaConverters._

/**
  * Finds JavaScriptCommandHandlers in Nashorn vars
  */
class JavaScriptCommandHandlerFinder(ctx: JavaScriptHandlerContext)
  extends BaseJavaScriptHandlerFinder[JavaScriptCommandHandler] (ctx) {

  override def kind = "command-handler"

  override def extractHandler(jsc: JavaScriptContext, handler: ScriptObjectMirror, ctx: JavaScriptHandlerContext): Option[JavaScriptCommandHandler] = {
    Some(new JavaScriptCommandHandler(jsc, handler, name(handler), description(handler), parameters(handler), mappedParameters(handler), tags(handler), intent(handler)))
  }

  /**
    * Extract intent from a var
    *
    * @param someVar
    * @return
    */
  protected def intent(someVar: ScriptObjectMirror): Seq[String] = {
    someVar.getMember("__intent") match {
      case som: ScriptObjectMirror =>
        val stringValues = som.values().asScala collect {
          case s: String => s
        }
        stringValues.toSeq
    }
  }

  /**
    * Look for mapped parameters (i.e. defined by the bot).
    *
    * See MappedParameters in Handlers.ts for examples.
    *
    * @return
    */
  protected def mappedParameters(someVar: ScriptObjectMirror): Seq[MappedParameter] = {
    getMember(someVar, Seq("__mappedParameters")) match {
      case Some(ps: ScriptObjectMirror) if !ps.isEmpty =>
        ps.asScala.collect {
          case (_, details: ScriptObjectMirror) =>
            val localKey = details.getMember("localKey").asInstanceOf[String]
            val foreignKey = details.getMember("foreignKey").asInstanceOf[String]
            MappedParameter(localKey,foreignKey)
        }.toSeq
      case _ => Seq()
    }
  }
}

/**
  * Runs a CommandHandler in Nashorn
  *
  * @param handler
  * @param name
  * @param description
  * @param parameters
  * @param tags
  * @param intent
  */
class JavaScriptCommandHandler(jsc: JavaScriptContext,
                               handler: ScriptObjectMirror,
                               override val name: String,
                               override val description: String,
                               parameters: Seq[Parameter],
                               override val mappedParameters: Seq[MappedParameter],
                               override val tags: Seq[Tag],
                               override val intent: Seq[String] = Seq())
  extends CommandHandler
  with MappedParameterSupport
  with JavaScriptUtils {

  addParameters(parameters)

  /**
    * We expect all mapped parameters to also be passed in with the normal params
    * @param ctx
    * @param params
    * @return
    */
  override def handle(ctx: CommandContext, params: ParameterValues): Option[Plan] = {
    val validated = addDefaultParameterValues(params)
    validateParameters(validated)
    invokeMemberFunction(jsc, handler, "handle", ctx, validated) match {
      case plan: ScriptObjectMirror => ConstructPlan(plan)
      case other => throw new InvalidHandlerResultException(s"$name CommandHandler did not return a recognized response ($other) when invoked with ${params.toString()}")
    }
  }
}
