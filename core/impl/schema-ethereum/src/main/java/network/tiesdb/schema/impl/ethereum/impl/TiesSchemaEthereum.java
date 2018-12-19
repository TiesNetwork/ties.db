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
package network.tiesdb.schema.impl.ethereum.impl;

import static network.tiesdb.util.Safecheck.nullreplace;

import java.io.IOException;
import java.math.BigInteger;

import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGasPrice;
import org.web3j.protocol.core.methods.response.EthGetCode;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.ReadonlyTransactionManager;
import org.web3j.tx.TransactionManager;
import org.web3j.utils.Numeric;

import com.tiesdb.schema.impl.SchemaImpl;
import com.tiesdb.schema.impl.contracts.TiesDB;

import network.tiesdb.exception.TiesConfigurationException;
import network.tiesdb.schema.api.TiesSchema;

public class TiesSchemaEthereum implements TiesSchema {

    private static final short ETHEREUM_NETWORK_ID = 0x3c; // slip-0044.Ether

    private final TiesDB contract;

    public TiesSchemaEthereum(TiesSchemaEthereumConfig ethereumConfig) throws IOException, TiesConfigurationException {

        Web3j web3j = Web3j.build(new HttpService(nullreplace(ethereumConfig.getWeb3ProviderEndpointUrl(), HttpService.DEFAULT_URL)));
        EthGasPrice gasPrice = web3j.ethGasPrice().send();
        TransactionManager tm = new ReadonlyTransactionManager(web3j, "0x0000000000000000000000000000000000000000");
        TiesDB contract = TiesDB.load(ethereumConfig.getTiesDBContractAddress(), web3j, tm, gasPrice.getGasPrice(), BigInteger.ZERO);

        if (!contract.isValid()) {
            EthGetCode ethGetCode = web3j.ethGetCode(ethereumConfig.getTiesDBContractAddress(), DefaultBlockParameterName.LATEST).send();
            if (ethGetCode.hasError()) {
                throw new TiesConfigurationException("Contract request error: " + ethGetCode.getError());
            }
            String code = Numeric.cleanHexPrefix(ethGetCode.getCode()).toUpperCase();
            String contractCode = Numeric.cleanHexPrefix(contract.getContractBinary()).toUpperCase();
            throw new TiesConfigurationException("Invalid contract address: contract binary missmatch\n" //
                    + "    Code: " + code + "\n" //
                    + "    Was not found in: " + contractCode );
        }

        this.contract = contract;

    }

    @Override
    public Tablespace getTablespace(String name) {
        return TablespaceImpl.newInstance(new SchemaImpl(contract).getTablespace(name));
    }

    @Override
    public short getSchemaNetwork() {
        return ETHEREUM_NETWORK_ID;
    }

}
