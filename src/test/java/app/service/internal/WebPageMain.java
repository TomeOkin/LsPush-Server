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

import app.data.model.WebPageInfo;
import app.data.parse.WebPageUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(JUnit4.class)
public class WebPageMain {
    private static final Logger logger = LoggerFactory.getLogger(WebPageMain.class);

    @Test
    public void test() throws Exception {
        WebPageInfo info = WebPageUtil.parse("http://www.jianshu.com/p/2a9fcf3c11e4#", null);
        logger.info(info.toString());
        info = WebPageUtil.parse("http://www.cnblogs.com/jasondan/p/3497757.html", null);
        logger.info(info.toString());
        info = WebPageUtil.parse("http://gold.xitu.io/entry/57cf7e4bbf22ec005f8ad230", null);
        logger.info(info.toString());
        info = WebPageUtil.parse("http://toutiao.io/posts/m0oht1", null);
        logger.info(info.toString());
        info =
            WebPageUtil.parse("http://yaq.qq.com/blog/10?hmsr=toutiao.io&utm_medium=toutiao.io&utm_source=toutiao.io",
                null);
        logger.info(info.toString());
    }
}
