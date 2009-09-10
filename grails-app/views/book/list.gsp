

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="main" />
        <title>Book List</title>
        <g:javascript library="prototype" />
        <g:javascript library="scriptaculous" />
        <filterpane:filterPaneIncludes />
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><a class="home" href="${createLinkTo(dir:'')}">Home</a></span>
            <span class="menuButton"><g:link class="create" action="create">New Book</g:link></span>
        </div>
        <div class="body">
            <h1>Book List</h1>
            <g:if test="${flash.message}">
                <div class="message">${flash.message}</div>
            </g:if>
            <filterpane:currentCriteria domainBean="com.zeddware.grails.plugins.filterpane.Book" dateFormat="${[title:'MM/dd/yyyy',releaseDate:'MMM dd, yyyy']}"
                removeImgDir="images" removeImgFile="bullet_delete.png" />
            <div class="list">
                <table>
                    <thead>
                        <tr>

                            <g:sortableColumn property="id" title="Id" params="${filterParams}" />
                            <g:sortableColumn property="title" title="Title" params="${filterParams}" />
                            <g:sortableColumn property="author" title="Author" params="${filterParams}" />
                            <g:sortableColumn property="bookType" title="Book Type" params="${filterParams}" />
                            <g:sortableColumn property="readPriority" title="Read Priority" params="${filterParams}" />
                            <g:sortableColumn property="releaseDate" title="Release Date" params="${filterParams}" />
                            <g:sortableColumn property="price" title="Price" params="${filterParams}" />
                            <g:sortableColumn property="inStock" title="In Stock" params="${filterParams}" />

                        </tr>
                    </thead>
                    <tbody>
                        <g:each in="${bookList}" status="i" var="book">
                            <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">

                                <td><g:link action="show" id="${book.id}">${book.id?.encodeAsHTML()}</g:link></td>
                                <td>${book.title?.encodeAsHTML()}</td>
                                <td>${book.author?.encodeAsHTML()}</td>
                                <td>${book.bookType.encodeAsHTML()}</td>
                                <td>${book.readPriority?.encodeAsHTML()}</td>
                                <td>${book.releaseDate?.encodeAsHTML()}</td>
                                <td>${book.price?.encodeAsHTML()}</td>
                                <td>${book.inStock?.encodeAsHTML()}</td>

                            </tr>
                        </g:each>
                    </tbody>
                </table>
            </div>
            <div class="paginateButtons">
                <filterpane:paginate total="${bookCount}" domainBean="Book" />
                <filterpane:filterButton textKey="fp.tag.filterButton.text" appliedTextKey="fp.tag.filterButton.appliedText" text="Filter Me" appliedText="Change Filter" />
                <filterpane:isNotFiltered>Pure and Unfiltered!</filterpane:isNotFiltered>
                <filterpane:isFiltered>Filter Applied!</filterpane:isFiltered>
            </div>
            <filterpane:filterPane domainBean="com.zeddware.grails.plugins.filterpane.Book"
                                   additionalProperties="identifier"
                                   associatedProperties="author.lastName,author.firstName,author.favoriteGenre,coAuthor.lastName"
                                   excludeProperties="cost"
                                   filterPropertyValues="${[releaseDate:[years:2015..1950,precision:'month'], bookType:[displayProperty:'display'], 'author.favoriteGenre':[displayProperty:'display']]}"
                                   titleKey="fp.tag.filterPane.titleText"/>
        </div>
    </body>
</html>
