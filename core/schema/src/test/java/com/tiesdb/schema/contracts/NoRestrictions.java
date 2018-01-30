package com.tiesdb.schema.contracts;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;

import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;

/**
 * <p>Auto generated code.
 * <p><strong>Do not modify!</strong>
 * <p>Please use the <a href="https://docs.web3j.io/command_line.html">web3j command line tools</a>,
 * or the org.web3j.codegen.SolidityFunctionWrapperGenerator in the 
 * <a href="https://github.com/web3j/web3j/tree/master/codegen">codegen module</a> to update.
 *
 * <p>Generated with web3j version 3.2.0.
 */
public class NoRestrictions extends Contract {
    private static final String BINARY = "0x6060604052341561000f57600080fd5b610bbc8061001e6000396000f3006060604052600436106100af576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff168063063209bb146100b45780631a7f6d9e146101485780631de1c2061461026257806334a1a0841461037c5780633684130014610496578063369fa3a2146105b057806349f8a9f0146106ca578063c4e70df0146107e4578063c53a975a146108bb578063e03842b714610992578063e3bb6be914610a26575b600080fd5b34156100bf57600080fd5b61012e600480803590602001908201803590602001908080601f0160208091040260200160405190810160405280939291908181526020018383808284378201915050505050509190803573ffffffffffffffffffffffffffffffffffffffff16906020019091905050610afd565b604051808215151515815260200191505060405180910390f35b341561015357600080fd5b610248600480803590602001908201803590602001908080601f0160208091040260200160405190810160405280939291908181526020018383808284378201915050505050509190803590602001908201803590602001908080601f0160208091040260200160405190810160405280939291908181526020018383808284378201915050505050509190803590602001908201803590602001908080601f0160208091040260200160405190810160405280939291908181526020018383808284378201915050505050509190803573ffffffffffffffffffffffffffffffffffffffff16906020019091905050610b09565b604051808215151515815260200191505060405180910390f35b341561026d57600080fd5b610362600480803590602001908201803590602001908080601f0160208091040260200160405190810160405280939291908181526020018383808284378201915050505050509190803590602001908201803590602001908080601f0160208091040260200160405190810160405280939291908181526020018383808284378201915050505050509190803590602001908201803590602001908080601f0160208091040260200160405190810160405280939291908181526020018383808284378201915050505050509190803573ffffffffffffffffffffffffffffffffffffffff16906020019091905050610b17565b604051808215151515815260200191505060405180910390f35b341561038757600080fd5b61047c600480803590602001908201803590602001908080601f0160208091040260200160405190810160405280939291908181526020018383808284378201915050505050509190803590602001908201803590602001908080601f0160208091040260200160405190810160405280939291908181526020018383808284378201915050505050509190803590602001908201803590602001908080601f0160208091040260200160405190810160405280939291908181526020018383808284378201915050505050509190803573ffffffffffffffffffffffffffffffffffffffff16906020019091905050610b25565b604051808215151515815260200191505060405180910390f35b34156104a157600080fd5b610596600480803590602001908201803590602001908080601f0160208091040260200160405190810160405280939291908181526020018383808284378201915050505050509190803590602001908201803590602001908080601f0160208091040260200160405190810160405280939291908181526020018383808284378201915050505050509190803590602001908201803590602001908080601f0160208091040260200160405190810160405280939291908181526020018383808284378201915050505050509190803573ffffffffffffffffffffffffffffffffffffffff16906020019091905050610b33565b604051808215151515815260200191505060405180910390f35b34156105bb57600080fd5b6106b0600480803590602001908201803590602001908080601f0160208091040260200160405190810160405280939291908181526020018383808284378201915050505050509190803590602001908201803590602001908080601f0160208091040260200160405190810160405280939291908181526020018383808284378201915050505050509190803590602001908201803590602001908080601f0160208091040260200160405190810160405280939291908181526020018383808284378201915050505050509190803573ffffffffffffffffffffffffffffffffffffffff16906020019091905050610b41565b604051808215151515815260200191505060405180910390f35b34156106d557600080fd5b6107ca600480803590602001908201803590602001908080601f0160208091040260200160405190810160405280939291908181526020018383808284378201915050505050509190803590602001908201803590602001908080601f0160208091040260200160405190810160405280939291908181526020018383808284378201915050505050509190803590602001908201803590602001908080601f0160208091040260200160405190810160405280939291908181526020018383808284378201915050505050509190803573ffffffffffffffffffffffffffffffffffffffff16906020019091905050610b4f565b604051808215151515815260200191505060405180910390f35b34156107ef57600080fd5b6108a1600480803590602001908201803590602001908080601f0160208091040260200160405190810160405280939291908181526020018383808284378201915050505050509190803590602001908201803590602001908080601f0160208091040260200160405190810160405280939291908181526020018383808284378201915050505050509190803573ffffffffffffffffffffffffffffffffffffffff16906020019091905050610b5d565b604051808215151515815260200191505060405180910390f35b34156108c657600080fd5b610978600480803590602001908201803590602001908080601f0160208091040260200160405190810160405280939291908181526020018383808284378201915050505050509190803590602001908201803590602001908080601f0160208091040260200160405190810160405280939291908181526020018383808284378201915050505050509190803573ffffffffffffffffffffffffffffffffffffffff16906020019091905050610b6a565b604051808215151515815260200191505060405180910390f35b341561099d57600080fd5b610a0c600480803590602001908201803590602001908080601f0160208091040260200160405190810160405280939291908181526020018383808284378201915050505050509190803573ffffffffffffffffffffffffffffffffffffffff16906020019091905050610b77565b604051808215151515815260200191505060405180910390f35b3415610a3157600080fd5b610ae3600480803590602001908201803590602001908080601f0160208091040260200160405190810160405280939291908181526020018383808284378201915050505050509190803590602001908201803590602001908080601f0160208091040260200160405190810160405280939291908181526020018383808284378201915050505050509190803573ffffffffffffffffffffffffffffffffffffffff16906020019091905050610b83565b604051808215151515815260200191505060405180910390f35b60006001905092915050565b600060019050949350505050565b600060019050949350505050565b600060019050949350505050565b600060019050949350505050565b600060019050949350505050565b600060019050949350505050565b6000600190509392505050565b6000600190509392505050565b60006001905092915050565b60006001905093925050505600a165627a7a72305820a5d75d577baf3892e5ec01bec7fd77f6fcea697ed97d1b0bf25e1415859278040029";

    protected static final HashMap<String, String> _addresses;

    static {
        _addresses = new HashMap<>();
    }

    protected NoRestrictions(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected NoRestrictions(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public RemoteCall<Boolean> canCreateTablespace(String param0, String param1) {
        Function function = new Function("canCreateTablespace", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Utf8String(param0), 
                new org.web3j.abi.datatypes.Address(param1)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}));
        return executeRemoteCallSingleValueReturn(function, Boolean.class);
    }

    public RemoteCall<Boolean> canCreateIndex(String param0, String param1, String param2, String param3) {
        Function function = new Function("canCreateIndex", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Utf8String(param0), 
                new org.web3j.abi.datatypes.Utf8String(param1), 
                new org.web3j.abi.datatypes.Utf8String(param2), 
                new org.web3j.abi.datatypes.Address(param3)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}));
        return executeRemoteCallSingleValueReturn(function, Boolean.class);
    }

    public RemoteCall<Boolean> canCreateField(String param0, String param1, String param2, String param3) {
        Function function = new Function("canCreateField", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Utf8String(param0), 
                new org.web3j.abi.datatypes.Utf8String(param1), 
                new org.web3j.abi.datatypes.Utf8String(param2), 
                new org.web3j.abi.datatypes.Address(param3)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}));
        return executeRemoteCallSingleValueReturn(function, Boolean.class);
    }

    public RemoteCall<Boolean> canDeleteField(String param0, String param1, String param2, String param3) {
        Function function = new Function("canDeleteField", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Utf8String(param0), 
                new org.web3j.abi.datatypes.Utf8String(param1), 
                new org.web3j.abi.datatypes.Utf8String(param2), 
                new org.web3j.abi.datatypes.Address(param3)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}));
        return executeRemoteCallSingleValueReturn(function, Boolean.class);
    }

    public RemoteCall<Boolean> canDeleteIndex(String param0, String param1, String param2, String param3) {
        Function function = new Function("canDeleteIndex", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Utf8String(param0), 
                new org.web3j.abi.datatypes.Utf8String(param1), 
                new org.web3j.abi.datatypes.Utf8String(param2), 
                new org.web3j.abi.datatypes.Address(param3)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}));
        return executeRemoteCallSingleValueReturn(function, Boolean.class);
    }

    public RemoteCall<Boolean> canDeleteTrigger(String param0, String param1, String param2, String param3) {
        Function function = new Function("canDeleteTrigger", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Utf8String(param0), 
                new org.web3j.abi.datatypes.Utf8String(param1), 
                new org.web3j.abi.datatypes.Utf8String(param2), 
                new org.web3j.abi.datatypes.Address(param3)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}));
        return executeRemoteCallSingleValueReturn(function, Boolean.class);
    }

    public RemoteCall<Boolean> canCreateTrigger(String param0, String param1, String param2, String param3) {
        Function function = new Function("canCreateTrigger", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Utf8String(param0), 
                new org.web3j.abi.datatypes.Utf8String(param1), 
                new org.web3j.abi.datatypes.Utf8String(param2), 
                new org.web3j.abi.datatypes.Address(param3)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}));
        return executeRemoteCallSingleValueReturn(function, Boolean.class);
    }

    public RemoteCall<Boolean> canDeleteTable(String param0, String param1, String param2) {
        Function function = new Function("canDeleteTable", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Utf8String(param0), 
                new org.web3j.abi.datatypes.Utf8String(param1), 
                new org.web3j.abi.datatypes.Address(param2)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}));
        return executeRemoteCallSingleValueReturn(function, Boolean.class);
    }

    public RemoteCall<Boolean> canCreateTable(String param0, String param1, String param2) {
        Function function = new Function("canCreateTable", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Utf8String(param0), 
                new org.web3j.abi.datatypes.Utf8String(param1), 
                new org.web3j.abi.datatypes.Address(param2)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}));
        return executeRemoteCallSingleValueReturn(function, Boolean.class);
    }

    public RemoteCall<Boolean> canDeleteTablespace(String param0, String param1) {
        Function function = new Function("canDeleteTablespace", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Utf8String(param0), 
                new org.web3j.abi.datatypes.Address(param1)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}));
        return executeRemoteCallSingleValueReturn(function, Boolean.class);
    }

    public RemoteCall<Boolean> canDistributeRanges(String param0, String param1, String param2) {
        Function function = new Function("canDistributeRanges", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Utf8String(param0), 
                new org.web3j.abi.datatypes.Utf8String(param1), 
                new org.web3j.abi.datatypes.Address(param2)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}));
        return executeRemoteCallSingleValueReturn(function, Boolean.class);
    }

    public static RemoteCall<NoRestrictions> deploy(Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(NoRestrictions.class, web3j, credentials, gasPrice, gasLimit, BINARY, "");
    }

    public static RemoteCall<NoRestrictions> deploy(Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
    	BigInteger value;
    	try {
			value = ((com.tiesdb.web3j.SequentialFastRawTransactionManager)transactionManager).encodeNonceToValue(BigInteger.ZERO);
		} catch (java.io.IOException e) {
			throw new RuntimeException(e);
		}
		if(BINARY.indexOf('_') >= 0)
			throw new RuntimeException("Contract binary contains unresolved libraries!");

        return deployRemoteCall(NoRestrictions.class, web3j, transactionManager, gasPrice, gasLimit, BINARY, "", value);
    }

    public static NoRestrictions load(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return new NoRestrictions(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    public static NoRestrictions load(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new NoRestrictions(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    protected String getStaticDeployedAddress(String networkId) {
        return _addresses.get(networkId);
    }

    public static String getPreviouslyDeployedAddress(String networkId) {
        return _addresses.get(networkId);
    }

    @Override
    protected RemoteCall<TransactionReceipt> executeRemoteCallTransaction(Function function) {
        return executeRemoteCallTransaction(function, BigInteger.ZERO);
    }

    @Override
    protected RemoteCall<TransactionReceipt> executeRemoteCallTransaction(
            Function function, BigInteger weiValue) {
    	
    	final String data = org.web3j.abi.FunctionEncoder.encode(function);
    	BigInteger encodedWei;
		try {
			encodedWei = ((com.tiesdb.web3j.SequentialFastRawTransactionManager)transactionManager).encodeNonceToValue(weiValue);
		} catch (java.io.IOException e) {
			throw new RuntimeException(e);
		}
    	
        return new RemoteCall<>(() -> send(contractAddress, data, encodedWei, gasPrice, gasLimit));
    }

}