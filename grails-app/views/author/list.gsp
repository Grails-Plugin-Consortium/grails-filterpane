

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="main" />
        <title>Author List</title>
        <filterpane:includes />
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><a class="home" href="${createLinkTo(dir:'')}">Home</a></span>
            <span class="menuButton"><g:link class="create" action="create">New Author</g:link></span>
        </div>
        <div class="body">
            <h1>Author List</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <div class="list">
                <table>
                    <thead>
                        <tr>
                        
                   	        <g:sortableColumn property="id" title="Id" params="${filterParams}" />
                        
                   	        <g:sortableColumn property="firstName" title="First Name" params="${filterParams}" />
                        
                   	        <g:sortableColumn property="lastName" title="Last Name" params="${filterParams}" />
                        
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${authorList}" status="i" var="author">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                        
                            <td><g:link action="show" id="${author.id}">${author.id?.encodeAsHTML()}</g:link></td>
                        
                            <td>${author.firstName?.encodeAsHTML()}</td>
                        
                            <td>${author.lastName?.encodeAsHTML()}</td>
                        
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
            <div class="paginateButtons">
                <filterpane:paginate total="${authorCount}" domainBean="Author" />
                <filterpane:filterButton textKey="fp.tag.filterButton.text" appliedTextKey="fp.tag.filterButton.appliedText" text="Filter Me" appliedText="Change Filter" />
                <filterpane:isNotFiltered>Pure and Unfiltered!</filterpane:isNotFiltered>
                <filterpane:isFiltered>Filter Applied!</filterpane:isFiltered>
            </div>
            <filterpane:filterPane domainBean="Author"
                                   associatedProperties="books.title, books.bookType"
                                   titleKey="fp.tag.filterPane.titleText"/>
        </div>
    </body>
</html>
