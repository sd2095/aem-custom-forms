function logAnalyticsFormStart(formName, formProduct, formReason, formSuccessType, errorMsg) {
    updateFormAttributes(formName, formProduct, formReason, "", "");
    if (digitalData && PubSub) {        
        PubSub.publish('analytics.formStart', digitalData.page.form);
    }
}

function logAnalyticsFormSuccess(formName, formProduct, formReason, formSuccessType, errorMsg) {    
    updateFormAttributes(formName, formProduct, formReason, formSuccessType, "");
    if (PubSub) {
        PubSub.publish('analytics.formSuccess', digitalData.page.form);
    }
}

function logAnalyticsFormSubmit(formName, formProduct, formReason, formSuccessType, errorMsg) {
    updateFormAttributes(formName, formProduct, formReason, "", "");
    if (!isFormSubmitted && PubSub) {
        isFormSubmitted = true;
        PubSub.publish('analytics.formSubmit', digitalData.page.form);
    }
}

function logAnalyticsFormError(formName, formProduct, formReason, formSuccessType, errorMsg) {
    updateFormAttributes(formName, formProduct, formReason, "", errorMsg);
    if (PubSub && errorMsg) {
        PubSub.publish('analytics.formError', digitalData.page.form);
    }
    formErrors=[];
}


function updateFormAttributes(formName, formProduct, formReason, formSuccessType, formErrors) {
    if (digitalData && digitalData.page) {
        form.formName = formName;
        form.formReason = 'product:' + formProduct + '|reason:' + formReason;
        form.formErrors=formErrors;
        form.formSuccessType=formSuccessType;
        digitalData.page.form=form;
    }
}

$.urlParam = function(name){
    var results = new RegExp('[\?&]' + name + '=([^&#]*)').exec(window.location.href);
    if (results == null || results == ''){
       return false;
    }
    else {
       return decodeURI(results[1]);
    }
}

function validateParam(param) {

  var str_arr = ["*","?","=","#","!","`","~"]; 
  if(param){
      for(var i = 0; i < str_arr.length ; i++){
        if(param.indexOf(str_arr[i]) >= 0){
            param = "";
            break;
        }    
      }   
      return param;
  }
  else{
	return "";
  }
}
function getContextHubData(){

    if(ContextHub){

        if(ContextHub.getItem("userData") && ContextHub.getItem("userData").userProfile){
			userDataCntxtHub = ContextHub.getItem("userData");
        }
        if(ContextHub.getItem("products")){
			productDataCntxtHub = ContextHub.getItem("products");
        }
    }
}

function captureCaseNumber(caseNumber,caseID) {
	if (digitalData) {
          digitalData["user"]["case"] = {};  
          var caseDetailsArr = [];
          caseDetailsArr = [{
              caseID: caseID,
              caseNumber: caseNumber
          }];
      digitalData["user"]["case"]["caseDetails"]= caseDetailsArr;
  }
}