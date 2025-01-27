/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.readwritesplitting.distsql.handler.update;

import org.apache.shardingsphere.infra.exception.rule.InvalidRuleConfigurationException;
import org.apache.shardingsphere.infra.exception.rule.MissingRequiredRuleException;
import org.apache.shardingsphere.infra.exception.resource.storageunit.MissingRequiredStorageUnitsException;
import org.apache.shardingsphere.distsql.segment.AlgorithmSegment;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.spi.exception.ServiceProviderNotFoundException;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.distsql.segment.ReadwriteSplittingRuleSegment;
import org.apache.shardingsphere.readwritesplitting.distsql.statement.AlterReadwriteSplittingRuleStatement;
import org.apache.shardingsphere.readwritesplitting.rule.ReadwriteSplittingRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlterReadwriteSplittingRuleExecutorTest {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereDatabase database;
    
    @Mock
    private ResourceMetaData resourceMetaData;
    
    private final AlterReadwriteSplittingRuleExecutor executor = new AlterReadwriteSplittingRuleExecutor();
    
    @BeforeEach
    void setUp() {
        when(database.getResourceMetaData()).thenReturn(resourceMetaData);
        executor.setDatabase(database);
    }
    
    @Test
    void assertCheckSQLStatementWithoutToBeAlteredRules() {
        ReadwriteSplittingRule rule = mock(ReadwriteSplittingRule.class);
        when(rule.getConfiguration()).thenReturn(new ReadwriteSplittingRuleConfiguration(Collections.emptyList(), Collections.emptyMap()));
        executor.setRule(rule);
        assertThrows(MissingRequiredRuleException.class, () -> executor.checkBeforeUpdate(createSQLStatement("TEST")));
    }
    
    @Test
    void assertCheckSQLStatementWithoutExistedResources() {
        when(resourceMetaData.getNotExistedDataSources(any())).thenReturn(Collections.singleton("read_ds_0"));
        ReadwriteSplittingRule rule = mock(ReadwriteSplittingRule.class);
        when(rule.getConfiguration()).thenReturn(createCurrentRuleConfiguration());
        executor.setRule(rule);
        assertThrows(MissingRequiredStorageUnitsException.class, () -> executor.checkBeforeUpdate(createSQLStatement("TEST")));
    }
    
    @Test
    void assertCheckSQLStatementWithoutToBeAlteredLoadBalancers() {
        when(database.getRuleMetaData().findRules(any())).thenReturn(Collections.emptyList());
        executor.setDatabase(database);
        ReadwriteSplittingRule rule = mock(ReadwriteSplittingRule.class);
        when(rule.getConfiguration()).thenReturn(createCurrentRuleConfiguration());
        executor.setRule(rule);
        assertThrows(ServiceProviderNotFoundException.class, () -> executor.checkBeforeUpdate(createSQLStatement("INVALID_TYPE")));
    }
    
    @Test
    void assertCheckSQLStatementWithDuplicateWriteResourceNamesInStatement() {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getResourceMetaData()).thenReturn(resourceMetaData);
        ReadwriteSplittingRule rule = mock(ReadwriteSplittingRule.class);
        when(rule.getConfiguration()).thenReturn(createCurrentRuleConfigurationWithMultipleRules());
        executor.setRule(rule);
        assertThrows(InvalidRuleConfigurationException.class,
                () -> executor.checkBeforeUpdate(createSQLStatementWithDuplicateWriteResourceNames("readwrite_ds_0", "readwrite_ds_1", "TEST")));
    }
    
    @Test
    void assertCheckSQLStatementWithDuplicateWriteResourceNames() {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getResourceMetaData()).thenReturn(resourceMetaData);
        ReadwriteSplittingRule rule = mock(ReadwriteSplittingRule.class);
        when(rule.getConfiguration()).thenReturn(createCurrentRuleConfigurationWithMultipleRules());
        executor.setRule(rule);
        assertThrows(InvalidRuleConfigurationException.class,
                () -> executor.checkBeforeUpdate(
                        createSQLStatement("readwrite_ds_0", "ds_write_1", Arrays.asList("read_ds_0", "read_ds_1"), "TEST")));
    }
    
    @Test
    void assertCheckSQLStatementWithDuplicateReadResourceNamesInStatement() {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getResourceMetaData()).thenReturn(resourceMetaData);
        ReadwriteSplittingRule rule = mock(ReadwriteSplittingRule.class);
        when(rule.getConfiguration()).thenReturn(createCurrentRuleConfigurationWithMultipleRules());
        executor.setRule(rule);
        assertThrows(InvalidRuleConfigurationException.class,
                () -> executor.checkBeforeUpdate(createSQLStatementWithDuplicateReadResourceNames("readwrite_ds_0", "readwrite_ds_1", "TEST")));
    }
    
    @Test
    void assertCheckSQLStatementWithDuplicateReadResourceNames() {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getResourceMetaData()).thenReturn(resourceMetaData);
        ReadwriteSplittingRule rule = mock(ReadwriteSplittingRule.class);
        when(rule.getConfiguration()).thenReturn(createCurrentRuleConfigurationWithMultipleRules());
        executor.setRule(rule);
        assertThrows(InvalidRuleConfigurationException.class,
                () -> executor.checkBeforeUpdate(createSQLStatement("readwrite_ds_1", "write_ds_1", Arrays.asList("read_ds_0_0", "read_ds_0_1"), "TEST")));
    }
    
    private AlterReadwriteSplittingRuleStatement createSQLStatement(final String loadBalancerTypeName) {
        ReadwriteSplittingRuleSegment ruleSegment = new ReadwriteSplittingRuleSegment("readwrite_ds", "write_ds", Arrays.asList("read_ds_0", "ds_read_ds_1"),
                new AlgorithmSegment(loadBalancerTypeName, new Properties()));
        return new AlterReadwriteSplittingRuleStatement(Collections.singleton(ruleSegment));
    }
    
    private AlterReadwriteSplittingRuleStatement createSQLStatement(final String ruleName, final String writeDataSource, final Collection<String> readDataSources, final String loadBalancerName) {
        ReadwriteSplittingRuleSegment ruleSegment = new ReadwriteSplittingRuleSegment(ruleName, writeDataSource, readDataSources, new AlgorithmSegment(loadBalancerName, new Properties()));
        return new AlterReadwriteSplittingRuleStatement(Collections.singleton(ruleSegment));
    }
    
    private AlterReadwriteSplittingRuleStatement createSQLStatementWithDuplicateWriteResourceNames(final String ruleName0, final String ruleName1, final String loadBalancerName) {
        ReadwriteSplittingRuleSegment ruleSegment0 = new ReadwriteSplittingRuleSegment(ruleName0, "write_ds", Arrays.asList("read_ds_0", "read_ds_1"),
                new AlgorithmSegment(loadBalancerName, new Properties()));
        ReadwriteSplittingRuleSegment ruleSegment1 = new ReadwriteSplittingRuleSegment(ruleName1, "write_ds", Arrays.asList("read_ds_2", "read_ds_3"),
                new AlgorithmSegment(loadBalancerName, new Properties()));
        return new AlterReadwriteSplittingRuleStatement(Arrays.asList(ruleSegment0, ruleSegment1));
    }
    
    private AlterReadwriteSplittingRuleStatement createSQLStatementWithDuplicateReadResourceNames(final String ruleName0, final String ruleName1, final String loadBalancerName) {
        ReadwriteSplittingRuleSegment ruleSegment0 = new ReadwriteSplittingRuleSegment(ruleName0, "write_ds_0", Arrays.asList("read_ds_0", "read_ds_1"),
                new AlgorithmSegment(loadBalancerName, new Properties()));
        ReadwriteSplittingRuleSegment ruleSegment1 = new ReadwriteSplittingRuleSegment(ruleName1, "write_ds_1", Arrays.asList("read_ds_0", "read_ds_1"),
                new AlgorithmSegment(loadBalancerName, new Properties()));
        return new AlterReadwriteSplittingRuleStatement(Arrays.asList(ruleSegment0, ruleSegment1));
    }
    
    private ReadwriteSplittingRuleConfiguration createCurrentRuleConfiguration() {
        ReadwriteSplittingDataSourceRuleConfiguration dataSourceRuleConfig =
                new ReadwriteSplittingDataSourceRuleConfiguration("readwrite_ds", "ds_write", Arrays.asList("read_ds_0", "read_ds_1"), "TEST");
        return new ReadwriteSplittingRuleConfiguration(new LinkedList<>(Collections.singleton(dataSourceRuleConfig)), Collections.emptyMap());
    }
    
    private ReadwriteSplittingRuleConfiguration createCurrentRuleConfigurationWithMultipleRules() {
        ReadwriteSplittingDataSourceRuleConfiguration dataSourceRuleConfig0 =
                new ReadwriteSplittingDataSourceRuleConfiguration("readwrite_ds_0", "ds_write_0", Arrays.asList("read_ds_0_0", "read_ds_0_1"), "TEST");
        ReadwriteSplittingDataSourceRuleConfiguration dataSourceRuleConfig1 =
                new ReadwriteSplittingDataSourceRuleConfiguration("readwrite_ds_1", "ds_write_1", Arrays.asList("read_ds_1_0", "read_ds_1_1"), "TEST");
        return new ReadwriteSplittingRuleConfiguration(new LinkedList<>(Arrays.asList(dataSourceRuleConfig0, dataSourceRuleConfig1)), Collections.emptyMap());
    }
}
