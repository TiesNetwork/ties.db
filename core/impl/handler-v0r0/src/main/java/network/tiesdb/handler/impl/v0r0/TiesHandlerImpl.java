/**
 * Copyright © 2017 Ties BV
 *
 * This file is part of Ties.DB project.
 *
 * Ties.DB project is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Ties.DB project is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with Ties.DB project. If not, see <https://www.gnu.org/licenses/lgpl-3.0>.
 */
package network.tiesdb.handler.impl.v0r0;

import java.io.IOException;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tiesdb.protocol.TiesDBProtocolManager;
import com.tiesdb.protocol.api.TiesDBProtocol;
import com.tiesdb.protocol.api.TiesDBProtocolHandler;
import com.tiesdb.protocol.api.TiesDBProtocolHandlerProvider;
import com.tiesdb.protocol.api.Version;
import com.tiesdb.protocol.exception.TiesDBException;
import com.tiesdb.protocol.exception.TiesDBProtocolException;
import com.tiesdb.protocol.v0r0.TiesDBProtocolV0R0;
import com.tiesdb.protocol.v0r0.TiesDBProtocolV0R0.Conversation;
import com.tiesdb.protocol.v0r0.reader.MessageReader;
import com.tiesdb.protocol.v0r0.reader.RequestReader;
import com.tiesdb.protocol.v0r0.writer.ResponseWriter;

import network.tiesdb.api.TiesVersion;
import network.tiesdb.context.api.TiesHandlerConfig;
import network.tiesdb.exception.TiesException;
import network.tiesdb.handler.api.TiesHandler;
import network.tiesdb.handler.impl.v0r0.controller.MessageController;
import network.tiesdb.handler.impl.v0r0.controller.RequestHandler;
import network.tiesdb.handler.impl.v0r0.controller.ServiceClientController;
import network.tiesdb.handler.impl.v0r0.util.StreamInput;
import network.tiesdb.handler.impl.v0r0.util.StreamOutput;
import network.tiesdb.service.api.TiesService;
import network.tiesdb.service.scope.api.TiesServiceScopeConsumer;
import network.tiesdb.service.scope.api.TiesServiceScopeException;
import network.tiesdb.transport.api.TiesInput;
import network.tiesdb.transport.api.TiesOutput;

/**
 * TiesDB handler implementation.
 * 
 * @author Anton Filatov (filatov@ties.network)
 */
public class TiesHandlerImpl implements TiesHandler, TiesDBProtocolHandler<TiesDBProtocolV0R0.Conversation>, TiesDBProtocolHandlerProvider {

    public class TiesDBProtocolRequestHandler implements TiesDBProtocolHandler<TiesDBProtocolV0R0.Conversation> {

        private final TiesServiceScopeConsumer consumer;

        public TiesDBProtocolRequestHandler(TiesServiceScopeConsumer consumer) {
            this.consumer = consumer;
        }

        @Override
        public void handle(Conversation session) throws TiesDBException {
            try (ServiceClientController scope = new ServiceClientController(service, session)) {
                consumer.accept(scope);
            } catch (TiesServiceScopeException | IOException e) {
                throw new TiesDBException("Service request failed", e);
            }
        }

    }

    private static final TiesHandlerImplVersion IMPLEMENTATION_VERSION = TiesHandlerImplVersion.v_0_0_1_prealpha;

    private static final Logger LOG = LoggerFactory.getLogger(TiesHandlerImpl.class);

    private final TiesService service;

    private final TiesHandlerConfigImpl config;

    private final Collection<TiesDBProtocol> protocols;

    private final MessageController messageController;

    public TiesHandlerImpl(TiesService service, TiesHandlerConfigImpl config) {

        if (null == config) {
            throw new NullPointerException("The config should not be null");
        }
        if (null == service) {
            throw new NullPointerException("The service should not be null");
        }

        this.service = service;
        this.config = config;

        this.protocols = TiesDBProtocolManager.getProtocols();
        if (protocols.isEmpty()) {
            throw new RuntimeException("No TiesDBProtocols found");
        }

        this.messageController = new MessageController(service);
    };

    @Override
    public TiesVersion getVersion() {
        return IMPLEMENTATION_VERSION;
    }

    @Override
    public TiesHandlerConfig getTiesHandlerConfig() {
        return config;
    }

    @Override
    public void handle(TiesServiceScopeConsumer consumer, final TiesOutput output) throws TiesException {
        for (TiesDBProtocol protocol : protocols) {
            try {
                final StreamOutput po = new StreamOutput(output.getOutputStream());
                LOG.debug("TiesDBProtocol selected {}", protocol);
                protocol.createChannel(po, new TiesDBProtocolHandlerProvider() {
                    @SuppressWarnings("unchecked")
                    @Override
                    public <S> TiesDBProtocolHandler<S> getHandler(Version localVersion, Version remoteVersion, S session)
                            throws TiesDBProtocolException {
                        if (!TiesDBProtocolV0R0.Conversation.class.isInstance(session)) {
                            throw new TiesDBProtocolException("Protocol negotiation was not implemented yet");
                        }
                        return (TiesDBProtocolHandler<S>) new TiesDBProtocolRequestHandler(consumer);
                    }
                });
                break;
            } catch (TiesDBException e) {
                LOG.error("Can't handle request {}", output, e);
                throw new TiesException(e.getMessage(), e);
            }
        }
    }

    @Override
    public void handle(final TiesInput tiesInput, final TiesOutput tiesOutput) throws TiesException {
        for (TiesDBProtocol protocol : protocols) {
            try {
                final StreamOutput po = new StreamOutput(tiesOutput.getOutputStream());
                final StreamInput pi = new StreamInput(tiesInput.getInputStream());
                LOG.debug("TiesDBProtocol selected {}", protocol);
                protocol.acceptChannel(pi, po, this);
                break;
            } catch (TiesDBException e) {
                LOG.error("Can't handle request {}", tiesInput, e);
                throw new TiesException(e.getMessage(), e);
            }
        }
    }

    @Override
    public void handle(Conversation session) throws TiesDBException {
        try {
            messageController.handle(session);
        } catch (Exception e) {
            throw new TiesDBException("Processing failed", e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <S> TiesDBProtocolHandler<S> getHandler(Version localVersion, Version remoteVersion, S session) throws TiesDBProtocolException {
        if (!TiesDBProtocolV0R0.Conversation.class.isInstance(session)) {
            throw new TiesDBProtocolException("Protocol negotiation was not implemented yet");
        }
        return (TiesDBProtocolHandler<S>) this;
    }

}
