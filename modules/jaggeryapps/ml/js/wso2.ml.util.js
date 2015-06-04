/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
 
function getParameterByName(name) {
    name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
    var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
    results = regex.exec(location.search);
    return results === null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
}

function getBaseUrl(fullUrl){
	var parts = fullUrl.split('/');
	return parts[0] + '//' + parts[2];
}
// function to build notifications
function buildNotification(message, notificationType) {
    var builtNotification;
    if(notificationType == 'warning') {
        builtNotification = 
        '<div class="alert alert-warning alert-dismissible" role="alert">' +
            '<button type="button" class="close" data-dismiss="alert" aria-label="Close"><span aria-hidden="true">&times;</span></button>' +
            '<img src="../../images/icons/ico-error.png" class="indi" />' +
            message +
        '</div>';            
    }
    else if(notificationType == 'info') {
        builtNotification = 
        '<div class="alert alert-info alert-dismissible" role="alert">' +
            '<button type="button" class="close" data-dismiss="alert" aria-label="Close"><span aria-hidden="true">&times;</span></button>' +
            '<img src="../../images/icons/ico-alert.png" class="indi" />' +
            message +
        '</div>';            
    }
    else if(notificationType == 'success') {
        builtNotification = 
        '<div class="alert alert-success alert-dismissible" role="alert">' +
            '<button type="button" class="close" data-dismiss="alert" aria-label="Close"><span aria-hidden="true">&times;</span></button>' +
            message +
        '</div>';            
    }
    return builtNotification;
}
// function to handle notifications
function handleNotification(notificationText, notificationArea, notificationType) {
    $(notificationArea).html(buildNotification(notificationText, notificationType));
}