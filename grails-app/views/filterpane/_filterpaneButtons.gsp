<div class="buttons">
    <span class="button">
        <input type="button" 
               value="${cancelText}" 
               onclick="return grailsFilterPane.hideElement('${containerId}');" />
    </span>
    <span class="button">
        <input type="button" 
               value="${clearText}" 
               onclick="return grailsFilterPane.clearFilterPane('${formName}');" />
    </span>
    <span class="button">
    	<g:actionSubmit value="${applyText}" action="${action}" />
    </span>
</div>