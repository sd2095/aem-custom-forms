(function ($, $document) {
    "use strict";

    $(document).on("foundation-contentloaded", function () {
        if ($(".cq-dialog-floating").length == 0) {
            $(document).trigger("dialog-ready");
        }
    });

    $(document).on("dialog-ready", function (e) {
        var optionSrcRadio = $("coral-radio[name='./optionsSource']");
        var contentPathInput = $("input[name='./contentPath']");
        var optionsMultifield = $("coral-multifield[data-granite-coral-multifield-name='./options']");
        var groupingMultifield = $("coral-multifield[data-granite-coral-multifield-name='./groupedoptions']");
        var groupingCheckbox = $("coral-checkbox[name='./grouping']");
        var designSelection = $('#editor-styleselector-form coral-selectlist coral-selectlist-group').find(".is-selected");

        showHideOptionsType($('coral-radio[name="./optionsSource"][checked]').val());
        changeDialogStructure();
        optionSrcRadio.on("change", function (e) {
            showHideOptionsType(e.target.value);
        });

        function showHideOptionsType(optionsType) {
            if (optionsType == "static") {
                contentPathInput.parents(".coral-Form-fieldwrapper").hide();
                checkGroupingOption();
                groupingCheckbox.on("change", function (e) {
                    checkGroupingOption();
                });
            } 
            else if (optionsType == "genericlist") {
                optionsMultifield.parents(".coral-Form-fieldwrapper").hide();
                groupingMultifield.parents(".coral-Form-fieldwrapper").hide();
                contentPathInput.parents(".coral-Form-fieldwrapper").show();

            }
        }

        function checkGroupingOption() {
            if (groupingCheckbox.attr("checked")) {
                optionsMultifield.parents(".coral-Form-fieldwrapper").hide();
                groupingMultifield.parents(".coral-Form-fieldwrapper").show();
            } 
            else {
                optionsMultifield.parents(".coral-Form-fieldwrapper").show();
                groupingMultifield.parents(".coral-Form-fieldwrapper").hide();
            }
        }

        function changeDialogStructure() {
            if (designSelection.text() == "Radio list") {
                groupingCheckbox.attr("checked", "true");
                $("coral-checkbox input[name='./grouping']").click(function () {
                    return false;
                });
            } 
            else {
                groupingCheckbox.removeAttr("checked");
                groupingCheckbox.hide();
            }
            checkGroupingOption();
        }
    });

})($, $(document));