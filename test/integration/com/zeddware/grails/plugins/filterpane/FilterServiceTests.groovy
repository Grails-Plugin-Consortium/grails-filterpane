package com.zeddware.grails.plugins.filterpane

import grails.test.*
import grails.test.mixin.TestFor

import org.grails.plugin.filterpane.FilterPaneService
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Test


@TestFor(FilterPaneService)
//@Mock([Book]) Book class is missing.
@Ignore
class FilterServiceTests {

    def filterPaneService
    def params

    @Before
    public void setUp() {
        defineBeans {
            filterPaneService(FilterPaneService)
          }
        filterPaneService = applicationContext.getBean("filterPaneService")
        assert filterPaneService 
        params = ['filter':[ op:[ title:'ILike' ], title:'think' ]]

    }

    @After
    public void tearDown() {
        params.clear()
    }

    @Test
    void testFilterBooksNoCount() {
        def results = filterPaneService.filter(params, Book.class)

        assert 2 == results?.size()
    }

    @Test
    void testFilterCount() {
        assert  2 == filterPaneService.count(params, Book.class)
    }

    @Test
    void testIdFilter() {
        params.filter.op.clear()
        params.filter.op.id = 'Equal'
        params.filter.id = 4
        assert 4 == filterPaneService.filter(params, Book.class)[0].id
    }

    @Test
    void testAssociatedCollectionFilter() {
        params = ['filter':[ op:[ 'books.title':'ILike' ], 'books.title':'think' ]]
        params.filter.op.books = [title:'ILike']
        params.filter.books = [title:'think']
        assert 2 == filterPaneService.count(params, Author.class)
    }

    @Test
    void testAssociationFilter() {
        params = ['filter':[ op:[ 'author.lastName':'Equal' ], 'author.lastName':'Lewis' ]]
        params.filter.op.author = [lastName:'Equal']
        params.filter.author = [lastName:'Lewis']
        assert 'Clive' == filterPaneService.filter(params, Book.class)[0].author.firstName
    }

    @Test
	void testEmptyParams() {
		params = [:]
		int count = Book.count()
		assert count == filterPaneService.filter(params, Book.class).size()
	}
}
