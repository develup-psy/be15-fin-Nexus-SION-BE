package com.nexus.sion.feature.squad.command.domain.service;

import com.nexus.sion.feature.squad.query.dto.response.DeveloperSummary;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SquadCombinationGeneratorImpl {
    public List<Map<String, List<DeveloperSummary>>> generate(
            Map<String, List<DeveloperSummary>> candidates,
            Map<String, Integer> requiredCountByRole) {

        List<String> roles = new ArrayList<>(candidates.keySet());
        List<Map<String, List<DeveloperSummary>>> results = new ArrayList<>();
        backtrack(candidates, requiredCountByRole, roles, 0, new HashMap<>(), results);
        return results;
    }

    private void backtrack(
            Map<String, List<DeveloperSummary>> candidates,
            Map<String, Integer> requiredCountByRole,
            List<String> roles,
            int depth,
            Map<String, List<DeveloperSummary>> current,
            List<Map<String, List<DeveloperSummary>>> results) {

        if (depth == roles.size()) {
            results.add(deepCopy(current));
            return;
        }

        String currentRole = roles.get(depth);
        List<DeveloperSummary> devList = candidates.get(currentRole);
        int required = requiredCountByRole.getOrDefault(currentRole, 1);

        if (devList == null || devList.size() < required) return;

        List<List<DeveloperSummary>> combinations = combinations(devList, required);
        for (List<DeveloperSummary> selected : combinations) {
            current.put(currentRole, selected);
            backtrack(candidates, requiredCountByRole, roles, depth + 1, current, results);
            current.remove(currentRole);
        }
    }

    // 조합 구하기: n명 선택
    private List<List<DeveloperSummary>> combinations(List<DeveloperSummary> list, int k) {
        List<List<DeveloperSummary>> result = new ArrayList<>();
        combineHelper(list, 0, k, new ArrayList<>(), result);
        return result;
    }

    private void combineHelper(List<DeveloperSummary> list, int index, int k,
                               List<DeveloperSummary> current,
                               List<List<DeveloperSummary>> result) {
        if (current.size() == k) {
            result.add(new ArrayList<>(current));
            return;
        }

        for (int i = index; i < list.size(); i++) {
            current.add(list.get(i));
            combineHelper(list, i + 1, k, current, result);
            current.remove(current.size() - 1);
        }
    }

    private Map<String, List<DeveloperSummary>> deepCopy(Map<String, List<DeveloperSummary>> original) {
        Map<String, List<DeveloperSummary>> copy = new HashMap<>();
        for (Map.Entry<String, List<DeveloperSummary>> entry : original.entrySet()) {
            copy.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
        return copy;
    }
}
