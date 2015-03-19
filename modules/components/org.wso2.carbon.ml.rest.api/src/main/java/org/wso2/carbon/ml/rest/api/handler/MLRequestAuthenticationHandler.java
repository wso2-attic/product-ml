/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.ml.rest.api.handler;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.configuration.security.AuthorizationPolicy;
import org.apache.cxf.jaxrs.ext.RequestHandler;
import org.apache.cxf.jaxrs.model.ClassResourceInfo;
import org.apache.cxf.message.Message;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.util.AnonymousSessionUtil;
import org.wso2.carbon.ml.rest.api.RestAPIConstants;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

public class MLRequestAuthenticationHandler implements RequestHandler{
	
	private static final Log logger = LogFactory.getLog(MLRequestAuthenticationHandler.class);
    private PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();

    /**
	  * Authenticate requests received at the ml endpoint, using HTTP basic-auth headers as the authentication
	  * mechanism. This method returns a null value which indicates that the request to be processed. 
	  */
	@Override
    public Response handleRequest(Message message, ClassResourceInfo resourceInfo) {
	    if(logger.isDebugEnabled()) {
	        logger.debug(String.format("Authenticating request: " + message.getId()));
        }
	    
        // If Mutual SSL is enabled
        HttpServletRequest request = (HttpServletRequest) message.get(RestAPIConstants.HTTP_REQUEST_HEADER);
        Object certObject = request.getAttribute(RestAPIConstants.CERTIFICATE_HEADER);
        
        // Extract auth header from the message.
        AuthorizationPolicy policy = message.get(AuthorizationPolicy.class);
        if (policy == null) {
            logger.error("Authentication header missing.");
            return Response.status(Response.Status.UNAUTHORIZED).type(MediaType.APPLICATION_JSON).entity(
                    "Authentication header missing.").build();
        }
        // Extract user credentials from the auth header.
        String username = policy.getUserName().trim();
        String password = policy.getPassword().trim();
        //sanity check
        if (StringUtils.isEmpty(username)) {
            logger.error("username is null/empty.");
            return Response.status(Response.Status.UNAUTHORIZED).type(MediaType.APPLICATION_JSON).entity("Username " +
                    "cannot be null").build();
        } else if (certObject == null && (StringUtils.isEmpty(password))) {
            logger.error("password is null/empty.");
            return Response.status(Response.Status.UNAUTHORIZED).type(MediaType.APPLICATION_JSON).entity("password " +
                    "cannot be null").build();
        }
        return Authenticate(certObject, username, password);
	}
	
	/**
	 * Authenticate with the user credentials.
	 * 
	 * @param certObject   Certificate object of the request
	 * @param username     Username
	 * @param password     Password
	 * @return             Response, if unauthorized. Null, if Authorized.
	 */
	private Response Authenticate(Object certObject, String username, String password){
	    carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        RealmService realmService = (RealmService)carbonContext.getOSGiService(RealmService.class, null);
        RegistryService registryService = (RegistryService) carbonContext.getOSGiService(RegistryService.class, null);
        String tenantDomain = MultitenantUtils.getTenantDomain(username);
        int tenantId;
        try {
            tenantId = realmService.getTenantManager().getTenantId(tenantDomain);
            UserRealm userRealm = null;
            if (certObject == null) {
                userRealm = AnonymousSessionUtil.getRealmByTenantDomain(registryService, realmService, tenantDomain);
                if (userRealm == null) {
                    logger.error("Invalid domain or unactivated tenant login");
                    return Response.status(Response.Status.UNAUTHORIZED).type(MediaType.APPLICATION_JSON).entity(
                            "Tenant not found").build();
                }
            }
            username = MultitenantUtils.getTenantAwareUsername(username);
            // if authenticated
            if (certObject != null || userRealm.getUserStoreManager().authenticate(username, password)) {
                // set the correct tenant info for downstream code.
                carbonContext.setTenantDomain(tenantDomain);
                carbonContext.setTenantId(tenantId);
                carbonContext.setUsername(username);
                return null;
            } else {
                logger.error(String.format("Authentication failed. Please check your username/password"));
                return Response.status(Response.Status.UNAUTHORIZED).type(MediaType.APPLICATION_JSON).entity(
                        "Authentication failed. Please check your username/password").build();
            }
        } catch (Exception e) {
            logger.error("Authentication failed: ", e);
            return Response.status(Response.Status.UNAUTHORIZED).type(MediaType.APPLICATION_JSON).entity(
                    "Authentication failed: ").build();
        }
	}
}
