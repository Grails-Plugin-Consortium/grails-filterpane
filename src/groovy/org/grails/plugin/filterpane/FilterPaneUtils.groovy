package org.grails.plugin.filterpane

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.codehaus.groovy.grails.commons.GrailsDomainClass

import java.text.SimpleDateFormat

/**
 * @author skrenek
 */
class FilterPaneUtils {

    private static SimpleDateFormat df = new SimpleDateFormat('EEE MMM dd hh:mm:ss zzz yyyy')
    private static final Log log = LogFactory.getLog(this)

    static Date parseDateFromDatePickerParams(paramProperty, params) {
        try {
            if(params[paramProperty] instanceof Date) {

                return (Date) params[paramProperty]

            } else if(params[paramProperty] instanceof String) {
                try {
                    return df.parse(params[paramProperty])
                } catch(Exception ex) {
                    /* Do nothing. */
                    log.debug("Parse exception for ${params[paramProperty]}: ${ex.message}")
                }
            }

            def year = params["${paramProperty}_year"]
            def month = params["${paramProperty}_month"]
            def day = params["${paramProperty}_day"]
            def hour = params["${paramProperty}_hour"]
            def minute = params["${paramProperty}_minute"]
            boolean paramExists = (minute || hour || day || month || year)

//                log.debug("Parsing date from params: ${year} ${month} ${day} ${hour} ${minute}")

            String format = ''
            String value = ''
            if(year != null) {
                format = "yyyy"
                value = year
            }
            if(month != null) {
                format += 'MM'
                value += zeroPad(month)
            }
            if(day != null) {
                format += 'dd'
                value += zeroPad(day)
            }
            if(hour != null) {
                format += 'HH'
                value += zeroPad(hour)
            } else if(paramProperty.endsWith('To')) {
                format += 'HH'
                value += '23'
            }

            if(minute != null) {
                format += 'mm'
                value += zeroPad(minute)
            } else if(paramProperty.endsWith('To')) {
                format += 'mm:ss.SSS'
                value += '59:59.999'
            }

            if(value == '' || !paramExists) { // Don't even bother parsing.  Just return null if blank.
                return null
            }

            log.debug("Parsing ${value} with format ${format}")
            return Date.parse(format, value)// new java.text.SimpleDateFormat(format).parse(value)
        } catch(Exception ex) {
            log.error("${ex.getClass().simpleName} parsing date for property ${paramProperty}: ${ex.message}")
            return null
        }
    }

    static Date getBeginningOfDay(aDate) {
        Date beginningOfDay = null
        if(aDate && Date.isAssignableFrom(aDate.class)) {
            Date date = (Date) aDate;
            Calendar calendar = Calendar.instance.with {
                time = date;
                set(Calendar.HOUR_OF_DAY, 0);
                set(Calendar.MINUTE, 0);
                set(Calendar.SECOND, 0);
                set(Calendar.MILLISECOND, 0);
                it
            }
            beginningOfDay = calendar.time
        }
        beginningOfDay
    }

    static Date getEndOfDay(aDate) {
        Date endOfDay = null
        if(aDate && Date.isAssignableFrom(aDate.class)) {
            Date date = (Date) aDate
            Calendar calendar = Calendar.instance.with {
                time = date
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
                it
            }
            endOfDay = calendar.time
        }
        endOfDay
    }

    private static zeroPad(val) {
        try {
            if(val != null) {
                int i = val as int
                return (i < 10) ? "0${i}" : val
            }
        } catch(Exception ex) {
            log.error ex
            return val
        }
    }

    static extractFilterParams(params) {
        def ret = [:]
        params.each { entry ->
            if(entry?.key?.startsWith("filter.") || entry?.key?.equals("filterProperties") || entry?.key?.equals("filterBean")) {
                ret[entry.key] = entry.value
            }
        }
        ret
    }

    static extractFilterParams(params, boolean datesToStruct) {
        def ret = [:]
        params.each { entry ->
            if(entry.key.startsWith("filter.") || entry.key.equals("filterProperties") || entry.key.equals("filterBean")) {
                def val = entry.value
                if(datesToStruct && val instanceof Date) val = 'struct'
                ret[entry.key] = val
            }
        }
        ret
    }

    static boolean isFilterApplied(params) {
        boolean isApplied = false
        params.each { key, value ->
            if(key.startsWith('filter.op') && value != null && !''.equals(value)) {
                isApplied = true
                return
            }
        }
        isApplied
    }

    static resolveDomainClass(grailsApplication, bean) {
        String beanName = null
        def result = null

        log.debug("resolveDomainClass: bean is ${bean?.class}")
        if(bean instanceof GrailsDomainClass) {
            return bean
        }

        if(bean instanceof Class) {
            beanName = bean.name
        } else if(bean instanceof String) {
            beanName = bean
        }

        if(beanName) {
            result = grailsApplication.getDomainClass(beanName)
            if(!result) {
                result = grailsApplication.domainClasses.find { it.clazz.simpleName == beanName }
            }
        }
        result
    }

    static resolveDomainProperty(grailsApplication, domainClass, property) {

        if("id".equals(property) || "identifier".equals(property)) {
            return domainClass.identifier
        }

        def thisDomainProp = domainClass.persistentProperties.find {
            it.name == property
        }

        thisDomainProp
    }

    static getOperatorMapKey(opType) {
        def type = 'text'
        if(opType.getSimpleName().equalsIgnoreCase("boolean")) {
            type = 'boolean'
        } else if( opType == Integer || opType == int || opType == Long || opType == long
                || opType == Double || opType == double || opType == Float || opType == float
                || opType == Short || opType == short || opType == BigDecimal || opType == BigInteger) {
            type = 'numeric'
        } else if(java.util.Date.isAssignableFrom(opType)) {
            type = 'date'
        } else if(opType.isEnum()) {
            type = 'enum'
        } else if(opType.simpleName.equalsIgnoreCase("currency")) {
            type = 'currency'
        }
        type
    }
}
