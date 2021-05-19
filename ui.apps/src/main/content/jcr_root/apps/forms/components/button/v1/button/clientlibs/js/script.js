(function ($, $document) {
    "use strict";

    $(document).on("dialog-ready", function() {
        var buttontype = $("coral-radio[name='./buttonType']");
        buttonTypeFunction();
        $("coral-radio[name='./buttonType']").on("change", function() {
			buttonTypeFunction();
        });

        function buttonTypeFunction() {
            var buttonTypeCheck = $("input[name='./buttonType']:checked").val();
            if(buttonTypeCheck == 'submit') {
				buttontype.parents('.coral-Form-fieldwrapper').siblings().find("input[name='./linkpath']").parent('.coral-Form-fieldwrapper').attr("style", "display: none !important");
                buttontype.parents('.coral-Form-fieldwrapper').siblings().find("input[name='./linkpath']").val('');
                buttontype.parents('.coral-Form-fieldwrapper').siblings().find("input[name='./openInNewWindow']").parents('.coral-Form-fieldwrapper').attr("style", "display: none !important");
                buttontype.parents('.coral-Form-fieldwrapper').siblings().find("input[name='./openInNewWindow']").removeAttr('checked')
            }
            else {
               	buttontype.parents('.coral-Form-fieldwrapper').siblings().find("input[name='./linkpath']").parent('.coral-Form-fieldwrapper').show();
                buttontype.parents('.coral-Form-fieldwrapper').siblings().find("input[name='./openInNewWindow']").parents('.coral-Form-fieldwrapper').show();
            }
        }
	});

})($, $(document));