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

/**
 * Base class for all Transporter exceptions.
 * <p>
 * All Transporter exceptions support a format string that can use
 * optionally supplied arguments during exception creation. The
 * format string follows the same rules as the <code>java.lang.String.format</code>
 * method.
 *
 * @author frdfsnlght <frdfsnlght@gmail.com>
 */
public class InquisitorException extends java.lang.Exception {

    /**
     * Creates a new exception.
     *
     * @param msg   a format string
     * @param args  zero or more optional arguments used by the format string
     */
    public InquisitorException(String msg, Object ... args) {
        super(String.format(msg, args));
    }

}
