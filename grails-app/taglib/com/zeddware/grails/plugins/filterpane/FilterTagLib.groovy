package com.zeddware.grails.plugins.filterpane

class FilterTagLib {

    static namespace = 'filterpane'

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
        'enum': ['', 'Equal', 'NotEqual']
    ]

    /**
     * Creates a link (button) that displays the filter pane when pressed.  The title attribute
     * may be used to modify the text on the button.  If omitted, the text will be "Filter".
     * The filterPaneId attribute may be specified if the associated filterPane tag has an id attribute.
     * Their values must be the same.
     */
    def filterButton = {attrs, body ->
        def textKey = attrs.textKey
        def text
        if (textKey != null) {
            text = g.message(code:textKey, default:g.message(code:'fp.tag.filterButton.text', default:'Filter'))
        } else {
            text = attrs.text ?: g.message(code:'fp.tag.filterButton.text', default:(attrs.title ?: 'Filter'))
        }
        def filterPaneId = attrs.id ?: (attrs.filterPaneId ?: 'filterPane')
        def styleClass = attrs.styleClass ?: attrs.class ?: ''
        def style = attrs.style ? " style=\"${attrs.style}\"" : ''
        if (FilterUtils.isFilterApplied(params)) {
            styleClass = "filter-applied ${styleClass}"

            if (attrs.appliedTextKey) {
                text = g.message(code:attrs.appliedTextKey, default:g.message(code:'fp.tag.filterButton.appliedText', default:'Change Filter'))
            } else if (attrs.appliedText && ! ''.equals(attrs.appliedText.trim())) {
                text = attrs.appliedText
            } else {
                text = g.message(code:'fp.tag.filterButton.appliedText', default:text)
            }
        }
        if (styleClass.length() > 0) styleClass = " class=\"${styleClass}\""

        out << "<a href=\"\" onclick=\"showElement('${filterPaneId}'); return false;\"${styleClass}${style}>${text}</a>"
    }

    /**
     * This tag generates necessary style and script includes.  Use this tag in the head element
     * of your pages.  There are no attributes for this tag.
     *
     * @deprecated Use the "includes" tag instead, as it is more DRY. 
     */
    def filterPaneIncludes = {
        out << "<link rel=\"stylesheet\" type=\"text/css\" href=\"${createLinkTo(dir: pluginContextPath + '/css', file: 'fp.css')}\" />\n"
        out << "<script type=\"text/javascript\" src=\"${createLinkTo(dir: pluginContextPath + "/js", file: 'filter.js')}\"></script>"
    }

    /**
     * This tag generates necessary style and script includes.  Use this tag in the head element
     * of your pages.  There are no attributes for this tag.
     *
     * @since 0.4
     */
    def includes = {
        out << "<link rel=\"stylesheet\" type=\"text/css\" href=\"${g.resource(dir: pluginContextPath + '/css', file: 'fp.css')}\" />\n"
        out << "<script type=\"text/javascript\" src=\"${g.resource(dir: pluginContextPath + "/js", file: 'filter.js')}\"></script>"
    }

    def isFiltered = { attrs, body ->
        if (FilterUtils.isFilterApplied(params)) {
            out << body()
        }
    }

    def isNotFiltered = { attrs, body ->
        if (! FilterUtils.isFilterApplied(params)) {
            out << body()
        }
    }

    /**
     * Grails pagination tag wrapper for use with the filterpane plugin.
     *
     * attribute total - a custom count to be used in pagination
     * attribute domainBean - the domain bean being filtered.  Ignored if total is specified.
     */
    def paginate = { attrs, body ->
        def filterParams = FilterUtils.extractFilterParams(params)
        def count = 0I

        if (attrs.total != null) {
            count = attrs.total
        } else if (attrs.domainBean) {
            def dc = FilterUtils.resolveDomainClass(grailsApplication, attrs.domainBean)
            //log.debug("paginate dc is ${dc}. clazz is ${dc.clazz}")
            if (dc) count = dc.clazz.count()
        }
        attrs.total = count
        attrs.params = filterParams
        out << g.paginate(attrs, body)
    }

    def currentCriteria = { attrs, body ->
        if (FilterUtils.isFilterApplied(params)) {
            def id = attrs.id ?: 'filterPaneCurrentCriteria'
            def title = attrs.title ? "title=\"${attrs.title}\" " : ''
            def clazz = attrs.class ? "class=\"${attrs.class}\" " : ''
            def style = attrs.style ? "style=\"${attrs.style}\" " : ''
            def dtFmt = attrs.dateFormat ?: 'yyyy-MM-dd HH:mm:ss'
            def filterParams = FilterUtils.extractFilterParams(params)
            def domainBean = FilterUtils.resolveDomainClass(grailsApplication, attrs.domainBean)
            def action = attrs.action ?: 'filter'

            out << """<ul id="${id}" ${clazz}${style}${title}>"""
            filterParams.each { key, filterOp ->
                log.debug "key is ${key}, filterOp is ${filterOp}"
                if (key.startsWith('filter.op') && filterOp != null && ! ''.equals(filterOp)) {
                    def prop = key[10..-1]
					def domainProp
                    if (prop.contains('.')) { // association.
                        def parts = prop.split('\\.')
                        domainProp = domainBean.getPropertyByName(parts[0])
                        domainProp = domainProp.referencedDomainClass.getPropertyByName(parts[1])
                    } else {
                        domainProp = domainBean.getPropertyByName(prop)
                    }
					def filterVal = "${filterParams["filter.${prop}"]}"
					boolean isNumericType = (domainProp.referencedPropertyType
						? Number.isAssignableFrom(domainProp.referencedPropertyType)
						: false)
					boolean isNumericAndBlank = isNumericType && ! "".equals(filterVal.toString().trim())

                    if (filterVal != null && (!isNumericType || isNumericAndBlank)) {

						if ('isnull'.equalsIgnoreCase(filterOp) || 'isnotnull'.equalsIgnoreCase(filterOp)) {
							filterVal = ''
						} else if (filterVal == 'struct') {
							filterVal = FilterUtils.parseDateFromDatePickerParams("filter.${prop}", params)
							if (filterVal) {
								def dateFormat = dtFmt
								if (dtFmt instanceof Map) {
									dateFormat = dtFmt[prop]
								}
								filterVal = "${g.formatDate(format:dateFormat, date:filterVal)}"
							}
						}
						def filterValTo = null
						if ('between'.equalsIgnoreCase(filterOp)) {
							filterValTo = filterParams["filter.${prop}To"]
							if (filterValTo == 'struct') {
								filterValTo = FilterUtils.parseDateFromDatePickerParams("filter.${prop}To", params)
								if (filterValTo) {
									def dateFormat = dtFmt
									if (dtFmt instanceof Map) {
										dateFormat = dtFmt[prop]
									}
									filterValTo = g.formatDate(format:dateFormat, date:filterValTo)
								}
							}
						}
						def newParams = [:]
						newParams.putAll(filterParams)
						newParams[key] = '' // <== This is what removes the criteria from the list.
						def removeText = attrs.removeImgDir && attrs.removeImgFile ? "<img src=\"${g.resource(dir:attrs.removeImgDir, file:attrs.removeImgFile)}\" alt=\"(X)\" title=\"Remove\" />" : '(X)'
						def removeLink = """<a href="${g.createLink(action:action,params:newParams)}" class="remove">${removeText}</a>"""
						if (filterValTo) {
							filterValTo = " and \"${filterValTo}\""
						} else {
							filterValTo = ''
						}
						out << """<li>${g.message(code:"fp.property.text.${prop}", default:g.message(code:"${domainProp.domainClass}.${domainProp.name}",default:domainProp.naturalName))} ${g.message(code:"fp.op.${filterOp}", default:filterOp)} "${filterVal}"${filterValTo} ${removeLink}</li>"""
					} // end if filterVal != null
                }
            }
            out << "</ul>"
        }
    }

    /**
     * This tag generates the filter pane itself.  As of release 0.4, this tag pulls as much filtering information from
     * the domain class as possible by default.  All attributes from 0.3.1 are still supported,
     * but are considered deprecated in favor of more sensible alternatives.
     *
     * TODO: Document attributes.
     *
     */
    def filterPane = {attrs, body ->

        // If people want to use the old tag's logic, let them.
        if (attrs.useLegacyFilterpane == "true") {
            if (log.isDebugEnabled()) log.debug("Calling legacy filterpane.")
            this.filterPaneLegacy(attrs, body)
            if (log.isDebugEnabled()) log.debug("Done with legacy filter pane.")
            return
        }

        def domain = attrs.domainBean ?: (attrs.filterBean ?: null)
        def bean = FilterUtils.resolveDomainClass(grailsApplication, domain)
        if (bean == null) {
            log.error("Unable to resolve filter domain class ${domain}")
            return
        }

        String titleKey = attrs.titleKey
        String title
        if (titleKey != null) {
            title = g.message(code:titleKey, default:g.message(code:'fp.tag.filterPane.titleText', default:'Filter'))
        } else {
            title = attrs.title ?: (attrs.filterPaneTitle ?: g.message(code:'fp.tag.filterPane.titleText', default:'Filter'))
        }
        String containerId = attrs.filterPaneId ?: (attrs.id ?: 'filterPane')
        String containerClass = attrs['class'] ?: (attrs.styleClass ?: (attrs.filterPaneClass ?: ''))
        String containerStyle = attrs.style ?: (attrs.filterPaneStyle ?: '')
        String formName = attrs.formName ?: (attrs.filterFormName ?: 'filterForm')

        def props = [:]
        List beanPersistentProps = bean.persistentProperties as List
        if (log.isDebugEnabled()) {
            log.debug("${beanPersistentProps.size()} Persistent props: ${beanPersistentProps}")
        }
        List associatedProps = []

        // If the user specified the properties, only include those.
        if (attrs.filterProperties) {
            beanPersistentProps = beanPersistentProps.findAll {bpp ->
                attrs.filterProperties?.contains(bpp.name)
            }
        }

        // Remove the last udpated property.  We don't filter this by default.
        def lastUpdated = beanPersistentProps.find { it.name == 'lastUpdated'}
        if (!attrs.filterProperties || !attrs.filterProperties.contains('lastUpdated')) {
            if (lastUpdated)
            beanPersistentProps.remove(lastUpdated)
        }

        // Remove association properties.  We can't handle them directly.
        associatedProps = beanPersistentProps.findAll { it.association == true && !it.type.isEnum() }
        beanPersistentProps.removeAll(associatedProps)

        // Add any non-default additional properties they specified.
        if (attrs.additionalProperties) {
            def ap = (attrs.additionalProperties instanceof List) ? attrs.additionalProperties : []
            if (attrs.additionalProperties instanceof String) {
                ap = attrs.additionalProperties.split(",") as List
            }
            if (ap.contains('id') || ap.contains('identifier')) {
                props[bean.identifier.name] = bean.identifier
            }
            if (ap.contains('version')) {
                props[bean.version.name] = bean.version
            }
            if (lastUpdated && ap.contains('lastUpdated')) {
                props[lastUpdated.name] = lastUpdated
            }
        }

        // Exclude anything they explicitly don't want filtered.
        if (attrs.excludeProperties) {
            def ep = (attrs.excludeProperties instanceof List) ? attrs.excludeProperties : []
            if (attrs.excludeProperties instanceof String) {
                ep = attrs.excludeProperties.split(",") as List
            }
            ep.each {
                def name = it.trim()
                def epObj = beanPersistentProps.find { bpp -> bpp.name == name }
                if (epObj != null) {
                    if (log.isDebugEnabled()) {
                        log.debug("Removing ${name} from filterable properties.")
                    }
                    beanPersistentProps.remove(epObj)
                }
            }
        }

        // Put the persistent props in the final map.
        beanPersistentProps.each {
            props[it.name] = it
        }

        // Add any associated properties.
        def associationNames = [:] // This prevents further churning through the list later.
        if (attrs.associatedProperties) {
            def ap = (attrs.associatedProperties instanceof List) ? attrs.associatedProperties : []
            if (attrs.associatedProperties instanceof String) {
                ap = attrs.associatedProperties.split(",") as List
            }

            ap.each {
                def name = it.trim()
                def parts = name.split("\\.")
                def association = associatedProps.find { it.name == parts[0] }
                if (association) {
                    def refDomain = association.referencedDomainClass
                    def refProperty = refDomain.persistentProperties.find { it.name == parts[1] }
                    if (refProperty) {
                        props[name] = refProperty
                        if (!associationNames[refProperty])
                            associationNames[refProperty] = []
                        associationNames[refProperty] << name
                    } else
                        log.error("Unable to find associated property for ${name}")
                } else {
                    log.error("Unable to find associated domain class for ${parts[0]}")
                }
            }
        }

        // Sort the properties by domain class order.
        def sortedProperties = props.values().sort(new org.codehaus.groovy.grails.scaffolding.DomainClassPropertyComparator(bean))

        /*
         * Notes: bean.properties contains all properties as DefaultGrailsDomainClassProperty instances, including id, etc.
         * bean.persistentProperties excludes id and version
         * bean.constrainedProperties contains the same as persistent, but instances are ConstrainedProperty instances.
         */

        if (bean && props) {
            // def filterPaneId = attrs.id ?: 'filterPane'
			def controller = attrs.controller
            def action = attrs.action ?: 'filter'
            def propsStr = ""
            int propsSize = props.size() - 1
            props.eachWithIndex {key, p, i ->
                if (!p.association) {
                    propsStr += key
                    if (i < propsSize) { propsStr += ',' }
                }
            }

            def sortKeys = []
            sortedProperties.each { sp ->
                sortKeys << (props.find { sp == it.value })?.key
            }

			// Added for 0.6.4 as a new attribute to the tag.
			boolean customForm = "true".equals(attrs.customForm) || attrs.customForm == true
			def openFormTag = ''
			def closeFormTag = ''
			def applyButton = ''
			if (customForm == false) {
				def actionURL = controller ? g.createLink(controller:controller, action:action) : action
				openFormTag = """<form id="${formName}" name="${formName}" action="${actionURL}" method="post">"""
				closeFormTag = '</form>'
				applyButton = """<span class="button">
        ${this.actionSubmit(action: action, value: g.message(code:'fp.tag.filterPane.button.apply.text', default:'Apply'))}
      </span>"""
			}
			

            def output = """\
<div id="${containerId}"
     class="filterPane ${containerClass ?: ''}"
     style="display:none;${containerStyle}">
  <h2>${title}</h2>
  ${openFormTag}
  <input type="hidden" name="filterProperties" value="${propsStr}" />
  <table cellspacing="0" cellpadding="0" class="filterTable">
"""
            sortedProperties.each {
                def assocName = associationNames[it]
                if (assocName) {
                    assocName = assocName.remove(0)
                }
                output += this.buildPropertyRow(bean, it, assocName, attrs, params)
            }
            output += """\
  </table>
  <div>
      ${g.message(code:'fp.tag.filterPane.sort.orderByText', default:'Order by')}"""

	  // Do an if check.  If the messages don't exist, default to the legacy natural name.'
	  def valueMessagePrefix = attrs.sortValueMessagePrefix ?: 'fp.property.text'
	  def firstKeyMsg = g.message(code:"${valueMessagePrefix}.${sortKeys[0]}", default:'false')
	  if (firstKeyMsg.equals('false')) {
		  output += this.select(name: "sort", from: sortedProperties, keys:sortKeys,
			optionValue: "naturalName",
			noSelection: ['': g.message(code:'fp.tag.filterPane.sort.noSelection.text', default:'Select a Property')],
			value: params.sort)
	  } else {
		  output += this.select(name: "sort", from: sortedProperties, keys: sortKeys,
			valueMessagePrefix: valueMessagePrefix,
			noSelection: ['': g.message(code:'fp.tag.filterPane.sort.noSelection.text', default:'Select a Property')],
			value: params.sort)
	  }
output += """\
      &nbsp;
      ${this.radio(name: "order", value: "asc", checked: params.order == 'asc',)}
      &nbsp;${g.message(code:'fp.tag.filterPane.sort.ascending', default:'Ascending')}&nbsp;
      ${this.radio(name: "order", value: "desc", checked: params.order == 'desc')}
      &nbsp;${g.message(code:'fp.tag.filterPane.sort.descending', default:'Descending')}
  </div>
  <div class="buttons">
      <span class="button">
        <input type="button" value="${g.message(code:'fp.tag.filterPane.button.cancel.text', default:'Cancel')}" onclick="return hideElement('${containerId}');" />
      </span>
      <span class="button">
        <input type="button" value="${g.message(code:'fp.tag.filterPane.button.clear.text', default:'Clear')}" onclick="return clearFilterPane('${formName}');" />
      </span>
      ${applyButton}
  </div>
  ${closeFormTag}
</div>
      """
            out << output
        }
    }

// <editor-fold defaultstate="collapsed">
    /**
     * Creates a div that can be shown that contains a filter (search) form.
     *
     * Parameters
     * <table>
     * <tr><th>Attribute</th><th>Required</th><th>Description</th></tr>
     * <tr><td>filterBean</td><td>Yes</td><td>The model class to extract filter properties from.</td></tr>
     * <tr><td>filterProperties</td><td>Yes</td><td>A list of filterBean's properties to include on the form.</td></tr>
     * <tr><td>filterPaneId</td><td>The ID attribute for the container div.  Defaults to 'filterPane'</td></tr>
     * <tr><td>filterPaneStyle</td><td>The style attribute for the container div.</td></tr>
     * <tr><td>filterPaneClass</td><td>The style class for the container div.</td></tr>
     * <tr><td>filterPaneTitle</td><td>The H2 (header) text for the form.  Defaults to 'Filter Settings'</td></tr>
     * <tr><td>filterFormName</td><td>The name attribute for the filter form.  Defaults to 'filterForm'</td></tr>
     * </table>
     * @deprecated Consider using the <code>filterPane</code> tag instead.
     */
    private def filterPaneLegacy (attrs, body) {
        //println "in legacy filterpane.  out is ${out}"
        def markup = new groovy.xml.MarkupBuilder(out)
        def formWriter = new StringWriter()
        def formBuilder = new groovy.xml.MarkupBuilder(formWriter)
        formBuilder.doubleQuotes = true
        if (attrs.filterBean && attrs.filterProperties) {
            def containerDivAttrs = ['class': 'filterPane ', style: 'display: none;', id: 'filterPane']
            def filterPaneTitle
            String titleKey = attrs.titleKey
            if (titleKey != null) {
                filterPaneTitle = g.message(code:titleKey, default:g.message(code:'fp.tag.filterPane.default-title', default:'Filter Settings'))
            } else {
                filterPaneTitle = attrs.title ?: (attrs.filterPaneTitle ?: g.message(code:'fp.tag.filterPane.default-title', default:'Filter Settings'))
            }
            def filterFormName = 'filterForm'
            def action = 'filter'
            if (attrs.filterPaneId) containerDivAttrs.id = attrs.filterPaneId
            if (attrs.filterPaneStyle) containerDivAttrs.style += attrs.filterPaneStyle
            if (attrs.filterPaneClass) containerDivAttrs['class'] += containerDivAttrs.filterPaneClass
            //            if (attrs.filterPaneTitle) filterPaneTitle = attrs.filterPaneTitle
            if (attrs.filterFormName) filterFormName = attrs.filterFormName
            if (attrs.filterPaneAction) action = attrs.filterPaneAction

            def bean = attrs.filterBean
            def props = attrs.filterProperties
            def mc = bean.getMetaClass()
            def readableProps = []
            readableProps.addAll(props)
            def propsStr = props.toString().minus('[').minus(']')

            for (int i = 0; i < readableProps.size(); i++) {
                readableProps[i] = FilterUtils.makeCamelCasePretty(readableProps[i])
            }

            formBuilder.input(type: 'hidden', name: 'filterProperties', value: propsStr)
            formBuilder.table(cellspacing: '0', cellpadding: '0', class: 'filterTable') {
                props.each {prop ->
                    def opName = "filter.op.${prop}"
                    def propName = "filter.${prop}"
                    def filterControlAttrs = [filterBean: bean, filterProperty: propName]
                    def type = FilterUtils.getOperatorMapKey(FilterUtils.getNestedMetaProperty(mc, prop).type)
                    if (attrs.filterPropertyValues && attrs.filterPropertyValues[prop]) filterControlAttrs.putAll(attrs.filterPropertyValues[prop])
                    if (filterControlAttrs['name']) filterControlAttrs['name'] = "filter.${filterControlAttrs['name']}"
                    filterControlAttrs.value = params[filterControlAttrs.name]
                    if (filterControlAttrs['id'] == null) {
                        if (filterControlAttrs['name'] != null) {
                            filterControlAttrs['id'] = filterControlAttrs['name']
                        } else {
                            filterControlAttrs['id'] = propName
                        }
                    }
                    tr() {
                        td(FilterUtils.makeCamelCasePretty(prop))
                        td('') {
                            formWriter << this.select(name: opName,
                                from: this.availableOpsByType[type],
                                keys: this.availableOpsByType[type],
                                valueMessagePrefix: 'fp.op',
                                value: params[opName],
                                onclick: "filterOpChange('${opName}', '${filterControlAttrs['id']}');")
                        }
                        td('') {
                            formWriter << this.createFilterControl(filterControlAttrs, '', (params[opName] != "IsNull" && params[opName] != "IsNotNull"))
                            if (type == "numeric" || type == "date") {
                                filterControlAttrs.name = propName + 'To'
                                filterControlAttrs.id = "${filterControlAttrs.id}To"
                                filterControlAttrs.value = params[filterControlAttrs.name]
                                boolean showToCtrl = filterControlAttrs.value != null && filterControlAttrs.value != "" && params[opName] == "Between"
                                span(style: (showToCtrl) ? "" : "display:none", id: "between-span-${prop}", '') {
                                    formWriter << '&nbsp;and&nbsp;'
                                    formWriter << this.createFilterControl(filterControlAttrs, '', true)
                                }
                            }
                        }
                    }
                }
                tr(valign: 'middle') {
                    td(g.message(code:'fp.tag.filterPane.sort.orderByText', default:'Order by'))
                    td(colspan: '2', '') {
                        formWriter << this.select(name: "sort", from: readableProps, keys: props, noSelection: ['': g.message(code:'fp.tag.filterPane.sort.noSelection.text', default:'Select a Field')], value: params['sort'])
                        formWriter << '&nbsp;'
                        formWriter << this.radio(name: "order", value: "asc", checked: params['order'] == 'asc', "&nbsp;${g.message(code:'fp.tag.filterPane.sort.ascending', default:'Ascending')}&nbsp;")
                        formWriter << this.radio(name: "order", value: "desc", checked: params['order'] == 'desc', "&nbsp;${g.message(code:'fp.tag.filterPane.sort.descending', default:'Descending')}}")
                    }
                }
            } // end form table.

            formBuilder.div(class: "buttons") {
                span(class: "button") {
                    input(type: "button", value: g.message(code:'fp.tag.filterPane.button.cancel.text', default:'Cancel'), onclick: "return hideElement('${containerDivAttrs.id}');")
                }
                span(class: "button") {
                    input(type: "button", value: g.message(code:'fp.tag.filterPane.button.clear.text', default:'Clear'), onclick: "return clearFilterPane('${filterFormName}');")
                }
                span(class: "button", '') {
                    formWriter << this.actionSubmit(action: action, value: g.message(code:'fp.tag.filterPane.button.apply.text', default:'Apply'))
                }
            } // end form button div.

            // Now create the main div.
            markup.div(containerDivAttrs) {
                h2("${filterPaneTitle}")
                this.out << this.form(name: filterFormName, action: action) {
                    formWriter.toString()

                }
            }
        }
    }
// </editor-fold>
    private String createFilterControl(def attrs, def body, boolean visible) {
        def stream = ""
        if (attrs.filterBean && attrs.filterProperty) {
            def mc = attrs.filterBean.getMetaClass()
            def mp = FilterUtils.getNestedMetaProperty(mc, attrs.filterProperty)
            def type = mp.type.getSimpleName()
            def finalAttrs = new java.util.HashMap(attrs)
            finalAttrs.remove('filterBean')
            finalAttrs.remove('filterProperty')
            finalAttrs.remove('values')
            finalAttrs.remove('valuesToken')
            finalAttrs.remove('out')
            if (!visible) {
                if (finalAttrs.style) {
                    finalAttrs.style = "display:none;${finalAttrs.style}"
                } else {
                    finalAttrs.style = "display:none"
                }
            }
            if (!finalAttrs.name) finalAttrs.name = attrs.filterProperty
            if (attrs['name']) finalAttrs['name'] = attrs['name']
            if (!finalAttrs['id']) finalAttrs['id'] = finalAttrs['name']

            if (type == "String" || type == "char" || Number.class.isAssignableFrom(mp.type)
                || type == "int" || type == "long" || type == "double" || type == "float") {
                if (attrs.values) {
                    def valueToken = attrs.valuesToken ? attrs.valuesToken : ' '
                    def valueList = (attrs.values instanceof List) ? attrs.values : attrs.values.tokenize(valueToken)
                    finalAttrs.putAll([from: valueList, value: params[attrs.filterProperty]])
                    stream = this.select(finalAttrs)
                } else {
                    def ctrlWriter = new StringWriter()
                    def markup = new groovy.xml.MarkupBuilder(ctrlWriter)
                    markup.doubleQuotes = true
                    finalAttrs.putAll([type: 'text', value: attrs.value ? attrs.value : params[attrs.filterProperty]])
                    if (attrs.id) finalAttrs['id'] = attrs.id
                    if (attrs.maxlength) finalAttrs['maxlength'] = attrs.maxlength
                    if (attrs['size']) finalAttrs['size'] = attrs['size']
                    markup.input(finalAttrs)
                    stream = ctrlWriter.toString()
                }
            } else if (type == "Date") {
                finalAttrs['value'] = FilterUtils.parseDateFromDatePickerParams(finalAttrs.name, params)
                //println "Final date attrs are ${finalAttrs}"
                def spanStyle = (finalAttrs.style) ? "style=\"${finalAttrs.style}\"" : ""
                stream = "<span id=\"" + finalAttrs['id'] + "-container\" ${spanStyle}>" + this.datePicker(finalAttrs) + "</span>"
            } else if (type == "Boolean" || type == "boolean") {
                finalAttrs.putAll(labels: ['Yes', 'No'], values: [true, false], value: params[attrs.filterProperty])
                stream = this.radioGroup(finalAttrs) { "<label for=\"${attrs.filterProperty}\">${it.label} </label>${it.radio}" }
            } else {

            }
        }
        return stream
    }

    private def createFilterControl(def property, def formPropName, def attrs, def params, def opId) {
        def type = property.type
        def out = ""
        if (type == String.class || type == char.class || Number.class.isAssignableFrom(type) 
                || type == int.class || type == long.class || type == double.class
                || type == float.class || type.isEnum()) {
                
            if (attrs.values) {
                def valueList

                if (type.isEnum() && attrs.displayProperty) {
                    valueList = []
                    type.enumConstants.each {
                        def value = it
//                        if (attrs.valueProperty) {
//                            if (attrs.valueProperty == 'ordinal') {
//                                value = it.ordinal()
//                            } else {
//                                value = it[attrs.valueProperty]
//                            }
//                        }
                        // the name property of an enum does not conform to the bean spec.  The method is just "name()"
                        def display = attrs.displayProperty == 'name' ? it.name() : it[attrs.displayProperty]
                        valueList << [id:value, name:display]
                    }

                    attrs.optionKey = 'id'
                    attrs.optionValue = 'name'
                } else {
                    def valueToken = attrs.valuesToken ?: ' '
                    valueList = attrs.values instanceof List ? attrs.values : attrs.values.tokenize(valueToken)
                }

                attrs.putAll([from: valueList, value: params[formPropName], onChange:"selectDefaultOperator('${opId}')"])

                def valueMessagePrefix = "fp.property.text.${property.name}"
                def valueMessageAltPrefix = "${property.domainClass.propertyName}.${property.name}"

                def messageSource = grailsAttributes.getApplicationContext().getBean("messageSource")
                def locale = org.springframework.web.servlet.support.RequestContextUtils.getLocale(request)
                if (messageSource.getMessage(valueMessagePrefix, null, null, locale) != null) {
                 	attrs.valueMessagePrefix = valueMessagePrefix
                } else if (messageSource.getMessage(valueMessageAltPrefix, null, null, locale) != null) {
                 	attrs.valueMessagePrefix = valueMessageAltPrefix
                }
                out = this.select(attrs)
            } else {
                attrs.onChange = "selectDefaultOperator('${opId}')"
                out = this.textField(attrs)
            }
        } else if (type == Date.class) {
            Date d = FilterUtils.parseDateFromDatePickerParams(formPropName, params)
            attrs.value = d
            attrs.onChange = "selectDefaultOperator('${opId}')"
            String style = attrs.style ? "style=\"${attrs.style}\"" : ''
			boolean isDayPrecision = attrs.precision == 'day'
			String strDayPrecision = ""
			if (!formPropName.endsWith("To")) {
				strDayPrecision = """<input type="hidden" name="filter.${property.domainClass.name}.${property.name}_isDayPrecision" value="${isDayPrecision ? 'y' : 'n'}" />"""
			}
            out = """<span id="${attrs.id}-container" ${style}>${this.datePicker(attrs)}</span>${strDayPrecision}"""

        } else if (type == Boolean.class || type == boolean.class) {
            def yes = radio(id:"${formPropName}.yes", name:formPropName, value:'true', checked:params[formPropName] == 'true', onClick:"selectDefaultOperator('${opId}')")
            def no = radio(id:"${formPropName}.no", name:formPropName, value:'false', checked:params[formPropName] == 'false', onClick:"selectDefaultOperator('${opId}')")
            out = """\
            <label for="${formPropName}.yes">${g.message(code:'fp.tag.filterPane.property.boolean.true', default:'Yes ')}</label>
            ${yes}
            <label for="${formPropName}.no">${g.message(code:'fp.tag.filterPane.property.boolean.false', default:'No ')}</label>
            ${no}
            """
        }
        return out
    }

    private def buildPropertyRow(def bean, def property, def propertyNameKey, def attrs, def params) {
        def fullPropName = (property.domainClass == bean) ? property.name : propertyNameKey
        def paramName = "filter.${fullPropName}"
        def opName = "filter.op.${fullPropName}"
        def type = FilterUtils.getOperatorMapKey(property.type)
        //if (log.isDebugEnabled()) log.debug("property ${property} key ${propertyNameKey} fullPropName ${fullPropName} type ${type}");

        def filterCtrlAttrs = [name: paramName, value: params[paramName]]
        if (attrs.filterPropertyValues && (attrs.filterPropertyValues[property.name] || attrs.filterPropertyValues[propertyNameKey])) {
            if (attrs.filterPropertyValues[property.name])
                filterCtrlAttrs.putAll(attrs.filterPropertyValues[property.name])
            if (attrs.filterPropertyValues[propertyNameKey])
                filterCtrlAttrs.putAll(attrs.filterPropertyValues[propertyNameKey])
        }
        if (!filterCtrlAttrs.id) {
            filterCtrlAttrs.id = property.name
        }

        // filter operator keys and text default to max available for the type.
        //def opText = []; opText.addAll(this.availableOpsByType[type].text)
        def opKeys = []; opKeys.addAll(this.availableOpsByType[type])

        // Remove all but = <> for enum properties
        //        if (property.type.isEnum()) {
        //            opKeys.clear()
        //            opKeys.addAll(this.availableOpsByType['enum'])
        //        }

        // If The property is not nullable, no need to allow them to filter in is or is not null.
        def constrainedProperty = property.domainClass.constrainedProperties[property.name]
        if ((constrainedProperty && !constrainedProperty.isNullable()) || property.name == 'id') {
            //            opText.remove('is-not-null')
            //            opText.remove('is-null')
            opKeys.remove("IsNotNull")
            opKeys.remove("IsNull")
        }

        // If the user did not specify a value list and the property is constrained with one, use the domain class's list.
        if (!filterCtrlAttrs.values) {
            List inList = constrainedProperty?.getInList()
            if (inList) {
                filterCtrlAttrs.values = inList
            }
            else if (property.type.isEnum()) {
                filterCtrlAttrs.values = property.type.enumConstants
            }
        }

        // If the values list is now specified, limit the operators to == or <>
        if (filterCtrlAttrs.values) {
            opKeys = ['', 'Equal', 'NotEqual']
        }

        // Create the operator dropdown.
        def opDropdown = this.select(id: opName, name: opName, from: opKeys, keys: opKeys,
            value: params[opName], valueMessagePrefix:'fp.op',
            onChange: "filterOpChange('${opName}', '${filterCtrlAttrs.id}');")
        if (params[opName] == "IsNull" || params[opName] == "IsNotNull") {
            filterCtrlAttrs.style = 'display:none;'
        }

        // Take care of the name.
        def fieldNameKey = "fp.property.text.${fullPropName}" // Default.
        def fieldNameAltKey = fieldNameKey // default for alt key.
        def fieldNamei18NTemplateKey = "${bean.propertyName}.${property.name}"
        def fieldName = property.naturalName

        if (property.domainClass != bean) { // association.
            fieldNameKey = "fp.property.text.${property.domainClass.propertyName}.${property.name}"
            fieldNamei18NTemplateKey = "${property.domainClass.propertyName}.${property.name}"
            fieldName = "${property.domainClass.naturalName}'s ${fieldName}"
        }
        fieldName = g.message(code:fieldNameKey, default: g.message(code:fieldNameAltKey, default:g.message(code:fieldNamei18NTemplateKey, default:fieldName)))

        def row = """\
    <tr>
      <td>${fieldName}</td>
      <td>${opDropdown}</td>
      <td>
        ${this.createFilterControl(property, paramName, filterCtrlAttrs, params, opName)}"""

        // For numeric and date types, build the "To" control, in case they select between.
        if (type == "numeric" || type == "date") {
            filterCtrlAttrs.name += 'To'
            filterCtrlAttrs.id += 'To'
            filterCtrlAttrs.value = params[filterCtrlAttrs.name]

            boolean showToCtrl
            if(filterCtrlAttrs?.value instanceof Date)
                showToCtrl = params[opName] == "Between" && filterCtrlAttrs?.value != ""
            else
                showToCtrl = params[opName] == "Between" && filterCtrlAttrs?.value?.trim() != ""

            row += """\
      <span style="${showToCtrl ? '' : 'display:none'}" id="between-span-${property.name}">
        &nbsp;${g.message(code:'fp.tag.filterPane.property.betweenValueSeparatorText', default:'and')}&nbsp;
        ${this.createFilterControl(property, filterCtrlAttrs.name, filterCtrlAttrs, params, opName)}
      </span>
      """
        }

        row += """\
      </td>
    </tr>"""
        return row
    }
}