package utils.serializers;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

public class ClassToPropertiesConverter
{
    public static Properties classToProperties(Object obj)
    {
        Coder converter = new Coder();
        return converter.toProperties(obj);
    }

    public static Object classFromProperties(Properties prop)
    {
        Decoder converter = new Decoder();
        return converter.fromProperties(prop);
    }

    protected static List<Field> buildFieldsList(Class<?> clazz)
    {
        ArrayList<Field> fieldsList = new ArrayList<>();
        Class<?> currentClass = clazz;
        while (currentClass != null)
        {
            Field [] fields = currentClass.getDeclaredFields();
            fieldsList.addAll(Arrays.asList(fields));
            currentClass = currentClass.getSuperclass();
        }

        Iterator<Field> fields = fieldsList.iterator();
        while (fields.hasNext())
        {
            Field field = fields.next();
            if (Modifier.isStatic(field.getModifiers()))
            {
                fields.remove();
            }
//            else if (Modifier.isTransient(field.getModifiers()))
//            {
//                fields.remove();
//            }
            if (field.getName().startsWith("class$"))
            {
                fields.remove();
            }
        }
        return fieldsList;
    }


    protected static class Coder
    {
        private Properties m_propData;
        private int m_iClassCount;
        private int m_iArrayCount;
        private HashMap<IdentityKeyWrapper, String> m_hsProcessedObjects;


        protected Properties toProperties(Object obj)
        {
            m_propData = new Properties();
            m_iClassCount = 0;
            m_iArrayCount = 0;
            m_hsProcessedObjects = new HashMap<IdentityKeyWrapper, String>();
            String strMainObjectID = storeObject(obj);
            m_propData.put("mainObjectID", String.valueOf(strMainObjectID));
            return m_propData;
        }

        private String storeObject(Object obj)
        {
            if (obj == null)
            {
                return null;
            }
            String strResult;
            Class<?> clazz = obj.getClass();
            if (clazz.equals(String.class))
            {
                strResult = (String)obj;
            }
            else if (clazz.isArray())
            {
                strResult = storeArrayImpl(obj);
            }
            else
            {
                strResult = storeObjectImpl(obj);
            }
            return strResult;
        }

        private String storeObjectImpl(Object obj)
        {
            IdentityKeyWrapper key = new IdentityKeyWrapper(obj);

            String objID = m_hsProcessedObjects.get(key);
            if (objID != null)
            {
                return objID;
            }
            m_iClassCount++;
            String strResult = "o"+m_iClassCount;

            m_hsProcessedObjects.put(key, strResult);

            String strKeyPrefix = strResult+".";
            String strMetadataPrefix = strKeyPrefix+"m.";
            String strFieldPrefix = strKeyPrefix+"f.";

            Class<?> clazz = obj.getClass();
            m_propData.put(strKeyPrefix+"className", clazz.getName());

            List<Field> fieldsList = buildFieldsList(clazz);

            for (Field field : fieldsList)
            {
                Class<?> fieldType = field.getType();
                String strFieldName = field.getName();

                String strFieldValue = "";
                try
                {
                    try
                    {
                        field.setAccessible(true);
                    }
                    catch (SecurityException ex)
                    {
                        System.out.println(
                                "Security exception / Ошибка безопасности:"+ex.getMessage());
                    }
                    if (fieldType.equals(Integer.TYPE))
                    {
                        strFieldValue = String.valueOf(field.getInt(obj));
                    }
                    else if (fieldType.equals(Character.TYPE))
                    {
                        strFieldValue = String.valueOf(field.getChar(obj));
                    }
                    else if (fieldType.equals(Byte.TYPE))
                    {
                        strFieldValue = String.valueOf(field.getByte(obj));
                    }
                    else if (fieldType.equals(Short.TYPE))
                    {
                        strFieldValue = String.valueOf(field.getShort(obj));
                    }
                    else if (fieldType.equals(Long.TYPE))
                    {
                        strFieldValue = String.valueOf(field.getLong(obj));
                    }
                    else if (fieldType.equals(Boolean.TYPE))
                    {
                        strFieldValue = String.valueOf(field.getBoolean(obj));
                    }
                    else if (fieldType.equals(Double.TYPE))
                    {
                        strFieldValue = String.valueOf(field.getDouble(obj));
                    }
                    else if (fieldType.equals(Float.TYPE))
                    {
                        strFieldValue = String.valueOf(field.getFloat(obj));
                    }
                    else
                    {
                        strFieldValue = storeObject(field.get(obj));
                    }
                }
                catch (IllegalAccessException ex)
                {
                    System.out.println("No access to field / Нет доступа к полю:"+ex.getMessage());
                    strFieldValue = null;
                }
                if (strFieldValue != null)
                {
                    m_propData.setProperty(strFieldPrefix+strFieldName, String.valueOf(strFieldValue));
                }
                else
                {
                    m_propData.setProperty(strFieldPrefix+strFieldName+".isNull", "true");
                }
            }

            return strResult;
        }

        private String storeArrayImpl(Object obj)
        {
            if (obj == null)
            {
                return null;
            }
            IdentityKeyWrapper key = new IdentityKeyWrapper(obj);

            String objID = m_hsProcessedObjects.get(key);
            if (objID != null)
            {
                return objID;
            }
            m_iArrayCount++;
            String strResult = "a"+m_iArrayCount;

            m_hsProcessedObjects.put(key, strResult);

            Class<?> clazz = obj.getClass();
            Class<?> fieldType = clazz.getComponentType();

            String strKeyPrefix = strResult+".";
            String strMetadataPrefix = strKeyPrefix+"m.";
            String strFieldPrefix = strKeyPrefix+"f.";

            int iLength = Array.getLength(obj);
            m_propData.put(strKeyPrefix+"className", fieldType.getName());
            m_propData.put(strKeyPrefix+"length", String.valueOf(iLength));

            String strFieldValue = "";
            for (int iIdx = 0; iIdx < iLength; iIdx++)
            {
                if (fieldType.equals(Integer.TYPE))
                {
                    strFieldValue = String.valueOf(Array.getInt(obj, iIdx));
                }
                else if (fieldType.equals(Character.TYPE))
                {
                    strFieldValue = String.valueOf(Array.getChar(obj, iIdx));
                }
                else if (fieldType.equals(Byte.TYPE))
                {
                    strFieldValue = String.valueOf(Array.getByte(obj, iIdx));
                }
                else if (fieldType.equals(Short.TYPE))
                {
                    strFieldValue = String.valueOf(Array.getShort(obj, iIdx));
                }
                else if (fieldType.equals(Long.TYPE))
                {
                    strFieldValue = String.valueOf(Array.getLong(obj, iIdx));
                }
                else if (fieldType.equals(Boolean.TYPE))
                {
                    strFieldValue = String.valueOf(Array.getBoolean(obj, iIdx));
                }
                else if (fieldType.equals(Double.TYPE))
                {
                    strFieldValue = String.valueOf(Array.getDouble(obj, iIdx));
                }
                else if (fieldType.equals(Float.TYPE))
                {
                    strFieldValue = String.valueOf(Array.getFloat(obj, iIdx));
                }
                else
                {
                    Object value = Array.get(obj, iIdx);
                    strFieldValue = storeObject(value);
                }
                String strFieldName = ""+iIdx;
                if (strFieldValue != null)
                {
                    m_propData.setProperty(strFieldPrefix+strFieldName, String.valueOf(strFieldValue));
                }
                else
                {
                    m_propData.setProperty(strFieldPrefix+strFieldName+".isNull", "true");
                }
            }

            return strResult;
        }

    }

    protected static class Decoder
    {
        private Properties m_propData;
        private HashMap<String, Object> m_hsProcessedObjects;

        protected Object fromProperties(Properties prop)
        {
            m_propData = prop;
            m_hsProcessedObjects = new HashMap<String, Object>();
            String strMainObjectID = (String)m_propData.get("mainObjectID");
            return restoreObject(strMainObjectID);
        }

        private Object restoreObject(String strValue)
        {
            if (strValue == null)
            {
                return null;
            }
            Object result;
            if (strValue.startsWith("a"))
            {
                result = restoreArrayImpl(strValue);
            }
            else if (strValue.startsWith("o"))
            {
                result = restoreObjectImpl(strValue);
            }
            else
            {
                result = strValue;
            }
            return result;
        }


        private Object restoreObjectImpl(String strID)
        {
            Object result = m_hsProcessedObjects.get(strID);
            if (result == null)
            {
                try
                {
                    String strKeyPrefix = strID+".";
                    String strMetadataPrefix = strKeyPrefix+"m.";
                    String strFieldPrefix = strKeyPrefix+"f.";

                    String strClassType = (String) m_propData.get(strKeyPrefix+"className");

                    Class<?> clazz = Class.forName(strClassType);
                    Constructor<?> constr = clazz.getDeclaredConstructor(new Class[0]);

                    try
                    {
                        constr.setAccessible(true);
                    }
                    catch (SecurityException ex)
                    {
                        System.out.println(
                                "Security exception / Ошибка безопасности:"+ex.getMessage());
                    }

                    result = constr.newInstance();
                    m_hsProcessedObjects.put(strID, result);

                    List<Field> fieldsList = buildFieldsList(clazz);

                    for (Field field : fieldsList)
                    {
                        Class<?> fieldType = field.getType();
                        String strFieldName = field.getName();
                        String strFieldValue = m_propData.getProperty(strFieldPrefix+strFieldName);
                        boolean bIsNull =
                                Boolean.parseBoolean(m_propData.getProperty(strFieldPrefix+strFieldName+".isNull", "false"));

                        try
                        {
                            try
                            {
                                field.setAccessible(true);
                            }
                            catch (SecurityException ex)
                            {
                                System.out.println(
                                        "Security exception / Ошибка безопасности:"+ex.getMessage());
                            }
                            if (fieldType.equals(Integer.TYPE))
                            {
                                field.setInt(result, Integer.parseInt(strFieldValue));
                            }
                            else if (fieldType.equals(Character.TYPE))
                            {
                                field.setChar(result, strFieldValue.charAt(0));
                            }
                            else if (fieldType.equals(Byte.TYPE))
                            {
                                field.setByte(result, Byte.parseByte(strFieldValue));
                            }
                            else if (fieldType.equals(Short.TYPE))
                            {
                                field.setShort(result, Short.parseShort(strFieldValue));
                            }
                            else if (fieldType.equals(Long.TYPE))
                            {
                                field.setLong(result, Long.parseLong(strFieldValue));
                            }
                            else if (fieldType.equals(Boolean.TYPE))
                            {
                                field.setBoolean(result, Boolean.parseBoolean(strFieldValue));
                            }
                            else if (fieldType.equals(Double.TYPE))
                            {
                                field.setDouble(result, Double.parseDouble(strFieldValue));
                            }
                            else if (fieldType.equals(Float.TYPE))
                            {
                                field.setFloat(result, Float.parseFloat(strFieldValue));
                            }
                            else
                            {
                                if (bIsNull)
                                {
                                    field.set(result, null);
                                }
                                else
                                {
                                    field.set(result, restoreObject(strFieldValue));
                                }
                            }
                        }
                        catch (IllegalAccessException ex)
                        {
                            System.out.println("No access to field / Нет доступа к полю:"+ex.getMessage());
                        }
                    }

                }
                catch (Exception ex)
                {
                    System.out.println("Can not restore class / Не удается восстановить класс:"+ex.getMessage());
                    result = null;
                }
            }
            return result;
        }

        private Object restoreArrayImpl(String strID)
        {
            Object result = m_hsProcessedObjects.get(strID);
            if (result == null)
            {

                try
                {
                    String strKeyPrefix = strID+".";
                    String strMetadataPrefix = strKeyPrefix+"m.";
                    String strFieldPrefix = strKeyPrefix+"f.";

                    String strComponentType = (String) m_propData.get(strKeyPrefix+"className");
                    int iLength = Integer.parseInt(
                            (String)m_propData.get(strKeyPrefix+"length"));

                    Class<?> fieldType;
                    if (Integer.TYPE.getName().equals(strComponentType))
                    {
                        fieldType = Integer.TYPE;
                    }
                    else if (Character.TYPE.getName().equals(strComponentType))
                    {
                        fieldType = Character.TYPE;
                    }
                    else if (Byte.TYPE.getName().equals(strComponentType))
                    {
                        fieldType = Byte.TYPE;
                    }
                    else if (Short.TYPE.getName().equals(strComponentType))
                    {
                        fieldType = Short.TYPE;
                    }
                    else if (Long.TYPE.getName().equals(strComponentType))
                    {
                        fieldType = Long.TYPE;
                    }
                    else if (Boolean.TYPE.getName().equals(strComponentType))
                    {
                        fieldType = Boolean.TYPE;
                    }
                    else if (Double.TYPE.getName().equals(strComponentType))
                    {
                        fieldType = Double.TYPE;
                    }
                    else if (Float.TYPE.getName().equals(strComponentType))
                    {
                        fieldType = Float.TYPE;
                    }
                    else
                    {
                        fieldType = Class.forName(strComponentType);
                    }
                    result = Array.newInstance(fieldType, iLength);
                    m_hsProcessedObjects.put(strID, result);

                    for (int iIdx = 0; iIdx < iLength; iIdx++)
                    {
                        String strFieldName = ""+iIdx;
                        String strFieldValue = m_propData.getProperty(strFieldPrefix+strFieldName);
                        boolean bIsNull =
                                Boolean.parseBoolean(m_propData.getProperty(strFieldPrefix+strFieldName+".isNull", "false"));

                        if (fieldType.equals(Integer.TYPE))
                        {
                            Array.setInt(result, iIdx, Integer.parseInt(strFieldValue));
                        }
                        else if (fieldType.equals(Character.TYPE))
                        {
                            Array.setChar(result, iIdx, strFieldValue.charAt(0));
                        }
                        else if (fieldType.equals(Byte.TYPE))
                        {
                            Array.setByte(result, iIdx, Byte.parseByte(strFieldValue));
                        }
                        else if (fieldType.equals(Short.TYPE))
                        {
                            Array.setShort(result, iIdx, Short.parseShort(strFieldValue));
                        }
                        else if (fieldType.equals(Long.TYPE))
                        {
                            Array.setLong(result, iIdx, Long.parseLong(strFieldValue));
                        }
                        else if (fieldType.equals(Boolean.TYPE))
                        {
                            Array.setBoolean(result, iIdx, Boolean.parseBoolean(strFieldValue));
                        }
                        else if (fieldType.equals(Double.TYPE))
                        {
                            Array.setDouble(result, iIdx, Double.parseDouble(strFieldValue));
                        }
                        else if (fieldType.equals(Float.TYPE))
                        {
                            Array.setDouble(result, iIdx, Float.parseFloat(strFieldValue));
                        }
                        else
                        {
                            if (bIsNull)
                            {
                                Array.set(result, iIdx, null);
                            }
                            else
                            {
                                Array.set(result, iIdx, restoreObject(strFieldValue));

                            }
                        }
                    }
                }
                catch (Exception ex)
                {
                    System.out.println("Cannot restore array / Не удается восстановить массив:"+ex.getMessage());
                    result = null;
                }
            }
            return result;
        }
    }


    static class IdentityKeyWrapper
    {
        private Object m_key;

        IdentityKeyWrapper(Object key)
        {
            m_key = key;
        }

        public Object getKey()
        {
            return m_key;
        }

        public boolean equals(Object obj)
        {
            if (obj instanceof IdentityKeyWrapper)
            {
                return ((IdentityKeyWrapper)obj).m_key == m_key;
            }
            return false;
        }

        public int hashCode()
        {
            return System.identityHashCode(m_key);
        }
    }

}
