package com.tiesdb.protocol.v0r0.reader;

import java.util.HashMap;

import com.tiesdb.protocol.v0r0.reader.EntryHeaderReader.EntryHeader;
import com.tiesdb.protocol.v0r0.reader.FieldReader.Field;

public interface Entry {

    EntryHeader getHeader();

    HashMap<String, Field> getFields();

}