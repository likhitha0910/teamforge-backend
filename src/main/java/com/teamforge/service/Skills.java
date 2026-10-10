package com.teamforge.service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class Skills {
    private Skills() {}
    public static Set<String> parse(String s) {
        Set<String> r = new LinkedHashSet<>();
        if (s != null) for (String x : s.split(",")) { x = x.trim().toLowerCase(); if (!x.isEmpty()) r.add(x); }
        return r;
    }
    /** skills in a that also appear in b */
    public static List<String> overlap(String a, String b) {
        Set<String> sb = parse(b);
        return parse(a).stream().filter(sb::contains).toList();
    }
}
