EVAL-01
Conversation create succeeds
Expected:
conversation row = 1
outbox row = 1

EVAL-02
Kafka unavailable
Expected:
conversation persists
outbox remains unpublished

EVAL-03
Kafka recovery
Expected:
previous event eventually published

EVAL-04
publisher crash after Kafka send
Expected:
duplicate may occur
consumer remains correct

EVAL-05
same eventId processed twice
Expected:
one logical downstream side effect

