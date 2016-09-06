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

import app.config.ResultCode;
import app.data.model.BaseResponse;
import app.data.model.internal.FileResource;
import app.data.model.internal.StringResource;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

@Service
public class ResourceService {
    public static final int RESOURCE_TYPE_AVATAR = 1;
    public static final int RESOURCE_TYPE_COMMON = 2;

    public static final String RESOURCE_AVATAR_PATH = "image/avatar";
    public static final String RESOURCE_COMMON_PATH = "image/common";

    private static final Logger logger = LoggerFactory.getLogger(ResourceService.class);

    public ResourceService() {
        resolvePath(RESOURCE_AVATAR_PATH);
        resolvePath(RESOURCE_COMMON_PATH);
    }

    public int upload(MultipartFile file, int resourceType, StringResource resource) {
        if (file.isEmpty()) {
            return ResultCode.FILE_EMPTY;
        }

        String path;
        if (resourceType == RESOURCE_TYPE_AVATAR) {
            path = RESOURCE_AVATAR_PATH;
        } else if (resourceType == RESOURCE_TYPE_COMMON) {
            path = RESOURCE_COMMON_PATH;
        } else {
            return ResultCode.UNKNOWN_RESOURCE;
        }

        resolvePath(path);
        String filename = resolveFilename(file.getOriginalFilename());
        try {
            OutputStream out = new FileOutputStream(new File(path + "/" + filename));
            BufferedOutputStream stream = new BufferedOutputStream(out);
            FileCopyUtils.copy(file.getInputStream(), stream);
            stream.close();
            resource.filename = filename;
        } catch (Exception e) {
            logger.warn("upload file failure", e);
            return ResultCode.UPLOAD_FILE_FAILURE;
        }
        return BaseResponse.COMMON_SUCCESS;
    }

    public int download(String filename, int resourceType, FileResource resource) {
        String path;
        if (resourceType == RESOURCE_TYPE_AVATAR) {
            path = RESOURCE_AVATAR_PATH;
        } else if (resourceType == RESOURCE_TYPE_COMMON) {
            path = RESOURCE_COMMON_PATH;
        } else {
            return ResultCode.UNKNOWN_RESOURCE;
        }

        resolvePath(path);
        File file = new File(path + "/" + filename);
        if (!file.exists() || !file.isFile()) {
            return ResultCode.RESOURCE_NOT_EXIT;
        }
        resource.file = file;
        return BaseResponse.COMMON_SUCCESS;
    }

    private String resolveFilename(String old) {
        String prefix, filename = RandomStringUtils.random(48, true, true);
        int start = old.lastIndexOf('.');
        if (start != -1) {
            prefix = old.substring(start);
            filename = filename.concat(prefix);
        }
        return filename;
    }

    private void resolvePath(String filepath) {
        File path = new File(filepath);
        if (!path.exists() || !path.isDirectory()) {
            //noinspection ResultOfMethodCallIgnored
            path.mkdirs();
        }
    }
}
