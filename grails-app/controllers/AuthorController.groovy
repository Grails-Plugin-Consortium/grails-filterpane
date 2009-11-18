import com.zeddware.grails.plugins.filterpane.*        
class AuthorController {

    def filterService
    
    def index = { redirect(action:list,params:params) }

    // the delete, save and update actions only accept POST requests
    def static allowedMethods = [delete:'POST', save:'POST', update:'POST']

    def list = {
        if(!params.max) params.max = 10
        [ authorList: Author.list( params ) ]
    }

    def filter = {
        if(!params.max) params.max = 10
        render( view:'list', model:[ authorList: filterService.filter( params, Author ), authorCount: filterService.count( params, Author ), filterParams: com.zeddware.grails.plugins.filterpane.FilterUtils.extractFilterParams(params), params:params ] )
    }

	def filterCustomForm = {
		log.debug('Doing custom form filter.')
		if(!params.max) params.max = 10
        render( view:'list', model:[ authorList: filterService.filter( params, Author ), authorCount: filterService.count( params, Author ), filterParams: com.zeddware.grails.plugins.filterpane.FilterUtils.extractFilterParams(params), params:params ] )
	}

    def show = {
        def author = Author.get( params.id )

        if(!author) {
            flash.message = "Author not found with id ${params.id}"
            redirect(action:list)
        }
        else { return [ author : author ] }
    }

    def delete = {
        def author = Author.get( params.id )
        if(author) {
            author.delete()
            flash.message = "Author ${params.id} deleted"
            redirect(action:list)
        }
        else {
            flash.message = "Author not found with id ${params.id}"
            redirect(action:list)
        }
    }

    def edit = {
        def author = Author.get( params.id )

        if(!author) {
            flash.message = "Author not found with id ${params.id}"
            redirect(action:list)
        }
        else {
            return [ author : author ]
        }
    }

    def update = {
        def author = Author.get( params.id )
        if(author) {
            author.properties = params
            if(!author.hasErrors() && author.save()) {
                flash.message = "Author ${params.id} updated"
                redirect(action:show,id:author.id)
            }
            else {
                render(view:'edit',model:[author:author])
            }
        }
        else {
            flash.message = "Author not found with id ${params.id}"
            redirect(action:edit,id:params.id)
        }
    }

    def create = {
        def author = new Author()
        author.properties = params
        return ['author':author]
    }

    def save = {
        def author = new Author(params)
        if(!author.hasErrors() && author.save()) {
            flash.message = "Author ${author.id} created"
            redirect(action:show,id:author.id)
        }
        else {
            render(view:'create',model:[author:author])
        }
    }
}