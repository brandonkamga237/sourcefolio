# bug-fixer memory

Fragile areas and how to reproduce things here. One line per fact.

- Lifecycle delays depend on time: reproduce with the fixed `Clock`, never with `Thread.sleep`
- Jobs are enqueued after commit: a test without a transaction sees the enqueue immediately, a real request only after commit
