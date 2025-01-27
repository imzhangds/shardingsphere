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

package org.apache.shardingsphere.sharding.algorithm.sharding.range;

import com.google.common.collect.Range;
import com.google.common.math.LongMath;
import org.apache.shardingsphere.infra.algorithm.core.exception.type.AlgorithmInitializationException;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;

import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Volume based range sharding algorithm.
 */
public final class VolumeBasedRangeShardingAlgorithm extends AbstractRangeShardingAlgorithm {
    
    private static final String RANGE_LOWER_KEY = "range-lower";
    
    private static final String RANGE_UPPER_KEY = "range-upper";
    
    private static final String SHARDING_VOLUME_KEY = "sharding-volume";
    
    @Override
    public Map<Integer, Range<Comparable<?>>> calculatePartitionRange(final Properties props) {
        ShardingSpherePreconditions.checkState(props.containsKey(RANGE_LOWER_KEY), () -> new AlgorithmInitializationException(this, "Lower range cannot be null."));
        ShardingSpherePreconditions.checkState(props.containsKey(RANGE_UPPER_KEY), () -> new AlgorithmInitializationException(this, "Upper range cannot be null."));
        ShardingSpherePreconditions.checkState(props.containsKey(SHARDING_VOLUME_KEY), () -> new AlgorithmInitializationException(this, "Sharding volume cannot be null."));
        long lower = Long.parseLong(props.getProperty(RANGE_LOWER_KEY));
        long upper = Long.parseLong(props.getProperty(RANGE_UPPER_KEY));
        long volume = Long.parseLong(props.getProperty(SHARDING_VOLUME_KEY));
        ShardingSpherePreconditions.checkState(upper - lower >= volume, () -> new AlgorithmInitializationException(this, "Range can not be smaller than volume."));
        int partitionSize = Math.toIntExact(LongMath.divide(upper - lower, volume, RoundingMode.CEILING));
        Map<Integer, Range<Comparable<?>>> result = new HashMap<>(partitionSize + 2, 1F);
        result.put(0, Range.lessThan(lower));
        for (int i = 0; i < partitionSize; i++) {
            result.put(i + 1, Range.closedOpen(lower + i * volume, Math.min(lower + (i + 1) * volume, upper)));
        }
        result.put(partitionSize + 1, Range.atLeast(upper));
        return result;
    }
    
    @Override
    public String getType() {
        return "VOLUME_RANGE";
    }
}
