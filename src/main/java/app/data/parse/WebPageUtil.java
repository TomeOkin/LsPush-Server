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
package app.data.parse;

import app.data.model.WebPageInfo;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.validation.constraints.NotNull;
import java.io.IOException;

public class WebPageUtil {
    // http://www.atool.org/useragent.php
    public static final String GOOGLE_USER_AGENT =
        "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/52.0.2743.116 Safari/537.36";
    public static final String FIREFOX_USER_AGENT =
        "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/43.2357.125 Safari/537.36 OPR/30.0.1835.88";

    public static WebPageInfo parse(String url) throws IOException {
        WebPageInfo info = new WebPageInfo();
        info.url = url;

        // hit toutiao.io
        if (info.url.startsWith("http://toutiao.io/posts/")) {
            info.url = info.url.replace("/posts/", "/j/");
        }

        Document doc = Jsoup.connect(info.url).userAgent(GOOGLE_USER_AGENT).get();
        info.url = doc.baseUri(); // or doc.location()

        // hit gold.xitu.io
        if (info.url.startsWith("http://gold.xitu.io/entry/")) {
            Elements origin = doc.select("div[class=ellipsis]");
            Elements originLink = origin.select("a[class=share-link]");
            info.url = originLink.attr("href");

            // reconnect
            doc = Jsoup.connect(info.url).userAgent(GOOGLE_USER_AGENT).get();
            info.url = doc.baseUri(); // or doc.location()
        }

        info.url = smartLink(info.url);

        // get title
        Elements metaTitle = doc.select("meta[property=og:title]");
        if (metaTitle != null) {
            info.title = metaTitle.attr("content");
        }
        if (StringUtils.isEmpty(info.title)) {
            metaTitle = doc.select("meta[property=twitter:title]");
            if (metaTitle != null) {
                info.title = metaTitle.attr("content");
            }
            info.title = StringUtils.isEmpty(info.title) ? doc.title() : info.title;
        }

        // get desc
        Elements metaDesc = doc.select("meta[property=og:description]");
        if (metaDesc != null) {
            info.description = metaDesc.attr("content");
        }
        if (StringUtils.isEmpty(info.description)) {
            metaDesc = doc.select("meta[property=twitter:description]");
            if (metaDesc != null) {
                info.description = metaDesc.attr("content");
            }
            if (StringUtils.isEmpty(info.description)) {
                metaDesc = doc.select("meta[name=description]");
                if (metaDesc != null) {
                    info.description = metaDesc.attr("content");
                }
                if (StringUtils.isEmpty(info.description)) {
                    metaDesc = doc.body().select("p");
                    if (metaDesc != null) {
                        for (Element element : metaDesc) {
                            info.description = element.text();
                            if (info.description != null && info.description.length() >= 20) {
                                break;
                            }
                        }
                    }
                }
            }
        }
        info.description = ellipsis(info.description, 120, "...");

        return info;
    }

    public static String smartLink(String old) {
        String url = old;
        int query = url.lastIndexOf('?');
        if (query != -1) {
            url = url.substring(0, query);
        }
        query = url.lastIndexOf('#');
        if (query != -1) {
            url = url.substring(0, query);
        }
        return url;
    }

    /**
     * 提供多字节字符友好的字符串截断
     */
    public static String ellipsis(String text, int limit, @NotNull String append) {
        if (text.length() <= limit) {
            return text;
        }

        final int space = limit - append.length();
        int i = 0, next = 0;
        while (i + next <= space) {
            i += next;
            int unicode = Character.codePointAt(text, i);
            next = Character.charCount(unicode);
        }
        return text.substring(0, i) + append;
    }
}