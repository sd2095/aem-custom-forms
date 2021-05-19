(function ($, $document) {
    "use strict";

    $(document).on("foundation-contentloaded", function(){
        if ($(".cq-dialog-floating").length == 0) {
            $(document).trigger("dialog-ready");
        }
    });

    $(document).on("dialog-ready", function(e) {

        var analyticsSwitch = $("coral-switch[name='./formAnalytics']");
        var formReasonTxt = $("input[name='./formReason']");
        var formTypeTxt = $("input[name='./formType']");
        var triggerInput = $("coral-multifield[data-granite-coral-multifield-name='./formAnalyticsTrigger']");
		var inbuiltFormAction = $("coral-radio[name='./formActionType']");
        var txtarea = $("textarea[name='./script']");
        var clientlib = $('input[name="./clientLibName"]');
        var servleturl = $('input[name="./servletUrl"]');
        var advancedControl = $("coral-switch[name='./prePopulate']");
        var formsrc = $("coral-radio[name='./formsrc']");
        var urlmap = $("coral-multifield[data-granite-coral-multifield-name='./param_map']");
        var successModal = $("coral-multifield[data-granite-coral-multifield-name='./success_modal_config']");
		var errorModal =  $("coral-multifield[data-granite-coral-multifield-name='./error_modal_config']");

        toggleAnalyticsSection();
        showHideOptionsType($('coral-radio[name="./formActionType"][checked]').val());
        toggleAdvancedControls();

        analyticsSwitch.on("change", function() {

			toggleAnalyticsSection();

        });

        inbuiltFormAction.on("change", function(e) {

            showHideOptionsType(e.target.value);
        });

        advancedControl.on("change", function() {

			toggleAdvancedControls();

        });


        function toggleAnalyticsSection() { 

            if(analyticsSwitch.attr('checked')){

                formReasonTxt.parents(".coral-Form-fieldwrapper").show();
                formTypeTxt.parents(".coral-Form-fieldwrapper").show();
                triggerInput.parents(".coral-Form-fieldwrapper").show();

            }
            else{

                formReasonTxt.parents(".coral-Form-fieldwrapper").hide();
                formTypeTxt.parents(".coral-Form-fieldwrapper").hide();
                triggerInput.parents(".coral-Form-fieldwrapper").hide();

            }


        }   

        function toggleAdvancedControls(){

            if(advancedControl.attr('checked')) {

                formsrc.parents(".coral-Form-fieldwrapper").show();
                urlmap.parents(".coral-Form-fieldwrapper").show();
                formsrc.parents(".coral-RadioGroup").attr("aria-required","true");
                urlmap.attr("aria-required", "true");

            }

            else{

                formsrc.parents(".coral-Form-fieldwrapper").hide();
                urlmap.parents(".coral-Form-fieldwrapper").hide();
                formsrc.parents(".coral-RadioGroup").removeAttr("aria-required");
                urlmap.removeAttr("aria-required");
            }
        }


        function showHideOptionsType(optionsType){            

            if(optionsType == "inbuilt") {

                removeReqAttr();
                addReqAttr(servleturl);   
                successModal.parents(".coral-Form-fieldwrapper").show();
                errorModal.parents(".coral-Form-fieldwrapper").show();
            } 
            else if(optionsType == "jscript") {

                removeReqAttr();
                addReqAttr(clientlib);

            }    
            else if(optionsType == "redit"){

                removeReqAttr();
                addReqAttr(servleturl);
                addReqAttr(txtarea);
            }
            else {

                removeReqAttr();
            }
        }

        function addReqAttr(element) {

            element.attr("aria-required", "true");
            element.parents(".coral-Form-fieldwrapper").show();

        }

        function removeReqAttr(){

            txtarea.removeAttr("aria-required");
            clientlib.removeAttr("aria-required");
            servleturl.removeAttr("aria-required");
            txtarea.parents(".coral-Form-fieldwrapper").hide();
            clientlib.parents(".coral-Form-fieldwrapper").hide();
            servleturl.parents(".coral-Form-fieldwrapper").hide();
            successModal.parents(".coral-Form-fieldwrapper").hide();
            errorModal.parents(".coral-Form-fieldwrapper").hide();
        }

    });

})($, $(document));