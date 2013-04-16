package org.grails.plugin.filterpane

import org.grails.plugin.filterpane.FilterPaneUtils
import org.grails.plugin.filterpane.Author
import org.grails.plugin.filterpane.Book

class BookController {
	
	def filterPaneService

	// the delete, save and update actions only accept POST requests
	//def static allowedMethods = [delete:'POST', save:'POST', update:'POST']

	def filter = {
		if(!params.max) params.max = 10
		render( view:'list', model:[ bookList: filterPaneService.filter( params, Book ), bookCount: filterPaneService.count( params, Book ), filterParams: FilterPaneUtils.extractFilterParams(params), params:params ] )
	}
}