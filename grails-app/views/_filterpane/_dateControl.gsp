<span id="${ctrlAttrs.id}-container" style="${ctrlAttrs.style}">
    <filterpane:datePicker ctrlAttrs="${ctrlAttrs}" />

    <g:if test="${ctrlAttrs.name?.endsWith('To')}">
        <input type="hidden"
               name="filter.${ctrlAttrs.domain}.${ctrlAttrs.propertyName}_isDayPrecision"
               value="${ctrlAttrs.isDayPrecision}"/>
    </g:if>
</span>
