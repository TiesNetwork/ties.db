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
package network.tiesdb.schema.impl.ethereum;

import network.tiesdb.context.api.TiesSchemaConfig;
import network.tiesdb.context.api.annotation.TiesConfigElement;
import network.tiesdb.schema.api.TiesSchemaFactory;
import network.tiesdb.schema.impl.ethereum.impl.TiesSchemaEthereumConfig;

@TiesConfigElement({ TiesSchemaConfigImpl.BINDING, TiesSchemaConfigImpl.SHORT_BINDING })
public class TiesSchemaConfigImpl implements TiesSchemaConfig, TiesSchemaEthereumConfig {

    static final String BINDING = "network.tiesdb.schema.Ethereum";
    static final String SHORT_BINDING = "SchemaEthereum";

    private String web3ProviderEndpointUrl;
    private String tiesDBContractAddress;

    public void setTiesDBContractAddress(String tiesDBContractAddress) {
        this.tiesDBContractAddress = tiesDBContractAddress;
    }

    public void setContractAddress(String tiesDBContractAddress) {
        this.tiesDBContractAddress = tiesDBContractAddress;
    }

    public void setWeb3ProviderEndpointUrl(String web3ProviderEndpointUrl) {
        this.web3ProviderEndpointUrl = web3ProviderEndpointUrl;
    }

    public void setEndpointUrl(String web3ProviderEndpointUrl) {
        this.web3ProviderEndpointUrl = web3ProviderEndpointUrl;
    }

    public TiesSchemaConfigImpl() {
        // NOP Is not empty config values
    }

    public TiesSchemaConfigImpl(String value) {
        // NOP If this constructor is called then config values is empty and we
        // should use default
    }

    @Override
    public TiesSchemaFactory getTiesSchemaFactory() {
        return new TiesSchemaFactoryImpl(this);
    }

    @Override
    public String getWeb3ProviderEndpointUrl() {
        return web3ProviderEndpointUrl;
    }

    @Override
    public String getTiesDBContractAddress() {
        return tiesDBContractAddress;
    }

}
