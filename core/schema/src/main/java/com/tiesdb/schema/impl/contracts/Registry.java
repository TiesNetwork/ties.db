package com.tiesdb.schema.impl.contracts;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.EventValues;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;
import rx.Observable;
import rx.functions.Func1;

/**
 * <p>Auto generated code.
 * <p><strong>Do not modify!</strong>
 * <p>Please use the <a href="https://docs.web3j.io/command_line.html">web3j command line tools</a>,
 * or the org.web3j.codegen.SolidityFunctionWrapperGenerator in the 
 * <a href="https://github.com/web3j/web3j/tree/master/codegen">codegen module</a> to update.
 *
 * <p>Generated with web3j version 3.2.0.
 */
public class Registry extends Contract {
    private static final String BINARY = "0x6060604052341561000f57600080fd5b6040516040806118c18339810160405280805190602001909190805190602001909190505081600260006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff16021790555080600360006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff16021790555050506117fa806100c76000396000f3006060604052600436106100ba576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff168063189a5a17146100bf57806331035e171461010c5780635d210950146101315780639a5444da146101545780639dc9a465146101c0578063a87430ba1461026e578063aa7c596f146102bb578063c084b10b14610308578063c0ee0b8a14610355578063ef3ab99c146103cf578063f35a3a87146103f2578063fc0c546a14610447575b600080fd5b34156100ca57600080fd5b6100f6600480803573ffffffffffffffffffffffffffffffffffffffff1690602001909190505061049c565b6040518082815260200191505060405180910390f35b341561011757600080fd5b61012f600480803515159060200190919050506104ba565b005b341561013c57600080fd5b61015260048080359060200190919050506106c0565b005b341561015f57600080fd5b6101aa600480803573ffffffffffffffffffffffffffffffffffffffff1690602001909190803573ffffffffffffffffffffffffffffffffffffffff16906020019091905050610880565b6040518082815260200191505060405180910390f35b34156101cb57600080fd5b610258600480803573ffffffffffffffffffffffffffffffffffffffff1690602001909190803573ffffffffffffffffffffffffffffffffffffffff1690602001909190803590602001909190803567ffffffffffffffff1690602001909190803560ff16906020019091908035600019169060200190919080356000191690602001909190505061090c565b6040518082815260200191505060405180910390f35b341561027957600080fd5b6102a5600480803573ffffffffffffffffffffffffffffffffffffffff1690602001909190505061104b565b6040518082815260200191505060405180910390f35b34156102c657600080fd5b6102f2600480803573ffffffffffffffffffffffffffffffffffffffff16906020019091905050611069565b6040518082815260200191505060405180910390f35b341561031357600080fd5b61033f600480803573ffffffffffffffffffffffffffffffffffffffff169060200190919050506110b5565b6040518082815260200191505060405180910390f35b6103cd600480803573ffffffffffffffffffffffffffffffffffffffff1690602001909190803590602001909190803590602001908201803590602001908080601f01602080910402602001604051908101604052809392919081815260200183838082843782019150505050505091905050611100565b005b34156103da57600080fd5b6103f06004808035906020019091905050611409565b005b34156103fd57600080fd5b61040561153a565b604051808273ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200191505060405180910390f35b341561045257600080fd5b61045a611560565b604051808273ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200191505060405180910390f35b60016020528060005260406000206000915090508060000154905081565b60003390506000600160008373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020019081526020016000206000015411151561051057600080fd5b81156105eb57600360009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1663398fa5c8826040518263ffffffff167c0100000000000000000000000000000000000000000000000000000000028152600401808273ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001915050600060405180830381600087803b15156105d257600080fd5b6102c65a03f115156105e357600080fd5b5050506106bc565b600360009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1663473e13ad826040518263ffffffff167c0100000000000000000000000000000000000000000000000000000000028152600401808273ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001915050600060405180830381600087803b15156106a757600080fd5b6102c65a03f115156106b857600080fd5b5050505b5050565b610714816000803373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020019081526020016000206000015461158690919063ffffffff16565b6000803373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060000181905550600260009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff166323b872dd3330846000604051602001526040518463ffffffff167c0100000000000000000000000000000000000000000000000000000000028152600401808473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020018373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020018281526020019350505050602060405180830381600087803b151561085a57600080fd5b6102c65a03f1151561086b57600080fd5b50505060405180519050151561087d57fe5b50565b60008060008473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060010160008373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060000154905092915050565b600080600080600080600160008d73ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002094506000856000015411151561096957600080fd5b6000808e73ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002093508360010160008d73ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001908152602001600020925082600001548b111580610a2a57508260010160009054906101000a900467ffffffffffffffff1667ffffffffffffffff168a67ffffffffffffffff16105b15610aa0577f08c379a0afcc32b1a39302f7cb8073359698411ab5fd6e3edb2c02c0b5fba8aa60405180806020018281038252601b8152602001807f4368657175652077617320616c72656164792072656465656d6564000000000081525060200191505060405180910390a16000955061103b565b8c8c8c8c60405180807f5449452063686571756500000000000000000000000000000000000000000000815250600a018573ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff166c010000000000000000000000000281526014018473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff166c010000000000000000000000000281526014018381526020018267ffffffffffffffff1667ffffffffffffffff167801000000000000000000000000000000000000000000000000028152600801945050505050604051809103902091506001828a8a8a604051600081526020016040526000604051602001526040518085600019166000191681526020018460ff1660ff16815260200183600019166000191681526020018260001916600019168152602001945050505050602060405160208103908084039060008661646e5a03f11515610c1b57600080fd5b50506020604051035173ffffffffffffffffffffffffffffffffffffffff168d73ffffffffffffffffffffffffffffffffffffffff16141515610cc9577f08c379a0afcc32b1a39302f7cb8073359698411ab5fd6e3edb2c02c0b5fba8aa6040518080602001828103825260168152602001807f5369676e617475726520636865636b206661696c65640000000000000000000081525060200191505060405180910390a16000955061103b565b82600001548b039050898360010160006101000a81548167ffffffffffffffff021916908367ffffffffffffffff1602179055508360000154811115610e7d578360000154905080836000016000828254019250508190555060008460000181905550600260009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1663a9059cbb8d836000604051602001526040518363ffffffff167c0100000000000000000000000000000000000000000000000000000000028152600401808373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200182815260200192505050602060405180830381600087803b1515610df957600080fd5b6102c65a03f11515610e0a57600080fd5b50505060405180519050507f2250e2993c15843b32621c89447cc589ee7a9f049c026986e545d3c2c0c6f9788d604051808273ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200191505060405180910390a1610f82565b8a8360000181905550808460000160008282540392505081905550600260009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1663a9059cbb8d836000604051602001526040518363ffffffff167c0100000000000000000000000000000000000000000000000000000000028152600401808373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200182815260200192505050602060405180830381600087803b1515610f6557600080fd5b6102c65a03f11515610f7657600080fd5b50505060405180519050505b7ff23abbcb7c7903642a87048e2eb81db0a73fdb6e4730287637c9a1161049f2788d8d8d86600001548f0385604051808673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020018573ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020018481526020018381526020018281526020019550505050505060405180910390a18095505b5050505050979650505050505050565b60006020528060005260406000206000915090508060000154905081565b6000600160008373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001908152602001600020600001549050919050565b60008060008373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001908152602001600020600001549050919050565b6000600260009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff1614151561115e57600080fd5b60003414151561116d57600080fd5b600482511015156113e85781600381518110151561118757fe5b9060200101517f010000000000000000000000000000000000000000000000000000000000000090047f0100000000000000000000000000000000000000000000000000000000000000027f01000000000000000000000000000000000000000000000000000000000000009004600883600281518110151561120657fe5b9060200101517f010000000000000000000000000000000000000000000000000000000000000090047f0100000000000000000000000000000000000000000000000000000000000000027f01000000000000000000000000000000000000000000000000000000000000009004600885600181518110151561128557fe5b9060200101517f010000000000000000000000000000000000000000000000000000000000000090047f0100000000000000000000000000000000000000000000000000000000000000027f01000000000000000000000000000000000000000000000000000000000000009004600887600081518110151561130457fe5b9060200101517f010000000000000000000000000000000000000000000000000000000000000090047f0100000000000000000000000000000000000000000000000000000000000000027f0100000000000000000000000000000000000000000000000000000000000000900463ffffffff169060020a020163ffffffff169060020a020163ffffffff169060020a0201905060008163ffffffff1614156113b6576113b184846115a4565b6113e3565b60018163ffffffff1614156113d4576113cf8484611650565b6113e2565b600015156113e157600080fd5b5b5b611403565b600082511415156113f857600080fd5b61140284846115a4565b5b50505050565b6114133382611650565b600260009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff166323b872dd3330846000604051602001526040518463ffffffff167c0100000000000000000000000000000000000000000000000000000000028152600401808473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020018373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020018281526020019350505050602060405180830381600087803b151561151457600080fd5b6102c65a03f1151561152557600080fd5b50505060405180519050151561153757fe5b50565b600360009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1681565b600260009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1681565b600080828401905083811015151561159a57fe5b8091505092915050565b6000811115156115b357600080fd5b611607816000808573ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020019081526020016000206000015461158690919063ffffffff16565b6000808473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001908152602001600020600001819055505050565b60008111151561165f57600080fd5b6116b481600160008573ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020019081526020016000206000015461158690919063ffffffff16565b600160008473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060000181905550600360009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16635c6dc219836040518263ffffffff167c0100000000000000000000000000000000000000000000000000000000028152600401808273ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001915050600060405180830381600087803b15156117b657600080fd5b6102c65a03f115156117c757600080fd5b50505050505600a165627a7a72305820b9200d3b41e70b02fae193fe9517a17093c2c17755f7662d3df16c7e52ed61730029";

    protected static final HashMap<String, String> _addresses;

    static {
        _addresses = new HashMap<>();
    }

    protected Registry(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected Registry(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public List<OverdraftEventResponse> getOverdraftEvents(TransactionReceipt transactionReceipt) {
        final Event event = new Event("Overdraft", 
                Arrays.<TypeReference<?>>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        List<EventValues> valueList = extractEventParameters(event, transactionReceipt);
        ArrayList<OverdraftEventResponse> responses = new ArrayList<OverdraftEventResponse>(valueList.size());
        for (EventValues eventValues : valueList) {
            OverdraftEventResponse typedResponse = new OverdraftEventResponse();
            typedResponse.deadbeat = (String) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Observable<OverdraftEventResponse> overdraftEventObservable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        final Event event = new Event("Overdraft", 
                Arrays.<TypeReference<?>>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(event));
        return web3j.ethLogObservable(filter).map(new Func1<Log, OverdraftEventResponse>() {
            @Override
            public OverdraftEventResponse call(Log log) {
                EventValues eventValues = extractEventParameters(event, log);
                OverdraftEventResponse typedResponse = new OverdraftEventResponse();
                typedResponse.deadbeat = (String) eventValues.getNonIndexedValues().get(0).getValue();
                return typedResponse;
            }
        });
    }

    public List<ChequeRedeemedEventResponse> getChequeRedeemedEvents(TransactionReceipt transactionReceipt) {
        final Event event = new Event("ChequeRedeemed", 
                Arrays.<TypeReference<?>>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}, new TypeReference<Address>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}));
        List<EventValues> valueList = extractEventParameters(event, transactionReceipt);
        ArrayList<ChequeRedeemedEventResponse> responses = new ArrayList<ChequeRedeemedEventResponse>(valueList.size());
        for (EventValues eventValues : valueList) {
            ChequeRedeemedEventResponse typedResponse = new ChequeRedeemedEventResponse();
            typedResponse.issuer = (String) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.beneficiary = (String) eventValues.getNonIndexedValues().get(1).getValue();
            typedResponse.total = (BigInteger) eventValues.getNonIndexedValues().get(2).getValue();
            typedResponse.claimed = (BigInteger) eventValues.getNonIndexedValues().get(3).getValue();
            typedResponse.redeemed = (BigInteger) eventValues.getNonIndexedValues().get(4).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Observable<ChequeRedeemedEventResponse> chequeRedeemedEventObservable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        final Event event = new Event("ChequeRedeemed", 
                Arrays.<TypeReference<?>>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}, new TypeReference<Address>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}));
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(event));
        return web3j.ethLogObservable(filter).map(new Func1<Log, ChequeRedeemedEventResponse>() {
            @Override
            public ChequeRedeemedEventResponse call(Log log) {
                EventValues eventValues = extractEventParameters(event, log);
                ChequeRedeemedEventResponse typedResponse = new ChequeRedeemedEventResponse();
                typedResponse.issuer = (String) eventValues.getNonIndexedValues().get(0).getValue();
                typedResponse.beneficiary = (String) eventValues.getNonIndexedValues().get(1).getValue();
                typedResponse.total = (BigInteger) eventValues.getNonIndexedValues().get(2).getValue();
                typedResponse.claimed = (BigInteger) eventValues.getNonIndexedValues().get(3).getValue();
                typedResponse.redeemed = (BigInteger) eventValues.getNonIndexedValues().get(4).getValue();
                return typedResponse;
            }
        });
    }

    public List<ErrorEventResponse> getErrorEvents(TransactionReceipt transactionReceipt) {
        final Event event = new Event("Error", 
                Arrays.<TypeReference<?>>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}));
        List<EventValues> valueList = extractEventParameters(event, transactionReceipt);
        ArrayList<ErrorEventResponse> responses = new ArrayList<ErrorEventResponse>(valueList.size());
        for (EventValues eventValues : valueList) {
            ErrorEventResponse typedResponse = new ErrorEventResponse();
            typedResponse.text = (String) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Observable<ErrorEventResponse> errorEventObservable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        final Event event = new Event("Error", 
                Arrays.<TypeReference<?>>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}));
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(event));
        return web3j.ethLogObservable(filter).map(new Func1<Log, ErrorEventResponse>() {
            @Override
            public ErrorEventResponse call(Log log) {
                EventValues eventValues = extractEventParameters(event, log);
                ErrorEventResponse typedResponse = new ErrorEventResponse();
                typedResponse.text = (String) eventValues.getNonIndexedValues().get(0).getValue();
                return typedResponse;
            }
        });
    }

    public RemoteCall<BigInteger> nodes(String param0) {
        Function function = new Function("nodes", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(param0)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteCall<TransactionReceipt> acceptRanges(Boolean accept) {
        Function function = new Function(
                "acceptRanges", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Bool(accept)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<TransactionReceipt> addUserDeposit(BigInteger amount) {
        Function function = new Function(
                "addUserDeposit", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(amount)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<BigInteger> getSent(String user, String beneficiary) {
        Function function = new Function("getSent", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(user), 
                new org.web3j.abi.datatypes.Address(beneficiary)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteCall<TransactionReceipt> cashCheque(String issuer, String beneficiary, BigInteger amount, BigInteger lastTimeStamp, BigInteger sigv, byte[] sigr, byte[] sigs) {
        Function function = new Function(
                "cashCheque", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(issuer), 
                new org.web3j.abi.datatypes.Address(beneficiary), 
                new org.web3j.abi.datatypes.generated.Uint256(amount), 
                new org.web3j.abi.datatypes.generated.Uint64(lastTimeStamp), 
                new org.web3j.abi.datatypes.generated.Uint8(sigv), 
                new org.web3j.abi.datatypes.generated.Bytes32(sigr), 
                new org.web3j.abi.datatypes.generated.Bytes32(sigs)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<BigInteger> users(String param0) {
        Function function = new Function("users", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(param0)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteCall<BigInteger> getNodeDeposit(String node) {
        Function function = new Function("getNodeDeposit", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(node)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteCall<BigInteger> getUserDeposit(String user) {
        Function function = new Function("getUserDeposit", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(user)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteCall<TransactionReceipt> tokenFallback(String _from, BigInteger _value, byte[] _data, BigInteger weiValue) {
        Function function = new Function(
                "tokenFallback", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(_from), 
                new org.web3j.abi.datatypes.generated.Uint256(_value), 
                new org.web3j.abi.datatypes.DynamicBytes(_data)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function, weiValue);
    }

    public RemoteCall<TransactionReceipt> addNodeDeposit(BigInteger amount) {
        Function function = new Function(
                "addNodeDeposit", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(amount)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<String> tiesDB() {
        Function function = new Function("tiesDB", 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteCall<String> token() {
        Function function = new Function("token", 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public static RemoteCall<Registry> deploy(Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit, String _token, String _tiesDB) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(_token), 
                new org.web3j.abi.datatypes.Address(_tiesDB)));
        return deployRemoteCall(Registry.class, web3j, credentials, gasPrice, gasLimit, BINARY, encodedConstructor);
    }

    public static RemoteCall<Registry> deploy(Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit, String _token, String _tiesDB) {
    	BigInteger value;
    	try {
			value = ((com.tiesdb.web3j.SequentialFastRawTransactionManager)transactionManager).encodeNonceToValue(BigInteger.ZERO);
		} catch (java.io.IOException e) {
			throw new RuntimeException(e);
		}

        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(_token), 
                new org.web3j.abi.datatypes.Address(_tiesDB)));
        return deployRemoteCall(Registry.class, web3j, transactionManager, gasPrice, gasLimit, BINARY, encodedConstructor, value);
    }

    public static Registry load(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return new Registry(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    public static Registry load(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new Registry(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    protected String getStaticDeployedAddress(String networkId) {
        return _addresses.get(networkId);
    }

    public static String getPreviouslyDeployedAddress(String networkId) {
        return _addresses.get(networkId);
    }

    public static class OverdraftEventResponse {
        public String deadbeat;
    }

    public static class ChequeRedeemedEventResponse {
        public String issuer;

        public String beneficiary;

        public BigInteger total;

        public BigInteger claimed;

        public BigInteger redeemed;
    }

    public static class ErrorEventResponse {
        public String text;
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