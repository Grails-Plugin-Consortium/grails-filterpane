package org.grails.plugin.filterpane.nested

import org.springframework.dao.DataIntegrityViolationException
import org.grails.plugin.filterpane.Book
import org.grails.plugin.filterpane.FilterPaneUtils

class RobotController {

    def filterPaneService

    // the delete, save and update actions only accept POST requests
    //def static allowedMethods = [delete:'POST', save:'POST', update:'POST']

    def filter = {
        if(!params.max) params.max = 10
        render( view:'list', model:[ robotList: filterPaneService.filter( params, Robot ), robotCount: filterPaneService.count( params, Robot ), filterParams: FilterPaneUtils.extractFilterParams(params), params:params ] )
    }
}
