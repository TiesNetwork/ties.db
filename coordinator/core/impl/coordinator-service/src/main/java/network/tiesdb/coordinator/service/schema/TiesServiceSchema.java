package network.tiesdb.coordinator.service.schema;

import static java.util.Objects.requireNonNull;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import network.tiesdb.schema.api.TiesSchema;
import network.tiesdb.schema.api.TiesSchema.Field;
import network.tiesdb.schema.api.TiesSchema.IndexType;
import network.tiesdb.router.api.TiesRouter.Node;
import network.tiesdb.schema.api.TiesSchema.Table;
import network.tiesdb.schema.api.TiesSchema.Tablespace;
import network.tiesdb.service.scope.api.TiesServiceScopeException;

public class TiesServiceSchema {

    private static final Logger LOG = LoggerFactory.getLogger(TiesServiceSchema.class);

    public static class CacheKey {

        private final String tablespace;
        private final String table;

        public CacheKey(String tablespaceName, String tableName) {
            this.tablespace = tablespaceName;
            this.table = tableName;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((table == null) ? 0 : table.hashCode());
            result = prime * result + ((tablespace == null) ? 0 : tablespace.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            CacheKey other = (CacheKey) obj;
            if (table == null) {
                if (other.table != null)
                    return false;
            } else if (!table.equals(other.table))
                return false;
            if (tablespace == null) {
                if (other.tablespace != null)
                    return false;
            } else if (!tablespace.equals(other.tablespace))
                return false;
            return true;
        }
    }

    public static class SchemaCache extends ConcurrentHashMap<CacheKey, Set<FieldDescription>> {
        private static final long serialVersionUID = -5958833752316194583L;
    }

    public static class FieldDescription {

        private final String name;
        private final String type;
        private final boolean isPrimaryKey;

        public FieldDescription(String name, String type, boolean isPrimaryKey) {
            this.name = name;
            this.type = type.toLowerCase();
            this.isPrimaryKey = isPrimaryKey;
        }

        public String getName() {
            return name;
        }

        public String getType() {
            return type;
        }

        public boolean isPrimaryKey() {
            return isPrimaryKey;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((name == null) ? 0 : name.hashCode());
            result = prime * result + ((type == null) ? 0 : type.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            FieldDescription other = (FieldDescription) obj;
            if (name == null) {
                if (other.name != null)
                    return false;
            } else if (!name.equals(other.name))
                return false;
            if (type == null) {
                if (other.type != null)
                    return false;
            } else if (!type.equals(other.type))
                return false;
            return true;
        }
    }

    public static class EndpointCache extends ConcurrentHashMap<CacheKey, Set<Node>> {
        private static final long serialVersionUID = -5958833752316194583L;
    }

    private final TiesSchema schema;
    private final SchemaCache schemaCache = new SchemaCache();
    private final EndpointCache endpointCache = new EndpointCache();

    public TiesServiceSchema(TiesSchema schema) {
        this.schema = schema;
    }

    public Set<FieldDescription> getFields(String tablespaceName, String tableName) {
        CacheKey key = new CacheKey(tablespaceName, tableName);
        Set<FieldDescription> fields = schemaCache.get(key);
        if (null == fields) {
            fields = schemaCache.computeIfAbsent(key, this::createSchemaCache);
        }
        return fields;
    }

    public Set<Node> getNodes(String tablespaceName, String tableName) {
        CacheKey key = new CacheKey(tablespaceName, tableName);
        Set<Node> nodes = endpointCache.get(key);
        if (null == nodes) {
            nodes = endpointCache.computeIfAbsent(key, this::createNodeCache);
        }
        return nodes;
    }

    // TODO synchronize on key to avoid whole cache locking
    private synchronized Set<Node> createNodeCache(CacheKey key) {
        return loadSchemaNodes(key, new HashSet<>());
    }

    // TODO synchronize on key to avoid whole cache locking
    private synchronized Set<FieldDescription> createSchemaCache(CacheKey key) {
        return loadSchemaFields(key, new HashSet<>());
    }

    private static void checkForInvalidModifications(Set<FieldDescription> refList, Set<FieldDescription> conList) {
        Iterator<FieldDescription> refIter = refList.iterator();
        Iterator<FieldDescription> conIter = conList.iterator();
        while (refIter.hasNext() && conIter.hasNext()) {
            FieldDescription ref = refIter.next();
            FieldDescription con;
            do {
                con = conIter.next();
                if (ref.equals(con) && (ref.isPrimaryKey && !con.isPrimaryKey)) {
                    throw new IllegalStateException("Field `" + ref.getName() + "`:" + ref.getType() + " was removed from primary keys");
                } else {
                    continue;
                }
            } while (conIter.hasNext());
        }
        if (refIter.hasNext()) {
            FieldDescription ref = refIter.next();
            throw new IllegalStateException("Field `" + ref.getName() + "`:" + ref.getType() + " was deleted from contract");
        }
    }

    void retryUpdateFailedDescriptors() {
        // NOP
    }

    void garbadgeCleanup() {
        // NOP
    }

    void updateAllDescriptors() {
        LOG.debug("Start updating schema: {}", schema);
        SchemaCache schemaCacheUpdated = new SchemaCache();
        schemaCache.forEach((k, v) -> {
            updateSchemaCache(k, v, schemaCacheUpdated);
        });
        schemaCache.putAll(schemaCacheUpdated);
        LOG.debug("Updating schema finished for: {}", schema);
    }

    private void updateSchemaCache(CacheKey cacheKey, Set<FieldDescription> cachedDescriptions, SchemaCache schemaCacheUpdated) {
        LOG.debug("Start updating: `{}`.`{}`", cacheKey.tablespace, cacheKey.table);
        try {
            Set<FieldDescription> contractDescriptions = loadSchemaFields(cacheKey, new HashSet<>());
            try {
                checkForInvalidModifications(cachedDescriptions, contractDescriptions);
            } catch (Throwable e) {
                throw new TiesServiceScopeException(
                        "Illegal schema `" + cacheKey.tablespace + "`.`" + cacheKey.table + "` modification detected", e);
            }
            if (cachedDescriptions.equals(contractDescriptions)) {
                LOG.debug("Update succeeded with no changes for: `{}`.`{}`", cacheKey.tablespace, cacheKey.table);
            } else {
                schemaCacheUpdated.put(cacheKey, contractDescriptions);
                LOG.debug("Update succeeded for: `{}`.`{}`", cacheKey.tablespace, cacheKey.table);
            }
        } catch (Throwable e) {
            LOG.error("Update failed for: `{}`.`{}`", cacheKey.tablespace, cacheKey.table, e);
        }
    }

    private Set<FieldDescription> loadSchemaFields(CacheKey key, Set<FieldDescription> descriptions) {
        Tablespace ts = schema.getTablespace(key.tablespace);
        requireNonNull(ts, "Tablespace not found");
        Table t = ts.getTable(key.table);
        requireNonNull(ts, "Table not found");

        t.getIndexes().forEach(idx -> {
            if (IndexType.PRIMARY.equals(idx.getType())) {
                idx.getFields().forEach(fld -> {
                    descriptions.add(new FieldDescription(fld.getName(), fld.getType(), true));
                });
            }
        });

        t.getFieldNames().forEach(fn -> {
            Field f = t.getField(fn);
            descriptions.add(new FieldDescription(fn, f.getType(), false));
        });

        return descriptions;
    }

    private Set<Node> loadSchemaNodes(CacheKey key, HashSet<Node> nodeset) {
        String sna = System.getProperty("network.tiesdb.debug.SingleNodeAddress");
        if (null != sna) {
            nodeset.add(new Node() {

                @Override
                public short getNodeNetwork() {
                    return schema.getSchemaNetwork();
                }

                @Override
                public String getAddressString() {
                    return sna;
                }

                @Override
                public String toString() {
                    return "SingleDebugNode(" + sna + ")";
                }

            });
        }
        return nodeset;
    }

}
