(function ($, $document) {
    "use strict";
    $(document).on("dialog-ready", function(e) {
        if($('#editor-styleselector-form coral-selectlist coral-selectlist-group').find('.is-selected').text() == "Embedded Input"){
			$('.coral-Form-fieldwrapper').find("input[name='./fieldPlaceholder']").parent().attr("style", "display: none !important");
            $('.coral-Form-fieldwrapper').find("input[name='./fieldSubLabel']").parent().attr("style", "display: none !important");
        }
        else{
            $('.coral-Form-fieldwrapper').find("input[name='./fieldPlaceholder']").parent().show();
            $('.coral-Form-fieldwrapper').find("input[name='./fieldSubLabel']").parent().show();
        }
    });
})($, $(document));