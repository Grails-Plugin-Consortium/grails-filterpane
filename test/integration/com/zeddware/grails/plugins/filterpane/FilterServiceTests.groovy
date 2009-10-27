package com.zeddware.grails.plugins.filterpane

import grails.test.*

class FilterServiceTests extends GrailsUnitTestCase {

    def filterService
    def params

    protected void setUp() {
        super.setUp()
        params = ['filter':[ op:[ title:'ILike' ], title:'think' ]]

    }

    protected void tearDown() {
        super.tearDown()
        params.clear()
    }

    void testFilterBooksNoCount() {
        def results = filterService.filter(params, Book.class, false)

        assertEquals('filter no count failed. ', 2, results?.size())
    }

    void testFilterCount() {
        assertEquals('filter count failed. ', 2, filterService.count(params, Book.class))
    }

    void testIdFilter() {
        params.filter.op.clear()
        params.filter.op.id = 'Equal'
        params.filter.id = 4
        assertEquals('filter id failed. ', 4, filterService.filter(params, Book.class, false)[0].id)
    }

    void testAssociatedCollectionFilter() {
        params = ['filter':[ op:[ 'books.title':'ILike' ], 'books.title':'think' ]]
        params.filter.op.books = [title:'ILike']
        params.filter.books = [title:'think']
        assertEquals('collection filter failed.', 2, filterService.count(params, Author.class))
    }

    void testAssociationFilter() {
        params = ['filter':[ op:[ 'author.lastName':'Equal' ], 'author.lastName':'Lewis' ]]
        params.filter.op.author = [lastName:'Equal']
        params.filter.author = [lastName:'Lewis']
        assertEquals('association filter failed.', 'Clive', filterService.filter(params, Book.class)[0].author.firstName)
    }

	void testEmptyParams() {
		params = [:]
		int count = Book.count()
		assertEquals('empty params test failed.', count, filterService.filter(params, Book.class).size())
	}
}
