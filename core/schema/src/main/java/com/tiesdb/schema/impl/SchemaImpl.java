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
package com.tiesdb.schema.impl;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import org.web3j.crypto.Credentials;
import org.web3j.crypto.Hash;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.tuples.generated.Tuple2;
import org.web3j.tx.Contract;
import org.web3j.tx.ManagedTransaction;

import com.tiesdb.schema.api.Node;
import com.tiesdb.schema.api.Schema;
import com.tiesdb.schema.api.Table;
import com.tiesdb.schema.api.Tablespace;
import com.tiesdb.schema.api.type.Address;
import com.tiesdb.schema.api.type.Id;
import com.tiesdb.schema.impl.contracts.TiesDB;

public class SchemaImpl extends ItemImpl implements Schema {
    TiesDB tiesDB;

    LinkedHashMap<Id, Tablespace> tablespaces;
    LinkedHashMap<Address, Node> nodes;

    private HashMap<Id, Table> tables;

    public SchemaImpl(String urlRpc, Credentials credentials, String tiesDBaddress) {
        this(TiesDB.load(//
                tiesDBaddress, //
                Web3j.build(new HttpService(urlRpc)), //
                credentials, //
                ManagedTransaction.GAS_PRICE, //
                Contract.GAS_LIMIT));
    }

    public SchemaImpl(String urlRpc, Credentials credentials, Address tiesDBaddress) {
        this(urlRpc, credentials, tiesDBaddress.toString());
    }

    public SchemaImpl(TiesDB tiesDB) {
        super(null);
        this.tiesDB = tiesDB;
        schema = this;
    }

    @Override
    protected void load() {
        if (notLoaded()) {
            Tuple2<List<byte[]>, List<String>> t = Utils.send(tiesDB.getStorage());

            tablespaces = new LinkedHashMap<Id, Tablespace>();
            for (Iterator<byte[]> it = t.getValue1().iterator(); it.hasNext();) {
                Id id = new IdImpl(it.next());
                tablespaces.put(id, new TablespaceImpl(this, id));
            }

            nodes = new LinkedHashMap<Address, Node>();
            for (Iterator<String> it = t.getValue2().iterator(); it.hasNext();) {
                Address id = new AddressImpl(it.next());
                nodes.put(id, new NodeImpl(this, id));
            }

            tables = new HashMap<Id, Table>();
        }
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

    @Override
    public LinkedHashMap<Id, Tablespace> getTablespaces() {
        load();
        return tablespaces;
    }

    @Override
    public LinkedHashMap<Address, Node> getNodes() {
        load();
        return nodes;
    }

    @Override
    public Tablespace getTablespace(Id id) {
        load();
        return tablespaces.get(id);
    }

    @Override
    public Node getNode(Address address) {
        load();
        return nodes.get(address);
    }

    @Override
    protected boolean notLoaded() {
        return tablespaces == null;
    }

    Table createTable(Tablespace ts, Id id) {
        load();
        Table tbl = tables.get(id);
        if (tbl != null)
            return tbl;

        if (ts == null) {
            byte[] tsidb = Utils.send(tiesDB.tableToTablespace(id.getValue()));
            assert (tsidb != null && tsidb.length == 32);
            Id tsid = new IdImpl(tsidb);
            ts = tablespaces.get(tsid);
            assert (ts != null);
        }

        tbl = new TableImpl(ts, id);
        tables.put(id, tbl);
        return tbl;
    }

    Node createNode(Address address) {
        load();
        Node node = nodes.get(address);
        if (node != null)
            return node;

        assert (false); // Node should be in global repository!
        return node;
    }

    @Override
    public Tablespace getTablespace(String name) {
        Id id = idFromName(name);
        return getTablespace(id);
    }
}
