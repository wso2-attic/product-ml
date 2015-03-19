/*
 * Copyright (c) 2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.ml.rest.api;


public class RestAPIConstants {

    public static final String RESOURCE_NOT_FOUND = "Resource not found : ";
    public static final String TYPE_JSON = "application/json";
    
    //Request Headers
    public static final String HTTP_REQUEST_HEADER = "HTTP.REQUEST";
    public static final String AUTHENTICATION_HEADER = "WWW-Authenticate";
    public static final String CERTIFICATE_HEADER = "javax.servlet.request.X509Certificate";
    
    // Authentication types
    public static final String BASIC_AUTHENTICATION = "Basic";
}
