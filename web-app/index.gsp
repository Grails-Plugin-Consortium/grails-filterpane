<html>
  <head>
    <title>Welcome to Filtered Grails</title>
    <meta name="layout" content="main" />
  </head>
  <body>
    <h1>Welcome to <i>Filtered</i> Grails</h1>
    <p style="margin-left:20px;width:80%">This plugin adds filtering capabilities to any Grails application.  The primary goals of this plugin include:</p>
    <ul style="margin-left:20px;width:80%">
    	<li>Easy integration with Grails list pages via custom tags</li>
    	<li>Smart operator display.  Only practical filter operations are available for a property's data type.  (e.g. no "like" operators for numeric properties)</li>
    	<li>Support for major operators including =, &lt;&gt;, &gt;, &lt;, &gt;=, &lt;=, like, ilike, between, is null, and is not null.</li>
    	<li>Smart filter value entry.  Date properties display a date picker, boolean's display radio buttons, etc.</li>
    	<li>Support for a custom list of values, such as a filtering a text property with a constrained list of values.</li>
    	<li>Works with Grails list sorting out of the box</li>
    	<li>Works with Grails pagination out of the box</li>
    </ul>
    
      <p style="margin-left:20px;width:60%;margin-top: 15px;font-size:16px"><g:link controller="book">View the Example Book List</g:link></p>
    <br/>
    <h2>Steps to Add a Filter Pane to a List Page</h2>
    <!--
    <h3 style="margin: 10px auto 10px auto">Add the required files to your project</h3>
    <ul>
      <li class="tree"><pre>
        |- grails-app
        |     |
        |     +- services
        |     |     |
        |     |     +- FilterService.groovy
        |     |
        |     +- taglib
        |     |     |
        |     |     +-- FilterTagLib.groovy
        |     |
        |     +- utils
        |           |
        |           +-- FilterUtils.groovy
        |
        |- web-app
        |     |
        |     +--- css
        |     |     |
        |     |     +- filter.css
        |     |
        |     +--- js
        |           |
        |           +- filter.js
      </pre></li>
    </ul>
    <br />-->
    
    <h3>list.gsp</h3>
    <ol>
      <li>
        <p>Add the following to the header of your list.gsp page:</p>
        <pre>
        &lt;filterpane:filterPaneIncludes />
        </pre>
      </li>
      <li>
        <p>Somewhere near the bottom of your list page (typically just before the end of the "body" tag), define the filter pane div.</p>
        <pre>
        &lt;filterpane:filterPane filterPaneId="filterPane" filterBean="${DomainClass}" filterProperties="${['propertyOne', 'propertyTwo']}" />
	</pre>
      </li>
      <li><p>Add a button to display the filter pane somewhere on your list page.  Inside the pagination div or in the nav menu works well.<br/>
	A custom tag is provided to help create this button.  The title attribute is optional.  If omitted, the value of the button will be "Filter".</p>
        <pre>	
&lt;g:filterButton title="Whatever" />
        </pre>
	Here is an example of the filter button placed next to pagination buttons: <img src="${createLinkTo(dir:'images',file:'filter_btn.jpg')}" alt="example Filter button" align="middle" /><br />
	<p>Please note that if your application uses scriptaculous and you use the filterButton tag, the filter div will fade in and out.  Otherwise the filter div simply appears and disappears.</p>
      </li>
    </ol>
    <br/>
    
    <h3>Controller</h3>
    <ol style="margin-bottom:50px">
      <li>
        <p>Inject the service into your controller.</p>
        <pre>
        def filterService
        </pre>
      </li>
      <li>
        <p>Create the filter action in your controller.</p>
        <pre>
        def filter = {
                if(!params.max) params.max = 10
                render( view:'list', model:[ domainClassList: filterService.filter( params, DomainClass ), params:params ] )
        }
        </pre>
        Where...
        <ul>
        	<li><i>domainClassList</i> is the name of the return list model the list action uses (e.g. <b>bookList</b>)</li>
        	<li><i>DomainClass</li> is the name of the domain class you are filtering (e.g. <b>Book</b>).</li>
        </ul>
      </li>
    </ol>
  </body>
</html>