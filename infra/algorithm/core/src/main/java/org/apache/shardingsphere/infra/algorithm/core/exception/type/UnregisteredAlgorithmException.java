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

package org.apache.shardingsphere.infra.algorithm.core.exception.type;

import org.apache.shardingsphere.infra.algorithm.core.exception.AlgorithmDefinitionException;
import org.apache.shardingsphere.infra.exception.core.external.sql.identifier.SQLExceptionIdentifier;
import org.apache.shardingsphere.infra.exception.core.external.sql.sqlstate.XOpenSQLState;

import java.util.Collection;

/**
 * Unregistered algorithm exception.
 */
public final class UnregisteredAlgorithmException extends AlgorithmDefinitionException {
    
    private static final long serialVersionUID = -4570489906443880879L;
    
    public UnregisteredAlgorithmException(final String algorithmType, final String algorithmName, final SQLExceptionIdentifier sqlExceptionIdentifier) {
        super(XOpenSQLState.NOT_FOUND, 2, "'%s' algorithm '%s' on %s is unregistered.", algorithmType, algorithmName, sqlExceptionIdentifier);
    }
    
    public UnregisteredAlgorithmException(final String algorithmType, final Collection<String> algorithmNames, final SQLExceptionIdentifier sqlExceptionIdentifier) {
        super(XOpenSQLState.NOT_FOUND, 2, "'%s' algorithm '%s' on %s are unregistered.", algorithmType, String.join(", ", algorithmNames), sqlExceptionIdentifier);
    }
}
