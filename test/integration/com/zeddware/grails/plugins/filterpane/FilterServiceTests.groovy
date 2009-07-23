package com.zeddware.grails.plugins.filterpane

import grails.test.*
import grails.

class FilterServiceTests extends GrailsUnitTestCase {

    def filterService

    protected void setUp() {
        super.setUp()

    }

    protected void tearDown() {
        super.tearDown()
    }

    /**
     *
     */
    void testFilterBooksNoCount() {
        def params = ['filter.op.title':'ILike', 'filter.title':'think']

        def results = filterService.filter(params, Book.class, false)

        assertTrue('filter no count failed. ', 1, results?.size())
    }
}
