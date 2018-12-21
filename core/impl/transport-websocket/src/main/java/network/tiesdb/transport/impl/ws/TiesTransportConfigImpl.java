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
package network.tiesdb.transport.impl.ws;

import java.net.InetAddress;
import java.util.concurrent.TimeUnit;

import network.tiesdb.context.api.TiesHandlerConfig;
import network.tiesdb.context.api.TiesTransportConfig;
import network.tiesdb.context.api.annotation.TiesConfigElement;
import network.tiesdb.transport.api.TiesTransportFactory;
import network.tiesdb.transport.impl.ws.TiesTransportFactoryImpl;

/**
 * TiesDB transport configuration implementation.
 * 
 * @author Anton Filatov (filatov@ties.network)
 */
@TiesConfigElement({ TiesTransportConfigImpl.BINDING, TiesTransportConfigImpl.SHORT_BINDING })
public class TiesTransportConfigImpl implements TiesTransportConfig {

    static final String BINDING = "network.tiesdb.transport.WebSocket";
    static final String SHORT_BINDING = "TransportWebSocket";

    public static class TransportSecurityConfig {

        private boolean securedSocket = false;

        public boolean isSecuredSocket() {
            return securedSocket;
        }

        public void setSecuredSocket(boolean securedSocket) {
            this.securedSocket = securedSocket;
        }
    }

    private TiesHandlerConfig handler;

    private String serviceAddress = InetAddress.getLoopbackAddress().getHostAddress();
    private Integer servicePort = 0;
    private TransportSecurityConfig security = new TransportSecurityConfig();
    private Integer typeOfService = null;
    private Integer acceptorThreadsCount = 1;
    private Integer workerThreadsCount = Runtime.getRuntime().availableProcessors();
    private long idleReaderTime = 0;
    private long idleWriterTime = 0;
    private long idleTime = 180;
    private String idleTimeUnit = TimeUnit.SECONDS.name();

    public TiesTransportConfigImpl() {
        // NOP Is not empty config values
    }

    public TiesTransportConfigImpl(String value) {
        // NOP If this constructor is called then config values is empty and we
        // should use default
    }

    public TiesHandlerConfig getHandlerConfig() {
        return handler;
    }

    public void setHandler(TiesHandlerConfig handler) {
        this.handler = handler;
    }

    @Override
    public String getServiceAddress() {
        return serviceAddress;
    }

    public void setServiceAddress(String serviceAddress) {
        this.serviceAddress = serviceAddress;
    }

    @Override
    public Integer getServicePort() {
        return servicePort;
    }

    public void setServicePort(Integer servicePort) {
        this.servicePort = servicePort;
    }

    @Override
    public TiesTransportFactory getTiesTransportFactory() {
        return new TiesTransportFactoryImpl(this);
    }

    public TransportSecurityConfig getSecurity() {
        return security;
    }

    public void setSecurity(TransportSecurityConfig security) {
        this.security = security;
    }

    public Integer getTypeOfService() {
        return this.typeOfService;
    }

    public void setTypeOfService(Integer typeOfService) {
        this.typeOfService = typeOfService;
    }

    public Integer getWorkerThreadsCount() {
        return workerThreadsCount;
    }

    public void setWorkerThreadsCount(Integer workerThreadsCount) {
        this.workerThreadsCount = workerThreadsCount;
    }

    public Integer getAcceptorThreadsCount() {
        return acceptorThreadsCount;
    }

    public void setAcceptorThreadsCount(Integer acceptorThreadsCount) {
        this.acceptorThreadsCount = acceptorThreadsCount;
    }

    public long getIdleReaderTime() {
        return idleReaderTime;
    }

    public void setIdleReaderTime(long idleReaderTime) {
        this.idleReaderTime = idleReaderTime;
    }

    public long getIdleWriterTime() {
        return idleWriterTime;
    }

    public void setIdleWriterTime(long idleWriterTime) {
        this.idleWriterTime = idleWriterTime;
    }

    public long getIdleTime() {
        return idleTime;
    }

    public void setIdleTime(long idleTime) {
        this.idleTime = idleTime;
    }

    public String getIdleTimeUnit() {
        return idleTimeUnit;
    }

    public void setIdleTimeUnit(String idleTimeUnit) {
        this.idleTimeUnit = idleTimeUnit;
    }
}
