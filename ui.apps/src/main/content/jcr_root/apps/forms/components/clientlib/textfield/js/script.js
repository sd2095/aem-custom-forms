$(document).ready(function () {
    $('.fts-form-field-input').focus(function () {
        if($(this).parents().hasClass('fts-formTextfield--v3')) {
        	$(this).siblings('.fts-formTextfield-label').addClass('fts-form-field-label--focused');
        }
    });

    $('.fts-form-field-input').blur(function () {
        var inputValue = $(this).val();
        if (inputValue == "") {
            $(this).parents('.fts-form__group').removeClass('focused');
        	$(this).siblings('.fts-formTextfield-label').removeClass('fts-form-field-label--focused');
        }
    });
});