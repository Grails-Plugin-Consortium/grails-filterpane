package com.zeddware.grails.plugins.filterpane

import grails.test.mixin.TestFor

import org.codehaus.groovy.grails.web.taglib.exceptions.GrailsTagException;
import org.grails.plugin.filterpane.FilterPaneTagLib
import org.junit.Test
import org.springframework.web.context.request.RequestContextHolder

@TestFor(FilterPaneTagLib)
class FilterTagLibTests {


    @Test    
    void testIncludes() {
        def expected = """<link rel="stylesheet" type="text/css" href="null/css/fp.css" />
<script type="text/javascript" src="null/js/fp.js"></script>"""
        def template = '<filterpane:includes />'
        assertOutputEquals(expected, template,[:], {def s = it.toString(); println "out is '${s}'"; return s })
    }

    @Test    
    void testIsFilteredNoOutput() {
        def template = '<filterpane:isFiltered>testing</filterpane:isFiltered>'
        assertOutputEquals('', template, [:], {def s = it.toString(); println "out is '${s}'"; return s })
    }

    @Test    
    void testIsFilteredWithOutput() {
        def expected = 'testing'
        def template = '<filterpane:isFiltered>testing</filterpane:isFiltered>'
        RequestContextHolder.currentRequestAttributes().params['filter.op.test'] = 'Like'
        assertOutputEquals(expected, template, ['filter.op.test':'Like'], {def s = it.toString(); println "out is '${s}'"; return s })
    }

    @Test    
    void testIsNotFilteredNoOutput() {
        def expected = ''
        def template = "<filterpane:isNotFiltered>testing</filterpane:isNotFiltered>"
        RequestContextHolder.currentRequestAttributes().params['filter.op.test'] = 'Like'
        assertOutputEquals(expected, template, [:], {def s = it.toString(); println "out is '${s}'"; return s })
    }

    @Test    
    void testIsNotFilteredWithOutput() {
        def expected = 'testing'
        def template = "<filterpane:isNotFiltered>testing</filterpane:isNotFiltered>"
        assertOutputEquals(expected, template, [:], {def s = it.toString(); println "out is '${s}'"; return s })
    }

    @Test(expected=GrailsTagException.class)    
    void testFilterLink_noValues(){
        def expected = '<a href="">testing</a>'
        def template = "<filterpane:filterLink >testing</filterpane:filterLink>"
        assertOutputEquals(expected, template, [:], {def s = it.toString(); println "out is '${s}'"; return s })
    }

    @Test()
    void testFilterLink(){
        def template = "<filterpane:filterLink controller='book' values='[author:\"Mr Test\"]'>testing</filterpane:filterLink>"
        def expected = '<a href="/book/filter?sort=&amp;order=&amp;filter.op.author=Equal&amp;filter.author=Mr+Test">testing</a>'
        assertOutputEquals(expected, template, [:], {def s = it.toString(); println "out is '${s}'"; return s })
    }

    @Test()
    void testFilterLink_IsNull(){
        def template = "<filterpane:filterLink controller='book' values='[author:[op:\"IsNull\"]]'>testing</filterpane:filterLink>"
        def expected = '<a href="/book/filter?sort=&amp;order=&amp;filter.op.author=IsNull&amp;filter.author=0">testing</a>'
        assertOutputEquals(expected, template, [:], {def s = it.toString(); println "out is '${s}'"; return s })
    }

    @Test()
    void testFilterLink_Between(){
        def template = "<filterpane:filterLink controller='book' values='[dateCreated:[op:\"Between\", value:\"2013-01-01\", to:\"2013-01-31\" ]]'>testing</filterpane:filterLink>"
        def expected = '<a href="/book/filter?sort=&amp;order=&amp;filter.op.dateCreated=Between&amp;filter.dateCreated=2013-01-01&amp;filter.dateCreatedTo=2013-01-31">testing</a>'
        assertOutputEquals(expected, template, [:], {def s = it.toString(); println "out is '${s}'"; return s })
    }

}
