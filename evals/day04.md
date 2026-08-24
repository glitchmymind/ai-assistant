EVAL-01
20 identical concurrent requests
Expected DB rows: 1

EVAL-02
retry after post-commit timeout
Expected DB rows: 1

EVAL-03
same key + same payload
Expected logical result: same conversation

EVAL-04
same key + different payload
Expected: documented behavior
