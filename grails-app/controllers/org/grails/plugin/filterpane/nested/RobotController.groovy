package org.grails.plugin.filterpane.nested

import org.grails.plugin.filterpane.FilterPaneUtils

class RobotController {

    def filterPaneService

    def filter() {
        if (!params.max) params.max = 10
        render view:'list',
               model: [robotList: filterPaneService.filter(params, Robot),
                       robotCount: filterPaneService.count(params, Robot),
                       filterParams: FilterPaneUtils.extractFilterParams(params), params:params]
    }
}
