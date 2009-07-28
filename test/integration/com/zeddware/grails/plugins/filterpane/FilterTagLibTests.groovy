package com.zeddware.grails.plugins.filterpane

import org.apache.log4j.Logger
import org.springframework.web.context.request.RequestContextHolder

class FilterTagLibTests extends grails.test.GroovyPagesTestCase {

    protected void setUp() {
        super.setUp()
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testIncludes() {
        def expected = """<link rel="stylesheet" type="text/css" href="/css/fp.css" />
<script type="text/javascript" src="/js/filter.js"></script>"""
        def template = '<filterpane:includes />'
        assertOutputEquals(expected, template,[:], {def s = it.toString(); println "out is '${s}'"; return s })
    }

    void testIsFilteredNoOutput() {
        def template = '<filterpane:isFiltered>testing</filterpane:isFiltered>'
        assertOutputEquals('', template, [:], {def s = it.toString(); println "out is '${s}'"; return s })
    }

    void testIsFilteredWithOutput() {
        def expected = 'testing'
        def template = '<filterpane:isFiltered>testing</filterpane:isFiltered>'
        RequestContextHolder.currentRequestAttributes().params['filter.op.test'] = 'Like'
        assertOutputEquals(expected, template, ['filter.op.test':'Like'], {def s = it.toString(); println "out is '${s}'"; return s })
    }

    void testIsNotFilteredNoOutput() {
        def expected = ''
        def template = "<filterpane:isNotFiltered>testing</filterpane:isNotFiltered>"
        RequestContextHolder.currentRequestAttributes().params['filter.op.test'] = 'Like'
        assertOutputEquals(expected, template, [:], {def s = it.toString(); println "out is '${s}'"; return s })
    }

    void testIsNotFilteredWithOutput() {
        def expected = 'testing'
        def template = "<filterpane:isNotFiltered>testing</filterpane:isNotFiltered>"
        assertOutputEquals(expected, template, [:], {def s = it.toString(); println "out is '${s}'"; return s })
    }
}
