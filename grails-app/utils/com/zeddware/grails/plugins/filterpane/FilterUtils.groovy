package com.zeddware.grails.plugins.filterpane

import org.codehaus.groovy.grails.commons.DefaultGrailsDomainClass
import org.apache.log4j.Logger

class FilterUtils {
    private static Logger log = Logger.getLogger(FilterUtils.class)
    
    static def makeCamelCasePretty(def string) {
        if (string == null)
        return null;
        char[] s = string.toString().toCharArray()
        def sb = new StringBuffer()
        boolean lastCharDot = false

        s.eachWithIndex {ch, i ->
            if (Character.isUpperCase(ch)) {
                if (Character.isLowerCase(s[i - 1])
                    || (s.length > i + 1 && Character.isLowerCase(s[i + 1]))) {
                    sb << ' '
                }
            } else if (Character.isLowerCase(ch) && lastCharDot) {
                ch = Character.toUpperCase(ch)
            }
            lastCharDot = false

            if (ch == 46) { // char comparisons in Groovy are a pain!
                sb << "'s "
                lastCharDot = true
            } else {
                sb << ch
            }

        }

        sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)))
        return sb.toString()
    }

    static def getOperatorMapKey(def opType) {
        if (opType.getSimpleName().equalsIgnoreCase("boolean")) {
            return 'boolean'
        } else if (opType == Integer || opType == int || opType == Long || opType == long
            || opType == Double || opType == double || opType == Float || opType == float
            || opType == Short || opType == short || opType == BigDecimal || opType == BigInteger) {
            return 'numeric'
        } else if (java.util.Date.isAssignableFrom(opType)) {
            return 'date'
        }
        return 'text'
    }

    static def getNestedMetaProperty(MetaClass mc, String propertyName) {
        def nest = propertyName.tokenize('.')
        MetaClass currMc = mc
        def mp = null
        nest.each() {egg ->
            mp = currMc.getMetaProperty(egg)
            if (mp != null) {
                currMc = mp.type.getMetaClass()
            }
        }
        return mp
    }

    static java.util.Date parseDateFromDatePickerParams(def paramProperty, def params) {
        try {
            def year = params["${paramProperty}_year"]
            def month = params["${paramProperty}_month"]
            def day = params["${paramProperty}_day"]
            def hour = params["${paramProperty}_hour"]
            def minute = params["${paramProperty}_minute"]

            if (log.isDebugEnabled()) {
                log.debug("Parsing date from params: ${year} ${month} ${day} ${hour} ${minute}")
            }

            String format = ''
            String value = ''
            if (year != null) {
                format = "yyyy"
                value = year
            }
            if (month != null) {
                format += 'MM'
                value += zeroPad(month)
            }
            if (day != null) {
                format += 'dd'
                value += zeroPad(day)
            }
            if (hour != null) {
                format += 'HH'
                value += zeroPad(hour)
            } else if (paramProperty.endsWith('To')) {
                format += 'HH'
                value += '23'
            }

            if (minute != null) {
                format += 'mm'
                value += zeroPad(minute)
            } else if (paramProperty.endsWith('To')) {
                format += 'mm:ss.SSS'
                value += '59:59.999'
            }

            if (log.isDebugEnabled()) log.debug("Parsing ${value} with format ${format}")
            return new java.text.SimpleDateFormat(format).parse(value)
        } catch (Exception ex) {
            log.error("${ex.getClass().simpleName} parsing date for property ${paramProperty}: ${ex.message}")
            return null
        }
    }

    private static def zeroPad(def val) {
        try {
            if (val != null) {
                int i = val as int
                if (i < 10) {
                    return "0${i}"
                } else {
                    return val
                }
            }
        } catch (Exception ex) {
            return val
        }
    }

    static def extractFilterParams(params) {
        def ret = [:]
        params.each {entry ->
            if (entry.key.startsWith("filter.") || entry.key.equals("filterProperties") || entry.key.equals("filterBean")) {
                ret[entry.key] = entry.value
            }
        }
        return ret
    }

    static boolean isFilterApplied(params) {
        boolean isApplied = false
        params.each { key, value ->
            if (key.startsWith('filter.op') && value != null && ! ''.equals(value)) {
                isApplied = true
                return
            }
        }
        return isApplied
    }

    static def resolveDomainClass(def grailsApplication, def bean) {
        if (log.isDebugEnabled()) log.debug("resolveDomainClass: bean is ${bean?.class}")
        if(bean instanceof DefaultGrailsDomainClass) {
            return bean
        }
        String beanName = null
        if (bean instanceof Class) {
            beanName = bean.simpleName
        } else if(bean instanceof String) {
            beanName = bean
        }
        if (beanName) {
            return grailsApplication.getDomainClass(beanName)
        }
        return null
    }
}