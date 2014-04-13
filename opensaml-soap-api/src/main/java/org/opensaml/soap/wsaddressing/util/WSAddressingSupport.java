/*
 * Licensed to the University Corporation for Advanced Internet Development, 
 * Inc. (UCAID) under one or more contributor license agreements.  See the 
 * NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The UCAID licenses this file to You under the Apache 
 * License, Version 2.0 (the "License"); you may not use this file except in 
 * compliance with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opensaml.soap.wsaddressing.util;

import net.shibboleth.utilities.java.support.primitive.StringSupport;

import org.opensaml.core.xml.AttributeExtensibleXMLObject;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.schema.XSBooleanValue;
import org.opensaml.soap.wsaddressing.IsReferenceParameterBearing;

import com.google.common.base.Objects;

/**
 * Helper methods for working with WS-Addressing.
 */
public final class WSAddressingSupport {

    /**
     * Private constructor.
     */
    private WSAddressingSupport() {
    }

    /**
     * Adds a <code>wsa:IsReferenceParameter</code> attribute to the given SOAP object.
     * 
     * @param soapObject the SOAP object to add the attribute to
     * @param isReferenceParameter whether IsReferenceParameter is true or false
     */
    public static void addWSAIsReferenceParameter(XMLObject soapObject, boolean isReferenceParameter) {
        if (soapObject instanceof IsReferenceParameterBearing) {
            ((IsReferenceParameterBearing)soapObject).setWSAIsReferenceParameter(
                    new XSBooleanValue(isReferenceParameter, false));
        } else if (soapObject instanceof AttributeExtensibleXMLObject) {
            ((AttributeExtensibleXMLObject)soapObject).getUnknownAttributes()
                .put(IsReferenceParameterBearing.WSA_IS_REFERENCE_PARAMETER_ATTR_NAME, 
                        new XSBooleanValue(isReferenceParameter, false).toString());
        } else {
            throw new IllegalArgumentException(
                    "Specified object was neither IsReferenceParameterBearing nor AttributeExtensible");
        }
    }

    /**
     * Get the <code>wsa:IsReferenceParameter</code> attribute from a given SOAP object.
     * 
     * @param soapObject the SOAP object to add the attribute to
     * 
     * @return value of the IsReferenceParameter attribute, or false if not present
     */
    public static boolean getWSAIsReferenceParameter(XMLObject soapObject) {
        if (soapObject instanceof IsReferenceParameterBearing) {
            XSBooleanValue value = ((IsReferenceParameterBearing)soapObject).isWSAIsReferenceParameterXSBoolean();
            if (value != null) {
                return value.getValue();
            }
        }
        if (soapObject instanceof AttributeExtensibleXMLObject) {
            String valueStr = StringSupport.trimOrNull(((AttributeExtensibleXMLObject)soapObject)
                    .getUnknownAttributes().get(IsReferenceParameterBearing.WSA_IS_REFERENCE_PARAMETER_ATTR_NAME)); 
            return Objects.equal("1", valueStr) || Objects.equal("true", valueStr);
        }
        return false;
    }
    
}