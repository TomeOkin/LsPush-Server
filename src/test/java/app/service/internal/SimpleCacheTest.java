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

import app.data.model.internal.Captcha;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

@RunWith(JUnit4.class)
public class SimpleCacheTest {
    @Test
    public void cacheKeyTest() {
        Cache<String, Captcha> authCodeMap = CacheBuilder.newBuilder()
            .initialCapacity(100)
            .maximumSize(500)
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .build();
        Captcha captcha = new Captcha();
        authCodeMap.put("123", captcha);
        Captcha one = authCodeMap.getIfPresent("123");
        Assert.assertNotNull(one);
    }

    @Test
    public void cacheExpireTest() {
        Cache<Integer, Val> cache = CacheBuilder.newBuilder()
            .initialCapacity(2)
            .maximumSize(4)
            .expireAfterWrite(1300, TimeUnit.MILLISECONDS)
            .build();
        cache.put(1, new Val());
        cache.put(2, new Val());
        Val i = cache.getIfPresent(1);
        if (i != null) {
            System.out.println(i.i);
            i.i++;
        } else {
            System.out.println("i is null");
        }

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                System.out.println("ssssssssssssssssssssssssssssss");
                Val i = cache.getIfPresent(1);
                if (i != null) {
                    System.out.println(i.i);
                    i.i++;
                    cache.put(1, i);
                } else {
                    System.out.println("i is null");
                }
                Val j = cache.getIfPresent(2);
                if (j != null) {
                    System.out.println(j.i);
                } else {
                    System.out.println("j is null");
                }
                if (i != null && i.i == 3) {
                    cache.put(3, new Val());
                }
            }
        }, 500, 500);
    }

    static class Val {
        public Integer i = 0;
    }
}
