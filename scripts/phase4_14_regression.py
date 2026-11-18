from pathlib import Path
root=Path(__file__).resolve().parents[1]
base=root/'services/exam-service/src/main'
s=(base/'java/org/medlearn/exam/ProgressOutboxDelivery.java').read_text()
t=(root/'services/exam-service/src/test/java/org/medlearn/exam/OutboxLifecyclePostgresTest.java').read_text()
assert 'FOR UPDATE SKIP LOCKED' in s
assert 'claim_token=gen_random_uuid()' in s
assert 'AND claim_token=?' in s
assert 'claim_token=NULL' in s
assert 'dead_lettered_at' in s
assert 'retry_count>=12' in s
assert 'make_interval' in s
assert 'assertEquals(0,delivery.succeeded(id,first.claimToken()))' in t
assert 'assertTrue(delivery.claim(1).isEmpty())' in t
assert (base/'resources/db/migration/V4__outbox_claim_fencing.sql').exists()
print('PASS: 10 Phase 4.14 source assertions')
