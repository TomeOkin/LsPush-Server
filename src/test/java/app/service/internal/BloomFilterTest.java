/*
 * Copyright 2016 TomeOkin
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
package app.service.internal;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnel;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.nio.charset.Charset;
import java.util.concurrent.ExecutionException;

@RunWith(JUnit4.class)
public class BloomFilterTest {
    final Funnel<String> stringFunnel = (Funnel<String>) (from, into) -> into.putString(from, Charset.forName("UTF-8"));
    BloomFilter<String> authCodeFilter = BloomFilter.create(stringFunnel, 5000, 0.01);
    int count;
    int i = 0;

    @Test
    public void bloomFilterTest() throws ExecutionException {
        for (; i < 1000000; i++) {
            generateAuthCode(6);
        }
        System.out.println("count is " + count);
        Assert.assertTrue(count <= (i / 10000 * 45));
    }

    private String generateAuthCode(int length) throws ExecutionException {
        if (length <= 4) {
            length = 4;
        }

        String authCode;
        authCode = RandomStringUtils.random(length, false, true);
        if (authCodeFilter.mightContain(authCode)) {
            if (authCodeFilter.expectedFpp() >= 0.01f) {
                resetBloomFilter();
            }
            authCode = RandomStringUtils.random(length, false, true);
            System.out.println("i is " + i);
            count++;
        }
        authCodeFilter.put(authCode); // mask authCode

        return authCode;
    }

    private void resetBloomFilter() {
        // 生成一个六位的数字字符串时，每 200 个独特的选择中只选择 1 个，一方面用于减少重复率，另一方面避免被推断出生成的数字
        authCodeFilter = BloomFilter.create(stringFunnel, 5000, 0.01);
    }
}
