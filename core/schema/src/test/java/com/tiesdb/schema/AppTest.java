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
package com.tiesdb.schema;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.Contract;
import org.web3j.tx.ManagedTransaction;
import org.web3j.tx.TransactionManager;

import com.tiesdb.schema.api.Node;
import com.tiesdb.schema.api.Range;
import com.tiesdb.schema.api.Ranges;
import com.tiesdb.schema.api.Schema;
import com.tiesdb.schema.api.Table;
import com.tiesdb.schema.api.Tablespace;
import com.tiesdb.schema.api.type.Address;
import com.tiesdb.schema.api.type.Id;
import com.tiesdb.schema.contracts.NoRestrictions;
import com.tiesdb.schema.impl.SchemaImpl;
import com.tiesdb.schema.impl.contracts.Registry;
import com.tiesdb.schema.impl.contracts.TieToken;
import com.tiesdb.schema.impl.contracts.TiesDB;
import com.tiesdb.web3j.SequentialFastRawTransactionManager;

/**
 * Unit test for simple App.
 */
public class AppTest
{
	//Rinkeby infura
	//0x1Eb675E72544bcc23F67f497c1DC59b5654BDE57
	static Credentials creator = Credentials.create("0xf67cc954586899177d0abfaadfb2223df89b983a6b0a452f6d9fdff89cfdb3be");
	//0x871BC2ee330bd8B24834716180E6247ea91aa1FC
	static Credentials node1 = Credentials.create("0xa1bf7c7c88a4a3926733f875351f36f9bb56ce911d8bf5fc2907c5669e3e728b");
	//0x830d8f24e3c3a67946FBD87f506b8EA91DFF29B5
	static Credentials node2 = Credentials.create("0x4ff874a6f05c29b8b68627b7097062641490d495252faa6b6bf1fa3c856e2129");
	//0x5802faac2dB248ee8AC3388210d2429b025705fD
	static Credentials node3 = Credentials.create("0xa04b349c425b9e02bdf71e31a3844ab8ab1bc1ead9c94569f16373bad9ddaf7c");
	//0xf635100a3329286b9CDcc4DB250E3bC65F1bFbCD
	static Credentials node4 = Credentials.create("0x2dbfdb98d327461750de2bb301c81cd6cae64f9c4f63d01fea76447f1d00b34e");
	
	//0xae5878eF36d90DE0700f49E5bfd9fb5A55AbB3FB
	static Credentials readonly = Credentials.create("0x388512004ef987046875b8e612271c2572b3fcfffbf64771892205164165cae0");
	
	private static final String TIETOKEN = "0x256846175c238d01F4B5698222888bE0766cb393"; //Good enough, no need to change
	private static final String NORESTRICTIONS = "0x29a60cea1aded2ef4b64ed219acdb0f351b5aded"; //Read only, no need to change
	
	private static final String REGISTRY = "0x737ba511993e3bee72919a1c55a35985997e1b0b";
	private static final String TIESDB = "0x22d1b55ebb5bcd17084c3c9d690056875263fec1";
	
	static String nodeUrl = "https://rinkeby.infura.io/biP9YQcNXTag7nvAELQJ";
//	static String nodeUrl = "http://127.0.0.1:9545";
	static Schema schema;
	static final BigInteger GAS_PRICE = BigInteger.valueOf(21_000_000_000L);
	static final BigInteger GAS_LIMIT_DEPLOY = BigInteger.valueOf(6_700_000L);
	static final BigInteger GAS_LIMIT = BigInteger.valueOf(1_000_000L);
	static Logger log = LoggerFactory.getLogger(AppTest.class.getName()); 

	public static void allOf(List<CompletableFuture<TransactionReceipt>> futuresList) throws InterruptedException, ExecutionException {
	    for(Iterator<CompletableFuture<TransactionReceipt>> it = futuresList.iterator(); it.hasNext();) {
	    	CompletableFuture<TransactionReceipt> cf = it.next();
	    	TransactionReceipt tr = cf.get();
	    	assertEquals(tr.getStatus(), "0x1", "Transaction " + tr.getTransactionHash() + " should have succeeded");
	    }
    	futuresList.clear();
	}
	
    @BeforeAll
    public static void testCreateDB() throws Exception {
    	Web3j w3j = Web3j.build(new HttpService(nodeUrl));
    	TransactionManager tm = new SequentialFastRawTransactionManager(w3j, creator);
    	
    	if(!REGISTRY.isEmpty() && !TIETOKEN.isEmpty() && !TIESDB.isEmpty()) {
        	log.info("Contracts are already deployed");
        	schema = new SchemaImpl(nodeUrl, readonly, TIESDB);
    		return; //All the contracts are ready, proceed to test
    	}
    	
    	log.info("Deploying contracts");
    	
    	CompletableFuture<TiesDB> cf1;
   		CompletableFuture<TieToken> cf3;
    	CompletableFuture<NoRestrictions> cf2; 
    	
   		if(!NORESTRICTIONS.isEmpty()) {
   			cf2 = new CompletableFuture<NoRestrictions>();
   			cf2.complete(NoRestrictions.load(NORESTRICTIONS, w3j, tm, GAS_PRICE, GAS_LIMIT));
   		} else {
   			cf2 = NoRestrictions.deploy(w3j, tm, GAS_PRICE, GAS_LIMIT_DEPLOY).sendAsync();
   		}
   		
   		if(!TIESDB.isEmpty()) {
   			cf1 = new CompletableFuture<TiesDB>();
   			cf1.complete(TiesDB.load(TIESDB, w3j, tm, GAS_PRICE, GAS_LIMIT));
   		} else {
   			cf1 = TiesDB.deploy(w3j, tm, GAS_PRICE, GAS_LIMIT_DEPLOY).sendAsync();
   		}
   		
   		if(!TIETOKEN.isEmpty()) {
   			cf3 = new CompletableFuture<TieToken>();
   			cf3.complete(TieToken.load(TIETOKEN, w3j, tm, GAS_PRICE, GAS_LIMIT));
   		} else {
   			cf3 = TieToken.deploy(w3j, tm, GAS_PRICE, GAS_LIMIT_DEPLOY, creator.getAddress()).sendAsync();
   		}
   		
    	TiesDB tiesDB = cf1.get();
    	NoRestrictions nr = cf2.get();
    	TieToken token = cf3.get();
    	
    	if(TIESDB.isEmpty())
    		assertEquals(tiesDB.getTransactionReceipt().get().getStatus(), "0x1", "Creation of TiesDB contract should be successful");
    	if(NORESTRICTIONS.isEmpty())
    		assertEquals(nr.getTransactionReceipt().get().getStatus(), "0x1", "Creation of NoRestrictions contract should be successful");
    	if(TIETOKEN.isEmpty())
    		assertEquals(token.getTransactionReceipt().get().getStatus(), "0x1", "Creation of TieToken contract should be successful");
    	
    	//Set new gas limit for operations
    	tiesDB = TiesDB.load(tiesDB.getContractAddress(), w3j, tm, GAS_PRICE, GAS_LIMIT);
    	token = TieToken.load(token.getContractAddress(), w3j, tm, GAS_PRICE, GAS_LIMIT);
    	
    	log.info("Contracts deployed: " + tiesDB.getContractAddress());
    	
    	token.enableTransfer(true).sendAsync();
    	token.setMinter(creator.getAddress()).sendAsync();
    	token.mint(node1.getAddress(), BigInteger.TEN.pow(21)).sendAsync();
    	token.mint(node2.getAddress(), BigInteger.TEN.pow(21)).sendAsync();
    	token.mint(node3.getAddress(), BigInteger.TEN.pow(21)).sendAsync();
    	token.mint(node4.getAddress(), BigInteger.TEN.pow(21)).sendAsync();
    	
    	CompletableFuture<Registry> cf6 = Registry.deploy(w3j, tm, ManagedTransaction.GAS_PRICE, Contract.GAS_LIMIT, token.getContractAddress(), tiesDB.getContractAddress()).sendAsync();

    	List<CompletableFuture<TransactionReceipt>> futures = new ArrayList<CompletableFuture<TransactionReceipt>>();
    	
    	final int TBLSPC_COUNT = 3;
    	final int TBL_COUNT = 4;
    	
    	log.info("Creating tablespaces");

    	for(int i=1; i<=TBLSPC_COUNT; ++i) {
    		futures.add(tiesDB.createTablespace("ts" + i, nr.getContractAddress()).sendAsync());
    	}
    	
    	Registry registry = cf6.get();
    	assertEquals(registry.getTransactionReceipt().get().getStatus(), "0x1", "Creation of Registry contract should be successful");
    	CompletableFuture<TransactionReceipt> cfSetreg = tiesDB.setRegistry(registry.getContractAddress()).sendAsync();
   	
    	schema = new SchemaImpl(nodeUrl, readonly, tiesDB.getContractAddress());

    	for(int i=1; i<=TBLSPC_COUNT; ++i) {
    		byte[] tsid = schema.idFromName("ts" + i).getValue();

        	log.info("Creating tables with fields, indexes for tablespace: ts" + i);
        	
    		for(int j=1; j<=TBL_COUNT; ++j) {
    			if (j == 4 && i > 1)
    				continue; //Only ts1 has 4 tables
    			
    			byte[] ts = schema.idFromName("ts" + i).getValue();
    			futures.add(tiesDB.createTable(ts, "table" + j).sendAsync());
    	    	
        		byte[] tid = schema.idFromName("ts" + i, "table" + j).getValue();
    			
        		futures.add(tiesDB.createField(tid, "field1" + j, "type1" + j, ("def1" + j).getBytes("utf-8")).sendAsync());
    			futures.add(tiesDB.createField(tid, "field2" + j, "type2" + j, ("def2" + j).getBytes("utf-8")).sendAsync());
    			futures.add(tiesDB.createField(tid, "field3" + j, "type3" + j, ("def3" + j).getBytes("utf-8")).sendAsync());
    			
    			List<byte[]> fields = new ArrayList<byte[]>();
    			fields.add(schema.idFromName("field1" + j).getValue());
    			
    			futures.add(tiesDB.createIndex(tid, "index" + j, BigInteger.ONE, fields).sendAsync());

    			fields.add(schema.idFromName("field2" + j).getValue());
    			futures.add(tiesDB.createIndex(tid, "index1" + j, BigInteger.valueOf(2), fields).sendAsync());

    			fields.add(schema.idFromName("field3" + j).getValue());
    			futures.add(tiesDB.createIndex(tid, "index2" + j, BigInteger.valueOf(4), fields).sendAsync());
    		}
    	}
		
    	for(int i=1; i<=TBLSPC_COUNT; ++i) {
        	log.info("Creating triggers for tablespace: ts" + i);
        	
    		byte[] tsid = schema.idFromName("ts" + i).getValue();
    		for(int j=1; j<=i && j <= TBL_COUNT; ++j) {
        		byte[] tid = schema.idFromName("ts" + i, "table" + j).getValue();
    			futures.add(tiesDB.createTrigger(tid, "trigger" + j, ("payload" + j).getBytes("utf-8")).sendAsync());
    		}
    	}
    	
    	log.info("Creating nodes");

    	TransactionManager tm1 = new SequentialFastRawTransactionManager(w3j, node1);
    	TransactionManager tm2 = new SequentialFastRawTransactionManager(w3j, node2);
    	TransactionManager tm3 = new SequentialFastRawTransactionManager(w3j, node3);
    	TransactionManager tm4 = new SequentialFastRawTransactionManager(w3j, node4);
    	
    	Registry registry1 = Registry.load(registry.getContractAddress(), w3j, tm1, GAS_PRICE, Contract.GAS_LIMIT);
    	Registry registry2 = Registry.load(registry.getContractAddress(), w3j, tm2, GAS_PRICE, Contract.GAS_LIMIT);
    	Registry registry3 = Registry.load(registry.getContractAddress(), w3j, tm3, GAS_PRICE, Contract.GAS_LIMIT);
    	Registry registry4 = Registry.load(registry.getContractAddress(), w3j, tm4, GAS_PRICE, Contract.GAS_LIMIT);

    	TieToken token1 = TieToken.load(token.getContractAddress(), w3j, tm1, GAS_PRICE, Contract.GAS_LIMIT);
    	TieToken token2 = TieToken.load(token.getContractAddress(), w3j, tm2, GAS_PRICE, Contract.GAS_LIMIT);
    	TieToken token3 = TieToken.load(token.getContractAddress(), w3j, tm3, GAS_PRICE, Contract.GAS_LIMIT);
    	TieToken token4 = TieToken.load(token.getContractAddress(), w3j, tm4, GAS_PRICE, Contract.GAS_LIMIT);
/*    	
    	byte[] sendToNodeCommand = new byte[]{0, 0, 0, 1};
    	futures.add(token1.transferAndPay(registry.getContractAddress(), BigInteger.TEN.pow(18), sendToNodeCommand, BigInteger.ZERO).sendAsync());
    	futures.add(token2.transferAndPay(registry.getContractAddress(), BigInteger.TEN.pow(19), sendToNodeCommand, BigInteger.ZERO).sendAsync());
    	futures.add(token3.transferAndPay(registry.getContractAddress(), BigInteger.TEN.pow(20), sendToNodeCommand, BigInteger.ZERO).sendAsync());
    	futures.add(token4.transfer(registry.getContractAddress(), BigInteger.TEN.pow(21), sendToNodeCommand).sendAsync());
*/    	

    	futures.add(token1.approve(registry.getContractAddress(), BigInteger.TEN.pow(18)).sendAsync());
    	futures.add(token2.approve(registry.getContractAddress(), BigInteger.TEN.pow(19)).sendAsync());
    	futures.add(token3.approve(registry.getContractAddress(), BigInteger.TEN.pow(20)).sendAsync());
    	futures.add(token4.approve(registry.getContractAddress(), BigInteger.TEN.pow(21)).sendAsync());

    	allOf(futures);
    	
    	futures.add(registry1.addNodeDeposit(BigInteger.TEN.pow(18)).sendAsync());
    	futures.add(registry2.addNodeDeposit(BigInteger.TEN.pow(19)).sendAsync());
    	futures.add(registry3.addNodeDeposit(BigInteger.TEN.pow(20)).sendAsync());
    	futures.add(registry4.addNodeDeposit(BigInteger.TEN.pow(21)).sendAsync());
    	
    	assertEquals(cfSetreg.get().getStatus(), "0x1", "Setting registry for TiesDB should be successful");

    	futures.add(registry1.acceptRanges(true).sendAsync());
    	futures.add(registry2.acceptRanges(true).sendAsync());
    	futures.add(registry3.acceptRanges(true).sendAsync());
    	futures.add(registry4.acceptRanges(true).sendAsync());
    	
    	allOf(futures);
    	
    	log.info("Distributing tables");
    	
    	for(int i=1; i<=TBLSPC_COUNT; ++i) {
    		byte[] tsid = schema.idFromName("ts" + i).getValue();
    		for(int j=1; j<=TBL_COUNT; ++j) {
    			if (j == 4 && i > 1)
    				continue; //Only ts1 has 4 tables
    			
        		byte[] tid = schema.idFromName("ts" + i, "table" + j).getValue();
        		futures.add(tiesDB.distribute(tid, BigInteger.valueOf(i), BigInteger.valueOf(j)).sendAsync());
    		}
    	}
    	
    	allOf(futures);
    	
    	log.info("Done");
    	
    	log.info("private static final String REGISTRY = \"" + registry.getContractAddress() + "\";");
    	log.info("private static final String TIETOKEN = \"" + token.getContractAddress() + "\";");
    	log.info("private static final String TIESDB = \"" + tiesDB.getContractAddress() + "\";");
    }

    /**
     * Rigorous Test :-)
     */
    @Test
    public void testApp()
    {
    	//schema is initialized by this time
        LinkedHashMap<Id, Tablespace> tss = schema.getTablespaces();
        assertEquals(3, tss.size(), "There should be 3 tablespaces");
        
        LinkedHashMap<Address, Node> nodes = schema.getNodes();
        assertEquals(4, nodes.size(), "There should be 4 nodes");
        
        Tablespace ts1 = tss.values().iterator().next();
        
        LinkedHashMap<Id, Table> tbls1 = ts1.getTables();
        assertEquals(4, tbls1.size(), "There should be 4 tables in Tablespace 1");

        Table tbl1 = tbls1.values().iterator().next();
        
        Node node = nodes.values().iterator().next();
        Ranges ranges = node.getTableRanges(tbl1.getId());
        
        List<Range> rs = ranges.getRanges();
        
        Tablespace ts3 = schema.getTablespace(schema.idFromName("ts3"));
        LinkedHashMap<Id, Table> tables = ts3.getTables();
        assertEquals(3, tables.size(), "There should be 3 tables in Tablespace 3");
        
        
    }
}
