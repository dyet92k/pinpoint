/*
 * Copyright 2016 Naver Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.web.dao.hbase.stat.v2;

import com.navercorp.pinpoint.common.server.bo.codec.stat.DirectBufferDecoder;
import com.navercorp.pinpoint.common.server.bo.stat.AgentStatType;
import com.navercorp.pinpoint.common.server.bo.stat.DirectBufferBo;
import com.navercorp.pinpoint.web.dao.stat.SampledDirectBufferDao;
import com.navercorp.pinpoint.web.mapper.stat.AgentStatMapperV2;
import com.navercorp.pinpoint.web.mapper.stat.SampledAgentStatResultExtractor;
import com.navercorp.pinpoint.web.mapper.stat.sampling.sampler.DirectBufferSampler;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.Range;
import com.navercorp.pinpoint.web.vo.stat.SampledDirectBuffer;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

/**
 * @author Roy Kim
 */
@Repository("sampledDirectBufferDaoV2")
public class HbaseSampledDirectBufferDaoV2 implements SampledDirectBufferDao {

    private final HbaseAgentStatDaoOperationsV2 operations;

    private final DirectBufferDecoder directBufferDecoder;
    private final DirectBufferSampler directBufferSampler;

    public HbaseSampledDirectBufferDaoV2(HbaseAgentStatDaoOperationsV2 operations, DirectBufferDecoder directBufferDecoder, DirectBufferSampler directBufferSampler) {
        this.operations = Objects.requireNonNull(operations, "operations");
        this.directBufferDecoder = Objects.requireNonNull(directBufferDecoder, "directBufferDecoder");
        this.directBufferSampler = Objects.requireNonNull(directBufferSampler, "directBufferSampler");
    }

    @Override
    public List<SampledDirectBuffer> getSampledAgentStatList(String agentId, TimeWindow timeWindow) {
        long scanFrom = timeWindow.getWindowRange().getFrom();
        long scanTo = timeWindow.getWindowRange().getTo() + timeWindow.getWindowSlotSize();
        Range range = Range.newRange(scanFrom, scanTo);
        AgentStatMapperV2<DirectBufferBo> mapper = operations.createRowMapper(directBufferDecoder, range);
        SampledAgentStatResultExtractor<DirectBufferBo, SampledDirectBuffer> resultExtractor = new SampledAgentStatResultExtractor<>(timeWindow, mapper, directBufferSampler);
        return operations.getSampledAgentStatList(AgentStatType.DIRECT_BUFFER, resultExtractor, agentId, range);
    }
}
