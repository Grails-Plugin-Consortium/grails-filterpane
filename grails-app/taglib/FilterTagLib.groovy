package com.zeddware.grails.plugins.filterpane.taglib

class FilterTagLib {

    static namespace = 'filterpane'

    /**
     * This map contains available filter operations by type.  It is used when creating the
     * individual rows in the filter pane.
     */
    def availableOpsByType = [
        text:[
            keys:['', 'ILike', 'NotILike', 'Like', 'NotLike', 'Equal', 'NotEqual', 'IsNull', 'IsNotNull'],
            text:['', 'Contains', 'Does Not Contain', 'Contains (Case Sensitive)', 
                'Does Not Contain (Case Sensitive)', 'Equal To', 'Not Equal To', 'Is Null',
                'Is Not Null']
        ],
        numeric:[
            keys:['', 'Equal', 'NotEqual', 'LessThan', 'LessThanEquals', 'GreaterThan',
                'GreaterThanEquals', 'Between', 'IsNull', 'IsNotNull'],
            text:['', 'Equal To', 'Not Equal To', 'Less Than', 'Less Than or Equal To',
                'Greater Than', 'Greater Than or Equal To', 'Between', 'Is Null', 'Is Not Null']
        ],
        date:[
            keys:['', 'Equal', 'NotEqual', 'LessThan', 'LessThanEquals', 'GreaterThan',
                'GreaterThanEquals', 'Between', 'IsNull', 'IsNotNull'],
            text:['', 'Equal To', 'Not Equal To', 'Less Than', 'Less Than or Equal To',
                'Greater Than', 'Greater Than or Equal To', 'Between', 'Is Null', 'Is Not Null']
        ],
        boolean:[
            keys:['', 'Equal', 'NotEqual', 'IsNull', 'IsNotNull'],
            text:['', 'Equal To', 'Not Equal To', 'Is Null', 'Is Not Null']
        ]
    ]
    /**
     * This tag is mainly for internal and test use.
     */
    def filterControl = { attrs, body ->
        out << createFilterControl(attrs, body)
    }
    
    /**
     * Creates a link (button) that displays the filter pane when pressed.  The title attribute
     * may be used to modify the text on the button.  If omitted, the text will be "Filter".
     */
    def filterButton = { attrs, body ->
        def title = attrs.title ?: 'Filter'
        out << "<a href=\"\" onclick=\"showElement('filterPane'); return false;\">${title}</a>"
    }

    /**
     * This tag generates necessary style and script includes.  Use this tag in the head element 
     * of your pages.  There are no attributes for this tag.
     */
    def filterPaneIncludes = {
        out << "<link rel=\"stylesheet\" type=\"text/css\" href=\"${createLinkTo(dir:pluginContextPath +'/css', file:'filter.css')}\" />\n"
        out << "<script type=\"text/javascript\" src=\"${createLinkTo(dir:pluginContextPath + "/js", file:'filter.js')}\"></script>"
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
     */
    def filterPane = { attrs, body ->
        def markup = new groovy.xml.MarkupBuilder(out)
        def formWriter = new StringWriter()
        def formBuilder = new groovy.xml.MarkupBuilder(formWriter)
        formBuilder.doubleQuotes = true
        if (attrs.filterBean && attrs.filterProperties) {
            def containerDivAttrs = ['class':'filterPane ', style:'display: none;', id:'filterPane']
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
			
            formBuilder.input(type:'hidden', name:'filterProperties', value:propsStr)
            formBuilder.table(cellspacing:'0', cellpadding:'0', class:'filterTable') {
                props.each { prop ->
                    def opName = "filter.op.${prop}"
                    def propName = "filter.${prop}"
                    def filterControlAttrs = [filterBean:bean, filterProperty:propName]
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
                            formWriter << this.select(name:opName, 
                                from:this.availableOpsByType[type].text, 
                                keys:this.availableOpsByType[type].keys, 
                                value:params[opName],
                                onclick:"filterOpChange('${opName}', '${filterControlAttrs['id']}');")
                        }
                        td('') {
                        	formWriter << this.createFilterControl(filterControlAttrs, '', (params[opName] != "IsNull" && params[opName] != "IsNotNull"))
                            if (type == "numeric" || type == "date") {
                               filterControlAttrs.name = propName + 'To'
                               filterControlAttrs.id = "${filterControlAttrs.id}To"
                               filterControlAttrs.value = params[filterControlAttrs.name]
                               boolean showToCtrl = filterControlAttrs.value != null && filterControlAttrs.value != "" && params[opName] == "Between"
                               span(style:(showToCtrl) ? "" : "display:none", id:"between-span-${prop}", '') {
                                    formWriter << '&nbsp;and&nbsp;'
                                    formWriter << this.createFilterControl(filterControlAttrs, '', true)
                               }
                            }
                        }
                    }
                }
                tr(valign:'middle') {
                    td('Order By')
                    td(colspan:'2', '') {
                        formWriter << this.select(name:"sort", from:readableProps, keys:props, noSelection:['':'Select a Field'], value:params['sort'])
                        formWriter << '&nbsp;'
                        formWriter << this.radio(name:"order", value:"asc", checked:params['order'] == 'asc', "&nbsp;Ascending&nbsp;")
                        formWriter << this.radio(name:"order", value:"desc", checked:params['order'] == 'desc', "&nbsp;Descending")
                    }
                }
            } // end form table.
                        
            formBuilder.div(class:"buttons") {
                span(class:"button") {
                    input(type:"button", value:"Cancel", onclick:"return hideElement('${containerDivAttrs.id}');")
                }
                span(class:"button") {
                    input(type:"button", value:"Clear", onclick:"return clearFilterPane('${filterFormName}');")
                }
                span(class:"button", '') {
                    formWriter << this.actionSubmit(action:action, value:"Apply")
                }
            } // end form button div.
	
            // Now create the main div.
            markup.div(containerDivAttrs) {
                h2("${filterPaneTitle}")
                this.out << this.form(name:filterFormName, action:action) { 
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
                    finalAttrs.putAll([from:valueList, value:params[attrs.filterProperty]])
                    stream = this.select(finalAttrs)
                } else {
                    def ctrlWriter = new StringWriter()
                    def markup = new groovy.xml.MarkupBuilder(ctrlWriter)
                    markup.doubleQuotes = true
                    finalAttrs.putAll([type:'text', value:attrs.value ? attrs.value : params[attrs.filterProperty]])
                    if (attrs.id) finalAttrs['id'] = attrs.id
                    if (attrs.maxlength) finalAttrs['maxlength'] = attrs.maxlength
                    if (attrs['size']) finalAttrs['size'] = attrs['size']
                    markup.input(finalAttrs)
                    stream = ctrlWriter.toString()
                }
            } else if (type == "Date") {
            	finalAttrs['value'] = FilterUtils.parseDateFromDatePickerParams(finalAttrs.name, params)
                println "Final date attrs are ${finalAttrs}"
                def spanStyle = (finalAttrs.style) ? "style=\"${finalAttrs.style}\"" : ""
                stream = "<span id=\"" + finalAttrs['id'] + "-container\" ${spanStyle}>" + this.datePicker(finalAttrs) + "</span>"
            } else if (type == "Boolean" || type == "boolean") {
                finalAttrs.putAll(labels:['Yes', 'No'], values:[true,false], value:params[attrs.filterProperty])
                stream = this.radioGroup(finalAttrs) { "<label for=\"${attrs.filterProperty}\">${it.label} </label>${it.radio}" }
            } else {
					
            }
        }
        return stream
    }
}