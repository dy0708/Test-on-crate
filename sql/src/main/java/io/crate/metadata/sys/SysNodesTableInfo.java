/*
 * Licensed to CRATE Technology GmbH ("Crate") under one or more contributor
 * license agreements.  See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.  Crate licenses
 * this file to you under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.  You may
 * obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * However, if you have executed another commercial license agreement
 * with Crate these terms will supersede the license and you may use the
 * software solely pursuant to the terms of the relevant commercial agreement.
 */

package io.crate.metadata.sys;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.crate.analyze.WhereClause;
import io.crate.metadata.*;
import io.crate.planner.RowGranularity;
import io.crate.types.DataType;
import io.crate.types.DataTypes;
import io.crate.types.StringType;
import org.elasticsearch.cluster.ClusterService;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.cluster.node.DiscoveryNodes;
import org.elasticsearch.common.inject.Inject;

import java.util.*;

public class SysNodesTableInfo extends SysTableInfo {

    public static final TableIdent IDENT = new TableIdent(SCHEMA, "nodes");
    private static final String[] PARTITIONS = new String[]{IDENT.name()};

    private static final ImmutableList<String> primaryKey = ImmutableList.of("id");

    public static final Map<ColumnIdent, ReferenceInfo> INFOS = new LinkedHashMap<>();
    private static final LinkedHashSet<ReferenceInfo> columns = new LinkedHashSet<>();

    static {
        register("id", DataTypes.STRING, null);
        register("name", DataTypes.STRING, null);
        register("hostname", DataTypes.STRING, null);
        register("port", DataTypes.OBJECT, null);
        register("port", DataTypes.INTEGER, ImmutableList.of("http"));
        register("port", DataTypes.INTEGER, ImmutableList.of("transport"));
        register("load", DataTypes.OBJECT, null);
        register("load", DataTypes.DOUBLE, ImmutableList.of("1"));
        register("load", DataTypes.DOUBLE, ImmutableList.of("5"));
        register("load", DataTypes.DOUBLE, ImmutableList.of("15"));
        register("mem", DataTypes.OBJECT, null);
        register("mem", DataTypes.LONG, ImmutableList.of("free"));
        register("mem", DataTypes.LONG, ImmutableList.of("used"));
        register("mem", DataTypes.SHORT, ImmutableList.of("free_percent"));
        register("mem", DataTypes.SHORT, ImmutableList.of("used_percent"));
        register("heap", DataTypes.OBJECT, null);
        register("heap", DataTypes.LONG, ImmutableList.of("free"));
        register("heap", DataTypes.LONG, ImmutableList.of("used"));
        register("heap", DataTypes.LONG, ImmutableList.of("max"));
        register("fs", DataTypes.OBJECT, null);
        register("fs", DataTypes.LONG, ImmutableList.of("total"));
        register("fs", DataTypes.LONG, ImmutableList.of("free"));
        register("fs", DataTypes.LONG, ImmutableList.of("used"));
        register("fs", DataTypes.DOUBLE, ImmutableList.of("free_percent"));
        register("fs", DataTypes.DOUBLE, ImmutableList.of("used_percent"));
        register("version", DataTypes.OBJECT, null);
        register("version", StringType.INSTANCE, ImmutableList.of("number"));
        register("version", StringType.INSTANCE, ImmutableList.of("build_hash"));
        register("version", DataTypes.BOOLEAN, ImmutableList.of("build_snapshot"));
    }

    private final ClusterService clusterService;

    @Inject
    public SysNodesTableInfo(ClusterService service) {
        clusterService = service;
    }

    private static ReferenceInfo register(String column, DataType type, List<String> path) {
        ReferenceInfo info = new ReferenceInfo(new ReferenceIdent(IDENT, column, path), RowGranularity.NODE, type);
        if (info.ident().isColumn()) {
            columns.add(info);
        }
        INFOS.put(info.ident().columnIdent(), info);
        return info;
    }

    @Override
    public ReferenceInfo getColumnInfo(ColumnIdent columnIdent) {
        return INFOS.get(columnIdent);
    }

    @Override
    public Collection<ReferenceInfo> columns() {
        return columns;
    }

    @Override
    public RowGranularity rowGranularity() {
        return RowGranularity.NODE;
    }

    @Override
    public TableIdent ident() {
        return IDENT;
    }

    @Override
    public Routing getRouting(WhereClause whereClause) {
        DiscoveryNodes nodes = clusterService.state().nodes();
        ImmutableMap.Builder<String, Map<String, Set<Integer>>> builder = ImmutableMap.builder();

        for (DiscoveryNode node : nodes) {
            builder.put(node.id(), ImmutableMap.<String, Set<Integer>>of());
        }

        return new Routing(builder.build());
    }

    @Override
    public List<String> primaryKey() {
        return primaryKey;
    }

    @Override
    public String[] concreteIndices() {
        return PARTITIONS;
    }

    @Override
    public Iterator<ReferenceInfo> iterator() {
        return INFOS.values().iterator();
    }
}
