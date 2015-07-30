package org.grails.plugins.filterpane

class BookController {

    FilterPaneService filterPaneService

    def filter() {
        if (!params.max) params.max = 10
        render view: 'list',
               model: [bookList: filterPaneService.filter(params, Book),
                       bookCount: filterPaneService.count(params, Book),
                       filterParams: FilterPaneUtils.extractFilterParams(params), params:params]
    }
}
