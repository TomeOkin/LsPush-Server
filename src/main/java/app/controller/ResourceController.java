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
package app.controller;

import app.config.ResultCode;
import app.data.model.BaseResponse;
import app.data.model.Response;
import app.data.model.internal.FileResource;
import app.data.model.internal.StringResource;
import app.service.ResourceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

@RestController
@RequestMapping("/api/resource")
public class ResourceController {
    private static final Logger logger = LoggerFactory.getLogger(ResourceController.class);

    private final ResourceService resourceService;

    @Autowired
    public ResourceController(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    /**
     * resourceType: 1: avatar，2：other
     */
    @PostMapping(value = "/upload/{resourceType:.*}")
    public Response<String> upload(@RequestParam("file") MultipartFile file, @PathVariable int resourceType) {
        logger.info("---------------upload resource-----------------");
        StringResource resource = new StringResource();
        int result = resourceService.upload(file, resourceType, resource);
        return ResultCode.get(result, "Upload file failure", resource.filename);
    }

    @GetMapping(value = "/download/{resourceType:.*}")
    public void download(HttpServletRequest request, HttpServletResponse response, @RequestParam String filename,
        @PathVariable int resourceType)
        throws IOException {
        logger.info("---------------download resource-----------------");
        FileResource resource = new FileResource();
        int result = resourceService.download(filename, resourceType, resource);
        if (result != BaseResponse.COMMON_SUCCESS) {
            throw new ResourceNotFoundException();
        }
        File file = resource.file;

        // get MIME type of the file
        ServletContext context = request.getServletContext();
        String mimeType = context.getMimeType(filename);
        if (mimeType == null) {
            mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }

        response.setContentType(mimeType);
        response.setContentLength((int) file.length());
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename);

        BufferedOutputStream out = new BufferedOutputStream(response.getOutputStream());
        FileCopyUtils.copy(new BufferedInputStream(new FileInputStream(file)), out);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    static class ResourceNotFoundException extends RuntimeException {
    }
}
