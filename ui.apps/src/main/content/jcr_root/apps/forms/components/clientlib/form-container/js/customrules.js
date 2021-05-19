$.validator.addMethod('captcha', function () {
  return validCaptcha();
}, "Please enter correct captcha.");

$.validator.addMethod('duplicateFileValidation', function (val, element) {
  return duplicateFileValidation(val);
}, "File with same name already exist.");

$.validator.addMethod('maxTotalFileSize', function () {
  return maxTotalFileSize();
}, "File exceeds the maximum total file size");

$.validator.addMethod("uploadFileSize", function (val, element) {
  if ($('input[type=file]:eq(0)')[0].files.length != 0) {
    var size = element.files[0].size;
    if (size > 10485760) {
      return false;
    } else {
      return true;
    }
  }
  return true;
}, "Please attach a file smaller than 10MB. If necessary, please zip the file.");

jQuery.validator.addMethod("validate_file_extension", function (value, element, param) {
  param = typeof param === "string" ? param.replace(/,/g, '|') : "png|jpe?g|gif|bmp|pdf|doc|docx|ppt|pptx|pps|ppsx|odt|xls|xlsx|csv|mp4|m4v|mov|wmv|avi|mpg|mp3|m4a|ogg|wav|key|zip|txt|srp|msg|xml|log|req|JPG|JPEG";
  return this.optional(element) || value.match(new RegExp(".(" + param + ")$", "i"));
}, "Please enter a value with a valid extension.");