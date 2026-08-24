EVAL-01
Redis empty + DB healthy
Expected:
correct response
cache populated

EVAL-02
Redis hit
Expected:
DB query count = 0

EVAL-03
Redis unavailable
Expected:
correct DB response

EVAL-04
Update conversation
Expected:
next GET returns updated value

EVAL-05
Malformed cached value
Expected:
no 500
DB fallback succeeds