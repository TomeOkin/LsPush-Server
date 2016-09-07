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
package app.service;

import app.data.model.WebPageInfo;
import app.data.parse.WebPageUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebPageMain {
    private static final Logger logger = LoggerFactory.getLogger(WebPageMain.class);

    public static void main(String[] args) throws Exception {
        WebPageInfo info = WebPageUtil.parse("http://www.jianshu.com/p/2a9fcf3c11e4#");
        logger.info(info.toString());
        info = WebPageUtil.parse("http://www.cnblogs.com/jasondan/p/3497757.html");
        logger.info(info.toString());
        info = WebPageUtil.parse("http://gold.xitu.io/entry/57cf7e4bbf22ec005f8ad230");
        logger.info(info.toString());
        info = WebPageUtil.parse("http://toutiao.io/posts/m0oht1");
        logger.info(info.toString());
        info =
            WebPageUtil.parse("http://yaq.qq.com/blog/10?hmsr=toutiao.io&utm_medium=toutiao.io&utm_source=toutiao.io");
        logger.info(info.toString());
    }
}
