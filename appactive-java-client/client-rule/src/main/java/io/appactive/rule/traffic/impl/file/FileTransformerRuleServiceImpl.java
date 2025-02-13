/*
 * Copyright 1999-2022 Alibaba Group Holding Ltd.
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

package io.appactive.rule.traffic.impl.file;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

import io.appactive.channel.file.FileReadDataSource;
import io.appactive.java.api.base.exception.ExceptionFactory;
import io.appactive.java.api.channel.ConverterInterface;
import io.appactive.java.api.rule.traffic.TransformerRuleService;
import io.appactive.java.api.utils.lang.StringUtils;
import io.appactive.rule.utils.FilePathUtil;
import io.appactive.support.log.LogUtil;
import io.appactive.rule.base.file.FileConstant;
import io.appactive.rule.traffic.bo.TransformerRuleBO;

public class FileTransformerRuleServiceImpl implements TransformerRuleService {

    private TransformerRuleBO transformerRuleBO;

    public FileTransformerRuleServiceImpl() {
        initFromFile(FilePathUtil.getTransformerRulePath());
    }

    public FileTransformerRuleServiceImpl(String filePath) {
        initFromFile(filePath);
    }

    @Override
    public String getRouteIdAfterTransformer(String routeId){
        if (StringUtils.isBlank(routeId)){
            return routeId;
        }
        Long mod = transformerRuleBO.getMod();
        if (mod == null){
            return routeId;
        }
        if (StringUtils.isNotNumbers(routeId)){
            throw ExceptionFactory.makeFault("sourceRouteId is not number");
        }
        Long sourceLangValue = Long.valueOf(routeId);

        Long tokenValue = sourceLangValue % mod;
        return String.valueOf(tokenValue);
    }


    private void initFromFile(String filePath) {
        if (StringUtils.isBlank(filePath)) {
            throw ExceptionFactory.makeFault("filePath is empty");
        }
        ConverterInterface<String, TransformerRuleBO> converterInterface = source -> JSON.parseObject(source,
            new TypeReference<TransformerRuleBO>() {});
        FileReadDataSource<TransformerRuleBO> fileReadDataSource = new FileReadDataSource<>(filePath,
            FileConstant.DEFAULT_CHARSET, FileConstant.DEFAULT_BUF_SIZE, converterInterface);
        try {
            transformerRuleBO = fileReadDataSource.read();
        } catch (Exception e) {
            String msg = "read file failed,e" + e.getMessage();
            LogUtil.error(msg,e);
            throw ExceptionFactory.makeFault(msg);
        }
    }
}
