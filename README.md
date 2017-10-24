# Autocomplete

A toy server cluster that exposes an autocomplete engine over HTTP.
It uses Raft for replication and a basic Trie as the stateful data structure.
It very naively supports the notion of a "weighted" entry to allow ordering.
*Definitely* use Elasticsearch or something like it instead of this.

### API

```
POST /autocomplete/{item}?weight={int}
-> 200 OK with no response body

> GET /autocomplete/{prefix}
-> 200 OK with body returns JSON list of strings with provided prefix
```
