# architecture-reviewer memory

Index of what past reviews established. One line per fact; details in topic files beside this one.

## Known debt (do not report unless a change makes it worse)

## Look for first
- A JPA entity, Spring Data `Page` or repository used outside `infra/persistence`
- A lazy association touched while mapping a list (hidden N+1)
- A network call (GitHub, portfolio, S3, SMTP) inside a transactional use case instead of a job
- A new foreign key or filtered column without an index in the same migration
