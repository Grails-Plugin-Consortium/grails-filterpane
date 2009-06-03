package com.zeddware.grails.plugins.filterpane

import grails.test.*

class FilterTabLibTests extends TagLibUnitTestCase {

    FilterTabLibTests() {
        super(FilterTagLib.class)
    }

    protected void setUp() {
        super.setUp()
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testIncludes() {
        def includes = tagLib.includes()
        def expected = """<link rel="stylesheet" type="text/css" href="/css/fp.css" />
<script type="text/javascript" src="/js/filter.js"></script>"""
        assertEquals expected, includes
    }
}
