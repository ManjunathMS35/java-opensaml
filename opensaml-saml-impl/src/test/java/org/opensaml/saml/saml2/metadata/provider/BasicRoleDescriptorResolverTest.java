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

package org.opensaml.saml.saml2.metadata.provider;

import java.util.Collections;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.utilities.java.support.component.ComponentValidationException;
import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import net.shibboleth.utilities.java.support.resolver.ResolverException;

import org.junit.Assert;
import org.opensaml.core.xml.XMLObjectBaseTestCase;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.criterion.EntityIdCriterion;
import org.opensaml.saml.criterion.EntityRoleCriterion;
import org.opensaml.saml.criterion.ProtocolCriterion;
import org.opensaml.saml.ext.saml2mdquery.AttributeQueryDescriptorType;
import org.opensaml.saml.metadata.MetadataResolver;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.RoleDescriptor;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 */
public class BasicRoleDescriptorResolverTest extends XMLObjectBaseTestCase {
    
    private BasicRoleDescriptorResolver roleResolver;
    
    @BeforeMethod
    public void setUp() {
        final EntityDescriptor entityDescriptor = buildTestDescriptor();
        
        MetadataResolver metadataResolver = new MetadataResolver() {
            public void validate() throws ComponentValidationException {}
            
            @Nullable public String getId() { return "foo"; }
            
            @Nullable
            public EntityDescriptor resolveSingle(CriteriaSet criteria) throws ResolverException {
                return entityDescriptor;
            }
            
            @Nonnull
            public Iterable<EntityDescriptor> resolve(CriteriaSet criteria) throws ResolverException {
                return Collections.singletonList(entityDescriptor);
            }
        };
        
        roleResolver = new BasicRoleDescriptorResolver(metadataResolver);
    }
    
    @Test
    public void testResolveSingleNoProtocol() throws ResolverException {
        RoleDescriptor roleDescriptor = roleResolver.resolveSingle(new CriteriaSet(
                new EntityIdCriterion("http://www.example.org"), 
                new EntityRoleCriterion(SPSSODescriptor.DEFAULT_ELEMENT_NAME)));
       Assert.assertNotNull("Resolved RoleDescriptor was null", roleDescriptor);
       Assert.assertEquals("Saw incorrect role type", SPSSODescriptor.DEFAULT_ELEMENT_NAME, 
               roleDescriptor.getElementQName());
    }
    
    @Test
    public void testResolveMultiNoProtocol() throws ResolverException {
        Iterable<RoleDescriptor> roleDescriptors = roleResolver.resolve(new CriteriaSet(
                new EntityIdCriterion("http://www.example.org"), 
                new EntityRoleCriterion(SPSSODescriptor.DEFAULT_ELEMENT_NAME)));
       Assert.assertNotNull("Resolved RoleDescriptor iterable was null", roleDescriptors);
       
       int count = 0;
       for (RoleDescriptor roleDescriptor : roleDescriptors) {
           Assert.assertEquals("Saw incorrect role type", SPSSODescriptor.DEFAULT_ELEMENT_NAME, 
                   roleDescriptor.getElementQName());
           count++;
       }
       
       Assert.assertEquals("Resolved unexpected number of RoleDescriptors", 2, count);
    }
    
    @Test
    public void testResolveSingleWithProtocol() throws ResolverException {
        RoleDescriptor roleDescriptor = roleResolver.resolveSingle(new CriteriaSet(
                new EntityIdCriterion("http://www.example.org"), 
                new EntityRoleCriterion(SPSSODescriptor.DEFAULT_ELEMENT_NAME),
                new ProtocolCriterion(SAMLConstants.SAML20P_NS)));
        
       Assert.assertNotNull("Resolved RoleDescriptor was null", roleDescriptor);
       Assert.assertEquals("Saw incorrect role type", SPSSODescriptor.DEFAULT_ELEMENT_NAME, 
               roleDescriptor.getElementQName());
       Assert.assertTrue("Returned RoleDescriptor didn't support specified protocol", 
               roleDescriptor.getSupportedProtocols().contains(SAMLConstants.SAML20P_NS));
    }
    
    @Test
    public void testResolveMultiWithProtocol() throws ResolverException {
        Iterable<RoleDescriptor> roleDescriptors = roleResolver.resolve(new CriteriaSet(
                new EntityIdCriterion("http://www.example.org"), 
                new EntityRoleCriterion(SPSSODescriptor.DEFAULT_ELEMENT_NAME),
                new ProtocolCriterion(SAMLConstants.SAML20P_NS)));
       Assert.assertNotNull("Resolved RoleDescriptor iterable was null", roleDescriptors);
       
       int count = 0;
       for (RoleDescriptor roleDescriptor : roleDescriptors) {
           Assert.assertEquals("Saw incorrect role type", SPSSODescriptor.DEFAULT_ELEMENT_NAME, 
                   roleDescriptor.getElementQName());
           Assert.assertTrue("Returned RoleDescriptor didn't support specified protocol", 
                   roleDescriptor.getSupportedProtocols().contains(SAMLConstants.SAML20P_NS));
           count++;
       }
       
       Assert.assertEquals("Resolved unexpected number of RoleDescriptors", 1, count);
    }
    
    // Helper methods
    
    private EntityDescriptor buildTestDescriptor() {
        EntityDescriptor entityDescriptor = buildXMLObject(EntityDescriptor.DEFAULT_ELEMENT_NAME);
        
        SPSSODescriptor spssoDescriptor1 = buildXMLObject(SPSSODescriptor.DEFAULT_ELEMENT_NAME);
        spssoDescriptor1.addSupportedProtocol(SAMLConstants.SAML11P_NS);
        entityDescriptor.getRoleDescriptors().add(spssoDescriptor1);
        
        AttributeQueryDescriptorType aqDescriptor = buildXMLObject(AttributeQueryDescriptorType.TYPE_NAME);
        aqDescriptor.addSupportedProtocol(SAMLConstants.SAML20P_NS);
        entityDescriptor.getRoleDescriptors().add(aqDescriptor);
        
        SPSSODescriptor spssoDescriptor2 = buildXMLObject(SPSSODescriptor.DEFAULT_ELEMENT_NAME);
        spssoDescriptor2.addSupportedProtocol(SAMLConstants.SAML20P_NS);
        entityDescriptor.getRoleDescriptors().add(spssoDescriptor2);
        
        return entityDescriptor;
    }


}
