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
import lombok.Getter;

/**
 * Recovery delivery pause configuration helper class.
 * 
 * @author Olesia Shuliaeva <os.netbox@gmail.com>
 */
public class PauseConfig implements Serializable {

    /**
     * RegEx to split recovery pause in period groups.
     */
    public static final String PAUSE_GROUP_REGEX = "(?:(\\d{1,5})\\s*:{1}\\s*)(?:(\\d{1,15})\\s*;?\\s*)";
    /**
     * RegEx compiled pattern to split recovery pause in period groups.
     */
    public static final Pattern PAUSE_GROUP_PATTERN = Pattern.compile(PAUSE_GROUP_REGEX);
    /**
     * RegEx to check recovery pause format.
     */
    public static final String PAUSE_RULE_REGEX = "(?:" + PAUSE_GROUP_REGEX + ")+";
    /**
     * RegEx compiled pattern to check recovery pause format.
     */
    public static final Pattern PAUSE_RULE_PATTERN = Pattern.compile(PAUSE_RULE_REGEX);

    /**
     * Recovery delivery pause configuration.
     */
    @Getter
    private String rule;
    /**
     * Recovery pause period groups.
     */
    private Map<Integer, Integer> map;
    /**
     * Recovery pause sorted interval starts.
     */
    private List<Integer> sortedKeys;

    /**
     * Recovery delivery pause configuration helper.
     * 
     * @param rule Recovery delivery pause configuration.
     * @throws RecoveryException Recovery processing exception.
     */
    public PauseConfig(String rule) throws RecoveryException {
        if (!checkRule(rule)) {
            throw new RecoveryException(String.format(
                    "The pause rule '%s' does not match '%s'!",
                    rule, PAUSE_RULE_REGEX));
        }
        initRule(rule);
    }

    /**
     * Check recovery delivery pause configuration.
     * 
     * @param rule Recovery delivery pause configuration.
     * @return True if pause configuration is OK.
     */
    public static boolean checkRule(String rule) {
        boolean result = false;
        if (rule != null) {
            Matcher matcher = PAUSE_RULE_PATTERN.matcher(rule);
            result = matcher.matches();
        }
        return result;
    }

    /**
     * Prepare pause configuration for usage.
     * 
     * @param rule Recovery delivery pause configuration.
     */
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

    /**
     * Get waiting timeout for specific delivery try.
     * 
     * @param count Specific delivery try.
     * @return Waiting timeout.
     */
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
