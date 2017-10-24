package com.autocomplete.model;

import com.autocomplete.coordination.commands.PutCommand;
import com.autocomplete.coordination.queries.GetWithPrefixQuery;
import com.autocomplete.util.trie.Trie;
import com.google.common.collect.Lists;
import io.atomix.copycat.Query;
import io.atomix.copycat.server.Commit;
import io.atomix.copycat.server.StateMachine;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

//import java.util;
public class Autocompleter extends StateMachine {
    private Trie<Character> trie = new Trie<>();
    private Map<String, Integer> weights = new HashMap<>();

    public boolean add(Commit<PutCommand> putCommand) {
        try {
            if (trie.add(Lists.charactersOf(putCommand.command().getString()))) {
                weights.put(putCommand.command().getString(), putCommand.command().getWeight());
                return true;
            }

            return false;
        }
        finally {
            putCommand.close();
        }
    }

    public String[] getWithPrefix(Commit<GetWithPrefixQuery> prefixQuery) {
        try {
            Set<List<Character>> charSets = trie.getKeysWithPrefix(Lists.charactersOf(prefixQuery.command().getPrefix()));
            List<String> leaves = charSets
                    .stream()
                    .map(this::stringFromCharList)
                    .sorted((a, b) -> weights.get(b) - weights.get(a))
                    .collect(Collectors.toList());
            return leaves.toArray(new String[leaves.size()]);
        }
        finally {
            prefixQuery.close();
        }
    }

    private String stringFromCharList(List<Character> chars) {
        StringBuilder sb = new StringBuilder();
        chars.forEach(sb::append);
        return sb.toString();
    }
}
