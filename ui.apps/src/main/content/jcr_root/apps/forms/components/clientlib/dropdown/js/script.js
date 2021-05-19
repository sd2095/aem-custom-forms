function ftsDropdownInit(ftsDropdownProps) {
  var query = {};
  var ftsDropdownElm = $("#" + ftsDropdownProps.fieldId);

  function addIconElement(elm) {
    var iconElm = document.createElement("span");
    $(iconElm)
      .addClass("select2-selection__arrow")
      .attr("role", "presentation");
    elm.append(iconElm);
  }

  function markMatch(text, term) {
    // Find where the match is
    var match = text.toUpperCase().indexOf(term.toUpperCase());

    var $result = $("<span></span>");

    // If there is no match, move on
    if (match < 0) {
      return $result.text(text);
    }

    // Put in whatever text is before the match
    $result.text(text.substring(0, match));

    // Mark the match
    var $match = $("<b></b>");
    $match.text(text.substring(match, match + term.length));

    // Append the matching text
    $result.append($match);

    // Put in whatever is after the match
    $result.append(text.substring(match + term.length));

    return $result;
  }

  function templateResult(item) {
    // No need to template the searching text
    if (item.loading) {
      return item.text;
    }

    if (
      item.element &&
      item.element.tagName.toLowerCase() === "optgroup" &&
      (!item.text || !ftsDropdownProps.grouping)
    ) {
      return null;
    }

    if (ftsDropdownProps.multipleSelection && item.selected) {
      return null;
    }

    var term = query.term || "";
    var $result = markMatch(item.text, term);

    if (item.icon) {
      $result = $(
        "<span><span class='fts-icon'><span class='fts-icon-inner'>" +
          item.icon +
          "</span></span>" +
          $result.html() +
          "</span>"
      );
    }
    return $result;
  }

  function initEvents() {
    var iconParent = ftsDropdownElm
      .parent()
      .find(".select2-selection.select2-selection--multiple");
    var icons = ftsDropdownElm.parent().find("#fts-dropdown-icons").html();
    addIconElement(iconParent);
    ftsDropdownElm
      .parent()
      .find(".select2-selection__arrow[role='presentation']")
      .html(icons)
      .find(".fts-dropdown-upArrow")
      .removeClass("fts-hidden");
    ftsDropdownElm.val().length > 0 &&
      ftsDropdownElm.closest(".fts-dropdown").hasClass("fts-dropdown--v2") &&
      ftsDropdownElm
        .closest(".fts-form__group")
        .addClass("fts-form-field-label--focused");

    ftsDropdownElm.on("select2:opening select2:closing", function () {
      if (!ftsDropdownProps.enableTypeAhead) {
        var $searchfield = $(this).parent().find(".select2-search__field");
        $searchfield.prop("disabled", true);
      }
    });
    ftsDropdownElm.on("select2:opening", function () {
      var iconsParent = $(this)
        .parent()
        .find(".select2-selection__arrow[role='presentation']");
      ftsDropdownElm.closest(".fts-dropdown").hasClass("fts-dropdown--v2") &&
        $(this)
          .closest(".fts-form__group")
          .addClass("fts-form-field-label--focused");
      iconsParent.find(".fts-dropdown-upArrow").addClass("fts-hidden");
      iconsParent.find(".fts-dropdown-downArrow").removeClass("fts-hidden");
    });
    ftsDropdownElm.on("select2:closing", function () {
      var iconsParent = $(this)
        .parent()
        .find(".select2-selection__arrow[role='presentation']");
      
        $(this).val().length === 0 &&
        $(this).closest(".fts-dropdown").hasClass("fts-dropdown--v2") &&
        $(this)
          .closest(".fts-form__group")
          .removeClass("fts-form-field-label--focused");
      iconsParent.find(".fts-dropdown-upArrow").removeClass("fts-hidden");
      iconsParent.find(".fts-dropdown-downArrow").addClass("fts-hidden");
    });
    ftsDropdownElm.on("select2:selecting", function (e) {
      if (ftsDropdownProps.useAsLink) {
        e.preventDefault();
        var url = e.params.args.data.url || "#";
        var anchorElm = document.createElement("a");
        anchorElm.setAttribute("href", url);
        anchorElm.click();
        $("#" + ftsDropdownProps.fieldId).select2("close");
      }
    });
    ftsDropdownElm.on("change.select2", function () {
      if(ftsDropdownElm.val().length > 0 && ftsDropdownElm.parent().find('label.error').text()) {
        ftsDropdownElm.removeClass("error");
        ftsDropdownElm.parent().find('label.error').hide();
      }
      else if(ftsDropdownElm.val().length === 0 && ftsDropdownElm.parent().find('label.error').text()) {
		ftsDropdownElm.addClass("error");
        ftsDropdownElm.parent().find('label.error').show();
      }
    });
  }

  ftsDropdownProps.ajaxSuccess = function (res) {
    var data = res.data;
    var placeholder = ftsDropdownProps.multipleSelection
      ? ftsDropdownProps.placeholder
      : '<span class="fts-dropodwn-placeholder--text">' +
        ftsDropdownProps.placeholder +
        "</span>";
    ftsDropdownElm.select2({
      dropdownParent: $("#" + ftsDropdownProps.parentId),
      dropdownCssClass: ftsDropdownProps.multipleSelection
        ? "fts-dropdown--multiple"
        : "fts-dropdown--single",
      multiple: ftsDropdownProps.multipleSelection,
      data: data,
      placeholder: placeholder,
      minimumResultsForSearch: ftsDropdownProps.enableTypeAhead ? 0 : Infinity,
	  maximumSelectionLength : ftsDropdownProps.maxSelectionLength || 10,
	  templateResult : templateResult,
      language: {
        searching: function (params) {
          // Intercept the query as it is happening
          query = params;

          // Change this to be appropriate for your application
          return "Searching…";
        },
      },
      escapeMarkup: function (markup) {
        return markup;
      },
    });
    initEvents();
  };
}

function dropdownFilter (parentVal, childId){
		
	var childData = $('#'+childId).data('select2').options.options.data;
	var filteredData = "<option></option>";
	
	for(var i=0; i< childData.length ; i++){
		
		if(childData[i].parent === parentVal){

			  filteredData = filteredData.concat("<option value=\""+childData[i].id+"\">"+childData[i].text+"</option>");               
		}
	}
	$('#'+childId).html(filteredData);
		
}