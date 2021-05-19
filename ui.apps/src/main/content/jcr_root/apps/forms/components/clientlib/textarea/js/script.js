var fileId = "";
$(document).ready(function () {
    fileId = $('.fts-file').find('input[type="file"]').attr('id');
    //onClickAttachment();
});

/*
function onClickAttachment() {
    $(document).on('click', '#addAttachment', function () {
        $("#attachments-size-limit-error").html("");
        $("#" + fileId).val('').trigger('click');
    });
}
*/

var totalFilesize = 0;
var attachementsObj = [];
var attachmentData = [];
var fileArr = new Array();
var attachArr = new Array();

function viewAttachment(input) {

    if (input.files && input.files[0]) {

        attachmentData = [];
        var reader = new FileReader();
        var fileName = $("#" + fileId).val();
        var size = $("#" + fileId)[0].files[0].size;
        var fileNameWithoutPath = fileName.substr(fileName.lastIndexOf('\\') + 1);
        var sizeInMB = (size / 1048576).toFixed(1) + ' MB';
        totalFilesize = totalFilesize + size;
        $("#" + fileId).valid();
        reader.onload = function (e) {
            var fileName = $("#" + fileId).val().split('\\').pop();
            if ($("#" + fileId).valid()) {
                var filetitle = fileNameWithoutPath.split('.')[fileNameWithoutPath.split('.').length - 2];
                var displayAttachment = '<div data-size="' + size + '" id="' + filetitle + '" class="display__file-attachment"> <span class="display__file-name" id="fileNameDetails">' + fileName + " (" + sizeInMB + ")" + '</span><span class="attachment-delete primary-link greyClear" id="' + (new Date).getTime() + '" onclick="removeAttachment(this.id)"> <img src="/etc/designs/myrefinitiv/clientlibs/icons/grey/clear-outline.svg" alt="clear"></span><div>';
                $('#feed__attachments-list').append(displayAttachment);
                var encodedBaseUrl = e.target.result;
                var attachfile = encodedBaseUrl;
                attachArr.push(attachfile);
                var item = new Object();
                item.filename = fileName;
                item.filetitle = filetitle;
                item.data = attachfile;
                attachementsObj.push(item);
                $.each(attachementsObj, function (i, item) {
                    attachmentData.push(item.data);
                });
                fileArr.push(fileName);
                $('#attachmentId').val(JSON.stringify(attachmentData));
                $('#attachArr').val(JSON.stringify(attachArr));
                $('#fileArr').val(JSON.stringify(fileArr));
            } else {
                totalFilesize = totalFilesize - size;
            }
            $("#" + fileId).val('');
        };
        reader.readAsDataURL(input.files[0]);
    }
}

function removeAttachment(elementid) {
    $('body').on('click', '#' + elementid, function (event) {
        var $this = $(this),
            filename = "",
            thistag = $this.parent().attr('id');
        if (attachementsObj) {
            attachementsObj = attachementsObj.filter(function (obj) {
                if (obj.filetitle == thistag) {
                    filename = obj.filename;
                    filedata = obj.data;
                }
                return obj.filetitle !== thistag;
            });
            $.each(attachementsObj, function (i, item) {
                attachmentData.push(item.data);
            });
            fileArr = fileArr.filter(function (value) {
                return value !== filename;
            });
            attachArr = attachArr.filter(function (value) {
                return value !== filedata;
            });
            if (attachementsObj.length == 0) {
                $("#" + fileId).val('');
                attachmentData = [];
                fileArr = [];
                attachArr = [];
            }
            $('#attachmentId').val(JSON.stringify(attachmentData));
            $('#attachArr').val(JSON.stringify(attachArr));
            $('#fileArr').val(JSON.stringify(fileArr));
        }
        totalFilesize = totalFilesize - $this.parent().data('size');
        $(this).parent().remove();
        $("#" + fileId).valid();
    });
}
function maxTotalFileSize() {
    return (totalFilesize < 10485760);
}
function duplicateFileValidation(fileName) {
    if (fileArr.indexOf(fileName) != -1) {
        $("#" + fileId).val('');
        return false;
    } else {
        return true;
    }
}
