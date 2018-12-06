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
package com.tiesdb.coordinator.bootstrap;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import network.tiesdb.context.api.TiesServiceConfig;
import network.tiesdb.context.api.TiesContext;
import network.tiesdb.context.api.TiesContextFactory;
import network.tiesdb.exception.TiesConfigurationException;

/**
 * TiesDB context wrapper.
 * 
 * <P>
 * Contains some utility methods for TiesDB context handling.
 * 
 * @author Anton Filatov (filatov@ties.network)
 */
public class TiesContextHandler {

    private static final Logger logger = LoggerFactory.getLogger(TiesContextHandler.class);

    public static interface TiesContextHandlerFactory {
        TiesContextHandler createHandler(TiesContextFactory contextFactory, URL contextUrl) throws TiesConfigurationException;
    }

    private static TiesContextHandlerFactory factory = new TiesContextHandlerFactory() {
        @Override
        public TiesContextHandler createHandler(TiesContextFactory contextFactory, URL contextUrl) throws TiesConfigurationException {
            return new TiesContextHandler(contextFactory, contextUrl);
        }
    };

    public static TiesContextHandlerFactory getFactory() {
        return factory;
    }

    public static void setFactory(TiesContextHandlerFactory newFactory) {
        if (null == newFactory) {
            throw new NullPointerException("The newFactory should not be null");
        }
        factory = newFactory;
    }

    private final TiesContext context;

    protected TiesContextHandler(TiesContextFactory contextService, URL contextUrl) throws TiesConfigurationException {
        this(initContext(contextService, contextUrl));
    }

    public TiesContextHandler(TiesContext context) {
        if (null == context) {
            throw new NullPointerException("The context should not be null");
        }
        this.context = context;
    }

    public TiesServiceConfig getConfig(String name) {
        return context.getConfig().get(name);
    }

    protected static TiesContext initContext(TiesContextFactory contextService, URL contextUrl) throws TiesConfigurationException {
        if (null == contextService) {
            throw new NullPointerException("The contextService should not be null");
        }
        if (null == contextUrl) {
            throw new NullPointerException("The contextUrl should not be null");
        }
        TiesContext context = null;
        try (InputStream is = contextUrl.openStream()) {
            context = contextService.readContext(is);
        } catch (IOException e) {
            throw new TiesConfigurationException("Context initialization failed", e);
        }
        if (null == context) {
            throw new TiesConfigurationException("TiesDB Service settings could not be read from " + contextUrl);
        }
        logger.debug("TiesDB Service settings read from {}", contextUrl);
        if (null == context.getConfig() || context.getConfig().isEmpty()) {
            throw new TiesConfigurationException("TiesDB Service settings configuration is missing");
        }
        return context;
    }

    public Set<String> getConfigsNames() {
        return context.getConfig().keySet();
    }

    public TiesContext getDelegate() {
        return context;
    }
}
