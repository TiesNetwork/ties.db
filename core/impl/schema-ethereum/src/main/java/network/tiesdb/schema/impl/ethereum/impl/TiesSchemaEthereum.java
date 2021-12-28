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
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.SignatureException;
import java.util.Arrays;
import java.util.UUID;

import org.web3j.crypto.ECDSASignature;
import org.web3j.crypto.Hash;
import org.web3j.crypto.Keys;
import org.web3j.crypto.Sign;
import org.web3j.crypto.Sign.SignatureData;
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
import network.tiesdb.service.scope.api.TiesCheque;

public class TiesSchemaEthereum implements TiesSchema {

    private static final short ETHEREUM_NETWORK_ID = 0x3c; // slip-0044.Ether

    private final TiesDB contract;

    private final String nodeAddress;

    public TiesSchemaEthereum(TiesSchemaEthereumConfig ethereumConfig) throws IOException, TiesConfigurationException {

        this.nodeAddress = ethereumConfig.getTiesDBNodeAddress();

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
                    + "    Code: " + contractCode + "\n" //
                    + "    Was not found in: " + code);
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

    @Override
    public String getNodeAddress() {
        return this.nodeAddress;
    }

    @Override
    public String getContractAddress() {
        return this.contract.getContractAddress();
    }

    @Override
    public boolean isChequeValid(TiesCheque cheque) throws SignatureException {
        return validateChequeSignature( //
                cheque.getTablespaceName(), //
                cheque.getTableName(), //
                cheque.getChequeSession(), //
                cheque.getChequeNumber(), //
                cheque.getChequeCropAmount(), //
                cheque.getSigner(), //
                cheque.getSignature());
    }

    protected boolean validateChequeSignature( //
            String tablespaceName, //
            String tableName, //
            UUID session, //
            BigInteger number, //
            BigInteger cropAmount, //
            byte[] signer, //
            byte[] signature) throws SignatureException {

        if (signer.length != 20) {
            throw new SignatureException("Signer address length missmatch, expected 20 bytes and got " + signer.length);
        }
        SignatureData signatureData = getSignatureData(signature);
        byte[] tKey = getTkey(tablespaceName, tableName);

        ByteBuffer packedData = ByteBuffer.allocate(20 + 20 + 16 + 32 + 32 + 32);
        packedData.put(Numeric.hexStringToByteArray(this.contract.getContractAddress())); // 20 bytes
        packedData.put(signer); // 20 bytes
        packedData.putLong(session.getMostSignificantBits()); // 8 of 16 bytes
        packedData.putLong(session.getLeastSignificantBits()); // next 8 of 16 bytes
        packedData.put(tKey); // 32 bytes
        packedData.put(Numeric.toBytesPadded(cropAmount, 32)); // 32 bytes
        packedData.put(Numeric.toBytesPadded(number, 32)); // 32 bytes
        byte[] hash = Hash.sha3(packedData.array());

        int header = signatureData.getV() & 0xFF;
        if (header < 27 || header > 34) {
            throw new SignatureException("Header byte out of range: " + header);
        }
        int recId = header - 27;
        ECDSASignature sig = new ECDSASignature(new BigInteger(1, signatureData.getR()), new BigInteger(1, signatureData.getS()));

        BigInteger publicKey = Sign.recoverFromSignature(recId, sig, hashMessage(hash));
        byte[] address = Keys.getAddress(Numeric.toBytesPadded(publicKey, 64));
        return Arrays.equals(signer, address);
    }

    private static byte[] hashMessage(byte[] data) {
        byte[] preamble = ("\u0019Ethereum Signed Message:\n" + Integer.toString(data.length)).getBytes();
        ByteBuffer buf = ByteBuffer.allocate(preamble.length + data.length);
        buf.put(preamble);
        buf.put(data);
        return Hash.sha3(buf.array());
    }

    private static SignatureData getSignatureData(byte[] signatureEncoded) throws SignatureException {
        if (signatureEncoded.length < 65)
            throw new SignatureException("Signature truncated, expected 65 bytes and got " + signatureEncoded.length);
        return new SignatureData(signatureEncoded[64], Arrays.copyOfRange(signatureEncoded, 0, 32),
                Arrays.copyOfRange(signatureEncoded, 32, 64));
    }

    private static byte[] getTkey(String tablespaceName, String tableName) throws SignatureException {
        try {
            return Hash.sha3((tablespaceName + '#' + tableName).getBytes("utf-8"));
        } catch (UnsupportedEncodingException e) {
            throw new SignatureException("Failed to get Table Key", e);
        }
    }

}
