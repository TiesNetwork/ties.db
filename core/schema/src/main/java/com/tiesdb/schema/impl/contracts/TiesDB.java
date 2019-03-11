package com.tiesdb.schema.impl.contracts;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.EventValues;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.DynamicArray;
import org.web3j.abi.datatypes.DynamicBytes;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.abi.datatypes.generated.Uint32;
import org.web3j.abi.datatypes.generated.Uint64;
import org.web3j.abi.datatypes.generated.Uint8;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.EthGetCode;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tuples.generated.Tuple2;
import org.web3j.tuples.generated.Tuple3;
import org.web3j.tuples.generated.Tuple8;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;
import rx.Observable;
import rx.functions.Func1;

/**
 * <p>Semi automatically generated code.
 * <p><strong>Do not regenerate!</strong>
 * <p>Use the <a href="https://docs.web3j.io/command_line.html">web3j command line tools</a>,
 * or the org.web3j.codegen.SolidityFunctionWrapperGenerator in the 
 * <a href="https://github.com/web3j/web3j/tree/master/codegen">codegen module</a> for generation.
 *
 * <p>Initially generated with web3j version 3.2.0.
 */
public class TiesDB extends Contract {

    private static final String BINARY = "";

    protected static final HashMap<String, String> _addresses;

    static {
        _addresses = new HashMap<>();
    }

    protected TiesDB(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        super(getCode(contractAddress, web3j), contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected TiesDB(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(getCode(contractAddress, web3j), contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    private static String getCode(String contractAddress, Web3j web3j) {
        if (!BINARY.isEmpty()) {
            return BINARY;
        }
        try {
            EthGetCode ethGetCode = web3j.ethGetCode(contractAddress, DefaultBlockParameterName.LATEST).send();
            return ethGetCode.getCode();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    public List<OwnershipTransferredEventResponse> getOwnershipTransferredEvents(TransactionReceipt transactionReceipt) {
        final Event event = new Event("OwnershipTransferred", 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}, new TypeReference<Address>() {}),
                Arrays.<TypeReference<?>>asList());
        List<EventValues> valueList = extractEventParameters(event, transactionReceipt);
        ArrayList<OwnershipTransferredEventResponse> responses = new ArrayList<OwnershipTransferredEventResponse>(valueList.size());
        for (EventValues eventValues : valueList) {
            OwnershipTransferredEventResponse typedResponse = new OwnershipTransferredEventResponse();
            typedResponse.log = transactionReceipt.getLogs().get(valueList.indexOf(eventValues));
            typedResponse.previousOwner = (String) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.newOwner = (String) eventValues.getIndexedValues().get(1).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Observable<OwnershipTransferredEventResponse> ownershipTransferredEventObservable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        final Event event = new Event("OwnershipTransferred", 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}, new TypeReference<Address>() {}),
                Arrays.<TypeReference<?>>asList());
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(event));
        return web3j.ethLogObservable(filter).map(new Func1<Log, OwnershipTransferredEventResponse>() {
            @Override
            public OwnershipTransferredEventResponse call(Log log) {
                EventValues eventValues = extractEventParameters(event, log);
                OwnershipTransferredEventResponse typedResponse = new OwnershipTransferredEventResponse();
                typedResponse.log = log;
                typedResponse.previousOwner = (String) eventValues.getIndexedValues().get(0).getValue();
                typedResponse.newOwner = (String) eventValues.getIndexedValues().get(1).getValue();
                return typedResponse;
            }
        });
    }

    public RemoteCall<Tuple3<String, String, List<byte[]>>> getTablespace(byte[] tsKey) {
        final Function function = new Function("getTablespace", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes32(tsKey)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}, new TypeReference<Address>() {}, new TypeReference<DynamicArray<Bytes32>>() {}));
        return new RemoteCall<Tuple3<String, String, List<byte[]>>>(
                new Callable<Tuple3<String, String, List<byte[]>>>() {
                    @Override
                    public Tuple3<String, String, List<byte[]>> call() throws Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);
                        return new Tuple3<String, String, List<byte[]>>(
                                (String) results.get(0).getValue(), 
                                (String) results.get(1).getValue(), 
                                convertToNative((List<Bytes32>) results.get(2).getValue()));
                    }
                });
    }

    public RemoteCall<TransactionReceipt> deleteTablespace(byte[] tsKey) {
        Function function = new Function(
                "deleteTablespace", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes32(tsKey)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<Tuple2<String, byte[]>> getTrigger(byte[] tKey, byte[] trKey) {
        final Function function = new Function("getTrigger", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes32(tKey), 
                new org.web3j.abi.datatypes.generated.Bytes32(trKey)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}, new TypeReference<DynamicBytes>() {}));
        return new RemoteCall<Tuple2<String, byte[]>>(
                new Callable<Tuple2<String, byte[]>>() {
                    @Override
                    public Tuple2<String, byte[]> call() throws Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);
                        return new Tuple2<String, byte[]>(
                                (String) results.get(0).getValue(), 
                                (byte[]) results.get(1).getValue());
                    }
                });
    }

    public RemoteCall<byte[]> tableToTablespace(byte[] tKey) {
        Function function = new Function("tableToTablespace", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes32(tKey)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bytes32>() {}));
        return executeRemoteCallSingleValueReturn(function, byte[].class);
    }

    public RemoteCall<TransactionReceipt> deleteIndex(byte[] tKey, byte[] iKey) {
        Function function = new Function(
                "deleteIndex", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes32(tKey), 
                new org.web3j.abi.datatypes.generated.Bytes32(iKey)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<Tuple3<String, String, byte[]>> getField(byte[] tKey, byte[] fKey) {
        final Function function = new Function("getField", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes32(tKey), 
                new org.web3j.abi.datatypes.generated.Bytes32(fKey)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}, new TypeReference<Utf8String>() {}, new TypeReference<DynamicBytes>() {}));
        return new RemoteCall<Tuple3<String, String, byte[]>>(
                new Callable<Tuple3<String, String, byte[]>>() {
                    @Override
                    public Tuple3<String, String, byte[]> call() throws Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);
                        return new Tuple3<String, String, byte[]>(
                                (String) results.get(0).getValue(), 
                                (String) results.get(1).getValue(), 
                                (byte[]) results.get(2).getValue());
                    }
                });
    }

    public RemoteCall<Tuple2<List<byte[]>, List<String>>> getStorage() {
        final Function function = new Function("getStorage", 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<DynamicArray<Bytes32>>() {}, new TypeReference<DynamicArray<Address>>() {}));
        return new RemoteCall<Tuple2<List<byte[]>, List<String>>>(
                new Callable<Tuple2<List<byte[]>, List<String>>>() {
                    @Override
                    public Tuple2<List<byte[]>, List<String>> call() throws Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);
                        return new Tuple2<List<byte[]>, List<String>>(
                                convertToNative((List<Bytes32>) results.get(0).getValue()), 
                                convertToNative((List<Address>) results.get(1).getValue()));
                    }
                });
    }

    public RemoteCall<TransactionReceipt> queueNode(String _node) {
        Function function = new Function(
                "queueNode", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(_node)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<TransactionReceipt> createTablespace(String tsName, String rs) {
        Function function = new Function(
                "createTablespace", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Utf8String(tsName), 
                new org.web3j.abi.datatypes.Address(rs)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<String> getFieldName(byte[] tKey, byte[] fKey) {
        Function function = new Function("getFieldName", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes32(tKey), 
                new org.web3j.abi.datatypes.generated.Bytes32(fKey)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteCall<TransactionReceipt> unqueueNode(String _node) {
        Function function = new Function(
                "unqueueNode", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(_node)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<TransactionReceipt> createNode(String _node) {
        Function function = new Function(
                "createNode", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(_node)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<TransactionReceipt> createIndex(byte[] tKey, String iName, BigInteger iType, List<byte[]> fields) {
        Function function = new Function(
                "createIndex", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes32(tKey), 
                new org.web3j.abi.datatypes.Utf8String(iName), 
                new org.web3j.abi.datatypes.generated.Uint8(iType), 
                new org.web3j.abi.datatypes.DynamicArray<org.web3j.abi.datatypes.generated.Bytes32>(
                        org.web3j.abi.Utils.typeMap(fields, org.web3j.abi.datatypes.generated.Bytes32.class))), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<Tuple3<String, BigInteger, List<byte[]>>> getIndex(byte[] tKey, byte[] iKey) {
        final Function function = new Function("getIndex", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes32(tKey), 
                new org.web3j.abi.datatypes.generated.Bytes32(iKey)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}, new TypeReference<Uint8>() {}, new TypeReference<DynamicArray<Bytes32>>() {}));
        return new RemoteCall<Tuple3<String, BigInteger, List<byte[]>>>(
                new Callable<Tuple3<String, BigInteger, List<byte[]>>>() {
                    @Override
                    public Tuple3<String, BigInteger, List<byte[]>> call() throws Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);
                        return new Tuple3<String, BigInteger, List<byte[]>>(
                                (String) results.get(0).getValue(), 
                                (BigInteger) results.get(1).getValue(), 
                                convertToNative((List<Bytes32>) results.get(2).getValue()));
                    }
                });
    }

    public RemoteCall<Tuple8<String, String, List<byte[]>, List<byte[]>, List<byte[]>, BigInteger, BigInteger, List<String>>> getTable(byte[] tKey) {
        final Function function = new Function("getTable", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes32(tKey)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}, new TypeReference<Utf8String>() {}, new TypeReference<DynamicArray<Bytes32>>() {}, new TypeReference<DynamicArray<Bytes32>>() {}, new TypeReference<DynamicArray<Bytes32>>() {}, new TypeReference<Uint32>() {}, new TypeReference<Uint32>() {}, new TypeReference<DynamicArray<Address>>() {}));
        return new RemoteCall<Tuple8<String, String, List<byte[]>, List<byte[]>, List<byte[]>, BigInteger, BigInteger, List<String>>>(
                new Callable<Tuple8<String, String, List<byte[]>, List<byte[]>, List<byte[]>, BigInteger, BigInteger, List<String>>>() {
                    @Override
                    public Tuple8<String, String, List<byte[]>, List<byte[]>, List<byte[]>, BigInteger, BigInteger, List<String>> call() throws Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);
                        return new Tuple8<String, String, List<byte[]>, List<byte[]>, List<byte[]>, BigInteger, BigInteger, List<String>>(
                                (String) results.get(0).getValue(), 
                                (String) results.get(1).getValue(), 
                                convertToNative((List<Bytes32>) results.get(2).getValue()), 
                                convertToNative((List<Bytes32>) results.get(3).getValue()), 
                                convertToNative((List<Bytes32>) results.get(4).getValue()), 
                                (BigInteger) results.get(5).getValue(), 
                                (BigInteger) results.get(6).getValue(), 
                                convertToNative((List<Address>) results.get(7).getValue()));
                    }
                });
    }

    public RemoteCall<List> getTableNodes(byte[] tKey) {
        Function function = new Function("getTableNodes", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes32(tKey)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<DynamicArray<Address>>() {}));
        return new RemoteCall<List>(
                new Callable<List>() {
                    @Override
                    public List call() throws Exception {
                        List<Type> result = (List<Type>) executeCallSingleValueReturn(function, List.class);
                        return convertToNative(result);
                    }
                });
    }

    public RemoteCall<String> owner() {
        Function function = new Function("owner", 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteCall<TransactionReceipt> createField(byte[] tKey, String fName, String fType, byte[] fDefault) {
        Function function = new Function(
                "createField", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes32(tKey), 
                new org.web3j.abi.datatypes.Utf8String(fName), 
                new org.web3j.abi.datatypes.Utf8String(fType), 
                new org.web3j.abi.datatypes.DynamicBytes(fDefault)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<Tuple2<Boolean, List<byte[]>>> getNode(String node) {
        final Function function = new Function("getNode", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(node)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}, new TypeReference<DynamicArray<Bytes32>>() {}));
        return new RemoteCall<Tuple2<Boolean, List<byte[]>>>(
                new Callable<Tuple2<Boolean, List<byte[]>>>() {
                    @Override
                    public Tuple2<Boolean, List<byte[]>> call() throws Exception {
                        List<Type> results = executeCallMultipleValueReturn(function);
                        return new Tuple2<Boolean, List<byte[]>>(
                                (Boolean) results.get(0).getValue(), 
                                convertToNative((List<Bytes32>) results.get(1).getValue()));
                    }
                });
    }

    public RemoteCall<String> getTriggerName(byte[] tKey, byte[] trKey) {
        Function function = new Function("getTriggerName", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes32(tKey), 
                new org.web3j.abi.datatypes.generated.Bytes32(trKey)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteCall<TransactionReceipt> deleteTrigger(byte[] tKey, byte[] trKey) {
        Function function = new Function(
                "deleteTrigger", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes32(tKey), 
                new org.web3j.abi.datatypes.generated.Bytes32(trKey)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<Boolean> hasIndex(byte[] tKey, byte[] iKey) {
        Function function = new Function("hasIndex", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes32(tKey), 
                new org.web3j.abi.datatypes.generated.Bytes32(iKey)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}));
        return executeRemoteCallSingleValueReturn(function, Boolean.class);
    }

    public RemoteCall<Boolean> hasTablespace(byte[] tsKey) {
        Function function = new Function("hasTablespace", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes32(tsKey)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}));
        return executeRemoteCallSingleValueReturn(function, Boolean.class);
    }

    public RemoteCall<TransactionReceipt> setRegistry(String _registry) {
        Function function = new Function(
                "setRegistry", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(_registry)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<Boolean> hasTable(byte[] tsKey, byte[] tKey) {
        Function function = new Function("hasTable", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes32(tsKey), 
                new org.web3j.abi.datatypes.generated.Bytes32(tKey)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}));
        return executeRemoteCallSingleValueReturn(function, Boolean.class);
    }

    public RemoteCall<Boolean> hasTrigger(byte[] tKey, byte[] trKey) {
        Function function = new Function("hasTrigger", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes32(tKey), 
                new org.web3j.abi.datatypes.generated.Bytes32(trKey)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}));
        return executeRemoteCallSingleValueReturn(function, Boolean.class);
    }

    public RemoteCall<TransactionReceipt> createTrigger(byte[] tKey, String trName, byte[] payload) {
        Function function = new Function(
                "createTrigger", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes32(tKey), 
                new org.web3j.abi.datatypes.Utf8String(trName), 
                new org.web3j.abi.datatypes.DynamicBytes(payload)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<TransactionReceipt> deleteField(byte[] tKey, byte[] fKey) {
        Function function = new Function(
                "deleteField", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes32(tKey), 
                new org.web3j.abi.datatypes.generated.Bytes32(fKey)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<String> getIndexName(byte[] tKey, byte[] iKey) {
        Function function = new Function("getIndexName", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes32(tKey), 
                new org.web3j.abi.datatypes.generated.Bytes32(iKey)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteCall<String> getTableName(byte[] tKey) {
        Function function = new Function("getTableName", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes32(tKey)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteCall<Boolean> hasTable(byte[] tKey) {
        Function function = new Function("hasTable", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes32(tKey)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}));
        return executeRemoteCallSingleValueReturn(function, Boolean.class);
    }

    public RemoteCall<TransactionReceipt> createTable(byte[] tsKey, String tName) {
        Function function = new Function(
                "createTable", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes32(tsKey), 
                new org.web3j.abi.datatypes.Utf8String(tName)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<List> getNodes() {
        Function function = new Function("getNodes", 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<DynamicArray<Address>>() {}));
        return new RemoteCall<List>(
                new Callable<List>() {
                    @Override
                    public List call() throws Exception {
                        List<Type> result = (List<Type>) executeCallSingleValueReturn(function, List.class);
                        return convertToNative(result);
                    }
                });
    }

    public RemoteCall<TransactionReceipt> deleteTable(byte[] tsKey, byte[] tKey) {
        Function function = new Function(
                "deleteTable", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes32(tsKey), 
                new org.web3j.abi.datatypes.generated.Bytes32(tKey)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<TransactionReceipt> distribute(byte[] tKey, BigInteger ranges, BigInteger replicas) {
        Function function = new Function(
                "distribute", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes32(tKey), 
                new org.web3j.abi.datatypes.generated.Uint32(ranges), 
                new org.web3j.abi.datatypes.generated.Uint32(replicas)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<String> getTablespaceName(byte[] tsKey) {
        Function function = new Function("getTablespaceName", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes32(tsKey)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteCall<TransactionReceipt> transferOwnership(String newOwner) {
        Function function = new Function(
                "transferOwnership", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(newOwner)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<Boolean> hasField(byte[] tKey, byte[] fKey) {
        Function function = new Function("hasField", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Bytes32(tKey), 
                new org.web3j.abi.datatypes.generated.Bytes32(fKey)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}));
        return executeRemoteCallSingleValueReturn(function, Boolean.class);
    }

    public RemoteCall<List> getNodeTableRanges(String node, byte[] tKey) {
        Function function = new Function("getNodeTableRanges", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(node), 
                new org.web3j.abi.datatypes.generated.Bytes32(tKey)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<DynamicArray<Uint64>>() {}));
        return new RemoteCall<List>(
                new Callable<List>() {
                    @Override
                    public List call() throws Exception {
                        List<Type> result = (List<Type>) executeCallSingleValueReturn(function, List.class);
                        return convertToNative(result);
                    }
                });
    }

    public static RemoteCall<TiesDB> deploy(Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(TiesDB.class, web3j, credentials, gasPrice, gasLimit, BINARY, "");
    }

    public static RemoteCall<TiesDB> deploy(Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
    	BigInteger value;
    	try {
			value = ((com.tiesdb.web3j.SequentialFastRawTransactionManager)transactionManager).encodeNonceToValue(BigInteger.ZERO);
		} catch (java.io.IOException e) {
			throw new RuntimeException(e);
		}
		if(BINARY.indexOf('_') >= 0)
			throw new RuntimeException("Contract binary contains unresolved libraries!");

        return deployRemoteCall(TiesDB.class, web3j, transactionManager, gasPrice, gasLimit, BINARY, "", value);
    }

    public static TiesDB load(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return new TiesDB(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    public static TiesDB load(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new TiesDB(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    protected static <OriginalType extends Type, NewType> List<NewType> convertToNative(List<OriginalType> arr) {
        return (List<NewType>) arr.stream().map(v -> v.getValue()).collect(Collectors.toList());
    }

    protected String getStaticDeployedAddress(String networkId) {
        return _addresses.get(networkId);
    }

    public static String getPreviouslyDeployedAddress(String networkId) {
        return _addresses.get(networkId);
    }

    public static class OwnershipTransferredEventResponse {
        public Log log;

        public String previousOwner;

        public String newOwner;
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
