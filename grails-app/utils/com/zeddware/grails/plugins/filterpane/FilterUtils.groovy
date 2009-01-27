package com.zeddware.grails.plugins.filterpane

class FilterUtils {
    static def makeCamelCasePretty(def string) {
        if (string == null)
        return null;
        char[] s = string.toString().toCharArray()
        def sb = new StringBuffer()
        boolean lastCharDot = false
		
        s.eachWithIndex { ch, i ->
            if (Character.isUpperCase(ch)) {
                if (Character.isLowerCase(s[i-1]) 
                    || (s.length > i+1 && Character.isLowerCase(s[i+1]))) {
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
        nest.each() { egg ->
            mp = currMc.getMetaProperty(egg)
            if (mp != null) {
                currMc = mp.type.getMetaClass()
            }
        }
        return mp
    }
	
    static java.util.Date parseDateFromDatePickerParams(def paramProperty, def params) {
    	//println "== parseDate params: ${params.toMapString()}"
		try {
			def year = params["${paramProperty}_year"]
            def month = params["${paramProperty}_month"]
            def day = params["${paramProperty}_day"]
            def hour = params["${paramProperty}_hour"]
            def minute = params["${paramProperty}_minute"]
            return new java.text.SimpleDateFormat('yyyy-MM-dd HH:mm').parse("${year}-${month}-${day} ${hour}:${minute}")
		} catch (Exception ex) {
			//println "${ex.getClass().simpleName} parsing date for property ${paramProperty}: ${ex.message}"
			return null
		}
    }
        
    static def extractFilterParams(params) {
        def ret = [:]
        params.each { entry ->
            if (entry.key.startsWith("filter.") || entry.key.equals("filterProperties") || entry.key.equals("filterBean")) {
                ret[entry.key] = entry.value
            }
        }
        return ret
    }        
}