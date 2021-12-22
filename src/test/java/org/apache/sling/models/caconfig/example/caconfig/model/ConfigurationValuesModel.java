/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.sling.models.caconfig.example.caconfig.model;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

/**
 * This model can be used to adapt configuration resource to a model.
 */
@Model(adaptables = Resource.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class ConfigurationValuesModel {

    @ValueMapValue
    private String stringParam;
    @ValueMapValue
    private int intParam;
    @ValueMapValue
    private boolean boolParam;

    public String getStringParam() {
        return stringParam;
    }
    public void setStringParam(String stringParam) {
        this.stringParam = stringParam;
    }
    public int getIntParam() {
        return intParam;
    }
    public void setIntParam(int intParam) {
        this.intParam = intParam;
    }
    public boolean isBoolParam() {
        return boolParam;
    }
    public void setBoolParam(boolean boolParam) {
        this.boolParam = boolParam;
    }

}
