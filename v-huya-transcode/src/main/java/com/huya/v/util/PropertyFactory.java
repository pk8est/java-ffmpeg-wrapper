/**
 * 
 */
package com.huya.v.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Properties;

/**
 * 扩展spring的PropertyPlaceholderConfigurer类，实现读取*.properties文件中的值到PropertiesHolder，方便其他代码中读取
 */
public class PropertyFactory extends PropertyPlaceholderConfigurer{
    
    protected void processProperties(ConfigurableListableBeanFactory beanFactoryToProcess,
            Properties props) throws BeansException {
        logger.info("开始处理属性……");
        this.initPropertiesHolder(props);
        super.processProperties(beanFactoryToProcess, props);
    }
    
    protected Properties loadProperty(File pptFile){
        Properties p = new Properties();
        try {
            p.load(new FileInputStream(pptFile));
        } catch (FileNotFoundException e) {
            logger.error("未找到文件："+pptFile.getName(), e);
        } catch (IOException e) {
            logger.error("读取文件时出现IO异常！", e);
        }
        return p;
    }
    
    protected void initPropertiesHolder(Properties props){
        if(props==null)
            return;
        
        Properties newProps = new Properties();
        for(Entry<Object, Object> entry: props.entrySet()){
            String key = (String) entry.getKey();
            String value = (String) entry.getValue();
            if(newProps.containsKey(key)){
                logger.warn("Properties中存在有Key为\"" + key + "\"的键值对，系统将复盖存在的键值对！" );
            }
            if(value.indexOf(DEFAULT_PLACEHOLDER_PREFIX)==-1){
                newProps.put(key, value);
            }else{
                newProps.put(key, parseStringValue(value, props, new HashSet<String>()));
            }
        }
        new PropertiesHolder(newProps);
    }
    
    /**
     * 属性持有器
     *
     */
    public static class PropertiesHolder{
        private final static Properties propsContainer = new Properties();
        protected final Log logger = LogFactory.getLog(getClass());
        private PropertiesHolder(Properties props){
            logger.info("初始化加载PropertiesHolder容器内容");
            PropertiesHolder.propsContainer.putAll(props);
        }
        public static String get(String key){
            return PropertiesHolder.propsContainer.getProperty(key);
        }
        public static String get(String key, String defaultValue){
            return PropertiesHolder.propsContainer.getProperty(key, defaultValue);
        }
    }
    
}
