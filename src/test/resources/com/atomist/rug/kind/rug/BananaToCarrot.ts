
// Generated by Rug to TypeScript transpiler.
// To take ownership of this file, simply delete the .rug file
    
import { EditProject } from '@atomist/rug/operations/ProjectEditor'
import { PathExpressionEngine } from '@atomist/rug/tree/PathExpression'
import { Editor, Tags, Parameter } from '@atomist/rug/operations/Decorators'
import { Pattern } from '@atomist/rug/operations/RugOperation'
import { File, Project } from '@atomist/rug/model/Core'

/**
    BananaToCarrot
    Paint it orange and make it crunchy
 */

@Editor("BananaToCarrot", "Paint it orange and make it crunchy")
@Tags("vegetable", "fruit")
class BananaToCarrot implements EditProject {

    @Parameter({
        displayName: "Banana Peel",
        description: "peel of the banana",
        pattern: Pattern.any,
        validInput: "slippery but protective",
        minLength: 1,
        maxLength: 100
    })
    peel: string;
    
    edit(project: Project) {
    
        let eng: PathExpressionEngine = project.context().pathExpressionEngine();
        
        let p = project
                if (true) {
                        eng.with<File>(p, '//File()', f => {
                            if (true) {
                                f.replace("banana", "carrots")
                            }
                        })
                }
    
    }

}
export let editor_bananaToCarrot = new BananaToCarrot();