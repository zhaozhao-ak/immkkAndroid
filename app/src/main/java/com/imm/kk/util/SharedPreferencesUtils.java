/**
 *
 */
package com.imm.kk.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;


import com.imm.kk.ui.X5WebViewActivity;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * SharedPreferences 工具类；方便保存数据
 *
 * @author zhuangzeqin
 */
public class SharedPreferencesUtils {

    public static final String HTTP_URL = "http_url";


    private SharedPreferencesUtils() {
        throw new UnsupportedOperationException("SharedPreferencesUtils cannot be instantiated");
    }

    /**
     * 保存在手机里面的文件名
     */
    private static final String FILE_NAME = "setting_data";

    /**
     * 保存数据的方法，我们需要拿到保存数据的具体类型，然后根据类型调用不同的保存方法
     *
     * @param context
     * @param key
     * @param object
     */
    public static void put(Context context, String key, Object object) {

        SharedPreferences sp = context.getSharedPreferences(FILE_NAME,
                Context.MODE_PRIVATE);

        Editor editor = sp.edit();

        if (object instanceof String) {
            editor.putString(key, (String) object);

        } else if (object instanceof Integer) {
            editor.putInt(key, (Integer) object);

        } else if (object instanceof Boolean) {
            editor.putBoolean(key, (Boolean) object);

        } else if (object instanceof Float) {
            editor.putFloat(key, (Float) object);

        } else if (object instanceof Long) {
            editor.putLong(key, (Long) object);

        } else {
            editor.putString(key, object.toString());
        }
        SharedPreferencesCompat.apply(editor);
    }

    public static String getHttpUrl(Context context) {
        SharedPreferences sp = context.getSharedPreferences(FILE_NAME,
                Context.MODE_PRIVATE);
        return sp.getString(HTTP_URL, X5WebViewActivity.HOME_URL);
    }

    /**
     * 得到保存数据的方法，我们根据默认值得到保存的数据的具体类型，然后调用相对于的方法获取值
     *
     * @param context
     * @param key
     * @param defaultObject
     * @return
     */
    public static Object get(Context context, String key, Object defaultObject) {
        SharedPreferences sp = context.getSharedPreferences(FILE_NAME,
                Context.MODE_PRIVATE);

        if (defaultObject instanceof String) {
            return sp.getString(key, (String) defaultObject);

        } else if (defaultObject instanceof Integer) {
            return sp.getInt(key, (Integer) defaultObject);

        } else if (defaultObject instanceof Boolean) {
            return sp.getBoolean(key, (Boolean) defaultObject);

        } else if (defaultObject instanceof Float) {
            return sp.getFloat(key, (Float) defaultObject);

        } else if (defaultObject instanceof Long) {
            return sp.getLong(key, (Long) defaultObject);
        }

        return null;
    }



    /**
     * 创建一个解决SharedPreferencesCompat.apply方法的一个兼容类
     *
     * @author zhy
     */
    private static class SharedPreferencesCompat {
        private static final Method sApplyMethod = findApplyMethod();

        /**
         * 反射查找apply的方法
         *
         * @return
         */
        @SuppressWarnings({"unchecked", "rawtypes"})
        private static Method findApplyMethod() {
            try {
                Class clz = Editor.class;
                return clz.getMethod("apply");
            } catch (NoSuchMethodException e) {
            }

            return null;
        }

        /**
         * 如果找到则使用apply执行，否则使用commit
         *
         * @param editor
         */
        public static void apply(Editor editor) {
            try {
                if (sApplyMethod != null) {
                    sApplyMethod.invoke(editor);

                    return;
                }
            } catch (IllegalArgumentException e) {
            } catch (IllegalAccessException e) {
            } catch (InvocationTargetException e) {
            }
            editor.commit();
        }
    }

}
