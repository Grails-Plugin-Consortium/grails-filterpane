package com.demo

import org.grails.plugins.filterpane.FilterPaneService
import org.grails.plugins.filterpane.FilterPaneUtils

class BookController {

    FilterPaneService filterPaneService

    def filter() {
        if (!params.max) params.max = 10
        render view: 'list',
               model: [bookList    : filterPaneService.filter(params, Book),
                       bookCount   : filterPaneService.count(params, Book),
                       filterParams: FilterPaneUtils.extractFilterParams(params), params:params]
    }
}
