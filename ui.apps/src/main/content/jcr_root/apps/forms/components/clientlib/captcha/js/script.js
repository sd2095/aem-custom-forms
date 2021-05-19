function recaptchaCallback() {
    var captchaResponse = $('#g-recaptcha-response').val();
    $.ajax({
        type: "GET",
        data: {
            captchaResponse: captchaResponse
        },
        url: "/bin/forms/captchaverification",
        dataType: "json",
        success: function (result) {
            var res = result.success;
            if (!res) {
                grecaptcha.reset();
                $('#' + fieldId + '-error').removeClass('fts-hidden');
                $('#' + fieldId + '-error').text(captchaerrormsg);
            } else {
                $('#' + fieldId + '-error').hide();
            }
        },
        error: function (jqXHR, textStatus, errorThrown) {
            grecaptcha.reset();
            $('#' + fieldId + '-error').removeClass('fts-hidden');
            $('#' + fieldId + '-error').text(captchaerrormsg);
        }
    });
}

var captchId="";
$(document).ready(function () {
    $('button[type="submit"]').on('click', function (e) {
        if (ContextHub && !ContextHub.getItem('userData') && typeof grecaptcha != "undefined" && (grecaptcha.getResponse() == '' || grecaptcha.getResponse() == undefined)) {
            $('#' + fieldId + '-error').show();
            $('#' + fieldId + '-error').text(captchaRequiredMsg);
            if ($(".validationform").valid())
				e.preventDefault();
        }
    });
	captchId = $('.captcha-box').find('input[type="text"]').attr('id');
    captcha();
});

function captcha() {
    var alpha = new Array('A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z');
    var i, a, code = '';
    for (i = 0; i < 7; i++) {
        a = alpha[Math.floor(Math.random() * alpha.length)];
        code += (' ' + a);
    }
    $("#mainCaptcha").attr('data-unselectable', code);
}

function validCaptcha() {
    var string1 = $('#mainCaptcha').attr('data-unselectable').replace(/ /g,'');
    var string2 = document.getElementById(captchId).value.replace(/ /g,'');
    if (string1 == string2) {
        $(".captcha-submit").attr('disabled', false);
		return true;
    } else {
        $(".captcha-submit").attr('disabled', true);
        return false;
    }
}