/*
 * Copyright 2012 frdfsnlght <frdfsnlght@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.frdfsnlght.inquisitor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author frdfsnlght <frdfsnlght@gmail.com>
 */


public final class WebRoute {

    private WebHandler handler;
    private Pattern pattern;
    private String[] paramNames;

    public WebRoute(WebHandler handler, String pattern, String ... paramNames) {
        this.handler = handler;
        this.pattern = Pattern.compile(pattern);
        this.paramNames = paramNames;
    }

    public boolean matches(WebRequest request, String uri) {
        Matcher matcher = pattern.matcher(uri);
        if (! matcher.matches()) return false;
        TypeMap params = new TypeMap();
        if ((paramNames != null) && (paramNames.length > 0) && (matcher.groupCount() <= paramNames.length))
            for (int i = 0; i < paramNames.length; i++) {
                if ((matcher.group(i + 1).length() == 0) && request.hasParameter(paramNames[i])) continue;
                params.put(paramNames[i], matcher.group(i + 1));
            }
        if (! handler.matchesRequest(request, params)) return false;
        request.setParameters(params);
        request.setHandler(handler);
        return true;
    }

}
