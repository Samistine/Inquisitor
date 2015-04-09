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

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.PatternSyntaxException;

/**
 *
 * @author frdfsnlght <frdfsnlght@gmail.com>
 */
public final class Options {

    private Object target;
    private Set<String> names;
    private String basePerm;
    private OptionsListener listener = null;

    public Options(Object target, Set<String> names, String basePerm, OptionsListener listener) {
        this.target = target;
        this.names = names;
        this.basePerm = basePerm;
        this.listener = listener;
    }

    public void getOptions(Context ctx, String option) throws OptionsException, PermissionsException {
        List<String> options = new ArrayList<String>();
        String opt = resolveOption(option);
        if (opt != null)
            options.add(opt);
        else {
            option = option.replaceAll("(^|[^\\.])\\*", ".*");
            for (String o : names)
                try {
                    if (o.matches(option))
                        options.add(o);
                } catch (PatternSyntaxException e) {}
            if (options.isEmpty())
                throw new OptionsException("no options match");
        }
        Collections.sort(options);
        for (String o : options) {
            try {
                ctx.send("%s=%s", o, getOption(ctx, o));
            } catch (PermissionsException e) {}
        }
    }

    private String resolveOption(String option) throws OptionsException {
        // look for literal match
        for (String opt : names) {
            if (opt.toLowerCase().equals(option.toLowerCase()))
                return opt;
        }
        // look for starting match
        String matched = null;
        for (String opt : names) {
            if (opt.toLowerCase().startsWith(option.toLowerCase())) {
                if (matched != null)
                    throw new OptionsException("option is ambiguous");
                matched = opt;
            }
        }
        return matched;
    }

    public String getOption(Context ctx, String option) throws OptionsException, PermissionsException {
        option = resolveOption(option);
        if (option == null)
            throw new OptionsException("unknown option");
        Permissions.require(ctx.getPlayer(), basePerm + ".option.get." + listener.getOptionPermission(ctx, option));
        String methodName = "get" +
                option.substring(0, 1).toUpperCase() +
                option.substring(1);
        try {
            Class<?> cls;
            if (target instanceof Class)
                cls = (Class)target;
            else
                cls = target.getClass();
            Method m = cls.getMethod(methodName);
            Object value = m.invoke(target);
            if (value == null) return "";
            if (m.getReturnType().isArray()) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < Array.getLength(value); i++) {
                    Object v = Array.get(value, i);
                    if (v != null) {
                        if (sb.length() > 0) sb.append(',');
                        sb.append(v.toString());
                    }
                }
                value = sb;
            }
            return value.toString();
        } catch (InvocationTargetException ite) {
            throw new OptionsException(ite.getCause().getMessage());
        } catch (NoSuchMethodException nsme) {
            throw new OptionsException("invalid method '%s'", methodName);
        } catch (IllegalAccessException iae) {
            throw new OptionsException("unable to read the option");
        }
    }

    @SuppressWarnings("unchecked")
    public void setOption(Context ctx, String option, String value) throws OptionsException, PermissionsException {
        option = resolveOption(option);
        if (option == null)
            throw new OptionsException("unknown option");
        Permissions.require(ctx.getPlayer(), basePerm + ".option.set." + listener.getOptionPermission(ctx, option));
        String setMethodName = "set" +
                option.substring(0, 1).toUpperCase() +
                option.substring(1);
        String getMethodName = "get" +
                option.substring(0, 1).toUpperCase() +
                option.substring(1);
        try {
            Class<?> cls;
            if (target instanceof Class)
                cls = (Class)target;
            else
                cls = target.getClass();
            Method m = cls.getMethod(getMethodName);
            Class rCls = m.getReturnType();
            m = cls.getMethod(setMethodName, rCls);
            if (rCls.isArray()) {
                List<String> values = new ArrayList<String>();
                for (StringTokenizer token = new StringTokenizer(value, ","); token.hasMoreTokens(); ) {
                    String v = token.nextToken();
                    if ((v == null) || v.equals("")) continue;
                    values.add(v);
                }
                Object array = Array.newInstance(rCls.getComponentType(), values.size());
                for (int i = 0; i < values.size(); i++)
                    Array.set(array, i, convertValue(values.get(i), rCls.getComponentType()));
                m.invoke(target, array);
            } else
                m.invoke(target, convertValue(value, rCls));
            if (listener != null)
                listener.onOptionSet(ctx, option, getOption(ctx, option));

        } catch (InvocationTargetException ite) {
            if (ite.getCause().getMessage() == null)
                throw new OptionsException(ite.getCause().toString());
            else
                throw new OptionsException(ite.getCause().getMessage());
        } catch (NoSuchMethodException nsme) {
            throw new OptionsException("invalid method '%s'", setMethodName);
        } catch (IllegalAccessException iae) {
            throw new OptionsException("unable to set the option");
        }
    }

    @SuppressWarnings("unchecked")
    public void addOption(Context ctx, String option, String value) throws OptionsException, PermissionsException {
        option = resolveOption(option);
        if (option == null)
            throw new OptionsException("unknown option");
        Permissions.require(ctx.getPlayer(), basePerm + ".option.set." + listener.getOptionPermission(ctx, option));
        String addMethodName = "add" +
                option.substring(0, 1).toUpperCase() +
                option.substring(1);
        String getMethodName = "get" +
                option.substring(0, 1).toUpperCase() +
                option.substring(1);
        try {
            Class<?> cls;
            if (target instanceof Class)
                cls = (Class)target;
            else
                cls = target.getClass();
            Method m = cls.getMethod(getMethodName);
            Class rCls = m.getReturnType();
            if (! rCls.isArray())
                throw new OptionsException("option value is not an array");
            rCls = rCls.getComponentType();
            m = cls.getMethod(addMethodName, rCls);
            for (StringTokenizer token = new StringTokenizer(value, ","); token.hasMoreTokens(); ) {
                value = token.nextToken().trim();
                if (value.equals("")) continue;
                m.invoke(target, convertValue(value, rCls));
            }
            if (listener != null)
                listener.onOptionSet(ctx, option, getOption(ctx, option));

        } catch (InvocationTargetException ite) {
            if (ite.getCause().getMessage() == null)
                throw new OptionsException(ite.getCause().toString());
            else
                throw new OptionsException(ite.getCause().getMessage());
        } catch (NoSuchMethodException nsme) {
            throw new OptionsException("invalid method '%s'", addMethodName);
        } catch (IllegalAccessException iae) {
            throw new OptionsException("unable to add the option");
        }
    }

    @SuppressWarnings("unchecked")
    public void removeOption(Context ctx, String option, String value) throws OptionsException, PermissionsException {
        option = resolveOption(option);
        if (option == null)
            throw new OptionsException("unknown option");
        Permissions.require(ctx.getPlayer(), basePerm + ".option.set." + listener.getOptionPermission(ctx, option));
        String removeMethodName = "remove" +
                option.substring(0, 1).toUpperCase() +
                option.substring(1);
        String getMethodName = "get" +
                option.substring(0, 1).toUpperCase() +
                option.substring(1);
        try {
            Class<?> cls;
            if (target instanceof Class)
                cls = (Class)target;
            else
                cls = target.getClass();
            Method m = cls.getMethod(getMethodName);
            Class rCls = m.getReturnType();
            if (! rCls.isArray())
                throw new OptionsException("option value is not an array");
            rCls = rCls.getComponentType();
            m = cls.getMethod(removeMethodName, rCls);
            for (StringTokenizer token = new StringTokenizer(value, ","); token.hasMoreTokens(); ) {
                value = token.nextToken().trim();
                m.invoke(target, convertValue(value, rCls));
            }
            if (listener != null)
                listener.onOptionSet(ctx, option, getOption(ctx, option));

        } catch (InvocationTargetException ite) {
            if (ite.getCause().getMessage() == null)
                throw new OptionsException(ite.getCause().toString());
            else
                throw new OptionsException(ite.getCause().getMessage());
        } catch (NoSuchMethodException nsme) {
            throw new OptionsException("invalid method '%s'", removeMethodName);
        } catch (IllegalAccessException iae) {
            throw new OptionsException("unable to add the option");
        }
    }

    private Object convertValue(String value, Class type) throws OptionsException {
        if (type == Boolean.TYPE)
            return Boolean.parseBoolean(value);
        else if (type == Integer.TYPE)
            return Integer.parseInt(value);
        else if (type == Float.TYPE)
            return Float.parseFloat(value);
        else if (type == Double.TYPE)
            return Double.parseDouble(value);
        else if (type == String.class)
            return value;
        else if (type.isEnum())
            try {
                return Utils.valueOf(type, value);
            } catch (IllegalArgumentException iae) {
                throw new OptionsException(iae.getMessage() + " option value '%s'", value);
            }
        else
            throw new OptionsException("unsupported option type '%s'", type);
    }

}
