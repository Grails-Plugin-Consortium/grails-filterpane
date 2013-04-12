<g:if test="${pageProperty(name: 'page.filterpane.filterbutton')}">
  <g:pageProperty name="page.filterpane.filterbutton"/>
</g:if>
<g:else>
  <a href=""
     onclick="grailsFilterPane.showElement('${filterPaneId}');return false;"
     class="${styleClass}"
     style="${style}">${text}</a>
</g:else>