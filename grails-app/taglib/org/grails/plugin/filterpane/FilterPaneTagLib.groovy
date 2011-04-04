package org.grails.plugin.filterpane

import grails.util.GrailsNameUtils
import grails.util.GrailsUtil

/**
 * @author skrenek
 *
 */
class FilterPaneTagLib {
	
	static namespace = "filterpane"
	
	private static final String LAST_UPDATED = "lastUpdated"
	private static final String DefaultFilterPaneId = "filterPane"
	
	/**
	 * This map contains available filter operations by type.  It is used when creating the
	 * individual rows in the filter pane.  The values in the text maps are key suffixes for the
	 * resource bundle.  The prefix used in the valueMessagePrefix attribute will be fp.op.
	 */
	def availableOpsByType = [
		text: ['', 'ILike', 'NotILike', 'Like', 'NotLike', 'Equal', 'NotEqual', 'IsNull', 'IsNotNull'],
		numeric: ['', 'Equal', 'NotEqual', 'LessThan', 'LessThanEquals', 'GreaterThan',
			'GreaterThanEquals', 'Between', 'IsNull', 'IsNotNull'],
		date: ['', 'Equal', 'NotEqual', 'LessThan', 'LessThanEquals', 'GreaterThan',
			'GreaterThanEquals', 'Between', 'IsNull', 'IsNotNull'],
		'boolean': ['', 'Equal', 'NotEqual', 'IsNull', 'IsNotNull'],
		'enum': ['', 'Equal', 'NotEqual'],
		currency: ['', 'Equal', 'NotEqual']
	]
	
	/**
	 * This tag generates necessary style and script includes.  Use this tag in the head element
	 * of your pages.<br/>
	 * <br/>
	 * Attributes
	 * <ul>
	 * <li>css - add this attribute to generate stylesheet includes (any value is fine)</li>
	 * <li>js - add this attribute to generate javascript includes (any value is fine)</li>
	 * <li>(none) - Using the tag with no attributes is identical to adding both. (&lt;filterpane:includes css="true" js="true" /&gt;</li>
	 * </ul>
	 *
	 * @since 0.4; attributes since 2.0
	 */
	def includes = { attrs, body ->
		boolean showCss = false
		boolean showJs = false
		if (attrs != null && attrs.size() > 0) {
			showCss = (attrs.css != null)
			showJs = (attrs.js != null)
		} else {
			showCss = true
			showJs = true
		}
		
		if (showCss)
		out << "<link rel=\"stylesheet\" type=\"text/css\" href=\"${g.resource(dir: 'css', plugin: 'filterpane', file: 'fp.css')}\" />\n"
		
		if (showJs)
		out << "<script type=\"text/javascript\" src=\"${g.resource(dir: 'js', plugin: 'filterpane', file: 'fp.js')}\"></script>"
	}
	
	def isFiltered = { attrs, body ->
		if (FilterPaneUtils.isFilterApplied(params)) {
			out << body()
		}
	}
	
	def isNotFiltered = { attrs, body ->
		if (! FilterPaneUtils.isFilterApplied(params)) {
			out << body()
		}
	}
	
	/**
	* Creates a link (button) that displays the filter pane when pressed.  The title attribute
	* may be used to modify the text on the button.  If omitted, the text will be "Filter".
	* The filterPaneId attribute may be specified if the associated filterPane tag has an id attribute.
	* Their values must be the same.
	*/
	def filterButton = { attrs, body ->
		def renderModel = [:]
		renderModel.filterPaneId = attrs.filterPaneId ?: DefaultFilterPaneId
		renderModel.styleClass = attrs.class ?: ''
		renderModel.style = attrs.style ?: ''
		
		if (FilterPaneUtils.isFilterApplied(params)) {
			renderModel.styleClass = "filter-applied ${renderModel.styleClass}"
			renderModel.text = resolveAttribute (attrs.appliedTextKey, "fp.tag.filterButton.appliedText", attrs.appliedText, "Filter")
			
		} else {
			renderModel.text = resolveAttribute (attrs.textKey, "fp.tag.filterButton.text", attrs.text, "Filter")
		}
		out << g.render(template:"/filterpane/filterButton", plugin:'filterpane', model:renderModel)
	}
	
	/**
	 * Grails pagination tag wrapper for use with the filterpane plugin.
	 *
	 * attribute total - a custom count to be used in pagination
	 * attribute domainBean - the domain bean being filtered.  Ignored if total is specified.
	 */
	def paginate = { attrs, body ->
		def filterParams = FilterPaneUtils.extractFilterParams(params)
		def count = 0I
		
		if (attrs.total != null) {
			count = attrs.total
		} else if (attrs.domainBean) {
			def dc = FilterPaneUtils.resolveDomainClass(grailsApplication, attrs.domainBean)
			
			if (dc) count = dc.clazz.count()
		}
		attrs.total = count
		attrs.params = filterParams
		out << g.paginate(attrs, body)
	}
	
	def currentCriteria = { attrs, body ->
		def renderModel = [:]
		boolean useFullAssociationPath = resolveBoolAttrValue(attrs.fullAssociationPathFieldNames ?: 'y')
		renderModel.isFiltered = FilterPaneUtils.isFilterApplied(params)
		if (renderModel.isFiltered == true) {
			renderModel.id = attrs.id ?: 'filterPaneCurrentCriteria'
			renderModel.quoteValues = resolveBoolAttrValue(attrs.quoteValues ?: 'y')
			renderModel.title = attrs.title ?: ''
			renderModel.styleClass = attrs['class'] ?: ''
			renderModel.style = attrs.style ?: ''
			renderModel.dateFormat = attrs.dateFormat ?: 'yyyy-MM-dd HH:mm:ss'
			renderModel.criteria = []
			renderModel.removeImgDir = attrs.removeImgDir ?: ''
			renderModel.removeImgFile = attrs.removeImgFile
			renderModel.action = attrs.action ?: 'filter'
			
			def filterParams = FilterPaneUtils.extractFilterParams(params, true)
			def domainBean = FilterPaneUtils.resolveDomainClass(grailsApplication, attrs.domainBean)
			
			def getProp = { key, filterOp ->
				if (key.startsWith('filter.op') && filterOp != null && filterOp != '')
					return key[10..-1]
				else
					return false
			}
			
			def getDomainProp = { prop ->
				
				if (prop.contains('.')) { // association.
					def parts = prop.split('\\.')
					def domainProp
					def domainObj = domainBean
					int lastPartIndex = parts.size()-1
					for (int i = 0; i < lastPartIndex; i++) {
						domainProp = domainObj.getPropertyByName(parts[i])
						domainObj = domainProp.referencedDomainClass 
					} 
					
					domainProp = domainObj.getPropertyByName(parts[lastPartIndex])
					return domainProp
				}
				else
					return domainBean.getPropertyByName(prop)
			}
			
			//log.debug("=================================================================")
			//log.debug("current criteria filterParams: ${filterParams}")
			
			filterParams.each { key, filterOp -> 
				
				def criteriaModel = [:]
				def prop = getProp(key, filterOp)
				
				if (prop != false) {
					def domainProp = getDomainProp(prop)
					def filterValue = filterParams["filter.${prop}"]
					def filterValueTo = null
					boolean isNumericType = (domainProp.referencedPropertyType
						? Number.isAssignableFrom(domainProp.referencedPropertyType)
						: false)
					boolean isNumericAndBlank = isNumericType && ! "".equals(filterValue.toString().trim())
					boolean isDateType = (domainProp.referencedPropertyType
						? Date.isAssignableFrom(domainProp.referencedPropertyType)
						: false)
					boolean isEnumType = domainProp?.referencedPropertyType?.isEnum()
					if (filterValue != null && (!isNumericType || isNumericAndBlank) && filterOp?.size() > 0) {
					  
					  if (isDateType) {
					    filterValue = FilterPaneUtils.parseDateFromDatePickerParams("filter.${prop}", filterParams)
							if (filterValue) {
								def df = renderModel.dateFormat
								if (df instanceof Map) {
									df = renderModel.dateFormat[prop]
								}
								filterValue = g.formatDate(format:df, date: filterValue)
							}
					  } else if (isEnumType) {
					    def tempMap = [:]
					    addFilterPropertyValues(attrs, tempMap, prop)
					    def enumValue = Enum.valueOf(domainProp.referencedPropertyType, filterValue)
  						if (enumValue && tempMap.displayProperty) {
  						  filterValue = tempMap.displayProperty == 'name' ? enumValue.name() : enumValue[tempMap.displayProperty]
  					  }
  					  
					  }
						
						def lcFilterOp = filterOp.toLowerCase()
						switch(lcFilterOp) {
							
							case 'isnull':
							case 'isnotnull':
								filterValue = ''
								break
							case 'between':
								filterValueTo = filterParams["filter.${prop}To"]
								if (filterValueTo == 'struct') {
									filterValueTo = FilterPaneUtils.parseDateFromDatePickerParams("filter.${prop}To", params)
									if (filterValueTo) {
										def df = renderModel.dateFormat
										if (df instanceof Map) {
											df = renderModel.dateFormat[prop]
										}
										filterValueTo = g.formatDate(format:df, date:filterValueTo)
									}
								}
								break
						} // end switch.
						
						criteriaModel.filterOp = filterOp
						criteriaModel.filterValue = filterValue
						criteriaModel.filterValueTo = filterValueTo
						criteriaModel.params = [:]
						criteriaModel.params.putAll(filterParams)
						criteriaModel.params.sort = params.sort
						criteriaModel.params.order = params.order
						criteriaModel.params[key] = '' // <== This is what removes the criteria from the list.
						criteriaModel.domainProp = domainProp // <-- TODO: look at this and resolve it how it is done on filterPane tag.
						criteriaModel.prop = prop
						criteriaModel.fieldName = resolveFieldName(prop, domainProp, prop.contains('.'), useFullAssociationPath)
						//log.debug("=================================================================")
						//log.debug("criteriaModel: ${criteriaModel}")
						renderModel.criteria << criteriaModel						
					} // end if fv != null
				}
			}
			//log.debug("=================================================================")
			//log.debug("renderModel: ${renderModel}")
			out << g.render(template:"/filterpane/currentCriteria", plugin:'filterpane', model:renderModel)
		}
		
	}
	
	def filterPane = { attrs, body ->
		
		if (!attrs.domain) {
			log.error("domain attribute is required")
			return
		}

		def renderModel = [customForm:false]
		
		// Validate required info
		def domain = FilterPaneUtils.resolveDomainClass(grailsApplication, attrs.domain)
		if (domain == null) {
			log.error("Unable to resolve domain class for ${attrs.domain}")
			return
		}
		
		boolean useFullAssociationPath = resolveBoolAttrValue(attrs.fullAssociationPathFieldNames ?: 'y')
		
		// Set up the render model.
		
		renderModel.title = resolveAttribute(attrs.titleKey, 'fp.tag.filterPane.titleText', attrs.title, 'Filter')
		renderModel.containerId = attrs.id ?: DefaultFilterPaneId
		renderModel.containerIsDialog = resolveBoolAttrValue(attrs.dialog) ? " fp-dialog" : ""
		renderModel.containerVisibleStyle = resolveBoolAttrValue(attrs.visible) ? "" : "display:none;" 
		renderModel.containerClass = attrs['class'] ?: (attrs.styleClass ?: '')
		renderModel.containerStyle = attrs.style ?: ''
		renderModel.formName = attrs.formName ?: 'filterPaneForm' 
		renderModel.controller = attrs.controller
		renderModel.action = attrs.action ?: 'filter'
		renderModel.customForm = "true".equalsIgnoreCase(attrs?.customForm) || attrs?.customForm == true
		renderModel.formAction = renderModel.controller ? 
			g.createLink(controller:renderModel.controller,	action: renderModel.action) :
			renderModel.action;
		renderModel.showSortPanel = attrs.showSortPanel ? resolveBoolAttrValue(attrs.showSortPanel) : true
		renderModel.showButtons = attrs.showButtons ? resolveBoolAttrValue(attrs.showButtons) : true
		renderModel.showTitle = attrs.showTitle ? resolveBoolAttrValue(attrs.showTitle) : true 
		
		/*
		 * Need properties to filter,
		 * associated properties
		 * additional properties
		 * excluded properties 
		 */
		
		def finalProps      = [:]
		List persistentProps = domain.persistentProperties as List
		List additionalPropNames = resolveListAttribute(attrs.additionalProperties)
		List excludePropNames    = resolveListAttribute(attrs.excludeProperties)
		List associatedPropNames = resolveListAttribute(attrs.associatedProperties) 
		
		// DEPRECATION: attribute name filterProperties as of 2.0
		List explicitPropNames   = resolveListAttribute(attrs.explicitProperties ?: attrs.filterProperties)
		
		// If they explicitly requested certain props, remove the others.
		if (explicitPropNames.size() > 0) {
			persistentProps = persistentProps.findAll { 
				explicitPropNames.contains(it.name)
			}
		}
				
		// Extract out the associations.  These are handled separately from simple properties.
		List associatedProps = persistentProps.findAll { 
			it.association == true && !it.type.isEnum()
		}
		persistentProps.removeAll(associatedProps)
		
		
		def lastUpdatedProp = persistentProps.find { it.name == LAST_UPDATED
		}
		
		if (lastUpdatedProp != null) {
			
			// Verify they did not explicitly request it before removing it.
			boolean keep = explicitPropNames.contains(LAST_UPDATED) || additionalPropNames.contains(LAST_UPDATED)
			if (keep == false) {
				persistentProps.remove(lastUpdatedProp)
			}
		}
		
		// Only id and version are supported right now.
		for (def ap : additionalPropNames) {
			
			if ("id".equals(ap) || "identifier".equals(ap)) {
				
				finalProps[domain.identifier] = domain.identifier.name
			} else if ("version".equals(ap)) {
				
				finalProps[domain.version] = domain.version.name
			}
		}
		
		excludePropNames.each { name ->
			def epObj = persistentProps.find { it.name == name
			}
			if (epObj != null) {
				debug { "Removing ${name} from final props"
				}
				persistentProps.remove(epObj)
			}
		}
		
		// Construct the final properties to filter
		persistentProps.each { finalProps[it] = it.name }
		
		// Add the associated properties
		associatedPropNames.each { dottedName ->
			
			addAssociatedProperty(finalProps, dottedName, associatedProps)
		}
		
		debug {"${finalProps.size()} final props: ${finalProps}"}
		
		// sortedProps is a list of GrailsDomainClassProperty instances, sorted by order they appear in the GrailsDomainClass 
		def sortedProps = finalProps.keySet().asList().sort(new org.codehaus.groovy.grails.scaffolding.DomainClassPropertyComparator(domain))
		
		renderModel.properties = []
		
		sortedProps.each { sp ->
			def map = [domainProperty:sp]
			def propertyKey  = finalProps[sp]
			def opName       = "filter.op.${propertyKey}"
			def propertyType = sp.type
			def type         = FilterPaneUtils.getOperatorMapKey(propertyType)
			
			
			def name = "filter.${propertyKey}"
			map.ctrlAttrs  = [name:name, value:params[name], opName:opName, domain:sp.domainClass.name, propertyName: sp.name ]//, domainProperty:sp]
			addFilterPropertyValues(attrs, map.ctrlAttrs, propertyKey)
			
			def opKeys = []
			opKeys.addAll(this.availableOpsByType[type])
			
			// If the property is not nullable, no need to allow them to filter 
			// in is or is not null.
			def constrainedProperty = sp.domainClass.constrainedProperties[sp.name]
			if ((constrainedProperty && !constrainedProperty.isNullable()) || sp.name == 'id') {
				opKeys.remove("IsNotNull")
				opKeys.remove("IsNull")
			}
			
			map.ctrlType = "text"
			if (propertyType.isAssignableFrom(Date.class)) {
				map.ctrlType = "date"
			} else if (propertyType == Boolean.class || propertyType == boolean.class) {
				map.ctrlType = "boolean"
			}
			
			// If the user did not specify a value list and the property is 
			// constrained with one, use the domain class's list.
			if (! map.ctrlAttrs.values) {
				List inList = constrainedProperty?.getInList()
				if (inList) {
					map.ctrlAttrs.values = inList
				}
				else if (sp.type.isEnum()) {
					//map.ctrlAttrs.values = sp.type.enumConstants as List
					def valueList = []
					
					sp.type.enumConstants.each { 
						def value = it
						if (map.ctrlAttrs.valueProperty) {
						  value = map.ctrlAttrs.valueProperty == 'ordinal' ? it.ordinal() : it[map.ctrlAttrs.valueProperty]
						}
						def display = value
						if (map.ctrlAttrs.displayProperty) {
						  display = map.ctrlAttrs.displayProperty == 'name' ? it.name() : it[map.ctrlAttrs.displayProperty]
					  }
						valueList << [id:value, name:display]
					}
					map.ctrlAttrs.values = valueList
					map.ctrlAttrs.optionKey = "id"
					map.ctrlAttrs.optionValue = "name"
				} // end else if
			}  else { // end if no values specified
				def valueToken = map.ctrlAttrs.valuesToken ?: ' '
				map.ctrlAttrs.values = map.ctrlAttrs.values instanceof List ? map.ctrlAttrs.values : map.ctrlAttrs.values.tokenize(valueToken)
			}
			
			// If the values list is now specified, limit the operators to == or <>
			if (map.ctrlAttrs.values) {
				opKeys = ['', 'Equal', 'NotEqual']
				
				// Also set up the rest of the dropdown ctrl attrs
				map.ctrlAttrs.from = map.ctrlAttrs.values
				map.ctrlAttrs.remove('values') // transferred to "from" property
				map.ctrlAttrs.noSelection = ['':'']
				
				def valueMessagePrefix = "fp.property.text.${sp.name}"
				def valueMessageAltPrefix = "${sp.domainClass.propertyName}.${sp.name}"
				def messageSource = grailsAttributes.getApplicationContext().getBean("messageSource")
				def locale = org.springframework.web.servlet.support.RequestContextUtils.getLocale(request)
				if (messageSource.getMessage(valueMessagePrefix, null, null, locale) != null) {
					map.ctrlAttrs.valueMessagePrefix = valueMessagePrefix
				} else if (messageSource.getMessage(valueMessageAltPrefix, null, null, locale) != null) {
					map.ctrlAttrs.valueMessagePrefix = valueMessageAltPrefix
				}
				map.ctrlType = "select"
			}
			
			if (map.ctrlType == 'select' || map.ctrlType == 'text') {
				map.ctrlAttrs.onChange = "grailsFilterPane.selectDefaultOperator('${opName}')"
			}
			
			// Create the operator dropdown attributes.
			map.opName = opName
			map.opKeys = opKeys
			map.opValue = params[opName]
//			def opDropdown = this.select(id: opName, name: opName, from: opKeys, keys: opKeys,
//			value: params[opName], valueMessagePrefix:'fp.op',
//			onChange: "filterOpChange('${opName}', '${map.ctrlAttrs.id}');")
			if (params[opName] == "IsNull" || params[opName] == "IsNotNull") {
				map.ctrlAttrs.style = 'display:none;'
			}
			
			// Note: propertyKey is the dotted name
			def fieldName = resolveFieldName(propertyKey, sp, propertyKey.contains('.'), useFullAssociationPath)
			
			map.fieldLabel = fieldName

			// Add this new field name as a property of this instance
			sp.metaClass.getFilterPaneFieldName = {-> new String(fieldName) }
			
			// For numeric and date types, build the "To" control, in case they select between.
			if (type == "numeric" || type == "date") {
				map.toCtrlAttrs = [:]
				map.toCtrlAttrs.putAll(map.ctrlAttrs)
				map.toCtrlAttrs.name += "To"
				map.toCtrlAttrs.id += "To"
				map.toCtrlAttrs.value = params[map.toCtrlAttrs.name]
				
				boolean showToCtrl = "between".equalsIgnoreCase(params[opName])
				if (showToCtrl) {
					if (map.toCtrlAttrs.value instanceof Date) {
						showToCtrl = map.toCtrlAttrs.value != "" 
					} else {
						showToCtrl = map.toCtrlAttrs.value?.trim() != ""
					}
				}
				map.toCtrlSpanStyle = showToCtrl ? "" : "display:none;"
			} // end if type numeric or date
			
			renderModel.properties << map
		} // end each sorted property
		
		def sortKeys = []
		sortedProps.each { sp ->
			def name = (finalProps.find { sp.name == it.value })?.value
			if (name) sortKeys << name
		}
		associatedPropNames.each { apn -> sortKeys << apn }
		log.debug("Sorted props: ${sortedProps}")
		log.debug("Sort keys: ${sortKeys}")
		
		renderModel.sortModel = [sortValueMessagePrefix:attrs.sortValueMessagePrefix ?: null,
			sortedProperties: sortedProps, 
			sortKeys: sortKeys,
			sortValue: params.sort ?: "",
			noSelection: ['':g.message(code:'fp.tag.filterPane.sort.noSelection.text', default:'Select a Property')],
			orderAsc: params.order == 'asc',
			orderDesc: params.order == 'desc']
		
		renderModel.buttonModel = [
			cancelText: g.message(code:'fp.tag.filterPane.button.cancel.text', default:'Cancel'),
			clearText:  g.message(code:'fp.tag.filterPane.button.clear.text', default:'Clear'),
			applyText:  g.message(code:'fp.tag.filterPane.button.apply.text', default:'Apply'),
			action: renderModel.action,
			containerId: renderModel.containerId,
			formName: renderModel.formName ]

		out << g.render(template:"/filterpane/filterpane", plugin:'filterpane', model:[fp:renderModel])
	}
	
	def date = { attrs, body ->
		
		Date d = FilterPaneUtils.parseDateFromDatePickerParams(attrs.name, params)
		def model = [:]
		model.putAll(attrs)
		model.value = d
		model.onChange = "grailsFilterPane.selectDefaultOperator('${attrs.opName}')"
		model.isDayPrecision = (attrs.precision == 'day') ? 'y' : 'n'
		def ret = g.render(template:"/filterpane/dateControl", plugin:'filterpane', model:[ctrlAttrs:model])
		out << ret
	}
	
	def bool = { attrs, body ->
		def ret = g.render(template:"/filterpane/boolean", plugin:'filterpane', model: attrs)
		out << ret
	}
	
	def input = { attrs, body ->
		
		def ret = null
		
		switch (attrs.ctrlType) {
			
			case 'date': 
				ret = date(attrs.ctrlAttrs)
				break
			case 'bool':
			case 'boolean':
				ret = bool(attrs.ctrlAttrs)
				break
			case 'select': 
				ret = g.select(attrs.ctrlAttrs)
				break
			case 'text':
				ret = g.textField(attrs.ctrlAttrs)
				break
			default: 
				ret = "<-- Unknown control type: ${attrs.ctrlType} -->"
				break
		}
		
		if (ret)
			out << ret
	}
	
	/**
	 * Resolve the value given the list of possible sources.  Order is as follows:<br/>
	 * <ol>
	 * <li>Message bundle, via "customKey"</li>
	 * <li>attr value</li>
	 * <li>Message bundle, via "localizationKey"</li>
	 * <li>default value</li>
	 * </ol>
	 * 
	 * @param customKey
	 * @param localizationKey
	 * @param attrValue
	 * @param defaultValue
	 * @return
	 */
	private def resolveAttribute(String customKey, String localizationKey, def attrValue, String defaultValue) {
		
		def result = null
		
		if (customKey != null) {
			result = g.message(code:customKey, default:defaultValue)
		} else if (attrValue != null) {
			result = attrValue
		} else {
			result = g.message(code:localizationKey, default:defaultValue)
		}
		
		if (result == null) 
			result = defaultValue
		
		return result
	}
	
	private List resolveListAttribute(attr) {
		List temp = []
		if (attr != null) {
			if (attr instanceof List) {
				temp = attr
			} else if (attr instanceof String) {
				temp = attr.split(",") as List
			}
		}
		return temp.collect { it.trim() }
	}
	
	private void debug(def msgClosure) {
		if (log.isDebugEnabled()) {
			log.debug(msgClosure())
		}
	}
	
	private void addAssociatedProperty(def finalProps, def dottedName, List associatedProps) {
		
		List parts = dottedName.split("\\.") as List
		def association = associatedProps.find { it.name == parts[0] }
		def refDomain = null
		def refProperty = null
		int index = 1
		def fieldNamePrefix = ""
		
		while (association && index < parts.size()) {
			refDomain = association.referencedDomainClass
			fieldNamePrefix += "${grails.util.GrailsNameUtils.getNaturalName(refDomain.clazz.simpleName)}'s "
			if ("id".equalsIgnoreCase(parts[index]) || "identifier".equalsIgnoreCase(parts[index])) {
				refProperty = refDomain.identifier
			} else {
				refProperty = refDomain.persistentProperties.find { it.name == parts[index] }
			}
			//log.debug("refDomain is ${refDomain}, refProperty is ${refProperty}, parts[${index}] = ${parts[index]}")
			association = (refProperty?.association == true && refProperty?.type.isEnum() == false) ? refProperty : null
			index += 1
		}
		
		if (refProperty && ! refProperty.association) {
			log.debug("adding association ${dottedName}")
			refProperty.metaClass.getFilterPaneFieldNamePrefix = {-> return fieldNamePrefix }
			finalProps[refProperty] = dottedName
		} else {
			//log.debug("not adding association ${dottedName}")
		}
	}
	
	private def addFilterPropertyValues(def tagAttrs, def ctrlAttrs, def propertyKey) {
		if (tagAttrs.filterPropertyValues && tagAttrs.filterPropertyValues[propertyKey])
			ctrlAttrs.putAll(tagAttrs.filterPropertyValues[propertyKey])
		
		if (! ctrlAttrs.id) {
			ctrlAttrs.id = propertyKey
		}
	}
	
	private boolean resolveBoolAttrValue(def attr) {
		return 'y'.equalsIgnoreCase(attr) || 't'.equalsIgnoreCase(attr) || "yes".equalsIgnoreCase(attr) || "true".equalsIgnoreCase(attr) 
	}
	
	private String resolveFieldName(def propName, def sp, boolean isAssociation, boolean useFullAssociationPath) {
		// Take care of the name (label).  Yuck!
		def fieldNameKey = "fp.property.text.${propName}" // Default.
		def fieldNameAltKey = fieldNameKey // default for alt key.
		def fieldNamei18NTemplateKey = "${sp.domainClass.name}.${sp.name}"
		def fieldName = sp.naturalName

		if (isAssociation == true) { // association.
			fieldNameKey = "fp.property.text.${sp.domainClass.propertyName}.${sp.name}"
			fieldNamei18NTemplateKey = "${sp.domainClass.propertyName}.${sp.name}"
			// GRAILSPLUGINS-2027 Fix.  associated properties displaying package name.
			def prefix = ""
			if (sp.filterPaneFieldNamePrefix && useFullAssociationPath) {
				prefix = sp.filterPaneFieldNamePrefix
			} else {
				prefix = "${grails.util.GrailsNameUtils.getNaturalName(sp.domainClass.clazz.simpleName)}'s "
			}
			fieldName = "${prefix}${fieldName}"
		}
		
		/*debug {->
			log.debug("fieldNameKey is ${fieldNameKey}")
			log.debug("fieldNameAltKey is ${fieldNameAltKey}")
			log.debug("fieldNamei18NTemplateKey is ${fieldNamei18NTemplateKey}")
			log.debug("fieldName is ${fieldName}")
		}*/
		
		fieldName = g.message(code:fieldNameKey, default: g.message(code:fieldNameAltKey, default:g.message(code:fieldNamei18NTemplateKey, default:fieldName)))
		//debug { -> log.debug("final fieldName is ${fieldName}") }
		
		return fieldName
	}
}
