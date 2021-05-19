(function(document, $) {
    "use strict";
    $(document).on("foundation-contentloaded", function(){
        if ($(".cq-dialog-floating").length == 0) {
            $(document).trigger("dialog-ready");
        }
    });

    $(document).on("dialog-ready", function(e) {

        function showHideOptionsType(optionsType){
            var $genlistInput = $('.select-options-path');
            var $staticlistInput = $('.select-options-static');

            if(optionsType == "genericlist") {
                $genlistInput.show();
                $staticlistInput.hide();
            } 
            else if(optionsType == "static") {
				$staticlistInput.show();
                $genlistInput.hide();
            }    
            else {
				$staticlistInput.hide();
                $genlistInput.hide();
            }
        }

        function showHideTooltip(){
            var $tooltip = $("input[name='./tooltipIcon']").prop('checked');
            if($tooltip){
                $("input[name='./tooltipIcon']").parents('.coral-Form-fieldwrapper').siblings().find("input[name='./tooltipId']").parent().show();
                $("input[name='./tooltipIcon']").parents('.coral-Form-fieldwrapper').siblings().find("input[name='./tooltipText']").parent().show();
            }
            else{
                $("input[name='./tooltipIcon']").parents('.coral-Form-fieldwrapper').siblings().find("input[name='./tooltipId']").parent().attr("style", "display: none !important");
                $("input[name='./tooltipIcon']").parents('.coral-Form-fieldwrapper').siblings().find("input[name='./tooltipText']").parent().attr("style", "display: none !important");
            }
        }
		
		function showHideMultiResultSet() {

            var $authpropelement = $("coral-checkbox[name='./multipleSource']").prop('checked');
            if($authpropelement){
				$("input[name='./authajaxendpoint']").parents(".coral-Form-fieldwrapper").show();
                $("input[name='./authajaxendpoint']").attr("aria-required","true");
            }
            else{
                $("input[name='./authajaxendpoint']").removeAttr("aria-required");
				$("input[name='./authajaxendpoint']").parents(".coral-Form-fieldwrapper").attr("style", "display: none !important");
            }

        }

        showHideOptionsType($('coral-radio[name="./optionsSource"][checked]').val());
        showHideTooltip();
		showHideMultiResultSet();

        $("coral-radio[name='./optionsSource']").on("change", function(e) {
            showHideOptionsType(e.target.value);
        });
        $("input[name='./tooltipIcon']").change(function() {
            showHideTooltip();
        });
		$("coral-checkbox[name='./multipleSource']").change(function() {
            showHideMultiResultSet();
        });

    });

})(document,$);
