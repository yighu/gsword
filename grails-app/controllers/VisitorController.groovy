class VisitorController {
    
    def index = { redirect(action:list,params:params) }

    // the delete, save and update actions only accept POST requests
    static def allowedMethods = [delete:'POST', save:'POST', update:'POST']

    def list = {
        if(!params.max) params.max = 10
        [ visitorList: Visitor.list( params ) ]
    }

    def show = {
        def visitor = Visitor.get( params.id )               

        if(!visitor) {
            flash.message = "visitor.not.found"
            flash.args = [params.id]
            flash.defaultMessage = "Visitor not found with id ${params.id}"
            redirect(action:list)
        }
        else { return [ visitor : visitor ] }
    }

    def delete = {
        def visitor = Visitor.get( params.id )
        if(visitor) {
            visitor.delete()
            flash.message = "visitor.deleted"
            flash.args = [params.id]
            flash.defaultMessage = "Visitor ${params.id} deleted"
            redirect(action:list)
        }
        else {
            flash.message = "visitor.not.found"
            flash.args = [params.id]
            flash.defaultMessage = "Visitor not found with id ${params.id}"
            redirect(action:list)
        }
    }

    def edit = {
        def visitor = Visitor.get( params.id )

        if(!visitor) {
            flash.message = "visitor.not.found"
            flash.args = [params.id]
            flash.defaultMessage = "Visitor not found with id ${params.id}"
            redirect(action:list)
        }
        else {
            return [ visitor : visitor ]
        }
    }

    def update = {
        def visitor = Visitor.get( params.id )
        if(visitor) {
            visitor.properties = params
            if(!visitor.hasErrors() && visitor.save()) {
                flash.message = "visitor.updated"
                flash.args = [params.id]
                flash.defaultMessage = "Visitor ${params.id} updated"
                redirect(action:show,id:visitor.id)
            }
            else {
                render(view:'edit',model:[visitor:visitor])
            }
        }
        else {
            flash.message = "visitor.not.found"
            flash.args = [params.id]
            flash.defaultMessage = "Visitor not found with id ${params.id}"
            redirect(action:edit,id:params.id)
        }
    }

    def create = {
        def visitor = new Visitor()
        visitor.properties = params
        return ['visitor':visitor]
    }

    def save = {
        def visitor = new Visitor(params)
        if(!visitor.hasErrors() && visitor.save()) {
            flash.message = "visitor.created"
            flash.args = ["${visitor.id}"]
            flash.defaultMessage = "Visitor ${visitor.id} created"
            redirect(action:show,id:visitor.id)
        }
        else {
            render(view:'create',model:[visitor:visitor])
        }
    }
}