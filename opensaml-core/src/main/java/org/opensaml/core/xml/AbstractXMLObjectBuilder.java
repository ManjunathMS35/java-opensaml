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

package org.opensaml.core.xml;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.namespace.QName;

import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.xml.DomTypeSupport;

import org.w3c.dom.Element;

/**
 * Base implementation for XMLObject builders.
 * 
 * <strong>Note:</strong> This class only works with {@link org.opensaml.core.xml.AbstractXMLObject}s
 * 
 * @param <XMLObjectType> the XMLObject type that this builder produces
 */
public abstract class AbstractXMLObjectBuilder<XMLObjectType extends XMLObject> implements
        XMLObjectBuilder<XMLObjectType> {

    /** {@inheritDoc} */
    @Nonnull public XMLObjectType buildObject(@Nonnull final QName objectName){
        return buildObject(objectName.getNamespaceURI(), objectName.getLocalPart(), objectName.getPrefix());
    }
    
    /** {@inheritDoc} */
    @Nonnull public XMLObjectType buildObject(@Nonnull final QName objectName, @Nullable final QName schemaType){
        return buildObject(objectName.getNamespaceURI(), objectName.getLocalPart(), objectName.getPrefix(), schemaType);
    }
    
    /** {@inheritDoc} */
    @Nonnull public abstract XMLObjectType buildObject(@Nullable final String namespaceURI,
            @Nonnull @NotEmpty final String localName, @Nullable final String namespacePrefix);

    /** {@inheritDoc} */
    @Nonnull public XMLObjectType buildObject(@Nullable final String namespaceURI, @Nonnull final String localName,
            @Nullable final String namespacePrefix, @Nullable final QName schemaType) {
        XMLObjectType xmlObject;

        xmlObject = buildObject(namespaceURI, localName, namespacePrefix);
        ((AbstractXMLObject) xmlObject).setSchemaType(schemaType);

        return xmlObject;
    }

    /** {@inheritDoc} */
    @Nonnull public XMLObjectType buildObject(@Nonnull final Element element) {
        XMLObjectType xmlObject;

        String localName = element.getLocalName();
        String nsURI = element.getNamespaceURI();
        String nsPrefix = element.getPrefix();
        QName schemaType = DomTypeSupport.getXSIType(element);

        xmlObject = buildObject(nsURI, localName, nsPrefix, schemaType);

        return xmlObject;
    }
}