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
import com.google.common.cache.Cache;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.UriComponentsBuilder;

import javax.validation.constraints.NotNull;
import java.io.IOException;

public class WebPageUtil {
    private static final Logger logger = LoggerFactory.getLogger(WebPageUtil.class);

    // http://www.atool.org/useragent.php
    public static final String GOOGLE_USER_AGENT =
        "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/52.0.2743.116 Safari/537.36";
    public static final String FIREFOX_USER_AGENT =
        "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/43.2357.125 Safari/537.36 OPR/30.0.1835.88";

    public static WebPageInfo parse(String url, Cache<String, WebPageInfo> urlInfoCache) throws IOException {
        String original = url;

        // hit toutiao.io
        // fixme http://toutiao.io/shares/640539/url
        if (original.startsWith("https://toutiao.io/posts/")) {
            original = original.replace("/posts/", "/k/");
        }

        // check cache
        WebPageInfo info = urlInfoCache != null ? urlInfoCache.getIfPresent(original) : null;
        if (info != null) {
            return info;
        } else {
            info = new WebPageInfo();
            info.url = original;
        }

        // attach url
        Document doc = requestUrl(info.url);
        info.url = doc.baseUri(); // or doc.location()

        // hit gold.xitu.io
        if (info.url.startsWith("http://gold.xitu.io/entry/")) {
            Elements origin = doc.select("div[class=ellipsis]");
            Elements originLink = origin.select("a[class=share-link]");
            info.url = originLink.attr("href");

            // reconnect
            doc = requestUrl(info.url);
            info.url = doc.baseUri(); // or doc.location()
        }

        info.url = smartUri(info.url);

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
        info.description = ellipsis(info.description, 140, "...");

        // cache info
        if (urlInfoCache != null) {
            urlInfoCache.put(original, info);
        }
        return info;
    }

    private static Document requestUrl(String url) throws IOException {
        return Jsoup.connect(url).userAgent(GOOGLE_USER_AGENT).timeout(20000).validateTLSCertificates(false).get();
    }

    //    public static String smartLink(String old) {
    //        if (old.contains("http://mp.weixin.qq.com/")) {
    //            return old;
    //        }
    //
    //        String url = old;
    //        int query = url.lastIndexOf('?');
    //        if (query != -1) {
    //            url = url.substring(0, query);
    //        }
    //        query = url.lastIndexOf('#');
    //        if (query != -1) {
    //            url = url.substring(0, query);
    //        }
    //
    //        logger.info("smartUri: {}", smartUri(old));
    //        return url;
    //    }

    public static String smartUri(String old) {
        return UriComponentsBuilder.fromUriString(old)
            .replaceQueryParam("utm_source")
            .replaceQueryParam("utm_medium")
            .replaceQueryParam("utm_campaign")
            .replaceQueryParam("utm_term")
            .replaceQueryParam("utm_content")
            .replaceQueryParam("hmsr")
            .build()
            .toUriString();
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
