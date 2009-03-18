package com.zeddware.grails.plugins.filterpane

class FilterTagLib {

    static namespace = 'filterpane'

    /**
     * This map contains available filter operations by type.  It is used when creating the
     * individual rows in the filter pane.
     */
    def availableOpsByType = [
            text: [
                    keys: ['', 'ILike', 'NotILike', 'Like', 'NotLike', 'Equal', 'NotEqual', 'IsNull', 'IsNotNull'],
                    text: ['', 'Contains', 'Does Not Contain', 'Contains (Case Sensitive)',
                            'Does Not Contain (Case Sensitive)', 'Equal To', 'Not Equal To', 'Is Null',
                            'Is Not Null']
            ],
            numeric: [
                    keys: ['', 'Equal', 'NotEqual', 'LessThan', 'LessThanEquals', 'GreaterThan',
                            'GreaterThanEquals', 'Between', 'IsNull', 'IsNotNull'],
                    text: ['', 'Equal To', 'Not Equal To', 'Less Than', 'Less Than or Equal To',
                            'Greater Than', 'Greater Than or Equal To', 'Between', 'Is Null', 'Is Not Null']
            ],
            date: [
                    keys: ['', 'Equal', 'NotEqual', 'LessThan', 'LessThanEquals', 'GreaterThan',
                            'GreaterThanEquals', 'Between', 'IsNull', 'IsNotNull'],
                    text: ['', 'Equal To', 'Not Equal To', 'Less Than', 'Less Than or Equal To',
                            'Greater Than', 'Greater Than or Equal To', 'Between', 'Is Null', 'Is Not Null']
            ],
            boolean: [
                    keys: ['', 'Equal', 'NotEqual', 'IsNull', 'IsNotNull'],
                    text: ['', 'Equal To', 'Not Equal To', 'Is Null', 'Is Not Null']
            ]
    ]
    /**
     * This tag is mainly for internal and test use.
     */
//  def filterControl = {attrs, body ->
//    out << createFilterControl(attrs, body)
//  }

    /**
     * Creates a link (button) that displays the filter pane when pressed.  The title attribute
     * may be used to modify the text on the button.  If omitted, the text will be "Filter".
     * The filterPaneId attribute may be specified if the associated filterPane tag has an id attribute.
     * Their values must be the same.
     */
    def filterButton = {attrs, body ->
        def title = attrs.text ?: (attrs.title ?: 'Filter')
        def filterPaneId = attrs.id ?: (attrs.filterPaneId ?: 'filterPane')
        out << "<a href=\"\" onclick=\"showElement('${filterPaneId}'); return false;\">${title}</a>"
    }

    /**
     * This tag generates necessary style and script includes.  Use this tag in the head element
     * of your pages.  There are no attributes for this tag.
     *
     * @deprecated Use the "includes" tag instead, as it is more DRY. 
     */
    def filterPaneIncludes = {
        out << "<link rel=\"stylesheet\" type=\"text/css\" href=\"${createLinkTo(dir: pluginContextPath + '/css', file: 'filter.css')}\" />\n"
        out << "<script type=\"text/javascript\" src=\"${createLinkTo(dir: pluginContextPath + "/js", file: 'filter.js')}\"></script>"
    }

    /**
     * This tag generates necessary style and script includes.  Use this tag in the head element
     * of your pages.  There are no attributes for this tag.
     *
     * @since 0.4
     */
    def includes = {
        out << "<link rel=\"stylesheet\" type=\"text/css\" href=\"${createLinkTo(dir: pluginContextPath + '/css', file: 'filter.css')}\" />\n"
        out << "<script type=\"text/javascript\" src=\"${createLinkTo(dir: pluginContextPath + "/js", file: 'filter.js')}\"></script>"
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
        def domain = attrs.domain ?: (attrs.filterBean ?: null)
        def bean = FilterUtils.resolveDomainClass(grailsApplication, domain)
        if (bean == null) {
            log.error("Unable to resolve filter domain class ${domain}")
            return
        }

        String title = attrs.title ?: (attrs.filterPaneTitle ?: 'Filter')
        String containerId = attrs.filterPaneId ?: (attrs.id ?: 'filterPane')
        String containerClass = attrs['class'] ?: (attrs.styleClass ?: (attrs.filterPaneClass ?: ''))
        String containerStyle = attrs.style ?: (attrs.filterPaneStyle ?: '')
        String formName = attrs.formName ?: (attrs.filterFormName ?: 'filterForm')

        def props = [:]
        List beanPersistentProps = bean.persistentProperties as List
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
        associatedProps = beanPersistentProps.findAll { it.association == true }
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
                    if (refProperty)
                        props[name] = refProperty
                    else
                        log.error("Unable to find associated property for ${name}")
                } else {
                    log.error("Unable to find associated domain class for ${parts[0]}")
                }
            }
        }

        /*
        * Notes: bean.properties contains all properties as DefaultGrailsDomainClassProperty instances, including id, etc.
        * bean.persistentProperties excludes id and version
        * bean.constrainedProperties contains the same as persistent, but instances are ConstrainedProperty instances.
        */

        if (bean && props) {
           // def filterPaneId = attrs.id ?: 'filterPane'
            def action = attrs.action ?: 'filter'
            def propsStr = ""
            int propsSize = props.size() - 1
            props.eachWithIndex {key, p, i ->
                if (!p.association) {
                    propsStr += key
                    if (i < propsSize) { propsStr += ',' }
                }
            }

            def output = """\
<div id="${containerId}"
     class="filterPane ${containerClass ?: ''}"
     style="display:none;${containerStyle}">
  <h2>${title}</h2>
  <form id="${formName}" name="${formName}" action="${action}">
  <input type="hidden" name="filterProperties" value="${propsStr}" />
  <table cellspacing="0" cellpadding="0" class="filterTable">
"""
            props.each { output += this.buildPropertyRow(bean, it, attrs, params) }
            output += """\
  </table>
  <div>
      Order by
      ${this.select(name: "sort", from: props.values(), optionKey: "name", optionValue: "naturalName", noSelection: ['': 'Select a Property'], value: params.sort)}
      &nbsp;
      ${this.radio(name: "order", value: "asc", checked: params.order == 'asc',)}
      &nbsp;Ascending&nbsp;
      ${this.radio(name: "order", value: "desc", checked: params.order == 'desc')}
      &nbsp;Descending 
  </div>
  <div class="buttons">
      <span class="button">
        <input type="button" value="Cancel" onclick="return hideElement('${containerId}');" />
      </span>
      <span class="button">
        <input type="button" value="Clear" onclick="return clearFilterPane('filterForm');" />
      </span>
      <span class="button">
        ${this.actionSubmit(action: action, value: "Apply")}
      </span>
  </div>
  </form>
</div>
      """
            out << output
        }
    }

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
    def filterPaneOld = {attrs, body ->
        def markup = new groovy.xml.MarkupBuilder(out)
        def formWriter = new StringWriter()
        def formBuilder = new groovy.xml.MarkupBuilder(formWriter)
        formBuilder.doubleQuotes = true
        if (attrs.filterBean && attrs.filterProperties) {
            def containerDivAttrs = ['class': 'filterPane ', style: 'display: none;', id: 'filterPane']
            def filterPaneTitle = 'Filter Settings'
            def filterFormName = 'filterForm'
            def action = 'filter'
            if (attrs.filterPaneId) containerDivAttrs.id = attrs.filterPaneId
            if (attrs.filterPaneStyle) containerDivAttrs.style += attrs.filterPaneStyle
            if (attrs.filterPaneClass) containerDivAttrs['class'] += containerDivAttrs.filterPaneClass
            if (attrs.filterPaneTitle) filterPaneTitle = attrs.filterPaneTitle
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
                                    from: this.availableOpsByType[type].text,
                                    keys: this.availableOpsByType[type].keys,
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
                    td('Order By')
                    td(colspan: '2', '') {
                        formWriter << this.select(name: "sort", from: readableProps, keys: props, noSelection: ['': 'Select a Field'], value: params['sort'])
                        formWriter << '&nbsp;'
                        formWriter << this.radio(name: "order", value: "asc", checked: params['order'] == 'asc', "&nbsp;Ascending&nbsp;")
                        formWriter << this.radio(name: "order", value: "desc", checked: params['order'] == 'desc', "&nbsp;Descending")
                    }
                }
            } // end form table.

            formBuilder.div(class: "buttons") {
                span(class: "button") {
                    input(type: "button", value: "Cancel", onclick: "return hideElement('${containerDivAttrs.id}');")
                }
                span(class: "button") {
                    input(type: "button", value: "Clear", onclick: "return clearFilterPane('${filterFormName}');")
                }
                span(class: "button", '') {
                    formWriter << this.actionSubmit(action: action, value: "Apply")
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

    private def createFilterControl(def property, def formPropName, def attrs, def params) {
        def type = property.type
        def out = ""
        if (type == String.class || type == char.class || Number.class.isAssignableFrom(type) || type == int.class || type == long.class || type == double.class || type == float.class) {

            if (attrs.values) {
                def valueToken = attrs.valuesToken ?: ' '
                def valueList = attrs.values instanceof List ? attrs.values : attrs.values.tokenize(valueToken)
                attrs.putAll([from: valueList, value: params[formPropName]])
                out = this.select(attrs)
            } else {
                out = this.textField(attrs)
            }
        } else if (type == Date.class) {
            Date d = FilterUtils.parseDateFromDatePickerParams(formPropName, params)
            attrs.value = d
            String style = attrs.style ? "style=\"${attrs.style}\"" : ''
            out = "<span id=\"${attrs.id}-container\" ${style}>${this.datePicker(attrs)}</span>"

        } else if (type == Boolean.class || type == boolean.class) {
            out = this.radioGroup([labels: ['Yes', 'No'], values: [true, false], value: params[formPropName], name: formPropName]) {
                "<label for=\"${formPropName}\">${it.label} </label>${it.radio}"
            }
        }
        return out
    }

    private def buildPropertyRow(def bean, def propertyEntry, def attrs, def params) {
        def fullPropName = propertyEntry.key
        def property = propertyEntry.value
        def paramName = "filter.${fullPropName}"
        def opName = "filter.op.${fullPropName}"
        def type = FilterUtils.getOperatorMapKey(property.type)

        def filterCtrlAttrs = [name: paramName, value: params[paramName]]
        if (attrs.filterPropertyValues && attrs.filterPropertyValues[property.name]) {
            filterCtrlAttrs.putAll(attrs.filterPropertyValues[property.name])
        }
        if (!filterCtrlAttrs.id) {
            filterCtrlAttrs.id = property.name
        }

        // filter operator keys and text default to max available for the type.
        def opText = []; opText.addAll(this.availableOpsByType[type].text)
        def opKeys = []; opKeys.addAll(this.availableOpsByType[type].keys)

        // If The property is not nullable, no need to allow them to filter in is or is not null.
        def constrainedProperty = property.domainClass.constrainedProperties[property.name]
        if ((constrainedProperty && !constrainedProperty.isNullable()) || property.name == 'id') {
            opText.remove('Is Not Null')
            opText.remove('Is Null')
            opKeys.remove("IsNotNull")
            opKeys.remove("IsNull")
        }

        // If the user did not specify a value list and the property is constrained with one, use the domain class's list.
        if (!filterCtrlAttrs.values) {
            List inList = constrainedProperty?.getInList()
            if (inList) {
                filterCtrlAttrs.values = inList
            }
        }

        // If the values list is now specified, limit the operators to == or <>
        if (filterCtrlAttrs.values) {
            opText = ['', 'Equal To', 'Not Equal To']
            opKeys = ['', 'Equal', 'NotEqual']
        }

        // Create the operator dropdown.
        def opDropdown = this.select(name: opName, from: opText, keys: opKeys, value: params[opName],
                onclick: "filterOpChange('${opName}', '${filterCtrlAttrs.id}');")
        if (params[opName] == "IsNull" || params[opName] == "IsNotNull") {
            filterCtrlAttrs.style = 'display:none;'
        }

        // Take care of the name.
        def fieldName = property.naturalName
        if (property.domainClass != bean) {
            fieldName = "${property.domainClass.naturalName}'s ${fieldName}"
        }

        def row = """\
    <tr>
      <td>${fieldName}</td>
      <td>${opDropdown}</td>
      <td>
        ${this.createFilterControl(property, paramName, filterCtrlAttrs, params)}"""

        // For numeric and date types, build the "To" control, in case they select between.
        if (type == "numeric" || type == "date") {
            filterCtrlAttrs.name += 'To'
            filterCtrlAttrs.id += 'To'
            filterCtrlAttrs.value = params[filterCtrlAttrs.name]
            boolean showToCtrl = params[opName] == "Between" && filterCtrlAttrs?.value?.trim() != ""
            row += """\
      <span style="${showToCtrl ? '' : 'display:none'}" id="between-span-${property.name}">
        &nbsp;and&nbsp;
        ${this.createFilterControl(property, paramName, filterCtrlAttrs, params)}
      </span>
      """
        }

        row += """\
      </td>
    </tr>"""
        return row
    }
}