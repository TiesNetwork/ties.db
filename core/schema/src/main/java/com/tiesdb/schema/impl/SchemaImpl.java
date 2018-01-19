package com.tiesdb.schema.impl;

import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.web3j.crypto.Credentials;
import org.web3j.crypto.Hash;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.Contract;
import org.web3j.tx.ManagedTransaction;

import com.tiesdb.schema.api.Node;
import com.tiesdb.schema.api.Schema;
import com.tiesdb.schema.api.Tablespace;
import com.tiesdb.schema.api.type.Address;
import com.tiesdb.schema.api.type.Id;
import com.tiesdb.schema.impl.contracts.TiesDB;

public class SchemaImpl implements Schema {
	Web3j w3j;
	TiesDB tiesDB;
    Credentials credentials;

	public SchemaImpl(String urlRpc, Credentials credentials, String tiesDBaddress){
		w3j = Web3j.build(new HttpService(urlRpc));
		this.credentials = credentials;
		this.tiesDB = TiesDB.load(tiesDBaddress, w3j, credentials, ManagedTransaction.GAS_PRICE, Contract.GAS_LIMIT);
	}
	
	public SchemaImpl(String urlRpc, Credentials credentials, Address tiesDBaddress){
		this(urlRpc, credentials, tiesDBaddress.toString());
	}
	
	public List<Tablespace> getTablespaceList() {
		List<Tablespace> list = new LinkedList<Tablespace>(); 
		List<byte[]> tss = (List<byte[]>)Utils.send(tiesDB.getTablespaceKeys());
		for(Iterator<byte[]> it = tss.iterator(); it.hasNext();) {
			Id id = new IdImpl(it.next());
			list.add(new TablespaceImpl(this, id));
		}
		return list;
	}

	public List<Node> getNodeList() {
		List<Node> list = new LinkedList<Node>(); 
		List<String> nodes = (List<String>)Utils.send(tiesDB.getNodes());
		for(Iterator<String> it = nodes.iterator(); it.hasNext();) {
			Address addr = new AddressImpl(it.next());
			list.add(new NodeImpl(this, addr));
		}
		return list;
	}

	@Override
	public Id idFromName(String name) {
		try {
			return new IdImpl(Hash.sha3(name.getBytes("utf-8")));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Id idFromName(String tablespace, String table) {
		return idFromName(tablespace + "#" + table);
	}

}
