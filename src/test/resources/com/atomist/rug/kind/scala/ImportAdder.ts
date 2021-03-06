import {Project,File} from '@atomist/rug/model/Core'
import {Editor} from '@atomist/rug/operations/Decorators'
import {PathExpression,TextTreeNode,TypeProvider} from '@atomist/rug/tree/PathExpression'
import {PathExpressionEngine} from '@atomist/rug/tree/PathExpression'
import {Match} from '@atomist/rug/tree/PathExpression'
import {ScalaPathExpressionEngine} from '@atomist/rug/ast/scala/ScalaPathExpressionEngine'
import * as scala from '@atomist/rug/ast/scala/Types'

/**
 * Uses Scala mixin add imports
 */

@Editor("Adds import")
class ImportAdder  {

    edit(project: Project) {
      let eng: PathExpressionEngine =
        new ScalaPathExpressionEngine(project.context.pathExpressionEngine)

      let findExistingScalaTestImport = `/src/Directory()/scala//ScalaFile()`   

      eng.with<scala.Source>(project, findExistingScalaTestImport, scalaFile => {
        scalaFile.addImport("org.scalatest.DiagrammedAssertions._")
        //if (scalaFile.value().indexOf("DiagrammedAssertions") < 0)
        //  throw new Error(`Content not right when i asked again: [${scalaFile.value()}]`)
        // else
        //   console.log(`Content right when i asked again: [${scalaFile.value()}]`)
      })
  }

}

export let editor = new ImportAdder()
