/*
 */
package com.artezio.recovery.server.data.types;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Алёна
 */
public class PauseConfig implements Serializable {

    public static final String PAUSE_GROUP_REGEX = "(?:(\\d{1,5})\\s*:{1}\\s*)(?:(\\d{1,15})\\s*;?\\s*)";
    public static final String PAUSE_RULE_REGEX = "(?:" + PAUSE_GROUP_REGEX + ")+";
    public static final Pattern PAUSE_RULE_PATTERN = Pattern.compile(PAUSE_RULE_REGEX);
    public static final Pattern PAUSE_GROUP_PATTERN = Pattern.compile(PAUSE_GROUP_REGEX);

    private String rule;
    private Map<Integer, Integer> map;
    private List<Integer> sortedKeys;

    public PauseConfig(String rule) throws RecoveryException {
        if (!checkRule(rule)) {
            throw new RecoveryException(String.format(
                    "The pause rule '%s' does not match '%s'!",
                    rule, PAUSE_RULE_REGEX));
        }
        initRule(rule);
    }

    public String getRule() {
        return rule;
    }

    public static boolean checkRule(String rule) {
        boolean result = false;
        if (rule != null) {
            Matcher matcher = PAUSE_RULE_PATTERN.matcher(rule);
            result = matcher.matches();
        }
        return result;
    }

    private void initRule(String rule) {
        this.rule = rule;
        map = new LinkedHashMap<>();
        Matcher m2 = PauseConfig.PAUSE_GROUP_PATTERN.matcher(rule);
        while (m2.find()) {
            Integer key = Integer.parseInt(m2.group(1));
            Integer value = Integer.parseInt(m2.group(2));
            map.put(key, value);
        }
        sortedKeys = new LinkedList<>(map.keySet());
        Collections.sort(sortedKeys);
    }

    public int getInterval(int count) {
        int i = 0;
        if (rule != null && !rule.isEmpty()
                && map != null && sortedKeys != null) {
            for (Integer key : sortedKeys) {
                if (count >= key) {
                    i = map.get(key);
                }
            }
        }
        return i;
    }
}
