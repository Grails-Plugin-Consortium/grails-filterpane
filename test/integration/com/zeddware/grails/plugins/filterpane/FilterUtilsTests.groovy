package com.zeddware.grails.plugins.filterpane

import grails.test.*
import grails.test.mixin.TestFor

import org.grails.plugin.filterpane.FilterPaneUtils
import org.junit.After
import org.junit.Before
import org.junit.Test

class FilterUtilsTests  {

	def params
	Date testDate

    @Before
    public void setUp() {
		testDate = java.sql.Timestamp.valueOf('2010-07-26 20:38:15.000')

		params = [testDate:testDate,
			testDate_year:'2010',
			testDate_month:'7',
			testDate_day:'26',
			testDate_hour:'20',
			testDate_minute:'38',
			testDate_second:'15'
		]
    }

    @After
    public void tearDown() {
    }

    // parseDateFromDatePickerParams tests
    @Test
	void testParseFromDate() {
		assert FilterPaneUtils.parseDateFromDatePickerParams('testDate', params)
	}

    @Test
	void testParseFromToString() {
		params.testDate = params.testDate.toString()
		assert FilterPaneUtils.parseDateFromDatePickerParams('testDate', params)
	}

    @Test
	void testParseFromStruct() {
		params.testDate = 'struct'
		assert FilterPaneUtils.parseDateFromDatePickerParams('testDate', params)
	}
}
