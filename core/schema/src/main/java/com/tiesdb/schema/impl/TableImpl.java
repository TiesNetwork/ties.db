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

import java.math.BigInteger;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import org.web3j.tuples.generated.Tuple8;

import com.tiesdb.schema.api.Field;
import com.tiesdb.schema.api.Index;
import com.tiesdb.schema.api.Node;
import com.tiesdb.schema.api.Table;
import com.tiesdb.schema.api.Tablespace;
import com.tiesdb.schema.api.Trigger;
import com.tiesdb.schema.api.type.Address;
import com.tiesdb.schema.api.type.Id;

public class TableImpl extends NamedItemImpl implements Table {
    Tablespace ts;

    LinkedHashMap<Id, Field> fields;
    LinkedHashMap<Id, Index> indexes;
    LinkedHashMap<Id, Trigger> triggers;
    LinkedHashMap<Address, Node> nodes;
    int replicas;
    int ranges;
    Index primary;

    public TableImpl(Tablespace ts, Id id) {
        super(((TablespaceImpl) ts).schema, id);
        this.ts = ts;
    }

    @Override
    public boolean hasField(Id id) {
        load();
        return fields.containsKey(id);
    }

    @Override
    public boolean hasTrigger(Id id) {
        load();
        return triggers.containsKey(id);
    }

    @Override
    public LinkedHashMap<Id, Field> getFields() {
        load();
        return fields;
    }

    @Override
    public LinkedHashMap<Id, Trigger> getTriggers() {
        load();
        return triggers;
    }

    @Override
    public LinkedHashMap<Id, Index> getIndexes() {
        load();
        return indexes;
    }

    @Override
    public Field getField(Id id) {
        load();
        return fields.get(id);
    }

    @Override
    public Trigger getTrigger(Id id) {
        load();
        return triggers.get(id);
    }

    @Override
    public Tablespace getTablespace() {
        return ts;
    }

    @Override
    public boolean hasIndex(Id id) {
        load();
        return indexes.containsKey(id);
    }

    @Override
    public Index getIndex(Id id) {
        load();
        return indexes.get(id);
    }

    @Override
    protected void load() {
        if (notLoaded()) {
            Tuple8<String, String, List<byte[]>, List<byte[]>, List<byte[]>, BigInteger, BigInteger, List<String>> t = Utils
                    .send(schema.tiesDB.getTable(id.getValue()));
            name = t.getValue1();

            fields = new LinkedHashMap<Id, Field>();
            for (Iterator<byte[]> it = t.getValue3().iterator(); it.hasNext();) {
                Id id = new IdImpl(it.next());
                fields.put(id, new FieldImpl(this, id));
            }

            triggers = new LinkedHashMap<Id, Trigger>();
            for (Iterator<byte[]> it = t.getValue4().iterator(); it.hasNext();) {
                Id id = new IdImpl(it.next());
                triggers.put(id, new TriggerImpl(this, id));
            }

            indexes = new LinkedHashMap<Id, Index>();
            for (Iterator<byte[]> it = t.getValue5().iterator(); it.hasNext();) {
                Id id = new IdImpl(it.next());
                Index index = new IndexImpl(this, id);
                indexes.put(id, index);

                if (index.getType() == 1)
                    primary = index;
            }

            replicas = t.getValue6().intValue();
            ranges = t.getValue7().intValue();

            nodes = new LinkedHashMap<Address, Node>();
            for (Iterator<String> it = t.getValue8().iterator(); it.hasNext();) {
                Address id = new AddressImpl(it.next());
                Node node = schema.getNode(id);
                assert (node != null);
                nodes.put(id, node);
            }
        }

    }

    @Override
    public LinkedHashMap<Address, Node> getNodes() {
        load();
        return nodes;
    }

    @Override
    public Node getNode(Address id) {
        load();
        return nodes.get(id);
    }

    @Override
    public boolean hasField(String name) {
        return hasField(schema.idFromName(name));
    }

    @Override
    public boolean hasTrigger(String name) {
        return hasTrigger(schema.idFromName(name));
    }

    @Override
    public boolean hasIndex(String name) {
        return hasIndex(schema.idFromName(name));
    }

    @Override
    public Field getField(String name) {
        return getField(schema.idFromName(name));
    }

    @Override
    public Trigger getTrigger(String name) {
        return getTrigger(schema.idFromName(name));
    }

    @Override
    public Index getIndex(String name) {
        return getIndex(schema.idFromName(name));
    }

    @Override
    public boolean isDistributed() {
        load();
        return replicas != 0 && ranges != 0;
    }

}
