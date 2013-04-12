<g:if test="${pageProperty(name: 'page.filterpane.datecontrol')}">
  <g:pageProperty name="page.filterpane.datecontrol"/>
</g:if>
<g:else>
  <span id="${ctrlAttrs.id}-container" style="${ctrlAttrs.style}">
    <%=g.datePicker(ctrlAttrs)%>
    <g:if test="${ctrlAttrs.name?.endsWith('To')}">
      <input type="hidden"
             name="filter.${ctrlAttrs.domain}.${ctrlAttrs.propertyName}_isDayPrecision"
             value="${ctrlAttrs.isDayPrecision}"/>
    </g:if>
  </span>
</g:else>
