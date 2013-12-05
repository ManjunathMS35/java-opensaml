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

package org.opensaml.profile.action.impl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.logic.Constraint;

import org.opensaml.messaging.context.MessageContext;
import org.opensaml.messaging.encoder.MessageEncoder;
import org.opensaml.messaging.encoder.MessageEncodingException;
import org.opensaml.messaging.handler.MessageHandler;
import org.opensaml.messaging.handler.MessageHandlerException;
import org.opensaml.profile.ProfileException;
import org.opensaml.profile.action.AbstractProfileAction;
import org.opensaml.profile.action.ActionSupport;
import org.opensaml.profile.action.EventIds;
import org.opensaml.profile.context.ProfileRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action that encodes an outbound response from the outbound {@link MessageContext}. 
 * 
 * <p>
 * If the supplied instance of {@link MessageEncoder} is not already initialized, this action will
 * handle supplying the message context to encode via {@link MessageEncoder#setMessageContext(MessageContext)}, 
 * followed by invoking {@link MessageEncoder#initialize()}. If the encoder is already initialized,
 * these operations will be skipped.
 * </p>
 *
 * 
 * @event {@link EventIds#PROCEED_EVENT_ID}
 * @event {@link EventIds#INVALID_MSG_CTX}
 * @event {@link EventIds#UNABLE_TO_ENCODE}
 * 
 * @post If ProfileRequestContext.getOutboundMessageContext() != null, it will be injected and
 * encoded.
 * @post The injected {@link MessageEncoder} is destroyed.
 */
public class EncodeMessage extends AbstractProfileAction {

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(EncodeMessage.class);

    /** The {@link MessageDecoder} instance used to decode the incoming message. */
    @Nonnull private final MessageEncoder encoder;
    
    /**
     * An optional {@link MessageHandler} instance to be invoked after 
     * {@link MessageEncoder#prepareContext()} and prior to {@link MessageEncoder#encode()}.
     */
    @Nullable private final MessageHandler messageHandler;
    
    /** The outbound MessageContext to encode. */
    @Nullable private MessageContext msgContext;

    /**
     * Constructor.
     * 
     * 
     * @param messageEncoder the {@link MessageEncoder} used for the outbound response
     */
    public EncodeMessage(@Nonnull final MessageEncoder messageEncoder) {
        this(messageEncoder, null);
    }
    
    /**
     * Constructor.
     * 
     * <p>
     * The supplied {@link MessageHandler} will be invoked on the {@link MessageContext} after 
     * {@link MessageEncoder#prepareContext()}, and prior to invoking {@link MessageEncoder#encode()}.
     * Its use is optional and primarily used for transport/binding-specific message handling, 
     * as opposed to more generalized message handling operations which would typically be invoked 
     * earlier than this action.  For more details see {@link MessageEncoder}.
     * </p>
     * 
     * @param messageEncoder the {@link MessageEncoder} used for the outbound response
     * @param handler the {@link MessageHandler} used for the outbound response
     */
    public EncodeMessage(@Nonnull final MessageEncoder messageEncoder, @Nullable final MessageHandler handler) {
        encoder = Constraint.isNotNull(messageEncoder, "MessageEncoder cannot be null");
        messageHandler = handler;
    }

    /** {@inheritDoc} */
    @Override
    protected boolean doPreExecute(@Nonnull final ProfileRequestContext profileRequestContext) throws ProfileException {
        
        msgContext = profileRequestContext.getOutboundMessageContext();
        if (msgContext == null) {
            log.debug("{} Outbound message context was null, skipping action", getLogPrefix());
            ActionSupport.buildEvent(profileRequestContext, EventIds.INVALID_MSG_CTX);
            
            // TODO: do we want to destroy the encoder here?
            encoder.destroy();
            return false;
        }
        
        return true;
    }
    
    /** {@inheritDoc} */
    @Override
    protected void doExecute(@Nonnull final ProfileRequestContext profileRequestContext) throws ProfileException {
        try {
            log.debug("{} Encoding outbound response using message encoder of type {} for this response",
                    getLogPrefix(), encoder.getClass().getName());

            if (!encoder.isInitialized()) {
                log.debug("{} Encoder was not initialized, injecting MessageContext and initializing", getLogPrefix());
                encoder.setMessageContext(msgContext);
                encoder.initialize();
            } else {
                log.debug("{} Encoder was already initialized, skipping MessageContext injection and init",
                        getLogPrefix());
            }
            
            encoder.prepareContext();
            
            if (messageHandler != null) {
                log.debug("{} Invoking message handler of type {} for this response", getLogPrefix(), 
                        messageHandler.getClass().getName());
                messageHandler.invoke(msgContext);
            }
            
            encoder.encode();
            
            log.debug("{} Outbound response encoded from a message of type {}", getLogPrefix(),
                    msgContext.getMessage().getClass().getName());
            
        } catch (MessageEncodingException | ComponentInitializationException | MessageHandlerException e) {
            log.error(getLogPrefix() + "{} Unable to encode outbound response", e);
            ActionSupport.buildEvent(profileRequestContext, EventIds.UNABLE_TO_ENCODE);
        } finally {
            // TODO: do we want to destroy the encoder here?
            encoder.destroy();
        }
    }
    
}