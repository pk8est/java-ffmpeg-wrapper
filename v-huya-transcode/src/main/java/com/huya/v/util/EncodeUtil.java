package com.huya.v.util;

import com.huya.v.preset.Preset;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2017/1/10.
 */
public class EncodeUtil {


    public static Preset getPreSet(String presetName){
        Preset preset = SpringContextService.getBean("preset" + presetName);
        return preset;
    }

    public static Map<String, Preset> getPreSets(String defaultPreset){
        return getPreSets(getDefaultPreSets(defaultPreset));
    }

    public static Map<String, Preset> getPreSets(String[] presets){
        Map<String, Preset> map = new HashMap<>();
        for (String presetName : presets){
            map.put(presetName, getPreSet(presetName));
        }
        return map;
    }

    public static String[] getDefaultPreSets(String defaultPreset){
        return defaultPreset.split(",");
    }

    public static boolean isDefaultPreSets(String presetName, String defaultPreset){
        return isDefaultPreSets(presetName, getDefaultPreSets(defaultPreset));
    }

    public static boolean isDefaultPreSets(String presetName, String[] presets){
        return Arrays.asList(presets).contains(presetName);
    }

}
