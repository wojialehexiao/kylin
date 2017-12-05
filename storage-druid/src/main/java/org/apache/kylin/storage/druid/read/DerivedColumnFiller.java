/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.kylin.storage.druid.read;

import java.util.Objects;

import org.apache.kylin.common.util.Array;
import org.apache.kylin.dict.lookup.ILookupTable;
import org.apache.kylin.metadata.tuple.Tuple;

public class DerivedColumnFiller implements ColumnFiller {
    private final DerivedIndexMapping mapping;
    private final ILookupTable lookupTable;
    private final int numHostCols;
    private final int numDerivedCols;
    private final Array<String> key;

    DerivedColumnFiller(DerivedIndexMapping mapping, ILookupTable lookupTable) {
        this.mapping = mapping;
        this.lookupTable = lookupTable;
        this.numHostCols = mapping.numHostColumns();
        this.numDerivedCols = mapping.numDerivedColumns();
        this.key = new Array<>(new String[numHostCols]);
    }

    @Override
    public void fill(Object[] row, Tuple tuple) {
        for (int i = 0; i < numHostCols; i++) {
            key.data[i] = Objects.toString(row[mapping.getHostIndex(i)], null);
        }
        String[] lookupRow = lookupTable.getRow(key);
        for (int i = 0; i < numDerivedCols; i++) {
            if (mapping.getTupleIndex(i) >= 0) {
                String value = lookupRow == null ? null : lookupRow[mapping.getLookupIndex(i)];
                tuple.setDimensionValue(mapping.getTupleIndex(i), value);
            }
        }
    }
}
