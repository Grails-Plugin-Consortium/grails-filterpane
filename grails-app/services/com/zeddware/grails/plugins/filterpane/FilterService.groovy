package com.zeddware.grails.plugins.filterpane

class FilterService {

    boolean transactional = true
    
    def filter(def params, Class filterClass) {
        return filter(params, filterClass, false)
    }
    
    def count(def params, Class filterClass) {
        return filter(params, filterClass, true)
    }

    private def filter(def params, Class filterClass, boolean doCount) {
    	println "filtering... params = ${params.filter.toMapString()}"
    	def filterProperties = params?.filterProperties?.tokenize(',')
        def filterParams = params.filter ? params.filter : params
        def filterOpParams = filterParams.op
        
        if (filterProperties) {
            def c = filterClass.createCriteria()
            def criteriaClosure = {
                def mc = filterClass.getMetaClass()
                and {
                    // First pull out the op map and store a list of its keys.
                    def keyList = []
                    keyList.addAll(filterOpParams.keySet())
                    keyList = keyList.sort() // Sort them to get nested properties next to each other.
                    println "op Keys = ${keyList}"
    				
                    // op = map entry.  op.key = property name.  op.value = operator.
                    // params[op.key] is the value
                    keyList.each() { propName ->
                    	println "\n=============================================================================."
                    	println "== ${propName}"
                        if (! propName.contains(".")) {// Skip associated property entries.  We'll use the map instead later.
                            
                            def filterOp = filterOpParams[propName]
                            def rawValue = filterParams[propName]
                            def rawValue2 = filterParams["${propName}To"]
			    			
                            if (filterOp instanceof Map && rawValue instanceof Map) {
                                // Are any of the values non-empty?
                                if (filterOp.values().find {it.length() > 0} != null) {
                                    println "== Adding association ${propName}"
                                    c."${propName}"() {
                                        filterOp.each() { opEntry ->
                                            def realPropName = opEntry.key
                                            def realOp = opEntry.value
                                            def realRawValue = rawValue[realPropName]
                                            def realRawValue2 = rawValue2 != null ? rawValue2["${realPropName}To"] : null
                                            def val = parseValue(realPropName, realPropName, realRawValue, mc.getMetaProperty(propName).type.getMetaClass(), filterParams)
                                            def val2 = parseValue(realPropName, "${realPropName}To", realRawValue2, mc.getMetaProperty(propName).type.getMetaClass(), filterParams)
                                            addCriterion(c, realPropName, realOp, val, val2)
                                        }
                                    }
                                }
                            } else {
                            	def val = parseValue(propName, propName, rawValue, mc, filterParams)
                                def val2 = parseValue(propName, "${propName}To", rawValue2, mc, filterParams)
                                println "==  val2 is ${val2} of type ${val2?.class}"
                                addCriterion(c, propName, filterOp, val, val2)
                            }
                        }
                    	println "==============================================================================='\n"
                    } // end each op
                } // end and
                
                if (doCount) {
                    c.projections {
                        rowCount()
                    }
                } else {
                    if (params.offset) {
                        firstResult(params.offset.toInteger())
                    }
                    if (params.max) {
                        maxResults(params.max.toInteger())
                    }
                    if (params.sort) {
                        order(params.sort, params.order ? params.order : 'asc')
                    }
                }
            } // end criteria
            def results = null
            if (doCount) {
                results = c.get(criteriaClosure)
            } else {
                results = c.list(criteriaClosure)
              
            }
            if (doCount && results instanceof List) {
                println "Returning count of 0"
                results = 0I
            }
            return results
    	} else {
            if (doCount) {
                return 0I
            }
            return filterClass.list(params)
    	}
    }
    
    private def addCriterion(def criteria, def propertyName, def op, def value, def value2) {
    	//println "== addCriterion IN =="
    	switch(op) {
            case 'Equal':
            criteria.eq(propertyName, value)
            break
            case 'NotEqual': 
            criteria.ne(propertyName, value)
            break
            case 'LessThan': 
            criteria.lt(propertyName, value)
            break
            case 'LessThanEquals':
            criteria.le(propertyName, value)
            break
            case 'GreaterThan':
            criteria.gt(propertyName, value)
            break
            case 'GreaterThanEquals':
            criteria.ge(propertyName, value)
            break
            case 'Like':
            if (!value.startsWith('*')) value = "*${value}"
            if (!value.endsWith('*')) value = "${value}*"
            criteria.like(propertyName, value?.replaceAll("\\*", "%"))
            break
            case 'ILike':
            if (!value.startsWith('*')) value = "*${value}"
            if (!value.endsWith('*')) value = "${value}*"
            criteria.ilike(propertyName, value?.replaceAll("\\*", "%"))
            break
            case 'NotLike':
            	if (!value.startsWith('*')) value = "*${value}"
            	if (!value.endsWith('*')) value = "${value}*"
            	criteria.not {
            		criteria.like(propertyName, value?.replaceAll("\\*", "%"))
            	}
            	break
            case 'NotILike':
            	if (!value.startsWith('*')) value = "*${value}"
                if (!value.endsWith('*')) value = "${value}*"
            	criteria.not {
            		criteria.ilike(propertyName, value?.replaceAll("\\*", "%"))
            	}
            	break
            case 'IsNull':
            criteria.isNull(propertyName)
            break
            case 'IsNotNull':
            criteria.isNotNull(propertyName)
            break
            case 'Between':
            criteria.between(propertyName, value, value2)
            break
            default:
            break
        } // end op switch
    	//println "== addCriterion OUT =="
    }
    
    def parseValue(def prop, def paramName, def rawValue, MetaClass mc, def params) {
    	def mp = FilterUtils.getNestedMetaProperty(mc, prop)
    	def val = rawValue
        if (val) {
            if (mp.type.getSimpleName().equalsIgnoreCase("boolean")) {
                val = val.toBoolean()
            } else if (mp.type == Integer || mp.type == int) {
                val = val.toInteger()
            } else if (mp.type == Long || mp.type == long) {
                val = val.toLong()
            } else if (mp.type == Double || mp.type == double) {
                val = val.toDouble()
            } else if (mp.type == Float || mp.type == float) {
                val = val.toFloat()
            } else if (mp.type == Short || mp.type == short) {
                val = val.toShort()
            } else if (mp.type == BigDecimal) {
                val = val.toBigDecimal()
            } else if (mp.type == BigInteger) {
                val = val.toBigInteger()
            } else if (java.util.Date.isAssignableFrom(mp.type)) {
                val = FilterUtils.parseDateFromDatePickerParams(paramName, params)
            }
        }
    	println "== Parsing value ${rawValue} from param ${paramName}. type is ${mp.type}.  Final value ${val}. Type ${val?.class}"
    	return val
    }
}
