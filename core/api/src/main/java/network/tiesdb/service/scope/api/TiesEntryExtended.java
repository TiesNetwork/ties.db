package network.tiesdb.service.scope.api;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface TiesEntryExtended extends TiesEntry {

    interface TypedFiled extends Field {

        interface Visitor<T> {

            T on(TypedHashField hashField) throws TiesServiceScopeException;

            T on(TypedValueField valueField) throws TiesServiceScopeException;

        }

        <T> T accept(TiesEntryExtended.TypedFiled.Visitor<T> v) throws TiesServiceScopeException;

        String getType();

    }

    interface TypedHashField extends TypedFiled, HashField {

        @Override
        default <T> T accept(TiesEntryExtended.TypedFiled.Visitor<T> v) throws TiesServiceScopeException {
            return v.on(this);
        }

    }

    interface TypedValueField extends TypedFiled, ValueField {

        Object getObject();

        @SuppressWarnings("unchecked")
        default <T> T get() {
            return (T) getObject();
        }

        @Override
        default <T> T accept(TiesEntryExtended.TypedFiled.Visitor<T> v) throws TiesServiceScopeException {
            return v.on(this);
        }

    }

    String getTablespaceName();

    String getTableName();

    Map<String, TypedHashField> getFieldHashes();

    Map<String, TypedValueField> getFieldValues();

    @Override
    default List<? extends TypedFiled> getFields() {
        return Stream.concat(getFieldHashes().values().stream(), getFieldValues().values().stream()).collect(Collectors.toList());
    }

}