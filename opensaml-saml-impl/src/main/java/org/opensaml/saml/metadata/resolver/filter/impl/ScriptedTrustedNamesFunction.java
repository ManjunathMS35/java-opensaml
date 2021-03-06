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

package org.opensaml.saml.metadata.resolver.filter.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.script.ScriptContext;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;

import net.shibboleth.utilities.java.support.annotation.constraint.NotEmpty;
import net.shibboleth.utilities.java.support.logic.Constraint;
import net.shibboleth.utilities.java.support.resource.Resource;
import net.shibboleth.utilities.java.support.scripting.EvaluableScript;

import org.opensaml.core.xml.XMLObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;

/**
 * A scripted {@link Function} which can be injected into
 * {@link SignatureValidationFilter#setDynamicTrustedNamesStrategy(Function)}.
 * 
 */
public class ScriptedTrustedNamesFunction implements Function<XMLObject, Set<String>> {

    /** The default language is JavaScript. */
    @Nonnull @NotEmpty public static final String DEFAULT_ENGINE = "JavaScript";

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(ScriptedTrustedNamesFunction.class);

    /** The script we care about. */
    @Nonnull private final EvaluableScript script;

    /** The custom object we can get inject into all scripts. */
    @Nullable private Object customObject;

    /** Debugging info. */
    @Nullable private final String logPrefix;

    /**
     * Constructor.
     * 
     * @param theScript the script we will evaluate.
     * @param extraInfo debugging information.
     */
    protected ScriptedTrustedNamesFunction(@Nonnull final EvaluableScript theScript, @Nullable final String extraInfo) {
        script = Constraint.isNotNull(theScript, "Supplied script cannot be null");
        logPrefix = "Scripted Function from " + extraInfo + " :";
    }

    /**
     * Constructor.
     * 
     * @param theScript the script we will evaluate.
     */
    protected ScriptedTrustedNamesFunction(@Nonnull final EvaluableScript theScript) {
        script = Constraint.isNotNull(theScript, "Supplied script should not be null");
        logPrefix = "Anonymous Scripted Function :";
    }

    /**
     * Return the custom (externally provided) object.
     * 
     * @return the custom object
     */
    @Nullable public Object getCustomObject() {
        return customObject;
    }

    /**
     * Set the custom (externally provided) object.
     * 
     * @param object the custom object
     */
    @Nullable public void setCustomObject(final Object object) {
        customObject = object;
    }

    /** {@inheritDoc} */
    @Override public Set<String> apply(@Nullable final XMLObject context) {

        final SimpleScriptContext scriptContext = new SimpleScriptContext();
        scriptContext.setAttribute("custom", getCustomObject(), ScriptContext.ENGINE_SCOPE);
        scriptContext.setAttribute("profileContext", context, ScriptContext.ENGINE_SCOPE);

        try {
            final Object output = script.eval(scriptContext);
            return (Set<String>) output;

        } catch (final ScriptException e) {
            log.error("{} Error while executing Function script", logPrefix, e);
            return null;
        }
    }

    /**
     * Factory to create {@link ScriptedTrustedNamesFunction} for {@link SignatureValidationFilter}s from a
     * {@link Resource}.
     * 
     * @param engineName the language
     * @param resource the resource to look at
     * @return the function
     * @throws ScriptException if the compile fails
     * @throws IOException if the file doesn't exist.
     */
    @Nonnull static ScriptedTrustedNamesFunction resourceScript(@Nonnull @NotEmpty final String engineName,
            @Nonnull final Resource resource) throws ScriptException, IOException {
        try (final InputStream is = resource.getInputStream()) {
            final EvaluableScript script = new EvaluableScript(engineName, is);
            return new ScriptedTrustedNamesFunction(script, resource.getDescription());
        }
    }

    /**
     * Factory to create {@link ScriptedTrustedNamesFunction} from a {@link Resource}.
     * 
     * @param resource the resource to look at
     * @return the function
     * @throws ScriptException if the compile fails
     * @throws IOException if the file doesn't exist.
     */
    @Nonnull static ScriptedTrustedNamesFunction resourceScript(@Nonnull final Resource resource)
            throws ScriptException, IOException {
        return resourceScript(DEFAULT_ENGINE, resource);
    }

    /**
     * Factory to create {@link ScriptedTrustedNamesFunction} for {@link SignatureValidationFilter}s from inline data.
     * 
     * @param scriptSource the script, as a string
     * @param engineName the language
     * @return the function
     * @throws ScriptException if the compile fails
     */
    @Nonnull static ScriptedTrustedNamesFunction inlineScript(@Nonnull @NotEmpty final String engineName,
            @Nonnull @NotEmpty final String scriptSource) throws ScriptException {
        final EvaluableScript script = new EvaluableScript(engineName, scriptSource);
        return new ScriptedTrustedNamesFunction(script, "Inline");
    }

    /**
     * Factory to create {@link ScriptedTrustedNamesFunction} for {@link SignatureValidationFilter}s from inline data.
     * 
     * @param scriptSource the script, as a string
     * @return the function
     * @throws ScriptException if the compile fails
     */
    @Nonnull static ScriptedTrustedNamesFunction inlineScript(@Nonnull @NotEmpty final String scriptSource)
            throws ScriptException {
        return inlineScript(DEFAULT_ENGINE, scriptSource);
    }

}