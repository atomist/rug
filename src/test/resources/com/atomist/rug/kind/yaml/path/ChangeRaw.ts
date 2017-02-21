import {Project,File} from '@atomist/rug/model/Core'
import {ProjectEditor} from '@atomist/rug/operations/ProjectEditor'
import {PathExpression,TextTreeNode,TypeProvider} from '@atomist/rug/tree/PathExpression'
import {PathExpressionEngine} from '@atomist/rug/tree/PathExpression'
import * as yaml from '@atomist/rug/ast/yaml/Types'
import {YamlPathExpressionEngine} from '@atomist/rug/ast/yaml/YamlPathExpressionEngine'

class ChangeRaw implements ProjectEditor {
    name: string = "ChangeRaw"
    description: string = "Adds import"

    edit(project: Project) {
      let eng: PathExpressionEngine =
        new YamlPathExpressionEngine(project.context().pathExpressionEngine())

      let findDependencies = `/*[@name='x.yml']/YamlFile()/group/value`

      eng.with<yaml.YamlString>(project, findDependencies, yamlValue => {
        //console.log(`Raw value is [${yamlValue.value()}]`)
        if (yamlValue.value() != "queen")
            throw new Error(`[${yamlValue.value()}] not 'queen'"`)
        if (yamlValue.text() != "queen")
            throw new Error(`[${yamlValue.text()}] not 'queen'`)
        yamlValue.updateText("Jefferson Airplane")
      })
  }

}

export let editor = new ChangeRaw()