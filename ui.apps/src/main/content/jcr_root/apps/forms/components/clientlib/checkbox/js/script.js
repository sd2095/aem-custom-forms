var isTermsSelect = false;
if(termsAndCondition) {
    $('body').on('click', ('#'+fieldIdValue), function (event) {
		checktermsvalidation();
    });
    $('body').on('click', ('.fts-checkbox a'), function (event) {
       var browserName = window.navigator.userAgent;
       isTermsSelect = true;       
       if (browserName.indexOf("MSIE ") != -1 || !navigator.userAgent.match(/Trident.*rv\:11\./)) {
           $('#'+fieldIdValue).prop('checked', true);
           checktermsvalidation();
       } else {
           $('#'+fieldIdValue).prop('checked', false);
       }
    });
}

function checktermsvalidation() {
    var termsCheck = $('#'+fieldIdValue).is(':checked');
    var fieldErrorElement = $('#'+fieldIdValue+'-error');

    if (termsCheck == false) {
        fieldErrorElement.addClass('fts-hidden');
        fieldErrorElement.removeClass('fts-show');
    } else {
        if (isTermsSelect == false) {
            fieldErrorElement.removeClass('fts-hidden');
            fieldErrorElement.addClass('fts-show');
            fieldErrorElement.text(tandcText);
        } else {
            fieldErrorElement.addClass('fts-hidden');
            fieldErrorElement.removeClass('fts-show');
        }
    }
}

$(window).on('load', function() {
    $('[type="checkbox"]').change(function () {
        if($(this).parents('.checkbox').hasClass('fts-checkbox--v3')) {
            var limitCheckBox = 1;
            if ($(this).parents('.fts-box-wrapper').parent().find('.check-checkbox:checked').length > limitCheckBox) {
            	this.checked = false;
        	}
            if ($(this).is(':checked')) {
                $(this).siblings('.fts-checkbox-inputfield').removeClass('fts-hidden');
            }
            else {
                $(this).siblings('.fts-checkbox-inputfield').addClass('fts-hidden');
            }
        }
    });
});