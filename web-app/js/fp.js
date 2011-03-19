var grailsFilterPane = {
	showElement : function(id) {
		try {
			if (typeof Effect != "undefined"
					&& typeof Effect.Appear != "undefined") {
				if ($(id) && $(id).visible() == false) {
					Effect.Appear(id, {
						duration : 0.5,
						queue : 'end'
					});
				}
			} else {
				var el = document.getElementById(id);
				if (el && el.style.display == 'none') {
					el.style.display = 'block';
				}
			}
		} catch (err) {
			alert(err);
		}
		return false;
	},

	hideElement : function(id) {
		if (typeof Effect != "undefined" && typeof Effect.Fade != "undefined") {
			if ($(id) && $(id).visible() == true) {
				Effect.Fade(id, {
					duration : 0.5,
					queue : 'end'
				});
			}
		} else {
			var el = document.getElementById(id);
			if (el && el.style.display != 'none') {
				el.style.display = 'none';
			}
		}
		return false;
	},

	clearFilterPane : function(id) {
		var form = document.getElementById(id);
		var el;

		for ( var i = 0; i < form.elements.length; i++) {
			el = form.elements[i];
			if (el && el.name.indexOf('filter.') == 0) {
				if (el.type == 'select-one') {
					el.selectedIndex = 0;
				} else if (el.type == 'text' || el.type == 'textarea') {
					el.value = '';
				}
			}
		}
	},

	filterOpChange : function(id, controlId) {
		// id should be of the form op.propertyName
		if (id.slice(0, 10) == 'filter.op.') {
			var prop = id.substring(10);
			var el = document.getElementById(id);
			var selection = el.options[el.selectedIndex];
			if (el) {
				if (el.type == 'select-one') {
					if (selection.value == 'Between') {
						grailsFilterPane.showElement('between-span-' + prop
								+ 'To');
					} else {
						grailsFilterPane.hideElement('between-span-' + prop
								+ 'To');
					}
				}

				var containerName = prop + '-container';
				if (selection.value == 'IsNull'
						|| selection.value == 'IsNotNull') {
					grailsFilterPane.hideElement(controlId);
					// Take care of date picker fields we created.
					if (document.getElementById(containerName)) {
						grailsFilterPane.hideElement(containerName);
					}
				} else {
					grailsFilterPane.showElement(controlId);
					// Take care of date picker fields.
					if (document.getElementById(containerName)) {
						grailsFilterPane.showElement(containerName);
					}
				}
			}
		}
	},

	selectDefaultOperator : function(id) {
		var dropdown = document.getElementById(id);
		if (dropdown && dropdown.selectedIndex <= 0) {
			dropdown.selectedIndex = 1;
		}
	}
};