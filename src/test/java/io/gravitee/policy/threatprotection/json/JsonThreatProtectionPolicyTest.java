/**
 * Copyright (C) 2015 The Gravitee team (http://gravitee.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.gravitee.policy.threatprotection.json;

import io.gravitee.common.http.HttpHeaders;
import io.gravitee.common.http.MediaType;
import io.gravitee.gateway.api.Request;
import io.gravitee.gateway.api.Response;
import io.gravitee.gateway.api.buffer.Buffer;
import io.gravitee.gateway.api.stream.ReadWriteStream;
import io.gravitee.policy.api.PolicyChain;
import io.gravitee.policy.api.PolicyResult;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import static io.gravitee.policy.threatprotection.json.JsonThreatProtectionPolicy.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author Jeoffrey HAEYAERT (jeoffrey.haeyaert at graviteesource.com)
 * @author GraviteeSource Team
 */
@RunWith(MockitoJUnitRunner.class)
public class JsonThreatProtectionPolicyTest {

    @Mock
    private Request request;

    @Mock
    private Response response;

    @Mock
    private PolicyChain policyChain;

    @Captor
    private ArgumentCaptor<PolicyResult> resultCaptor;

    private static final String JSON = "{\n" +
            "    \"travel\": {\n" +
            "        \"type\": \"TOURISM\",\n" +
            "        \"language\": \"FR\",\n" +
            "        \"isPolicyHolderTravelling\": true,\n" +
            "        \"start\": \"2019-06-03\",\n" +
            "        \"end\": \"2019-06-05\"\n" +
            "    },\n" +
            "    \"policyHolder\": {\n" +
            "        \"civility\": \"xxxxxx\",\n" +
            "        \"firstname\": \"xxxxxx\",\n" +
            "        \"lastname\": \"xxxxxx\",\n" +
            "        \"maidenname\": \"xxxxxx\",\n" +
            "        \"id\": \"45465\",\n" +
            "        \"phone\": \"0606060607\",\n" +
            "        \"professionalPhone\": \"0101010101\",\n" +
            "        \"email\": \"xxxxxxxxxxx\",\n" +
            "        \"address\": {\n" +
            "            \"street\": \"my street\",\n" +
            "            \"zipcode\": \"xxxxxx\",\n" +
            "            \"city\": \"xxxxxx\",\n" +
            "            \"countryCode\": \"xxxxxx\"\n" +
            "        }\n" +
            "    },\n" +
            "    \"beneficiaries\": [\n" +
            "        {\n" +
            "            \"civility\": \"xxxx\",\n" +
            "            \"firstname\": \"xxxxx\",\n" +
            "            \"lastname\": \"xxxxx\",\n" +
            "            \"maidenname\": \"xxxxxxx\",\n" +
            "            \"relationtopolicyholder\": \"xxxxxxx\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"civility\": \"xxxx\",\n" +
            "            \"firstname\": \"xxxxx\",\n" +
            "            \"lastname\": \"xxxxx\",\n" +
            "            \"maidenname\": \"xxxxxxx\",\n" +
            "            \"relationtopolicyholder\": \"xxxxxxx\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"civility\": \"xxxx\",\n" +
            "            \"firstname\": \"xxxxx\",\n" +
            "            \"lastname\": \"xxxxx\",\n" +
            "            \"maidenname\": \"xxxxxxx\",\n" +
            "            \"relationtopolicyholder\": \"xxxxxxx\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"civility\": \"xxxx\",\n" +
            "            \"firstname\": \"xxxxx\",\n" +
            "            \"lastname\": \"xxxxx\",\n" +
            "            \"maidenname\": \"xxxxxxx\",\n" +
            "            \"relationtopolicyholder\": \"xxxxxxx\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"civility\": \"xxxx\",\n" +
            "            \"firstname\": \"xxxxx\",\n" +
            "            \"lastname\": \"xxxxx\",\n" +
            "            \"maidenname\": \"xxxxxxx\",\n" +
            "            \"relationtopolicyholder\": \"xxxxxxx\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"civility\": \"xxxx\",\n" +
            "            \"firstname\": \"xxxxx\",\n" +
            "            \"lastname\": \"xxxxx\",\n" +
            "            \"maidenname\": \"xxxxxxx\",\n" +
            "            \"relationtopolicyholder\": \"xxxxxxx\"\n" +
            "        }\n" +
            "    ]\n" +
            "}";


    JsonThreatProtectionPolicyConfiguration configuration;

    private JsonThreatProtectionPolicy cut;

    @Before
    public void before() {

        configuration = new JsonThreatProtectionPolicyConfiguration();
        configuration.setMaxArraySize(100);
        configuration.setMaxDepth(1000);
        configuration.setMaxEntries(100);
        configuration.setMaxNameLength(100);
        configuration.setMaxValueLength(100);

        cut = new JsonThreatProtectionPolicy(configuration);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        when(request.headers()).thenReturn(httpHeaders);
    }

    @Test
    public void shouldAcceptAllWhenContentTypeIsNotJson() {

        Mockito.reset(request);
        when(request.headers()).thenReturn(new HttpHeaders());
        ReadWriteStream<?> readWriteStream = cut.onRequestContent(request, policyChain);

        assertNull(readWriteStream);
    }

    @Test
    public void shouldAcceptValidJson() {

        ReadWriteStream<Buffer> readWriteStream = cut.onRequestContent(request, policyChain);

        assertNotNull(readWriteStream);

        readWriteStream.write(Buffer.buffer("{ \"valid\": true, \"array\": [ 1, 2, 3 ], \"container\": { \"a\": true } }"));
        readWriteStream.end();

        verifyZeroInteractions(policyChain);
    }

    @Test
    public void shouldRejectInvalidJson() {

        ReadWriteStream<Buffer> readWriteStream = cut.onRequestContent(request, policyChain);

        assertNotNull(readWriteStream);

        readWriteStream.write(Buffer.buffer("Invalid"));
        readWriteStream.end();

        verify(policyChain, times(1)).streamFailWith(resultCaptor.capture());
        assertEquals(JSON_THREAT_DETECTED_KEY, resultCaptor.getValue().key());
    }

    @Test
    public void shouldRejectWhenMaxNameLengthExceeded() {

        configuration.setMaxNameLength(4);
        ReadWriteStream<Buffer> readWriteStream = cut.onRequestContent(request, policyChain);

        assertNotNull(readWriteStream);

        readWriteStream.write(Buffer.buffer("{ \"valid\": true, \"array\": [ 1, 2, 3 ], \"container\": { \"a\": \"123456789\" } }"));
        readWriteStream.end();

        verify(policyChain, times(1)).streamFailWith(resultCaptor.capture());
        assertEquals(JSON_THREAT_MAX_NAME_LENGTH_KEY, resultCaptor.getValue().key());
    }

    @Test
    public void shouldRejectWhenMaxValueLengthExceeded() {

        configuration.setMaxValueLength(8);
        ReadWriteStream<Buffer> readWriteStream = cut.onRequestContent(request, policyChain);

        assertNotNull(readWriteStream);

        readWriteStream.write(Buffer.buffer("{ \"valid\": false, \"array\": [ 1, 2, 3 ], \"container\": { \"a\": \"123456789\" } }"));
        readWriteStream.end();

        verify(policyChain, times(1)).streamFailWith(resultCaptor.capture());
        assertEquals(JSON_THREAT_MAX_VALUE_LENGTH_KEY, resultCaptor.getValue().key());
    }

    @Test
    public void shouldRejectWhenMaxObjectEntriesExceeded() {

        configuration.setMaxEntries(2);
        ReadWriteStream<Buffer> readWriteStream = cut.onRequestContent(request, policyChain);

        assertNotNull(readWriteStream);

        readWriteStream.write(Buffer.buffer("{ \"valid\": false, \"array\": [ 1, 2, 3 ], \"container\": { \"a\": \"123456789\" } }"));
        readWriteStream.end();

        verify(policyChain, times(1)).streamFailWith(resultCaptor.capture());
        assertEquals(JSON_THREAT_MAX_ENTRIES_KEY, resultCaptor.getValue().key());
    }

    @Test
    public void shouldRejectWhenMaxArraySizeExceeded() {

        configuration.setMaxArraySize(2);
        ReadWriteStream<Buffer> readWriteStream = cut.onRequestContent(request, policyChain);

        assertNotNull(readWriteStream);

        readWriteStream.write(Buffer.buffer("{ \"valid\": false, \"array\": [ 1, 2, 3 ], \"container\": { \"a\": \"123456789\" } }"));
        readWriteStream.end();

        verify(policyChain, times(1)).streamFailWith(resultCaptor.capture());
        assertEquals(JSON_MAX_ARRAY_SIZE_KEY, resultCaptor.getValue().key());
    }

    @Test
    public void shouldRejectWhenMaxArrayOfObjectSizeExceeded() {

        configuration.setMaxArraySize(2);
        ReadWriteStream<Buffer> readWriteStream = cut.onRequestContent(request, policyChain);

        assertNotNull(readWriteStream);

        readWriteStream.write(Buffer.buffer(JSON));
        readWriteStream.end();

        verify(policyChain, times(1)).streamFailWith(any(PolicyResult.class));
    }

    @Test
    public void shouldRejectWhenMaxArraySizeExceededInSubArray() {

        configuration.setMaxArraySize(4);
        ReadWriteStream<Buffer> readWriteStream = cut.onRequestContent(request, policyChain);

        assertNotNull(readWriteStream);

        readWriteStream.write(Buffer.buffer("{\"a\":[\"1\",\"2\",\"3\",[\"1\",\"2\",\"3\",[\"1\",\"2\",\"3\",\"4\",\"5\"]]]}"));
        readWriteStream.end();

        verify(policyChain, times(1)).streamFailWith(resultCaptor.capture());
        assertEquals(JSON_MAX_ARRAY_SIZE_KEY, resultCaptor.getValue().key());
    }

    @Test
    public void shouldAcceptWhenMaxArraySizeDoesNotExceed() {

        configuration.setMaxArraySize(4);
        ReadWriteStream<Buffer> readWriteStream = cut.onRequestContent(request, policyChain);

        assertNotNull(readWriteStream);

        readWriteStream.write(Buffer.buffer("{\"a\":[\"1\",\"2\",\"3\",[\"1\",\"2\",\"3\",[\"1\",\"2\",\"3\",\"4\"]]]}"));
        readWriteStream.end();

        verifyNoInteractions(policyChain);
    }

    @Test
    public void shouldRejectWhenMaxDepthExceeded() {

        configuration.setMaxDepth(1);
        ReadWriteStream<Buffer> readWriteStream = cut.onRequestContent(request, policyChain);

        assertNotNull(readWriteStream);

        readWriteStream.write(Buffer.buffer("{ \"valid\": false, \"array\": [ 1, 2, 3 ], \"container\": { \"a\": \"123456789\" } }"));
        readWriteStream.end();

        verify(policyChain, times(1)).streamFailWith(resultCaptor.capture());
        assertEquals(JSON_THREAT_MAX_DEPTH_KEY, resultCaptor.getValue().key());
    }

    @Test
    public void shouldRejectWhenMaxDepthInArraysExceeded() {

        configuration.setMaxDepth(6);
        ReadWriteStream<Buffer> readWriteStream = cut.onRequestContent(request, policyChain);

        assertNotNull(readWriteStream);

        readWriteStream.write(Buffer.buffer("{ \"valid\": false, \"array\": [ 1, 2, 3 ], \"container\": { \"a\": [ [ [ [ [ \"123456789\" ] ] ] ] ] } }"));
        readWriteStream.end();

        verify(policyChain, times(1)).streamFailWith(resultCaptor.capture());
        assertEquals(JSON_THREAT_MAX_DEPTH_KEY, resultCaptor.getValue().key());
    }

    @Test
    public void shouldAcceptWhenMaxDepthInArraysDoesNotExceed() {

        configuration.setMaxDepth(7);
        ReadWriteStream<Buffer> readWriteStream = cut.onRequestContent(request, policyChain);

        assertNotNull(readWriteStream);

        readWriteStream.write(Buffer.buffer("{ \"valid\": false, \"array\": [ 1, 2, 3 ], \"container\": { \"a\": [ [ [ [ [ \"123456789\" ] ] ] ] ] } }"));
        readWriteStream.end();

        verifyNoInteractions(policyChain);
    }
}