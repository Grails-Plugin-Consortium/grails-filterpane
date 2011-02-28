<g:if test="${isFiltered == true}">
	<ul id="${id}" class="${styleClass}" style="${style}" title="${title}">
		<g:each in="${criteria}" var="c">
			<li>
				${c.fieldName} 
				<g:message code="fp.op.${c.filterOp}" default="${c.filterOp}" />
				"${c.filterValue}"
				<g:if test="${c.filterOp == 'between'}">
					and "${c.filterValueTo}""
				</g:if>
				<a href="${g.createLink(action:action,params:c.params)}" class="remove">
					<g:if test="${removeImgFile != null}">
						<img src="${g.resource(dir:removeImgDir, file:removeImgFile)}" alt="(X)" title="${g.message(code:'fp.currentCriteria.removeTitle', default:'Remove')}" />
					</g:if>
					<g:else>
						(X)
					</g:else>
				</a>
			</li>
		</g:each>
	</ul>
</g:if>